package br.com.fiap.filazero.dao;

import br.com.fiap.filazero.exception.BancoDadosException;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Executa uma operacao do BO dentro de uma transacao: abre UMA conexao, entrega essa
 * conexao para os DAOs usados na operacao e, no final, confirma tudo (commit).
 * Se qualquer passo falhar, desfaz tudo (rollback). A conexao e sempre fechada.
 *
 * Isso garante, por exemplo, que ao cancelar uma consulta (RN03) o status da consulta,
 * o horario da agenda e o encaixe da lista de espera sejam gravados juntos ou nenhum deles.
 */
public final class Transacao {

    /** Operacao que usa a conexao e devolve um resultado. */
    @FunctionalInterface
    public interface Operacao<T> {
        T executar(Connection conexao) throws SQLException;
    }

    /** Operacao que usa a conexao e nao devolve nada. */
    @FunctionalInterface
    public interface OperacaoSemRetorno {
        void executar(Connection conexao) throws SQLException;
    }

    private Transacao() {
    }

    public static <T> T executar(Operacao<T> operacao) {
        try (Connection conexao = ConnectionFactory.getConnection()) {
            conexao.setAutoCommit(false);
            try {
                T resultado = operacao.executar(conexao);
                conexao.commit();
                return resultado;
            } catch (SQLException | RuntimeException e) {
                desfazer(conexao, e);
                throw e;
            }
        } catch (SQLException e) {
            throw new BancoDadosException("Erro ao acessar o banco de dados.", e);
        }
    }

    public static void executarSemRetorno(OperacaoSemRetorno operacao) {
        executar(conexao -> {
            operacao.executar(conexao);
            return null;
        });
    }

    private static void desfazer(Connection conexao, Exception erroOriginal) {
        try {
            conexao.rollback();
        } catch (SQLException erroNoRollback) {
            erroOriginal.addSuppressed(erroNoRollback);
        }
    }
}
