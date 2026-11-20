<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>SGHA - Painel do Discente</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css">
    <link rel="stylesheet" href="${ctx}/css/sgha.css">
</head>
<body class="bg-light">

<nav class="navbar navbar-expand-lg navbar-dark bg-primary shadow-sm">
    <div class="container">
        <a class="navbar-brand fw-bold" href="${ctx}/discente">SGHA | Sistema de Horas</a>
        <span class="navbar-text text-white">
            <i class="bi bi-person-circle"></i> Discente:
            <strong><c:out value="${discente.nomeCompleto}"/></strong>
            (Matrícula: <c:out value="${discente.matricula}"/>)
            <a class="btn btn-sm btn-outline-light ms-3" href="${ctx}/logout"><i class="bi bi-box-arrow-right"></i> Sair</a>
        </span>
    </div>
</nav>

<div class="container my-4">

    <c:if test="${not empty flashTexto}">
        <div class="alert alert-${flashTipo} alert-dismissible fade show" role="alert">
            <c:out value="${flashTexto}"/>
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    </c:if>

    <!-- Consolidação de carga horária (todas as comissões somadas; só conta CONCLUIDO) -->
    <div class="row mb-4">
        <c:forEach items="${tipos}" var="tipo">
            <div class="col-md-4">
                <div class="card border-0 shadow-sm text-center bg-white">
                    <div class="card-body">
                        <h6 class="text-muted"><c:out value="${tipo.rotulo}"/></h6>
                        <h3 class="fw-bold">${horasPorTipo[tipo]} / ${tipo.horasExigidas}h</h3>
                    </div>
                </div>
            </div>
        </c:forEach>
    </div>

    <div class="d-flex justify-content-between align-items-center mb-3">
        <h4 class="fw-bold">Minhas Solicitações</h4>
        <button class="btn btn-primary" data-bs-toggle="modal" data-bs-target="#modalNovaSolicitacao">
            <i class="bi bi-plus-lg"></i> Nova Solicitação
        </button>
    </div>

    <div class="card border-0 shadow-sm">
        <div class="card-body p-0">
            <div class="table-responsive">
                <table class="table table-hover align-middle mb-0">
                    <thead class="table-dark">
                        <tr>
                            <th>#ID</th>
                            <th>Documento</th>
                            <th>Comissão destino</th>
                            <th>Horas (sol/conc)</th>
                            <th>Status</th>
                            <th>Parecer público</th>
                            <th>Operações</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:if test="${empty solicitacoes}">
                            <tr><td colspan="7" class="text-center text-muted py-4">Nenhuma solicitação ainda. Clique em "Nova Solicitação".</td></tr>
                        </c:if>

                        <c:forEach items="${solicitacoes}" var="s">
                            <tr>
                                <td>#${s.id}</td>
                                <td>
                                    <c:out value="${s.nomeDocumento}"/>
                                    <c:if test="${not empty s.caminhoArquivoComprovante}">
                                        <a href="${ctx}/arquivo?id=${s.id}" target="_blank" title="Ver comprovante"><i class="bi bi-paperclip"></i></a>
                                    </c:if>
                                    <c:if test="${not empty s.observacaoDiscente}">
                                        <div class="small text-muted"><c:out value="${s.observacaoDiscente}"/></div>
                                    </c:if>
                                </td>
                                <td><span class="badge bg-secondary"><c:out value="${s.tipoHoras.rotulo}"/></span></td>
                                <td>
                                    ${s.horasSolicitadas}h /
                                    <c:choose>
                                        <c:when test="${empty s.horasConcedidas}">--</c:when>
                                        <c:otherwise>${s.horasConcedidas}h</c:otherwise>
                                    </c:choose>
                                </td>
                                <td><span class="badge status-${s.status}">${s.status}</span></td>
                                <td>
                                    <c:choose>
                                        <c:when test="${not empty s.observacaoParecerPublico}"><c:out value="${s.observacaoParecerPublico}"/></c:when>
                                        <c:otherwise><em class="text-muted">sem parecer</em></c:otherwise>
                                    </c:choose>
                                </td>
                                <td class="text-nowrap">
                                    <c:forEach items="${s.operacoesDiscente}" var="op">
                                        <button type="button"
                                                class="btn btn-sm ${op.destrutiva ? 'btn-outline-danger' : 'btn-outline-primary'} btn-operacao"
                                                data-id="${s.id}" data-acao="${op}"
                                                data-titulo="${op.rotulo} - solicitação #${s.id}"
                                                data-justificativa="${op.exigeJustificativa}">
                                            <c:out value="${op.rotulo}"/>
                                        </button>
                                    </c:forEach>
                                    <button class="btn btn-sm btn-link" type="button" data-bs-toggle="collapse" data-bs-target="#hist${s.id}">Histórico</button>
                                </td>
                            </tr>
                            <!-- Trilha de auditoria da solicitação -->
                            <tr class="collapse" id="hist${s.id}">
                                <td colspan="7" class="bg-white">
                                    <ul class="small mb-0">
                                        <c:forEach items="${s.historicos}" var="h">
                                            <li>
                                                <strong>${h.dataRegistroFormatada}</strong> -
                                                <span class="badge status-${h.status}">${h.status}</span>
                                                por <c:out value="${h.alteradoPor}"/>:
                                                <c:out value="${h.observacaoOuParecer}"/>
                                            </li>
                                        </c:forEach>
                                    </ul>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<!-- Modal: nova solicitação (multipart: envia o comprovante junto) -->
