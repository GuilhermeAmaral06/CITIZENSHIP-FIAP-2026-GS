-- ======================================================================================
--  GLOBAL SOLUTION 2026.2 - CITIZENTECH - ENGENHARIA DE SOFTWARE - 2o ANO
--  DISCIPLINA ...: DATABASE DESIGN
--  SCRIPT .......: D.D.L. (estrutura do banco)            ARQUIVO: ddl.sql
--  SOLUCAO ......: Fila Zero - senha digital na UBS, previsao de faltas e encaixe
--                  automatico pela lista de espera
--  INTEGRANTES ..: Diego Bondezan ................................. RM562013
--                  Diogo Weyne .................................... RM572347
--                  Felipe Otavio Garcia Madeira ................... RM563521
--                  Guilherme Amaral ............................... RM562112
--                  Joao Victor Cazarini Del Bello Cardozo ......... RM562543
--  SGBD .........: Oracle Database 19c (testado em Oracle 18c XE e 23ai Free)
--
--  COMO EXECUTAR
--    1. ddl.sql  (este arquivo)  2. dml.sql  3. dql.sql
--    No Oracle SQL Developer, abra o arquivo e execute-o inteiro com "Run Script" (F5).
--    O script e REEXECUTAVEL: a Parte 0 remove as tabelas e sequences do Fila Zero, se
--    ja existirem, e na primeira execucao simplesmente nao encontra nada para remover.
--
--  CONTEUDO
--    PARTE 0 .. Limpeza do ambiente (remove somente objetos deste script)
--    PARTE 1 .. CREATE TABLE: 11 tabelas com PK, NOT NULL, UNIQUE, CHECK e DEFAULT
--    PARTE 2 .. ALTER TABLE: 15 chaves estrangeiras
--    PARTE 3 .. CREATE SEQUENCE: 11 sequences para a API Java (comecam em 1000)
--    PARTE 4 .. COMMENT ON TABLE: descricao de cada tabela
--
--  REGRAS COMUNS DO PROJETO (as mesmas no Java, no notebook e no prototipo)
--    Risco de falta ..... 1 Baixo (< 0,20) | 2 Medio (0,20 a 0,35) | 3 Alto (>= 0,35)
--    Status consulta .... AGENDADA, CONFIRMADA, REALIZADA, FALTOU, CANCELADA
--    LGPD ............... paciente identificado so por codigo anonimo (sem nome, CPF ou
--                         telefone); todos os dados de exemplo sao ficticios
--
--  OBSERVACAO: os textos foram gravados sem acentos para que o script rode em qualquer
--  configuracao de NLS e de editor.
-- ======================================================================================


-- ======================================================================================
--  PARTE 0 - LIMPEZA DO AMBIENTE
-- ======================================================================================
--  Ignora apenas ORA-00942 (tabela inexistente) e ORA-02289 (sequence inexistente).
--  Qualquer outro erro e repassado.
-- ======================================================================================
DECLARE
    TYPE t_nomes IS TABLE OF VARCHAR2(30);
    v_tabelas t_nomes := t_nomes(
        'TBL_LISTA_ESPERA', 'TBL_SENHA', 'TBL_PREVISAO_FALTA', 'TBL_LEMBRETE',
        'TBL_CONSULTA', 'TBL_HORARIO_AGENDA', 'TBL_PACIENTE', 'TBL_PROFISSIONAL',
        'TBL_ESPECIALIDADE', 'TBL_SERVICO', 'TBL_UBS');
    v_sequences t_nomes := t_nomes(
        'SEQ_UBS', 'SEQ_SERVICO', 'SEQ_ESPECIALIDADE', 'SEQ_PROFISSIONAL', 'SEQ_PACIENTE',
        'SEQ_HORARIO_AGENDA', 'SEQ_CONSULTA', 'SEQ_LEMBRETE', 'SEQ_PREVISAO_FALTA',
        'SEQ_SENHA', 'SEQ_LISTA_ESPERA');
