package src.compilador;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class FileHelper{



    public static Map<String, Integer> readMapFile(String path) {
            Map<String, Integer> map = new HashMap<>();

            try {
                File archivo = new File(path);
                Scanner scanner = new Scanner(archivo);

                while (scanner.hasNext()) {
                    String palabra_reservada = scanner.next();
                    int identificador = scanner.nextInt();
                    map.put(palabra_reservada, identificador);
                }

                scanner.close();
            } catch (FileNotFoundException excepcion) {
                System.out.println("No se pudo leer el archivo " + path);
                excepcion.printStackTrace();
            }

            return map;
        }
    
}
