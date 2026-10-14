package src.accion_semantica;
import java.io.*;
import src.compilador.*;

public class AS12 extends Accion_Semantica{

    @Override 
    public int ejecutar(StringBuilder token_actual, PushbackReader reader, char caracter_actual){
        token_actual.append(caracter_actual);
        String token_cerrado = token_actual.toString();
        // Tema 10: las cadenas multilineas se guardan sin saltos de linea en la Tabla de Simbolos
        String cadena_limpia = token_cerrado.replace("\r", "").replace("\n", "");
        int id = TablaSimbolos.agregarOBuscarConstante(cadena_limpia);
        AnalizadorLexico.setReferenciaTablaSimbolos(id);
        return TablaPalabrasReservadas.obtenerIdentificador("CADENA");
    }
}

