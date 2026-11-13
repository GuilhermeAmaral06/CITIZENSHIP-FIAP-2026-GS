# Fila Zero API

> **Chegue na hora da sua vez.** API REST do Fila Zero: senha digital na UBS, consultas com
> encaixe automático pela lista de espera e previsão de faltas.

Global Solution FIAP 2026.2 (tema CitizenTech) · Domain Driven Design – Java ·
Engenharia de Software · 2º ano.

| | |
|---|---|
| **Repositório** | https://github.com/GuilhermeAmaral06/CITIZENSHIP-FIAP-2026-GS |
| **Deploy (Render)** | _preencher após o deploy_ |
| **Teste rápido** | abra o link do deploy (página com todos os endpoints) ou `/health` |

## Integrantes

| Nome | RM |
|---|---|
| Diego Bondezan | RM562013 |
| Diogo Weyne | RM572347 |
| Felipe Otavio Garcia Madeira | RM563521 |
| Guilherme Amaral | RM562112 |
| João Victor Cazarini Del Bello Cardozo | RM562543 |

## O que a API faz

Quem depende da UBS passa horas na sala de espera sem saber quanto falta, enquanto consultas
agendadas ficam vazias porque outros pacientes faltam sem avisar. A API cuida de três frentes:

- **Senhas:** o paciente retira uma senha (app ou totem) e vê quantas pessoas estão na frente e a
  espera estimada; a recepção chama as senhas intercalando preferenciais e normais.
- **Consultas:** agendamento em horário livre, confirmação, registro de falta e cancelamento.
  Ao cancelar, a vaga vai automaticamente para o primeiro da lista de espera.
- **Previsão de faltas:** mostra o risco de falta de cada consulta (calculado pelo notebook de
  Data Science) e quantos lembretes ela deve receber.

## Camadas

Pacote base: `br.com.fiap.filazero`

| Camada | Pacote | O que faz |
|---|---|---|
| Model | `model` | Uma classe para cada tabela (`Ubs`, `Servico`, `Especialidade`, `Profissional`, `Paciente`, `HorarioAgenda`, `Consulta`, `Lembrete`, `PrevisaoFalta`, `Senha`, `ListaEspera`) e os enums com os mesmos valores dos CHECKs do banco |
| DAO | `dao` | `ConnectionFactory` e um DAO JDBC por tabela usada pela API. O DAO só acessa o banco |
| BO | `bo` | `UbsBO`, `SenhaBO`, `ConsultaBO`, `ListaEsperaBO` e `PrevisaoBO`: concentram as regras RN01 a RN07 |
| Controller | `controller` | Endpoints REST com GET, POST, PUT e DELETE |
| Exceções | `exception` | Exceções próprias e o `GlobalExceptionHandler` (`@RestControllerAdvice`) |
| Apoio | `dto`, `config`, `util` | Corpos das requisições com validação, CORS, Swagger e data/hora de Brasília |

Fluxo de uma requisição: `Controller → BO → DAO → Oracle`.

Cada operação do BO roda dentro de uma transação (`dao/Transacao`): usa uma única conexão e, se
qualquer passo falhar, nada é gravado. É isso que mantém o cancelamento, a liberação do horário
e o encaixe (RN03) sempre juntos.

## Ferramentas e versões

| Ferramenta | Versão |
|---|---|
| Java JDK | 21 |
| Spring Boot | 3.5.16 (Spring Web, Validation) |
| Maven | 3.9 (incluso pelo Maven Wrapper: `mvnw`) |
| Banco de dados | Oracle (servidor da FIAP: `oracle.fiap.com.br:1521:ORCL`) |
| Driver | Oracle JDBC `ojdbc11` |
| Página de testes | springdoc-openapi 2.10.0 (Swagger UI) |
| IDE | Visual Studio Code + Extension Pack for Java + Spring Boot Extension Pack |
| Deploy | Render (Docker) |

