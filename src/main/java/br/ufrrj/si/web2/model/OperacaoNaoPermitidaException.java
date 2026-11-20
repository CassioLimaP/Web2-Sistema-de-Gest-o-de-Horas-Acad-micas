package br.ufrrj.si.web2.model;

/** Lançada quando uma regra de negócio (status, perfil, vigência) impede a operação. */
public class OperacaoNaoPermitidaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public OperacaoNaoPermitidaException(String mensagem) {
        super(mensagem);
    }
}
