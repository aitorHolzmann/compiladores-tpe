package src.accion_semantica;
import java.io.*;
import src.compilador.AnalizadorLexico;
import src.compilador.TablaPalabrasReservadas;

public class AS1 extends Accion_Semantica{

    @Override 
    //TODO: la AS1 tambien se ejecuta al cerrar los mensajes multilinea con " " ", o al terminar un comentario. Que hacemos? 
    public int ejecutar(StringBuilder token_actual, Reader reader){
        try{
            char caracter_actual = (char) reader.read();
            token_actual.append(caracter_actual);
            String token_cerrado = token_actual.toString();

            int identificador_palabra_reservada = TablaPalabrasReservadas.obtenerIdentificador(token_cerrado);
            return identificador_palabra_reservada; // Este devuelve el numero de token.

        } catch (Exception e){
            e.printStackTrace();
        }

        return AnalizadorLexico.token_abierto;
    }
}
