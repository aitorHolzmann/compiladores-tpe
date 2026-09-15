package src.token;
import src.compilador.TablaPalabrasReservadas;
public class TokenConstante extends Token {
    private int valor;

    public TokenConstante(String lexema, int linea, int valor) {
        super(TablaPalabrasReservadas.obtenerIdentificador("SHORTINT"));
        this.lexema = lexema;
        this.linea = linea;
        setValor(valor);
    }

    public int getTipo(){
        return tipo;
    }

    public int getValor() {
        return valor;
    }

    public void setValor(int valor) {
        this.valor = valor;
    }

}