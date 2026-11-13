-- ======================================================================================
--  GLOBAL SOLUTION 2026.2 - CITIZENTECH - ENGENHARIA DE SOFTWARE - 2o ANO
--  DISCIPLINA ...: DATABASE DESIGN
--  SCRIPT .......: D.M.L. (carga de dados de exemplo)     ARQUIVO: dml.sql
--  SOLUCAO ......: Fila Zero
--  INTEGRANTES ..: Diego Bondezan (RM562013), Diogo Weyne (RM572347),
--                  Felipe Otavio Garcia Madeira (RM563521), Guilherme Amaral (RM562112),
--                  Joao Victor Cazarini Del Bello Cardozo (RM562543)
--
--  COMO EXECUTAR
--    Execute depois do ddl.sql, inteiro, com "Run Script" (F5). O ddl.sql recria as
--    tabelas vazias; para recarregar os dados, rode os dois de novo, nessa ordem.
--
--  DADOS
--    Todos os dados sao FICTICIOS: nomes de UBS, enderecos, profissionais e registros
--    foram inventados, e os pacientes sao identificados so por codigo anonimo.
--    Total: 11 tabelas e 120 linhas; todas as tabelas tem pelo menos 5 linhas.
--
--  LINHA DO TEMPO DOS EXEMPLOS
--    A carga representa a situacao na sexta-feira 13/11/2026:
--    - segunda 09/11: agenda ja realizada (consultas REALIZADA, FALTOU e um encaixe)
--    - sexta 13/11: senhas da demanda espontanea da manha
--    - segunda 16/11: agenda futura (AGENDADA, CONFIRMADA, cancelamentos, encaixe e
--      previsoes de falta calculadas pelo modelo em 12/11)
--
--  PERSONAS DO PROJETO PRESENTES NOS DADOS
--    PAC-0001 = Dona Cida (67 anos, hipertensa e diabetica, atendimento prioritario)
--    PAC-0002 = Juliana (29 anos);  PAC-0003 = Theo (4 anos) e PAC-0004 = Lara (1 ano),
--    filhos da Juliana. A recepcionista Patricia trabalha na UBS Jardim das Flores (1).
-- ======================================================================================


-- ======================================================================================
--  1. TBL_UBS (5 linhas)
-- ======================================================================================
INSERT INTO TBL_UBS (id_ubs, nm_ubs, ds_endereco, nm_bairro, nr_latitude, nr_longitude, hr_abertura, hr_fechamento)
VALUES (1, 'UBS Jardim das Flores', 'Rua das Acacias, 120', 'Guaianases', -23.540100, -46.412300, '07:00', '19:00');
INSERT INTO TBL_UBS (id_ubs, nm_ubs, ds_endereco, nm_bairro, nr_latitude, nr_longitude, hr_abertura, hr_fechamento)
VALUES (2, 'UBS Vila Esperanca', 'Avenida Central, 900', 'Lajeado', -23.536800, -46.395100, '07:00', '19:00');
INSERT INTO TBL_UBS (id_ubs, nm_ubs, ds_endereco, nm_bairro, nr_latitude, nr_longitude, hr_abertura, hr_fechamento)
VALUES (3, 'UBS Parque Sao Jorge', 'Rua Sao Jorge, 45', 'Cidade Tiradentes', -23.582200, -46.404700, '07:00', '17:00');
INSERT INTO TBL_UBS (id_ubs, nm_ubs, ds_endereco, nm_bairro, nr_latitude, nr_longitude, hr_abertura, hr_fechamento)
VALUES (4, 'UBS Jardim Primavera', 'Rua Ipe Roxo, 310', 'Itaim Paulista', -23.500900, -46.398300, '07:00', '19:00');
INSERT INTO TBL_UBS (id_ubs, nm_ubs, ds_endereco, nm_bairro, nr_latitude, nr_longitude, hr_abertura, hr_fechamento)
VALUES (5, 'UBS Recanto Verde', 'Estrada do Recanto, 1500', 'Guaianases', -23.551200, -46.421000, '07:00', '17:00');


