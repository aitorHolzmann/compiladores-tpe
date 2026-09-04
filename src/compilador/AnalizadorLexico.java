package src.compilador;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PushbackReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
public class AnalizadorLexico {
   
    //RANGOS
    public static final int MaxCaracteres = 22;
    //RANGO SHORTINT: -2⁷ HASTA 2⁷-1
    private static final int ValorMinimoInt = -128;
    private static final int ValorMaximoInt = 127;
    
    //RANGO SINGLEF
    private static final double ValorMinimoFloat = 1.17549435e-38;
    private static final double ValorMaximoFloat = 3.40282347e+38;
    
    

    //Caracteres especiales
    private static final char BLANCO = ' ';
    private static final char TAB = '\t';
    private static final char SALTO_LINEA = '\n';
    private static final int IDENTIFICADOR = 257;
    private static final int NUMERO = 258;
    private static final int CONSTANTE = 259;
    private static final int CADENA = 260;
    private static final char DIGITO = '0';
    private static final char LETRA_MINUSCULA = 'a';
    private static final char LETRA_MAYUSCULA = 'A';
    


    
    //Atributos de la clase
    public static Reader reader;
    private static int estado_actual = 0;
    private static int linea_actual = 1;    

    // Cantidad de estados del automata 12 + Final + Error
    private static final int CANT_ESTADOS = 14;

    //Cantidad de simbolos reconocidos 24 + Otros
    private static final int CANT_SIMBOLOS = 25;
    private static final StringBuilder token_actual = new StringBuilder();

    //Estructuras
    private static final String PATH_TABLA_TRANSICION_ESTADO = "src/estructuras/tabla_transicion_estados";
    private static final String PATH_TABLA_ACCIONES_SEMANTICAS = "src/estructuras/tabla_acciones_semanticas";
    private static final int[][]tabla_transicion_estado = leerMatriz(PATH_TABLA_TRANSICION_ESTADO, CANT_ESTADOS, CANT_SIMBOLOS);
    private static final int[][]tabla_acciones_semanticas = leerMatriz(PATH_TABLA_ACCIONES_SEMANTICAS, CANT_ESTADOS, CANT_SIMBOLOS);
    
    
    public static char getTipoCaracter(char caracterActual){
        if (Character.isDigit(caracterActual)){
            return DIGITO;
        } else if (caracterActual != 's' && Character.isLowerCase(caracterActual)){
            return LETRA_MINUSCULA;
        } else if (Character.isUpperCase(caracterActual)){
            return LETRA_MAYUSCULA;
        } else {
            return caracterActual;
        }
    }
    
    
    private static int indexarCaracter(char caracterActual){
        switch (getTipoCaracter(caracterActual)) { 
            case DIGITO: 
                return 0;
            case LETRA_MINUSCULA:
                return 1;
            case LETRA_MAYUSCULA:
                return 2;           
            case BLANCO:
                return 3;
            case TAB:
                return 4;
            case SALTO_LINEA:
                return 5;
            case '+':
                return 6;
            case '-':
                return 7;
            case '*':
                return 8;  
            case '/':
                return 9;
            case '<':
                return 10;
            case '>':
                return 11;
            case '!':
                return 12;
            case '=':
                return 13;
            case ':':
                return 14;
            case '_':
                return 15;
            case '"':
                return 16;
            case '$':
                return 17;
            case '(':
                return 18;
            case ')':
                return 19;
            case '.':
                return 20;
            case ',':
                return 21;
            case ';':
                return 22;
            default:
                return 23;
        }
    }

    public static void cambiarEstado(Reader reader, char caracterActual){
        //int indice_caracter = indexarCaracter(caracterActual);
        //AccionSemantica accion = acciones_semanticas[estado_actual][indice_caracter];
        //int resultado = accion.ejecutar(reader, token_actual);
        //estado_actual = transicion_estados[estado_actual][indice_caracter];
        //return resultado;
        
        int indice_caracter = indexarCaracter(caracterActual);
        System.out.println("El caracter actual es: " + indice_caracter); 
    }

    public static int[][] leerMatriz(String path, int filas, int columnas) {
        int[][] matriz = new int[filas][columnas];

        try {
            File archivo = new File(path);
            Scanner scanner = new Scanner(archivo);

            for (int i = 0; i < filas; ++i) {
                for (int j = 0; j < columnas; ++j) {
                    matriz[i][j] = Integer.parseInt(scanner.nextLine());
                }
            }

            scanner.close();
        } catch (FileNotFoundException excepcion) {
            System.out.println("No se pudo leer el archivo " + path);  
            excepcion.printStackTrace();
        }

        return matriz;
    }
}
