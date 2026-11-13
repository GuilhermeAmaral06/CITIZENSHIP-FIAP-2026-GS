package br.com.fiap.filazero.dto;

import br.com.fiap.filazero.model.StatusSenha;
import jakarta.validation.constraints.NotNull;

/**
 * Corpo do PUT /senhas/{id}: o novo status (CHAMADA, ATENDIDA ou DESISTIU).
 */
public record StatusSenhaRequest(
        @NotNull(message = "O status é obrigatório.") StatusSenha status) {
}
