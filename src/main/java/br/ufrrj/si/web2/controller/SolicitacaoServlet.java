package br.ufrrj.si.web2.controller;

import java.io.IOException;

import br.ufrrj.si.web2.dao.SolicitacaoDAO;
import br.ufrrj.si.web2.model.Discente;
import br.ufrrj.si.web2.model.OperacaoDiscente;
import br.ufrrj.si.web2.model.OperacaoNaoPermitidaException;
import br.ufrrj.si.web2.model.Solicitacao;
import br.ufrrj.si.web2.model.StatusSolicitacao;
import br.ufrrj.si.web2.model.TipoHoras;
import br.ufrrj.si.web2.util.ArmazenamentoArquivo;
import br.ufrrj.si.web2.util.SessaoUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

/** Nova solicitação com upload do comprovante. O arquivo vai para disco; a entidade guarda só o nome. */
@WebServlet("/solicitacao")
@MultipartConfig(
		fileSizeThreshold = 1024 * 1024 * 1, // 1 MB em memória antes de ir para disco temporário
		maxFileSize = 1024 * 1024 * 5,       // 5 MB por arquivo
		maxRequestSize = 1024 * 1024 * 10    // 10 MB por requisição
)
public class SolicitacaoServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private final SolicitacaoDAO solicitacaoDAO = new SolicitacaoDAO();

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Discente discente = SessaoUtil.getDiscenteLogado(req);
		try {
			// 1) Lê e valida tudo ANTES de gravar o arquivo
			String nomeDocumento = req.getParameter("nomeDocumento");
			if (nomeDocumento == null || nomeDocumento.trim().isEmpty()) {
				throw new IllegalArgumentException("Informe o título/nome do documento.");
			}
			TipoHoras tipo = lerTipoHoras(req.getParameter("tipoHoras"));
			String horasParam = req.getParameter("horasSolicitadas");
			int horas = Integer.parseInt(horasParam == null ? "" : horasParam.trim()); // vazio -> NumberFormatException
			if (horas <= 0) {
				throw new IllegalArgumentException("A quantidade de horas deve ser maior que zero.");
			}
			Part comprovante = req.getPart("arquivoComprovante");
			if (comprovante == null || comprovante.getSize() == 0) {
				throw new IllegalArgumentException("Anexe o comprovante (PDF, PNG ou JPG).");
			}

			// 2) Grava o arquivo fora do container e monta a entidade
			String nomeArquivo = ArmazenamentoArquivo.salvar(comprovante, discente.getMatricula());

			Solicitacao solicitacao = new Solicitacao();
			solicitacao.setDiscente(discente);
			solicitacao.setNomeDocumento(nomeDocumento.trim());
			solicitacao.setTipoHoras(tipo);
			solicitacao.setHorasSolicitadas(horas);
			String observacao = req.getParameter("observacaoDiscente");
			solicitacao.setObservacaoDiscente(observacao == null || observacao.trim().isEmpty() ? null : observacao.trim());
			solicitacao.setCaminhoArquivoComprovante(nomeArquivo);
			solicitacao.atualizarStatusPorDiscente(StatusSolicitacao.CRIADA, "Solicitação criada pelo discente.");

			boolean enviarAgora = "enviar".equals(req.getParameter("acao"));
			if (enviarAgora) {
				solicitacao.aplicarOperacaoDiscente(OperacaoDiscente.ENVIAR, "Enviada para a " + tipo.getDescricao() + ".");
			}
			solicitacaoDAO.persist(solicitacao);

			SessaoUtil.flash(req, "success", enviarAgora
					? "Solicitação enviada para a " + tipo.getDescricao() + "."
					: "Rascunho salvo (status CRIADA). Envie quando quiser.");

		} catch (NumberFormatException e) {
			SessaoUtil.flash(req, "danger", "Informe a quantidade de horas como número inteiro.");
		} catch (IllegalArgumentException | OperacaoNaoPermitidaException e) {
			SessaoUtil.flash(req, "danger", e.getMessage());
		} catch (IllegalStateException | ServletException e) {
			// Tomcat lança isso quando o limite do @MultipartConfig é excedido
			SessaoUtil.flash(req, "danger", "Arquivo grande demais (limite de 5 MB por arquivo).");
		}
		resp.sendRedirect(req.getContextPath() + "/discente");
	}

	private TipoHoras lerTipoHoras(String valor) {
		try {
			return TipoHoras.valueOf(valor);
		} catch (IllegalArgumentException | NullPointerException e) {
			throw new IllegalArgumentException("Selecione a comissão destino.");
		}
	}
}
