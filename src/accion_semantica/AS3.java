package src.accion_semantica;

import java.io.PushbackReader;
import java.io.Reader;
import src.compilador.AnalizadorLexico;

public class AS3 extends Accion_Semantica{
    // pepe_$ -> estoy en el "$" y no tiene que ser parte de la variable identificador
    
    @Override 
    public int ejecutar(StringBuilder token_actual, PushbackReader reader, char caracter_actual){
        String token_entregado = token_actual.toString();
        if (token_entregado.length() > 22){
            String truncado = token_entregado.substring(0, 22); // o 21?
            token_entregado = truncado;
            AnalizadorLexico.mostrarWarning();
        }
        AnalizadorLexico.setLexema(token_entregado);
        return AnalizadorLexico.IDENTIFICADOR;
    }
}