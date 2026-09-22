package src.accion_semantica;
import java.io.*;
import src.compilador.TablaPalabrasReservadas;

public class AS1 extends Accion_Semantica{

    @Override 
    
    public int ejecutar(StringBuilder token_actual, PushbackReader reader, char caracter_actual){

        token_actual.append(caracter_actual);
        String token_cerrado = token_actual.toString();

        int identificador_palabra_reservada = TablaPalabrasReservadas.obtenerIdentificador(token_cerrado);
        return identificador_palabra_reservada; // Este devuelve el numero de token.

    }
}