BEGIN
    FOR i IN 1 .. v_tabelas.COUNT LOOP
        BEGIN
            EXECUTE IMMEDIATE 'DROP TABLE ' || v_tabelas(i) || ' CASCADE CONSTRAINTS PURGE';
        EXCEPTION
            WHEN OTHERS THEN
                IF SQLCODE <> -942 THEN
                    RAISE;
                END IF;
        END;
    END LOOP;

    FOR i IN 1 .. v_sequences.COUNT LOOP
        BEGIN
            EXECUTE IMMEDIATE 'DROP SEQUENCE ' || v_sequences(i);
        EXCEPTION
            WHEN OTHERS THEN
                IF SQLCODE <> -2289 THEN
                    RAISE;
                END IF;
        END;
    END LOOP;
END;
/


-- ======================================================================================
--  PARTE 1 - CREATE TABLE
-- ======================================================================================

-- --------------------------------------------------------------------------------------
--  1.1  TBL_UBS - unidades basicas de saude atendidas pelo Fila Zero
-- --------------------------------------------------------------------------------------
CREATE TABLE TBL_UBS (
    id_ubs          NUMBER(5)      NOT NULL,
    nm_ubs          VARCHAR2(80)   NOT NULL,
    ds_endereco     VARCHAR2(150)  NOT NULL,
    nm_bairro       VARCHAR2(60)   NOT NULL,
    nr_latitude     NUMBER(9,6)    NOT NULL,
    nr_longitude    NUMBER(9,6)    NOT NULL,
    hr_abertura     VARCHAR2(5)    NOT NULL,
    hr_fechamento   VARCHAR2(5)    NOT NULL,
    CONSTRAINT pk_ubs PRIMARY KEY (id_ubs),
    CONSTRAINT uk_ubs_nome UNIQUE (nm_ubs),
    CONSTRAINT ck_ubs_latitude CHECK (nr_latitude BETWEEN -90 AND 90),
    CONSTRAINT ck_ubs_longitude CHECK (nr_longitude BETWEEN -180 AND 180),
    CONSTRAINT ck_ubs_hr_abertura CHECK (REGEXP_LIKE(hr_abertura, '^([01][0-9]|2[0-3]):[0-5][0-9]$')),
    CONSTRAINT ck_ubs_hr_fechamento CHECK (REGEXP_LIKE(hr_fechamento, '^([01][0-9]|2[0-3]):[0-5][0-9]$')),
    CONSTRAINT ck_ubs_horario CHECK (hr_abertura < hr_fechamento)
);

-- --------------------------------------------------------------------------------------
--  1.2  TBL_SERVICO - servicos de demanda espontanea (atendidos por senha)
-- --------------------------------------------------------------------------------------
CREATE TABLE TBL_SERVICO (
    id_servico          NUMBER(3)     NOT NULL,
    nm_servico          VARCHAR2(40)  NOT NULL,
    cd_prefixo_senha    CHAR(1)       NOT NULL,
    nr_tempo_medio_min  NUMBER(3)     NOT NULL,
    CONSTRAINT pk_servico PRIMARY KEY (id_servico),
    CONSTRAINT uk_servico_nome UNIQUE (nm_servico),
    CONSTRAINT uk_servico_prefixo UNIQUE (cd_prefixo_senha),
    CONSTRAINT ck_servico_tempo CHECK (nr_tempo_medio_min > 0)
);

-- --------------------------------------------------------------------------------------
--  1.3  TBL_ESPECIALIDADE - especialidades das consultas agendadas
-- --------------------------------------------------------------------------------------
CREATE TABLE TBL_ESPECIALIDADE (
    id_especialidade       NUMBER(3)     NOT NULL,
    nm_especialidade       VARCHAR2(40)  NOT NULL,
    nr_duracao_padrao_min  NUMBER(3)     NOT NULL,
    CONSTRAINT pk_especialidade PRIMARY KEY (id_especialidade),
    CONSTRAINT uk_especialidade_nome UNIQUE (nm_especialidade),
    CONSTRAINT ck_especialidade_duracao CHECK (nr_duracao_padrao_min > 0)
);

-- --------------------------------------------------------------------------------------
--  1.4  TBL_PROFISSIONAL - profissionais que atendem consultas agendadas
-- --------------------------------------------------------------------------------------
CREATE TABLE TBL_PROFISSIONAL (
    id_profissional   NUMBER(5)     NOT NULL,
    nm_profissional   VARCHAR2(80)  NOT NULL,
    nr_registro       VARCHAR2(20)  NOT NULL,
    id_ubs            NUMBER(5)     NOT NULL,
    id_especialidade  NUMBER(3)     NOT NULL,
    CONSTRAINT pk_profissional PRIMARY KEY (id_profissional),
    CONSTRAINT uk_profissional_registro UNIQUE (nr_registro)
);