-- ======================================================================================
--  2. TBL_SERVICO (5 linhas: o dominio completo dos servicos de demanda espontanea)
-- ======================================================================================
INSERT INTO TBL_SERVICO (id_servico, nm_servico, cd_prefixo_senha, nr_tempo_medio_min) VALUES (1, 'Acolhimento', 'A', 15);
INSERT INTO TBL_SERVICO (id_servico, nm_servico, cd_prefixo_senha, nr_tempo_medio_min) VALUES (2, 'Vacina', 'V', 5);
INSERT INTO TBL_SERVICO (id_servico, nm_servico, cd_prefixo_senha, nr_tempo_medio_min) VALUES (3, 'Curativo', 'C', 10);
INSERT INTO TBL_SERVICO (id_servico, nm_servico, cd_prefixo_senha, nr_tempo_medio_min) VALUES (4, 'Farmacia', 'F', 4);
INSERT INTO TBL_SERVICO (id_servico, nm_servico, cd_prefixo_senha, nr_tempo_medio_min) VALUES (5, 'Coleta de exames', 'E', 6);


-- ======================================================================================
--  3. TBL_ESPECIALIDADE (5 linhas: o dominio completo das especialidades)
-- ======================================================================================
INSERT INTO TBL_ESPECIALIDADE (id_especialidade, nm_especialidade, nr_duracao_padrao_min) VALUES (1, 'Clinica geral', 20);
INSERT INTO TBL_ESPECIALIDADE (id_especialidade, nm_especialidade, nr_duracao_padrao_min) VALUES (2, 'Pediatria', 20);
INSERT INTO TBL_ESPECIALIDADE (id_especialidade, nm_especialidade, nr_duracao_padrao_min) VALUES (3, 'Ginecologia', 30);
INSERT INTO TBL_ESPECIALIDADE (id_especialidade, nm_especialidade, nr_duracao_padrao_min) VALUES (4, 'Enfermagem', 15);
INSERT INTO TBL_ESPECIALIDADE (id_especialidade, nm_especialidade, nr_duracao_padrao_min) VALUES (5, 'Odontologia', 30);


-- ======================================================================================
--  4. TBL_PROFISSIONAL (7 linhas; nomes e registros ficticios)
-- ======================================================================================
INSERT INTO TBL_PROFISSIONAL (id_profissional, nm_profissional, nr_registro, id_ubs, id_especialidade) VALUES (1, 'Ana Paula Ribeiro', 'CRM-SP 900101', 1, 1);
INSERT INTO TBL_PROFISSIONAL (id_profissional, nm_profissional, nr_registro, id_ubs, id_especialidade) VALUES (2, 'Marcos Vinicius Lima', 'CRM-SP 900102', 1, 2);
INSERT INTO TBL_PROFISSIONAL (id_profissional, nm_profissional, nr_registro, id_ubs, id_especialidade) VALUES (3, 'Renata Souza Prado', 'COREN-SP 900103', 1, 4);
INSERT INTO TBL_PROFISSIONAL (id_profissional, nm_profissional, nr_registro, id_ubs, id_especialidade) VALUES (4, 'Carlos Eduardo Nunes', 'CRM-SP 900104', 2, 1);
INSERT INTO TBL_PROFISSIONAL (id_profissional, nm_profissional, nr_registro, id_ubs, id_especialidade) VALUES (5, 'Fernanda Oliveira Reis', 'CRM-SP 900105', 3, 3);
INSERT INTO TBL_PROFISSIONAL (id_profissional, nm_profissional, nr_registro, id_ubs, id_especialidade) VALUES (6, 'Paulo Henrique Martins', 'CRO-SP 900106', 4, 5);
INSERT INTO TBL_PROFISSIONAL (id_profissional, nm_profissional, nr_registro, id_ubs, id_especialidade) VALUES (7, 'Beatriz Santos Moura', 'CRM-SP 900107', 5, 2);


