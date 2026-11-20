<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>SGHA - Painel da Comissão</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css">
    <link rel="stylesheet" href="${ctx}/css/sgha.css">
</head>
<body class="bg-light">

<nav class="navbar navbar-expand-lg navbar-dark bg-dark shadow-sm">
    <div class="container">
        <a class="navbar-brand fw-bold text-warning" href="${ctx}/comissao">SGHA | Módulo de Gestão</a>
        <span class="navbar-text text-white">
            <i class="bi bi-shield-lock-fill text-warning"></i>
            <strong><c:out value="${atuacao.perfil.rotulo}"/></strong> /
            <c:out value="${atuacao.membro.nomeCompleto}"/>
            <small class="text-white-50">
                (mandato desde ${atuacao.dataInicio}
                <c:choose>
                    <c:when test="${empty atuacao.dataValidade}">, sem data final</c:when>
                    <c:otherwise>, válido até ${atuacao.dataValidade}</c:otherwise>
                </c:choose>)
            </small>
            <a class="btn btn-sm btn-outline-warning ms-3" href="${ctx}/logout"><i class="bi bi-box-arrow-right"></i> Sair</a>
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

    <!-- Filtros: nome/matrícula do discente e tipo de horas -->
    <div class="card border-0 shadow-sm mb-4">
        <div class="card-body">
            <form class="row g-3 align-items-center" action="${ctx}/comissao" method="GET">
                <div class="col-md-5">
                    <input type="text" class="form-control" name="q" value="<c:out value='${termo}'/>"
                           placeholder="Buscar por nome do discente ou matrícula...">
                </div>
                <div class="col-md-4">
                    <select class="form-select" name="tipo">
                        <option value="">Todas as comissões</option>
                        <c:forEach items="${tipos}" var="t">
                            <option value="${t}" ${tipoFiltro == t.name() ? 'selected' : ''}><c:out value="${t.rotulo}"/></option>
                        </c:forEach>
                    </select>
                </div>
                <div class="col-md-3">
                    <button type="submit" class="btn btn-dark w-100"><i class="bi bi-search"></i> Pesquisar</button>
                </div>
            </form>
        </div>
    </div>

    <!-- Fila: só o que o perfil pode ver -->
    <div class="card border-0 shadow-sm mb-4">
        <div class="table-responsive">
            <table class="table table-hover align-middle mb-0">
                <thead class="table-dark">
                    <tr>
                        <th>#ID</th><th>Discente</th><th>Documento</th><th>Comissão</th>
                        <th>Horas (sol/conc)</th><th>Status</th><th></th>
                    </tr>
                </thead>
                <tbody>
                    <c:if test="${empty solicitacoes}">
                        <tr><td colspan="7" class="text-center text-muted py-4">Nenhuma solicitação encontrada para o seu perfil.</td></tr>
                    </c:if>
                    <c:forEach items="${solicitacoes}" var="s">
                        <tr class="${not empty selecionada and selecionada.id == s.id ? 'table-warning' : ''}">
                            <td>#${s.id}</td>
                            <td><c:out value="${s.discente.nomeCompleto}"/><div class="small text-muted"><c:out value="${s.discente.matricula}"/></div></td>
                            <td><c:out value="${s.nomeDocumento}"/></td>
                            <td><span class="badge bg-secondary"><c:out value="${s.tipoHoras.rotulo}"/></span></td>
                            <td>
                                ${s.horasSolicitadas}h /
                                <c:choose>
                                    <c:when test="${empty s.horasConcedidas}">--</c:when>
                                    <c:otherwise>${s.horasConcedidas}h</c:otherwise>
                                </c:choose>
                            </td>
                            <td><span class="badge status-${s.status}">${s.status}</span></td>
                            <td><a class="btn btn-sm btn-outline-dark"
                                   href="${ctx}/comissao?id=${s.id}&q=<c:out value='${termo}'/>&tipo=${tipoFiltro}">Abrir</a></td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>
    </div>

    <!-- Análise da solicitação selecionada -->
    <c:if test="${not empty selecionada}">
        <div class="card border-0 shadow-sm border-start border-4 border-warning mb-4">
            <div class="card-header bg-white d-flex justify-content-between align-items-center">
                <h5 class="fw-bold mb-0">Solicitação #${selecionada.id}</h5>
                <span class="badge fs-6 status-${selecionada.status}">${selecionada.status}</span>
            </div>
            <div class="card-body">

                <!-- Alerta automático de duplicidade cruzada -->
                <c:if test="${not empty duplicidades}">
                    <div class="alert alert-danger" role="alert">
                        <strong><i class="bi bi-exclamation-triangle-fill"></i> POSSÍVEL DUPLICIDADE DE DOCUMENTO!</strong>
                        O discente <strong><c:out value="${selecionada.discente.nomeCompleto}"/></strong> tem outra solicitação com documento semelhante:
                        <ul class="mb-0 mt-2">
                            <c:forEach items="${duplicidades}" var="d">
                                <li>
                                    #${d.id} - "<c:out value="${d.nomeDocumento}"/>" na <strong><c:out value="${d.tipoHoras.descricao}"/></strong>,
                                    status <span class="badge status-${d.status}">${d.status}</span>,
                                    ${d.horasSolicitadas}h solicitadas,
                                    <c:choose>
                                        <c:when test="${empty d.horasConcedidas}">sem horas concedidas ainda</c:when>
                                        <c:otherwise>${d.horasConcedidas}h concedidas</c:otherwise>
                                    </c:choose>.
                                    <c:if test="${not empty d.observacaoParecerPublico}">
                                        Parecer: <em><c:out value="${d.observacaoParecerPublico}"/></em>
                                    </c:if>
                                </li>
                            </c:forEach>
                        </ul>
                    </div>
                </c:if>

                <div class="row">
                    <div class="col-md-6">
                        <p class="mb-1"><strong>Discente:</strong> <c:out value="${selecionada.discente.nomeCompleto}"/> (<c:out value="${selecionada.discente.matricula}"/>)</p>
                        <p class="mb-1"><strong>Documento:</strong> <c:out value="${selecionada.nomeDocumento}"/>
                            <c:if test="${not empty selecionada.caminhoArquivoComprovante}">
                                <a href="${ctx}/arquivo?id=${selecionada.id}" target="_blank"><i class="bi bi-paperclip"></i> ver comprovante</a>
                            </c:if>
                        </p>
                        <p class="mb-1"><strong>Comissão destino:</strong> <c:out value="${selecionada.tipoHoras.descricao}"/></p>
                        <p class="mb-1"><strong>Horas:</strong> ${selecionada.horasSolicitadas}h solicitadas</p>
                        <p class="mb-1"><strong>Observação do discente:</strong> <c:out value="${selecionada.observacaoDiscente}" default="-"/></p>
                    </div>
                    <div class="col-md-6">
                        <p class="mb-1"><strong>Parecer público</strong> (visível ao discente):<br>
                            <c:out value="${selecionada.observacaoParecerPublico}" default="-"/></p>
                        <p class="mb-1"><strong>Observação administrativa</strong> (privada: coordenação, secretaria e comissões):<br>
                            <c:out value="${selecionada.observacaoAdministrativa}" default="-"/></p>
                    </div>
                </div>

                <hr>

                <!-- Ajuste do nome do documento -->
                <c:if test="${podeAjustarNome}">
                    <form class="row g-2 mb-3" action="${ctx}/comissao/acao" method="POST">
                        <input type="hidden" name="id" value="${selecionada.id}">
                        <input type="hidden" name="acao" value="AJUSTAR_NOME">
                        <div class="col-md-9">
                            <label class="form-label fw-semibold" for="novoNome">Corrigir nome do documento (se divergir do comprovante)</label>
                            <input type="text" class="form-control" id="novoNome" name="novoNome"
                                   value="<c:out value='${selecionada.nomeDocumento}'/>" required>
                        </div>
                        <div class="col-md-3 d-flex align-items-end">
                            <button class="btn btn-outline-dark w-100" type="submit">Salvar nome</button>
                        </div>
                    </form>
                </c:if>

                <!-- Parecer -->
                <c:if test="${podeParecer}">
                    <form action="${ctx}/comissao/acao" method="POST" class="border rounded p-3 bg-white">
                        <h6 class="fw-bold">
                            <c:choose>
                                <c:when test="${selecionada.status == 'RECURSO_COLEGIADO'}">Decisão final do Colegiado / Coordenador</c:when>
                                <c:when test="${selecionada.status == 'RECURSO'}">Parecer do recurso</c:when>
                                <c:otherwise>Emitir parecer</c:otherwise>
                            </c:choose>
                        </h6>
                        <input type="hidden" name="id" value="${selecionada.id}">
                        <input type="hidden" name="acao" value="PARECER">
                        <div class="row g-3">
                            <div class="col-md-3">
                                <label class="form-label" for="horasConcedidas">Horas concedidas (0 a ${selecionada.horasSolicitadas})</label>
                                <input type="number" class="form-control" id="horasConcedidas" name="horasConcedidas"
                                       min="0" max="${selecionada.horasSolicitadas}" value="${selecionada.horasSolicitadas}" required>
                            </div>
                            <div class="col-md-9">
                                <label class="form-label" for="parecerPublico">Observação do parecer (pública)</label>
                                <textarea class="form-control" id="parecerPublico" name="parecerPublico" rows="2" required></textarea>
                            </div>
                            <div class="col-12">
                                <label class="form-label" for="observacaoAdministrativa">Observação administrativa (privada)</label>
                                <textarea class="form-control" id="observacaoAdministrativa" name="observacaoAdministrativa" rows="2"></textarea>
                            </div>
                        </div>
                        <button class="btn btn-warning mt-3 fw-bold" type="submit">Registrar parecer</button>
                    </form>
                </c:if>

                <!-- Restauração: prerrogativa exclusiva do Coordenador -->
                <c:if test="${podeRestaurar}">
                    <form action="${ctx}/comissao/acao" method="POST" class="mt-3">
                        <input type="hidden" name="id" value="${selecionada.id}">
                        <input type="hidden" name="acao" value="RESTAURAR">
                        <button class="btn btn-success" type="submit"><i class="bi bi-arrow-counterclockwise"></i>
                            Restaurar solicitação cancelada ao status anterior</button>
                    </form>
                </c:if>

                <c:if test="${not podeParecer and not podeAjustarNome and not podeRestaurar}">
                    <div class="alert alert-secondary mb-0">
                        Nenhuma ação disponível para o seu perfil nesta solicitação (consulta apenas ou fora da fase de parecer).
                    </div>
                </c:if>

                <hr>
                <h6 class="fw-bold">Trilha de auditoria</h6>
                <ul class="small mb-0">
                    <c:forEach items="${selecionada.historicos}" var="h">
                        <li>
                            <strong>${h.dataRegistroFormatada}</strong> -
                            <span class="badge status-${h.status}">${h.status}</span>
                            por <c:out value="${h.alteradoPor}"/>:
                            <c:out value="${h.observacaoOuParecer}"/>
                        </li>
                    </c:forEach>
                </ul>
            </div>
        </div>
    </c:if>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