<div class="modal fade" id="modalNovaSolicitacao" tabindex="-1">
    <div class="modal-dialog modal-lg">
        <form class="modal-content" action="${ctx}/solicitacao" method="POST" enctype="multipart/form-data">
            <div class="modal-header">
                <h5 class="modal-title">Nova solicitação de horas</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
                <div class="mb-3">
                    <label class="form-label fw-semibold" for="nomeDocumento">Título / nome do documento</label>
                    <input type="text" class="form-control" id="nomeDocumento" name="nomeDocumento" required>
                </div>
                <div class="row">
                    <div class="col-md-8 mb-3">
                        <label class="form-label fw-semibold" for="tipoHoras">Comissão destino</label>
                        <select class="form-select" id="tipoHoras" name="tipoHoras" required>
                            <option value="" selected disabled>Selecione...</option>
                            <c:forEach items="${tipos}" var="tipo">
                                <option value="${tipo}"><c:out value="${tipo.descricao}"/></option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="col-md-4 mb-3">
                        <label class="form-label fw-semibold" for="horasSolicitadas">Horas pretendidas</label>
                        <input type="number" min="1" class="form-control" id="horasSolicitadas" name="horasSolicitadas" required>
                    </div>
                </div>
                <div class="mb-3">
                    <label class="form-label fw-semibold" for="arquivoComprovante">Comprovante (PDF, PNG ou JPG, até 5 MB)</label>
                    <input type="file" class="form-control" id="arquivoComprovante" name="arquivoComprovante"
                           accept=".pdf,.png,.jpg,.jpeg" required>
                </div>
                <div class="mb-3">
                    <label class="form-label fw-semibold" for="observacaoDiscente">Observação do discente (opcional)</label>
                    <textarea class="form-control" id="observacaoDiscente" name="observacaoDiscente" rows="3"></textarea>
                </div>
            </div>
            <div class="modal-footer">
                <button type="submit" name="acao" value="salvar" class="btn btn-outline-secondary">Salvar rascunho (CRIADA)</button>
                <button type="submit" name="acao" value="enviar" class="btn btn-primary">Enviar para a comissão</button>
            </div>
        </form>
    </div>
</div>

<!-- Modal genérico de confirmação das operações (enviar, cancelar, excluir, aceitar, recurso...) -->
<div class="modal fade" id="modalOperacao" tabindex="-1">
    <div class="modal-dialog">
        <form class="modal-content" action="${ctx}/discente/acao" method="POST">
            <div class="modal-header">
                <h5 class="modal-title" id="tituloOperacao">Confirmar</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
                <input type="hidden" name="id" id="opId">
                <input type="hidden" name="acao" id="opAcao">
                <label class="form-label" for="opJustificativa">Justificativa / observação</label>
                <textarea class="form-control" name="justificativa" id="opJustificativa" rows="3"></textarea>
                <div class="form-text" id="opAjuda"></div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-light" data-bs-dismiss="modal">Voltar</button>
                <button type="submit" class="btn btn-primary">Confirmar</button>
            </div>
        </form>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script>
    // Preenche o modal genérico com a operação clicada; recurso exige justificativa
    document.querySelectorAll('.btn-operacao').forEach(function (botao) {
        botao.addEventListener('click', function () {
            var exige = botao.dataset.justificativa === 'true';
            document.getElementById('tituloOperacao').textContent = botao.dataset.titulo;
            document.getElementById('opId').value = botao.dataset.id;
            document.getElementById('opAcao').value = botao.dataset.acao;
            document.getElementById('opJustificativa').required = exige;
            document.getElementById('opJustificativa').value = '';
            document.getElementById('opAjuda').textContent = exige ? 'Obrigatória para abrir recurso.' : 'Opcional.';
            new bootstrap.Modal(document.getElementById('modalOperacao')).show();
        });
    });
</script>
</body>
</html>
