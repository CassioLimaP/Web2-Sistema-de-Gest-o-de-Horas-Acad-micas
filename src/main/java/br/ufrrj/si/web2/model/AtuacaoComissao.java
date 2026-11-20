package br.ufrrj.si.web2.model;

//import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;

//@Entity
//@Table(name = "atuacoes_comissao")
public class AtuacaoComissao implements Serializable {

    private static final long serialVersionUID = 1L;

//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//    @ManyToOne(optional = false)
//    @JoinColumn(name = "membro_id", nullable = false)
    private MembroComissao membro;

//    @Enumerated(EnumType.STRING)
//    @Column(nullable = false, length = 30)
    private PerfilComissao perfil;

//    @Column(nullable = false)
    private LocalDate dataInicio;

    // Se null, significa mandato em aberto (indeterminado até encerramento)
    private LocalDate dataValidade;

    // Permite revogação manual imediata antes da dataValidade, se necessário
//    @Column(nullable = false)
    private boolean ativo = true;

    public enum PerfilComissao {
        COMISSAO_EXTENSAO("Comissão de Extensão", TipoHoras.EXTENSAO),
        COMISSAO_COMPLEMENTAR("Comissão de Atividades Complementares", TipoHoras.COMPLEMENTAR),
        COMISSAO_ESTAGIO("Comissão de Estágio", TipoHoras.ESTAGIO),
        COORDENADOR("Coordenador do Curso", null),
        SECRETARIO("Secretário do Curso", null);

        private final String rotulo;
        private final TipoHoras tipoExclusivo; // null = não é restrito a uma comissão

        PerfilComissao(String rotulo, TipoHoras tipoExclusivo) {
            this.rotulo = rotulo;
            this.tipoExclusivo = tipoExclusivo;
        }

        public String getRotulo() { return rotulo; }

        /** Pode emitir parecer? Comissões: só no seu tipo. Coordenador: qualquer. Secretário: nunca. */
        public boolean podeAvaliar(TipoHoras tipo) {
            return this == COORDENADOR || (tipoExclusivo != null && tipoExclusivo == tipo);
        }

        /** Pode consultar? Coordenador e Secretário: tudo. Comissões: só o seu tipo. */
        public boolean podeConsultar(TipoHoras tipo) {
            return this == COORDENADOR || this == SECRETARIO || (tipoExclusivo != null && tipoExclusivo == tipo);
        }
    }

    public AtuacaoComissao() {}

    // Verifica se o mandato está ativo e dentro do período de vigência
    public boolean isVigenteHoje() {
        if (!this.ativo) return false;
        LocalDate hoje = LocalDate.now();
        boolean iniciou = this.dataInicio != null && !hoje.isBefore(this.dataInicio);
        boolean naoExpirou = (this.dataValidade == null) || !hoje.isAfter(this.dataValidade);
        return iniciou && naoExpirou;
    }

    /**
     * Regra de visibilidade nas filas:
     * - CRIADA é rascunho: só o próprio discente enxerga;
     * - CANCELADA não aparece nas filas das comissões (só Coordenador, que restaura, e Secretário, que consulta);
     * - demais status: conforme o tipo de horas do perfil.
     */
    public boolean podeVisualizar(Solicitacao solicitacao) {
        if (!isVigenteHoje() || solicitacao.getStatus() == StatusSolicitacao.CRIADA) return false;
        if (solicitacao.getStatus() == StatusSolicitacao.CANCELADA) {
            return perfil == PerfilComissao.COORDENADOR || perfil == PerfilComissao.SECRETARIO;
        }
        return perfil.podeConsultar(solicitacao.getTipoHoras());
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public MembroComissao getMembro() { return membro; }
    public void setMembro(MembroComissao membro) { this.membro = membro; }
    public PerfilComissao getPerfil() { return perfil; }
    public void setPerfil(PerfilComissao perfil) { this.perfil = perfil; }
    public LocalDate getDataInicio() { return dataInicio; }
    public void setDataInicio(LocalDate dataInicio) { this.dataInicio = dataInicio; }
    public LocalDate getDataValidade() { return dataValidade; }
    public void setDataValidade(LocalDate dataValidade) { this.dataValidade = dataValidade; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
}