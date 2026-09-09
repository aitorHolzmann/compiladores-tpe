package src.accion_semantica;

import compilador.AnalizadorLexico;
import java.io.Reader;

public class AS3 extends Accion_Semantica{
    // pepe_$ -> estoy en el "$" y no tiene que ser parte de la variable identificador
    
    @Override 
    public int ejecutar(StringBuilder token_actual, Reader reader){
        return AnalizadorLexico.IDENTIFICADOR;
    }
}