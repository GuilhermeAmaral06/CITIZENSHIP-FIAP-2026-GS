package br.com.fiap.filazero.dto;

import br.com.fiap.filazero.model.Servico;

/**
 * Item da resposta do GET /ubs/{id}/espera: a fila de um servico da UBS neste momento.
 *
 * @param servico           o servico (Acolhimento, Vacina...)
 * @param pessoasAguardando quantas senhas estao aguardando
 * @param esperaEstimadaMin espera de quem retirar uma senha agora (RN05)
 * @param proximaSenha      codigo da proxima senha a ser chamada (RN06), ou null se a fila esta vazia
 */
public record EsperaServicoResponse(
        Servico servico,
        int pessoasAguardando,
        int esperaEstimadaMin,
        String proximaSenha) {
}