-- ======================================================================================
--  5. TBL_PACIENTE (14 linhas; so codigo anonimo, sem nome, CPF ou telefone)
-- ======================================================================================
--  colunas: codigo, ano de nascimento, genero, prioritario, Bolsa Familia, hipertensao,
--           diabetes, tem celular, UBS de referencia
-- Dona Cida
INSERT INTO TBL_PACIENTE VALUES (1,  'PAC-0001', 1959, 'F', 'S', 'N', 'S', 'S', 'S', 1);
-- Juliana
INSERT INTO TBL_PACIENTE VALUES (2,  'PAC-0002', 1997, 'F', 'N', 'S', 'N', 'N', 'S', 1);
-- Theo, filho da Juliana
INSERT INTO TBL_PACIENTE VALUES (3,  'PAC-0003', 2022, 'M', 'N', 'S', 'N', 'N', 'N', 1);
-- Lara (crianca de colo)
INSERT INTO TBL_PACIENTE VALUES (4,  'PAC-0004', 2025, 'F', 'S', 'S', 'N', 'N', 'N', 1);
-- idoso sem celular
INSERT INTO TBL_PACIENTE VALUES (5,  'PAC-0005', 1948, 'M', 'S', 'N', 'S', 'N', 'N', 1);
INSERT INTO TBL_PACIENTE VALUES (6,  'PAC-0006', 1985, 'M', 'N', 'N', 'N', 'S', 'S', 2);
-- gestante
INSERT INTO TBL_PACIENTE VALUES (7,  'PAC-0007', 1990, 'F', 'S', 'N', 'N', 'N', 'S', 1);
INSERT INTO TBL_PACIENTE VALUES (8,  'PAC-0008', 2001, 'M', 'N', 'N', 'N', 'N', 'S', 1);
INSERT INTO TBL_PACIENTE VALUES (9,  'PAC-0009', 1972, 'F', 'N', 'N', 'S', 'N', 'S', 3);
-- pessoa com deficiencia
INSERT INTO TBL_PACIENTE VALUES (10, 'PAC-0010', 1966, 'M', 'S', 'N', 'N', 'N', 'S', 4);
INSERT INTO TBL_PACIENTE VALUES (11, 'PAC-0011', 2012, 'F', 'N', 'S', 'N', 'N', 'S', 5);
INSERT INTO TBL_PACIENTE VALUES (12, 'PAC-0012', 1955, 'F', 'S', 'N', 'S', 'S', 'N', 1);
INSERT INTO TBL_PACIENTE VALUES (13, 'PAC-0013', 2019, 'M', 'N', 'S', 'N', 'N', 'N', 1);
INSERT INTO TBL_PACIENTE VALUES (14, 'PAC-0014', 2016, 'F', 'N', 'N', 'N', 'N', 'N', 1);


