package br.ufrrj.si.web2.dao;

import java.util.List;

/**
 * Contrato dos DAOs. No Entregável 2 é implementado em memória (BancoMemoria);
 * no Entregável 3 as mesmas assinaturas passam a usar EntityManager (JPA/Hibernate).
 */
public interface DAOInterface<T> {
	public void persist(T obj);
	public void remove(T obj);
	public T merge(T obj);
	public void refresh(T obj);
	public T find(Long id);
	public List<T> findAll();
}
