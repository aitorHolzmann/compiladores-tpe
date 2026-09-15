package src.compilador;

import java.io.*;
import src.Parser.*;

public class Main {

    public static void main(String[] args) {
    AnalizadorLexico analizador = new AnalizadorLexico();
    var cant_estados = analizador.get_cant_estados();
    var cant_simbolos = analizador.get_cant_simbolos();

    String PATH_PROGRAMA = "tests/prueba1.txt";
    System.out.println("Se inicia la compilacion del archivo: " + PATH_PROGRAMA);
    try {
        AnalizadorLexico.reader = new PushbackReader(new BufferedReader(new FileReader(PATH_PROGRAMA)));
        
        while (AnalizadorLexico.yylex() != -1 );
        
        System.out.println("Fin del archivo");
    } catch (Exception e) {
        e.printStackTrace();
    }
    
}

/* 
    public static void main(String[] args) throws Exception {
        Parser parser = new Parser();
        
        AnalizadorLexico.reader = new java.io.PushbackReader(
            new java.io.BufferedReader(new java.io.FileReader(args[0])));
        parser.yyparse();
    }
    */
}