# SGHA — Entregável 2 (modelagem + projeto Java Web inicial)

Java 11+ · Servlets/JSP/JSTL (Jakarta, **Tomcat 10.1**) · Gradle (WAR) · dados **em memória** (sem banco ainda).

## Como rodar

**Eclipse:** File → Import → *Gradle → Existing Gradle Project* (Buildship baixa o Gradle sozinho).
**Linha de comando** (com Gradle ≥ 8.5 instalado):

```
gradle testeDominio     # roda a classe com main (valida o modelo, sem servidor)
gradle war              # gera build/libs/sgha.war
```

Copie `build/libs/sgha.war` para `TOMCAT/webapps/` e abra http://localhost:8080/sgha/
(Tomcat **10.1** — ele usa `jakarta.servlet`; no Tomcat 9 os imports não funcionam.)

Os comprovantes vão para `~/uploads/sgha/` (fora do Tomcat). Para mudar: `-Dsgha.upload.dir=/caminho`.

## Contas de teste (pré-cadastradas em `BancoMemoria`)

| Usuário | Senha | Perfil |
|---|---|---|
| `cmendes` | 123456 | Comissão de Extensão |
| `alima` | 123456 | Comissão de Atividades Complementares |
| `frocha` | 123456 | Comissão de Estágio |
| `tfranca` | admin123 | Coordenador (já foi da Extensão: 2 mandatos) |
| `mteixeira` | 123456 | Secretário (só consulta) |
| `hsouza` | 123456 | **Mandato vencido ontem → login bloqueado** |

Discente de exemplo: matrícula `2024005412` (já tem solicitações; duas delas têm o mesmo documento em comissões diferentes, para ver o alerta de duplicidade). Qualquer outra matrícula cria cadastro novo.

## Mapa do enunciado → código

| Exigência do Entregável 2 | Onde está |
|---|---|
| Entidades, enums, vigência, auditoria | `model/` (`AtuacaoComissao.isVigenteHoje()`, `HistoricoStatus`, `StatusSolicitacao`, `TipoHoras`) |
| Classe com `main` simulando o modelo | `util/TesteDominioSGHA` |
| Controladores acessando o modelo, persistência em memória | `controller/*Servlet`, `dao/*DAO` + `dao/BancoMemoria` |
| JSP/HTML: portal e formulários | `webapp/index.jsp`, `WEB-INF/views/discente.jsp`, `comissao.jsp` |
| Upload com `@MultipartConfig`, fora do container | `controller/SolicitacaoServlet` + `util/ArmazenamentoArquivo` |
| Sessão de 2 min + logout | `WEB-INF/web.xml` + `controller/LogoutServlet` |

Regras de negócio (status, permissões, duplicidade) ficam no **model** (`Solicitacao`, `AtuacaoComissao`); os servlets só traduzem a requisição; os JSPs só exibem.

## O que mudou em relação ao exemplo do professor

- `PerfilComissao` ganhou rótulo e as regras `podeAvaliar` / `podeConsultar` (quem vê/avalia qual comissão).
- `Solicitacao` ganhou a máquina de estados (`getOperacoesDiscente`, `aplicarOperacaoDiscente`, `emitirParecer`, `ajustarNomeDocumento`, `restaurar`, `possuiDocumentoSemelhanteA`).
- Novos: `OperacaoDiscente`, `OperacaoNaoPermitidaException`; `TipoHoras` com horas exigidas (60/120/300, valores do protótipo — confirme com o PPC).
- As anotações JPA continuam **comentadas** (entram no Entregável 3).
- Na sessão guardamos só o ID; a cada requisição o usuário é recarregado e a vigência é rechecada (mandato que vence com a pessoa logada perde o acesso).

## Ainda não feito (fica para o Entregável 3 / final)

- Persistência real: JPA/Hibernate + MySQL (hoje os dados somem ao reiniciar o Tomcat).
- Operação **Alterar** da solicitação em CRIADA (o enunciado lista; não há tela de edição).
- Cancelamento administrativo de solicitação CONCLUÍDO.
- Senhas em texto puro e sem HTTPS: permitido pelo enunciado, não use fora do trabalho.