-- ======================================================================================
--  6. TBL_HORARIO_AGENDA (20 linhas)
-- ======================================================================================
--  09/11 (ja passou): Dra. Ana Paula (clinica geral, UBS 1), das 8h as 9h40
INSERT INTO TBL_HORARIO_AGENDA VALUES (101, 1, TO_DATE('2026-11-09 08:00', 'YYYY-MM-DD HH24:MI'), 20, 'OCUPADO');
INSERT INTO TBL_HORARIO_AGENDA VALUES (102, 1, TO_DATE('2026-11-09 08:20', 'YYYY-MM-DD HH24:MI'), 20, 'OCUPADO');
INSERT INTO TBL_HORARIO_AGENDA VALUES (103, 1, TO_DATE('2026-11-09 08:40', 'YYYY-MM-DD HH24:MI'), 20, 'OCUPADO');
INSERT INTO TBL_HORARIO_AGENDA VALUES (104, 1, TO_DATE('2026-11-09 09:00', 'YYYY-MM-DD HH24:MI'), 20, 'OCUPADO');
INSERT INTO TBL_HORARIO_AGENDA VALUES (105, 1, TO_DATE('2026-11-09 09:20', 'YYYY-MM-DD HH24:MI'), 20, 'OCUPADO');
INSERT INTO TBL_HORARIO_AGENDA VALUES (106, 1, TO_DATE('2026-11-09 09:40', 'YYYY-MM-DD HH24:MI'), 20, 'LIVRE');
--  16/11 (proxima segunda): clinica geral (UBS 1)
INSERT INTO TBL_HORARIO_AGENDA VALUES (111, 1, TO_DATE('2026-11-16 08:00', 'YYYY-MM-DD HH24:MI'), 20, 'OCUPADO');
INSERT INTO TBL_HORARIO_AGENDA VALUES (112, 1, TO_DATE('2026-11-16 08:20', 'YYYY-MM-DD HH24:MI'), 20, 'OCUPADO');
INSERT INTO TBL_HORARIO_AGENDA VALUES (113, 1, TO_DATE('2026-11-16 08:40', 'YYYY-MM-DD HH24:MI'), 20, 'OCUPADO');
INSERT INTO TBL_HORARIO_AGENDA VALUES (114, 1, TO_DATE('2026-11-16 09:00', 'YYYY-MM-DD HH24:MI'), 20, 'OCUPADO');
-- consulta cancelada, lista vazia
INSERT INTO TBL_HORARIO_AGENDA VALUES (115, 1, TO_DATE('2026-11-16 09:20', 'YYYY-MM-DD HH24:MI'), 20, 'LIVRE');
INSERT INTO TBL_HORARIO_AGENDA VALUES (116, 1, TO_DATE('2026-11-16 09:40', 'YYYY-MM-DD HH24:MI'), 20, 'LIVRE');
--  16/11: Dr. Marcos (pediatria, UBS 1)
INSERT INTO TBL_HORARIO_AGENDA VALUES (121, 2, TO_DATE('2026-11-16 08:00', 'YYYY-MM-DD HH24:MI'), 20, 'OCUPADO');
INSERT INTO TBL_HORARIO_AGENDA VALUES (122, 2, TO_DATE('2026-11-16 08:20', 'YYYY-MM-DD HH24:MI'), 20, 'OCUPADO');
-- cancelamento + encaixe
INSERT INTO TBL_HORARIO_AGENDA VALUES (123, 2, TO_DATE('2026-11-16 08:40', 'YYYY-MM-DD HH24:MI'), 20, 'OCUPADO');
INSERT INTO TBL_HORARIO_AGENDA VALUES (124, 2, TO_DATE('2026-11-16 09:00', 'YYYY-MM-DD HH24:MI'), 20, 'LIVRE');
--  16/11: outras UBS
INSERT INTO TBL_HORARIO_AGENDA VALUES (131, 4, TO_DATE('2026-11-16 08:00', 'YYYY-MM-DD HH24:MI'), 20, 'OCUPADO');
-- profissional em capacitacao
INSERT INTO TBL_HORARIO_AGENDA VALUES (132, 4, TO_DATE('2026-11-16 08:20', 'YYYY-MM-DD HH24:MI'), 20, 'BLOQUEADO');
INSERT INTO TBL_HORARIO_AGENDA VALUES (141, 5, TO_DATE('2026-11-16 10:00', 'YYYY-MM-DD HH24:MI'), 30, 'OCUPADO');
INSERT INTO TBL_HORARIO_AGENDA VALUES (151, 6, TO_DATE('2026-11-16 13:00', 'YYYY-MM-DD HH24:MI'), 30, 'OCUPADO');


