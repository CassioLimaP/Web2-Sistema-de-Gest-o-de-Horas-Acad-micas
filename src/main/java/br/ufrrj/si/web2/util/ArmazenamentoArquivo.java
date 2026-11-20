package br.ufrrj.si.web2.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import jakarta.servlet.http.Part;

/**
 * Salva comprovantes numa pasta FORA do container web (não some no redeploy do WAR).
 * Pasta padrão: ~/uploads/sgha. Para mudar: -Dsgha.upload.dir=/outro/caminho
 * No banco guardamos apenas o nome único do arquivo, nunca o conteúdo.
 */
public final class ArmazenamentoArquivo {

	private static final Set<String> EXTENSOES_PERMITIDAS = new HashSet<>(Arrays.asList("pdf", "png", "jpg", "jpeg"));

	private ArmazenamentoArquivo() { }

	public static Path diretorioBase() {
		String dir = System.getProperty("sgha.upload.dir");
		if (dir == null || dir.trim().isEmpty()) {
			dir = System.getProperty("user.home") + File.separator + "uploads" + File.separator + "sgha";
		}
		return Paths.get(dir).toAbsolutePath().normalize();
	}

	/** Grava o arquivo enviado e devolve o nome único gerado (esse é o valor que vai para a entidade). */
	public static String salvar(Part parte, String matricula) throws IOException {
		String extensao = extensaoDe(parte.getSubmittedFileName());
		if (!EXTENSOES_PERMITIDAS.contains(extensao)) {
			throw new IllegalArgumentException("Formato não permitido. Envie PDF, PNG ou JPG.");
		}
		Files.createDirectories(diretorioBase());
		String prefixo = matricula.replaceAll("[^A-Za-z0-9]", "");
		String nomeUnico = prefixo + "_" + UUID.randomUUID() + "." + extensao;
		try (InputStream entrada = parte.getInputStream()) {
			Files.copy(entrada, diretorioBase().resolve(nomeUnico), StandardCopyOption.REPLACE_EXISTING);
		}
		return nomeUnico;
	}

	/** Resolve o nome para um caminho, impedindo escapar da pasta base (path traversal). */
	public static Path resolver(String nomeArquivo) {
		Path base = diretorioBase();
		Path alvo = base.resolve(nomeArquivo).normalize();
		if (!alvo.startsWith(base)) {
			throw new IllegalArgumentException("Caminho inválido.");
		}
		return alvo;
	}

	public static void excluirSilenciosamente(String nomeArquivo) {
		if (nomeArquivo == null) return;
		try {
			Files.deleteIfExists(resolver(nomeArquivo));
		} catch (IOException | IllegalArgumentException e) {
			// arquivo órfão não impede a operação principal
		}
	}

	public static String tipoConteudo(String nomeArquivo) {
		switch (extensaoDe(nomeArquivo)) {
			case "pdf": return "application/pdf";
			case "png": return "image/png";
			case "jpg":
			case "jpeg": return "image/jpeg";
			default: return "application/octet-stream";
		}
	}

	private static String extensaoDe(String nome) {
		if (nome == null || nome.lastIndexOf('.') < 0) return "";
		return nome.substring(nome.lastIndexOf('.') + 1).toLowerCase();
	}
}
