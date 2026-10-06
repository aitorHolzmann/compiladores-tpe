package compilador;

import accion_semantica.AccionSemantica;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class AnalizadorLexico {
    public static final char TABULACION = '\t';
    public static final char BLANCO = ' ';
    public static final char NUEVA_LINEA = '\n';
    public static final char RETORNO_CARRO = '\r';
    public static final int IDENTIFICADOR = 257;
    public static final int CONSTANTE = 258;
    public static final int CADENA = 259;
    public static final int LONGITUD_IDENTIFICADOR = 22;
    public static final long MAX_LONG_VALUE = (long) Math.pow(2, 32) - 1;
    public static final double MIN_DOUBLE_VALUE = 2.2250738585072015E-308;
    public static final double MAX_DOUBLE_VALUE = 1.7976931348623156E+308;

    public static Reader lector;

    private static final char DIGITO = '0';
    private static final char MINUSCULA = 'a';
    private static final char MAYUSCULA = 'A';
    private static final int CANTIDAD_ESTADOS = 21;
    private static final int CANTIDAD_CARACTERES = 25;
    private static final String ARCHIVO_MATRIZ_ESTADOS = "src/matrizEstados.txt";
    private static final String ARCHIVO_MATRIZ_ACCIONES = "src/matrizAcciones.txt";

    private static final AccionSemantica[][] acciones_semanticas = FileHelper.readActionMatrixFile(ARCHIVO_MATRIZ_ACCIONES, CANTIDAD_ESTADOS, CANTIDAD_CARACTERES);
    private static final int[][] transicion_estados = FileHelper.readIntMatrixFile(ARCHIVO_MATRIZ_ESTADOS, CANTIDAD_ESTADOS, CANTIDAD_CARACTERES);

    public static final StringBuilder token_actual = new StringBuilder();

    public static int estado_actual = 0;
    private static int linea_actual = 1;

    public static int getLineaActual() {
        return linea_actual;
    }

    public static void setLineaActual(int numero_linea) {
        linea_actual = numero_linea;
    }

    /**
     *
     * @param caracter
     * @return el caracter colapsado en caso de ser un digito, minuscula o mayuscula diferente a "E"
     */
    private static char obtenerTipoCaracter(char caracter) {
        if (Character.isDigit(caracter)) {
            return DIGITO;
        } else if (Character.isLowerCase(caracter)) {
            return MINUSCULA;
        } else if (caracter != 'E' && Character.isUpperCase(caracter)) {
            return MAYUSCULA;
        } else {
            return caracter;
        }
    }

    // Se llama por cada caracter que se lee
    public static int cambiarEstado(Reader lector, char caracter) {
        int caracter_actual;
        switch (obtenerTipoCaracter(caracter)) {
            case BLANCO:
                caracter_actual = 0;
                break;
            case TABULACION:
                caracter_actual = 1;
                break;
            case NUEVA_LINEA:
            case RETORNO_CARRO:
                caracter_actual = 2;
                break;
            case MINUSCULA:
                caracter_actual = 3;
                break;
            case MAYUSCULA:
                caracter_actual = 4;
                break;
            case '_':
                caracter_actual = 5;
                break;
            case DIGITO:
                caracter_actual = 6;
                break;
            case '.':
                caracter_actual = 7;
                break;
            case 'E':
                caracter_actual = 8;
                break;
            case '+':
                caracter_actual = 9;
                break;
            case '-':
                caracter_actual = 10;
                break;
            case '/':
                caracter_actual = 11;
                break;
            case '(':
                caracter_actual = 12;
                break;
            case ')':
                caracter_actual = 13;
                break;
            case ',':
                caracter_actual = 14;
                break;
            case ';':
                caracter_actual = 15;
                break;
            case ':':
                caracter_actual = 16;
                break;
            case '=':
                caracter_actual = 17;
                break;
            case '>':
                caracter_actual = 18;
                break;
            case '<':
                caracter_actual = 19;
                break;
            case '&':
                caracter_actual = 20;
                break;
            case '|':
                caracter_actual = 21;
                break;
            case '*':
                caracter_actual = 22;
                break;
            case '%':
                caracter_actual = 23;
                break;
            default:
                caracter_actual = 24;
                break;
        }

        AccionSemantica accion_a_ejecutar = acciones_semanticas[estado_actual][caracter_actual];
        int identificador_token = accion_a_ejecutar.ejecutar(lector, token_actual);
        estado_actual = transicion_estados[estado_actual][caracter_actual];

        return identificador_token;
    }
}
