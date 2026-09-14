//### This file created by BYACC 1.8(/Java extension  1.15)
//### Java capabilities added 7 Jan 97, Bob Jamison
//### Updated : 27 Nov 97  -- Bob Jamison, Joe Nieten
//###           01 Jan 98  -- Bob Jamison -- fixed generic semantic constructor
//###           01 Jun 99  -- Bob Jamison -- added Runnable support
//###           06 Aug 00  -- Bob Jamison -- made state variables class-global
//###           03 Jan 01  -- Bob Jamison -- improved flags, tracing
//###           16 May 01  -- Bob Jamison -- added custom stack sizing
//###           04 Mar 02  -- Yuval Oren  -- improved java performance, added options
//###           14 Mar 02  -- Tomas Hurka -- -d support, static initializer workaround
//### Please send bug reports to tom@hukatronic.cz
//### static char yysccsid[] = "@(#)yaccpar	1.8 (Berkeley) 01/20/90";



package src.compilador;



//#line 2 "grupo20.y"
import src.compilador.*;
//#line 19 "Parser.java"




public class Parser
{

boolean yydebug;        //do I want debug output?
int yynerrs;            //number of errors so far
int yyerrflag;          //was there an error?
int yychar;             //the current working character

//########## MESSAGES ##########
//###############################################################
// method: debug
//###############################################################
void debug(String msg)
{
  if (yydebug)
    System.out.println(msg);
}

//########## STATE STACK ##########
final static int YYSTACKSIZE = 500;  //maximum stack size
int statestk[] = new int[YYSTACKSIZE]; //state stack
int stateptr;
int stateptrmax;                     //highest index of stackptr
int statemax;                        //state when highest index reached
//###############################################################
// methods: state stack push,pop,drop,peek
//###############################################################
final void state_push(int state)
{
  try {
		stateptr++;
		statestk[stateptr]=state;
	 }
	 catch (ArrayIndexOutOfBoundsException e) {
     int oldsize = statestk.length;
     int newsize = oldsize * 2;
     int[] newstack = new int[newsize];
     System.arraycopy(statestk,0,newstack,0,oldsize);
     statestk = newstack;
     statestk[stateptr]=state;
  }
}
final int state_pop()
{
  return statestk[stateptr--];
}
final void state_drop(int cnt)
{
  stateptr -= cnt; 
}
final int state_peek(int relative)
{
  return statestk[stateptr-relative];
}
//###############################################################
// method: init_stacks : allocate and prepare stacks
//###############################################################
final boolean init_stacks()
{
  stateptr = -1;
  val_init();
  return true;
}
//###############################################################
// method: dump_stacks : show n levels of the stacks
//###############################################################
void dump_stacks(int count)
{
int i;
  System.out.println("=index==state====value=     s:"+stateptr+"  v:"+valptr);
  for (i=0;i<count;i++)
    System.out.println(" "+i+"    "+statestk[i]+"      "+valstk[i]);
  System.out.println("======================");
}


//########## SEMANTIC VALUES ##########
//public class ParserVal is defined in ParserVal.java


String   yytext;//user variable to return contextual strings
ParserVal yyval; //used to return semantic vals from action routines
ParserVal yylval;//the 'lval' (result) I got from yylex()
ParserVal valstk[];
int valptr;
//###############################################################
// methods: value stack push,pop,drop,peek.
//###############################################################
void val_init()
{
  valstk=new ParserVal[YYSTACKSIZE];
  yyval=new ParserVal();
  yylval=new ParserVal();
  valptr=-1;
}
void val_push(ParserVal val)
{
  if (valptr>=YYSTACKSIZE)
    return;
  valstk[++valptr]=val;
}
ParserVal val_pop()
{
  if (valptr<0)
    return new ParserVal();
  return valstk[valptr--];
}
void val_drop(int cnt)
{
int ptr;
  ptr=valptr-cnt;
  if (ptr<0)
    return;
  valptr = ptr;
}
ParserVal val_peek(int relative)
{
int ptr;
  ptr=valptr-relative;
  if (ptr<0)
    return new ParserVal();
  return valstk[ptr];
}
final ParserVal dup_yyval(ParserVal val)
{
  ParserVal dup = new ParserVal();
  dup.ival = val.ival;
  dup.dval = val.dval;
  dup.sval = val.sval;
  dup.obj = val.obj;
  return dup;
}
//#### end semantic value section ####
public final static short IDENTIFICADOR=257;
public final static short CONSTANTE=258;
public final static short CADENA=259;
public final static short IF=260;
public final static short ELSE=261;
public final static short END_IF=262;
public final static short BEGIN=263;
public final static short END=264;
public final static short POUT=265;
public final static short RET=266;
public final static short CLASS=267;
public final static short FUNCTION=268;
public final static short SHORTINT=269;
public final static short SINGLEF=270;
public final static short REPEAT=271;
public final static short UNTIL=272;
public final static short AUTO=273;
public final static short COMPTIME=274;
public final static short IMPORT=275;
public final static short FROM=276;
public final static short EXPORT=277;
public final static short TO=278;
public final static short EXTENDS=279;
public final static short TOS=280;
public final static short ASIGNAR=281;
public final static short MAYORIGUAL=282;
public final static short MENORIGUAL=283;
public final static short IGUALIGUAL=284;
public final static short DISTINTO=285;
public final static short YYERRCODE=256;
final static short yylhs[] = {                           -1,
    0,    1,    1,    3,    3,    3,    3,    3,    9,    9,
    4,   10,   10,    8,    5,    5,   11,   11,   12,    6,
   13,   13,   14,   14,   15,   15,   15,   16,   16,   17,
   17,    7,    2,    2,   18,   18,   18,   18,   18,   18,
   25,   26,   26,   19,   19,   19,   28,   29,   20,   20,
   30,   30,   21,   21,   31,   32,   32,   32,   32,   32,
   32,   22,   22,   23,   24,   27,   27,   27,   33,   33,
   33,   34,   34,   34,   34,   34,   34,   34,
};
final static short yylen[] = {                            2,
    5,    0,    2,    1,    1,    1,    1,    1,    1,    1,
    3,    1,    3,    4,   11,   10,    1,    3,    2,    7,
    0,    3,    0,    2,    4,   10,    3,    0,    3,    0,
    4,    3,    0,    2,    2,    2,    1,    2,    2,    1,
    3,    1,    1,    3,    3,    3,    3,    4,    4,    6,
    1,    3,    9,    7,    3,    1,    1,    1,    1,    1,
    1,    4,    4,    4,    7,    1,    3,    3,    1,    3,
    3,    1,    1,    1,    1,    1,    4,    5,
};
final static short yydefred[] = {                         0,
    2,    0,    0,    0,   33,    0,    9,   10,    0,    0,
    3,    4,    5,    6,    7,    8,    0,   12,    0,    0,
    0,    0,    0,    0,    0,   32,    0,    0,    0,    1,
    0,    0,    0,   34,    0,    0,   37,    0,    0,   40,
    0,    0,    0,    0,    0,    0,    0,   11,   13,    0,
    0,    0,    0,    0,    0,    0,   33,   42,   43,    0,
   35,   36,   38,   39,    0,    0,    0,   23,    0,   14,
    0,    0,   73,    0,   74,    0,   75,   76,    0,   69,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,   17,    0,    0,
    0,    0,    0,    0,    0,    0,   49,    0,   48,   56,
   57,   58,   59,   60,   61,    0,    0,   63,   62,   64,
   41,    0,    0,    0,    0,   24,   19,    0,    0,    2,
    0,    0,    0,    0,   70,   71,    0,    0,    0,    0,
    0,   20,    0,    0,   18,   33,    0,    0,   77,   50,
    0,    0,    0,   27,    0,    0,    0,    0,   33,   78,
    0,   54,   65,    0,    0,   25,    0,    0,    0,    0,
    0,   16,    0,   53,   33,   15,    0,    0,    0,    0,
   26,    0,    0,   31,
};
final static short yydgoto[] = {                          2,
    3,   20,   11,   12,   13,   14,   15,   16,   96,   19,
   97,   98,   44,   95,  126,  157,  181,   34,   35,   75,
   37,   38,   39,   40,   59,   60,   81,   77,   78,   82,
   86,  116,   79,   80,
};
final static short yysindex[] = {                      -196,
    0,    0, -174, -192,    0, -190,    0,    0, -189, -100,
    0,    0,    0,    0,    0,    0, -132,    0,   47,  -99,
 -162, -172, -192, -140,   53,    0, -126,   -3,   95,    0,
  110,  154,  -56,    0,  153,  157,    0,  172,  175,    0,
 -107,  -81,  -40,  -26,  198,   65,  199,    0,    0, -188,
 -188,  -12, -188, -188, -177, -188,    0,    0,    0,  -25,
    0,    0,    0,    0, -188, -188, -192,    0, -100,    0,
 -100,   17,    0,  206,    0,  160,    0,    0,   79,    0,
  160,   60,  212,   23,  -16,  216,  217,  106,  119,  -46,
  219,  160,  160,  221, -159,    3,  115,    0,  127,  230,
 -188, -188, -188, -188, -188, -188,    0, -188,    0,    0,
    0,    0,    0,    0,    0, -188,  -56,    0,    0,    0,
    0, -188,  213, -192,   14,    0,    0, -100,   18,    0,
 -188,  134,   79,   79,    0,    0,  160,  137,  160,  -39,
  241,    0,   70,    2,    0,    0, -129,  145,    0,    0,
  -56,  224,  229,    0,   11, -100,  233,  -36,    0,    0,
   35,    0,    0, -192,  141,    0,  234,   20,  239,  221,
   36,    0,  243,    0,    0,    0,   30,  244,   27,   28,
    0, -192,   74,    0,
};
final static short yyrindex[] = {                         0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
   42,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,  -34,    0,    0,    0,  248,    0,    0,  -29,    0,
  148,    0,  -41,    0,    0,    0,    0,    0,    0,    0,
    0,  249,  250,   48,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,  -21,   -9,    0,    0,  155,    0,  259,    0,
    0,    0,    0,  251,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,  253,
    0,    0,    0,    0,    0,    0,    0,    0, -116,    0,
    0,    0,    0,    0,
};
final static short yygindex[] = {                         0,
  183,  -52,    0,    0,    0,    0,    0,    0,   37,   31,
  -54,  186,    0,    0,    0,    0,    0,   22,    0,   25,
    0,    0,    0,    0,    0,  -61,   21,   29,   40,  207,
  194,    0,  124,  128,
};
final static int YYTABLESIZE=316;
static short yytable[];
static { yytable();}
static void yytable(){
yytable = new short[]{                         47,
   47,   47,   47,   47,   90,   47,   72,   72,   72,   72,
   72,   66,   72,   66,   66,   66,   99,   47,   47,   67,
   47,   67,   67,   67,   72,   72,  102,   72,  103,   66,
   66,   68,   66,   68,   68,   68,   51,   67,   67,   17,
   67,  156,   52,  115,   36,  114,   23,   25,   41,   68,
   68,   47,   68,   46,   58,  140,   51,   36,   72,   42,
    1,   41,   52,   66,   18,  102,   21,  103,   72,   73,
   76,   67,   42,   84,   85,   88,   89,  100,   22,   72,
   73,   87,    4,   68,   45,   92,   93,   53,    5,  161,
   27,   74,    6,  158,    7,    8,   27,   94,    9,   10,
  107,  165,   74,  106,  123,   26,  168,   53,   27,    7,
    8,   48,   43,   27,   36,  109,   47,   27,   41,  124,
  104,  132,  177,   70,   18,  105,  137,    4,  154,   42,
   49,  125,  184,  159,   54,   24,  139,    6,   58,    7,
    8,   36,   85,    9,   10,   41,  119,   30,  102,   55,
  103,  148,   30,   30,  143,  129,   42,   28,  128,  120,
   29,  102,   30,  103,   30,   31,   32,  130,    7,    8,
  128,   33,   58,   65,  149,   36,  102,  150,  103,   41,
  106,  171,   36,   17,  128,  160,   41,  102,   51,  103,
   42,   51,   36,   56,  170,   52,   41,   42,   52,   66,
   28,   36,  102,   29,  103,   41,   57,   42,   31,   32,
   28,   61,  183,   29,   33,   62,   42,  121,   31,   32,
   28,  151,  152,   29,   33,  133,  134,  167,   31,   32,
   63,  135,  136,   64,   33,   67,   68,   69,   71,   47,
   47,   47,   47,   47,   83,  101,   91,   72,   72,   72,
   72,  108,   66,   66,   66,   66,  117,  118,  122,  127,
   67,   67,   67,   67,   27,  110,  111,  112,  113,  131,
  144,  142,   68,   68,   68,   68,   28,   50,  155,   29,
  146,  153,  162,  173,   31,   32,   28,  163,  164,   29,
   33,  166,  172,  178,   31,   32,  169,  174,  175,   55,
   33,  176,  179,  180,   21,  182,   44,   45,   46,   28,
   22,   29,  147,  145,  138,  141,
};
}
static short yycheck[];
static { yycheck(); }
static void yycheck() {
yycheck = new short[] {                         41,
   42,   43,   44,   45,   57,   47,   41,   42,   43,   44,
   45,   41,   47,   43,   44,   45,   71,   59,   60,   41,
   62,   43,   44,   45,   59,   60,   43,   62,   45,   59,
   60,   41,   62,   43,   44,   45,   40,   59,   60,    3,
   62,   40,   46,   60,   20,   62,   10,   17,   20,   59,
   60,   93,   62,   23,   33,  117,   40,   33,   93,   20,
  257,   33,   46,   93,  257,   43,  257,   45,  257,  258,
   50,   93,   33,   53,   54,   55,   56,   61,  268,  257,
  258,  259,  257,   93,  257,   65,   66,   91,  263,  151,
   44,  280,  267,  146,  269,  270,   44,   67,  273,  274,
   41,  156,  280,   44,  264,   59,  159,   91,   44,  269,
  270,   59,  275,   44,   90,   93,  257,   44,   90,  279,
   42,  101,  175,   59,  257,   47,  106,  257,   59,   90,
  257,   95,   59,  263,   40,  268,  116,  267,  117,  269,
  270,  117,  122,  273,  274,  117,   41,  264,   43,   40,
   45,  131,  269,  270,  124,   41,  117,  257,   44,   41,
  260,   43,  279,   45,  264,  265,  266,   41,  269,  270,
   44,  271,  151,  281,   41,  151,   43,   41,   45,  151,
   44,   41,  158,  147,   44,   41,  158,   43,   41,   45,
  151,   44,  168,   40,  164,   41,  168,  158,   44,  281,
  257,  177,   43,  260,   45,  177,  263,  168,  265,  266,
  257,   59,  182,  260,  271,   59,  177,  264,  265,  266,
  257,  261,  262,  260,  271,  102,  103,  264,  265,  266,
   59,  104,  105,   59,  271,  276,  263,   40,   40,  281,
  282,  283,  284,  285,  257,   40,  272,  282,  283,  284,
  285,   40,  282,  283,  284,  285,   41,   41,   40,  257,
  282,  283,  284,  285,   44,  282,  283,  284,  285,   40,
  257,   59,  282,  283,  284,  285,  257,  281,  277,  260,
  263,   41,   59,  264,  265,  266,  257,   59,  278,  260,
  271,   59,   59,  264,  265,  266,  262,   59,  263,   41,
  271,   59,   59,  277,  263,  278,   59,   59,   59,   59,
  263,   59,  130,  128,  108,  122,
};
}
final static short YYFINAL=2;
final static short YYMAXTOKEN=285;
final static String yyname[] = {
"end-of-file",null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,"'('","')'","'*'","'+'","','",
"'-'","'.'","'/'",null,null,null,null,null,null,null,null,null,null,null,"';'",
"'<'","'='","'>'",null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
"'['",null,"']'",null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,"IDENTIFICADOR","CONSTANTE","CADENA","IF",
"ELSE","END_IF","BEGIN","END","POUT","RET","CLASS","FUNCTION","SHORTINT",
"SINGLEF","REPEAT","UNTIL","AUTO","COMPTIME","IMPORT","FROM","EXPORT","TO",
"EXTENDS","TOS","ASIGNAR","MAYORIGUAL","MENORIGUAL","IGUALIGUAL","DISTINTO",
};
final static String yyrule[] = {
"$accept : programa",
"programa : IDENTIFICADOR sentencias_declarativas BEGIN sentencias_ejecutables END",
"sentencias_declarativas :",
"sentencias_declarativas : sentencias_declarativas sentencia_declarativa",
"sentencia_declarativa : declaracion_variables",
"sentencia_declarativa : declaracion_funcion",
"sentencia_declarativa : declaracion_clase",
"sentencia_declarativa : declaracion_objeto",
"sentencia_declarativa : declaracion_comptime",
"tipo : SHORTINT",
"tipo : SINGLEF",
"declaracion_variables : tipo lista_identificadores ';'",
"lista_identificadores : IDENTIFICADOR",
"lista_identificadores : lista_identificadores ',' IDENTIFICADOR",
"declaracion_comptime : COMPTIME tipo lista_identificadores ';'",
"declaracion_funcion : tipo FUNCTION IDENTIFICADOR '(' parametros_formales ')' sentencias_declarativas BEGIN sentencias_ejecutables END ';'",
"declaracion_funcion : AUTO FUNCTION IDENTIFICADOR '(' parametros_formales ')' BEGIN sentencias_ejecutables END ';'",
"parametros_formales : parametro",
"parametros_formales : parametros_formales ',' parametro",
"parametro : tipo IDENTIFICADOR",
"declaracion_clase : CLASS IDENTIFICADOR clausula_import BEGIN miembros_clase END ';'",
"clausula_import :",
"clausula_import : IMPORT FROM lista_identificadores",
"miembros_clase :",
"miembros_clase : miembros_clase miembro_clase",
"miembro_clase : tipo IDENTIFICADOR clausula_export ';'",
"miembro_clase : tipo IDENTIFICADOR '(' parametros_formales ')' BEGIN sentencias_ejecutables END ';' clausula_export_opt",
"miembro_clase : EXTENDS lista_identificadores ';'",
"clausula_export :",
"clausula_export : EXPORT TO lista_identificadores",
"clausula_export_opt :",
"clausula_export_opt : EXPORT TO lista_identificadores ';'",
"declaracion_objeto : IDENTIFICADOR lista_identificadores ';'",
"sentencias_ejecutables :",
"sentencias_ejecutables : sentencias_ejecutables sentencia_ejecutable",
"sentencia_ejecutable : asignacion ';'",
"sentencia_ejecutable : invocacion ';'",
"sentencia_ejecutable : sentencia_if",
"sentencia_ejecutable : sentencia_pout ';'",
"sentencia_ejecutable : sentencia_ret ';'",
"sentencia_ejecutable : sentencia_repeat_until",
"bloque : BEGIN sentencias_ejecutables END",
"bloque_o_sentencia : sentencia_ejecutable",
"bloque_o_sentencia : bloque",
"asignacion : IDENTIFICADOR ASIGNAR expresion",
"asignacion : acceso_atributo ASIGNAR expresion",
"asignacion : acceso_posicional ASIGNAR expresion",
"acceso_atributo : IDENTIFICADOR '.' IDENTIFICADOR",
"acceso_posicional : IDENTIFICADOR '[' expresion ']'",
"invocacion : IDENTIFICADOR '(' lista_parametros_reales ')'",
"invocacion : IDENTIFICADOR '.' IDENTIFICADOR '(' lista_parametros_reales ')'",
"lista_parametros_reales : expresion",
"lista_parametros_reales : lista_parametros_reales ',' expresion",
"sentencia_if : IF '(' condicion ')' bloque_o_sentencia ELSE bloque_o_sentencia END_IF ';'",
"sentencia_if : IF '(' condicion ')' bloque_o_sentencia END_IF ';'",
"condicion : expresion comparador expresion",
"comparador : MAYORIGUAL",
"comparador : MENORIGUAL",
"comparador : IGUALIGUAL",
"comparador : DISTINTO",
"comparador : '>'",
"comparador : '<'",
"sentencia_pout : POUT '(' expresion ')'",
"sentencia_pout : POUT '(' CADENA ')'",
"sentencia_ret : RET '(' expresion ')'",
"sentencia_repeat_until : REPEAT bloque_o_sentencia UNTIL '(' condicion ')' ';'",
"expresion : termino",
"expresion : expresion '+' termino",
"expresion : expresion '-' termino",
"termino : factor",
"termino : termino '*' factor",
"termino : termino '/' factor",
"factor : IDENTIFICADOR",
"factor : CONSTANTE",
"factor : invocacion",
"factor : acceso_atributo",
"factor : acceso_posicional",
"factor : TOS '(' expresion ')'",
"factor : IDENTIFICADOR '=' '(' expresion ')'",
};

//#line 242 "grupo20.y"

int yylex() {
    int token = AnalizadorLexico.yylex();
    if (token == IDENTIFICADOR || token == CONSTANTE || token == CADENA) {
        yylval = new ParserVal(AnalizadorLexico.referenciaTablaSimbolos);
    }
    return token;
}

void yyerror(String s) {
    System.out.println("Error de sintaxis en linea " + AnalizadorLexico.getLineaActual() + ": " + s);
}

public static void main(String[] args) throws Exception {
    Parser parser = new Parser();
    AnalizadorLexico.reader = new java.io.PushbackReader(
        new java.io.BufferedReader(new java.io.FileReader(args[0])));
    parser.yyparse();
}
//#line 416 "Parser.java"
//###############################################################
// method: yylexdebug : check lexer state
//###############################################################
void yylexdebug(int state,int ch)
{
String s=null;
  if (ch < 0) ch=0;
  if (ch <= YYMAXTOKEN) //check index bounds
     s = yyname[ch];    //now get it
  if (s==null)
    s = "illegal-symbol";
  debug("state "+state+", reading "+ch+" ("+s+")");
}





//The following are now global, to aid in error reporting
int yyn;       //next next thing to do
int yym;       //
int yystate;   //current parsing state from state table
String yys;    //current token string


//###############################################################
// method: yyparse : parse input and execute indicated items
//###############################################################
int yyparse()
{
boolean doaction;
  init_stacks();
  yynerrs = 0;
  yyerrflag = 0;
  yychar = -1;          //impossible char forces a read
  yystate=0;            //initial state
  state_push(yystate);  //save it
  val_push(yylval);     //save empty value
  while (true) //until parsing is done, either correctly, or w/error
    {
    doaction=true;
    if (yydebug) debug("loop"); 
    //#### NEXT ACTION (from reduction table)
    for (yyn=yydefred[yystate];yyn==0;yyn=yydefred[yystate])
      {
      if (yydebug) debug("yyn:"+yyn+"  state:"+yystate+"  yychar:"+yychar);
      if (yychar < 0)      //we want a char?
        {
        yychar = yylex();  //get next token
        if (yydebug) debug(" next yychar:"+yychar);
        //#### ERROR CHECK ####
        if (yychar < 0)    //it it didn't work/error
          {
          yychar = 0;      //change it to default string (no -1!)
          if (yydebug)
            yylexdebug(yystate,yychar);
          }
        }//yychar<0
      yyn = yysindex[yystate];  //get amount to shift by (shift index)
      if ((yyn != 0) && (yyn += yychar) >= 0 &&
          yyn <= YYTABLESIZE && yycheck[yyn] == yychar)
        {
        if (yydebug)
          debug("state "+yystate+", shifting to state "+yytable[yyn]);
        //#### NEXT STATE ####
        yystate = yytable[yyn];//we are in a new state
        state_push(yystate);   //save it
        val_push(yylval);      //push our lval as the input for next rule
        yychar = -1;           //since we have 'eaten' a token, say we need another
        if (yyerrflag > 0)     //have we recovered an error?
           --yyerrflag;        //give ourselves credit
        doaction=false;        //but don't process yet
        break;   //quit the yyn=0 loop
        }

    yyn = yyrindex[yystate];  //reduce
    if ((yyn !=0 ) && (yyn += yychar) >= 0 &&
            yyn <= YYTABLESIZE && yycheck[yyn] == yychar)
      {   //we reduced!
      if (yydebug) debug("reduce");
      yyn = yytable[yyn];
      doaction=true; //get ready to execute
      break;         //drop down to actions
      }
    else //ERROR RECOVERY
      {
      if (yyerrflag==0)
        {
        yyerror("syntax error");
        yynerrs++;
        }
      if (yyerrflag < 3) //low error count?
        {
        yyerrflag = 3;
        while (true)   //do until break
          {
          if (stateptr<0)   //check for under & overflow here
            {
            yyerror("stack underflow. aborting...");  //note lower case 's'
            return 1;
            }
          yyn = yysindex[state_peek(0)];
          if ((yyn != 0) && (yyn += YYERRCODE) >= 0 &&
                    yyn <= YYTABLESIZE && yycheck[yyn] == YYERRCODE)
            {
            if (yydebug)
              debug("state "+state_peek(0)+", error recovery shifting to state "+yytable[yyn]+" ");
            yystate = yytable[yyn];
            state_push(yystate);
            val_push(yylval);
            doaction=false;
            break;
            }
          else
            {
            if (yydebug)
              debug("error recovery discarding state "+state_peek(0)+" ");
            if (stateptr<0)   //check for under & overflow here
              {
              yyerror("Stack underflow. aborting...");  //capital 'S'
              return 1;
              }
            state_pop();
            val_pop();
            }
          }
        }
      else            //discard this token
        {
        if (yychar == 0)
          return 1; //yyabort
        if (yydebug)
          {
          yys = null;
          if (yychar <= YYMAXTOKEN) yys = yyname[yychar];
          if (yys == null) yys = "illegal-symbol";
          debug("state "+yystate+", error recovery discards token "+yychar+" ("+yys+")");
          }
        yychar = -1;  //read another
        }
      }//end error recovery
    }//yyn=0 loop
    if (!doaction)   //any reason not to proceed?
      continue;      //skip action
    yym = yylen[yyn];          //get count of terminals on rhs
    if (yydebug)
      debug("state "+yystate+", reducing "+yym+" by rule "+yyn+" ("+yyrule[yyn]+")");
    if (yym>0)                 //if count of rhs not 'nil'
      yyval = val_peek(yym-1); //get current semantic value
    yyval = dup_yyval(yyval); //duplicate yyval if ParserVal is used as semantic value
    switch(yyn)
      {
//########## USER-SUPPLIED ACTIONS ##########
case 1:
//#line 36 "grupo20.y"
{ System.out.println("Programa reconocido correctamente"); }
break;
//#line 569 "Parser.java"
//########## END OF USER-SUPPLIED ACTIONS ##########
    }//switch
    //#### Now let's reduce... ####
    if (yydebug) debug("reduce");
    state_drop(yym);             //we just reduced yylen states
    yystate = state_peek(0);     //get new state
    val_drop(yym);               //corresponding value drop
    yym = yylhs[yyn];            //select next TERMINAL(on lhs)
    if (yystate == 0 && yym == 0)//done? 'rest' state and at first TERMINAL
      {
      if (yydebug) debug("After reduction, shifting from state 0 to state "+YYFINAL+"");
      yystate = YYFINAL;         //explicitly say we're done
      state_push(YYFINAL);       //and save it
      val_push(yyval);           //also save the semantic value of parsing
      if (yychar < 0)            //we want another character?
        {
        yychar = yylex();        //get next character
        if (yychar<0) yychar=0;  //clean, if necessary
        if (yydebug)
          yylexdebug(yystate,yychar);
        }
      if (yychar == 0)          //Good exit (if lex returns 0 ;-)
         break;                 //quit the loop--all DONE
      }//if yystate
    else                        //else not done yet
      {                         //get next state and push, for next yydefred[]
      yyn = yygindex[yym];      //find out where to go
      if ((yyn != 0) && (yyn += yystate) >= 0 &&
            yyn <= YYTABLESIZE && yycheck[yyn] == yystate)
        yystate = yytable[yyn]; //get new state
      else
        yystate = yydgoto[yym]; //else go to new defred
      if (yydebug) debug("after reduction, shifting from state "+state_peek(0)+" to state "+yystate+"");
      state_push(yystate);     //going again, so push state & val...
      val_push(yyval);         //for next action
      }
    }//main loop
  return 0;//yyaccept!!
}
//## end of method parse() ######################################



//## run() --- for Thread #######################################
/**
 * A default run method, used for operating this parser
 * object in the background.  It is intended for extending Thread
 * or implementing Runnable.  Turn off with -Jnorun .
 */
public void run()
{
  yyparse();
}
//## end of method run() ########################################



//## Constructors ###############################################
/**
 * Default constructor.  Turn off with -Jnoconstruct .

 */
public Parser()
{
  //nothing to do
}


/**
 * Create a parser, setting the debug to true or false.
 * @param debugMe true for debugging, false for no debug.
 */
public Parser(boolean debugMe)
{
  yydebug=debugMe;
}
//###############################################################



}
//################### END OF CLASS ##############################
