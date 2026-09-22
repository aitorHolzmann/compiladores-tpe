package src.compilador;
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






//#line 1 "gram.y"

import java.io.*;
//#line 20 "Parser.java"




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
public final static short CTE_SHORTINT=286;
public final static short CTE_SINGLEF=287;
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
    0,    3,    0,    4,    0,    0,    1,    1,    5,    5,
    5,    5,    5,    6,    6,   12,   12,   12,   13,   13,
   11,   11,    7,   15,    7,    7,    7,   17,    7,    7,
   16,   14,   14,   14,   18,   18,   18,    8,    8,   19,
   19,   19,   20,   20,   20,   21,   21,   22,   22,   23,
   24,   24,   25,   25,   25,    9,    9,   10,   10,   10,
   10,    2,    2,   26,   26,   26,   26,   26,   26,   26,
   27,   27,   33,   33,   33,   28,   28,   28,   28,   28,
   28,   34,   34,   34,   34,   34,   34,   34,   34,   34,
   34,   35,   35,   35,   35,   35,   35,   35,   35,   35,
   35,   37,   37,   36,   36,   38,   38,   39,   39,   40,
   29,   29,   29,   43,   43,   43,   43,   41,   44,   44,
   44,   44,   44,   44,   42,   42,   42,   30,   30,   30,
   30,   30,   30,   45,   45,   45,   45,   31,   31,   31,
   32,
};
final static short yylen[] = {                            2,
    5,    0,    6,    0,    6,    4,    0,    2,    1,    1,
    1,    1,    1,    3,    3,    1,    3,    2,    1,    3,
    1,    1,   11,    0,   12,   10,    5,    0,   12,    4,
    6,    0,    1,    3,    2,    2,    2,    8,    7,    0,
    3,    2,    0,    3,    2,    0,    2,    1,    1,    4,
   11,   10,    0,    3,    2,    3,    2,    4,    3,    3,
    2,    0,    2,    2,    2,    1,    1,    2,    2,    2,
    3,    3,    4,    4,    4,    1,    3,    3,    3,    3,
    2,    1,    3,    3,    3,    3,    2,    2,    2,    2,
    2,    1,    1,    1,    3,    4,    1,    1,    1,    1,
    1,    5,    6,    1,    3,    5,    5,    2,    2,    4,
    6,    5,    4,    2,    4,    1,    3,    3,    1,    1,
    1,    1,    1,    1,    3,    1,    2,    5,    4,    6,
    5,    5,    4,    3,    2,    2,    1,    4,    4,    3,
    4,
};
final static short yydefred[] = {                         0,
    2,    7,    0,    7,    0,    0,    4,    0,   62,    0,
   21,   22,    0,    0,    8,    9,   10,   11,   12,   13,
    0,    0,   62,   62,   19,    0,    0,    0,    0,    0,
    0,    0,   16,    0,    0,   62,    0,    0,   56,    0,
    0,    0,    0,    1,    0,    0,   93,   94,    0,    0,
    0,    0,    0,    0,   63,    0,    0,   66,   67,    0,
    0,    0,    0,   82,   97,   98,   99,  100,    0,    0,
   28,    0,    0,   60,   15,   24,    0,   18,   14,    0,
    0,    3,    5,   20,   70,    0,    0,    0,    0,    0,
    0,    0,    0,    0,  101,    0,    0,    0,   62,    0,
    0,  126,    0,    0,    0,  108,  109,   87,   88,   64,
    0,    0,   65,   68,   69,    0,   89,   90,   91,    0,
    0,    0,    0,    0,    0,    0,   58,    0,    0,   17,
    0,    0,    0,   95,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,  119,  120,  121,  122,  123,  124,
    0,    0,    0,  140,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,   79,    0,   80,    0,    0,   85,
   83,   86,   84,    0,    0,   46,    0,    0,    0,    0,
   33,    0,    0,   27,    0,    0,   96,   74,   73,   75,
    0,    0,    0,    0,  116,  113,    0,    0,    0,  138,
  139,  141,  125,    0,  135,  129,  133,    0,    0,    0,
  110,   45,    0,    0,    0,   37,   36,   35,    0,   31,
    0,    7,  107,    0,  102,    0,  106,    0,  114,    0,
  112,  134,  131,  128,  132,    0,   44,    0,    0,    0,
   47,   48,   49,    7,   34,    7,    0,  103,    0,  117,
  111,  130,   38,    0,    0,    0,    0,    0,   62,  115,
    0,    0,    0,    0,   62,   62,    0,    0,    0,    0,
    0,   50,    0,    0,    0,    0,    7,    0,    0,    0,
   23,   62,    0,   29,   25,    0,   62,    0,    0,    0,
    0,   52,    0,   51,
};
final static short yydgoto[] = {                          3,
    5,   27,    4,   24,   15,   16,   17,   18,   19,   20,
  179,   35,   26,  180,  128,   22,  125,  181,   70,  176,
  214,  241,  242,  243,  264,   55,   56,   57,   58,   59,
   60,   61,   95,   63,   64,  136,   65,   66,   67,   68,
  159,  103,  196,  151,  160,
};
final static short yysindex[] = {                      -137,
    0,    0,    0,    0,  352,  221,    0, -233,    0, -221,
    0,    0, -225, -102,    0,    0,    0,    0,    0,    0,
   76, -214,    0,    0,    0,   42,  103, -218,  -97, -233,
  123,   39,    0,  -18,  -37,    0,  119,  136,    0, -142,
   72,  -32,   18,    0,   93,   99,    0,    0,    3,  101,
  445,   59,  217,  217,    0,   88,  -10,    0,    0,   90,
  127,  -72,  -16,    0,    0,    0,    0,    0, -230,  -62,
    0,  166,  141,    0,    0,    0,  172,    0,    0,  -39,
  151,    0,    0,    0,    0,   -3,   62, -133,  -20,  185,
  168,   96,   83,  358,    0,  208,   49,   83,    0,   28,
   83,    0,  -29,   83,  -16,    0,    0,    0,    0,    0,
   87,  177,    0,    0,    0,   83,    0,    0,    0,   44,
  232, -233,  211,  -11,  225, -129,    0,  241, -129,    0,
  234,   83,  -15,    0,  -15,   25,  214,  219,  245,  285,
   83,  -43,  312,  313,    0,    0,    0,    0,    0,    0,
   83,  168,  321,    0,  189,  296,  184,   83,  323,  319,
   -7,   28,   83,  503,    0,  -16,    0,  -16,  -15,    0,
    0,    0,    0,  211,  -34,    0, -129,  114,   30,   67,
    0, -129,  147,    0,  509,   83,    0,    0,    0,    0,
   71,  534,  168,  327,    0,    0,  168,  -15,  -43,    0,
    0,    0,    0,  339,    0,    0,    0,  332,  337,   38,
    0,    0,  146, -104,  163,    0,    0,    0, -129,    0,
  192,    0,    0,  -15,    0,  254,    0,  -46,    0,  -43,
    0,    0,    0,    0,    0,  338,    0,  351,  154,  162,
    0,    0,    0,    0,    0,    0,  498,    0,  369,    0,
    0,    0,    0,  390,  395,  -38,  585,  587,    0,    0,
 -129, -129, -245,  380,    0,    0,  249,  258,  280, -233,
  211,    0,  275,  308,  384,  190,    0,  211,  386,  391,
    0,    0,  610,    0,    0,  414,    0,  176,  430,  407,
  176,    0,  425,    0,
};
final static short yyrindex[] = {                         0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,  376,  467,  235,    0,    0,
  378,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,  130,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,  438,  483,    0,    0,    0,    0,    0,    0,    0,
    0,    0,  402,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,   40,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,  514,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,  240,  -70,    0,  408,    0,    0,  408,    0,
  501,    0,  449,    0,  424,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,  125,    0,  450,    0,
    0,    0,    0,    0,    0,  537,    0,  542,  451,    0,
    0,    0,    0,  253,    0,    0,  408,    0,    0,    0,
    0,  408,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,   47,    0,    0,
    0,    0,    0,  456,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,  435,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,  560,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,   34,  457,    0,    0,    0,    0,
  408,  408,    0,    0,    0,    0,    0,    0,    0,    0,
  461,    0,    0,    0,  562,    0,    0,  462,    0,    0,
    0,    0,    0,    0,    0,    0,    0,  457,    0,    0,
  457,    0,    0,    0,
};
final static short yygindex[] = {                         0,
   -1,  -19,    0,    0,    0,    0,    0,    0,    0,    0,
   -5,    0,  -12, -110,    0,    0,    0,  303,    0,    0,
    0,    0,    0,    0,  207,    4,    0,  640,    0,    0,
    0,    0,  -17,   26,    2,  349,    0,    0,    0,    0,
  448,  -76, -176,    0,  361,
};
final static int YYTABLESIZE=889;
static short yytable[];
static { yytable();}
static void yytable(){
yytable = new short[]{                         21,
   21,   31,    6,   37,   38,   40,   80,   87,   30,   62,
  163,   25,  250,   89,  142,  195,   81,   73,  183,   62,
   62,   79,  231,   25,  212,  120,   25,  111,   90,  112,
  121,   62,  111,  208,  112,   28,  132,  270,   53,   51,
   29,   52,  101,   54,   53,   51,   36,   52,  113,   54,
  122,  207,  102,  251,  108,  109,  123,   93,   88,   53,
   51,   69,   52,   62,   54,  187,  215,  158,  186,   53,
   51,  221,   52,   62,   54,  199,  105,   19,  236,  157,
   92,   92,   92,   92,   92,   40,   92,  118,   52,  154,
   53,   51,   19,   52,  102,   54,  235,   75,   92,   92,
   39,   92,  134,   53,   51,  118,   52,  220,   54,  174,
  219,  225,   53,   51,   84,   52,  228,   54,    1,    2,
  230,  171,  173,  137,   53,   51,  178,   52,   53,   54,
   85,   52,   97,   54,   62,   87,  166,  168,   98,   62,
  104,   89,   11,   12,   53,   51,  110,   52,  114,   54,
  268,  269,  138,  139,   25,  102,   90,  238,   71,   72,
   53,   51,  213,   52,  127,   54,   40,   11,   12,   11,
   12,   92,   92,  239,   92,   62,   92,   53,   51,   62,
   52,   74,   54,  127,   40,  115,   88,  222,   92,   40,
  219,   43,   53,   51,  116,   52,  102,   54,  124,  127,
  102,   43,   43,  244,  237,  126,  219,   43,  240,   53,
   51,  129,   52,  249,   54,  193,  194,  130,   53,   78,
  247,   52,   25,   54,  141,   53,   51,  256,   52,  201,
   54,  111,  246,  112,   86,  219,  140,   76,   77,  267,
  117,   21,  257,  263,  258,  273,  274,  162,  152,   62,
  271,   21,   21,   92,   40,   62,   62,  278,   41,   42,
   43,   52,  286,   99,  177,   45,   46,  289,   62,  118,
  119,   62,  175,   91,   92,  283,   52,   21,   49,  100,
  182,   50,   47,   48,   92,  217,  218,   50,   47,   48,
   53,   51,  184,   52,  248,   54,   92,  186,  276,  170,
   92,  219,   50,   47,   48,   92,  188,   92,   92,   92,
   92,  189,   50,   47,   48,   19,   53,   51,   92,   52,
  277,   54,  153,  219,  191,   92,   92,   92,   50,   47,
   48,   32,   33,   50,   47,   48,  202,  190,  111,   92,
  112,   34,  165,   92,  106,  107,   50,   47,   48,   53,
   51,  132,   52,  197,   54,   50,   47,   48,   41,   42,
   43,  200,  143,  205,   44,   45,   46,   50,   47,   48,
  216,   50,   47,   48,   41,   42,   43,  206,   49,  232,
   82,   45,   46,  127,  127,  229,   92,   50,   47,   48,
  233,   41,   42,   43,   49,  234,  252,   83,   45,   46,
  111,  127,  112,   50,   47,   48,   41,   42,   43,  253,
  254,   49,  131,   45,   46,   92,   92,  150,  255,  149,
   50,   47,   48,   41,   42,   43,   49,  260,   99,  261,
   45,   46,  167,   92,  262,   50,   47,   48,  272,   41,
   42,   43,  281,   49,  284,  203,   45,   46,   32,  285,
  282,   32,   50,   47,   48,   53,   51,  263,   52,   49,
   54,   50,   47,   48,  104,  292,    6,  104,   50,   47,
   48,   53,   51,   92,   52,  105,   54,    8,  105,  101,
  101,   23,  101,  294,  101,   10,   53,  172,   92,   52,
   96,   54,   11,   12,  290,   40,  101,  293,   13,   14,
   42,   50,   47,   48,   41,   42,   43,   71,  137,   72,
  275,   45,   46,   41,  136,   53,   50,   47,   48,   55,
   54,  245,  209,   76,   49,   76,   76,   76,    0,    0,
   41,   42,   43,   50,   47,   48,  279,   45,   46,  226,
  144,   76,   76,  211,   76,  111,    0,  112,  161,  223,
   49,  111,    0,  112,   81,    0,   81,   81,   81,   50,
   47,   48,    0,   41,   42,   43,    0,    0,    0,  280,
   45,   46,   81,   81,  227,   81,  111,   77,  112,   77,
   77,   77,   78,   49,   78,   78,   78,    0,    0,    0,
    0,    0,   50,   47,   48,   77,   77,    0,   77,    0,
   78,   78,    0,   78,    0,  204,    0,    7,    8,    0,
  210,    0,    9,    0,    0,    0,   10,    0,    0,    0,
    0,    0,    0,   11,   12,  145,  146,  147,  148,   13,
   14,   57,   57,   61,   61,    0,   57,    0,   61,    0,
   57,    0,   61,    0,    0,    0,    0,   57,   57,   61,
   61,    0,    0,   57,   57,   61,   61,   59,   59,    0,
    0,    0,   59,    0,    0,    0,   59,    0,    0,   41,
   42,   43,    0,   59,   59,  288,   45,   46,    0,   59,
   59,    0,   94,    0,    0,   41,   42,   43,    0,   49,
    0,  291,   45,   46,  101,    0,    0,    0,   50,   47,
   48,   92,    0,    0,    0,   49,    0,    0,    0,    0,
    0,    0,    0,    0,   50,   47,   48,    0,    0,    0,
    0,    0,    0,  101,  101,  133,  135,    0,    0,   50,
   47,   48,   94,    0,    0,    0,  155,  156,    0,   94,
   94,    0,    0,  164,    0,    0,    0,    0,    0,    0,
   76,   76,   76,   76,    8,  169,   30,   30,  259,    0,
    0,   30,   10,    0,    0,   30,    0,    0,    0,   11,
   12,  185,   30,   30,    0,   13,   14,    0,   30,   30,
  192,   81,   81,   81,   81,    0,    0,    0,    0,    0,
  198,    0,    0,    0,    0,    0,    0,   94,    0,    0,
    0,   94,   94,    0,   77,   77,   77,   77,    0,   78,
   78,   78,   78,    0,    0,   39,   39,   26,   26,    0,
   39,    0,   26,    0,   39,  224,   26,    0,    0,    0,
  135,   39,   39,   26,   26,    0,    0,   39,   39,   26,
   26,    8,    0,    8,    0,  265,    0,  266,    0,   10,
    0,   10,    0,    0,    0,    0,   11,   12,   11,   12,
    0,    0,   13,   14,   13,   14,    8,    0,    0,    0,
  287,    0,    0,    0,   10,    0,    0,    0,    0,    0,
    0,   11,   12,    0,    0,    0,    0,   13,   14,
};
}
static short yycheck[];
static { yycheck(); }
static void yycheck() {
yycheck = new short[] {                          5,
    6,   14,    4,   23,   24,   44,   44,   40,   14,   27,
   40,  257,   59,   46,   91,   59,   36,   30,  129,   37,
   38,   59,  199,  257,   59,   42,  257,   43,   61,   45,
   47,   49,   43,   41,   45,  257,   40,  283,   42,   43,
  266,   45,   40,   47,   42,   43,  261,   45,   59,   47,
  281,   59,   49,  230,   53,   54,   69,   40,   91,   42,
   43,  280,   45,   81,   47,   41,  177,   40,   44,   42,
   43,  182,   45,   91,   47,  152,   51,   44,   41,   99,
   41,   42,   43,   44,   45,   44,   47,   41,   45,   41,
   42,   43,   59,   45,   91,   47,   59,   59,   59,   60,
   59,   62,   41,   42,   43,   59,   45,   41,   47,  122,
   44,   41,   42,   43,  257,   45,  193,   47,  256,  257,
  197,  120,  121,  257,   42,   43,  256,   45,   42,   47,
   59,   45,   40,   47,  152,   40,  111,  112,   40,  157,
   40,   46,  272,  273,   42,   43,   59,   45,   59,   47,
  261,  262,  286,  287,  257,  152,   61,  262,  256,  257,
   42,   43,  175,   45,   40,   47,   44,  272,  273,  272,
  273,   42,   43,  278,   45,  193,   47,   42,   43,  197,
   45,   59,   47,   59,   44,   59,   91,   41,   59,   44,
   44,  262,   42,   43,  267,   45,  193,   47,  261,   59,
  197,  272,  273,   41,   59,   40,   44,  278,  214,   42,
   43,   40,   45,  260,   47,  259,  260,  257,   42,  257,
  222,   45,  257,   47,   40,   42,   43,  240,   45,   41,
   47,   43,   41,   45,  267,   44,  257,  256,  257,  259,
  257,  247,  244,  282,  246,  265,  266,  277,   41,  267,
  263,  257,  258,  257,   44,  273,  274,  270,  256,  257,
  258,   45,  282,  261,   40,  263,  264,  287,  286,  286,
  287,  289,  284,  256,  257,  277,   45,  283,  276,  277,
   40,  285,  286,  287,  257,  256,  257,  285,  286,  287,
   42,   43,   59,   45,   41,   47,  257,   44,   41,  256,
  257,   44,  285,  286,  287,  257,   93,  268,  269,  270,
  271,   93,  285,  286,  287,  282,   42,   43,  257,   45,
   41,   47,  274,   44,   40,  286,  287,  257,  285,  286,
  287,  256,  257,  285,  286,  287,   41,   93,   43,  257,
   45,  266,  256,  257,  286,  287,  285,  286,  287,   42,
   43,   40,   45,   41,   47,  285,  286,  287,  256,  257,
  258,   41,  267,   41,  262,  263,  264,  285,  286,  287,
  257,  285,  286,  287,  256,  257,  258,   59,  276,   41,
  262,  263,  264,  259,  260,   59,  257,  285,  286,  287,
   59,  256,  257,  258,  276,   59,   59,  262,  263,  264,
   43,  277,   45,  285,  286,  287,  256,  257,  258,   59,
  257,  276,  262,  263,  264,  286,  287,   60,  257,   62,
  285,  286,  287,  256,  257,  258,  276,   59,  261,   40,
  263,  264,  256,  257,   40,  285,  286,  287,   59,  256,
  257,  258,   59,  276,   59,  262,  263,  264,   41,   59,
  261,   44,  285,  286,  287,   42,   43,  282,   45,  276,
   47,  285,  286,  287,   41,   59,    0,   44,  285,  286,
  287,   42,   43,  257,   45,   41,   47,  257,   44,   42,
   43,  261,   45,   59,   47,  265,   42,  256,  257,   45,
   43,   47,  272,  273,  288,  261,   59,  291,  278,  279,
  261,  285,  286,  287,  256,  257,  258,   59,   59,   59,
  262,  263,  264,  261,   59,   59,  285,  286,  287,   59,
   59,  219,  162,   41,  276,   43,   44,   45,   -1,   -1,
  256,  257,  258,  285,  286,  287,  262,  263,  264,  191,
   93,   59,   60,   41,   62,   43,   -1,   45,  101,   41,
  276,   43,   -1,   45,   41,   -1,   43,   44,   45,  285,
  286,  287,   -1,  256,  257,  258,   -1,   -1,   -1,  262,
  263,  264,   59,   60,   41,   62,   43,   41,   45,   43,
   44,   45,   41,  276,   43,   44,   45,   -1,   -1,   -1,
   -1,   -1,  285,  286,  287,   59,   60,   -1,   62,   -1,
   59,   60,   -1,   62,   -1,  158,   -1,  256,  257,   -1,
  163,   -1,  261,   -1,   -1,   -1,  265,   -1,   -1,   -1,
   -1,   -1,   -1,  272,  273,  268,  269,  270,  271,  278,
  279,  256,  257,  256,  257,   -1,  261,   -1,  261,   -1,
  265,   -1,  265,   -1,   -1,   -1,   -1,  272,  273,  272,
  273,   -1,   -1,  278,  279,  278,  279,  256,  257,   -1,
   -1,   -1,  261,   -1,   -1,   -1,  265,   -1,   -1,  256,
  257,  258,   -1,  272,  273,  262,  263,  264,   -1,  278,
  279,   -1,   43,   -1,   -1,  256,  257,  258,   -1,  276,
   -1,  262,  263,  264,  257,   -1,   -1,   -1,  285,  286,
  287,  257,   -1,   -1,   -1,  276,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,  285,  286,  287,   -1,   -1,   -1,
   -1,   -1,   -1,  286,  287,   86,   87,   -1,   -1,  285,
  286,  287,   93,   -1,   -1,   -1,   97,   98,   -1,  100,
  101,   -1,   -1,  104,   -1,   -1,   -1,   -1,   -1,   -1,
  268,  269,  270,  271,  257,  116,  256,  257,  261,   -1,
   -1,  261,  265,   -1,   -1,  265,   -1,   -1,   -1,  272,
  273,  132,  272,  273,   -1,  278,  279,   -1,  278,  279,
  141,  268,  269,  270,  271,   -1,   -1,   -1,   -1,   -1,
  151,   -1,   -1,   -1,   -1,   -1,   -1,  158,   -1,   -1,
   -1,  162,  163,   -1,  268,  269,  270,  271,   -1,  268,
  269,  270,  271,   -1,   -1,  256,  257,  256,  257,   -1,
  261,   -1,  261,   -1,  265,  186,  265,   -1,   -1,   -1,
  191,  272,  273,  272,  273,   -1,   -1,  278,  279,  278,
  279,  257,   -1,  257,   -1,  261,   -1,  261,   -1,  265,
   -1,  265,   -1,   -1,   -1,   -1,  272,  273,  272,  273,
   -1,   -1,  278,  279,  278,  279,  257,   -1,   -1,   -1,
  261,   -1,   -1,   -1,  265,   -1,   -1,   -1,   -1,   -1,
   -1,  272,  273,   -1,   -1,   -1,   -1,  278,  279,
};
}
final static short YYFINAL=3;
final static short YYMAXTOKEN=287;
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
"IGUALIGUAL","DISTINTO","SHORTINT","SINGLEF","CADENA",null,"REPEAT","UNTIL",
"AUTO","COMPTIME","IMPORT","FROM","EXPORT","TO","EXTENDS","TOS","CTE_SHORTINT",
"CTE_SINGLEF",
};
final static String yyrule[] = {
"$accept : programa",
"programa : IDENTIFICADOR sentencias_declarativas BEGIN sentencias_ejecutables END",
"$$1 :",
"programa : error $$1 sentencias_declarativas BEGIN sentencias_ejecutables END",
"$$2 :",
"programa : IDENTIFICADOR sentencias_declarativas error $$2 sentencias_ejecutables END",
"programa : IDENTIFICADOR sentencias_declarativas BEGIN sentencias_ejecutables",
"sentencias_declarativas :",
"sentencias_declarativas : sentencias_declarativas sentencia_declarativa",
"sentencia_declarativa : declaracion_variables",
"sentencia_declarativa : declaracion_funciones",
"sentencia_declarativa : declaracion_clase",
"sentencia_declarativa : declaracion_objeto",
"sentencia_declarativa : declaracion_comptime",
"declaracion_variables : tipo lista_variables ';'",
"declaracion_variables : tipo error ';'",
"lista_variables : IDENTIFICADOR",
"lista_variables : lista_variables ',' IDENTIFICADOR",
"lista_variables : lista_variables IDENTIFICADOR",
"lista_identificadores : IDENTIFICADOR",
"lista_identificadores : lista_identificadores ',' IDENTIFICADOR",
"tipo : SHORTINT",
"tipo : SINGLEF",
"declaracion_funciones : tipo FUNCTION IDENTIFICADOR '(' lista_parametros_formales ')' sentencias_declarativas BEGIN sentencias_ejecutables END ';'",
"$$3 :",
"declaracion_funciones : tipo FUNCTION error $$3 '(' lista_parametros_formales ')' sentencias_declarativas BEGIN sentencias_ejecutables END ';'",
"declaracion_funciones : tipo FUNCTION IDENTIFICADOR '(' lista_parametros_formales ')' sentencias_declarativas BEGIN sentencias_ejecutables END",
"declaracion_funciones : encabezado_auto_funcion BEGIN sentencias_ejecutables END ';'",
"$$4 :",
"declaracion_funciones : AUTO FUNCTION error $$4 '(' lista_parametros_formales ')' sentencias_declarativas BEGIN sentencias_ejecutables END ';'",
"declaracion_funciones : encabezado_auto_funcion BEGIN sentencias_ejecutables END",
"encabezado_auto_funcion : AUTO FUNCTION IDENTIFICADOR '(' lista_parametros_formales ')'",
"lista_parametros_formales :",
"lista_parametros_formales : parametro_formal",
"lista_parametros_formales : lista_parametros_formales ',' parametro_formal",
"parametro_formal : tipo IDENTIFICADOR",
"parametro_formal : tipo error",
"parametro_formal : error IDENTIFICADOR",
"declaracion_clase : CLASS IDENTIFICADOR importacion_opcional BEGIN herencia_opcional miembros_clase END ';'",
"declaracion_clase : CLASS IDENTIFICADOR importacion_opcional BEGIN herencia_opcional miembros_clase END",
"importacion_opcional :",
"importacion_opcional : IMPORT FROM lista_identificadores",
"importacion_opcional : IMPORT lista_identificadores",
"herencia_opcional :",
"herencia_opcional : EXTENDS lista_identificadores ';'",
"herencia_opcional : EXTENDS ';'",
"miembros_clase :",
"miembros_clase : miembros_clase miembro_clase",
"miembro_clase : atributo_clase",
"miembro_clase : metodo_clase",
"atributo_clase : tipo lista_identificadores exportacion_opcional ';'",
"metodo_clase : tipo IDENTIFICADOR '(' lista_parametros_formales ')' sentencias_declarativas BEGIN sentencias_ejecutables END exportacion_opcional ';'",
"metodo_clase : AUTO IDENTIFICADOR '(' lista_parametros_formales ')' BEGIN sentencias_ejecutables END exportacion_opcional ';'",
"exportacion_opcional :",
"exportacion_opcional : EXPORT TO lista_identificadores",
"exportacion_opcional : EXPORT lista_identificadores",
"declaracion_objeto : IDENTIFICADOR lista_identificadores ';'",
"declaracion_objeto : IDENTIFICADOR lista_identificadores",
"declaracion_comptime : COMPTIME tipo lista_identificadores ';'",
"declaracion_comptime : COMPTIME tipo lista_identificadores",
"declaracion_comptime : COMPTIME lista_identificadores ';'",
"declaracion_comptime : COMPTIME lista_identificadores",
"sentencias_ejecutables :",
"sentencias_ejecutables : sentencias_ejecutables sentencia_ejecutable",
"sentencia_ejecutable : asignacion ';'",
"sentencia_ejecutable : expresion ';'",
"sentencia_ejecutable : sentencia_if",
"sentencia_ejecutable : sentencia_repeat_until",
"sentencia_ejecutable : sentencia_pout ';'",
"sentencia_ejecutable : sentencia_ret ';'",
"sentencia_ejecutable : error ';'",
"asignacion : IDENTIFICADOR ASIGNAR expresion",
"asignacion : acceso_posicional ASIGNAR expresion",
"acceso_posicional : IDENTIFICADOR '[' CTE_SHORTINT ']'",
"acceso_posicional : IDENTIFICADOR '[' IDENTIFICADOR ']'",
"acceso_posicional : IDENTIFICADOR '[' CTE_SINGLEF ']'",
"expresion : termino",
"expresion : expresion '+' termino",
"expresion : expresion '-' termino",
"expresion : expresion '+' error",
"expresion : expresion '-' error",
"expresion : '+' termino",
"termino : factor",
"termino : termino '*' factor",
"termino : termino '/' factor",
"termino : termino '*' error",
"termino : termino '/' error",
"termino : '*' factor",
"termino : '/' factor",
"termino : termino IDENTIFICADOR",
"termino : termino CTE_SHORTINT",
"termino : termino CTE_SINGLEF",
"factor : IDENTIFICADOR",
"factor : CTE_SHORTINT",
"factor : CTE_SINGLEF",
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
"unica : IDENTIFICADOR ASIGNAR '(' expresion ')'",
"numero_negativo : '-' CTE_SHORTINT",
"numero_negativo : '-' CTE_SINGLEF",
"conversion_tos : TOS '(' expresion ')'",
"sentencia_if : IF '(' condicion ')' bloque resto_if",
"sentencia_if : IF condicion ')' bloque resto_if",
"sentencia_if : IF error bloque resto_if",
"resto_if : END_IF ';'",
"resto_if : ELSE bloque END_IF ';'",
"resto_if : ';'",
"resto_if : ELSE bloque ';'",
"condicion : expresion comparador expresion",
"comparador : MAYORIGUAL",
"comparador : MENORIGUAL",
"comparador : IGUALIGUAL",
"comparador : DISTINTO",
"comparador : '>'",
"comparador : '<'",
"bloque : BEGIN sentencias_ejecutables END",
"bloque : sentencia_ejecutable",
"bloque : BEGIN sentencias_ejecutables",
"sentencia_repeat_until : REPEAT bloque UNTIL condicion_iteracion ';'",
"sentencia_repeat_until : REPEAT UNTIL condicion_iteracion ';'",
"sentencia_repeat_until : REPEAT bloque '(' condicion ')' ';'",
"sentencia_repeat_until : REPEAT '(' condicion ')' ';'",
"sentencia_repeat_until : REPEAT bloque '(' condicion ';'",
"sentencia_repeat_until : REPEAT '(' condicion ';'",
"condicion_iteracion : '(' condicion ')'",
"condicion_iteracion : condicion ')'",
"condicion_iteracion : '(' condicion",
"condicion_iteracion : condicion",
"sentencia_pout : POUT '(' CADENA ')'",
"sentencia_pout : POUT '(' expresion ')'",
"sentencia_pout : POUT '(' ')'",
"sentencia_ret : RET '(' expresion ')'",
};

