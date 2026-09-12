package src.accion_semantica;

import java.io.PushbackReader;
import java.io.Reader;
import src.compilador.AnalizadorLexico;
import src.compilador.TablaPalabrasReservadas;

public class AS6 extends Accion_Semantica{
    // Tiene que cerrar el token de numeros enteros, ver si esta en el rango y devolver el token de SHORTINT
    
    //POR QUE GUARDAMOS $s?
    @Override 
    public int ejecutar(StringBuilder token_actual, PushbackReader reader, char caracter_actual){
        //120$s
        token_actual.append(caracter_actual);
        String resultado = token_actual.toString();
        int posicion = resultado.indexOf('$');
        String soloDigitos = resultado.substring(0, posicion);
        int resultado_entero = Integer.parseInt(soloDigitos);
        if (resultado_entero >= AnalizadorLexico.ValorMinimoInt && resultado_entero <= AnalizadorLexico.ValorMaximoInt) {
            return TablaPalabrasReservadas.obtenerIdentificador("SHORTINT");
        }
        System.out.println("WARNING: El numero se sale de rango"); // deberia ser un error
        token_actual.setLength(0);
        return AnalizadorLexico.token_abierto;
    }
}

/*Aca mi token va a tener algo del estilo "120$s"
Como hago para que al parsear a INT solo tome el 120??

Respuesta Claude:          
        int posicion = resultado.indexOf('$');
        String soloDigitos = resultado.substring(0, posicion);
*/