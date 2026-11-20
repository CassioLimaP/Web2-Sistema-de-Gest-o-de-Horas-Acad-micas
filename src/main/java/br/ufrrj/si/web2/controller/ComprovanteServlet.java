package br.ufrrj.si.web2.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import br.ufrrj.si.web2.dao.SolicitacaoDAO;
import br.ufrrj.si.web2.model.AtuacaoComissao;
import br.ufrrj.si.web2.model.Discente;
import br.ufrrj.si.web2.model.Solicitacao;
import br.ufrrj.si.web2.util.ArmazenamentoArquivo;
import br.ufrrj.si.web2.util.SessaoUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** Entrega o comprovante (que está fora do container) só a quem tem direito: dono ou perfil que enxerga a solicitação. */
@WebServlet("/arquivo")
public class ComprovanteServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private final SolicitacaoDAO solicitacaoDAO = new SolicitacaoDAO();

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String idParam = req.getParameter("id");
		Solicitacao s = (idParam != null && idParam.matches("\\d+")) ? solicitacaoDAO.find(Long.valueOf(idParam)) : null;
		if (s == null || s.getCaminhoArquivoComprovante() == null) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		Discente discente = SessaoUtil.getDiscenteLogado(req);
		AtuacaoComissao atuacao = SessaoUtil.getAtuacaoLogada(req);
		boolean donoDaSolicitacao = discente != null && s.getDiscente().getId().equals(discente.getId());
		boolean perfilAutorizado = atuacao != null && atuacao.podeVisualizar(s);
		if (!donoDaSolicitacao && !perfilAutorizado) {
			resp.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		Path arquivo = ArmazenamentoArquivo.resolver(s.getCaminhoArquivoComprovante());
		if (!Files.exists(arquivo)) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		resp.setContentType(ArmazenamentoArquivo.tipoConteudo(arquivo.getFileName().toString()));
		resp.setHeader("Content-Disposition", "inline; filename=\"comprovante_" + s.getId() + "\"");
		resp.setContentLengthLong(Files.size(arquivo));
		Files.copy(arquivo, resp.getOutputStream());
	}
}
