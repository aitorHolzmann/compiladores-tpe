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
    0,    3,    0,    4,    0,    0,    1,    1,    5,    5,
    5,    5,    5,    6,    6,   12,   12,   12,   13,   13,
   11,   11,    7,   15,    7,    7,    7,   17,    7,    7,
   16,   14,   14,   14,   18,   18,   18,    8,    8,   19,
   19,   19,   20,   20,   20,   21,   21,   22,   22,   23,
   24,   24,   25,   25,   25,    9,    9,   10,   10,   10,
   10,    2,    2,   26,   26,   26,   26,   26,   26,   26,
   27,   27,   33,   33,   28,   28,   28,   28,   28,   28,
   34,   34,   34,   34,   34,   34,   34,   34,   34,   35,
   35,   35,   35,   35,   35,   35,   35,   35,   37,   37,
   36,   36,   38,   38,   39,   40,   29,   29,   29,   43,
   43,   43,   43,   41,   44,   44,   44,   44,   44,   44,
   42,   42,   42,   30,   30,   30,   30,   30,   30,   45,
   45,   45,   45,   31,   31,   31,   32,
};
final static short yylen[] = {                            2,
    5,    0,    6,    0,    6,    4,    0,    2,    1,    1,
    1,    1,    1,    3,    3,    1,    3,    2,    1,    3,
    1,    1,   11,    0,   12,   10,    5,    0,   12,    4,
    6,    0,    1,    3,    2,    2,    2,    8,    7,    0,
    3,    2,    0,    3,    2,    0,    2,    1,    1,    4,
   11,   10,    0,    3,    2,    3,    2,    4,    3,    3,
    2,    0,    2,    2,    2,    1,    1,    2,    2,    2,
    3,    3,    4,    4,    1,    3,    3,    3,    3,    2,
    1,    3,    3,    3,    3,    2,    2,    2,    2,    1,
    1,    3,    4,    1,    1,    1,    1,    1,    5,    6,
    1,    3,    5,    5,    2,    4,    6,    5,    4,    2,
    4,    1,    3,    3,    1,    1,    1,    1,    1,    1,
    3,    1,    2,    5,    4,    6,    5,    5,    4,    3,
    2,    2,    1,    4,    4,    3,    4,
};
final static short yydefred[] = {                         0,
    2,    7,    0,    7,    0,    0,    4,    0,   62,    0,
   21,   22,    0,    0,    8,    9,   10,   11,   12,   13,
    0,    0,   62,   62,   19,    0,    0,    0,    0,    0,
    0,    0,   16,    0,    0,   62,    0,    0,   56,    0,
    0,    0,    0,    1,    0,    0,   91,    0,    0,    0,
    0,    0,    0,   63,    0,    0,   66,   67,    0,    0,
    0,    0,   81,   94,   95,   96,   97,    0,    0,   28,
    0,    0,   60,   15,   24,    0,   18,   14,    0,    0,
    3,    5,   20,   70,    0,    0,    0,    0,    0,    0,
    0,    0,    0,   98,    0,    0,    0,   62,    0,    0,
  122,    0,    0,    0,  105,   86,   87,   64,    0,    0,
   65,   68,   69,    0,   88,   89,    0,    0,    0,    0,
    0,    0,    0,   58,    0,    0,   17,    0,    0,    0,
   92,    0,    0,    0,    0,    0,    0,    0,    0,    0,
  115,  116,  117,  118,  119,  120,    0,    0,    0,  136,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
   78,    0,   79,    0,    0,   84,   82,   85,   83,    0,
    0,   46,    0,    0,    0,    0,   33,    0,    0,   27,
    0,    0,   93,   74,   73,    0,    0,    0,    0,  112,
  109,    0,    0,    0,  134,  135,  137,  121,    0,  131,
  125,  129,    0,    0,    0,  106,   45,    0,    0,    0,
   37,   36,   35,    0,   31,    0,    7,  104,    0,   99,
    0,  103,    0,  110,    0,  108,  130,  127,  124,  128,
    0,   44,    0,    0,    0,   47,   48,   49,    7,   34,
    7,    0,  100,    0,  113,  107,  126,   38,    0,    0,
    0,    0,    0,   62,  111,    0,    0,    0,    0,   62,
   62,    0,    0,    0,    0,    0,   50,    0,    0,    0,
    0,    7,    0,    0,    0,   23,   62,    0,   29,   25,
    0,   62,    0,    0,    0,    0,   52,    0,   51,
};
final static short yydgoto[] = {                          3,
    5,   27,    4,   24,   15,   16,   17,   18,   19,   20,
  175,   35,   26,  176,  125,   22,  122,  177,   69,  172,
  209,  236,  237,  238,  259,   54,   55,   56,   57,   58,
   59,   60,   94,   62,   63,  133,   64,   65,   66,   67,
  155,  102,  191,  147,  156,
};
final static short yysindex[] = {                      -166,
    0,    0,    0,    0,  336,  451,    0, -241,    0, -214,
    0,    0, -218,  105,    0,    0,    0,    0,    0,    0,
  -38, -208,    0,    0,    0,   76,   66, -222, -127, -241,
   98,    8,    0,  -58,  -31,    0,   81,   96,    0, -186,
   39,   14,  -13,    0,   41,   46,    0,   19,   63,  258,
 -165,   12,   12,    0,   75,  143,    0,    0,   91,  102,
 -142,   90,    0,    0,    0,    0,    0, -242, -106,    0,
  128,  135,    0,    0,    0,  133,    0,    0,  -42,  111,
    0,    0,    0,    0,   -7,   35, -220,  -27,  196,   50,
  509,  150,  -37,    0,  224,   27,  150,    0,    4,  150,
    0,  -21,  150,   90,    0,    0,    0,    0,  167,  175,
    0,    0,    0,  150,    0,    0,  107,  229, -241,  222,
  -14,  251, -108,    0,  253, -108,    0,  239,  150,  117,
    0,  117,  277,  216,  226,  300,  150,  -52,  315,  329,
    0,    0,    0,    0,    0,    0,  150,   50,  335,    0,
  291,  305,  127,  150,  339,  342,   47,    4,  150,  352,
    0,   90,    0,   90,  117,    0,    0,    0,    0,  222,
  -18,    0, -108,  131,  -46,  407,    0, -108,  412,    0,
  368,  150,    0,    0,    0,   42,  385,   50,  349,    0,
    0,   50,  117,  -52,    0,    0,    0,    0,  353,    0,
    0,    0,  351,  377,   53,    0,    0,  162,  -82,  418,
    0,    0,    0, -108,    0,  420,    0,    0,  117,    0,
  469,    0,  -47,    0,  -52,    0,    0,    0,    0,    0,
  378,    0,  382,  172,  192,    0,    0,    0,    0,    0,
    0,  506,    0,  395,    0,    0,    0,    0,  379,  423,
  -22,  508,  531,    0,    0, -108, -108, -233,  413,    0,
    0,  142,  496,  513, -241,  222,    0,  158,  182,  414,
  221,    0,  222,  425,  432,    0,    0,  533,    0,    0,
  212,    0,  220,  243,  444,  220,    0,  453,    0,
};
final static short yyrindex[] = {                         0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,  360,  516,  256,    0,    0,
  370,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,   74,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
  104,  449,    0,    0,    0,    0,    0,    0,    0,    0,
    0,  394,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
  436,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,  479,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,  260,
  193,    0,  515,    0,    0,  515,    0,  397,    0,  467,
    0,  517,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,   56,    0,  483,    0,    0,    0,    0,    0,
    0,  486,    0,  491,  485,    0,    0,    0,    0,  303,
    0,    0,  515,    0,    0,    0,    0,  515,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,   86,    0,    0,    0,    0,    0,  493,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,  519,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,  422,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,  -33,
  507,    0,    0,    0,    0,  515,  515,    0,    0,    0,
    0,    0,    0,    0,    0,  510,    0,    0,    0,  424,
    0,    0,  512,    0,    0,    0,    0,    0,    0,    0,
    0,    0,  507,    0,    0,  507,    0,    0,    0,
};
final static short yygindex[] = {                         0,
   -1,  -19,    0,    0,    0,    0,    0,    0,    0,    0,
   -5,    0,  -12,  -74,    0,    0,    0,  358,    0,    0,
    0,    0,    0,    0,  279,  399,    0,  482,    0,    0,
    0,    0,  -17,   68,   49,  387,    0,    0,    0,    0,
   22,  -48, -180,    0,  417,
};
final static int YYTABLESIZE=812;
static short yytable[];
static { yytable();}
static void yytable(){
yytable = new short[]{                         21,
   21,   31,    6,   37,   38,  109,  190,  110,   30,   61,
   19,  245,   79,  226,   25,   25,   80,   72,  159,   61,
   61,   40,  146,   25,  145,   19,   92,   78,   52,   50,
   61,   51,  129,   53,   52,   50,  134,   51,  119,   53,
  207,  138,   28,  154,  246,   52,   50,   29,   51,  265,
   53,  179,   36,   86,  135,  120,   51,   68,  100,   88,
   52,   50,   61,   51,   95,   53,   74,  150,   52,   50,
   83,   51,   61,   53,   89,  131,   52,   50,  153,   51,
   96,   53,  220,   52,   50,   97,   51,  203,   53,    1,
    2,   52,   50,  231,   51,  123,   53,   84,  210,  194,
  106,  107,  103,  216,   87,  202,  170,   52,   50,  105,
   51,  230,   53,  140,  123,   90,   90,  104,   90,   40,
   90,  157,   52,   50,  114,   51,  114,   53,   70,   71,
   61,  117,   90,  108,   39,   61,  118,   52,   50,  223,
   51,   40,   53,  225,  114,   98,   98,  174,   98,  112,
   98,   51,   52,   50,  121,   51,   73,   53,  208,  109,
  113,  110,   98,   11,   12,  167,  169,  123,   52,   50,
   61,   51,  126,   53,   61,  199,  162,  164,   40,  233,
  205,  263,  264,   52,   50,  109,   51,  110,   53,   11,
   12,   52,   50,  124,   51,  234,   53,   75,   76,   52,
   50,  111,   51,  235,   53,   40,  188,  189,   52,  212,
  213,   51,  244,   53,  127,  242,   52,   32,   33,   51,
  232,   53,  251,   52,   50,   77,   51,   34,   53,  136,
  141,  142,  143,  144,  262,  137,   21,  252,   25,  253,
  268,  269,   90,   91,   61,  266,   21,   21,   19,   91,
   61,   61,  273,   52,   50,  158,   51,  281,   53,  258,
   91,   47,  284,   61,  148,   40,   61,   47,   91,  171,
  278,   49,   21,   51,   41,   42,   43,   49,   47,   98,
   85,   45,   46,   91,   52,   50,   47,   51,   49,   53,
  173,   91,  178,   47,   48,   99,   49,  180,   91,   52,
  149,   47,   51,   49,   53,   41,   42,   43,  184,   47,
   98,   49,   45,   46,  123,  123,   47,  183,  185,   49,
  182,   41,   42,   43,   47,   48,   49,   44,   45,   46,
   90,  196,  123,  109,   49,  110,   41,   42,   43,  186,
   47,   48,   81,   45,   46,  197,  115,  109,   90,  110,
   49,   41,   42,   43,  129,   47,   48,   82,   45,   46,
   98,   25,  166,   91,  116,   49,   41,   42,   43,  192,
   47,   48,  128,   45,   46,  195,   11,   12,   98,  200,
   49,   47,   41,   42,   43,   47,   48,  211,  198,   45,
   46,   49,  206,  227,  109,   49,  110,   41,   42,   43,
  201,   47,   48,  270,   45,   46,   91,  224,  218,  228,
  109,   49,  110,   41,   42,   43,   47,   48,  256,  274,
   45,   46,  161,   91,   47,  222,   49,  109,  249,  110,
  163,   91,   47,   48,   49,  229,  247,   41,   42,   43,
  248,   47,   49,  275,   45,   46,  101,  215,  250,   47,
  214,   49,  217,  255,   43,  214,   47,   48,  239,   49,
  241,  214,  257,  214,   43,   43,   49,   41,   42,   43,
   43,  267,  276,  283,   45,   46,   90,   90,   90,   90,
   90,  277,   90,  279,  168,   91,   47,   48,  101,   75,
  280,   75,   75,   75,   90,   90,   49,   90,   41,   42,
   43,  258,  287,   47,  286,   45,   46,   75,   75,  243,
   75,  289,  182,   49,   91,    6,   40,   47,   48,   80,
   42,   80,   80,   80,   93,   71,   76,   49,   76,   76,
   76,   77,   47,   77,   77,   77,  271,   80,   80,  214,
   80,  133,   49,   72,   76,   76,  101,   76,   86,   77,
   77,  132,   77,  272,   88,   32,  214,  101,   32,  102,
  101,  285,  102,   41,  288,   53,  130,  132,   55,   89,
   54,  240,  221,   93,  204,    0,    0,  151,  152,    0,
   93,   93,    0,    0,  160,    0,  101,    0,    0,    0,
  101,    7,    8,    0,    0,  165,    9,    0,    0,   87,
   10,    0,    0,    0,    0,    0,    0,   11,   12,    0,
  181,    0,    0,   13,   14,   57,   57,    0,  187,    0,
   57,    0,    0,    0,   57,   61,   61,    0,  193,    0,
   61,   57,   57,    0,   61,   93,    0,   57,   57,   93,
   93,   61,   61,    0,    0,    0,    0,   61,   61,   59,
   59,    0,   30,   30,   59,    0,    0,   30,   59,    0,
    0,   30,    0,  219,    0,   59,   59,  132,   30,   30,
    0,   59,   59,    0,   30,   30,    0,   39,   39,   26,
   26,    0,   39,    0,   26,    0,   39,    0,   26,    0,
    0,    0,   90,   39,   39,   26,   26,    0,    0,   39,
   39,   26,   26,   90,   90,   90,   90,    8,    0,    0,
   90,   23,    0,    0,    0,   10,   75,   75,   75,   75,
    0,    0,   11,   12,    0,    0,    0,    0,   13,   14,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,   80,   80,   80,   80,
    0,    0,    0,   76,   76,   76,   76,    0,   77,   77,
   77,   77,    8,    0,    8,    0,  254,    0,  260,    0,
   10,    0,   10,    0,    0,  139,    0,   11,   12,   11,
   12,    0,    0,   13,   14,   13,   14,    8,    0,    8,
    0,  261,    0,  282,    0,   10,    0,   10,    0,    0,
    0,    0,   11,   12,   11,   12,    0,    0,   13,   14,
   13,   14,
};
}
static short yycheck[];
static { yycheck(); }
static void yycheck() {
yycheck = new short[] {                          5,
    6,   14,    4,   23,   24,   43,   59,   45,   14,   27,
   44,   59,   44,  194,  257,  257,   36,   30,   40,   37,
   38,   44,   60,  257,   62,   59,   40,   59,   42,   43,
   48,   45,   40,   47,   42,   43,  257,   45,  281,   47,
   59,   90,  257,   40,  225,   42,   43,  266,   45,  283,
   47,  126,  261,   40,  275,   68,   45,  280,   40,   46,
   42,   43,   80,   45,   43,   47,   59,   41,   42,   43,
  257,   45,   90,   47,   61,   41,   42,   43,   98,   45,
   40,   47,   41,   42,   43,   40,   45,   41,   47,  256,
  257,   42,   43,   41,   45,   40,   47,   59,  173,  148,
   52,   53,   40,  178,   91,   59,  119,   42,   43,  275,
   45,   59,   47,   92,   59,   42,   43,   50,   45,   44,
   47,  100,   42,   43,  267,   45,   41,   47,  256,  257,
  148,   42,   59,   59,   59,  153,   47,   42,   43,  188,
   45,   44,   47,  192,   59,   42,   43,  256,   45,   59,
   47,   45,   42,   43,  261,   45,   59,   47,  171,   43,
   59,   45,   59,  272,  273,  117,  118,   40,   42,   43,
  188,   45,   40,   47,  192,  154,  109,  110,   44,  262,
  159,  256,  257,   42,   43,   43,   45,   45,   47,  272,
  273,   42,   43,   59,   45,  278,   47,  256,  257,   42,
   43,   59,   45,  209,   47,   44,  259,  260,   42,  256,
  257,   45,  260,   47,  257,  217,   42,  256,  257,   45,
   59,   47,  235,   42,   43,  257,   45,  266,   47,  257,
  268,  269,  270,  271,  254,   40,  242,  239,  257,  241,
  260,  261,  256,  257,  262,  258,  252,  253,  282,  257,
  268,  269,  265,   42,   43,  277,   45,  277,   47,  282,
  257,  275,  282,  281,   41,   44,  284,  275,  257,  284,
  272,  285,  278,   45,  256,  257,  258,  285,  275,  261,
  267,  263,  264,  257,   42,   43,  275,   45,  285,   47,
   40,  257,   40,  275,  276,  277,  285,   59,  257,   42,
  274,  275,   45,  285,   47,  256,  257,  258,   93,  275,
  261,  285,  263,  264,  259,  260,  275,   41,   93,  285,
   44,  256,  257,  258,  275,  276,  285,  262,  263,  264,
  257,   41,  277,   43,  285,   45,  256,  257,  258,   40,
  275,  276,  262,  263,  264,   41,  257,   43,  275,   45,
  285,  256,  257,  258,   40,  275,  276,  262,  263,  264,
  257,  257,  256,  257,  275,  285,  256,  257,  258,   41,
  275,  276,  262,  263,  264,   41,  272,  273,  275,   41,
  285,  275,  256,  257,  258,  275,  276,  257,  262,  263,
  264,  285,   41,   41,   43,  285,   45,  256,  257,  258,
   59,  275,  276,  262,  263,  264,  257,   59,   41,   59,
   43,  285,   45,  256,  257,  258,  275,  276,   40,  262,
  263,  264,  256,  257,  275,   41,  285,   43,  257,   45,
  256,  257,  275,  276,  285,   59,   59,  256,  257,  258,
   59,  275,  285,  262,  263,  264,   48,   41,  257,  275,
   44,  285,   41,   59,  262,   44,  275,  276,   41,  285,
   41,   44,   40,   44,  272,  273,  285,  256,  257,  258,
  278,   59,   59,  262,  263,  264,   41,   42,   43,   44,
   45,  261,   47,   59,  256,  257,  275,  276,   90,   41,
   59,   43,   44,   45,   59,   60,  285,   62,  256,  257,
  258,  282,   59,  275,  262,  263,  264,   59,   60,   41,
   62,   59,   44,  285,  257,    0,  261,  275,  276,   41,
  261,   43,   44,   45,   43,   59,   41,  285,   43,   44,
   45,   41,  275,   43,   44,   45,   41,   59,   60,   44,
   62,   59,  285,   59,   59,   60,  148,   62,   40,   59,
   60,   59,   62,   41,   46,   41,   44,   41,   44,   41,
   44,  283,   44,  261,  286,   59,   85,   86,   59,   61,
   59,  214,  186,   92,  158,   -1,   -1,   96,   97,   -1,
   99,  100,   -1,   -1,  103,   -1,  188,   -1,   -1,   -1,
  192,  256,  257,   -1,   -1,  114,  261,   -1,   -1,   91,
  265,   -1,   -1,   -1,   -1,   -1,   -1,  272,  273,   -1,
  129,   -1,   -1,  278,  279,  256,  257,   -1,  137,   -1,
  261,   -1,   -1,   -1,  265,  256,  257,   -1,  147,   -1,
  261,  272,  273,   -1,  265,  154,   -1,  278,  279,  158,
  159,  272,  273,   -1,   -1,   -1,   -1,  278,  279,  256,
  257,   -1,  256,  257,  261,   -1,   -1,  261,  265,   -1,
   -1,  265,   -1,  182,   -1,  272,  273,  186,  272,  273,
   -1,  278,  279,   -1,  278,  279,   -1,  256,  257,  256,
  257,   -1,  261,   -1,  261,   -1,  265,   -1,  265,   -1,
   -1,   -1,  257,  272,  273,  272,  273,   -1,   -1,  278,
  279,  278,  279,  268,  269,  270,  271,  257,   -1,   -1,
  275,  261,   -1,   -1,   -1,  265,  268,  269,  270,  271,
   -1,   -1,  272,  273,   -1,   -1,   -1,   -1,  278,  279,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,  268,  269,  270,  271,
   -1,   -1,   -1,  268,  269,  270,  271,   -1,  268,  269,
  270,  271,  257,   -1,  257,   -1,  261,   -1,  261,   -1,
  265,   -1,  265,   -1,   -1,  267,   -1,  272,  273,  272,
  273,   -1,   -1,  278,  279,  278,  279,  257,   -1,  257,
   -1,  261,   -1,  261,   -1,  265,   -1,  265,   -1,   -1,
   -1,   -1,  272,  273,  272,  273,   -1,   -1,  278,  279,
  278,  279,
};
}
final static short YYFINAL=3;
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
"acceso_posicional : IDENTIFICADOR '[' CONSTANTE ']'",
"acceso_posicional : IDENTIFICADOR '[' IDENTIFICADOR ']'",
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
"termino : termino CONSTANTE",
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
"unica : IDENTIFICADOR ASIGNAR '(' expresion ')'",
"numero_negativo : '-' CONSTANTE",
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

