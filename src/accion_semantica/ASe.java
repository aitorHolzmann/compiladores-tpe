package src.accion_semantica;

import java.io.PushbackReader;
import java.io.Reader;
import src.compilador.AnalizadorLexico;

public class ASe extends Accion_Semantica{
    @Override 
    public int ejecutar(StringBuilder token_actual, PushbackReader reader, char caracter_actual){
        token_actual.append(caracter_actual);
        AnalizadorLexico.mostrarError("Caracter o simbolo no pertenece al lenguaje: '" + token_actual.toString() + "'");
        return 0;
    }
}