-- ======================================================================================
--  7. TBL_CONSULTA (18 linhas)
-- ======================================================================================
--  colunas: id, paciente, horario, data do agendamento, status, encaixe
--  09/11 (ja aconteceu)
INSERT INTO TBL_CONSULTA VALUES (201, 1,  101, TO_DATE('2026-10-13', 'YYYY-MM-DD'), 'REALIZADA', 'N');
INSERT INTO TBL_CONSULTA VALUES (202, 5,  102, TO_DATE('2026-09-28', 'YYYY-MM-DD'), 'FALTOU',    'N');
-- respondeu NAO_VOU
INSERT INTO TBL_CONSULTA VALUES (203, 8,  103, TO_DATE('2026-10-27', 'YYYY-MM-DD'), 'CANCELADA', 'N');
-- encaixe da lista de espera
INSERT INTO TBL_CONSULTA VALUES (204, 12, 103, TO_DATE('2026-11-07', 'YYYY-MM-DD'), 'REALIZADA', 'S');
INSERT INTO TBL_CONSULTA VALUES (205, 7,  104, TO_DATE('2026-10-29', 'YYYY-MM-DD'), 'REALIZADA', 'N');
INSERT INTO TBL_CONSULTA VALUES (206, 2,  105, TO_DATE('2026-11-03', 'YYYY-MM-DD'), 'FALTOU',    'N');
--  16/11 (agenda futura)
INSERT INTO TBL_CONSULTA VALUES (211, 1,  111, TO_DATE('2026-11-09', 'YYYY-MM-DD'), 'CONFIRMADA', 'N');
INSERT INTO TBL_CONSULTA VALUES (212, 5,  112, TO_DATE('2026-10-19', 'YYYY-MM-DD'), 'AGENDADA',   'N');
INSERT INTO TBL_CONSULTA VALUES (213, 8,  113, TO_DATE('2026-10-23', 'YYYY-MM-DD'), 'AGENDADA',   'N');
INSERT INTO TBL_CONSULTA VALUES (214, 12, 114, TO_DATE('2026-11-06', 'YYYY-MM-DD'), 'AGENDADA',   'N');
-- respondeu NAO_VOU
INSERT INTO TBL_CONSULTA VALUES (215, 7,  115, TO_DATE('2026-10-30', 'YYYY-MM-DD'), 'CANCELADA',  'N');
-- Theo
INSERT INTO TBL_CONSULTA VALUES (221, 3,  121, TO_DATE('2026-10-15', 'YYYY-MM-DD'), 'AGENDADA',   'N');
-- Lara
INSERT INTO TBL_CONSULTA VALUES (222, 4,  122, TO_DATE('2026-11-02', 'YYYY-MM-DD'), 'CONFIRMADA', 'N');
-- respondeu NAO_VOU
INSERT INTO TBL_CONSULTA VALUES (224, 14, 123, TO_DATE('2026-10-20', 'YYYY-MM-DD'), 'CANCELADA',  'N');
-- encaixe na vaga da 224
INSERT INTO TBL_CONSULTA VALUES (223, 13, 123, TO_DATE('2026-11-11', 'YYYY-MM-DD'), 'AGENDADA',   'S');
INSERT INTO TBL_CONSULTA VALUES (231, 6,  131, TO_DATE('2026-10-29', 'YYYY-MM-DD'), 'AGENDADA',   'N');
INSERT INTO TBL_CONSULTA VALUES (241, 9,  141, TO_DATE('2026-11-04', 'YYYY-MM-DD'), 'CONFIRMADA', 'N');
INSERT INTO TBL_CONSULTA VALUES (251, 10, 151, TO_DATE('2026-10-09', 'YYYY-MM-DD'), 'AGENDADA',   'N');


