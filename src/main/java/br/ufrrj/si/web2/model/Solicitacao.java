package br.ufrrj.si.web2.model;
//import javax.persistence.*;
import java.io.Serializable;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

//@Entity
//@Table(name = "solicitacoes")
public class Solicitacao implements Serializable {

	// Adicionado para garantir a compatibilidade na serialização (sessão / JPA)
    private static final long serialVersionUID = 1L;
    
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//    @ManyToOne(optional = false, fetch = FetchType.EAGER)
//    @JoinColumn(name = "discente_id", nullable = false)
    private Discente discente;

//    @Column(nullable = false)//, length = 150)
    private String nomeDocumento;

    //@Column(length = 255)
    private String caminhoArquivoComprovante;

//    @Enumerated(EnumType.STRING)
//    @Column(nullable = false)//, length = 30)
    private TipoHoras tipoHoras;

//    @Column(nullable = false)
    private Integer horasSolicitadas;

    private Integer horasConcedidas;

    //@Column(columnDefinition = "TEXT")
    private String observacaoDiscente;

    //@Column(columnDefinition = "TEXT")
    private String observacaoParecerPublico; // Visível para o discente

    //@Column(columnDefinition = "TEXT")
    private String observacaoAdministrativa; // Visível APENAS para comissões/coordenação

//    @Enumerated(EnumType.STRING)
//    @Column(nullable = false)//, length = 30)
    private StatusSolicitacao status = StatusSolicitacao.CRIADA;

//    @Enumerated(EnumType.STRING)
    //@Column(length = 30)
    private StatusSolicitacao statusAnteriorAoCancelamento;

//    @Column(nullable = false)
    private LocalDateTime dataCriacao = LocalDateTime.now();

    private LocalDateTime dataUltimaAtualizacao = LocalDateTime.now();

//    @OneToMany(mappedBy = "solicitacao", cascade = CascadeType.ALL, orphanRemoval = true)
//    @OrderBy("dataRegistro DESC")
    private List<HistoricoStatus> historicos = new ArrayList<>();

//    Chamado quando o DISCENTE altera o status (Ex: CRIADA -> ENVIADA, AVALIADA -> RECURSO)
    public void atualizarStatusPorDiscente(StatusSolicitacao novoStatus, String justificativa) {
        if (novoStatus == StatusSolicitacao.CANCELADA) {
            this.statusAnteriorAoCancelamento = this.status;
        }
        this.status = novoStatus;
        this.dataUltimaAtualizacao = LocalDateTime.now();

        HistoricoStatus h = new HistoricoStatus(this, novoStatus, justificativa, this.discente.getNomeCompleto());
        this.historicos.add(h);
    }

//    Chamado quando a COMISSÃO / COORDENADOR emite parecer ou restaura cancelamento
    public void atualizarStatusPorComissao(StatusSolicitacao novoStatus, String justificativa, AtuacaoComissao atuacao) {
        if (novoStatus == StatusSolicitacao.CANCELADA) {
            this.statusAnteriorAoCancelamento = this.status;
        }
        this.status = novoStatus;
        this.dataUltimaAtualizacao = LocalDateTime.now();

        HistoricoStatus h = new HistoricoStatus(this, novoStatus, justificativa, atuacao);
        this.historicos.add(h);
    }

    // ======================================================================
    // MÁQUINA DE ESTADOS - lado do DISCENTE
    // ======================================================================

    /** Operações liberadas ao discente neste momento, conforme o status atual. */
    public List<OperacaoDiscente> getOperacoesDiscente() {
        List<OperacaoDiscente> ops = new ArrayList<>();
        switch (status) {
            case CRIADA: // único status que permite exclusão física
                ops.add(OperacaoDiscente.ENVIAR);
                ops.add(OperacaoDiscente.CANCELAR);
                ops.add(OperacaoDiscente.EXCLUIR);
                break;
            case ENVIADA: // a partir daqui não exclui mais, só cancela
            case PARECER_PENDENTE:
            case RECURSO:
            case RECURSO_COLEGIADO:
                ops.add(OperacaoDiscente.CANCELAR);
                break;
            case AVALIADA:
                ops.add(OperacaoDiscente.ACEITAR);
                ops.add(OperacaoDiscente.RECORRER);
                ops.add(OperacaoDiscente.CANCELAR);
                break;
            case AVALIADA_APOS_RECURSO:
                ops.add(OperacaoDiscente.ACEITAR);
                ops.add(OperacaoDiscente.RECORRER_COLEGIADO);
                ops.add(OperacaoDiscente.CANCELAR);
                break;
            default: // ACEITA, CONCLUIDO, CANCELADA: somente leitura
                break;
        }
        return ops;
    }

    public boolean podeSerExcluida() {
        return status == StatusSolicitacao.CRIADA;
    }

