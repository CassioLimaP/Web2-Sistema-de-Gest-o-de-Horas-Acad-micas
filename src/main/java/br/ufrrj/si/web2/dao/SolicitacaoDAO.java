package br.ufrrj.si.web2.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import br.ufrrj.si.web2.model.AtuacaoComissao;
import br.ufrrj.si.web2.model.Discente;
import br.ufrrj.si.web2.model.Solicitacao;
import br.ufrrj.si.web2.model.StatusSolicitacao;
import br.ufrrj.si.web2.model.TipoHoras;

public class SolicitacaoDAO implements DAOInterface<Solicitacao> {

	private final BancoMemoria banco = BancoMemoria.getInstancia();

	@Override
	public void persist(Solicitacao s) {
		if (s.getId() == null) s.setId(banco.proximoId());
		banco.getSolicitacoes().add(s);
		if (!s.getDiscente().getSolicitacoes().contains(s)) {
			s.getDiscente().getSolicitacoes().add(s);
		}
	}

	@Override
	public void remove(Solicitacao s) {
		banco.getSolicitacoes().remove(s);
		s.getDiscente().getSolicitacoes().remove(s);
	}

	@Override
	public Solicitacao merge(Solicitacao s) { return s; }

	@Override
	public void refresh(Solicitacao s) { }

	@Override
	public Solicitacao find(Long id) {
		for (Solicitacao s : banco.getSolicitacoes()) {
			if (s.getId().equals(id)) return s;
		}
		return null;
	}

	@Override
	public List<Solicitacao> findAll() { return new ArrayList<>(banco.getSolicitacoes()); }

	/** Solicitações do discente, mais recentes primeiro. */
	public List<Solicitacao> listarPorDiscente(Discente discente) {
		List<Solicitacao> lista = new ArrayList<>(discente.getSolicitacoes());
		Collections.sort(lista, Comparator.comparing(Solicitacao::getId).reversed());
		return lista;
	}

	/**
	 * Fila de uma atuação (perfil vigente): só o que ela pode ver, filtrada por
	 * nome/matrícula do discente e/ou tipo de horas (ambos opcionais).
	 */
	public List<Solicitacao> listarVisiveisPara(AtuacaoComissao atuacao, String termo, TipoHoras tipo) {
		String t = termo == null ? "" : termo.trim().toLowerCase();
		List<Solicitacao> fila = new ArrayList<>();
		for (Solicitacao s : banco.getSolicitacoes()) {
			if (!atuacao.podeVisualizar(s)) continue;
			if (tipo != null && s.getTipoHoras() != tipo) continue;
			if (!t.isEmpty() && !s.getDiscente().getNomeCompleto().toLowerCase().contains(t)
					&& !s.getDiscente().getMatricula().contains(t)) continue;
			fila.add(s);
		}
		Collections.sort(fila, Comparator.comparing(Solicitacao::getId).reversed());
		return fila;
	}

	/**
	 * Duplicidade cruzada: outras solicitações do MESMO discente, em qualquer comissão,
	 * com documento semelhante. Ignora rascunhos (CRIADA) e canceladas (não contam horas).
	 */
	public List<Solicitacao> buscarPossiveisDuplicidades(Solicitacao alvo) {
		List<Solicitacao> achadas = new ArrayList<>();
		for (Solicitacao outra : alvo.getDiscente().getSolicitacoes()) {
			if (outra.getId().equals(alvo.getId())) continue;
			if (outra.getStatus() == StatusSolicitacao.CRIADA || outra.getStatus() == StatusSolicitacao.CANCELADA) continue;
			if (alvo.possuiDocumentoSemelhanteA(outra)) achadas.add(outra);
		}
		return achadas;
	}
}
