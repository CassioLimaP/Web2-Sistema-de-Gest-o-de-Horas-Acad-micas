package br.ufrrj.si.web2.model;

/** Operações que o discente pode disparar no dashboard (dependem do status da solicitação). */
public enum OperacaoDiscente {
    ENVIAR("Enviar", false, false),
    CANCELAR("Cancelar", true, false),
    ACEITAR("Aceitar parecer", false, false),
    RECORRER("Abrir recurso", false, true),
    RECORRER_COLEGIADO("Recurso ao Colegiado", false, true),
    EXCLUIR("Excluir", true, false);

    private final String rotulo;
    private final boolean destrutiva;
    private final boolean exigeJustificativa;

    OperacaoDiscente(String rotulo, boolean destrutiva, boolean exigeJustificativa) {
        this.rotulo = rotulo;
        this.destrutiva = destrutiva;
        this.exigeJustificativa = exigeJustificativa;
    }

    public String getRotulo() { return rotulo; }
    public boolean isDestrutiva() { return destrutiva; }
    public boolean isExigeJustificativa() { return exigeJustificativa; }
}
