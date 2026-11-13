package br.com.fiap.filazero.model;

/**
 * Status da consulta. Os nomes repetem os valores do CHECK ck_consulta_status.
 * Fluxo: AGENDADA -> CONFIRMADA -> REALIZADA, ou FALTOU, ou CANCELADA.
 */
public enum StatusConsulta {
    AGENDADA,
    CONFIRMADA,
    REALIZADA,
    FALTOU,
    CANCELADA;

    /** RN04: uma consulta REALIZADA, FALTOU ou CANCELADA nao pode mais ser alterada. */
    public boolean isFinal() {
        return this == REALIZADA || this == FALTOU || this == CANCELADA;
    }
}
