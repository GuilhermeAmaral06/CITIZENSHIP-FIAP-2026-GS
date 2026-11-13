package br.com.fiap.filazero.dto;

import br.com.fiap.filazero.model.StatusConsulta;
import jakarta.validation.constraints.NotNull;

/**
 * Corpo do PUT /consultas/{id}: o novo status (CONFIRMADA, REALIZADA ou FALTOU).
 */
public record StatusConsultaRequest(
        @NotNull(message = "O status é obrigatório.") StatusConsulta status) {
}