-- --------------------------------------------------------------------------------------
--  1.5  TBL_PACIENTE - pacientes, identificados apenas por codigo anonimo (LGPD)
-- --------------------------------------------------------------------------------------
CREATE TABLE TBL_PACIENTE (
    id_paciente          NUMBER(6)     NOT NULL,
    cd_paciente_anonimo  VARCHAR2(12)  NOT NULL,
    nr_ano_nascimento    NUMBER(4)     NOT NULL,
    tp_genero            CHAR(1)       NOT NULL,
    fl_prioritario       CHAR(1)       DEFAULT 'N' NOT NULL,
    fl_bolsa_familia     CHAR(1)       DEFAULT 'N' NOT NULL,
    fl_hipertensao       CHAR(1)       DEFAULT 'N' NOT NULL,
    fl_diabetes          CHAR(1)       DEFAULT 'N' NOT NULL,
    fl_tem_celular       CHAR(1)       DEFAULT 'S' NOT NULL,
    id_ubs_referencia    NUMBER(5)     NOT NULL,
    CONSTRAINT pk_paciente PRIMARY KEY (id_paciente),
    CONSTRAINT uk_paciente_codigo UNIQUE (cd_paciente_anonimo),
    CONSTRAINT ck_paciente_ano CHECK (nr_ano_nascimento BETWEEN 1900 AND 2026),
    CONSTRAINT ck_paciente_genero CHECK (tp_genero IN ('F', 'M', 'O')),
    CONSTRAINT ck_paciente_prioritario CHECK (fl_prioritario IN ('S', 'N')),
    CONSTRAINT ck_paciente_bolsa CHECK (fl_bolsa_familia IN ('S', 'N')),
    CONSTRAINT ck_paciente_hipertensao CHECK (fl_hipertensao IN ('S', 'N')),
    CONSTRAINT ck_paciente_diabetes CHECK (fl_diabetes IN ('S', 'N')),
    CONSTRAINT ck_paciente_celular CHECK (fl_tem_celular IN ('S', 'N'))
);

-- --------------------------------------------------------------------------------------
--  1.6  TBL_HORARIO_AGENDA - horarios da agenda de cada profissional
-- --------------------------------------------------------------------------------------
CREATE TABLE TBL_HORARIO_AGENDA (
    id_horario       NUMBER(8)     NOT NULL,
    id_profissional  NUMBER(5)     NOT NULL,
    dt_horario       DATE          NOT NULL,
    nr_duracao_min   NUMBER(3)     NOT NULL,
    st_horario       VARCHAR2(10)  DEFAULT 'LIVRE' NOT NULL,
    CONSTRAINT pk_horario_agenda PRIMARY KEY (id_horario),
    CONSTRAINT uk_horario_prof_data UNIQUE (id_profissional, dt_horario),
    CONSTRAINT ck_horario_duracao CHECK (nr_duracao_min > 0),
    CONSTRAINT ck_horario_status CHECK (st_horario IN ('LIVRE', 'OCUPADO', 'BLOQUEADO'))
);

-- --------------------------------------------------------------------------------------
--  1.7  TBL_CONSULTA - consultas agendadas; um horario pode ter uma consulta cancelada
--       e depois um encaixe, por isso id_horario nao e UNIQUE
-- --------------------------------------------------------------------------------------
CREATE TABLE TBL_CONSULTA (
    id_consulta     NUMBER(8)     NOT NULL,
    id_paciente     NUMBER(6)     NOT NULL,
    id_horario      NUMBER(8)     NOT NULL,
    dt_agendamento  DATE          NOT NULL,
    st_consulta     VARCHAR2(12)  DEFAULT 'AGENDADA' NOT NULL,
    fl_encaixe      CHAR(1)       DEFAULT 'N' NOT NULL,
    CONSTRAINT pk_consulta PRIMARY KEY (id_consulta),
    CONSTRAINT ck_consulta_status CHECK (st_consulta IN ('AGENDADA', 'CONFIRMADA', 'REALIZADA', 'FALTOU', 'CANCELADA')),
    CONSTRAINT ck_consulta_encaixe CHECK (fl_encaixe IN ('S', 'N'))
);

