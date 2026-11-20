package br.ufrrj.si.web2.controller;

import java.io.IOException;

import br.ufrrj.si.web2.dao.SolicitacaoDAO;
import br.ufrrj.si.web2.model.Discente;
import br.ufrrj.si.web2.model.OperacaoDiscente;
import br.ufrrj.si.web2.model.OperacaoNaoPermitidaException;
import br.ufrrj.si.web2.model.Solicitacao;
import br.ufrrj.si.web2.util.ArmazenamentoArquivo;
import br.ufrrj.si.web2.util.SessaoUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** Operações do discente sobre uma solicitação: enviar, cancelar, excluir, aceitar, recorrer. */
@WebServlet("/discente/acao")
public class AcaoDiscenteServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private final SolicitacaoDAO solicitacaoDAO = new SolicitacaoDAO();

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Discente discente = SessaoUtil.getDiscenteLogado(req);
		try {
			Long id = Long.valueOf(req.getParameter("id"));
			String acaoParam = req.getParameter("acao");
			if (acaoParam == null) throw new IllegalArgumentException("Ação ausente.");
			OperacaoDiscente operacao = OperacaoDiscente.valueOf(acaoParam);
			Solicitacao solicitacao = solicitacaoDAO.find(id);

			// só o dono mexe na própria solicitação
			if (solicitacao == null || !solicitacao.getDiscente().getId().equals(discente.getId())) {
				throw new OperacaoNaoPermitidaException("Solicitação não encontrada.");
			}

			if (operacao == OperacaoDiscente.EXCLUIR) {
				if (!solicitacao.podeSerExcluida()) {
					throw new OperacaoNaoPermitidaException(
							"Só é possível excluir solicitações CRIADA. A partir de ENVIADA, apenas cancelar.");
				}
				solicitacaoDAO.remove(solicitacao);
				ArmazenamentoArquivo.excluirSilenciosamente(solicitacao.getCaminhoArquivoComprovante());
				SessaoUtil.flash(req, "success", "Solicitação #" + id + " excluída.");
			} else {
				solicitacao.aplicarOperacaoDiscente(operacao, req.getParameter("justificativa"));
				solicitacaoDAO.merge(solicitacao);
				SessaoUtil.flash(req, "success", "Solicitação #" + id + ": " + operacao.getRotulo()
						+ " realizada. Status atual: " + solicitacao.getStatus() + ".");
			}
		} catch (OperacaoNaoPermitidaException e) {
			SessaoUtil.flash(req, "danger", e.getMessage());
		} catch (IllegalArgumentException e) { // id ou ação inválidos
			SessaoUtil.flash(req, "danger", "Requisição inválida.");
		}
		resp.sendRedirect(req.getContextPath() + "/discente");
	}
}
