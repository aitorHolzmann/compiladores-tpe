package src.compilador;

import java.io.*;

public class Main {
    public static void main(String[] args) {
    
    AnalizadorLexico analizador = new AnalizadorLexico();
    var cant_estados = analizador.get_cant_estados();
    var cant_simbolos = analizador.get_cant_simbolos();

    String PATH_PROGRAMA = "tests/prueba1.txt";
    System.out.println("Se inicia la compilacion del archivo: " + PATH_PROGRAMA);
    try {
        AnalizadorLexico.reader = new PushbackReader(new BufferedReader(new FileReader(PATH_PROGRAMA)));
        int caracter_leido = AnalizadorLexico.reader.read();
        while(caracter_leido != -1){
            //System.out.println("El caracter leido es: " + caracter_leido);
            //caracter_leido = AnalizadorLexico.reader.read();
            analizador.analizar((char) caracter_leido);
            caracter_leido = AnalizadorLexico.reader.read();
        }
        System.out.println("Se llego al final del archivo");
    
    } catch (Exception e) {
        e.printStackTrace();
    }

    
    }
}