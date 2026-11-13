package br.com.fiap.filazero.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Corpo do POST /consultas: quem vai ser atendido e em qual horario da agenda.
 */
public record ConsultaRequest(
        @NotNull(message = "O paciente é obrigatório (idPaciente).") Long idPaciente,
        @NotNull(message = "O horário é obrigatório (idHorario).") Long idHorario) {
}