-- ======================================================================================
--  8. TBL_LEMBRETE (12 linhas)
-- ======================================================================================
--  colunas: id, consulta, canal, envio, resposta, data da resposta
INSERT INTO TBL_LEMBRETE VALUES (301, 203, 'SMS',    TO_DATE('2026-11-06 18:00', 'YYYY-MM-DD HH24:MI'), 'NAO_VOU',      TO_DATE('2026-11-07 08:12', 'YYYY-MM-DD HH24:MI'));
INSERT INTO TBL_LEMBRETE VALUES (302, 202, 'AGENTE', TO_DATE('2026-11-06 10:00', 'YYYY-MM-DD HH24:MI'), 'SEM_RESPOSTA', NULL);
INSERT INTO TBL_LEMBRETE VALUES (303, 206, 'APP',    TO_DATE('2026-11-08 09:00', 'YYYY-MM-DD HH24:MI'), 'SEM_RESPOSTA', NULL);
INSERT INTO TBL_LEMBRETE VALUES (304, 211, 'APP',    TO_DATE('2026-11-13 09:00', 'YYYY-MM-DD HH24:MI'), 'VOU',          TO_DATE('2026-11-13 09:05', 'YYYY-MM-DD HH24:MI'));
INSERT INTO TBL_LEMBRETE VALUES (305, 212, 'AGENTE', TO_DATE('2026-11-12 10:30', 'YYYY-MM-DD HH24:MI'), 'SEM_RESPOSTA', NULL);
INSERT INTO TBL_LEMBRETE VALUES (306, 213, 'SMS',    TO_DATE('2026-11-12 18:00', 'YYYY-MM-DD HH24:MI'), 'SEM_RESPOSTA', NULL);
-- 2o lembrete: risco alto
INSERT INTO TBL_LEMBRETE VALUES (307, 213, 'APP',    TO_DATE('2026-11-13 18:00', 'YYYY-MM-DD HH24:MI'), 'SEM_RESPOSTA', NULL);
INSERT INTO TBL_LEMBRETE VALUES (308, 215, 'APP',    TO_DATE('2026-11-12 09:00', 'YYYY-MM-DD HH24:MI'), 'NAO_VOU',      TO_DATE('2026-11-12 12:40', 'YYYY-MM-DD HH24:MI'));
INSERT INTO TBL_LEMBRETE VALUES (309, 222, 'APP',    TO_DATE('2026-11-12 09:00', 'YYYY-MM-DD HH24:MI'), 'VOU',          TO_DATE('2026-11-12 09:30', 'YYYY-MM-DD HH24:MI'));
INSERT INTO TBL_LEMBRETE VALUES (310, 224, 'SMS',    TO_DATE('2026-11-10 18:00', 'YYYY-MM-DD HH24:MI'), 'NAO_VOU',      TO_DATE('2026-11-11 07:02', 'YYYY-MM-DD HH24:MI'));
INSERT INTO TBL_LEMBRETE VALUES (311, 251, 'SMS',    TO_DATE('2026-11-12 18:00', 'YYYY-MM-DD HH24:MI'), 'REMARCAR',     TO_DATE('2026-11-13 07:50', 'YYYY-MM-DD HH24:MI'));
INSERT INTO TBL_LEMBRETE VALUES (312, 241, 'TOTEM',  TO_DATE('2026-11-13 08:10', 'YYYY-MM-DD HH24:MI'), 'VOU',          TO_DATE('2026-11-13 08:11', 'YYYY-MM-DD HH24:MI'));


-- ======================================================================================
--  9. TBL_PREVISAO_FALTA (12 linhas)
-- ======================================================================================
--  Previsoes das consultas de 16/11, calculadas em 12/11 pelo modelo de regressao
--  logistica (versao logreg-v1). Valores de exemplo no formato do previsoes_falta.csv
--  gerado pelo notebook de Data Science. Faixas: < 0,20 baixo; 0,20 a 0,35 medio;
--  >= 0,35 alto.
INSERT INTO TBL_PREVISAO_FALTA VALUES (401, 211, 0.080, 1, 'logreg-v1', TO_DATE('2026-11-12 02:00', 'YYYY-MM-DD HH24:MI'));
INSERT INTO TBL_PREVISAO_FALTA VALUES (402, 212, 0.520, 3, 'logreg-v1', TO_DATE('2026-11-12 02:00', 'YYYY-MM-DD HH24:MI'));
INSERT INTO TBL_PREVISAO_FALTA VALUES (403, 213, 0.410, 3, 'logreg-v1', TO_DATE('2026-11-12 02:00', 'YYYY-MM-DD HH24:MI'));
INSERT INTO TBL_PREVISAO_FALTA VALUES (404, 214, 0.270, 2, 'logreg-v1', TO_DATE('2026-11-12 02:00', 'YYYY-MM-DD HH24:MI'));
INSERT INTO TBL_PREVISAO_FALTA VALUES (405, 215, 0.230, 2, 'logreg-v1', TO_DATE('2026-11-12 02:00', 'YYYY-MM-DD HH24:MI'));
INSERT INTO TBL_PREVISAO_FALTA VALUES (406, 221, 0.310, 2, 'logreg-v1', TO_DATE('2026-11-12 02:00', 'YYYY-MM-DD HH24:MI'));
INSERT INTO TBL_PREVISAO_FALTA VALUES (407, 222, 0.120, 1, 'logreg-v1', TO_DATE('2026-11-12 02:00', 'YYYY-MM-DD HH24:MI'));
INSERT INTO TBL_PREVISAO_FALTA VALUES (408, 223, 0.150, 1, 'logreg-v1', TO_DATE('2026-11-12 02:00', 'YYYY-MM-DD HH24:MI'));
INSERT INTO TBL_PREVISAO_FALTA VALUES (409, 224, 0.360, 3, 'logreg-v1', TO_DATE('2026-11-12 02:00', 'YYYY-MM-DD HH24:MI'));
INSERT INTO TBL_PREVISAO_FALTA VALUES (410, 231, 0.220, 2, 'logreg-v1', TO_DATE('2026-11-12 02:00', 'YYYY-MM-DD HH24:MI'));
INSERT INTO TBL_PREVISAO_FALTA VALUES (411, 241, 0.110, 1, 'logreg-v1', TO_DATE('2026-11-12 02:00', 'YYYY-MM-DD HH24:MI'));
INSERT INTO TBL_PREVISAO_FALTA VALUES (412, 251, 0.480, 3, 'logreg-v1', TO_DATE('2026-11-12 02:00', 'YYYY-MM-DD HH24:MI'));


