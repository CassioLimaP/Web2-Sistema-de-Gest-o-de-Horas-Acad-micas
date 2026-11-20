package br.ufrrj.si.web2.util;

import br.ufrrj.si.web2.dao.DiscenteDAO;
import br.ufrrj.si.web2.dao.MembroComissaoDAO;
import br.ufrrj.si.web2.model.AtuacaoComissao;
import br.ufrrj.si.web2.model.Discente;
import br.ufrrj.si.web2.model.MembroComissao;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * Centraliza o que fica na sessão HTTP. Guardamos só os IDs (não os objetos):
 * assim, a cada requisição o usuário é recarregado e a vigência do mandato é rechecada.
 */
public final class SessaoUtil {

	private static final String ATR_DISCENTE_ID = "discenteId";
	private static final String ATR_MEMBRO_ID = "membroId";
	private static final String ATR_FLASH_TIPO = "flashTipo";
	private static final String ATR_FLASH_TEXTO = "flashTexto";

	private static final DiscenteDAO discenteDAO = new DiscenteDAO();
	private static final MembroComissaoDAO membroDAO = new MembroComissaoDAO();

	private SessaoUtil() { }

	public static void iniciarSessaoDiscente(HttpServletRequest req, Discente discente) {
		reiniciar(req).setAttribute(ATR_DISCENTE_ID, discente.getId());
	}

	public static void iniciarSessaoComissao(HttpServletRequest req, MembroComissao membro) {
		reiniciar(req).setAttribute(ATR_MEMBRO_ID, membro.getId());
	}

	/** Descarta a sessão anterior (evita fixação de sessão e mistura de perfis) e abre uma nova. */
	private static HttpSession reiniciar(HttpServletRequest req) {
		HttpSession antiga = req.getSession(false);
		if (antiga != null) antiga.invalidate();
		return req.getSession(true);
	}

	public static void encerrar(HttpServletRequest req) {
		HttpSession sessao = req.getSession(false);
		if (sessao != null) sessao.invalidate();
	}

	public static Discente getDiscenteLogado(HttpServletRequest req) {
		HttpSession sessao = req.getSession(false);
		if (sessao == null || sessao.getAttribute(ATR_DISCENTE_ID) == null) return null;
		return discenteDAO.find((Long) sessao.getAttribute(ATR_DISCENTE_ID));
	}

	/** Retorna a atuação vigente HOJE do membro logado, ou null se não há login ou o mandato expirou. */
	public static AtuacaoComissao getAtuacaoLogada(HttpServletRequest req) {
		HttpSession sessao = req.getSession(false);
		if (sessao == null || sessao.getAttribute(ATR_MEMBRO_ID) == null) return null;
		MembroComissao membro = membroDAO.find((Long) sessao.getAttribute(ATR_MEMBRO_ID));
		return membro == null ? null : membro.getAtuacaoVigente();
	}

	// ---- Mensagens "flash": sobrevivem a um redirect e somem depois de exibidas ----

	public static void flash(HttpServletRequest req, String tipo, String texto) {
		HttpSession sessao = req.getSession(false);
		if (sessao != null) {
			sessao.setAttribute(ATR_FLASH_TIPO, tipo); // success | danger | warning
			sessao.setAttribute(ATR_FLASH_TEXTO, texto);
		}
	}

	public static void transferirFlashParaRequisicao(HttpServletRequest req) {
		HttpSession sessao = req.getSession(false);
		if (sessao == null || sessao.getAttribute(ATR_FLASH_TEXTO) == null) return;
		req.setAttribute("flashTipo", sessao.getAttribute(ATR_FLASH_TIPO));
		req.setAttribute("flashTexto", sessao.getAttribute(ATR_FLASH_TEXTO));
		sessao.removeAttribute(ATR_FLASH_TIPO);
		sessao.removeAttribute(ATR_FLASH_TEXTO);
	}
}
