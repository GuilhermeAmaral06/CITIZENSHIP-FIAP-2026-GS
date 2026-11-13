package br.com.fiap.filazero.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Corpo do POST /lista-espera: o paciente e a vaga (UBS + especialidade) que ele aguarda.
 */
public record ListaEsperaRequest(
        @NotNull(message = "O paciente é obrigatório (idPaciente).") Long idPaciente,
        @NotNull(message = "A UBS é obrigatória (idUbs).") Long idUbs,
        @NotNull(message = "A especialidade é obrigatória (idEspecialidade).") Long idEspecialidade) {
}
