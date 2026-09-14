package src.token;
public class TokenCadena extends Token {
    private String valor;
    private String tipo="cadena";

    public TokenCadena(String lexema) {
        this.lexema = lexema;
    }

    public String getTipo(){
        return tipo;
    }

    public String getLexema() {
        return lexema;
    }
}