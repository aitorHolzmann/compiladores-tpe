package src.token;
public class TokenFloat extends Token {
    private double valor;
    private boolean enRango;
    private String tipo="float";

    public TokenFloat(String lexema) {
        this.lexema = lexema;
    }

    public String getTipo(){
        return tipo;
    }

    public String getLexema() {
        return lexema;
    }
}