    /** Aplica uma operação do discente (exceto EXCLUIR, que remove o objeto e é feita via DAO). */
    public void aplicarOperacaoDiscente(OperacaoDiscente operacao, String justificativa) {
        if (operacao == OperacaoDiscente.EXCLUIR || !getOperacoesDiscente().contains(operacao)) {
            throw new OperacaoNaoPermitidaException(
                    "A operação \"" + operacao.getRotulo() + "\" não é permitida para uma solicitação " + status + ".");
        }
        boolean semTexto = justificativa == null || justificativa.trim().isEmpty();
        if (operacao.isExigeJustificativa() && semTexto) {
            throw new OperacaoNaoPermitidaException("Informe a justificativa do recurso.");
        }
        String texto = semTexto ? operacao.getRotulo() + " pelo discente." : justificativa.trim();

        switch (operacao) {
            case ENVIAR:
                atualizarStatusPorDiscente(StatusSolicitacao.ENVIADA, texto);
                break;
            case CANCELAR:
                atualizarStatusPorDiscente(StatusSolicitacao.CANCELADA, texto);
                break;
            case ACEITAR:
                if (status == StatusSolicitacao.AVALIADA) { // AVALIADA -> ACEITA -> CONCLUIDO
                    atualizarStatusPorDiscente(StatusSolicitacao.ACEITA, texto);
                }
                atualizarStatusPorDiscente(StatusSolicitacao.CONCLUIDO, "Processo finalizado após aceite do discente.");
                break;
            case RECORRER:
                atualizarStatusPorDiscente(StatusSolicitacao.RECURSO, texto);
                break;
            case RECORRER_COLEGIADO:
                atualizarStatusPorDiscente(StatusSolicitacao.RECURSO_COLEGIADO, texto);
                break;
            default:
                throw new OperacaoNaoPermitidaException("Operação desconhecida.");
        }
    }

    // ======================================================================
    // MÁQUINA DE ESTADOS - lado da COMISSÃO / COORDENADOR
    // ======================================================================

    public boolean isAguardandoParecer() {
        return status == StatusSolicitacao.ENVIADA || status == StatusSolicitacao.PARECER_PENDENTE
                || status == StatusSolicitacao.RECURSO || status == StatusSolicitacao.RECURSO_COLEGIADO;
    }

    /** O recurso colegiado só é decidido pelo Coordenador; os demais pela comissão do tipo ou Coordenador. */
    public boolean podeReceberParecerDe(AtuacaoComissao atuacao) {
        if (atuacao == null || !atuacao.isVigenteHoje() || !isAguardandoParecer()) return false;
        if (!atuacao.getPerfil().podeAvaliar(tipoHoras)) return false;
        return status != StatusSolicitacao.RECURSO_COLEGIADO
                || atuacao.getPerfil() == AtuacaoComissao.PerfilComissao.COORDENADOR;
    }

    public boolean podeAjustarNomeDe(AtuacaoComissao atuacao) {
        if (atuacao == null || !atuacao.isVigenteHoje() || !atuacao.getPerfil().podeAvaliar(tipoHoras)) return false;
        return status != StatusSolicitacao.CRIADA && status != StatusSolicitacao.CANCELADA
                && status != StatusSolicitacao.CONCLUIDO && status != StatusSolicitacao.ACEITA;
    }

    public boolean podeSerRestauradaPor(AtuacaoComissao atuacao) {
        return atuacao != null && atuacao.isVigenteHoje()
                && atuacao.getPerfil() == AtuacaoComissao.PerfilComissao.COORDENADOR
                && status == StatusSolicitacao.CANCELADA && statusAnteriorAoCancelamento != null;
    }

    public void emitirParecer(AtuacaoComissao atuacao, Integer horas, String parecerPublico, String obsAdministrativa) {
        if (!podeReceberParecerDe(atuacao)) {
            throw new OperacaoNaoPermitidaException("Você não pode emitir parecer para esta solicitação (status " + status + ").");
        }
        if (horas == null || horas < 0 || horas > horasSolicitadas) {
            throw new OperacaoNaoPermitidaException("Horas concedidas devem estar entre 0 e " + horasSolicitadas + ".");
        }
        if (parecerPublico == null || parecerPublico.trim().isEmpty()) {
            throw new OperacaoNaoPermitidaException("O parecer (público) é obrigatório.");
        }
        StatusSolicitacao novoStatus;
        if (status == StatusSolicitacao.RECURSO) {
            novoStatus = StatusSolicitacao.AVALIADA_APOS_RECURSO;
        } else if (status == StatusSolicitacao.RECURSO_COLEGIADO) {
            novoStatus = StatusSolicitacao.CONCLUIDO; // decisão final: conclui automaticamente
        } else {
            novoStatus = StatusSolicitacao.AVALIADA;
        }
        this.horasConcedidas = horas;
        this.observacaoParecerPublico = parecerPublico.trim();
        this.observacaoAdministrativa = (obsAdministrativa == null || obsAdministrativa.trim().isEmpty())
                ? null : obsAdministrativa.trim();
        // Só o parecer público vai para a trilha: o histórico pode ser exibido ao discente.
        atualizarStatusPorComissao(novoStatus, horas + "h concedidas. " + this.observacaoParecerPublico, atuacao);
    }