## Banco de dados

A pasta `database/` traz os scripts da entrega de Database Design, sem alterações:

| Script | Para que serve |
|---|---|
| `ddl.sql` | Cria as 11 tabelas, as 15 chaves estrangeiras e as 11 sequences. É reexecutável: remove antes os objetos do Fila Zero que já existirem |
| `dml.sql` | Carga de exemplo (120 linhas), com IDs abaixo de 1000 |

Os registros criados pela API usam as sequences, que começam em 1000, e por isso nunca colidem
com a carga de exemplo.

## Como executar (passo a passo no VS Code)

1. **Instale** o [JDK 21](https://adoptium.net/) (marque a opção de configurar o `JAVA_HOME`), o
   [VS Code](https://code.visualstudio.com/) e o [Git](https://git-scm.com/).
2. **Clone** o repositório e abra a pasta no VS Code (`File > Open Folder`):
   ```
   git clone https://github.com/GuilhermeAmaral06/CITIZENSHIP-FIAP-2026-GS.git
   ```
3. **Instale as extensões** que o VS Code sugerir para o projeto (Extension Pack for Java e
   Spring Boot Extension Pack) e aguarde o projeto ser importado (barra inferior).
4. **Crie o banco** na sua conta do Oracle da FIAP: no SQL Developer, abra `database/ddl.sql` e
   execute com **Run Script (F5)**; depois faça o mesmo com `database/dml.sql`.
   Conexão: host `oracle.fiap.com.br`, porta `1521`, SID `ORCL`.
   Se o banco já foi criado para a entrega de Database Design, este passo já está feito.
5. **Configure o acesso ao banco**: copie `.env.example` com o nome `.env` e preencha:
   ```
   DB_URL=jdbc:oracle:thin:@oracle.fiap.com.br:1521:ORCL
   DB_USER=rm000000
   DB_PASSWORD=sua_senha
   ```
   O `.env` está no `.gitignore`: a senha nunca vai para o GitHub. Também é possível definir
   `DB_URL`, `DB_USER` e `DB_PASSWORD` como variáveis de ambiente, como no Render.
6. **Execute**: abra `src/main/java/br/com/fiap/filazero/FilaZeroApplication.java` e clique em
   **Run** (▶), ou rode no terminal do VS Code:
   ```
   .\mvnw.cmd spring-boot:run        (Windows)
   ./mvnw spring-boot:run            (Mac / Linux)
   ```
7. **Teste**: abra http://localhost:8080 — a página lista todos os endpoints e permite executar
   cada um pelo botão **Try it out**. O `/health` mostra se o banco foi configurado.

Testes automatizados (não usam o Oracle: sobem um banco H2 em memória com os mesmos scripts):
`.\mvnw.cmd test` (Windows) ou `./mvnw test` (Mac / Linux).

## Endpoints

| Método | Rota | Descrição | Respostas |
|---|---|---|---|
| GET | `/health` | Verifica se a API está no ar (e "acorda" o Render) | 200 |
| GET | `/ubs` | Lista as UBS | 200 |
| GET | `/ubs/{id}` | Busca uma UBS | 200 · 404 |
| POST | `/ubs` | Cadastra uma UBS | 201 · 400 · 409 |
| PUT | `/ubs/{id}` | Atualiza uma UBS | 200 · 400 · 404 · 409 |
| DELETE | `/ubs/{id}` | Remove uma UBS | 204 · 404 · 409 |
| GET | `/ubs/{id}/espera` | Espera estimada por serviço (RN05) | 200 · 404 |
| POST | `/senhas` | Retira uma senha (RN05 e RN06) | 201 · 400 |
| GET | `/senhas/{id}` | Posição na fila e espera estimada | 200 · 404 |
| PUT | `/senhas/{id}` | Recepção chama ou encerra a senha | 200 · 400 · 404 · 409 |
| GET | `/consultas?ubs={id}&data={aaaa-mm-dd}&status={st}` | Lista consultas com filtro | 200 · 400 |
| GET | `/consultas/{id}` | Busca uma consulta | 200 · 404 |
| POST | `/consultas` | Agenda uma consulta (RN01 e RN02) | 201 · 400 · 422 |
| PUT | `/consultas/{id}` | Confirma ou registra realizada ou falta (RN04) | 200 · 400 · 404 · 409 |
| DELETE | `/consultas/{id}` | Cancela, libera o horário e encaixa (RN03) | 204 · 404 · 409 |
| POST | `/lista-espera` | Entra na lista de espera | 201 · 400 · 409 |
| GET | `/lista-espera?ubs={id}&especialidade={id}` | Lista a fila de espera | 200 |
| DELETE | `/lista-espera/{id}` | Sai da lista de espera | 204 · 404 · 409 |
| GET | `/previsoes-falta?ubs={id}&data={aaaa-mm-dd}` | Probabilidade e nível de risco por consulta (RN07) | 200 · 400 |

Todos os filtros das listagens são opcionais.

### Corpos das requisições

`POST /ubs` e `PUT /ubs/{id}`
```json
{
  "nome": "UBS Vila Nova",
  "endereco": "Rua Um, 10",
  "bairro": "Lajeado",
  "latitude": -23.5,
  "longitude": -46.4,
  "horaAbertura": "07:00",
  "horaFechamento": "18:00"
}
```

`POST /senhas` — `idPaciente` e `prioridade` são opcionais; `origem` é `APP` ou `TOTEM`
```json
{ "idUbs": 1, "idServico": 2, "idPaciente": 2, "prioridade": "NORMAL", "origem": "APP" }
```

`PUT /senhas/{id}` — `CHAMADA`, `ATENDIDA` ou `DESISTIU`
```json
{ "status": "CHAMADA" }
```

`POST /consultas`
```json
{ "idPaciente": 2, "idHorario": 116 }
```

`PUT /consultas/{id}` — `CONFIRMADA`, `REALIZADA` ou `FALTOU`
```json
{ "status": "CONFIRMADA" }
```

`POST /lista-espera`
```json
{ "idPaciente": 8, "idUbs": 1, "idEspecialidade": 2 }
```

### Roteiro para demonstrar a RN03 com a carga de exemplo

1. `POST /lista-espera` com `{"idPaciente": 8, "idUbs": 1, "idEspecialidade": 2}` — o paciente
   PAC-0008 entra na lista de pediatria da UBS 1.
2. `DELETE /consultas/222` — cancela a consulta de pediatria de 16/11 às 08:20 (horário 122).
3. `GET /consultas?ubs=1&data=2026-11-16` — a 222 aparece `CANCELADA` e existe uma consulta nova
   no mesmo horário, do PAC-0008, com `"encaixe": true`.
4. `GET /lista-espera?ubs=1&especialidade=2` — o PAC-0003 continua aguardando: ele estava na
   frente, mas já tinha pediatria nesse dia (RN02), então a vaga foi para o próximo.

Para voltar ao estado inicial, execute de novo `ddl.sql` e `dml.sql`.

## Regras de negócio

| Regra | O que diz | Onde está |
|---|---|---|
| RN01 | Só é possível agendar num horário `LIVRE`; ao agendar, o horário passa a `OCUPADO` | `ConsultaBO.agendar` |
| RN02 | O mesmo paciente não pode ter duas consultas da mesma especialidade no mesmo dia (consulta cancelada não conta) | `ConsultaBO.agendar` |
| RN03 | Ao cancelar, o horário volta a `LIVRE` e o primeiro da lista de espera da mesma UBS e especialidade é encaixado, com `encaixe = true`. Se não houver ninguém, o horário fica livre | `ConsultaBO.cancelar` e `ListaEsperaBO.encaixarPrimeiroDaLista` |
| RN04 | Consulta `REALIZADA`, `FALTOU` ou `CANCELADA` não pode mais ser alterada | `ConsultaBO.atualizarStatus` e `cancelar` |
| RN05 | Espera estimada da senha = pessoas à frente × tempo médio do serviço | `SenhaBO.calcularEspera` |
| RN06 | Senhas preferenciais (Lei 10.048/2000) são chamadas intercaladas com as normais: uma preferencial a cada duas normais | `SenhaBO.ordenarFila` |
| RN07 | Consulta com risco Alto recebe dois lembretes (48 h e 24 h antes); as demais recebem um | `PrevisaoBO.definirLembretes` |

Detalhes de implementação:

- **RN03:** a ordem da lista é maior prioridade primeiro e, no empate, quem entrou antes. Quem
  tem prioridade legal entra na lista com peso 5. No encaixe são pulados quem já tem consulta da
  especialidade no dia (RN02) e o próprio paciente que acabou de cancelar.
- **RN06:** a primeira preferencial do dia é chamada antes das normais. A recepção só consegue
  chamar a próxima senha da fila; `GET /ubs/{id}/espera` informa qual é.
- **RN07:** `GET /previsoes-falta` devolve, para cada consulta, os lembretes já enviados, quantos
  ela deve receber e as datas. Consulta já finalizada não recebe mais lembrete.
- **Risco de falta:** 1 Baixo (< 0,20) · 2 Médio (0,20 a 0,35) · 3 Alto (≥ 0,35), igual ao banco.
- **LGPD:** o paciente é identificado só pelo código anônimo, e os dados de saúde e de benefício
  social (hipertensão, diabetes, Bolsa Família) não são devolvidos nas respostas da API.

## Tratamento de exceções

Todo erro volta no mesmo JSON:

```json
{
  "timestamp": "2026-11-11T17:48:28",
  "status": 422,
  "erro": "Unprocessable Entity",
  "mensagem": "O horário 116 não está livre (situação: OCUPADO). Só é possível agendar em horário LIVRE (RN01).",
  "caminho": "/consultas"
}
```

| Status | Quando acontece | Exceção |
|---|---|---|
| 400 | Dados inválidos: campo obrigatório ausente, formato errado, id inexistente no corpo, JSON mal formatado | validação (`@Valid`) e `DadosInvalidosException` |
| 404 | Registro ou endereço não encontrado | `RecursoNaoEncontradoException` |
| 405 | Método HTTP não permitido para o endereço | — |
| 409 | A situação atual do registro não permite a operação (RN04, senha fora da vez, duplicidade, UBS em uso) | `ConflitoException` |
| 422 | Regra de negócio impede o agendamento (RN01 e RN02) | `RegraNegocioException` |
| 500 | Falha de acesso ao banco de dados ou erro inesperado | `BancoDadosException` |

## Deploy no Render

Usuário e senha do Oracle nunca ficam no código nem no GitHub: a `ConnectionFactory` lê as
variáveis de ambiente `DB_URL`, `DB_USER` e `DB_PASSWORD`.

1. Envie o código para o GitHub (branch `main`).
2. Em https://render.com crie um **New > Web Service** e conecte este repositório.
3. Runtime: **Docker** (o `Dockerfile` está na raiz). Plano: **Free**.
4. Em **Environment**, crie as variáveis:
   | Variável | Valor |
   |---|---|
   | `DB_URL` | `jdbc:oracle:thin:@oracle.fiap.com.br:1521:ORCL` |
   | `DB_USER` | `rm` + RM |
   | `DB_PASSWORD` | senha do Oracle |
5. Clique em **Deploy** e, ao terminar, abra `https://<nome>.onrender.com/health`.

O plano gratuito do Render "dorme" depois de um tempo sem uso: a primeira requisição pode levar
cerca de um minuto. A API não está fora do ar, só está acordando.
