package br.com.fiap.filazero;

import br.com.fiap.filazero.dao.ConnectionFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Banco usado pelos testes automatizados: um H2 em memoria, no modo de compatibilidade
 * Oracle, criado a partir dos mesmos scripts da equipe (database/ddl.sql e database/dml.sql).
 * Assim os testes nao dependem do Oracle da FIAP nem alteram os dados de la.
 */
final class BancoDeTeste {

    private BancoDeTeste() {
    }

    /** Aponta a ConnectionFactory para o H2. Precisa ser chamado antes de qualquer conexao. */
    static void configurar() {
        System.setProperty("DB_URL", "jdbc:h2:mem:filazero;MODE=Oracle;DB_CLOSE_DELAY=-1");
        System.setProperty("DB_USER", "sa");
        System.setProperty("DB_PASSWORD", "sa");
    }

    /** Apaga tudo e recria as tabelas com a carga de exemplo, para cada teste comecar do mesmo ponto. */
    static void recriar() throws Exception {
        try (Connection conexao = ConnectionFactory.getConnection();
             Statement stmt = conexao.createStatement()) {
            stmt.execute("DROP ALL OBJECTS");
            executarScript(stmt, Path.of("database/ddl.sql"));
            executarScript(stmt, Path.of("database/dml.sql"));
        }
    }

    private static void executarScript(Statement stmt, Path arquivo) throws Exception {
        // No Windows o Git pode gravar os arquivos com quebra de linha \r\n: padroniza para \n
        String script = Files.readString(arquivo).replace("\r\n", "\n");
        // A Parte 0 do ddl.sql e um bloco PL/SQL de limpeza, que so existe no Oracle.
        // O bloco termina na linha que tem apenas "/".
        int fimDoBlocoPlSql = script.indexOf("\n/\n");
        if (fimDoBlocoPlSql >= 0) {
            script = script.substring(fimDoBlocoPlSql + 3);
        }
        StringBuilder semComentarios = new StringBuilder();
        for (String linha : script.split("\n")) {
            if (!linha.trim().startsWith("--")) {
                semComentarios.append(linha).append('\n');
            }
        }
        for (String comando : separarComandos(semComentarios.toString())) {
            stmt.execute(comando);
        }
    }

    /** Separa o script nos comandos terminados por ";", sem cortar um ";" que esteja dentro de um texto entre aspas. */
    private static List<String> separarComandos(String script) {
        List<String> comandos = new ArrayList<>();
        StringBuilder atual = new StringBuilder();
        boolean dentroDeTexto = false;
        for (char caractere : script.toCharArray()) {
            if (caractere == '\'') {
                dentroDeTexto = !dentroDeTexto;
            }
            if (caractere == ';' && !dentroDeTexto) {
                if (!atual.toString().isBlank()) {
                    comandos.add(atual.toString().trim());
                }
                atual.setLength(0);
            } else {
                atual.append(caractere);
            }
        }
        return comandos;
    }
}
