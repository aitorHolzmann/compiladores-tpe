package src.token;
import src.compilador.TablaPalabrasReservadas;
public class TokenFloat extends Token {
    private double valor;

    public TokenFloat(String lexema, int linea) {
        super(TablaPalabrasReservadas.obtenerIdentificador("SINGLEF"));
        this.linea = linea;
        this.lexema = lexema;
    }

    public int getTipo(){
        return tipo;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

}