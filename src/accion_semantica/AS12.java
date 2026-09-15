package src.accion_semantica;
import java.io.*;
import src.compilador.AnalizadorLexico;
import src.compilador.TablaPalabrasReservadas;
import src.compilador.TablaSimbolos;
import src.token.*;

public class AS12 extends Accion_Semantica{

    @Override 
    //TODO: la AS1 tambien se ejecuta al cerrar los mensajes multilinea con " " ", o al terminar un comentario. Que hacemos? 
    public int ejecutar(StringBuilder token_actual, PushbackReader reader, char caracter_actual){
        token_actual.append(caracter_actual);
        String token_cerrado = token_actual.toString();

        Token nuevo_token_cadena = new TokenCadena(token_cerrado, AnalizadorLexico.getLineaActual());
        int nuevo_identificador = TablaSimbolos.agregarSimbolo(nuevo_token_cadena);
        AnalizadorLexico.setReferenciaTablaSimbolos(nuevo_identificador);
        
        return TablaPalabrasReservadas.obtenerIdentificador("CADENA");
    }
}