    public void ajustarNomeDocumento(AtuacaoComissao atuacao, String novoNome) {
        if (!podeAjustarNomeDe(atuacao)) {
            throw new OperacaoNaoPermitidaException("Você não pode alterar o nome do documento desta solicitação.");
        }
        if (novoNome == null || novoNome.trim().isEmpty()) {
            throw new OperacaoNaoPermitidaException("Informe o novo nome do documento.");
        }
        String anterior = this.nomeDocumento;
        this.nomeDocumento = novoNome.trim();
        atualizarStatusPorComissao(status, "Nome do documento alterado de \"" + anterior + "\" para \"" + this.nomeDocumento + "\".", atuacao);
    }

    /** Só o Coordenador restaura, e volta exatamente ao status anterior ao cancelamento. */
    public void restaurar(AtuacaoComissao coordenador) {
        if (!podeSerRestauradaPor(coordenador)) {
            throw new OperacaoNaoPermitidaException("Somente o Coordenador pode restaurar uma solicitação cancelada.");
        }
        StatusSolicitacao destino = statusAnteriorAoCancelamento;
        this.statusAnteriorAoCancelamento = null;
        atualizarStatusPorComissao(destino, "Solicitação restaurada pelo Coordenador ao status " + destino + ".", coordenador);
    }

    // ======================================================================
    // DUPLICIDADE
    // ======================================================================

    /** Compara nomes normalizados (sem acento, caixa ou extensão): igual ou um contido no outro. */
    public boolean possuiDocumentoSemelhanteA(Solicitacao outra) {
        String a = normalizar(this.nomeDocumento);
        String b = normalizar(outra.nomeDocumento);
        if (a.isEmpty() || b.isEmpty()) return false;
        return a.equals(b) || (Math.min(a.length(), b.length()) >= 8 && (a.contains(b) || b.contains(a)));
    }

    private static String normalizar(String texto) {
        if (texto == null) return "";
        String s = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase();
        s = s.replaceAll("\\.(pdf|png|jpe?g)$", "");
        return s.replaceAll("[^a-z0-9]+", " ").trim();
    }

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public String getDataCriacaoFormatada() { return dataCriacao.format(FORMATO); }
    public String getDataUltimaAtualizacaoFormatada() { return dataUltimaAtualizacao.format(FORMATO); }

    // Getters e Setters ...
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Discente getDiscente() { return discente; }
    public void setDiscente(Discente discente) { this.discente = discente; }
    public String getNomeDocumento() { return nomeDocumento; }
    public void setNomeDocumento(String nomeDocumento) { this.nomeDocumento = nomeDocumento; }
    public String getCaminhoArquivoComprovante() { return caminhoArquivoComprovante; }
    public void setCaminhoArquivoComprovante(String caminhoArquivoComprovante) { this.caminhoArquivoComprovante = caminhoArquivoComprovante; }
    public TipoHoras getTipoHoras() { return tipoHoras; }
    public void setTipoHoras(TipoHoras tipoHoras) { this.tipoHoras = tipoHoras; }
    public Integer getHorasSolicitadas() { return horasSolicitadas; }
    public void setHorasSolicitadas(Integer horasSolicitadas) { this.horasSolicitadas = horasSolicitadas; }
    public Integer getHorasConcedidas() { return horasConcedidas; }
    public void setHorasConcedidas(Integer horasConcedidas) { this.horasConcedidas = horasConcedidas; }
    public String getObservacaoDiscente() { return observacaoDiscente; }
    public void setObservacaoDiscente(String observacaoDiscente) { this.observacaoDiscente = observacaoDiscente; }
    public String getObservacaoParecerPublico() { return observacaoParecerPublico; }
    public void setObservacaoParecerPublico(String observacaoParecerPublico) { this.observacaoParecerPublico = observacaoParecerPublico; }
    public String getObservacaoAdministrativa() { return observacaoAdministrativa; }
    public void setObservacaoAdministrativa(String observacaoAdministrativa) { this.observacaoAdministrativa = observacaoAdministrativa; }
    public StatusSolicitacao getStatus() { return status; }
    public void setStatus(StatusSolicitacao status) { this.status = status; }
    public StatusSolicitacao getStatusAnteriorAoCancelamento() { return statusAnteriorAoCancelamento; }
    public void setStatusAnteriorAoCancelamento(StatusSolicitacao statusAnteriorAoCancelamento) { this.statusAnteriorAoCancelamento = statusAnteriorAoCancelamento; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }
    public LocalDateTime getDataUltimaAtualizacao() { return dataUltimaAtualizacao; }
    public void setDataUltimaAtualizacao(LocalDateTime dataUltimaAtualizacao) { this.dataUltimaAtualizacao = dataUltimaAtualizacao; }
    public List<HistoricoStatus> getHistoricos() { return historicos; }
    public void setHistoricos(List<HistoricoStatus> historicos) { this.historicos = historicos; }
    
}