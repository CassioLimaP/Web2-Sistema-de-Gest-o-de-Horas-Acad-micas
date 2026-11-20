package br.ufrrj.si.web2.controller;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;

/** Garante UTF-8 em formulários (acentos) antes de qualquer servlet ler os parâmetros. */
@WebFilter("/*")
public class EncodingFilter implements Filter {

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {
		req.setCharacterEncoding("UTF-8");
		res.setCharacterEncoding("UTF-8");
		chain.doFilter(req, res);
	}
}
