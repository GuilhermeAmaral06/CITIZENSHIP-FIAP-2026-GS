package br.com.fiap.filazero.dao;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Conversoes entre os tipos do banco e os do Java, usadas pelos DAOs.
 */
final class Conversor {

    private Conversor() {
    }

    /** Colunas fl_* guardam 'S' ou 'N'. */
    static boolean paraBoolean(String simOuNao) {
        return "S".equals(simOuNao);
    }

    static String paraSimNao(boolean valor) {
        return valor ? "S" : "N";
    }

    /** Colunas DATE do Oracle guardam data e hora; null continua null. */
    static LocalDateTime paraDataHora(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    static Timestamp paraTimestamp(LocalDateTime dataHora) {
        return dataHora == null ? null : Timestamp.valueOf(dataHora);
    }

    /** Primeiro instante do dia: usado com o proximo dia para filtrar "tudo que aconteceu no dia". */
    static Timestamp inicioDoDia(LocalDate dia) {
        return Timestamp.valueOf(dia.atStartOfDay());
    }

    static Timestamp inicioDoDiaSeguinte(LocalDate dia) {
        return Timestamp.valueOf(dia.plusDays(1).atStartOfDay());
    }
}