//#line 427 "gram.y"

/* CODIGO DE SOPORTE                                                         */

static Parser parser;

static int cant_errores = 0;
static int cant_retornos_auto = -1;

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
//#line 644 "Parser.java"
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
//#line 49 "gram.y"
{ 
            if (cant_errores == 0) {
                System.out.println("Programa reconocido correctamente"); 
            } else {
                System.out.println("Compilacion finalizada con " + cant_errores + " error(es) sintactico(s)");
            }
        }
break;
case 2:
//#line 56 "gram.y"
{ yyerror("Falta el nombre del programa al inicio"); }
break;
case 3:
//#line 57 "gram.y"
{ 
            System.out.println("Compilacion finalizada con " + cant_errores + " error(es) sintactico(s)");
        }
break;
case 4:
//#line 60 "gram.y"
{ yyerror("Falta el delimitador BEGIN de sentencias ejecutables"); }
break;
case 5:
//#line 61 "gram.y"
{
            System.out.println("Compilacion finalizada con " + cant_errores + " error(es) sintactico(s)");
        }
break;
case 6:
//#line 65 "gram.y"
{
            yyerror("Falta el delimitador END al final del programa");
            System.out.println("Compilacion finalizada con " + cant_errores + " error(es) sintactico(s)");
        }
break;
case 15:
//#line 90 "gram.y"
{ yyerror("Falta ';' al final de la declaracion de variables"); }
break;
case 18:
//#line 96 "gram.y"
{ yyerror("Falta ',' entre los identificadores"); }
break;
case 24:
//#line 116 "gram.y"
{ yyerror("Falta nombre de funcion"); }
break;
case 26:
//#line 122 "gram.y"
{ yyerror("Falta ';' al final de la declaracion de funcion"); }
break;
case 27:
//#line 124 "gram.y"
{
            if (cant_retornos_auto == 0) {
                yyerror("Ausencia de sentencia de retorno 'RET' en funcion AUTO");
            }
            cant_retornos_auto = -1;
        }
