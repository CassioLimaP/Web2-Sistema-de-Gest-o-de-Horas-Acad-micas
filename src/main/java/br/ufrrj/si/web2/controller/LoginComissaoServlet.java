package br.ufrrj.si.web2.controller;

import java.io.IOException;

import br.ufrrj.si.web2.dao.MembroComissaoDAO;
import br.ufrrj.si.web2.model.MembroComissao;
import br.ufrrj.si.web2.util.SessaoUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** Autenticação de Comissões, Coordenador e Secretário (contas pré-cadastradas) com checagem de vigência. */
@WebServlet("/loginComissao")
public class LoginComissaoServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private final MembroComissaoDAO membroDAO = new MembroComissaoDAO();

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String username = req.getParameter("username");
		String senha = req.getParameter("senha");

		MembroComissao membro = (username == null) ? null : membroDAO.buscarPorUsername(username.trim());
		if (membro == null || senha == null || !membro.getSenha().equals(senha)) {
			resp.sendRedirect(req.getContextPath() + "/index.jsp?erro=credenciais");
			return;
		}
		if (membro.getAtuacaoVigente() == null) { // ex-membro: mandato vencido, inativo ou ainda não iniciado
			resp.sendRedirect(req.getContextPath() + "/index.jsp?erro=vigencia");
			return;
		}

		SessaoUtil.iniciarSessaoComissao(req, membro);
		resp.sendRedirect(req.getContextPath() + "/comissao");
	}
}
