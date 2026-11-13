package br.com.fiap.filazero.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

/**
 * Data e hora no fuso de Brasilia. O servidor do deploy (Render) roda em UTC,
 * entao usamos o fuso explicitamente para as datas gravadas ficarem corretas.
 */
public final class DataHora {

    private static final ZoneId FUSO_BRASILIA = ZoneId.of("America/Sao_Paulo");

    private DataHora() {
    }

    /** Data e hora atuais, sem fracao de segundo (as colunas DATE do Oracle guardam ate o segundo). */
    public static LocalDateTime agora() {
        return LocalDateTime.now(FUSO_BRASILIA).truncatedTo(ChronoUnit.SECONDS);
    }

    public static LocalDate hoje() {
        return LocalDate.now(FUSO_BRASILIA);
    }
}
