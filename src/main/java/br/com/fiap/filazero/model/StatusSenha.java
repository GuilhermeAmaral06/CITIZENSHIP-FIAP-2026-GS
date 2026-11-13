package br.com.fiap.filazero.model;

/**
 * Status da senha. Os nomes repetem os valores do CHECK ck_senha_status.
 * Fluxo: AGUARDANDO -> CHAMADA -> ATENDIDA. A senha tambem pode ir para DESISTIU.
 */
public enum StatusSenha {
    AGUARDANDO,
    CHAMADA,
    ATENDIDA,
    DESISTIU;

    /** Senhas ATENDIDA ou DESISTIU estao encerradas e nao mudam mais. */
    public boolean isFinal() {
        return this == ATENDIDA || this == DESISTIU;
    }
}