break;
case 28:
//#line 130 "gram.y"
{ yyerror("Falta nombre de funcion"); }
break;
case 30:
//#line 134 "gram.y"
{
            if (cant_retornos_auto == 0) {
                yyerror("Ausencia de sentencia de retorno 'RET' en funcion AUTO");
            }
            cant_retornos_auto = -1;
            yyerror("Falta ';' al final de la declaracion de funcion");
        }
break;
case 31:
//#line 145 "gram.y"
{ cant_retornos_auto = 0; }
break;
case 36:
//#line 156 "gram.y"
{ yyerror("Falta de nombre de parametro formal en declaracion de funcion"); }
break;
case 37:
//#line 157 "gram.y"
{ yyerror("Falta de tipo del parametro formal en declaracion de funcion"); }
break;
case 39:
//#line 168 "gram.y"
{ yyerror("Falta ';' al final de la declaracion de clase"); }
break;
case 42:
//#line 175 "gram.y"
{ yyerror("Falta la palabra clave 'FROM' en la declaracion IMPORT"); }
break;
case 45:
//#line 182 "gram.y"
{ yyerror("Falta nombre o lista de clases a heredar luego de 'EXTENDS'"); }
break;
case 55:
//#line 211 "gram.y"
{ yyerror("Falta la palabra clave 'TO' en la declaracion EXPORT"); }
break;
case 57:
//#line 219 "gram.y"
{ yyerror("Falta ';' al final de la declaracion de objeto"); }
break;
case 59:
//#line 227 "gram.y"
{ yyerror("Falta ';' al final de la declaracion comptime"); }
break;
case 60:
//#line 229 "gram.y"
{ yyerror("Falta el tipo de dato en la declaracion comptime"); }
break;
case 61:
//#line 231 "gram.y"
{
            yyerror("Falta el tipo de dato en la declaracion comptime");
            yyerror("Falta ';' al final de la declaracion comptime");
        }
