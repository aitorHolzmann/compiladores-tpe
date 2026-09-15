package src.token;

import src.compilador.TablaPalabrasReservadas;

public class TokenIdentificador extends Token {
    private boolean truncado;
    private String valor;

    public TokenIdentificador(String lexema, int linea, boolean estaTruncado) {
        super(TablaPalabrasReservadas.obtenerIdentificador("IDENTIFICADOR"));
        this.linea = linea;
        this.lexema = lexema;
        this.truncado = estaTruncado;
    }

    public int getTipo() {
        return this.tipo;
    }
    
    public void setValor(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return this.valor;
    }

    public boolean estaTruncado() {
        return truncado;
    }

}