package br.com.fiap.filazero;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes de integracao: sobem a aplicacao inteira (controller -> BO -> DAO) sobre um banco
 * H2 em memoria com a carga de exemplo da equipe (database/dml.sql). Cada teste comeca com
 * o banco recriado, entao um teste nao interfere no outro.
 *
 * Os ids usados aqui (pacientes, horarios, consultas...) sao os da carga de exemplo.
 * Registros criados pela API recebem ids a partir de 1000 (as sequences comecam em 1000).
 */
@SpringBootTest
@AutoConfigureMockMvc
class FilaZeroApiTest {

    static {
        BancoDeTeste.configurar();
    }

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void recriarBanco() throws Exception {
        BancoDeTeste.recriar();
    }

    // ------------------------------------------------------------------ Health

    @Test
    void healthRespondeEARaizLevaParaADocumentacao() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("UP")))
                .andExpect(jsonPath("$.bancoConfigurado", is(true)));
        mockMvc.perform(get("/"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "/swagger-ui.html"));
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title", is("Fila Zero API")))
                .andExpect(jsonPath("$.paths['/consultas/{id}'].delete").exists())
                .andExpect(jsonPath("$.paths['/ubs/{id}/espera'].get").exists());
    }

    // --------------------------------------------------------------------- UBS

    @Test
    void listaEBuscaUbs() throws Exception {
        mockMvc.perform(get("/ubs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)))
                .andExpect(jsonPath("$[0].nome", is("UBS Jardim Primavera")));
        mockMvc.perform(get("/ubs/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome", is("UBS Jardim das Flores")))
                .andExpect(jsonPath("$.bairro", is("Guaianases")))
                .andExpect(jsonPath("$.latitude", is(-23.5401)))
                .andExpect(jsonPath("$.horaAbertura", is("07:00")));
        mockMvc.perform(get("/ubs/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.erro", is("Not Found")))
                .andExpect(jsonPath("$.mensagem", is("UBS com id 9999 não encontrada.")))
                .andExpect(jsonPath("$.caminho", is("/ubs/9999")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void cadastraAtualizaERemoveUbs() throws Exception {
        String corpo = """
                {"nome": " UBS Vila Nova ", "endereco": "Rua Um, 10", "bairro": "Lajeado",
                 "latitude": -23.5, "longitude": -46.4, "horaAbertura": "07:00", "horaFechamento": "18:00"}
                """;
        mockMvc.perform(post("/ubs").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/ubs/1000")))
                .andExpect(jsonPath("$.id", is(1000)))
                .andExpect(jsonPath("$.nome", is("UBS Vila Nova")));

        // Nome repetido (UNIQUE uk_ubs_nome)
        mockMvc.perform(post("/ubs").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem", is("Já existe uma UBS cadastrada com o nome UBS Vila Nova.")));

        mockMvc.perform(put("/ubs/1000").contentType(MediaType.APPLICATION_JSON).content("""
                        {"nome": "UBS Vila Nova", "endereco": "Rua Um, 20", "bairro": "Lajeado",
                         "latitude": -23.5, "longitude": -46.4, "horaAbertura": "08:00", "horaFechamento": "17:00"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1000)));
        mockMvc.perform(get("/ubs/1000"))
                .andExpect(jsonPath("$.endereco", is("Rua Um, 20")))
                .andExpect(jsonPath("$.horaAbertura", is("08:00")));

        mockMvc.perform(delete("/ubs/1000")).andExpect(status().isNoContent());
        mockMvc.perform(get("/ubs/1000")).andExpect(status().isNotFound());
        mockMvc.perform(delete("/ubs/1000")).andExpect(status().isNotFound());
        mockMvc.perform(put("/ubs/1000").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isNotFound());
    }

    @Test
    void validaOsDadosDaUbs() throws Exception {
        mockMvc.perform(post("/ubs").contentType(MediaType.APPLICATION_JSON).content("""
                        {"nome": "", "endereco": "Rua Um, 10", "bairro": "Lajeado",
                         "latitude": 100, "longitude": -46.4, "horaAbertura": "7h", "horaFechamento": "18:00"}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.mensagem", containsString("O nome da UBS é obrigatório.")))
                .andExpect(jsonPath("$.mensagem", containsString("A latitude deve ficar entre -90 e 90.")))
                .andExpect(jsonPath("$.mensagem", containsString("O horário de abertura deve estar no formato HH:MM.")));

        mockMvc.perform(post("/ubs").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());

        // Abertura depois do fechamento (CHECK ck_ubs_horario)
        mockMvc.perform(post("/ubs").contentType(MediaType.APPLICATION_JSON).content("""
                        {"nome": "UBS Teste", "endereco": "Rua Um, 10", "bairro": "Lajeado",
                         "latitude": -23.5, "longitude": -46.4, "horaAbertura": "19:00", "horaFechamento": "07:00"}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem", is("O horário de abertura deve ser anterior ao de fechamento.")));

        // Renomear para o nome de outra UBS
        mockMvc.perform(put("/ubs/2").contentType(MediaType.APPLICATION_JSON).content("""
                        {"nome": "UBS Jardim das Flores", "endereco": "Avenida Central, 900", "bairro": "Lajeado",
                         "latitude": -23.5368, "longitude": -46.3951, "horaAbertura": "07:00", "horaFechamento": "19:00"}
                        """))
                .andExpect(status().isConflict());

        // UBS em uso nao pode ser removida, e continua existindo depois da tentativa
        mockMvc.perform(delete("/ubs/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro", is("Conflict")));
        mockMvc.perform(get("/ubs/1")).andExpect(status().isOk());
    }

    // ------------------------------------------------------------------ Senhas

    @Test
    void esperaPorServicoUsaACargaDeExemplo() throws Exception {
        // Na UBS 1 so a senha A007 (id 514) aguarda, no Acolhimento (15 min por atendimento)
        mockMvc.perform(get("/ubs/1/espera"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)))
                .andExpect(jsonPath("$[0].servico.nome", is("Acolhimento")))
                .andExpect(jsonPath("$[0].pessoasAguardando", is(1)))
                .andExpect(jsonPath("$[0].esperaEstimadaMin", is(15)))
                .andExpect(jsonPath("$[0].proximaSenha", is("A007")))
                .andExpect(jsonPath("$[1].servico.nome", is("Vacina")))
                .andExpect(jsonPath("$[1].pessoasAguardando", is(0)))
                .andExpect(jsonPath("$[1].proximaSenha", nullValue()));
        mockMvc.perform(get("/ubs/9999/espera")).andExpect(status().isNotFound());

        mockMvc.perform(get("/senhas/514"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo", is("A007")))
                .andExpect(jsonPath("$.status", is("AGUARDANDO")))
                .andExpect(jsonPath("$.paciente", nullValue()))
                .andExpect(jsonPath("$.pessoasNaFrente", is(0)))
                .andExpect(jsonPath("$.esperaEstimadaMin", is(0)));
        // Senha ja atendida, de paciente identificado: sem posicao na fila e sem dados de saude no JSON
        mockMvc.perform(get("/senhas/501"))
                .andExpect(jsonPath("$.status", is("ATENDIDA")))
                .andExpect(jsonPath("$.prioridade", is("PREFERENCIAL")))
                .andExpect(jsonPath("$.paciente.codigoAnonimo", is("PAC-0001")))
                .andExpect(jsonPath("$.paciente.prioritario", is(true)))
                .andExpect(jsonPath("$.paciente.hipertensao").doesNotExist())
                .andExpect(jsonPath("$.paciente.diabetes").doesNotExist())
                .andExpect(jsonPath("$.paciente.bolsaFamilia").doesNotExist())
                .andExpect(jsonPath("$.pessoasNaFrente", nullValue()));
        mockMvc.perform(get("/senhas/9999")).andExpect(status().isNotFound());
    }

    @Test
    void senhasSaoChamadasIntercalandoPreferenciaisENormais() throws Exception {
        // UBS 3 nao tem senhas na carga de exemplo. Servico 2 = Vacina (5 min por atendimento).
        int normal1 = retirarSenha("{\"idUbs\": 3, \"idServico\": 2, \"origem\": \"TOTEM\"}")
                .andExpect(jsonPath("$.codigo", is("V001")))
                .andExpect(jsonPath("$.prioridade", is("NORMAL")))
                .andExpect(jsonPath("$.status", is("AGUARDANDO")))
                .andExpect(jsonPath("$.pessoasNaFrente", is(0)))
                .andExpect(jsonPath("$.esperaEstimadaMin", is(0)))
                .andReturn().getResponse().getContentAsString().transform(FilaZeroApiTest::idDe);
        int normal2 = retirarSenha("{\"idUbs\": 3, \"idServico\": 2, \"origem\": \"APP\"}")
                .andExpect(jsonPath("$.codigo", is("V002")))
                .andExpect(jsonPath("$.pessoasNaFrente", is(1)))
                .andExpect(jsonPath("$.esperaEstimadaMin", is(5)))
                .andReturn().getResponse().getContentAsString().transform(FilaZeroApiTest::idDe);
        int normal3 = retirarSenha("{\"idUbs\": 3, \"idServico\": 2, \"origem\": \"APP\"}")
                .andExpect(jsonPath("$.pessoasNaFrente", is(2)))
                .andReturn().getResponse().getContentAsString().transform(FilaZeroApiTest::idDe);

        // RN06: a primeira preferencial do dia passa na frente das normais
        int preferencial1 = retirarSenha(
                "{\"idUbs\": 3, \"idServico\": 2, \"prioridade\": \"PREFERENCIAL\", \"origem\": \"TOTEM\"}")
                .andExpect(jsonPath("$.codigo", is("V004")))
                .andExpect(jsonPath("$.pessoasNaFrente", is(0)))
                .andReturn().getResponse().getContentAsString().transform(FilaZeroApiTest::idDe);
        mockMvc.perform(get("/senhas/" + normal1)).andExpect(jsonPath("$.pessoasNaFrente", is(1)));
        // RN05: 3 pessoas na frente x 5 min
        mockMvc.perform(get("/senhas/" + normal3))
                .andExpect(jsonPath("$.pessoasNaFrente", is(3)))
                .andExpect(jsonPath("$.esperaEstimadaMin", is(15)));

        // So a proxima da fila pode ser chamada
        mudarStatusSenha(normal1, "CHAMADA")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem", containsString("A próxima senha a ser chamada é a V004")));
        mudarStatusSenha(preferencial1, "ATENDIDA").andExpect(status().isConflict());
        mudarStatusSenha(preferencial1, "CHAMADA")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CHAMADA")))
                .andExpect(jsonPath("$.dataChamada").isNotEmpty())
                .andExpect(jsonPath("$.pessoasNaFrente", nullValue()));
        mudarStatusSenha(preferencial1, "CHAMADA").andExpect(status().isConflict());
        mudarStatusSenha(preferencial1, "ATENDIDA").andExpect(status().isOk());
        mudarStatusSenha(preferencial1, "DESISTIU").andExpect(status().isConflict());

        // Paciente 1 (Dona Cida) e prioritario: recebe senha preferencial mesmo sem pedir.
        // Depois de uma preferencial vem duas normais, entao ela entra atras de V001 e V002.
        int preferencial2 = retirarSenha("{\"idUbs\": 3, \"idServico\": 2, \"idPaciente\": 1, \"origem\": \"APP\"}")
                .andExpect(jsonPath("$.codigo", is("V005")))
                .andExpect(jsonPath("$.prioridade", is("PREFERENCIAL")))
                .andExpect(jsonPath("$.paciente.codigoAnonimo", is("PAC-0001")))
                .andExpect(jsonPath("$.pessoasNaFrente", is(2)))
                .andExpect(jsonPath("$.esperaEstimadaMin", is(10)))
                .andReturn().getResponse().getContentAsString().transform(FilaZeroApiTest::idDe);
        mockMvc.perform(get("/senhas/" + normal3)).andExpect(jsonPath("$.pessoasNaFrente", is(3)));

        mudarStatusSenha(preferencial2, "CHAMADA").andExpect(status().isConflict());
        mudarStatusSenha(normal1, "CHAMADA").andExpect(status().isOk());
        mudarStatusSenha(normal1, "ATENDIDA").andExpect(status().isOk());
        mudarStatusSenha(normal2, "CHAMADA").andExpect(status().isOk());
        // Chamado e nao apareceu
        mudarStatusSenha(normal2, "DESISTIU")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dataChamada").isNotEmpty());

        // Duas normais ja foram chamadas: agora e a vez da preferencial
        mockMvc.perform(get("/ubs/3/espera"))
                .andExpect(jsonPath("$[1].servico.nome", is("Vacina")))
                .andExpect(jsonPath("$[1].pessoasAguardando", is(2)))
                .andExpect(jsonPath("$[1].esperaEstimadaMin", is(10)))
                .andExpect(jsonPath("$[1].proximaSenha", is("V005")));
        mudarStatusSenha(normal3, "CHAMADA").andExpect(status().isConflict());

        // Desistir antes de ser chamado: a senha e encerrada sem data de chamada
        mudarStatusSenha(normal3, "DESISTIU")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dataChamada", nullValue()));
        mudarStatusSenha(normal3, "CHAMADA").andExpect(status().isConflict());
        mudarStatusSenha(preferencial2, "CHAMADA").andExpect(status().isOk());
    }

    @Test
    void validaARetiradaEAChamadaDeSenha() throws Exception {
        retirarSenhaComErro("{\"idUbs\": 9999, \"idServico\": 2, \"origem\": \"APP\"}")
                .andExpect(jsonPath("$.mensagem", is("Não existe UBS com id 9999.")));
        retirarSenhaComErro("{\"idUbs\": 3, \"idServico\": 99, \"origem\": \"APP\"}");
        retirarSenhaComErro("{\"idUbs\": 3, \"idServico\": 2, \"idPaciente\": 9999, \"origem\": \"APP\"}");
        retirarSenhaComErro("{\"idUbs\": 3, \"idServico\": 2}")
                .andExpect(jsonPath("$.mensagem", is("A origem é obrigatória (APP ou TOTEM).")));
        retirarSenhaComErro("{\"idUbs\": 3, \"idServico\": 2, \"origem\": \"WHATSAPP\"}");

        // Paciente 2 (Juliana) nao e prioritaria, mas pode pedir preferencial (crianca de colo)
        retirarSenha("{\"idUbs\": 3, \"idServico\": 1, \"idPaciente\": 2, \"prioridade\": \"PREFERENCIAL\", "
                + "\"origem\": \"APP\"}")
                .andExpect(jsonPath("$.codigo", is("A001")))
                .andExpect(jsonPath("$.prioridade", is("PREFERENCIAL")));

        // A senha A007 da carga de exemplo e a proxima do Acolhimento da UBS 1
        mudarStatusSenha(514, "AGUARDANDO").andExpect(status().isBadRequest());
        mockMvc.perform(put("/senhas/514").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem", is("O status é obrigatório.")));
        mudarStatusSenha(514, "CHAMADA").andExpect(status().isOk());
        // Senha ja encerrada na carga de exemplo
        mudarStatusSenha(501, "CHAMADA").andExpect(status().isConflict());
        mudarStatusSenha(9999, "CHAMADA").andExpect(status().isNotFound());
    }

    // --------------------------------------------------------------- Consultas

    @Test
    void listaConsultasComFiltros() throws Exception {
        mockMvc.perform(get("/consultas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(18)));
        mockMvc.perform(get("/consultas").param("ubs", "1").param("data", "2026-11-16"))
                .andExpect(jsonPath("$", hasSize(9)))
                .andExpect(jsonPath("$[0].id", is(211)))
                .andExpect(jsonPath("$[0].horario.dataHora", is("2026-11-16T08:00:00")));
        mockMvc.perform(get("/consultas").param("ubs", "1").param("data", "2026-11-16").param("status", "AGENDADA"))
                .andExpect(jsonPath("$", hasSize(5)))
                .andExpect(jsonPath("$[*].status", everyItem(is("AGENDADA"))));
        mockMvc.perform(get("/consultas").param("status", "FALTOU"))
                .andExpect(jsonPath("$[*].id", contains(202, 206)));
        mockMvc.perform(get("/consultas").param("ubs", "9999")).andExpect(jsonPath("$", hasSize(0)));

        mockMvc.perform(get("/consultas").param("data", "16/11/2026"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem", is("Valor inválido para o parâmetro 'data': 16/11/2026")));
        mockMvc.perform(get("/consultas").param("status", "ADIADA")).andExpect(status().isBadRequest());

        mockMvc.perform(get("/consultas/223"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("AGENDADA")))
                .andExpect(jsonPath("$.encaixe", is(true)))
                .andExpect(jsonPath("$.dataAgendamento", is("2026-11-11T00:00:00")))
                .andExpect(jsonPath("$.paciente.codigoAnonimo", is("PAC-0013")))
                .andExpect(jsonPath("$.horario.id", is(123)))
                .andExpect(jsonPath("$.horario.status", is("OCUPADO")))
                .andExpect(jsonPath("$.horario.profissional.nome", is("Marcos Vinicius Lima")))
                .andExpect(jsonPath("$.horario.profissional.especialidade.nome", is("Pediatria")))
                .andExpect(jsonPath("$.horario.profissional.ubs.nome", is("UBS Jardim das Flores")));
        mockMvc.perform(get("/consultas/9999")).andExpect(status().isNotFound());
    }

    @Test
    void agendaSomenteEmHorarioLivre() throws Exception {
        // Horario 116: clinica geral, UBS 1, 16/11 09:40, LIVRE. Paciente 2 = Juliana.
        agendar(2, 116)
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/consultas/1000")))
                .andExpect(jsonPath("$.id", is(1000)))
                .andExpect(jsonPath("$.status", is("AGENDADA")))
                .andExpect(jsonPath("$.encaixe", is(false)))
                .andExpect(jsonPath("$.paciente.codigoAnonimo", is("PAC-0002")))
                .andExpect(jsonPath("$.horario.status", is("OCUPADO")));
        mockMvc.perform(get("/consultas/1000")).andExpect(jsonPath("$.horario.status", is("OCUPADO")));

        // RN01: o mesmo horario agora esta ocupado
        agendar(6, 116)
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status", is(422)))
                .andExpect(jsonPath("$.mensagem", containsString("RN01")));
        // RN01: horario bloqueado tambem nao pode ser agendado
        agendar(6, 132)
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensagem", containsString("BLOQUEADO")));

        // Dados invalidos
        agendar(9999, 124).andExpect(status().isBadRequest());
        agendar(2, 9999).andExpect(status().isBadRequest());
        mockMvc.perform(post("/consultas").contentType(MediaType.APPLICATION_JSON).content("{\"idPaciente\": 2}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem", is("O horário é obrigatório (idHorario).")));
    }

    @Test
    void naoAgendaDuasConsultasDaMesmaEspecialidadeNoMesmoDia() throws Exception {
        // RN02: paciente 1 (Dona Cida) ja tem a consulta 211 de clinica geral em 16/11
        agendar(1, 115)
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensagem",
                        is("O paciente PAC-0001 já tem consulta de Clinica geral em 16/11/2026 (RN02).")));
        // O horario continua livre depois da tentativa recusada
        mockMvc.perform(get("/consultas").param("ubs", "1").param("data", "2026-11-16"))
                .andExpect(jsonPath("$", hasSize(9)));

        // Outra especialidade no mesmo dia e permitida (horario 124 = pediatria)
        agendar(1, 124).andExpect(status().isCreated());
        // Consulta cancelada nao conta: a paciente 7 teve a 215 cancelada nesse dia
        agendar(7, 115).andExpect(status().isCreated());
    }

    @Test
    void confirmaERegistraComparecimentoRespeitandoARn04() throws Exception {
        mudarStatusConsulta(212, "CONFIRMADA")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CONFIRMADA")));
        mudarStatusConsulta(212, "CONFIRMADA").andExpect(status().isConflict());
        mudarStatusConsulta(212, "REALIZADA").andExpect(status().isOk());
        mockMvc.perform(get("/consultas/212")).andExpect(jsonPath("$.status", is("REALIZADA")));

        // RN04: depois de realizada nada mais muda
        mudarStatusConsulta(212, "FALTOU")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.mensagem", containsString("RN04")));
        mockMvc.perform(delete("/consultas/212")).andExpect(status().isConflict());

        // Falta registrada direto de AGENDADA
        mudarStatusConsulta(213, "FALTOU").andExpect(status().isOk());
        // RN04 para consultas que ja estavam finalizadas na carga de exemplo
        mudarStatusConsulta(201, "CONFIRMADA").andExpect(status().isConflict());
        mudarStatusConsulta(215, "CONFIRMADA").andExpect(status().isConflict());

        // O PUT nao cancela nem volta para AGENDADA
        mudarStatusConsulta(214, "CANCELADA")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem", is("Para cancelar a consulta use DELETE /consultas/214.")));
        mudarStatusConsulta(214, "AGENDADA").andExpect(status().isBadRequest());
        mockMvc.perform(put("/consultas/214").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        mudarStatusConsulta(9999, "CONFIRMADA").andExpect(status().isNotFound());
    }

    @Test
    void cancelarEncaixaOPrimeiroDaListaDeEspera() throws Exception {
        // Paciente 8 entra na lista de espera de pediatria (especialidade 2) da UBS 1
        mockMvc.perform(post("/lista-espera").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idPaciente\": 8, \"idUbs\": 1, \"idEspecialidade\": 2}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1000)));

        // RN03: cancela a consulta 222 (pediatria, UBS 1, 16/11 08:20, horario 122)
        mockMvc.perform(delete("/consultas/222")).andExpect(status().isNoContent());
        mockMvc.perform(get("/consultas/222")).andExpect(jsonPath("$.status", is("CANCELADA")));

        // O primeiro da lista (603, Theo) ja tem pediatria nesse dia (RN02), entao a vaga vai
        // para o proximo: o paciente 8, numa consulta nova marcada como encaixe.
        mockMvc.perform(get("/consultas/1000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paciente.codigoAnonimo", is("PAC-0008")))
                .andExpect(jsonPath("$.status", is("AGENDADA")))
                .andExpect(jsonPath("$.encaixe", is(true)))
                .andExpect(jsonPath("$.horario.id", is(122)))
                .andExpect(jsonPath("$.horario.status", is("OCUPADO")));

        // Theo continua aguardando; o paciente 8 saiu da fila como ENCAIXADO
        mockMvc.perform(get("/lista-espera").param("ubs", "1").param("especialidade", "2"))
                .andExpect(jsonPath("$[*].id", contains(603)));
        mockMvc.perform(delete("/lista-espera/1000"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem", containsString("ENCAIXADO")));

        // Uma consulta cancelada nao pode ser cancelada de novo (RN04)
        mockMvc.perform(delete("/consultas/222")).andExpect(status().isConflict());
        mockMvc.perform(delete("/consultas/9999")).andExpect(status().isNotFound());
    }

    @Test
    void cancelarSemNinguemParaEncaixarApenasLiberaOHorario() throws Exception {
        // Consulta 222: o unico da lista (Theo) nao pode ser encaixado (RN02). O horario 122 fica LIVRE.
        mockMvc.perform(delete("/consultas/222")).andExpect(status().isNoContent());
        mockMvc.perform(get("/consultas/222"))
                .andExpect(jsonPath("$.status", is("CANCELADA")))
                .andExpect(jsonPath("$.horario.status", is("LIVRE")));
        mockMvc.perform(get("/lista-espera").param("ubs", "1").param("especialidade", "2"))
                .andExpect(jsonPath("$[*].id", contains(603)));

        // Consulta 231 (paciente 6, clinica geral, UBS 2): o unico da lista e o proprio paciente 6,
        // que nao recebe de volta a vaga que acabou de cancelar.
        mockMvc.perform(delete("/consultas/231")).andExpect(status().isNoContent());
        mockMvc.perform(get("/consultas/231")).andExpect(jsonPath("$.horario.status", is("LIVRE")));
        mockMvc.perform(get("/lista-espera").param("ubs", "2")).andExpect(jsonPath("$[*].id", contains(605)));

        // Com o horario livre de novo, outro paciente consegue agendar normalmente
        agendar(9, 131).andExpect(status().isCreated()).andExpect(jsonPath("$.encaixe", is(false)));
    }

    // --------------------------------------------------------- Lista de espera

    @Test
    void entraListaESaiDaListaDeEspera() throws Exception {
        // Ordem do encaixe: maior prioridade primeiro
        mockMvc.perform(get("/lista-espera"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", contains(605, 603, 604)))
                .andExpect(jsonPath("$[*].status", everyItem(is("AGUARDANDO"))))
                .andExpect(jsonPath("$[1].paciente.codigoAnonimo", is("PAC-0003")))
                .andExpect(jsonPath("$[1].ubs.nome", is("UBS Jardim das Flores")))
                .andExpect(jsonPath("$[1].especialidade.nome", is("Pediatria")))
                .andExpect(jsonPath("$[1].idConsultaEncaixe", nullValue()));
        mockMvc.perform(get("/lista-espera").param("especialidade", "2"))
                .andExpect(jsonPath("$[*].id", contains(603, 604)));

        // Paciente 1 (Dona Cida) tem prioridade legal: entra com peso 5 e fica na frente do paciente 6
        mockMvc.perform(post("/lista-espera").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idPaciente\": 1, \"idUbs\": 2, \"idEspecialidade\": 1}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/lista-espera/1000")))
                .andExpect(jsonPath("$.id", is(1000)))
                .andExpect(jsonPath("$.prioridade", is(5)))
                .andExpect(jsonPath("$.status", is("AGUARDANDO")))
                .andExpect(jsonPath("$.dataEntrada").isNotEmpty());
        mockMvc.perform(get("/lista-espera").param("ubs", "2"))
                .andExpect(jsonPath("$[*].id", contains(1000, 605)));
        // Paciente sem prioridade legal entra com peso 0
        mockMvc.perform(post("/lista-espera").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idPaciente\": 2, \"idUbs\": 1, \"idEspecialidade\": 3}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.prioridade", is(0)));

        // Ja esta na lista dessa UBS e especialidade
        mockMvc.perform(post("/lista-espera").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idPaciente\": 3, \"idUbs\": 1, \"idEspecialidade\": 2}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem",
                        is("O paciente PAC-0003 já está na lista de espera de Pediatria nesta UBS.")));
        // Dados invalidos
        mockMvc.perform(post("/lista-espera").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idPaciente\": 3, \"idUbs\": 1, \"idEspecialidade\": 99}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/lista-espera").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idPaciente\": 3, \"idUbs\": 1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem", is("A especialidade é obrigatória (idEspecialidade).")));

        mockMvc.perform(delete("/lista-espera/603")).andExpect(status().isNoContent());
        mockMvc.perform(get("/lista-espera").param("especialidade", "2"))
                .andExpect(jsonPath("$[*].id", contains(604)));
        mockMvc.perform(delete("/lista-espera/603")).andExpect(status().isConflict());
        // Registro ja encaixado na carga de exemplo
        mockMvc.perform(delete("/lista-espera/601")).andExpect(status().isConflict());
        mockMvc.perform(delete("/lista-espera/9999")).andExpect(status().isNotFound());
    }

    // ------------------------------------------------------ Previsoes de falta

    @Test
    void previsoesMostramRiscoELembretesDaRn07() throws Exception {
        mockMvc.perform(get("/previsoes-falta")).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(12)));
        mockMvc.perform(get("/previsoes-falta").param("ubs", "2").param("data", "2026-11-16"))
                .andExpect(jsonPath("$[*].consulta.id", contains(231)));
        mockMvc.perform(get("/previsoes-falta").param("data", "2026-11-09")).andExpect(jsonPath("$", hasSize(0)));

        // UBS 1 em 16/11, em ordem de horario (dois profissionais atendem ao mesmo tempo)
        mockMvc.perform(get("/previsoes-falta").param("ubs", "1").param("data", "2026-11-16"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].consulta.id", contains(211, 221, 212, 222, 213, 223, 224, 214, 215)))
                // 211: risco baixo -> 1 lembrete, 24 h antes; ja enviado e respondido "VOU"
                .andExpect(jsonPath("$[0].probabilidadeFalta", is(0.08)))
                .andExpect(jsonPath("$[0].nivelRisco", is("BAIXO")))
                .andExpect(jsonPath("$[0].versaoModelo", is("logreg-v1")))
                .andExpect(jsonPath("$[0].lembretesNecessarios", is(1)))
                .andExpect(jsonPath("$[0].datasDosLembretes", contains("2026-11-15T08:00:00")))
                .andExpect(jsonPath("$[0].lembretesEnviados", hasSize(1)))
                .andExpect(jsonPath("$[0].lembretesEnviados[0].canal", is("APP")))
                .andExpect(jsonPath("$[0].lembretesEnviados[0].resposta", is("VOU")))
                // 212: risco alto -> 2 lembretes (48 h e 24 h antes); so 1 foi enviado ate agora
                .andExpect(jsonPath("$[2].probabilidadeFalta", is(0.52)))
                .andExpect(jsonPath("$[2].nivelRisco", is("ALTO")))
                .andExpect(jsonPath("$[2].lembretesNecessarios", is(2)))
                .andExpect(jsonPath("$[2].datasDosLembretes", contains("2026-11-14T08:20:00", "2026-11-15T08:20:00")))
                .andExpect(jsonPath("$[2].lembretesEnviados", hasSize(1)))
                // 213: risco alto com os 2 lembretes ja enviados
                .andExpect(jsonPath("$[4].nivelRisco", is("ALTO")))
                .andExpect(jsonPath("$[4].lembretesEnviados[*].id", contains(306, 307)))
                // 224: risco alto, mas a consulta foi cancelada -> nao recebe mais lembrete
                .andExpect(jsonPath("$[6].nivelRisco", is("ALTO")))
                .andExpect(jsonPath("$[6].consulta.status", is("CANCELADA")))
                .andExpect(jsonPath("$[6].lembretesNecessarios", is(0)))
                .andExpect(jsonPath("$[6].datasDosLembretes", hasSize(0)))
                // 214: risco medio -> 1 lembrete; nenhum enviado ainda
                .andExpect(jsonPath("$[7].nivelRisco", is("MEDIO")))
                .andExpect(jsonPath("$[7].lembretesNecessarios", is(1)))
                .andExpect(jsonPath("$[7].lembretesEnviados", hasSize(0)))
                // dados de saude do paciente nao aparecem na resposta
                .andExpect(jsonPath("$[0].consulta.paciente.codigoAnonimo", is("PAC-0001")))
                .andExpect(jsonPath("$[0].consulta.paciente.hipertensao").doesNotExist());
    }

    // -------------------------------------------------- Tratamento de excecoes

    @Test
    void devolveErrosPadronizados() throws Exception {
        // JSON mal formatado
        mockMvc.perform(post("/consultas").contentType(MediaType.APPLICATION_JSON).content("{\"idPaciente\": "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.erro", is("Bad Request")))
                .andExpect(jsonPath("$.caminho", is("/consultas")));
        // Id que nao e numero
        mockMvc.perform(get("/ubs/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem", is("Valor inválido para o parâmetro 'id': abc")));
        // Endereco que nao existe
        mockMvc.perform(get("/nao-existe"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
        // Verbo nao aceito
        mockMvc.perform(delete("/ubs"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status", is(405)));
        // Corpo que nao e JSON
        mockMvc.perform(post("/ubs").contentType(MediaType.TEXT_PLAIN).content("texto"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.status", is(415)));
    }

    // ------------------------------------------------------------- Auxiliares

    private static int idDe(String json) {
        return JsonPath.read(json, "$.id");
    }

    private ResultActions retirarSenha(String corpo) throws Exception {
        return mockMvc.perform(post("/senhas").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated());
    }

    private ResultActions retirarSenhaComErro(String corpo) throws Exception {
        return mockMvc.perform(post("/senhas").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isBadRequest());
    }

    private ResultActions mudarStatusSenha(int id, String novoStatus) throws Exception {
        return mockMvc.perform(put("/senhas/" + id).contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\": \"" + novoStatus + "\"}"));
    }

    private ResultActions agendar(int idPaciente, int idHorario) throws Exception {
        return mockMvc.perform(post("/consultas").contentType(MediaType.APPLICATION_JSON)
                .content("{\"idPaciente\": " + idPaciente + ", \"idHorario\": " + idHorario + "}"));
    }

    private ResultActions mudarStatusConsulta(int id, String novoStatus) throws Exception {
        return mockMvc.perform(put("/consultas/" + id).contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\": \"" + novoStatus + "\"}"));
    }
}