break;
case 70:
//#line 252 "gram.y"
{ yyerror("Sentencia ejecutable malformada o falta ';' previo"); }
break;
case 78:
//#line 270 "gram.y"
{ yyerror("Falta un operando en la expresion"); }
break;
case 79:
//#line 271 "gram.y"
{ yyerror("Falta un operando en la expresion"); }
break;
case 80:
//#line 272 "gram.y"
{ yyerror("Falta un operando en la expresion"); }
break;
case 84:
//#line 278 "gram.y"
{ yyerror("Falta un operando en la expresion"); }
break;
case 85:
//#line 279 "gram.y"
{ yyerror("Falta un operando en la expresion"); }
break;
case 86:
//#line 280 "gram.y"
{ yyerror("Falta un operando en la expresion"); }
break;
case 87:
//#line 281 "gram.y"
{ yyerror("Falta un operando en la expresion"); }
break;
case 88:
//#line 282 "gram.y"
{ yyerror("Falta operador en la expresion"); }
break;
case 89:
//#line 283 "gram.y"
{ yyerror("Falta operador en la expresion"); }
break;
case 91:
//#line 289 "gram.y"
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
case 104:
//#line 328 "gram.y"
{ yyerror("Uso del simbolo ':=' donde debe usarse '=' en asignacion dentro de expresion"); }
break;
case 105:
//#line 333 "gram.y"
{
            int id_pos = val_peek(0).ival;
            String lexema_pos = TablaSimbolos.obtenerAtributo(id_pos, TablaSimbolos.LEXEMA);
            int id_neg = TablaSimbolos.convertirANegativo(lexema_pos);
            yyval.ival = id_neg;
        }