//#line 438 "gram.y"


/* CODIGO DE SOPORTE                                                         */

static Parser parser;

static int cant_errores = 0;
static int cant_retornos_auto = -1;

// Muestra por pantalla la regla reconocida y la linea donde termino (al reducir)
static void imprimirRegla(String regla) {
    System.out.println("Regla " + regla + " en linea " + AnalizadorLexico.getLineaActual());
}

public static void main(String[] args) {
    String ruta = "prueba_gramatica";
    boolean debug = false;
    if (args.length > 0) {
        ruta = args[0];
    }
    if (args.length > 1 && args[1].equals("-v")) {
        debug = true;
    }
    System.out.println("Compilando archivo: " + ruta);
    try {
        AnalizadorLexico.reader = new PushbackReader(new BufferedReader(new FileReader(ruta)));
        parser = new Parser(debug);
        parser.yyparse(); 
        TablaSimbolos.imprimirTabla();
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
    if (s.equals("syntax error")) {
        return; // Se omite el mensaje generico de byacc/j para usar las descripciones especificas
    }
    cant_errores++;
    System.out.println("Error sintactico (linea " + AnalizadorLexico.getLineaActual() + "): " + s);
}
//#line 676 "Parser.java"
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
//#line 50 "gram.y"
{ 
            if (cant_errores == 0) {
                System.out.println("Programa reconocido correctamente"); 
            } else {
                System.out.println("Compilacion finalizada con " + cant_errores + " error(es) sintactico(s)");
            }
        }
break;
case 2:
//#line 57 "gram.y"
{ yyerror("Falta el nombre del programa al inicio"); }
break;
case 3:
//#line 58 "gram.y"
{ 
            System.out.println("Compilacion finalizada con " + cant_errores + " error(es) sintactico(s)");
        }
break;
case 4:
//#line 61 "gram.y"
{ yyerror("Falta el delimitador BEGIN de sentencias ejecutables"); }
break;
case 5:
//#line 62 "gram.y"
{
            System.out.println("Compilacion finalizada con " + cant_errores + " error(es) sintactico(s)");
        }
break;
case 6:
//#line 66 "gram.y"
{
            yyerror("Falta el delimitador END al final del programa");
            System.out.println("Compilacion finalizada con " + cant_errores + " error(es) sintactico(s)");
        }
break;
case 9:
//#line 80 "gram.y"
{ imprimirRegla("declaracion_variables"); }
break;
case 10:
//#line 81 "gram.y"
{ imprimirRegla("declaracion_funciones"); }
break;
case 11:
//#line 82 "gram.y"
{ imprimirRegla("declaracion_clase"); }
break;
case 12:
//#line 83 "gram.y"
{ imprimirRegla("declaracion_objeto"); }
break;
case 13:
//#line 84 "gram.y"
{ imprimirRegla("declaracion_comptime"); }
break;
case 15:
//#line 91 "gram.y"
{ yyerror("Falta ';' al final de la declaracion de variables"); }
break;
case 18:
//#line 97 "gram.y"
{ yyerror("Falta ',' entre los identificadores"); }
break;
case 24:
//#line 117 "gram.y"
{ yyerror("Falta nombre de funcion"); }
break;
case 26:
//#line 123 "gram.y"
{ yyerror("Falta ';' al final de la declaracion de funcion"); }
break;
case 27:
//#line 125 "gram.y"
{
            if (cant_retornos_auto == 0) {
                yyerror("Ausencia de sentencia de retorno 'RET' en funcion AUTO");
            }
            cant_retornos_auto = -1;
        }
break;
case 28:
//#line 131 "gram.y"
{ yyerror("Falta nombre de funcion"); }
break;
case 30:
//#line 135 "gram.y"
{
            if (cant_retornos_auto == 0) {
                yyerror("Ausencia de sentencia de retorno 'RET' en funcion AUTO");
            }
            cant_retornos_auto = -1;
            yyerror("Falta ';' al final de la declaracion de funcion");
        }
break;
case 31:
//#line 146 "gram.y"
{ cant_retornos_auto = 0; }
break;
case 35:
//#line 156 "gram.y"
{ imprimirRegla("parametro_formal"); }
break;
case 36:
//#line 157 "gram.y"
{ yyerror("Falta de nombre de parametro formal en declaracion de funcion"); }
break;
case 37:
//#line 158 "gram.y"
{ yyerror("Falta de tipo del parametro formal en declaracion de funcion"); }
break;
case 39:
//#line 169 "gram.y"
{ yyerror("Falta ';' al final de la declaracion de clase"); }
break;
case 41:
//#line 174 "gram.y"
{ imprimirRegla("importacion_opcional"); }
break;
case 42:
//#line 176 "gram.y"
{ yyerror("Falta la palabra clave 'FROM' en la declaracion IMPORT"); }
break;
case 44:
//#line 181 "gram.y"
{ imprimirRegla("herencia_opcional"); }
break;
case 45:
//#line 183 "gram.y"
{ yyerror("Falta nombre o lista de clases a heredar luego de 'EXTENDS'"); }
break;
case 54:
//#line 210 "gram.y"
{ imprimirRegla("exportacion_opcional"); }
break;
case 55:
//#line 212 "gram.y"
{ yyerror("Falta la palabra clave 'TO' en la declaracion EXPORT"); }
break;
case 56:
//#line 218 "gram.y"
{ imprimirRegla("declaracion_de_objeto"); }
break;
case 57:
//#line 220 "gram.y"
{ yyerror("Falta ';' al final de la declaracion de objeto"); }
break;
case 59:
//#line 228 "gram.y"
{ yyerror("Falta ';' al final de la declaracion comptime"); }
break;
case 60:
//#line 230 "gram.y"
{ yyerror("Falta el tipo de dato en la declaracion comptime"); }
break;
case 61:
//#line 232 "gram.y"
{
            yyerror("Falta el tipo de dato en la declaracion comptime");
            yyerror("Falta ';' al final de la declaracion comptime");
        }
break;
case 63:
//#line 242 "gram.y"
{ imprimirRegla("sentencia_ejecutable"); }
break;
case 70:
//#line 253 "gram.y"
{ yyerror("Sentencia ejecutable malformada o falta ';' previo"); }
break;
case 75:
//#line 265 "gram.y"
{ yyerror("Acceso posicional invalido: solo se permite constante SHORTINT positiva o identificador"); }
break;
case 79:
//#line 273 "gram.y"
{ yyerror("Falta un operando en la expresion"); }
break;
case 80:
//#line 274 "gram.y"
{ yyerror("Falta un operando en la expresion"); }
break;
case 81:
//#line 275 "gram.y"
{ yyerror("Falta un operando en la expresion"); }
break;
case 85:
//#line 281 "gram.y"
{ yyerror("Falta un operando en la expresion"); }
break;
case 86:
//#line 282 "gram.y"
{ yyerror("Falta un operando en la expresion"); }
break;
case 87:
//#line 283 "gram.y"
{ yyerror("Falta un operando en la expresion"); }
break;
case 88:
//#line 284 "gram.y"
{ yyerror("Falta un operando en la expresion"); }
break;
case 89:
//#line 285 "gram.y"
{ yyerror("Falta operador en la expresion"); }
break;
case 90:
//#line 286 "gram.y"
{ yyerror("Falta operador en la expresion"); }
break;
case 91:
//#line 287 "gram.y"
{ yyerror("Falta operador en la expresion"); }
break;
case 93:
//#line 293 "gram.y"
{
            int id = val_peek(0).ival;
            String lexema = TablaSimbolos.obtenerAtributo(id, TablaSimbolos.LEXEMA);
            int valor = Integer.parseInt(lexema);
            if (valor > AnalizadorLexico.ValorMaximoInt) {
                yyerror("Constante shortint positiva fuera de rango (" + lexema + "). Rango permitido: [-128, 127]");
            }
        }
break;
case 94:
//#line 302 "gram.y"
{
            int id = val_peek(0).ival;
            String lexema = TablaSimbolos.obtenerAtributo(id, TablaSimbolos.LEXEMA);
            double valor = Double.parseDouble(lexema);
            if (valor != 0.0 && (valor < AnalizadorLexico.ValorMinimoFloat || valor > AnalizadorLexico.ValorMaximoFloat)) {
                yyerror("Constante singlef fuera de rango (" + lexema + ")");
            }
        }
break;
case 102:
//#line 320 "gram.y"
{ imprimirRegla("invocacion_metodo"); }
break;
case 103:
//#line 321 "gram.y"
{ imprimirRegla("invocacion_metodo"); }
break;
case 107:
//#line 333 "gram.y"
{ yyerror("Uso del simbolo ':=' donde debe usarse '=' en asignacion dentro de expresion"); }
break;
case 108:
//#line 338 "gram.y"
{
            int id_pos = val_peek(0).ival;
            String lexema_pos = TablaSimbolos.obtenerAtributo(id_pos, TablaSimbolos.LEXEMA);
            int id_neg = TablaSimbolos.convertirANegativo(lexema_pos);
            yyval.ival = id_neg;
        }
break;
case 109:
//#line 345 "gram.y"
{
            int id_pos = val_peek(0).ival;
            String lexema_pos = TablaSimbolos.obtenerAtributo(id_pos, TablaSimbolos.LEXEMA);
            int id_neg = TablaSimbolos.convertirANegativo(lexema_pos);
            yyval.ival = id_neg;
        }
break;
case 112:
//#line 362 "gram.y"
{ yyerror("Falta '(' de apertura en condicion de seleccion"); }
break;
case 113:
//#line 364 "gram.y"
{ yyerror("Condicion de seleccion malformada o error en parentesis"); }
break;
case 116:
//#line 371 "gram.y"
{ yyerror("Falta palabra clave END_IF al final de la sentencia IF"); }
break;
case 117:
//#line 373 "gram.y"
{ yyerror("Falta palabra clave END_IF al final de la sentencia IF"); }
break;
case 127:
//#line 393 "gram.y"
{ yyerror("Falta el delimitador END en el bloque de sentencias"); }
break;
case 129:
//#line 400 "gram.y"
{ yyerror("Falta el cuerpo de la iteracion REPEAT"); }
break;
case 130:
//#line 402 "gram.y"
{ yyerror("Falta palabra clave UNTIL en la iteracion REPEAT"); }
break;
case 131:
//#line 404 "gram.y"
{ yyerror("Falta palabra clave UNTIL y cuerpo en la iteracion REPEAT"); }
break;
case 132:
//#line 406 "gram.y"
{ yyerror("Falta palabra clave UNTIL y ')' en la iteracion REPEAT"); }
break;
case 133:
//#line 408 "gram.y"
{ yyerror("Falta palabra clave UNTIL, cuerpo y ')' en la iteracion REPEAT"); }
break;
case 135:
//#line 414 "gram.y"
{ yyerror("Falta '(' de apertura en condicion de iteracion"); }
break;
case 136:
//#line 416 "gram.y"
{ yyerror("Falta ')' de cierre en condicion de iteracion"); }
break;
case 137:
//#line 418 "gram.y"
{ yyerror("Faltan parentesis en condicion de iteracion"); }
break;
case 140:
//#line 426 "gram.y"
{ yyerror("Falta argumento en sentencia pout"); }
break;
case 141:
//#line 431 "gram.y"
{
            if (cant_retornos_auto >= 0) {
                cant_retornos_auto++;
            }
        }
break;
//#line 1156 "Parser.java"
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
