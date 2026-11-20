package br.ufrrj.si.web2.controller;

import java.io.IOException;

import br.ufrrj.si.web2.dao.DiscenteDAO;
import br.ufrrj.si.web2.model.Discente;
import br.ufrrj.si.web2.util.SessaoUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Identificação do discente: nome + matrícula.
 * Matrícula nova -> cadastra automaticamente; existente -> recupera o cadastro (e as solicitações).
 * Depois de cadastrado, a conta não é alterada nem excluída.
 */
@WebServlet("/acessoDiscente")
public class AcessoDiscenteServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private final DiscenteDAO discenteDAO = new DiscenteDAO();

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String nome = req.getParameter("nomeCompleto");
		String matricula = req.getParameter("matricula");

		if (nome == null || nome.trim().isEmpty() || matricula == null || matricula.trim().isEmpty()) {
			resp.sendRedirect(req.getContextPath() + "/index.jsp?erro=dados");
			return;
		}
		matricula = matricula.trim();

		Discente discente = discenteDAO.buscarPorMatricula(matricula);
		if (discente == null) { // primeiro acesso: cadastro automático
			discente = new Discente();
			discente.setNomeCompleto(nome.trim());
			discente.setMatricula(matricula);
			discenteDAO.persist(discente);
		}

		SessaoUtil.iniciarSessaoDiscente(req, discente);
		resp.sendRedirect(req.getContextPath() + "/discente");
	}
}
