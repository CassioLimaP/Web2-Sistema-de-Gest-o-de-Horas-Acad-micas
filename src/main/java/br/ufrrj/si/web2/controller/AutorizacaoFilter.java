package br.ufrrj.si.web2.controller;

import java.io.IOException;

import br.ufrrj.si.web2.util.SessaoUtil;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Barreira de acesso (RBAC básico): área do discente exige sessão de discente;
 * área da comissão exige membro com mandato VIGENTE hoje (checado a cada requisição).
 * Sessão expirada (2 min de inatividade) cai aqui e volta para o portal.
 */
@WebFilter(urlPatterns = { "/discente", "/discente/*", "/solicitacao", "/comissao", "/comissao/*", "/arquivo" })
public class AutorizacaoFilter implements Filter {

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;
		String caminho = request.getServletPath();

		boolean ehDiscente = SessaoUtil.getDiscenteLogado(request) != null;
		boolean ehComissao = SessaoUtil.getAtuacaoLogada(request) != null;

		boolean autorizado;
		if (caminho.startsWith("/comissao")) {
			autorizado = ehComissao;
		} else if (caminho.equals("/arquivo")) {
			autorizado = ehDiscente || ehComissao; // o ComprovanteServlet ainda confere a posse/permissão
		} else {
			autorizado = ehDiscente;
		}

		if (autorizado) {
			chain.doFilter(req, res);
		} else {
			response.sendRedirect(request.getContextPath() + "/index.jsp?erro=sessao");
		}
	}
}
