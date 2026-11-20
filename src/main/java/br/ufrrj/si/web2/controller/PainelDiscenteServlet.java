package br.ufrrj.si.web2.controller;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import br.ufrrj.si.web2.dao.SolicitacaoDAO;
import br.ufrrj.si.web2.model.Discente;
import br.ufrrj.si.web2.model.TipoHoras;
import br.ufrrj.si.web2.util.SessaoUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** Dashboard do discente: monta o modelo (lista + totais de horas) e delega a exibição ao JSP. */
@WebServlet("/discente")
public class PainelDiscenteServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private final SolicitacaoDAO solicitacaoDAO = new SolicitacaoDAO();

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Discente discente = SessaoUtil.getDiscenteLogado(req);

		// Consolidação total: soma tudo do aluno, independentemente da comissão que avaliou
		Map<TipoHoras, Integer> horasPorTipo = new LinkedHashMap<>();
		for (TipoHoras tipo : TipoHoras.values()) {
			horasPorTipo.put(tipo, discente.getHorasConcluidas(tipo));
		}

		req.setAttribute("discente", discente);
		req.setAttribute("solicitacoes", solicitacaoDAO.listarPorDiscente(discente));
		req.setAttribute("horasPorTipo", horasPorTipo);
		req.setAttribute("tipos", TipoHoras.values());
		SessaoUtil.transferirFlashParaRequisicao(req);

		req.getRequestDispatcher("/WEB-INF/views/discente.jsp").forward(req, resp);
	}
}
