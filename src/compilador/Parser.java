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



//#line 2 "gram.y"
import java.io.*;
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
public final static short IF=258;
public final static short ELSE=259;
public final static short END_IF=260;
public final static short BEGIN=261;
public final static short END=262;
public final static short POUT=263;
public final static short RET=264;
public final static short CLASS=265;
public final static short FUNCTION=266;
public final static short ASIGNAR=267;
public final static short MAYORIGUAL=268;
public final static short MENORIGUAL=269;
public final static short IGUALIGUAL=270;
public final static short DISTINTO=271;
public final static short SHORTINT=272;
public final static short SINGLEF=273;
public final static short CADENA=274;
public final static short CONSTANTE=275;
public final static short REPEAT=276;
public final static short UNTIL=277;
public final static short AUTO=278;
public final static short COMPTIME=279;
public final static short IMPORT=280;
public final static short FROM=281;
public final static short EXPORT=282;
public final static short TO=283;
public final static short EXTENDS=284;
public final static short TOS=285;
public final static short YYERRCODE=256;
final static short yylhs[] = {                           -1,
    0,    1,    1,    3,    3,    3,    3,    3,    4,   10,
   10,    9,    9,    5,    5,   11,   11,   11,   12,    6,
   13,   13,   14,   14,   15,   15,   16,   16,   17,   18,
   18,   19,   19,    7,    8,    2,    2,   20,   20,   20,
   20,   20,   20,   21,   21,   27,   27,   22,   22,   22,
   28,   28,   28,   29,   29,   29,   29,   29,   29,   29,
   29,   29,   31,   31,   30,   30,   32,   33,   34,   23,
   23,   35,   37,   37,   37,   37,   37,   37,   36,   36,
   24,   25,   25,   26,
};
final static short yylen[] = {                            2,
    5,    0,    2,    1,    1,    1,    1,    1,    3,    1,
    3,    1,    1,   11,   10,    0,    1,    3,    2,    8,
    0,    3,    0,    3,    0,    2,    1,    1,    4,   11,
   10,    0,    3,    3,    4,    0,    2,    2,    2,    1,
    1,    2,    2,    3,    3,    4,    4,    1,    3,    3,
    1,    3,    3,    1,    1,    3,    4,    1,    1,    1,
    1,    1,    5,    6,    1,    3,    5,    2,    4,    9,
    7,    3,    1,    1,    1,    1,    1,    1,    3,    1,
    7,    4,    4,    4,
};
final static short yydefred[] = {                         0,
    2,    0,    0,    0,   36,    0,   12,   13,    0,    0,
    3,    4,    5,    6,    7,    8,    0,   10,    0,    0,
    0,    0,    0,    0,    0,   34,    0,    0,    0,    1,
    0,    0,   55,    0,    0,    0,   37,    0,    0,   40,
   41,    0,    0,    0,    0,   51,   58,   59,   60,   61,
    0,    0,    0,    0,    0,    9,   11,    0,    0,    0,
    0,    0,    0,    0,    0,   36,   80,    0,    0,   68,
   38,    0,    0,   39,   42,   43,    0,    0,    0,    0,
    0,    0,   35,    0,    0,    0,   62,   56,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,   52,   53,    0,    0,   25,
    0,    0,   17,    0,    0,   57,   47,   46,    0,    0,
   73,   74,   75,   76,   77,   78,    0,    0,   82,   83,
   84,   79,    0,   69,    0,    0,   19,    0,    0,    2,
    0,   63,    0,   67,    0,    0,    0,   24,    0,    0,
    0,   26,   27,   28,   18,   36,    0,   64,    0,    0,
    0,   20,    0,    0,    0,    0,   36,    0,   71,   81,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
   29,   15,    0,   70,    0,    2,    0,   14,   36,    0,
    0,   36,    0,    0,    0,    0,   31,    0,   30,
};
final static short yydgoto[] = {                          2,
    3,   20,   11,   12,   13,   14,   15,   16,  111,   19,
  112,  113,   52,  110,  136,  152,  153,  154,  174,   37,
   38,   39,   40,   41,   42,   43,   87,   45,   46,   90,
   47,   48,   49,   50,   96,   68,  127,
};
final static short yysindex[] = {                      -241,
    0,    0, -217, -237,    0, -181,    0,    0, -230, -245,
    0,    0,    0,    0,    0,    0, -233,    0,   -6,  -43,
 -191, -140, -237, -133,   19,    0, -127,  -31,  104,    0,
  109,  121,    0,  -35,  126, -105,    0,  130,  -20,    0,
    0,  131,  138,  -66,   25,    0,    0,    0,    0,    0,
  -79,  -58,  168,   61,  172,    0,    0,  -27,  -41, -240,
  -39,  185,  -27,   29,  -27,    0,    0,  -46,  -27,    0,
    0,  -27,  -27,    0,    0,    0,  -27,  -27,  -27, -237,
  -47, -245,    0, -245,  -32,  139,    0,    0,  139,   85,
  145,  146,  203,  -27,   65,  215,  225,   34,  111,  -11,
  247,  128,   25,   25,  139,    0,    0,  249, -237,    0,
   43,  107,    0,  118,  -27,    0,    0,    0,  -40,  166,
    0,    0,    0,    0,    0,    0,  -27,  -35,    0,    0,
    0,    0,  -27,    0,   69, -221,    0, -245,   40,    0,
  139,    0,  133,    0,  139, -214,  264,    0,  252,   55,
   56,    0,    0,    0,    0,    0,  -93,    0,  -35,  260,
  265,    0,  285,  286,  -25,   -3,    0,   71,    0,    0,
 -245, -245,   49,  279,  280,    5,  282,  169,  183, -237,
    0,    0,  283,    0,   82,    0,  249,    0,    0,  -74,
   13,    0,   62,   21,  287,   62,    0,  288,    0,
};
final static short yyrindex[] = {                         0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
   84,    0,    0,    0,    0,    0,    0,   26,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,   98,   47,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
 -208,  250,    0,  250,   39,  289,    0,    0,  251,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,   52,   59,  290,    0,    0,   89,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
  258,    0,    0,    0,  310,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,  -33,  293,    0,    0,    0,    0,    0,
  250,  250,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,  294,    0,    0,    0,
    0,    0,  293,    0,    0,  293,    0,    0,    0,
};
final static short yygindex[] = {                         0,
 -137,  -34,    0,    0,    0,    0,    0,    0,    3,   14,
  -37,  216,    0,    0,    0,    0,    0,    0,  144,  -12,
    0,   73,    0,    0,    0,    0,  -13,  103,  117,  236,
    0,    0,    0,    0,  223, -116,    0,
};
final static int YYTABLESIZE=356;
static short yytable[];
static { yytable();}
static void yytable(){
yytable = new short[]{                         88,
  142,   36,  157,   36,   36,   17,   44,   59,   59,   36,
   10,  146,   23,   61,   61,    1,   91,   36,   27,   18,
   44,   67,   72,   18,   73,   10,    7,    8,   62,   62,
   25,  100,   24,   36,   92,   22,   54,   27,   74,    4,
  149,   36,  168,    5,  159,  160,  114,    6,  190,   36,
    7,    8,   26,   23,    7,    8,  150,   36,   60,   60,
    9,   10,   27,   23,   23,   36,   78,   54,   54,   23,
   54,   79,   54,   36,  130,   21,   72,   56,   73,   54,
   54,   54,   54,   54,   54,   54,   44,   48,   51,   48,
   48,   48,   49,  108,   49,   49,   49,   54,   54,   50,
   54,   50,   50,   50,   27,   48,   48,   72,   48,   73,
   49,   49,   27,   49,   44,   67,   53,   50,   50,   83,
   50,  166,  135,   55,  126,  116,  125,  148,  115,   57,
   86,   89,  176,  178,  179,   95,   98,   99,  151,   62,
   62,  102,   62,   63,   62,   44,   67,  139,   64,  105,
  138,  131,   44,   72,  191,   73,   62,  194,  140,   17,
   65,  138,   44,    4,  165,   69,  120,  167,  134,   70,
   72,    6,   73,  158,  103,  104,  115,   44,    7,    8,
   44,   72,    4,   73,    9,   10,  192,  141,   71,   75,
    6,   89,   17,  187,  106,  107,   76,    7,    8,  145,
   77,   80,   81,    9,   10,   95,  144,   82,   72,  185,
   73,   84,  138,   28,   29,   85,   85,   93,   30,   31,
   32,   28,   29,  186,   94,   66,  138,   31,   32,   85,
  101,   33,   34,   33,   33,   58,  109,  117,  118,   33,
   34,   35,  119,   35,   35,   28,   29,   33,   10,   35,
  132,   31,   32,   28,   29,  128,  173,   35,  175,   31,
   32,   28,   29,   33,   34,  129,  183,   31,   32,   28,
   29,   33,   34,   35,  193,   31,   32,   28,   29,   33,
   34,   35,  196,   31,   32,   85,  133,   33,   34,   35,
   16,   65,   27,   16,   65,   33,   34,   35,   66,  137,
  156,   66,   97,   33,  161,   35,   54,   54,   54,   54,
  162,  163,  164,   35,   48,   48,   48,   48,  169,   49,
   49,   49,   49,  170,  171,  172,   50,   50,   50,   50,
  177,  180,  121,  122,  123,  124,  195,  181,  182,  198,
  184,  188,  189,  173,   21,  197,  199,   44,   45,   22,
   72,   32,   33,  155,  143,  147,
};
}
static short yycheck[];
static { yycheck(); }
static void yycheck() {
yycheck = new short[] {                         41,
   41,   45,  140,   45,   45,    3,   20,   40,   40,   45,
   44,  128,   10,   46,   46,  257,  257,   45,   44,  257,
   34,   34,   43,  257,   45,   59,  272,  273,   61,   61,
   17,   66,  266,   45,  275,  266,   23,   44,   59,  257,
  262,   45,  159,  261,  259,  260,   84,  265,  186,   45,
  272,  273,   59,  262,  272,  273,  278,   45,   91,   91,
  278,  279,   44,  272,  273,   45,   42,   42,   43,  278,
   45,   47,   47,   45,   41,  257,   43,   59,   45,   41,
   42,   43,   44,   45,   59,   47,  100,   41,  280,   43,
   44,   45,   41,   80,   43,   44,   45,   59,   60,   41,
   62,   43,   44,   45,   44,   59,   60,   43,   62,   45,
   59,   60,   44,   62,  128,  128,  257,   59,   60,   59,
   62,  156,  109,  257,   60,   41,   62,   59,   44,  257,
   58,   59,  167,  171,  172,   63,   64,   65,  136,   42,
   43,   69,   45,   40,   47,  159,  159,   41,   40,   77,
   44,   41,  166,   43,  189,   45,   59,  192,   41,  157,
   40,   44,  176,  257,  151,   40,   94,  261,   41,  275,
   43,  265,   45,   41,   72,   73,   44,  191,  272,  273,
  194,   43,  257,   45,  278,  279,  261,  115,   59,   59,
  265,  119,  190,  180,   78,   79,   59,  272,  273,  127,
  267,  281,  261,  278,  279,  133,   41,   40,   43,   41,
   45,   40,   44,  257,  258,  257,  257,  257,  262,  263,
  264,  257,  258,   41,   40,  261,   44,  263,  264,  257,
  277,  275,  276,  275,  275,  267,  284,   93,   93,  275,
  276,  285,   40,  285,  285,  257,  258,  275,  282,  285,
  262,  263,  264,  257,  258,   41,  282,  285,  262,  263,
  264,  257,  258,  275,  276,   41,  262,  263,  264,  257,
  258,  275,  276,  285,  262,  263,  264,  257,  258,  275,
  276,  285,  262,  263,  264,  257,   40,  275,  276,  285,
   41,   41,   44,   44,   44,  275,  276,  285,   41,  257,
  261,   44,  274,  275,   41,  285,  268,  269,  270,  271,
   59,  257,  257,  285,  268,  269,  270,  271,   59,  268,
  269,  270,  271,   59,   40,   40,  268,  269,  270,  271,
  260,  283,  268,  269,  270,  271,  193,   59,   59,  196,
   59,   59,  261,  282,  261,   59,   59,   59,   59,  261,
   41,   59,   59,  138,  119,  133,
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
null,null,null,null,null,null,null,"IDENTIFICADOR","IF","ELSE","END_IF","BEGIN",
"END","POUT","RET","CLASS","FUNCTION","ASIGNAR","MAYORIGUAL","MENORIGUAL",
"IGUALIGUAL","DISTINTO","SHORTINT","SINGLEF","CADENA","CONSTANTE","REPEAT",
"UNTIL","AUTO","COMPTIME","IMPORT","FROM","EXPORT","TO","EXTENDS","TOS",
};
final static String yyrule[] = {
"$accept : programa",
"programa : IDENTIFICADOR sentencias_declarativas BEGIN sentencias_ejecutables END",
"sentencias_declarativas :",
"sentencias_declarativas : sentencias_declarativas sentencia_declarativa",
"sentencia_declarativa : declaracion_variables",
"sentencia_declarativa : declaracion_funciones",
"sentencia_declarativa : declaracion_clase",
"sentencia_declarativa : declaracion_objeto",
"sentencia_declarativa : declaracion_comptime",
"declaracion_variables : tipo lista_identificadores ';'",
"lista_identificadores : IDENTIFICADOR",
"lista_identificadores : lista_identificadores ',' IDENTIFICADOR",
"tipo : SHORTINT",
"tipo : SINGLEF",
"declaracion_funciones : tipo FUNCTION IDENTIFICADOR '(' lista_parametros_formales ')' sentencias_declarativas BEGIN sentencias_ejecutables END ';'",
"declaracion_funciones : AUTO FUNCTION IDENTIFICADOR '(' lista_parametros_formales ')' BEGIN sentencias_ejecutables END ';'",
"lista_parametros_formales :",
"lista_parametros_formales : parametro_formal",
"lista_parametros_formales : lista_parametros_formales ',' parametro_formal",
"parametro_formal : tipo IDENTIFICADOR",
"declaracion_clase : CLASS IDENTIFICADOR importacion_opcional BEGIN herencia_opcional miembros_clase END ';'",
"importacion_opcional :",
"importacion_opcional : IMPORT FROM lista_identificadores",
"herencia_opcional :",
"herencia_opcional : EXTENDS lista_identificadores ';'",
"miembros_clase :",
"miembros_clase : miembros_clase miembro_clase",
"miembro_clase : atributo_clase",
"miembro_clase : metodo_clase",
"atributo_clase : tipo lista_identificadores exportacion_opcional ';'",
"metodo_clase : tipo IDENTIFICADOR '(' lista_parametros_formales ')' sentencias_declarativas BEGIN sentencias_ejecutables END exportacion_opcional ';'",
"metodo_clase : AUTO IDENTIFICADOR '(' lista_parametros_formales ')' BEGIN sentencias_ejecutables END exportacion_opcional ';'",
"exportacion_opcional :",
"exportacion_opcional : EXPORT TO lista_identificadores",
"declaracion_objeto : IDENTIFICADOR lista_identificadores ';'",
"declaracion_comptime : COMPTIME tipo lista_identificadores ';'",
"sentencias_ejecutables :",
"sentencias_ejecutables : sentencias_ejecutables sentencia_ejecutable",
"sentencia_ejecutable : asignacion ';'",
"sentencia_ejecutable : expresion ';'",
"sentencia_ejecutable : sentencia_if",
"sentencia_ejecutable : sentencia_repeat_until",
"sentencia_ejecutable : sentencia_pout ';'",
"sentencia_ejecutable : sentencia_ret ';'",
"asignacion : IDENTIFICADOR ASIGNAR expresion",
"asignacion : acceso_posicional ASIGNAR expresion",
"acceso_posicional : IDENTIFICADOR '[' CONSTANTE ']'",
"acceso_posicional : IDENTIFICADOR '[' IDENTIFICADOR ']'",
"expresion : termino",
"expresion : expresion '+' termino",
"expresion : expresion '-' termino",
"termino : factor",
"termino : termino '*' factor",
"termino : termino '/' factor",
"factor : IDENTIFICADOR",
"factor : CONSTANTE",
"factor : IDENTIFICADOR '(' ')'",
"factor : IDENTIFICADOR '(' lista_expresiones ')'",
"factor : invocacion_metodo",
"factor : unica",
"factor : numero_negativo",
"factor : conversion_tos",
"factor : acceso_posicional",
"invocacion_metodo : IDENTIFICADOR '.' IDENTIFICADOR '(' ')'",
"invocacion_metodo : IDENTIFICADOR '.' IDENTIFICADOR '(' lista_expresiones ')'",
"lista_expresiones : expresion",
"lista_expresiones : lista_expresiones ',' expresion",
"unica : IDENTIFICADOR '=' '(' expresion ')'",
"numero_negativo : '-' CONSTANTE",
"conversion_tos : TOS '(' expresion ')'",
"sentencia_if : IF '(' condicion ')' bloque ELSE bloque END_IF ';'",
"sentencia_if : IF '(' condicion ')' bloque END_IF ';'",
"condicion : expresion comparador expresion",
"comparador : MAYORIGUAL",
"comparador : MENORIGUAL",
"comparador : IGUALIGUAL",
"comparador : DISTINTO",
"comparador : '>'",
"comparador : '<'",
"bloque : BEGIN sentencias_ejecutables END",
"bloque : sentencia_ejecutable",
"sentencia_repeat_until : REPEAT bloque UNTIL '(' condicion ')' ';'",
"sentencia_pout : POUT '(' CADENA ')'",
"sentencia_pout : POUT '(' expresion ')'",
"sentencia_ret : RET '(' expresion ')'",
};

//#line 309 "gram.y"

/* ========================================================================= */
/* CODIGO DE SOPORTE                                                         */
/* ========================================================================= */

static Parser parser;

public static void main(String[] args) {
    String ruta = "prueba_gramatica";
    if (args.length > 0) {
        ruta = args[0];
    }
    System.out.println("Compilando archivo: " + ruta);
    try {
        AnalizadorLexico.reader = new PushbackReader(new BufferedReader(new FileReader(ruta)));
        parser = new Parser(true);
        parser.yyparse(); TablaSimbolos.imprimirTabla();
    } catch (Exception e) {
        e.printStackTrace();
    }
}

int yylex() {
    int token = AnalizadorLexico.yylex();
    if (token <= 0) {
        return 0; // EOF para byacc/j
    }
    yylval = new ParserVal(AnalizadorLexico.referenciaTablaSimbolos);
    return token;
}

void yyerror(String s) {
    System.out.println("Error sintactico (linea " + AnalizadorLexico.getLineaActual() + "): " + s);
}
//#line 450 "Parser.java"
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
//#line 51 "gram.y"
{ System.out.println("Programa reconocido correctamente"); }
break;
case 55:
//#line 208 "gram.y"
{
            int id = val_peek(0).ival;
            String lexema = TablaSimbolos.obtenerAtributo(id, TablaSimbolos.LEXEMA);
            String tipo = TablaSimbolos.obtenerAtributo(id, "TIPO");
            if (tipo.equals("SHORTINT")) {
                int valor = Integer.parseInt(lexema);
                if (valor > AnalizadorLexico.ValorMaximoInt) {
                    yyerror("Constante shortint positiva fuera de rango (" + lexema + "). Rango permitido: [-128, 127]");
                }
            } else if (tipo.equals("SINGLEF")) {
                double valor = Double.parseDouble(lexema);
                if (valor != 0.0 && (valor < AnalizadorLexico.ValorMinimoFloat || valor > AnalizadorLexico.ValorMaximoFloat)) {
                    yyerror("Constante singlef fuera de rango (" + lexema + ")");
                }
            }
        }
break;
case 68:
//#line 250 "gram.y"
{
            int id_pos = val_peek(0).ival;
            String lexema_pos = TablaSimbolos.obtenerAtributo(id_pos, TablaSimbolos.LEXEMA);
            int id_neg = TablaSimbolos.convertirANegativo(lexema_pos);
            yyval.ival = id_neg;
        }
break;
//#line 631 "Parser.java"
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
