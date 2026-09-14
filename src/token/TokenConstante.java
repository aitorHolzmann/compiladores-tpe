package src.token;
public class TokenConstante extends Token {
    private int valor;
    private boolean enRango;
    private String tipo="constante";

    public TokenConstante(String lexema) {
        this.lexema = lexema;
    }

    public String getTipo(){
        return tipo;
    }

    public String getLexema() {
        return lexema;
    }
}