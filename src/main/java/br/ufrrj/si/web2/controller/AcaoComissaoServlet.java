package br.ufrrj.si.web2.controller;

import java.io.IOException;

import br.ufrrj.si.web2.dao.SolicitacaoDAO;
import br.ufrrj.si.web2.model.AtuacaoComissao;
import br.ufrrj.si.web2.model.OperacaoNaoPermitidaException;
import br.ufrrj.si.web2.model.Solicitacao;
import br.ufrrj.si.web2.util.SessaoUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Ações da comissão: emitir parecer, ajustar o nome do documento, restaurar cancelada (só Coordenador).
 * As regras (quem pode, em qual status) ficam no modelo; o servlet só traduz a requisição.
 */
@WebServlet("/comissao/acao")
public class AcaoComissaoServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private final SolicitacaoDAO solicitacaoDAO = new SolicitacaoDAO();

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		AtuacaoComissao atuacao = SessaoUtil.getAtuacaoLogada(req);
		String idParam = req.getParameter("id");
		try {
			Solicitacao solicitacao = solicitacaoDAO.find(Long.valueOf(idParam));
			if (solicitacao == null || !atuacao.podeVisualizar(solicitacao)) {
				throw new OperacaoNaoPermitidaException("Solicitação não encontrada para o seu perfil.");
			}

			String acao = req.getParameter("acao");
			if ("PARECER".equals(acao)) {
				String horasParam = req.getParameter("horasConcedidas");
				if (horasParam == null) throw new IllegalArgumentException("Horas ausentes.");
				Integer horas = Integer.valueOf(horasParam.trim());
				solicitacao.emitirParecer(atuacao, horas, req.getParameter("parecerPublico"),
						req.getParameter("observacaoAdministrativa"));
				SessaoUtil.flash(req, "success", "Parecer registrado. Status: " + solicitacao.getStatus() + ".");
			} else if ("AJUSTAR_NOME".equals(acao)) {
				solicitacao.ajustarNomeDocumento(atuacao, req.getParameter("novoNome"));
				SessaoUtil.flash(req, "success", "Nome do documento atualizado.");
			} else if ("RESTAURAR".equals(acao)) {
				solicitacao.restaurar(atuacao);
				SessaoUtil.flash(req, "success", "Solicitação restaurada para " + solicitacao.getStatus() + ".");
			} else {
				throw new IllegalArgumentException("Ação desconhecida.");
			}
			solicitacaoDAO.merge(solicitacao);
		} catch (OperacaoNaoPermitidaException e) {
			SessaoUtil.flash(req, "danger", e.getMessage());
		} catch (IllegalArgumentException e) { // inclui NumberFormatException
			SessaoUtil.flash(req, "danger", "Dados inválidos na requisição.");
		}
		String destino = (idParam != null && idParam.matches("\\d+")) ? "/comissao?id=" + idParam : "/comissao";
		resp.sendRedirect(req.getContextPath() + destino);
	}
}
