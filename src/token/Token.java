package src.token;
import src.compilador.TablaPalabrasReservadas;

public abstract class Token {
    protected int id; //posicion en la tabla de simbolos
    protected String lexema; //lexema del token
    protected int linea; //para reportar warnings y errores
    protected int tipo;

    public Token(int tipo) {
        this.id = -1;
        this.lexema =  null;
    }

    public abstract int getTipo();
    
    public String getLexema() {
        return lexema;
    }
    public int getLinea() {
        return linea;
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

}