break;
case 108:
//#line 350 "gram.y"
{ yyerror("Falta '(' de apertura en condicion de seleccion"); }
break;
case 109:
//#line 352 "gram.y"
{ yyerror("Condicion de seleccion malformada o error en parentesis"); }
break;
case 112:
//#line 359 "gram.y"
{ yyerror("Falta palabra clave END_IF al final de la sentencia IF"); }
break;
case 113:
//#line 361 "gram.y"
{ yyerror("Falta palabra clave END_IF al final de la sentencia IF"); }
break;
case 123:
//#line 381 "gram.y"
{ yyerror("Falta el delimitador END en el bloque de sentencias"); }
break;
case 125:
//#line 388 "gram.y"
{ yyerror("Falta el cuerpo de la iteracion REPEAT"); }
break;
case 126:
//#line 390 "gram.y"
{ yyerror("Falta palabra clave UNTIL en la iteracion REPEAT"); }
break;
case 127:
//#line 392 "gram.y"
{ yyerror("Falta palabra clave UNTIL y cuerpo en la iteracion REPEAT"); }
break;
case 128:
//#line 394 "gram.y"
{ yyerror("Falta palabra clave UNTIL y ')' en la iteracion REPEAT"); }
break;
case 129:
//#line 396 "gram.y"
{ yyerror("Falta palabra clave UNTIL, cuerpo y ')' en la iteracion REPEAT"); }
break;
case 131:
//#line 402 "gram.y"
{ yyerror("Falta '(' de apertura en condicion de iteracion"); }
break;
case 132:
//#line 404 "gram.y"
{ yyerror("Falta ')' de cierre en condicion de iteracion"); }
break;
case 133:
//#line 406 "gram.y"
{ yyerror("Faltan parentesis en condicion de iteracion"); }
break;
case 136:
//#line 414 "gram.y"
{ yyerror("Falta argumento en sentencia pout"); }
break;
case 137:
//#line 419 "gram.y"
{
            if (cant_retornos_auto >= 0) {
                cant_retornos_auto++;
            }
        }
break;
//#line 1052 "Parser.java"
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
