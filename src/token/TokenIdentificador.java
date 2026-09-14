package src.token;
public class TokenIdentificador extends Token {
    private String lexema;
    private boolean truncado;
    private String tipo="identificador";

    public TokenIdentificador(String lexema) {
        this.lexema = lexema;
    }

    public String getTipo(){
        return tipo;
    }

}