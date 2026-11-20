package br.ufrrj.si.web2.dao;

import java.util.ArrayList;
import java.util.List;

import br.ufrrj.si.web2.model.Discente;

public class DiscenteDAO implements DAOInterface<Discente> {

	private final BancoMemoria banco = BancoMemoria.getInstancia();

	@Override
	public void persist(Discente d) {
		if (d.getId() == null) d.setId(banco.proximoId());
		banco.getDiscentes().add(d);
	}

	/** Conta de discente não pode ser excluída (enunciado). Mantido só para respeitar a interface. */
	@Override
	public void remove(Discente d) {
		throw new UnsupportedOperationException("Discentes não podem ser excluídos.");
	}

	@Override
	public Discente merge(Discente d) { return d; } // em memória o objeto já é o "gerenciado"

	@Override
	public void refresh(Discente d) { }

	@Override
	public Discente find(Long id) {
		for (Discente d : banco.getDiscentes()) {
			if (d.getId().equals(id)) return d;
		}
		return null;
	}

	@Override
	public List<Discente> findAll() { return new ArrayList<>(banco.getDiscentes()); }

	public Discente buscarPorMatricula(String matricula) {
		for (Discente d : banco.getDiscentes()) {
			if (d.getMatricula().equals(matricula)) return d;
		}
		return null;
	}

	/** Busca por nome ou matrícula (usada pelo Secretário/Comissões). */
	public List<Discente> pesquisar(String termo) {
		String t = termo == null ? "" : termo.trim().toLowerCase();
		List<Discente> achados = new ArrayList<>();
		for (Discente d : banco.getDiscentes()) {
			if (t.isEmpty() || d.getNomeCompleto().toLowerCase().contains(t) || d.getMatricula().contains(t)) {
				achados.add(d);
			}
		}
		return achados;
	}
}
