package br.ufrrj.si.web2.model;

/**
 * Categoria de horas = comissão destino da solicitação.
 * As horas exigidas vieram do protótipo do professor (discente.html); ajuste se o PPC for outro.
 */
public enum TipoHoras {
    EXTENSAO("Comissão de Ações Extensionistas", "Ações Extensionistas", 60),
    COMPLEMENTAR("Comissão de Atividades Complementares", "Atividades Complementares", 120),
    ESTAGIO("Comissão de Estágio", "Horas de Estágio", 300);

    private final String descricao;
    private final String rotulo;
    private final int horasExigidas;

    TipoHoras(String descricao, String rotulo, int horasExigidas) {
        this.descricao = descricao;
        this.rotulo = rotulo;
        this.horasExigidas = horasExigidas;
    }

    public String getDescricao() { return descricao; }
    public String getRotulo() { return rotulo; }
    public int getHorasExigidas() { return horasExigidas; }
}
