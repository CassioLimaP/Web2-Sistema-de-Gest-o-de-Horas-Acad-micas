package br.ufrrj.si.web2.util;

import java.time.LocalDate;

import br.ufrrj.si.web2.model.AtuacaoComissao;
import br.ufrrj.si.web2.model.AtuacaoComissao.PerfilComissao;
import br.ufrrj.si.web2.model.Discente;
import br.ufrrj.si.web2.model.HistoricoStatus;
import br.ufrrj.si.web2.model.MembroComissao;
import br.ufrrj.si.web2.model.OperacaoDiscente;
import br.ufrrj.si.web2.model.OperacaoNaoPermitidaException;
import br.ufrrj.si.web2.model.Solicitacao;
import br.ufrrj.si.web2.model.StatusSolicitacao;
import br.ufrrj.si.web2.model.TipoHoras;

/**
 * Entregável 2: valida o modelo SEM servidor e SEM banco.
 * Executar como "Java Application" (Eclipse) ou com:  gradle testeDominio
 */
public class TesteDominioSGHA {

	public static void main(String[] args) {
		titulo("1. Membros e vigência dos mandatos");
		MembroComissao carlos = membro(1L, "Prof. Carlos Mendes", "cmendes");
		AtuacaoComissao extensao = atuacao(10L, carlos, PerfilComissao.COMISSAO_EXTENSAO,
				LocalDate.now().minusMonths(2), LocalDate.now().plusMonths(10));

		MembroComissao tiago = membro(2L, "Prof. Tiago França", "tfranca");
		atuacao(11L, tiago, PerfilComissao.COMISSAO_EXTENSAO, LocalDate.now().minusYears(3), LocalDate.now().minusYears(1).minusDays(1)); // mandato antigo
		AtuacaoComissao coordenador = atuacao(12L, tiago, PerfilComissao.COORDENADOR, LocalDate.now().minusYears(1), null);

		MembroComissao helena = membro(3L, "Profa. Helena Souza", "hsouza");
		atuacao(13L, helena, PerfilComissao.COMISSAO_ESTAGIO, LocalDate.now().minusYears(2), LocalDate.now().minusDays(1));

		MembroComissao ana = membro(4L, "Profa. Ana Lima", "alima");
		AtuacaoComissao complementar = atuacao(14L, ana, PerfilComissao.COMISSAO_COMPLEMENTAR, LocalDate.now().minusMonths(6), null);

		System.out.println("Carlos vigente?  " + (carlos.getAtuacaoVigente() != null ? "SIM (" + carlos.getAtuacaoVigente().getPerfil().getRotulo() + ")" : "NÃO"));
		System.out.println("Tiago vigente?   " + (tiago.getAtuacaoVigente() != null ? "SIM (" + tiago.getAtuacaoVigente().getPerfil().getRotulo() + ") - 2 mandatos no histórico" : "NÃO"));
		System.out.println("Helena vigente?  " + (helena.getAtuacaoVigente() != null ? "SIM" : "NÃO (login bloqueado)"));

		titulo("2. Discente, solicitação e fluxo com recurso");
		Discente aluno = new Discente();
		aluno.setId(100L);
		aluno.setNomeCompleto("Lucas Oliveira Santos");
		aluno.setMatricula("2024005412");

		Solicitacao feira = solicitacao(501L, aluno, "Certificado Feira de Tecnologia 2026", TipoHoras.EXTENSAO, 40);
		System.out.println("Operações em CRIADA: " + feira.getOperacoesDiscente());
		feira.aplicarOperacaoDiscente(OperacaoDiscente.ENVIAR, "Enviada para a Comissão de Extensão.");
		System.out.println("Operações em ENVIADA: " + feira.getOperacoesDiscente() + "  (exclusão não existe mais)");

		feira.emitirParecer(extensao, 25, "Deferido parcialmente (25h): o certificado comprova 3 dias.", "Sem duplicidade em Estágio.");
		System.out.println("Após parecer: " + feira.getStatus() + " | operações: " + feira.getOperacoesDiscente());

		feira.aplicarOperacaoDiscente(OperacaoDiscente.RECORRER, "Anexei declaração complementar da coordenação do evento.");
		feira.emitirParecer(extensao, 35, "Recurso deferido parcialmente: 35h.", null);
		feira.aplicarOperacaoDiscente(OperacaoDiscente.RECORRER_COLEGIADO, "Ainda discordo: o evento teve 5 dias.");
		System.out.println("Recurso colegiado: comissão de extensão pode decidir? " + feira.podeReceberParecerDe(extensao));
		feira.emitirParecer(coordenador, 40, "Decisão final do Colegiado: 40h.", "Aprovado em reunião.");
		System.out.println("Após decisão do colegiado: " + feira.getStatus() + " (conclui automaticamente)");
		System.out.println("Horas de Extensão do aluno: " + aluno.getHorasConcluidas(TipoHoras.EXTENSAO) + "/" + TipoHoras.EXTENSAO.getHorasExigidas());

		titulo("3. Cancelamento e restauração (só Coordenador)");
		Solicitacao python = solicitacao(502L, aluno, "Curso Python 20h", TipoHoras.COMPLEMENTAR, 20);
		python.aplicarOperacaoDiscente(OperacaoDiscente.ENVIAR, null);
		python.aplicarOperacaoDiscente(OperacaoDiscente.CANCELAR, "Cancelei por engano.");
		System.out.println("Status: " + python.getStatus() + " | guardado para restaurar: " + python.getStatusAnteriorAoCancelamento());
		System.out.println("Comissão complementar pode restaurar? " + python.podeSerRestauradaPor(complementar));
		python.restaurar(coordenador);
		System.out.println("Após restauração pelo Coordenador: " + python.getStatus());

		titulo("4. Regras que DEVEM ser bloqueadas");
		tentar("Excluir solicitação já ENVIADA", () -> {
			if (!python.podeSerExcluida()) throw new OperacaoNaoPermitidaException("Só CRIADA pode ser excluída.");
		});
		tentar("Comissão de Extensão avaliando solicitação de Complementares", () -> python.emitirParecer(extensao, 10, "x", null));
		tentar("Mandato vencido (Helena, Estágio) tentando emitir parecer na própria comissão", () -> {
			Solicitacao estagio = solicitacao(505L, aluno, "Declaração de estágio", TipoHoras.ESTAGIO, 100);
			estagio.aplicarOperacaoDiscente(OperacaoDiscente.ENVIAR, null);
			estagio.emitirParecer(helena.getAtuacoes().get(0), 80, "x", null);
		});
		tentar("Recurso sem justificativa", () -> {
			Solicitacao s = solicitacao(503L, aluno, "Outro certificado", TipoHoras.COMPLEMENTAR, 10);
			s.aplicarOperacaoDiscente(OperacaoDiscente.ENVIAR, null);
			s.emitirParecer(complementar, 5, "ok", null);
			s.aplicarOperacaoDiscente(OperacaoDiscente.RECORRER, "  ");
		});
		tentar("Secretário emitindo parecer", () -> {
			MembroComissao m = membro(5L, "Marcos", "mteixeira");
			AtuacaoComissao sec = atuacao(15L, m, PerfilComissao.SECRETARIO, LocalDate.now().minusYears(1), null);
			python.emitirParecer(sec, 10, "x", null);
		});

		titulo("5. Duplicidade cruzada (mesmo documento em outra comissão)");
		Solicitacao feiraCompl = solicitacao(504L, aluno, "certificado feira de tecnologia 2026.pdf", TipoHoras.COMPLEMENTAR, 20);
		System.out.println("Documentos semelhantes? " + feiraCompl.possuiDocumentoSemelhanteA(feira));

		titulo("6. Trilha de auditoria (HistoricoStatus) da solicitação #501");
		for (HistoricoStatus h : feira.getHistoricos()) {
			System.out.printf("[%s] %-22s | %-52s | %s%n", h.getDataRegistroFormatada(), h.getStatus(),
					h.getAlteradoPor(), h.getObservacaoOuParecer());
		}
	}

