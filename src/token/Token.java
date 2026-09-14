package src.token;
public abstract class Token {
    protected int id; //posicion en la tabla de simbolos
    protected String lexema; //lexema del token
    protected int linea; //para reportar warnings y errores
    // hay que cambiar las AS paara controlar rangos y demas dentro de cada token o no habrian problemas de esta manera?
    public abstract String getTipo();
    public String getLexema() {
        return lexema;
    }
    public int getLinea() {
        return linea;
    }
    public int getId() {
        return id;
    }
}