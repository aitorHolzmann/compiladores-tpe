package src.accion_semantica;

import java.io.PushbackReader;
import java.io.Reader;
import src.compilador.AnalizadorLexico;
import src.compilador.TablaPalabrasReservadas;

public class AS6 extends Accion_Semantica{
    @Override 
    public int ejecutar(StringBuilder token_actual, PushbackReader reader, char caracter_actual){
        token_actual.append(caracter_actual);
        String resultado = token_actual.toString();
        int resultado_entero = Integer.parseInt(resultado);
        if (resultado_entero >= AnalizadorLexico.ValorMinimoInt && resultado_entero <= AnalizadorLexico.ValorMaximoInt) {
            return TablaPalabrasReservadas.obtenerIdentificador("SHORTINT");
        }
        return AnalizadorLexico.token_abierto;
    }
}