	// ---------- auxiliares só para deixar o main legível ----------

	private static MembroComissao membro(Long id, String nome, String username) {
		MembroComissao m = new MembroComissao();
		m.setId(id);
		m.setNomeCompleto(nome);
		m.setUsername(username);
		m.setSenha("123456");
		return m;
	}

	private static AtuacaoComissao atuacao(Long id, MembroComissao m, PerfilComissao perfil, LocalDate inicio, LocalDate validade) {
		AtuacaoComissao a = new AtuacaoComissao();
		a.setId(id);
		a.setMembro(m);
		a.setPerfil(perfil);
		a.setDataInicio(inicio);
		a.setDataValidade(validade);
		a.setAtivo(true);
		m.getAtuacoes().add(a);
		return a;
	}

	private static Solicitacao solicitacao(Long id, Discente d, String doc, TipoHoras tipo, int horas) {
		Solicitacao s = new Solicitacao();
		s.setId(id);
		s.setDiscente(d);
		s.setNomeDocumento(doc);
		s.setTipoHoras(tipo);
		s.setHorasSolicitadas(horas);
		s.setCaminhoArquivoComprovante("2024005412_" + id + ".pdf");
		s.atualizarStatusPorDiscente(StatusSolicitacao.CRIADA, "Rascunho criado pelo discente.");
		d.getSolicitacoes().add(s);
		return s;
	}

	private static void tentar(String descricao, Runnable acao) {
		try {
			acao.run();
			System.out.println("FALHA: deveria ter sido bloqueado -> " + descricao);
		} catch (OperacaoNaoPermitidaException e) {
			System.out.println("Bloqueado OK: " + descricao + "\n     motivo: " + e.getMessage());
		}
	}

	private static void titulo(String texto) {
		System.out.println("\n=== " + texto + " ===");
	}
}
