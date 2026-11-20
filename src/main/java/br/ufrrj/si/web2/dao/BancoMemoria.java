package br.ufrrj.si.web2.dao;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

import br.ufrrj.si.web2.model.AtuacaoComissao;
import br.ufrrj.si.web2.model.AtuacaoComissao.PerfilComissao;
import br.ufrrj.si.web2.model.Discente;
import br.ufrrj.si.web2.model.MembroComissao;
import br.ufrrj.si.web2.model.OperacaoDiscente;
import br.ufrrj.si.web2.model.Solicitacao;
import br.ufrrj.si.web2.model.StatusSolicitacao;
import br.ufrrj.si.web2.model.TipoHoras;

/**
 * "Banco de dados" em memória do Entregável 2 (simula a persistência).
 * Singleton: todos os DAOs compartilham as mesmas listas enquanto o Tomcat estiver de pé.
 * Os dados somem ao reiniciar o servidor; no Entregável 3 isso passa para o MySQL.
 */
public final class BancoMemoria {

	private static final BancoMemoria INSTANCIA = new BancoMemoria();

	private final AtomicLong sequencia = new AtomicLong(100);
	private final List<Discente> discentes = new CopyOnWriteArrayList<>();
	private final List<MembroComissao> membros = new CopyOnWriteArrayList<>();
	private final List<Solicitacao> solicitacoes = new CopyOnWriteArrayList<>();

	private BancoMemoria() {
		carregarDadosIniciais();
	}

	public static BancoMemoria getInstancia() { return INSTANCIA; }

	public Long proximoId() { return sequencia.incrementAndGet(); }
	public List<Discente> getDiscentes() { return discentes; }
	public List<MembroComissao> getMembros() { return membros; }
	public List<Solicitacao> getSolicitacoes() { return solicitacoes; }

	// ---------------------------------------------------------------------
	// Dados de demonstração (contas "pré-cadastradas" exigidas no enunciado)
	// ---------------------------------------------------------------------
	private void carregarDadosIniciais() {
		LocalDate hoje = LocalDate.now();

		MembroComissao carlos = novoMembro("cmendes", "123456", "Prof. Carlos Mendes");
		novaAtuacao(carlos, PerfilComissao.COMISSAO_EXTENSAO, hoje.minusMonths(2), hoje.plusMonths(10));

		MembroComissao ana = novoMembro("alima", "123456", "Profa. Ana Beatriz Lima");
		novaAtuacao(ana, PerfilComissao.COMISSAO_COMPLEMENTAR, hoje.minusMonths(6), null);

		MembroComissao fernando = novoMembro("frocha", "123456", "Prof. Fernando Rocha");
		AtuacaoComissao atuacaoEstagio = novaAtuacao(fernando, PerfilComissao.COMISSAO_ESTAGIO, hoje.minusMonths(1), null);

		// Dinâmica de mandatos: já foi da comissão de extensão (encerrado), hoje é Coordenador
		MembroComissao tiago = novoMembro("tfranca", "admin123", "Prof. Tiago França");
		novaAtuacao(tiago, PerfilComissao.COMISSAO_EXTENSAO, hoje.minusYears(3), hoje.minusYears(1).minusDays(1));
		novaAtuacao(tiago, PerfilComissao.COORDENADOR, hoje.minusYears(1), null);

		MembroComissao marcos = novoMembro("mteixeira", "123456", "Marcos Teixeira");
		novaAtuacao(marcos, PerfilComissao.SECRETARIO, hoje.minusYears(2), null);

		// Mandato vencido ontem: login deve ser bloqueado
		MembroComissao helena = novoMembro("hsouza", "123456", "Profa. Helena Souza");
		novaAtuacao(helena, PerfilComissao.COMISSAO_ESTAGIO, hoje.minusYears(2), hoje.minusDays(1));

		// Discente de exemplo com solicitações em estados variados
		Discente lucas = new Discente();
		lucas.setId(proximoId());
		lucas.setNomeCompleto("Lucas Oliveira Santos");
		lucas.setMatricula("2024005412");
		discentes.add(lucas);

		Solicitacao feiraExtensao = novaSolicitacao(lucas, "Certificado Feira de Tecnologia 2026", TipoHoras.EXTENSAO, 40,
				"Monitor na organização da Feira de Tecnologia da UFRRJ.");
		feiraExtensao.aplicarOperacaoDiscente(OperacaoDiscente.ENVIAR, "Enviada para a Comissão de Extensão.");

		// Mesmo documento enviado a OUTRA comissão: dispara o alerta de duplicidade cruzada
		Solicitacao feiraComplementar = novaSolicitacao(lucas, "certificado feira de tecnologia 2026.pdf", TipoHoras.COMPLEMENTAR, 20,
				"Participação na mesma feira.");
		feiraComplementar.aplicarOperacaoDiscente(OperacaoDiscente.ENVIAR, "Enviada para Atividades Complementares.");

		novaSolicitacao(lucas, "Certificado Curso Python 20h", TipoHoras.COMPLEMENTAR, 20, "Curso online de Python.");

		Solicitacao estagio = novaSolicitacao(lucas, "Declaração de Estágio - Empresa XPTO", TipoHoras.ESTAGIO, 100,
				"Estágio de verão.");
		estagio.aplicarOperacaoDiscente(OperacaoDiscente.ENVIAR, "Enviada para a Comissão de Estágio.");
		estagio.emitirParecer(atuacaoEstagio, 80, "Deferido parcialmente: a declaração comprova 80h.",
				"Conferir assinatura do supervisor antes de homologar.");
		// status final: AVALIADA (aguardando o discente aceitar ou recorrer)
		assert estagio.getStatus() == StatusSolicitacao.AVALIADA;
	}

	private MembroComissao novoMembro(String username, String senha, String nome) {
		MembroComissao m = new MembroComissao();
		m.setId(proximoId());
		m.setUsername(username);
		m.setSenha(senha); // senha em texto puro: permitido no enunciado (sem recuperação/troca de senha)
		m.setNomeCompleto(nome);
		membros.add(m);
		return m;
	}

	private AtuacaoComissao novaAtuacao(MembroComissao membro, PerfilComissao perfil, LocalDate inicio, LocalDate validade) {
		AtuacaoComissao a = new AtuacaoComissao();
		a.setId(proximoId());
		a.setMembro(membro);
		a.setPerfil(perfil);
		a.setDataInicio(inicio);
		a.setDataValidade(validade);
		a.setAtivo(true);
		membro.getAtuacoes().add(a);
		return a;
	}

	private Solicitacao novaSolicitacao(Discente d, String nomeDoc, TipoHoras tipo, int horas, String obs) {
		Solicitacao s = new Solicitacao();
		s.setId(proximoId());
		s.setDiscente(d);
		s.setNomeDocumento(nomeDoc);
		s.setTipoHoras(tipo);
		s.setHorasSolicitadas(horas);
		s.setObservacaoDiscente(obs);
		s.atualizarStatusPorDiscente(StatusSolicitacao.CRIADA, "Solicitação criada pelo discente.");
		d.getSolicitacoes().add(s);
		solicitacoes.add(s);
		return s;
	}
}
