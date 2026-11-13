package br.com.fiap.filazero.dto;

import br.com.fiap.filazero.model.OrigemSenha;
import br.com.fiap.filazero.model.TipoPrioridade;
import jakarta.validation.constraints.NotNull;

/**
 * Corpo do POST /senhas. O paciente e opcional (a senha do totem pode ser anonima) e a
 * prioridade tambem: sem ela a senha e NORMAL, a menos que o paciente seja prioritario.
 */
public record SenhaRequest(
        @NotNull(message = "A UBS é obrigatória (idUbs).") Long idUbs,
        @NotNull(message = "O serviço é obrigatório (idServico).") Long idServico,
        Long idPaciente,
        TipoPrioridade prioridade,
        @NotNull(message = "A origem é obrigatória (APP ou TOTEM).") OrigemSenha origem) {
}
