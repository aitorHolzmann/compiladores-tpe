package src.accion_semantica;

import java.io.PushbackReader;
import java.io.Reader;
import src.compilador.AnalizadorLexico;

public class AS7 extends Accion_Semantica{
    @Override 
    public int ejecutar(StringBuilder token_actual, PushbackReader reader, char caracter_actual){
        // Si estamos en estado 0 es por el primer caracter es un "." es decir que no es un metodo de un objeto. Por lo tanto agregamos al string "0." y luego leemos el siguiente
        if (AnalizadorLexico.estado_actual == 0){
            token_actual.append("0.");
            return AnalizadorLexico.token_abierto;
        }
        return 0; // Aca no va a ser 0. Falta implementar los manejos de metodos de objetos.
    }
}