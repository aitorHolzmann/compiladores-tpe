package src.compilador;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class GeneradorAssembler {

    private static final Map<String, String> mapaNombresAsm = new HashMap<>();
    private static int contadorStr = 0;
    private static StringBuilder codigoData;
    private static StringBuilder codigoCode;

    public static String obtenerNombreAsm(String claveTS) {
        if (claveTS == null) {
            return "";
        }
        if (mapaNombresAsm.containsKey(claveTS)) {
            return mapaNombresAsm.get(claveTS);
        }
        if (claveTS.startsWith("@")) {
            return claveTS;
        }

        String uso = TablaSimbolos.obtenerUso(claveTS);
        Object ocs = TablaSimbolos.obtenerAtributo(claveTS, "OCURRENCIAS");

        if (uso == null && ocs != null) {
            String tipo = TablaSimbolos.obtenerTipo(claveTS);
            String nombre;
            if ("CADENA".equals(tipo)) {
                nombre = "_str_" + contadorStr;
                contadorStr++;
            } else {
                String limpio = claveTS.replace("-", "neg_")
                                       .replace(".", "_")
                                       .replace("+", "pos_");
                nombre = "_cte_" + limpio;
            }
            mapaNombresAsm.put(claveTS, nombre);
            return nombre;
        }

        if (claveTS.contains(TablaSimbolos.SEPARADOR)) {
            String nombre = "_" + claveTS.replace(TablaSimbolos.SEPARADOR, "_");
            mapaNombresAsm.put(claveTS, nombre);
            return nombre;
        }

        String nombre = "_" + claveTS;
        mapaNombresAsm.put(claveTS, nombre);
        return nombre;
    }

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

    public static String obtenerNombreAux(String ambito, int indice) {
        if (TablaSimbolos.AMBITO_GLOBAL.equals(ambito)) {
            return "@aux" + indice;
        }
        return "@aux_" + ambito.replace(TablaSimbolos.SEPARADOR, "_") + "_" + indice;
    }

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

                if ("+".equals(op) || "-".equals(op) || "*".equals(op) || "/".equals(op) || "tos".equals(op) || "CALL".equals(op)) {
                    String nombreAux = obtenerNombreAux(ambito, i);
                    String tipoAux = tipo;
                    if ("tos".equals(op) && tipoAux == null) {
                        tipoAux = "SHORTINT";
                    }
                    if ("CALL".equals(op) && tipoAux == null) {
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

    private static boolean esAccesoPosicionalVariable(String op) {
        if (op == null || op.startsWith("[") || !op.contains("[") || !op.endsWith("]")) {
            return false;
        }
        int corchete = op.indexOf('[');
        String contenidoIdx = op.substring(corchete + 1, op.length() - 1);
        return contenidoIdx.contains(TablaSimbolos.SEPARADOR);
    }

    private static String obtenerTipoAccesoPosicional(String op) {
        int corchete = op.indexOf('[');
        String claveObj = op.substring(0, corchete);
        String nomClase = TablaSimbolos.obtenerTipo(claveObj);
        String claveClase = TablaSimbolos.AMBITO_GLOBAL + TablaSimbolos.SEPARADOR + nomClase;
        ArrayList<String> atrsTotales = TablaSimbolos.obtenerAtributosTotalesClase(claveClase);
        if (!atrsTotales.isEmpty()) {
            String tipoAtr = TablaSimbolos.obtenerTipo(atrsTotales.get(0));
            if (tipoAtr != null && !tipoAtr.isEmpty()) {
                return tipoAtr;
            }
        }
        return "SHORTINT";
    }

    // Resuelve lectura de operandos, incluyendo acceso posicional con indice variable
    private static String resolverOperandoLectura(String opRaw, String tipoEsperado, String ambito, String ambitoLimpio, int idxTerceto, int nroOp, ArrayList<Terceto> lista, StringBuilder sb) {
        if (!esAccesoPosicionalVariable(opRaw)) {
            return resolverOperandoAsm(opRaw, ambito, lista);
        }
        int corchete = opRaw.indexOf('[');
        String claveObj = opRaw.substring(0, corchete);
        String contenidoIdx = opRaw.substring(corchete + 1, opRaw.length() - 1);
        String nomClase = TablaSimbolos.obtenerTipo(claveObj);
        String claveClase = TablaSimbolos.AMBITO_GLOBAL + TablaSimbolos.SEPARADOR + nomClase;
        ArrayList<String> atrsTotales = TablaSimbolos.obtenerAtributosTotalesClase(claveClase);

        String tipoDest = (tipoEsperado != null && !tipoEsperado.isEmpty()) ? tipoEsperado : obtenerTipoAccesoPosicional(opRaw);
        String auxPos;
        if ("SINGLEF".equals(tipoDest)) {
            auxPos = (nroOp == 1) ? "@aux_pos_f1" : "@aux_pos_f2";
        } else {
            auxPos = (nroOp == 1) ? "@aux_pos_s1" : "@aux_pos_s2";
        }

        String varIdxAsm = resolverOperandoAsm(contenidoIdx, ambito, lista);
        for (int k = 0; k < atrsTotales.size(); k++) {
            String claveAtrAbs = atrsTotales.get(k);
            String tipoAtrK = TablaSimbolos.obtenerTipo(claveAtrAbs);
            if (tipoDest != null && tipoAtrK != null && !tipoDest.equals(tipoAtrK)) {
                continue;
            }
            String atrAsm = obtenerNombreAsm(claveObj + "_" + TablaSimbolos.obtenerNombre(claveAtrAbs));
            String etiquetaSkip = "Label_read_pos_" + ambitoLimpio + "_" + idxTerceto + "_" + nroOp + "_" + k;
            sb.append("    MOV AL, ").append(varIdxAsm).append("\n");
            sb.append("    CMP AL, ").append(k).append("\n");
            sb.append("    JNE ").append(etiquetaSkip).append("\n");
            if ("SHORTINT".equals(tipoAtrK)) {
                sb.append("    MOV AL, ").append(atrAsm).append("\n");
                sb.append("    MOV ").append(auxPos).append(", AL\n");
            } else if ("SINGLEF".equals(tipoAtrK)) {
                sb.append("    FLD ").append(atrAsm).append("\n");
                sb.append("    FSTP ").append(auxPos).append("\n");
            }
            sb.append(etiquetaSkip).append(":\n");
        }
        return auxPos;
    }

    // Resuelve acceso posicional obj[idx]
    private static String resolverAccesoPosicional(String op, String ambito, ArrayList<Terceto> lista) {
        if (op == null) {
            return "";
        }
        if (op.contains("[") && op.endsWith("]")) {
            int corchete = op.indexOf('[');
            String claveObj = op.substring(0, corchete);
            String contenidoIdx = op.substring(corchete + 1, op.length() - 1);

            String nomClase = TablaSimbolos.obtenerTipo(claveObj);
            String claveClase = TablaSimbolos.AMBITO_GLOBAL + TablaSimbolos.SEPARADOR + nomClase;
            ArrayList<String> atrsTotales = TablaSimbolos.obtenerAtributosTotalesClase(claveClase);

            // Si no tiene '@' es una constante (ej: 0$s); si tiene '@' es una variable (ej: main@a)
            if (!contenidoIdx.contains(TablaSimbolos.SEPARADOR)) {
                int idx = Integer.parseInt(contenidoIdx.replace("$s", "").trim());
                if (idx >= 0 && idx < atrsTotales.size()) {
                    String claveAtrAbs = atrsTotales.get(idx);
                    return claveObj + "_" + TablaSimbolos.obtenerNombre(claveAtrAbs);
                }
            }
        }
        return op;
    }

    public static String resolverOperandoAsm(String op, String ambito, ArrayList<Terceto> lista) {
        if (op == null) {
            return "";
        }
        String opResuelto = resolverAccesoPosicional(op, ambito, lista);
        if (opResuelto.startsWith("[") && opResuelto.endsWith("]")) {
            int idx = Integer.parseInt(opResuelto.substring(1, opResuelto.length() - 1));
            if (lista != null && idx >= 0 && idx < lista.size()) {
                Terceto ref = lista.get(idx);
                if (":=".equals(ref.getOperador())) {
                    return obtenerNombreAsm(resolverAccesoPosicional(ref.getOperando1(), ambito, lista));
                }
            }
            return obtenerNombreAux(ambito, idx);
        }
        return obtenerNombreAsm(opResuelto);
    }

    private static int extraerIndiceDestino(String op) {
        if (op == null) {
            return -1;
        }
        String limpio = op.replace("[", "").replace("]", "").trim();
        try {
            return Integer.parseInt(limpio);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static String obtenerTipoOperando(String op, ArrayList<Terceto> lista) {
        if (op == null) {
            return "SHORTINT";
        }
        if (op.startsWith("[") && op.endsWith("]")) {
            try {
                int idx = Integer.parseInt(op.substring(1, op.length() - 1));
                if (lista != null && idx >= 0 && idx < lista.size()) {
                    String tipo = lista.get(idx).getTipo();
                    if (tipo != null && !tipo.isEmpty()) {
                        return tipo;
                    }
                }
            } catch (NumberFormatException e) {
                // ignorar
            }
        }
        if (esAccesoPosicionalVariable(op)) {
            return obtenerTipoAccesoPosicional(op);
        }
        String tipo = TablaSimbolos.obtenerTipo(op);
        if (tipo != null && !tipo.isEmpty()) {
            return tipo;
        }
        if (op.endsWith("$s") || op.contains("$")) {
            return "SHORTINT";
        }
        if (op.contains(".") || op.contains("s") || op.contains("S") || op.contains("e") || op.contains("E")) {
            return "SINGLEF";
        }
        return "SHORTINT";
    }

    private static void generarCodigoAmbito(String ambito, ArrayList<Terceto> listaTercetos, StringBuilder sb) {
        if (listaTercetos == null) {
            return;
        }
        String ambitoLimpio = ambito.replace(TablaSimbolos.SEPARADOR, "_");
        HashSet<Integer> etiquetas = new HashSet<Integer>();
        for (int i = 0; i < listaTercetos.size(); i++) {
            Terceto t = listaTercetos.get(i);
            String op = t.getOperador();
            if ("BF".equals(op)) {
                int dest = extraerIndiceDestino(t.getOperando2());
                if (dest >= 0) {
                    etiquetas.add(dest);
                }
            } else if ("BI".equals(op)) {
                int dest = extraerIndiceDestino(t.getOperando1());
                if (dest >= 0) {
                    etiquetas.add(dest);
                }
            }
        }

        String ultimoOpComp = "";
        String ultimoTipoComp = "";

        for (int i = 0; i < listaTercetos.size(); i++) {
            if (etiquetas.contains(i)) {
                sb.append("Label_").append(ambitoLimpio).append("_").append(i).append(":\n");
            }

            Terceto t = listaTercetos.get(i);
            String op = t.getOperador();
            String tipo = t.getTipo();

            if ("+".equals(op)) {
                String op1 = resolverOperandoLectura(t.getOperando1(), tipo, ambito, ambitoLimpio, i, 1, listaTercetos, sb);
                String op2 = resolverOperandoLectura(t.getOperando2(), tipo, ambito, ambitoLimpio, i, 2, listaTercetos, sb);
                String aux = obtenerNombreAux(ambito, i);
                if ("SHORTINT".equals(tipo)) {
                    sb.append("    MOV AL, ").append(op1).append("\n");
                    sb.append("    ADD AL, ").append(op2).append("\n");
                    sb.append("    MOV ").append(aux).append(", AL\n");
                } else if ("SINGLEF".equals(tipo)) {
                    // Control overflow en suma de flotantes
                    sb.append("    FLD ").append(op1).append("\n");
                    sb.append("    FADD ").append(op2).append("\n");
                    sb.append("    FLD ST(0)\n");
                    sb.append("    FABS\n");
                    sb.append("    FCOMP @max_singlef\n");
                    sb.append("    FSTSW AX\n");
                    sb.append("    SAHF\n");
                    sb.append("    JA _err_overflow_float\n");
                    sb.append("    FSTP ").append(aux).append("\n");
                }
            } else if ("-".equals(op)) {
                String op1 = resolverOperandoLectura(t.getOperando1(), tipo, ambito, ambitoLimpio, i, 1, listaTercetos, sb);
                String op2 = resolverOperandoLectura(t.getOperando2(), tipo, ambito, ambitoLimpio, i, 2, listaTercetos, sb);
                String aux = obtenerNombreAux(ambito, i);
                if ("SHORTINT".equals(tipo)) {
                    sb.append("    MOV AL, ").append(op1).append("\n");
                    sb.append("    SUB AL, ").append(op2).append("\n");
                    sb.append("    MOV ").append(aux).append(", AL\n");
                } else if ("SINGLEF".equals(tipo)) {
                    sb.append("    FLD ").append(op1).append("\n");
                    sb.append("    FSUB ").append(op2).append("\n");
                    sb.append("    FSTP ").append(aux).append("\n");
                }
            } else if ("*".equals(op)) {
                String op1 = resolverOperandoLectura(t.getOperando1(), tipo, ambito, ambitoLimpio, i, 1, listaTercetos, sb);
                String op2 = resolverOperandoLectura(t.getOperando2(), tipo, ambito, ambitoLimpio, i, 2, listaTercetos, sb);
                String aux = obtenerNombreAux(ambito, i);
                if ("SHORTINT".equals(tipo)) {
                    // Control overflow en producto de enteros
                    sb.append("    MOV AL, ").append(op1).append("\n");
                    sb.append("    IMUL ").append(op2).append("\n");
                    sb.append("    JO _err_overflow_entero\n");
                    sb.append("    MOV ").append(aux).append(", AL\n");
                } else if ("SINGLEF".equals(tipo)) {
                    sb.append("    FLD ").append(op1).append("\n");
                    sb.append("    FMUL ").append(op2).append("\n");
                    sb.append("    FSTP ").append(aux).append("\n");
                }
            } else if ("/".equals(op)) {
                String op1 = resolverOperandoLectura(t.getOperando1(), tipo, ambito, ambitoLimpio, i, 1, listaTercetos, sb);
                String op2 = resolverOperandoLectura(t.getOperando2(), tipo, ambito, ambitoLimpio, i, 2, listaTercetos, sb);
                String aux = obtenerNombreAux(ambito, i);
                if ("SHORTINT".equals(tipo)) {
                    sb.append("    MOV AL, ").append(op1).append("\n");
                    sb.append("    CBW\n");
                    sb.append("    IDIV ").append(op2).append("\n");
                    sb.append("    MOV ").append(aux).append(", AL\n");
                } else if ("SINGLEF".equals(tipo)) {
                    sb.append("    FLD ").append(op1).append("\n");
                    sb.append("    FDIV ").append(op2).append("\n");
                    sb.append("    FSTP ").append(aux).append("\n");
                }
            } else if (":=".equals(op)) {
                String op1Raw = t.getOperando1();
                if (tipo == null) {
                    tipo = TablaSimbolos.obtenerTipo(op1Raw);
                }
                String op2 = resolverOperandoLectura(t.getOperando2(), tipo, ambito, ambitoLimpio, i, 2, listaTercetos, sb);

                if (op1Raw != null && op1Raw.contains("[") && op1Raw.endsWith("]")) {
                    int corchete = op1Raw.indexOf('[');
                    String claveObj = op1Raw.substring(0, corchete);
                    String contenidoIdx = op1Raw.substring(corchete + 1, op1Raw.length() - 1);
                    String nomClase = TablaSimbolos.obtenerTipo(claveObj);
                    String claveClase = TablaSimbolos.AMBITO_GLOBAL + TablaSimbolos.SEPARADOR + nomClase;
                    ArrayList<String> atrsTotales = TablaSimbolos.obtenerAtributosTotalesClase(claveClase);

                    // Si el indice no tiene '@' es constante (ej: 0$s); si tiene '@' es variable (ej: main@a)
                    if (!contenidoIdx.contains(TablaSimbolos.SEPARADOR)) {
                        int idxConst = Integer.parseInt(contenidoIdx.replace("$s", "").trim());
                        String claveAtrAbs = atrsTotales.get(idxConst);
                        String op1Asm = obtenerNombreAsm(claveObj + "_" + TablaSimbolos.obtenerNombre(claveAtrAbs));
                        if ("SHORTINT".equals(tipo)) {
                            sb.append("    MOV AL, ").append(op2).append("\n");
                            sb.append("    MOV ").append(op1Asm).append(", AL\n");
                        } else if ("SINGLEF".equals(tipo)) {
                            sb.append("    FLD ").append(op2).append("\n");
                            sb.append("    FSTP ").append(op1Asm).append("\n");
                        }
                    } else {
                        String varIdxAsm = resolverOperandoAsm(contenidoIdx, ambito, listaTercetos);
                        for (int k = 0; k < atrsTotales.size(); k++) {
                            String claveAtrAbs = atrsTotales.get(k);
                            String tipoAtrK = TablaSimbolos.obtenerTipo(claveAtrAbs);
                            if (tipo != null && tipoAtrK != null && !tipo.equals(tipoAtrK)) {
                                continue;
                            }
                            String op1Asm = obtenerNombreAsm(claveObj + "_" + TablaSimbolos.obtenerNombre(claveAtrAbs));
                            String etiquetaSkip = "Label_skip_pos_" + ambitoLimpio + "_" + i + "_" + k;
                            sb.append("    MOV AL, ").append(varIdxAsm).append("\n");
                            sb.append("    CMP AL, ").append(k).append("\n");
                            sb.append("    JNE ").append(etiquetaSkip).append("\n");
                            if ("SHORTINT".equals(tipoAtrK)) {
                                sb.append("    MOV AL, ").append(op2).append("\n");
                                sb.append("    MOV ").append(op1Asm).append(", AL\n");
                            } else if ("SINGLEF".equals(tipoAtrK)) {
                                sb.append("    FLD ").append(op2).append("\n");
                                sb.append("    FSTP ").append(op1Asm).append("\n");
                            }
                            sb.append(etiquetaSkip).append(":\n");
                        }
                    }
                } else {
                    String op1 = resolverOperandoAsm(op1Raw, ambito, listaTercetos);
                    if ("SHORTINT".equals(tipo)) {
                        sb.append("    MOV AL, ").append(op2).append("\n");
                        sb.append("    MOV ").append(op1).append(", AL\n");
                    } else if ("SINGLEF".equals(tipo)) {
                        sb.append("    FLD ").append(op2).append("\n");
                        sb.append("    FSTP ").append(op1).append("\n");
                    } else if (tipo != null) {
                        String claveClase = TablaSimbolos.AMBITO_GLOBAL + TablaSimbolos.SEPARADOR + tipo;
                        ArrayList<String> atrsTotales = TablaSimbolos.obtenerAtributosTotalesClase(claveClase);
                        for (int k = 0; k < atrsTotales.size(); k++) {
                            String claveAtrAbs = atrsTotales.get(k);
                            String nomAtr = TablaSimbolos.obtenerNombre(claveAtrAbs);
                            String tipoAtr = TablaSimbolos.obtenerTipo(claveAtrAbs);
                            String destAtr = obtenerNombreAsm(op1Raw + "_" + nomAtr);
                            String origAtr = obtenerNombreAsm(t.getOperando2() + "_" + nomAtr);
                            if ("SHORTINT".equals(tipoAtr)) {
                                sb.append("    MOV AL, ").append(origAtr).append("\n");
                                sb.append("    MOV ").append(destAtr).append(", AL\n");
                            } else if ("SINGLEF".equals(tipoAtr)) {
                                sb.append("    FLD ").append(origAtr).append("\n");
                                sb.append("    FSTP ").append(destAtr).append("\n");
                            }
                        }
                    }
                }
            } else if ("tos".equals(op)) {
                String op1 = resolverOperandoLectura(t.getOperando1(), "SINGLEF", ambito, ambitoLimpio, i, 1, listaTercetos, sb);
                String aux = obtenerNombreAux(ambito, i);
                // Control perdida de informacion en tos()
                sb.append("    FLD ").append(op1).append("\n");
                sb.append("    FCOMP @max_shortint_f\n");
                sb.append("    FSTSW AX\n");
                sb.append("    SAHF\n");
                sb.append("    JA _err_perdida_info\n");
                sb.append("    FLD ").append(op1).append("\n");
                sb.append("    FCOMP @min_shortint_f\n");
                sb.append("    FSTSW AX\n");
                sb.append("    SAHF\n");
                sb.append("    JB _err_perdida_info\n");
                sb.append("    FLD ").append(op1).append("\n");
                sb.append("    FISTP @aux_tos\n");
                sb.append("    FILD @aux_tos\n");
                sb.append("    FLD ").append(op1).append("\n");
                sb.append("    FCOMPP\n");
                sb.append("    FSTSW AX\n");
                sb.append("    SAHF\n");
                sb.append("    JNE _err_perdida_info\n");
                sb.append("    MOV AX, @aux_tos\n");
                sb.append("    MOV ").append(aux).append(", AL\n");
            } else if ("RET".equals(op)) {
                String op1Raw = t.getOperando1();
                if (tipo == null) {
                    if (esAccesoPosicionalVariable(op1Raw)) {
                        tipo = obtenerTipoAccesoPosicional(op1Raw);
                    } else {
                        tipo = TablaSimbolos.obtenerTipo(op1Raw);
                    }
                }
                String op1 = resolverOperandoLectura(op1Raw, tipo, ambito, ambitoLimpio, i, 1, listaTercetos, sb);
                String tipoSubrutina = TablaSimbolos.obtenerTipo(ambito);
                if (tipoSubrutina == null) {
                    tipoSubrutina = tipo;
                }
                if ("SHORTINT".equals(tipoSubrutina)) {
                    sb.append("    MOV AL, ").append(op1).append("\n");
                    sb.append("    MOV _ret_").append(ambitoLimpio).append(", AL\n");
                } else if ("SINGLEF".equals(tipoSubrutina)) {
                    if ("SHORTINT".equals(tipo)) {
                        sb.append("    MOV AL, ").append(op1).append("\n");
                        sb.append("    CBW\n");
                        sb.append("    MOV @aux_tos, AX\n");
                        sb.append("    FILD @aux_tos\n");
                        sb.append("    FSTP _ret_").append(ambitoLimpio).append("\n");
                    } else {
                        sb.append("    FLD ").append(op1).append("\n");
                        sb.append("    FSTP _ret_").append(ambitoLimpio).append("\n");
                    }
                }
                sb.append("    JMP _fin_").append(ambitoLimpio).append("\n");
            } else if ("CALL".equals(op)) {
                String claveSub = t.getOperando1();
                String claveObj = t.getOperando2();
                String subLimpio = claveSub.replace(TablaSimbolos.SEPARADOR, "_");

                if (claveObj != null && !"".equals(claveObj) && !"-".equals(claveObj)) {
                    String nomClase = TablaSimbolos.obtenerTipo(claveObj);
                    String claveClase = TablaSimbolos.AMBITO_GLOBAL + TablaSimbolos.SEPARADOR + nomClase;
                    ArrayList<String> atrsTotales = TablaSimbolos.obtenerAtributosTotalesClase(claveClase);
                    for (int k = 0; k < atrsTotales.size(); k++) {
                        String claveAtrAbs = atrsTotales.get(k);
                        String nomAtr = TablaSimbolos.obtenerNombre(claveAtrAbs);
                        String varObjAtr = obtenerNombreAsm(claveObj + "_" + nomAtr);
                        String varClaseAtr = obtenerNombreAsm(claveAtrAbs);
                        String tipoAtr = TablaSimbolos.obtenerTipo(claveAtrAbs);
                        if ("SHORTINT".equals(tipoAtr)) {
                            sb.append("    MOV AL, ").append(varObjAtr).append("\n");
                            sb.append("    MOV ").append(varClaseAtr).append(", AL\n");
                        } else if ("SINGLEF".equals(tipoAtr)) {
                            sb.append("    FLD ").append(varObjAtr).append("\n");
                            sb.append("    FSTP ").append(varClaseAtr).append("\n");
                        }
                    }
                }

                sb.append("    CALL _proc_").append(subLimpio).append("\n");

                if (claveObj != null && !"".equals(claveObj) && !"-".equals(claveObj)) {
                    String nomClase = TablaSimbolos.obtenerTipo(claveObj);
                    String claveClase = TablaSimbolos.AMBITO_GLOBAL + TablaSimbolos.SEPARADOR + nomClase;
                    ArrayList<String> atrsTotales = TablaSimbolos.obtenerAtributosTotalesClase(claveClase);
                    for (int k = 0; k < atrsTotales.size(); k++) {
                        String claveAtrAbs = atrsTotales.get(k);
                        String nomAtr = TablaSimbolos.obtenerNombre(claveAtrAbs);
                        String varObjAtr = obtenerNombreAsm(claveObj + "_" + nomAtr);
                        String varClaseAtr = obtenerNombreAsm(claveAtrAbs);
                        String tipoAtr = TablaSimbolos.obtenerTipo(claveAtrAbs);
                        if ("SHORTINT".equals(tipoAtr)) {
                            sb.append("    MOV AL, ").append(varClaseAtr).append("\n");
                            sb.append("    MOV ").append(varObjAtr).append(", AL\n");
                        } else if ("SINGLEF".equals(tipoAtr)) {
                            sb.append("    FLD ").append(varClaseAtr).append("\n");
                            sb.append("    FSTP ").append(varObjAtr).append("\n");
                        }
                    }
                }

                String aux = obtenerNombreAux(ambito, i);
                if ("SHORTINT".equals(tipo)) {
                    sb.append("    MOV AL, _ret_").append(subLimpio).append("\n");
                    sb.append("    MOV ").append(aux).append(", AL\n");
                } else if ("SINGLEF".equals(tipo)) {
                    sb.append("    FLD _ret_").append(subLimpio).append("\n");
                    sb.append("    FSTP ").append(aux).append("\n");
                }
            } else if ("POUT".equals(op)) {
                String op1Raw = t.getOperando1();
                if (tipo == null) {
                    if (op1Raw != null && op1Raw.startsWith("[") && op1Raw.endsWith("]")) {
                        int idx = Integer.parseInt(op1Raw.substring(1, op1Raw.length() - 1));
                        tipo = listaTercetos.get(idx).getTipo();
                    } else if (esAccesoPosicionalVariable(op1Raw)) {
                        tipo = obtenerTipoAccesoPosicional(op1Raw);
                    } else {
                        tipo = TablaSimbolos.obtenerTipo(op1Raw);
                    }
                }
                String opAsm = resolverOperandoLectura(op1Raw, tipo, ambito, ambitoLimpio, i, 1, listaTercetos, sb);
                if ("CADENA".equals(tipo)) {
                    sb.append("    invoke crt_printf, cfm$(\"%s\\n\"), OFFSET ").append(opAsm).append("\n");
                } else if ("SHORTINT".equals(tipo)) {
                    sb.append("    MOV AL, ").append(opAsm).append("\n");
                    sb.append("    MOVSX AX, AL\n");
                    sb.append("    MOV @aux_print_short, AX\n");
                    sb.append("    invoke crt_printf, cfm$(\"%hd\\n\"), @aux_print_short\n");
                } else if ("SINGLEF".equals(tipo)) {
                    sb.append("    FLD ").append(opAsm).append("\n");
                    sb.append("    FSTP @aux_print_float\n");
                    sb.append("    invoke crt_printf, cfm$(\"%Lf\\n\"), @aux_print_float\n");
                }
            } else if (">".equals(op) || "<".equals(op) || ">=".equals(op) || "<=".equals(op) || "==".equals(op) || "!=".equals(op)) {
                ultimoOpComp = op;
                if (tipo != null && !tipo.isEmpty()) {
                    ultimoTipoComp = tipo;
                } else {
                    String tipo1 = obtenerTipoOperando(t.getOperando1(), listaTercetos);
                    String tipo2 = obtenerTipoOperando(t.getOperando2(), listaTercetos);
                    if ("SINGLEF".equals(tipo1) || "SINGLEF".equals(tipo2)) {
                        ultimoTipoComp = "SINGLEF";
                    } else {
                        ultimoTipoComp = "SHORTINT";
                    }
                }
                String op1 = resolverOperandoLectura(t.getOperando1(), ultimoTipoComp, ambito, ambitoLimpio, i, 1, listaTercetos, sb);
                String op2 = resolverOperandoLectura(t.getOperando2(), ultimoTipoComp, ambito, ambitoLimpio, i, 2, listaTercetos, sb);
                if ("SHORTINT".equals(ultimoTipoComp)) {
                    sb.append("    MOV AL, ").append(op1).append("\n");
                    sb.append("    CMP AL, ").append(op2).append("\n");
                } else if ("SINGLEF".equals(ultimoTipoComp)) {
                    sb.append("    FLD ").append(op1).append("\n");
                    sb.append("    FCOMP ").append(op2).append("\n");
                    sb.append("    FSTSW AX\n");
                    sb.append("    SAHF\n");
                }
            } else if ("BF".equals(op)) {
                int dest = extraerIndiceDestino(t.getOperando2());
                String etiqueta = "Label_" + ambitoLimpio + "_" + dest;
                String salto = "";
                if ("SHORTINT".equals(ultimoTipoComp)) {
                    if (">".equals(ultimoOpComp)) {
                        salto = "JLE";
                    } else if (">=".equals(ultimoOpComp)) {
                        salto = "JL";
                    } else if ("<".equals(ultimoOpComp)) {
                        salto = "JGE";
                    } else if ("<=".equals(ultimoOpComp)) {
                        salto = "JG";
                    } else if ("==".equals(ultimoOpComp)) {
                        salto = "JNE";
                    } else if ("!=".equals(ultimoOpComp)) {
                        salto = "JE";
                    }
                } else if ("SINGLEF".equals(ultimoTipoComp)) {
                    if (">".equals(ultimoOpComp)) {
                        salto = "JBE";
                    } else if (">=".equals(ultimoOpComp)) {
                        salto = "JB";
                    } else if ("<".equals(ultimoOpComp)) {
                        salto = "JAE";
                    } else if ("<=".equals(ultimoOpComp)) {
                        salto = "JA";
                    } else if ("==".equals(ultimoOpComp)) {
                        salto = "JNE";
                    } else if ("!=".equals(ultimoOpComp)) {
                        salto = "JE";
                    }
                }
                if (!salto.isEmpty()) {
                    sb.append("    ").append(salto).append(" ").append(etiqueta).append("\n");
                }
            } else if ("BI".equals(op)) {
                int dest = extraerIndiceDestino(t.getOperando1());
                String etiqueta = "Label_" + ambitoLimpio + "_" + dest;
                sb.append("    JMP ").append(etiqueta).append("\n");
            }
        }

        if (etiquetas.contains(listaTercetos.size())) {
            sb.append("Label_").append(ambitoLimpio).append("_").append(listaTercetos.size()).append(":\n");
        }
    }

    public static String generarAssembler() {
        mapaNombresAsm.clear();
        contadorStr = 0;
        codigoData = new StringBuilder();
        codigoCode = new StringBuilder();

        Map<String, ArrayList<Terceto>> tercetosPorAmbito = GeneradorTerceto.obtenerTercetosPorAmbito();
        registrarAuxiliaresYConstantes(tercetosPorAmbito);

        StringBuilder encabezado = new StringBuilder();
        encabezado.append("include \\masm32\\include\\masm32rt.inc\n\n");

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
                if (uso.equals("objeto") || (uso.equals("parametro") && !"SHORTINT".equals(tipo) && !"SINGLEF".equals(tipo))) {
                    String claveClase = TablaSimbolos.AMBITO_GLOBAL + TablaSimbolos.SEPARADOR + tipo;
                    ArrayList<String> atrsObj = TablaSimbolos.obtenerAtributosTotalesClase(claveClase);
                    for (int k = 0; k < atrsObj.size(); k++) {
                        String claveAtrAbs = atrsObj.get(k);
                        String nomAtr = TablaSimbolos.obtenerNombre(claveAtrAbs);
                        String tipoAtr = TablaSimbolos.obtenerTipo(claveAtrAbs);
                        String nombreAsmAtr = obtenerNombreAsm(clave + "_" + nomAtr);
                        if ("SHORTINT".equals(tipoAtr)) {
                            codigoData.append("    ").append(nombreAsmAtr).append(" DB ?\n");
                        } else if ("SINGLEF".equals(tipoAtr)) {
                            codigoData.append("    ").append(nombreAsmAtr).append(" DD ?\n");
                        }
                    }
                } else if (uso.equals("variable") || uso.equals("parametro") || uso.equals("auxiliar") || uso.equals("atributo")) {
                    String nombreAsm = obtenerNombreAsm(clave);
                    if (TablaSimbolos.esComptime(clave)) {
                        codigoData.append("    ; variable comptime omitida: ").append(nombreAsm).append("\n");
                    } else if ("SHORTINT".equals(tipo)) {
                        codigoData.append("    ").append(nombreAsm).append(" DB ?\n");
                    } else if ("SINGLEF".equals(tipo)) {
                        codigoData.append("    ").append(nombreAsm).append(" DD ?\n");
                    }
                } else if (uso.equals("funcion") || uso.equals("metodo")) {
                    String ambitoLimpio = clave.replace(TablaSimbolos.SEPARADOR, "_");
                    if ("SHORTINT".equals(tipo)) {
                        codigoData.append("    _ret_").append(ambitoLimpio).append(" DB ?\n");
                    } else if ("SINGLEF".equals(tipo)) {
                        codigoData.append("    _ret_").append(ambitoLimpio).append(" DD ?\n");
                    }
                    // Bandera de activacion para control de recursion
                    codigoData.append("    @activa_").append(ambitoLimpio).append(" DB 0\n");
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
        codigoData.append("    @aux_tos DW ?\n");
        codigoData.append("    @aux_pos_s1 DB ?\n");
        codigoData.append("    @aux_pos_s2 DB ?\n");
        codigoData.append("    @aux_pos_f1 DD ?\n");
        codigoData.append("    @aux_pos_f2 DD ?\n");
        codigoData.append("    @max_singlef DD 3.40282347E+38\n");
        codigoData.append("    @min_shortint_f DD -128.0\n");
        codigoData.append("    @max_shortint_f DD 127.0\n");
        codigoData.append("    _msg_err_ovf_float DB \"Error en tiempo de ejecucion: Overflow en suma de punto flotante (SINGLEF)\", 0\n");
        codigoData.append("    _msg_err_ovf_entero DB \"Error en tiempo de ejecucion: Overflow en producto de enteros (SHORTINT)\", 0\n");
        codigoData.append("    _msg_err_perdida_tos DB \"Error en tiempo de ejecucion: Perdida de informacion en conversion tos()\", 0\n");
        codigoData.append("    _msg_err_recursion DB \"Error en tiempo de ejecucion: Recursion mutua o directa no permitida en funciones\", 0\n\n");

        codigoCode.append(".code\n");

        for (Map.Entry<String, ArrayList<Terceto>> entry : tercetosPorAmbito.entrySet()) {
            String ambito = entry.getKey();
            if (!TablaSimbolos.AMBITO_GLOBAL.equals(ambito)) {
                String ambitoLimpio = ambito.replace(TablaSimbolos.SEPARADOR, "_");
                codigoCode.append("_proc_").append(ambitoLimpio).append(":\n");
                codigoCode.append("    PUSH EBP\n");
                codigoCode.append("    MOV EBP, ESP\n");
                // Control de recursion en invocaciones
                codigoCode.append("    CMP @activa_").append(ambitoLimpio).append(", 1\n");
                codigoCode.append("    JE _err_recursion\n");
                codigoCode.append("    MOV @activa_").append(ambitoLimpio).append(", 1\n");
                generarCodigoAmbito(ambito, entry.getValue(), codigoCode);
                codigoCode.append("_fin_").append(ambitoLimpio).append(":\n");
                codigoCode.append("    MOV @activa_").append(ambitoLimpio).append(", 0\n");
                codigoCode.append("    MOV ESP, EBP\n");
                codigoCode.append("    POP EBP\n");
                codigoCode.append("    RET\n\n");
            }
        }

        codigoCode.append("start:\n");
        ArrayList<Terceto> tercetosMain = tercetosPorAmbito.get(TablaSimbolos.AMBITO_GLOBAL);
        if (tercetosMain != null) {
            generarCodigoAmbito(TablaSimbolos.AMBITO_GLOBAL, tercetosMain, codigoCode);
        }

        codigoCode.append("    invoke ExitProcess, 0\n\n");
        codigoCode.append("_err_overflow_float:\n");
        codigoCode.append("    invoke crt_printf, cfm$(\"%s\\n\"), OFFSET _msg_err_ovf_float\n");
        codigoCode.append("    invoke ExitProcess, 1\n\n");
        codigoCode.append("_err_overflow_entero:\n");
        codigoCode.append("    invoke crt_printf, cfm$(\"%s\\n\"), OFFSET _msg_err_ovf_entero\n");
        codigoCode.append("    invoke ExitProcess, 1\n\n");
        codigoCode.append("_err_perdida_info:\n");
        codigoCode.append("    invoke crt_printf, cfm$(\"%s\\n\"), OFFSET _msg_err_perdida_tos\n");
        codigoCode.append("    invoke ExitProcess, 1\n\n");
        codigoCode.append("_err_recursion:\n");
        codigoCode.append("    invoke crt_printf, cfm$(\"%s\\n\"), OFFSET _msg_err_recursion\n");
        codigoCode.append("    invoke ExitProcess, 1\n\n");
        codigoCode.append("end start\n");

        return encabezado.toString() + codigoData.toString() + codigoCode.toString();
    }

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

    public static void imprimirAssembler(String contenidoAsm) {
        System.out.println("\n----------------------------------------");
        System.out.println("         CODIGO ASSEMBLER (MASM32)      ");
        System.out.println("----------------------------------------");
        System.out.println(contenidoAsm);
    }
}
