package src.compilador;

import java.io.*;

public class Main {
    public static void main(String[] args) {
    
    AnalizadorLexico analizador = new AnalizadorLexico();
    var cant_estados = analizador.get_cant_estados();
    var cant_simbolos = analizador.get_cant_simbolos();

    String PATH_PROGRAMA = "tests/test_parametro_formal.txt";
    System.out.println("Se inicia la compilacion del archivo: " + PATH_PROGRAMA);
    try {
        AnalizadorLexico.reader = new PushbackReader(new BufferedReader(new FileReader(PATH_PROGRAMA)));
        
        while (AnalizadorLexico.yylex() != -1 );
        
        System.out.println("Fin del archivo");
    } catch (Exception e) {
        e.printStackTrace();
    }

    
    }
}