-- --------------------------------------------------------------------------------------
--  1.8  TBL_LEMBRETE - lembretes enviados e respostas dos pacientes
-- --------------------------------------------------------------------------------------
CREATE TABLE TBL_LEMBRETE (
    id_lembrete  NUMBER(8)     NOT NULL,
    id_consulta  NUMBER(8)     NOT NULL,
    tp_canal     VARCHAR2(6)   NOT NULL,
    dt_envio     DATE          NOT NULL,
    tp_resposta  VARCHAR2(12)  DEFAULT 'SEM_RESPOSTA' NOT NULL,
    dt_resposta  DATE,
    CONSTRAINT pk_lembrete PRIMARY KEY (id_lembrete),
    CONSTRAINT ck_lembrete_canal CHECK (tp_canal IN ('APP', 'SMS', 'TOTEM', 'AGENTE')),
    CONSTRAINT ck_lembrete_resposta CHECK (tp_resposta IN ('VOU', 'NAO_VOU', 'REMARCAR', 'SEM_RESPOSTA')),
    CONSTRAINT ck_lembrete_datas CHECK (dt_resposta IS NULL OR dt_resposta >= dt_envio),
    CONSTRAINT ck_lembrete_coerencia CHECK (
        (tp_resposta = 'SEM_RESPOSTA' AND dt_resposta IS NULL)
        OR (tp_resposta <> 'SEM_RESPOSTA' AND dt_resposta IS NOT NULL))
);

-- --------------------------------------------------------------------------------------
--  1.9  TBL_PREVISAO_FALTA - probabilidade de falta calculada pelo modelo de Data
--       Science; uma previsao por consulta. O CHECK de coerencia garante que o nivel de
--       risco segue as mesmas faixas usadas em todo o projeto.
-- --------------------------------------------------------------------------------------
CREATE TABLE TBL_PREVISAO_FALTA (
    id_previsao       NUMBER(8)     NOT NULL,
    id_consulta       NUMBER(8)     NOT NULL,
    pc_prob_falta     NUMBER(4,3)   NOT NULL,
    nr_nivel_risco    NUMBER(1)     NOT NULL,
    ds_versao_modelo  VARCHAR2(20)  NOT NULL,
    dt_calculo        DATE          NOT NULL,
    CONSTRAINT pk_previsao_falta PRIMARY KEY (id_previsao),
    CONSTRAINT uk_previsao_consulta UNIQUE (id_consulta),
    CONSTRAINT ck_previsao_prob CHECK (pc_prob_falta BETWEEN 0 AND 1),
    CONSTRAINT ck_previsao_nivel CHECK (nr_nivel_risco IN (1, 2, 3)),
    CONSTRAINT ck_previsao_coerencia CHECK (
        (nr_nivel_risco = 1 AND pc_prob_falta < 0.20)
        OR (nr_nivel_risco = 2 AND pc_prob_falta >= 0.20 AND pc_prob_falta < 0.35)
        OR (nr_nivel_risco = 3 AND pc_prob_falta >= 0.35))
);

-- --------------------------------------------------------------------------------------
--  1.10 TBL_SENHA - senhas digitais da demanda espontanea; o paciente e opcional porque
--       a senha retirada no totem pode ser anonima
-- --------------------------------------------------------------------------------------
CREATE TABLE TBL_SENHA (
    id_senha       NUMBER(8)     NOT NULL,
    id_ubs         NUMBER(5)     NOT NULL,
    id_servico     NUMBER(3)     NOT NULL,
    id_paciente    NUMBER(6),
    cd_senha       VARCHAR2(5)   NOT NULL,
    tp_prioridade  VARCHAR2(12)  DEFAULT 'NORMAL' NOT NULL,
    tp_origem      VARCHAR2(5)   NOT NULL,
    dt_emissao     DATE          NOT NULL,
    dt_chamada     DATE,
    st_senha       VARCHAR2(10)  DEFAULT 'AGUARDANDO' NOT NULL,
    CONSTRAINT pk_senha PRIMARY KEY (id_senha),
    CONSTRAINT ck_senha_prioridade CHECK (tp_prioridade IN ('NORMAL', 'PREFERENCIAL')),
    CONSTRAINT ck_senha_origem CHECK (tp_origem IN ('APP', 'TOTEM')),
    CONSTRAINT ck_senha_status CHECK (st_senha IN ('AGUARDANDO', 'CHAMADA', 'ATENDIDA', 'DESISTIU')),
    CONSTRAINT ck_senha_datas CHECK (dt_chamada IS NULL OR dt_chamada >= dt_emissao),
    CONSTRAINT ck_senha_chamada CHECK (st_senha NOT IN ('CHAMADA', 'ATENDIDA') OR dt_chamada IS NOT NULL)
);

