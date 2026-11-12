package src.compilador;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GeneradorAssembler {

    // Mapeo de claves de la tabla de simbolos a identificadores validos en MASM32
    private static final Map<String, String> mapaNombresAsm = new HashMap<>();
    private static int contadorStr = 0;
    private static StringBuilder codigoData;
    private static StringBuilder codigoCode;

    // Obtiene el identificador valido para MASM32 segun el tipo de simbolo
    public static String obtenerNombreAsm(String claveTS) {
        if (claveTS == null) {
            return "";
        }
        if (mapaNombresAsm.containsKey(claveTS)) {
            return mapaNombresAsm.get(claveTS);
        }
        // Auxiliares del sistema o internas que ya empiezan con @
        if (claveTS.startsWith("@")) {
            return claveTS;
        }

        String uso = TablaSimbolos.obtenerUso(claveTS);
        Object ocs = TablaSimbolos.obtenerAtributo(claveTS, "OCURRENCIAS");

        // Si es una constante registrada en la tabla de simbolos
        if (uso == null && ocs != null) {
            String tipo = TablaSimbolos.obtenerTipo(claveTS);
            String nombre;
            if ("CADENA".equals(tipo)) {
                nombre = "_str_" + contadorStr;
                contadorStr++;
            } else {
                // Constante SHORTINT o SINGLEF
                String limpio = claveTS.replace("-", "neg_")
                                       .replace(".", "_")
                                       .replace("+", "pos_");
                nombre = "_cte_" + limpio;
            }
            mapaNombresAsm.put(claveTS, nombre);
            return nombre;
        }

        // Variables, parametros o atributos con ambito
        if (claveTS.contains(TablaSimbolos.SEPARADOR)) {
            String nombre = "_" + claveTS;
            mapaNombresAsm.put(claveTS, nombre);
            return nombre;
        }

        // Por defecto prefijo _
        String nombre = "_" + claveTS;
        mapaNombresAsm.put(claveTS, nombre);
        return nombre;
    }

    // Formatea el literal flotante para directiva DD de MASM32
    private static String formatearLiteralSinglef(String lit) {
        if (lit == null || lit.isEmpty()) {
            return "0.0";
        }
        String formateado = lit.replace('s', 'E').replace('S', 'E');
        if (formateado.startsWith("-.")) {
            formateado = "-0." + formateado.substring(2);
        } else if (formateado.startsWith(".")) {
            formateado = "0." + formateado.substring(1);
        }
        if (formateado.endsWith(".")) {
            formateado = formateado + "0";
        }
        if (!formateado.contains(".") && !formateado.contains("E") && !formateado.contains("e")) {
            formateado = formateado + ".0";
        }
        return formateado;
    }

    // Genera el nombre de la variable auxiliar asociada a un terceto
    public static String obtenerNombreAux(String ambito, int indice) {
        if (TablaSimbolos.AMBITO_GLOBAL.equals(ambito)) {
            return "@aux" + indice;
        }
        return "@aux_" + ambito.replace(TablaSimbolos.SEPARADOR, "_") + "_" + indice;
    }

    // Si un operando constante plegado no estaba en la Tabla de Simbolos, lo da de alta
    private static void registrarConstanteSiFalta(String op, String tipoSugerido) {
        if (op == null || op.equals("-") || op.equals("_") || op.equals("?") || op.startsWith("[")) {
            return;
        }
        if (op.contains("[") && op.endsWith("]")) {
            return;
        }
        if (!TablaSimbolos.existeSimbolo(op) && !op.contains(TablaSimbolos.SEPARADOR)) {
            String tipo = tipoSugerido;
            if (op.startsWith("\"")) {
                tipo = "CADENA";
            } else if (op.contains(".") || op.contains("s") || op.contains("S") || op.contains("e") || op.contains("E")) {
                tipo = "SINGLEF";
            } else if (tipo == null) {
                tipo = "SHORTINT";
            }
            TablaSimbolos.agregarOBuscarConstante(op, tipo);
        }
    }

    // Pre-registra variables @aux y constantes plegadas en la Tabla de Simbolos antes de emitir .data
    private static void registrarAuxiliaresYConstantes(Map<String, ArrayList<Terceto>> tercetosPorAmbito) {
        if (tercetosPorAmbito == null) {
            return;
        }
        for (Map.Entry<String, ArrayList<Terceto>> entry : tercetosPorAmbito.entrySet()) {
            String ambito = entry.getKey();
            ArrayList<Terceto> lista = entry.getValue();
            if (lista == null) {
                continue;
            }
            for (int i = 0; i < lista.size(); i++) {
                Terceto t = lista.get(i);
                String op = t.getOperador();
                String tipo = t.getTipo();

                if ("tos".equals(op)) {
                    registrarConstanteSiFalta(t.getOperando1(), "SINGLEF");
                } else {
                    registrarConstanteSiFalta(t.getOperando1(), tipo);
                    registrarConstanteSiFalta(t.getOperando2(), tipo);
                }

                if ("+".equals(op) || "-".equals(op) || "*".equals(op) || "/".equals(op) || "tos".equals(op)) {
                    String nombreAux = obtenerNombreAux(ambito, i);
                    String tipoAux = tipo;
                    if ("tos".equals(op) && tipoAux == null) {
                        tipoAux = "SHORTINT";
                    }
                    if (!TablaSimbolos.existeSimbolo(nombreAux)) {
                        TablaSimbolos.agregarIdentificador(nombreAux);
                        TablaSimbolos.agregarAtributo(nombreAux, TablaSimbolos.TIPO, tipoAux);
                        TablaSimbolos.agregarAtributo(nombreAux, TablaSimbolos.USO, "auxiliar");
                    }
                }
            }
        }
    }

    // Resuelve el acceso posicional si corresponde (Tema 29)
    private static String resolverAccesoPosicional(String op) {
        if (op == null) {
            return "";
        }
        return op;
    }

    // Traduce un operando de terceto (variable, constante o referencia [i]) a su nombre en Assembler
    public static String resolverOperandoAsm(String op, String ambito, ArrayList<Terceto> lista) {
        if (op == null) {
            return "";
        }
        if (op.startsWith("[") && op.endsWith("]")) {
            int idx = Integer.parseInt(op.substring(1, op.length() - 1));
            if (lista != null && idx >= 0 && idx < lista.size()) {
                Terceto ref = lista.get(idx);
                // Tema 17: si referencia a una asignacion en expresion, el valor quedo en operando1
                if (":=".equals(ref.getOperador())) {
                    return obtenerNombreAsm(resolverAccesoPosicional(ref.getOperando1()));
                }
            }
            return obtenerNombreAux(ambito, idx);
        }
        return obtenerNombreAsm(resolverAccesoPosicional(op));
    }

    // Genera el codigo Assembler completo para MASM32
    public static String generarAssembler() {
        mapaNombresAsm.clear();
        contadorStr = 0;
        codigoData = new StringBuilder();
        codigoCode = new StringBuilder();

        Map<String, ArrayList<Terceto>> tercetosPorAmbito = GeneradorTerceto.obtenerTercetosPorAmbito();
        registrarAuxiliaresYConstantes(tercetosPorAmbito);

        // 1. Encabezado estandar MASM32 de la catedra
        StringBuilder encabezado = new StringBuilder();
        encabezado.append(".386\n");
        encabezado.append(".model flat, stdcall\n");
        encabezado.append("option casemap :none\n");
        encabezado.append("include \\masm32\\include\\masm32rt.inc\n");
        encabezado.append("includelib \\masm32\\lib\\kernel32.lib\n");
        encabezado.append("includelib \\masm32\\lib\\masm32.lib\n");
        encabezado.append("dll_dllcrt0 PROTO C\n");
        encabezado.append("printf PROTO C :VARARG\n\n");

        // 2. Seccion .data con variables, constantes y auxiliares
        codigoData.append(".data\n");
        Map<String, Map<String, Object>> tabla = TablaSimbolos.obtenerTabla();
        for (Map.Entry<String, Map<String, Object>> entrada : tabla.entrySet()) {
            String clave = entrada.getKey();
            Map<String, Object> atr = entrada.getValue();
            if (atr == null) {
                continue;
            }
            String uso = (String) atr.get(TablaSimbolos.USO);
            String tipo = (String) atr.get(TablaSimbolos.TIPO);
            Object ocs = atr.get("OCURRENCIAS");

            if (uso != null) {
                if (uso.equals("variable") || uso.equals("parametro") || uso.equals("auxiliar") || uso.equals("atributo")) {
                    String nombreAsm = obtenerNombreAsm(clave);
                    if (TablaSimbolos.esComptime(clave)) {
                        codigoData.append("    ; variable comptime omitida: ").append(nombreAsm).append("\n");
                    } else if ("SHORTINT".equals(tipo)) {
                        codigoData.append("    ").append(nombreAsm).append(" DB ?\n");
                    } else if ("SINGLEF".equals(tipo)) {
                        codigoData.append("    ").append(nombreAsm).append(" DD ?\n");
                    }
                }
            } else if (ocs != null) {
                String nombreAsm = obtenerNombreAsm(clave);
                if ("SHORTINT".equals(tipo)) {
                    codigoData.append("    ").append(nombreAsm).append(" DB ").append(clave).append("\n");
                } else if ("SINGLEF".equals(tipo)) {
                    String lit = formatearLiteralSinglef(clave);
                    codigoData.append("    ").append(nombreAsm).append(" DD ").append(lit).append("\n");
                } else if ("CADENA".equals(tipo)) {
                    String contenido = clave;
                    if (contenido.startsWith("\"") && contenido.endsWith("\"") && contenido.length() >= 2) {
                        contenido = contenido.substring(1, contenido.length() - 1);
                    }
                    if (contenido.isEmpty()) {
                        codigoData.append("    ").append(nombreAsm).append(" DB 0\n");
                    } else {
                        codigoData.append("    ").append(nombreAsm).append(" DB \"").append(contenido).append("\", 0\n");
                    }
                }
            }
        }
        codigoData.append("    @aux_print_short DW ?\n");
        codigoData.append("    @aux_print_float DQ ?\n");
        codigoData.append("    @aux_tos DW ?\n\n");

        // 3. Seccion .code con inicio en start:
        codigoCode.append(".code\n");
        codigoCode.append("start:\n");

        String ambitoMain = TablaSimbolos.AMBITO_GLOBAL;
        ArrayList<Terceto> tercetosMain = tercetosPorAmbito.get(ambitoMain);
        if (tercetosMain != null) {
            for (int i = 0; i < tercetosMain.size(); i++) {
                Terceto t = tercetosMain.get(i);
                String op = t.getOperador();
                String tipo = t.getTipo();

                if ("+".equals(op)) {
                    String op1 = resolverOperandoAsm(t.getOperando1(), ambitoMain, tercetosMain);
                    String op2 = resolverOperandoAsm(t.getOperando2(), ambitoMain, tercetosMain);
                    String aux = obtenerNombreAux(ambitoMain, i);
                    if ("SHORTINT".equals(tipo)) {
                        // Suma entera de 8 bits en registro AL
                        codigoCode.append("    MOV AL, ").append(op1).append("\n");
                        codigoCode.append("    ADD AL, ").append(op2).append("\n");
                        codigoCode.append("    MOV ").append(aux).append(", AL\n");
                    } else if ("SINGLEF".equals(tipo)) {
                        // Suma flotante de 32 bits en coprocesador 80x87
                        codigoCode.append("    FLD ").append(op1).append("\n");
                        codigoCode.append("    FADD ").append(op2).append("\n");
                        codigoCode.append("    FSTP ").append(aux).append("\n");
                    }
                } else if ("-".equals(op)) {
                    String op1 = resolverOperandoAsm(t.getOperando1(), ambitoMain, tercetosMain);
                    String op2 = resolverOperandoAsm(t.getOperando2(), ambitoMain, tercetosMain);
                    String aux = obtenerNombreAux(ambitoMain, i);
                    if ("SHORTINT".equals(tipo)) {
                        // Resta entera de 8 bits en registro AL
                        codigoCode.append("    MOV AL, ").append(op1).append("\n");
                        codigoCode.append("    SUB AL, ").append(op2).append("\n");
                        codigoCode.append("    MOV ").append(aux).append(", AL\n");
                    } else if ("SINGLEF".equals(tipo)) {
                        // Resta flotante de 32 bits en coprocesador 80x87
                        codigoCode.append("    FLD ").append(op1).append("\n");
                        codigoCode.append("    FSUB ").append(op2).append("\n");
                        codigoCode.append("    FSTP ").append(aux).append("\n");
                    }
                } else if ("*".equals(op)) {
                    String op1 = resolverOperandoAsm(t.getOperando1(), ambitoMain, tercetosMain);
                    String op2 = resolverOperandoAsm(t.getOperando2(), ambitoMain, tercetosMain);
                    String aux = obtenerNombreAux(ambitoMain, i);
                    if ("SHORTINT".equals(tipo)) {
                        // Multiplicacion entera con signo de 8 bits (AL * op2 -> AX, parte baja en AL)
                        codigoCode.append("    MOV AL, ").append(op1).append("\n");
                        codigoCode.append("    IMUL ").append(op2).append("\n");
                        codigoCode.append("    MOV ").append(aux).append(", AL\n");
                    } else if ("SINGLEF".equals(tipo)) {
                        // Multiplicacion flotante de 32 bits en coprocesador 80x87
                        codigoCode.append("    FLD ").append(op1).append("\n");
                        codigoCode.append("    FMUL ").append(op2).append("\n");
                        codigoCode.append("    FSTP ").append(aux).append("\n");
                    }
                } else if ("/".equals(op)) {
                    String op1 = resolverOperandoAsm(t.getOperando1(), ambitoMain, tercetosMain);
                    String op2 = resolverOperandoAsm(t.getOperando2(), ambitoMain, tercetosMain);
                    String aux = obtenerNombreAux(ambitoMain, i);
                    if ("SHORTINT".equals(tipo)) {
                        // Division entera con signo de 8 bits (CBW extiende AL a AX; cociente en AL)
                        codigoCode.append("    MOV AL, ").append(op1).append("\n");
                        codigoCode.append("    CBW\n");
                        codigoCode.append("    IDIV ").append(op2).append("\n");
                        codigoCode.append("    MOV ").append(aux).append(", AL\n");
                    } else if ("SINGLEF".equals(tipo)) {
                        // Division flotante de 32 bits en coprocesador 80x87
                        codigoCode.append("    FLD ").append(op1).append("\n");
                        codigoCode.append("    FDIV ").append(op2).append("\n");
                        codigoCode.append("    FSTP ").append(aux).append("\n");
                    }
                } else if (":=".equals(op)) {
                    String op1 = resolverOperandoAsm(t.getOperando1(), ambitoMain, tercetosMain);
                    String op2 = resolverOperandoAsm(t.getOperando2(), ambitoMain, tercetosMain);
                    if (tipo == null) {
                        tipo = TablaSimbolos.obtenerTipo(t.getOperando1());
                    }
                    if ("SHORTINT".equals(tipo)) {
                        codigoCode.append("    MOV AL, ").append(op2).append("\n");
                        codigoCode.append("    MOV ").append(op1).append(", AL\n");
                    } else if ("SINGLEF".equals(tipo)) {
                        codigoCode.append("    FLD ").append(op2).append("\n");
                        codigoCode.append("    FSTP ").append(op1).append("\n");
                    }
                } else if ("tos".equals(op)) {
                    // Tema 34: conversion explicita de SINGLEF a SHORTINT
                    String op1 = resolverOperandoAsm(t.getOperando1(), ambitoMain, tercetosMain);
                    String aux = obtenerNombreAux(ambitoMain, i);
                    codigoCode.append("    FLD ").append(op1).append("\n");
                    codigoCode.append("    FISTP @aux_tos\n");
                    codigoCode.append("    MOV AX, @aux_tos\n");
                    codigoCode.append("    MOV ").append(aux).append(", AL\n");
                } else if ("POUT".equals(op)) {
                    String op1Raw = t.getOperando1();
                    String opAsm = resolverOperandoAsm(op1Raw, ambitoMain, tercetosMain);
                    if (tipo == null) {
                        if (op1Raw != null && op1Raw.startsWith("[") && op1Raw.endsWith("]")) {
                            int idx = Integer.parseInt(op1Raw.substring(1, op1Raw.length() - 1));
                            tipo = tercetosMain.get(idx).getTipo();
                        } else {
                            tipo = TablaSimbolos.obtenerTipo(op1Raw);
                        }
                    }
                    if ("CADENA".equals(tipo)) {
                        codigoCode.append("    invoke printf, cfm$(\"%s\\n\"), OFFSET ").append(opAsm).append("\n");
                    } else if ("SHORTINT".equals(tipo)) {
                        codigoCode.append("    MOV AL, ").append(opAsm).append("\n");
                        codigoCode.append("    MOVSX AX, AL\n");
                        codigoCode.append("    MOV @aux_print_short, AX\n");
                        codigoCode.append("    invoke printf, cfm$(\"%hd\\n\"), @aux_print_short\n");
                    } else if ("SINGLEF".equals(tipo)) {
                        codigoCode.append("    FLD ").append(opAsm).append("\n");
                        codigoCode.append("    FSTP @aux_print_float\n");
                        codigoCode.append("    invoke printf, cfm$(\"%Lf\\n\"), @aux_print_float\n");
                    }
                }
            }
        }

        codigoCode.append("    invoke ExitProcess, 0\n");
        codigoCode.append("end start\n");

        return encabezado.toString() + codigoData.toString() + codigoCode.toString();
    }

    // Guarda el contenido assembler reemplazando o agregando extension .asm
    public static void guardarArchivo(String rutaFuente, String contenidoAsm) {
        String rutaSalida;
        if (rutaFuente.endsWith(".txt")) {
            rutaSalida = rutaFuente.substring(0, rutaFuente.length() - 4) + ".asm";
        } else {
            rutaSalida = rutaFuente + ".asm";
        }
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(rutaSalida, java.nio.charset.StandardCharsets.UTF_8));
            writer.write(contenidoAsm);
            writer.close();
            System.out.println("Archivo assembler generado en: " + rutaSalida);
        } catch (IOException e) {
            System.err.println("Error al escribir el archivo assembler: " + e.getMessage());
        }
    }

    // Muestra el codigo assembler generado por consola
    public static void imprimirAssembler(String contenidoAsm) {
        System.out.println("\n----------------------------------------");
        System.out.println("         CODIGO ASSEMBLER (MASM32)      ");
        System.out.println("----------------------------------------");
        System.out.println(contenidoAsm);
    }
}
