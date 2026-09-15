package src.token;
import src.compilador.TablaPalabrasReservadas;

public class TokenCadena extends Token {
    private String valor;

    public TokenCadena(String lexema, int linea) {
        super(TablaPalabrasReservadas.obtenerIdentificador("CADENA"));
        this.lexema = lexema;
        this.linea = linea;
    }

    public int getTipo(){
        return tipo;
    }
    /* 
    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }
    */
}