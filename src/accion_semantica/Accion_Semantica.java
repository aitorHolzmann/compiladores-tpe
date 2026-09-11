package src.accion_semantica;

import java.io.*;

public abstract class Accion_Semantica {

    public Accion_Semantica() {
    };
    
    protected int TOKEN_ACTIVO = -1;
    protected int ERROR = -2;
    protected int WARNING = -3;
    public abstract int ejecutar(StringBuilder token_actual, PushbackReader reader, char caracter_actual);
}