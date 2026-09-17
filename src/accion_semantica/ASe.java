package src.accion_semantica;

import java.io.PushbackReader;
import java.io.Reader;
import src.compilador.AnalizadorLexico;

public class ASe extends Accion_Semantica{
    @Override 
    public int ejecutar(StringBuilder token_actual, PushbackReader reader, char caracter_actual){
        token_actual.append(caracter_actual);
        System.out.println("Error en linea "+ AnalizadorLexico.getLineaActual());
        System.out.println(token_actual.toString() + " No pertenece al lenguaje");
        return 0;
    }
}