-- ======================================================================================
--  10. TBL_SENHA (16 linhas: manha de sexta 13/11)
-- ======================================================================================
--  colunas: id, UBS, servico, paciente (NULL = senha anonima do totem), senha,
--           prioridade, origem, emissao, chamada, status
-- Dona Cida
INSERT INTO TBL_SENHA VALUES (501, 1, 1, 1,    'A001', 'PREFERENCIAL', 'TOTEM', TO_DATE('2026-11-13 06:58', 'YYYY-MM-DD HH24:MI'), TO_DATE('2026-11-13 07:12', 'YYYY-MM-DD HH24:MI'), 'ATENDIDA');
INSERT INTO TBL_SENHA VALUES (502, 1, 1, 2,    'A002', 'NORMAL',       'APP',   TO_DATE('2026-11-13 07:01', 'YYYY-MM-DD HH24:MI'), TO_DATE('2026-11-13 07:30', 'YYYY-MM-DD HH24:MI'), 'ATENDIDA');
INSERT INTO TBL_SENHA VALUES (503, 1, 1, NULL, 'A003', 'NORMAL',       'TOTEM', TO_DATE('2026-11-13 07:03', 'YYYY-MM-DD HH24:MI'), TO_DATE('2026-11-13 07:47', 'YYYY-MM-DD HH24:MI'), 'ATENDIDA');
INSERT INTO TBL_SENHA VALUES (504, 1, 1, 5,    'A004', 'PREFERENCIAL', 'TOTEM', TO_DATE('2026-11-13 07:05', 'YYYY-MM-DD HH24:MI'), TO_DATE('2026-11-13 07:20', 'YYYY-MM-DD HH24:MI'), 'ATENDIDA');
INSERT INTO TBL_SENHA VALUES (505, 1, 1, 8,    'A005', 'NORMAL',       'APP',   TO_DATE('2026-11-13 07:10', 'YYYY-MM-DD HH24:MI'), TO_DATE('2026-11-13 08:05', 'YYYY-MM-DD HH24:MI'), 'ATENDIDA');
INSERT INTO TBL_SENHA VALUES (506, 1, 1, NULL, 'A006', 'NORMAL',       'TOTEM', TO_DATE('2026-11-13 07:15', 'YYYY-MM-DD HH24:MI'), NULL,                                              'DESISTIU');
-- Lara
INSERT INTO TBL_SENHA VALUES (507, 1, 2, 4,    'V001', 'PREFERENCIAL', 'APP',   TO_DATE('2026-11-13 07:30', 'YYYY-MM-DD HH24:MI'), TO_DATE('2026-11-13 07:36', 'YYYY-MM-DD HH24:MI'), 'ATENDIDA');
INSERT INTO TBL_SENHA VALUES (508, 1, 2, 7,    'V002', 'PREFERENCIAL', 'TOTEM', TO_DATE('2026-11-13 07:32', 'YYYY-MM-DD HH24:MI'), TO_DATE('2026-11-13 07:38', 'YYYY-MM-DD HH24:MI'), 'ATENDIDA');
INSERT INTO TBL_SENHA VALUES (509, 1, 2, NULL, 'V003', 'NORMAL',       'TOTEM', TO_DATE('2026-11-13 07:40', 'YYYY-MM-DD HH24:MI'), TO_DATE('2026-11-13 07:49', 'YYYY-MM-DD HH24:MI'), 'ATENDIDA');
INSERT INTO TBL_SENHA VALUES (510, 1, 4, 12,   'F001', 'PREFERENCIAL', 'TOTEM', TO_DATE('2026-11-13 07:45', 'YYYY-MM-DD HH24:MI'), TO_DATE('2026-11-13 07:52', 'YYYY-MM-DD HH24:MI'), 'ATENDIDA');
INSERT INTO TBL_SENHA VALUES (511, 1, 4, NULL, 'F002', 'NORMAL',       'TOTEM', TO_DATE('2026-11-13 07:50', 'YYYY-MM-DD HH24:MI'), TO_DATE('2026-11-13 08:01', 'YYYY-MM-DD HH24:MI'), 'ATENDIDA');
INSERT INTO TBL_SENHA VALUES (512, 1, 3, NULL, 'C001', 'NORMAL',       'TOTEM', TO_DATE('2026-11-13 08:00', 'YYYY-MM-DD HH24:MI'), TO_DATE('2026-11-13 08:12', 'YYYY-MM-DD HH24:MI'), 'CHAMADA');
INSERT INTO TBL_SENHA VALUES (513, 1, 5, NULL, 'E001', 'NORMAL',       'APP',   TO_DATE('2026-11-13 07:20', 'YYYY-MM-DD HH24:MI'), TO_DATE('2026-11-13 07:31', 'YYYY-MM-DD HH24:MI'), 'ATENDIDA');
INSERT INTO TBL_SENHA VALUES (514, 1, 1, NULL, 'A007', 'NORMAL',       'APP',   TO_DATE('2026-11-13 08:10', 'YYYY-MM-DD HH24:MI'), NULL,                                              'AGUARDANDO');
INSERT INTO TBL_SENHA VALUES (515, 2, 1, NULL, 'A001', 'NORMAL',       'TOTEM', TO_DATE('2026-11-13 07:05', 'YYYY-MM-DD HH24:MI'), TO_DATE('2026-11-13 07:40', 'YYYY-MM-DD HH24:MI'), 'ATENDIDA');
INSERT INTO TBL_SENHA VALUES (516, 2, 2, 6,    'V001', 'NORMAL',       'APP',   TO_DATE('2026-11-13 07:15', 'YYYY-MM-DD HH24:MI'), TO_DATE('2026-11-13 07:22', 'YYYY-MM-DD HH24:MI'), 'ATENDIDA');


