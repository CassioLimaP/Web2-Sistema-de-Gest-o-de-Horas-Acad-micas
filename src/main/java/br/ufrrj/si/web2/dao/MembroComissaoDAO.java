package br.ufrrj.si.web2.dao;

import java.util.ArrayList;
import java.util.List;

import br.ufrrj.si.web2.model.MembroComissao;

public class MembroComissaoDAO implements DAOInterface<MembroComissao> {

	private final BancoMemoria banco = BancoMemoria.getInstancia();

	@Override
	public void persist(MembroComissao m) {
		if (m.getId() == null) m.setId(banco.proximoId());
		banco.getMembros().add(m);
	}

	@Override
	public void remove(MembroComissao m) {
		throw new UnsupportedOperationException("Membros não são excluídos: perdem a vigência (ficam inativos).");
	}

	@Override
	public MembroComissao merge(MembroComissao m) { return m; }

	@Override
	public void refresh(MembroComissao m) { }

	@Override
	public MembroComissao find(Long id) {
		for (MembroComissao m : banco.getMembros()) {
			if (m.getId().equals(id)) return m;
		}
		return null;
	}

	@Override
	public List<MembroComissao> findAll() { return new ArrayList<>(banco.getMembros()); }

	public MembroComissao buscarPorUsername(String username) {
		for (MembroComissao m : banco.getMembros()) {
			if (m.getUsername().equals(username)) return m;
		}
		return null;
	}
}
