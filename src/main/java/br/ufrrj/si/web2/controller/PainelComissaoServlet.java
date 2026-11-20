package br.ufrrj.si.web2.controller;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import br.ufrrj.si.web2.dao.SolicitacaoDAO;
import br.ufrrj.si.web2.model.AtuacaoComissao;
import br.ufrrj.si.web2.model.Solicitacao;
import br.ufrrj.si.web2.model.TipoHoras;
import br.ufrrj.si.web2.util.SessaoUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** Painel de Comissões / Coordenador / Secretário: fila filtrada pelo perfil + análise da solicitação escolhida. */
@WebServlet("/comissao")
public class PainelComissaoServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private final SolicitacaoDAO solicitacaoDAO = new SolicitacaoDAO();

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		AtuacaoComissao atuacao = SessaoUtil.getAtuacaoLogada(req);

		String termo = req.getParameter("q");
		TipoHoras filtroTipo = null;
		String tipoParam = req.getParameter("tipo");
		if (tipoParam != null && !tipoParam.isEmpty()) {
			try {
				filtroTipo = TipoHoras.valueOf(tipoParam);
			} catch (IllegalArgumentException e) {
				filtroTipo = null; // filtro inválido é ignorado
			}
		}

		Solicitacao selecionada = null;
		String idParam = req.getParameter("id");
		if (idParam != null && idParam.matches("\\d+")) {
			Solicitacao candidata = solicitacaoDAO.find(Long.valueOf(idParam));
			if (candidata != null && atuacao.podeVisualizar(candidata)) { // não abre o que o perfil não pode ver
				selecionada = candidata;
			}
		}

		List<Solicitacao> duplicidades = Collections.emptyList();
		if (selecionada != null) {
			duplicidades = solicitacaoDAO.buscarPossiveisDuplicidades(selecionada);
		}

		req.setAttribute("atuacao", atuacao);
		req.setAttribute("solicitacoes", solicitacaoDAO.listarVisiveisPara(atuacao, termo, filtroTipo));
		req.setAttribute("selecionada", selecionada);
		req.setAttribute("duplicidades", duplicidades);
		req.setAttribute("podeParecer", selecionada != null && selecionada.podeReceberParecerDe(atuacao));
		req.setAttribute("podeAjustarNome", selecionada != null && selecionada.podeAjustarNomeDe(atuacao));
		req.setAttribute("podeRestaurar", selecionada != null && selecionada.podeSerRestauradaPor(atuacao));
		req.setAttribute("tipos", TipoHoras.values());
		req.setAttribute("termo", termo == null ? "" : termo);
		req.setAttribute("tipoFiltro", filtroTipo == null ? "" : filtroTipo.name());
		SessaoUtil.transferirFlashParaRequisicao(req);

		req.getRequestDispatcher("/WEB-INF/views/comissao.jsp").forward(req, resp);
	}
}
