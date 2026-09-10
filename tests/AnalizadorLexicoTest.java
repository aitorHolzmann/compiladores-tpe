package tests;

import src.compilador.AnalizadorLexico;

public class AnalizadorLexicoTest {
    public static void main(String[] args) {
        verificar('7', '0');
        verificar('a', 'a');
        verificar('S', 'A');
        verificar('+', '+');

        System.out.println("AnalizadorLexicoTest: OK");
    }

    private static void verificar(char entrada, char esperado) {
        char resultado = AnalizadorLexico.getTipoCaracter(entrada);
        if (resultado != esperado) {
            throw new AssertionError("Para '" + entrada + "' se esperaba '"
                    + esperado + "', pero se obtuvo '" + resultado + "'");
        }
    }
}