-- --------------------------------------------------------------------------------------
--  1.11 TBL_LISTA_ESPERA - fila de espera por especialidade; quando o paciente e
--       encaixado, a consulta criada fica registrada em id_consulta_encaixe
-- --------------------------------------------------------------------------------------
CREATE TABLE TBL_LISTA_ESPERA (
    id_espera            NUMBER(8)     NOT NULL,
    id_paciente          NUMBER(6)     NOT NULL,
    id_ubs               NUMBER(5)     NOT NULL,
    id_especialidade     NUMBER(3)     NOT NULL,
    id_consulta_encaixe  NUMBER(8),
    dt_entrada           DATE          NOT NULL,
    nr_prioridade        NUMBER(3)     DEFAULT 0 NOT NULL,
    st_espera            VARCHAR2(10)  DEFAULT 'AGUARDANDO' NOT NULL,
    CONSTRAINT pk_lista_espera PRIMARY KEY (id_espera),
    CONSTRAINT uk_espera_consulta UNIQUE (id_consulta_encaixe),
    CONSTRAINT ck_espera_prioridade CHECK (nr_prioridade >= 0),
    CONSTRAINT ck_espera_status CHECK (st_espera IN ('AGUARDANDO', 'ENCAIXADO', 'DESISTIU')),
    CONSTRAINT ck_espera_encaixe CHECK (
        (st_espera = 'ENCAIXADO' AND id_consulta_encaixe IS NOT NULL)
        OR (st_espera <> 'ENCAIXADO' AND id_consulta_encaixe IS NULL))
);


-- ======================================================================================
--  PARTE 2 - ALTER TABLE: CHAVES ESTRANGEIRAS
-- ======================================================================================
ALTER TABLE TBL_PROFISSIONAL ADD CONSTRAINT fk_profissional_ubs
    FOREIGN KEY (id_ubs) REFERENCES TBL_UBS (id_ubs);
ALTER TABLE TBL_PROFISSIONAL ADD CONSTRAINT fk_profissional_especialidade
    FOREIGN KEY (id_especialidade) REFERENCES TBL_ESPECIALIDADE (id_especialidade);

ALTER TABLE TBL_PACIENTE ADD CONSTRAINT fk_paciente_ubs
    FOREIGN KEY (id_ubs_referencia) REFERENCES TBL_UBS (id_ubs);

ALTER TABLE TBL_HORARIO_AGENDA ADD CONSTRAINT fk_horario_profissional
    FOREIGN KEY (id_profissional) REFERENCES TBL_PROFISSIONAL (id_profissional);

ALTER TABLE TBL_CONSULTA ADD CONSTRAINT fk_consulta_paciente
    FOREIGN KEY (id_paciente) REFERENCES TBL_PACIENTE (id_paciente);
ALTER TABLE TBL_CONSULTA ADD CONSTRAINT fk_consulta_horario
    FOREIGN KEY (id_horario) REFERENCES TBL_HORARIO_AGENDA (id_horario);

ALTER TABLE TBL_LEMBRETE ADD CONSTRAINT fk_lembrete_consulta
    FOREIGN KEY (id_consulta) REFERENCES TBL_CONSULTA (id_consulta);

ALTER TABLE TBL_PREVISAO_FALTA ADD CONSTRAINT fk_previsao_consulta
    FOREIGN KEY (id_consulta) REFERENCES TBL_CONSULTA (id_consulta);

