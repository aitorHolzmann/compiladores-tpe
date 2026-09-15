package src.token;

import src.compilador.TablaPalabrasReservadas;

public class TokenIdentificador extends Token {
    private boolean truncado;

    public TokenIdentificador(String lexema, int linea) {
        super(TablaPalabrasReservadas.obtenerIdentificador("IDENTIFICADOR"));
        this.linea = linea;
        this.lexema = lexema;
    }

    public int getTipo() {
        return this.tipo;
    }

    public void truncar() {
        this.truncado = true;
    }

    public boolean estaTruncado() {
        return truncado;
    }

}