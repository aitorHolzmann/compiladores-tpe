package src.compilador;

import java.io.*;
import java.util.Scanner;
import src.accion_semantica.*;

public class AnalizadorLexico {

   
    //RANGOS
    public static final int MaxCaracteres = 22;
    //RANGO SHORTINT: -2⁷ HASTA 2⁷-1
    public static final int ValorMinimoInt = -128;
    public static final int ValorMaximoInt = 127;
    
    //RANGO SINGLEF
    public static final double ValorMinimoFloat = 1.17549435e-38;
    public static final double ValorMaximoFloat = 3.40282347e+38;
    
    

    //Caracteres especiales

    //private static final int NUMERO = 258; //Que hacemos si viene un numero sin sufijo. ej: x = 2

    private static final char BLANCO = ' ';
    private static final char TAB = '\t';
    private static final char SALTO_LINEA = '\n';
    
    private static final char DIGITO = '0';
    private static final char LETRA_MINUSCULA = 'a';
    private static final char LETRA_MAYUSCULA = 'A';
    private static final char EXPONENTE = 's';

    public static int referenciaTablaSimbolos = -1;

    
    //Atributos de la clase
    public static PushbackReader reader;
    public static int estado_actual = 0;
    private static int linea_actual = 1;   

    // Cantidad de estados del automata 12 + Final + Error
    private static final int CANT_ESTADOS = 14;

    //Cantidad de simbolos reconocidos 24 + Otros
    private static final int CANT_SIMBOLOS = 25;
    private static StringBuilder token_actual = new StringBuilder();
    public static final int token_abierto = -1;


    //Estructuras
    private static final String PATH_TABLA_TRANSICION_ESTADO = "src/estructuras/tabla_transicion_estados";
    private static final String PATH_TABLA_ACCIONES_SEMANTICAS = "src/estructuras/tabla_acciones_semanticas";
    private static final int[][]tabla_transicion_estado = leerMatriz(PATH_TABLA_TRANSICION_ESTADO, CANT_ESTADOS, CANT_SIMBOLOS);
    public static final int[][]tabla_acciones_semanticas = leerMatriz(PATH_TABLA_ACCIONES_SEMANTICAS, CANT_ESTADOS, CANT_SIMBOLOS);
    

    
    // GETTERS
    public int get_cant_simbolos(){
        return CANT_SIMBOLOS;
    }

    public int get_cant_estados(){
        return CANT_ESTADOS;
    }

    public static void setReferenciaTablaSimbolos(int referencia){
        referenciaTablaSimbolos = referencia;
    }


    // LOGICA DE NEGOCIO
    public static char getTipoCaracter(char caracterActual){
        if (Character.isDigit(caracterActual)){
            return DIGITO;
        } else if (caracterActual == 's' && (estado_actual == 5 || estado_actual == 7)) {
            return EXPONENTE;
        } else if (Character.isLowerCase(caracterActual)){
            return LETRA_MINUSCULA;
        } else if (Character.isUpperCase(caracterActual)){
            return LETRA_MAYUSCULA;
        } else {
            return caracterActual;
        }
    }

    public static void mostrarWarning(){
        System.out.println("WARNING: Linea: " + linea_actual + "\n El identificador tenia mas de 22 caracteres y fue truncado");
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
            case EXPONENTE:
                return 18;
            case '(':
                return 19;
            case ')':
                return 20;
            case '.':
                return 21;
            case ',':
                return 22;
            case ';':
                return 23;
            default:
                return 24;           

        }
    }


    public static int analizar(char caracterActual){
        int indice_caracter = indexarCaracter(caracterActual);
        //System.out.println("El caracter actual es: " + caracterActual);
        int numero_accion_semantica = tabla_acciones_semanticas[estado_actual][indice_caracter];
        //System.out.println("Accion semantica: " + numero_accion_semantica);
        int token_devuelto = ejecutar_accion_semantica(numero_accion_semantica, reader, caracterActual);
        //System.out.println("Token devuelto: "+ token_devuelto);
        estado_actual = tabla_transicion_estado[estado_actual][indice_caracter];


        if (token_devuelto != -1){
            token_actual.setLength(0);
            estado_actual = 0;
        }

        return token_devuelto;
    }

    public static int yylex(){
        try {
            int caracter_leido = reader.read();
            while (caracter_leido != -1){
                int token = analizar((char) caracter_leido);
                
                if (token != token_abierto){
                    System.out.println("El token es: " + token);
                    return token;
                }
                caracter_leido = reader.read();
            }
            return -1; // debe retornar 0 al final
        } catch (IOException e){
            e.printStackTrace();
            return -1;
        }

    }

    public static int ejecutar_accion_semantica(int accion, PushbackReader reader, char caracter_actual){
        Accion_Semantica accion_semantica = getAccionSemantica(accion);
        return accion_semantica.ejecutar(token_actual, reader, caracter_actual);
    }

    public static void incrementarLinea(){
        linea_actual = linea_actual + 1;
    }



    public static int getLineaActual() {
        return linea_actual;
    }

    public static StringBuilder getTokenActual() {
        return token_actual;
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

    public static Accion_Semantica getAccionSemantica(int numero){
        switch (numero) {
            case 0:
                return new ASe();
            case 1:
                return new AS1();
            case 2:
                return new AS2();
            case 3:
                return new AS3();
            case 4:
                return new AS4();
            case 5:
                return new AS5();
            case 6:
                return new AS6();
            case 7:
                return new AS7();
            case 8:
                return new AS8();
            case 9:
                return new AS9();
            case 10:
                return new AS10();
            case 11:
                return new AS11();
            case 12:
                return new AS12();
            case 13:
                return new ASw();
            default:
                return null;
        }
    }

}