-- ======================================================================================
--  11. TBL_LISTA_ESPERA (6 linhas)
-- ======================================================================================
--  colunas: id, paciente, UBS, especialidade, consulta do encaixe, entrada, prioridade,
--           status. A prioridade soma dias na fila e prioridade legal (usada pelo motor
--           de agenda de Dynamic Programming).
INSERT INTO TBL_LISTA_ESPERA VALUES (601, 12, 1, 1, 204,  TO_DATE('2026-10-20', 'YYYY-MM-DD'), 8, 'ENCAIXADO');
INSERT INTO TBL_LISTA_ESPERA VALUES (602, 13, 1, 2, 223,  TO_DATE('2026-10-28', 'YYYY-MM-DD'), 5, 'ENCAIXADO');
-- Theo quer uma vaga antes de 16/11
INSERT INTO TBL_LISTA_ESPERA VALUES (603, 3,  1, 2, NULL, TO_DATE('2026-11-05', 'YYYY-MM-DD'), 3, 'AGUARDANDO');
INSERT INTO TBL_LISTA_ESPERA VALUES (604, 11, 5, 2, NULL, TO_DATE('2026-11-02', 'YYYY-MM-DD'), 2, 'AGUARDANDO');
INSERT INTO TBL_LISTA_ESPERA VALUES (605, 6,  2, 1, NULL, TO_DATE('2026-11-06', 'YYYY-MM-DD'), 4, 'AGUARDANDO');
-- conseguiu agendar direto
INSERT INTO TBL_LISTA_ESPERA VALUES (606, 10, 4, 5, NULL, TO_DATE('2026-08-20', 'YYYY-MM-DD'), 6, 'DESISTIU');

COMMIT;

-- Fim do dml.sql. Proximo passo: executar o dql.sql.