ALTER TABLE TBL_SENHA ADD CONSTRAINT fk_senha_ubs
    FOREIGN KEY (id_ubs) REFERENCES TBL_UBS (id_ubs);
ALTER TABLE TBL_SENHA ADD CONSTRAINT fk_senha_servico
    FOREIGN KEY (id_servico) REFERENCES TBL_SERVICO (id_servico);
ALTER TABLE TBL_SENHA ADD CONSTRAINT fk_senha_paciente
    FOREIGN KEY (id_paciente) REFERENCES TBL_PACIENTE (id_paciente);

ALTER TABLE TBL_LISTA_ESPERA ADD CONSTRAINT fk_espera_paciente
    FOREIGN KEY (id_paciente) REFERENCES TBL_PACIENTE (id_paciente);
ALTER TABLE TBL_LISTA_ESPERA ADD CONSTRAINT fk_espera_ubs
    FOREIGN KEY (id_ubs) REFERENCES TBL_UBS (id_ubs);
ALTER TABLE TBL_LISTA_ESPERA ADD CONSTRAINT fk_espera_especialidade
    FOREIGN KEY (id_especialidade) REFERENCES TBL_ESPECIALIDADE (id_especialidade);
ALTER TABLE TBL_LISTA_ESPERA ADD CONSTRAINT fk_espera_consulta
    FOREIGN KEY (id_consulta_encaixe) REFERENCES TBL_CONSULTA (id_consulta);


-- ======================================================================================
--  PARTE 3 - SEQUENCES
-- ======================================================================================
--  Usadas pela API Java para gerar novos IDs. A carga de exemplo (dml.sql) usa IDs fixos
--  abaixo de 1000, por isso as sequences comecam em 1000 e nunca colidem com ela.
-- ======================================================================================
CREATE SEQUENCE SEQ_UBS            START WITH 1000 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE SEQ_SERVICO        START WITH 1000 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE SEQ_ESPECIALIDADE  START WITH 1000 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE SEQ_PROFISSIONAL   START WITH 1000 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE SEQ_PACIENTE       START WITH 1000 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE SEQ_HORARIO_AGENDA START WITH 1000 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE SEQ_CONSULTA       START WITH 1000 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE SEQ_LEMBRETE       START WITH 1000 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE SEQ_PREVISAO_FALTA START WITH 1000 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE SEQ_SENHA          START WITH 1000 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE SEQ_LISTA_ESPERA   START WITH 1000 INCREMENT BY 1 NOCACHE;


-- ======================================================================================
--  PARTE 4 - COMENTARIOS DAS TABELAS
-- ======================================================================================
COMMENT ON TABLE TBL_UBS IS 'Unidades basicas de saude atendidas pelo Fila Zero';
COMMENT ON TABLE TBL_SERVICO IS 'Servicos de demanda espontanea atendidos por senha (acolhimento, vacina, curativo, farmacia, coleta de exames)';
COMMENT ON TABLE TBL_ESPECIALIDADE IS 'Especialidades das consultas agendadas';
COMMENT ON TABLE TBL_PROFISSIONAL IS 'Profissionais de saude que atendem consultas agendadas (dados ficticios)';
COMMENT ON TABLE TBL_PACIENTE IS 'Pacientes identificados apenas por codigo anonimo, sem nome, CPF ou telefone (LGPD)';
COMMENT ON TABLE TBL_HORARIO_AGENDA IS 'Horarios da agenda de cada profissional: livre, ocupado ou bloqueado';
COMMENT ON TABLE TBL_CONSULTA IS 'Consultas agendadas; fl_encaixe = S indica vaga reaproveitada pela lista de espera';
COMMENT ON TABLE TBL_LEMBRETE IS 'Lembretes enviados por app, SMS, totem ou agente comunitario e as respostas dos pacientes';
COMMENT ON TABLE TBL_PREVISAO_FALTA IS 'Probabilidade de falta por consulta, calculada pelo modelo de regressao logistica';
COMMENT ON TABLE TBL_SENHA IS 'Senhas digitais da demanda espontanea, retiradas pelo app ou pelo totem';
COMMENT ON TABLE TBL_LISTA_ESPERA IS 'Fila de espera por especialidade usada para encaixar pacientes nas vagas liberadas';

-- Fim do ddl.sql. Proximo passo: executar o dml.sql.
