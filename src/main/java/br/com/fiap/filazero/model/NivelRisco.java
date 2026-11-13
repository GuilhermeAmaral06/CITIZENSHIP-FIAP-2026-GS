package br.com.fiap.filazero.model;

/**
 * Nivel de risco de falta, igual em todo o projeto (banco, notebook, Java e prototipo):
 * BAIXO = probabilidade menor que 0,20; MEDIO = de 0,20 a 0,35; ALTO = 0,35 ou mais.
 * O banco guarda o codigo numerico (1, 2 ou 3) na coluna nr_nivel_risco.
 */
public enum NivelRisco {
    BAIXO(1),
    MEDIO(2),
    ALTO(3);

    private final int codigo;

    NivelRisco(int codigo) {
        this.codigo = codigo;
    }

    public int getCodigo() {
        return codigo;
    }

    /** Converte o codigo gravado no banco (1, 2 ou 3) no nivel de risco. */
    public static NivelRisco deCodigo(int codigo) {
        for (NivelRisco nivel : values()) {
            if (nivel.codigo == codigo) {
                return nivel;
            }
        }
        throw new IllegalArgumentException("Nível de risco desconhecido: " + codigo);
    }
}
