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
    5,    5,    5,    6,    6,   12,   12,   11,   11,    7,
   14,    7,    7,    7,   15,    7,    7,   13,   13,   13,
   16,    8,    8,   17,   17,   18,   18,   19,   19,   20,
   20,   21,   22,   22,   23,   23,    9,    9,   10,   10,
    2,    2,   24,   24,   24,   24,   24,   24,   24,   25,
   25,   31,   31,   26,   26,   26,   32,   32,   32,   33,
   33,   33,   33,   33,   33,   33,   33,   33,   35,   35,
   34,   34,   36,   37,   38,   27,   27,   39,   41,   41,
   41,   41,   41,   41,   40,   40,   40,   28,   29,   29,
   30,
};
final static short yylen[] = {                            2,
    5,    0,    6,    0,    6,    4,    0,    2,    1,    1,
    1,    1,    1,    3,    2,    1,    3,    1,    1,   11,
    0,   12,   10,   10,    0,   12,    9,    0,    1,    3,
    2,    8,    7,    0,    3,    0,    3,    0,    2,    1,
    1,    4,   11,   10,    0,    3,    3,    2,    4,    3,
    0,    2,    2,    2,    1,    1,    2,    2,    2,    3,
    3,    4,    4,    1,    3,    3,    1,    3,    3,    1,
    1,    3,    4,    1,    1,    1,    1,    1,    5,    6,
    1,    3,    5,    2,    4,    9,    7,    3,    1,    1,
    1,    1,    1,    1,    3,    1,    2,    7,    4,    4,
    4,
};
final static short yydefred[] = {                         0,
    2,    7,    0,    7,    0,    0,    4,    0,   51,    0,
   18,   19,    0,    0,    8,    9,   10,   11,   12,   13,
    0,   51,   51,   16,    0,    0,    0,    0,    0,    0,
    0,    0,    0,   47,    0,    0,    0,    0,    1,    0,
    0,   71,    0,    0,    0,   52,    0,    0,   55,   56,
    0,    0,    0,    0,   67,   74,   75,   76,   77,    0,
    0,   25,    0,    0,   21,    0,   14,    3,    5,   17,
   59,    0,    0,    0,    0,    0,    0,    0,    0,   51,
   96,    0,    0,   84,   53,    0,    0,   54,   57,   58,
    0,    0,    0,    0,    0,    0,    0,   49,    0,    0,
    0,    0,   78,   72,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,   68,   69,    0,    0,   38,    0,    0,    0,   29,
    0,    0,    0,   73,   63,   62,    0,    0,   89,   90,
   91,   92,   93,   94,    0,    0,   99,  100,  101,   95,
    0,   85,    0,    0,    0,   31,    0,    0,    0,    7,
    0,   79,    0,   83,    0,    0,    0,   37,    0,    0,
    0,   39,   40,   41,    7,   30,   51,    7,    0,   80,
    0,    0,    0,   32,    0,    0,    0,    0,    0,    0,
   51,    0,   87,   98,    0,    0,    0,    0,   51,    0,
   51,    0,    0,    0,    0,    0,   42,    0,   24,    0,
    0,   86,    0,    7,    0,    0,    0,   20,   51,    0,
   26,   22,    0,   51,    0,    0,    0,    0,   44,    0,
   43,
};
final static short yydgoto[] = {                          3,
    5,   26,    4,   23,   15,   16,   17,   18,   19,   20,
  128,   25,  129,   99,   96,  130,   61,  126,  154,  172,
  173,  174,  198,   46,   47,   48,   49,   50,   51,   52,
  103,   54,   55,  106,   56,   57,   58,   59,  112,   82,
  145,
};
final static short yysindex[] = {                      -197,
    0,    0,    0,    0, -118, -183,    0, -254,    0, -235,
    0,    0, -227, -192,    0,    0,    0,    0,    0,    0,
 -215,    0,    0,    0,  -12,  -43, -210, -107, -254,  -52,
   24,  -10,   -7,    0, -212,   42,    3,   71,    0,   75,
   80,    0,   16,   85, -133,    0,  105,    7,    0,    0,
  110,  112,  -88,   57,    0,    0,    0,    0,    0, -119,
  -77,    0,  149,   33,    0,  178,    0,    0,    0,    0,
    0,    1,  -41, -234,  -60,  189,    1,  -14,    1,    0,
    0,  -46,    1,    0,    0,    1,    1,    0,    0,    0,
    1,    1,    1, -254,  -73,  245, -192,    0,  250, -192,
   -6,   26,    0,    0,   26,  -15,  170,  212,  275,    1,
  103,  279,  286,  131,  182,   31,  289,  185,   57,   57,
   26,    0,    0,  291, -254,    0, -192,   88,   56,    0,
 -192,   66,    1,    0,    0,    0,  -40,  285,    0,    0,
    0,    0,    0,    0,    1,   16,    0,    0,    0,    0,
    1,    0,   49, -105,   78,    0, -192,   89,   86,    0,
   26,    0,   90,    0,   26,   99,  315,    0,  306,  118,
  123,    0,    0,    0,    0,    0,    0,    0, -120,    0,
   16,  339,  340,    0,  354,  361,  -20,  -70,   46,  254,
    0,  140,    0,    0, -192, -192,  119,  345,    0,  346,
    0,   61,  351,  139,  157, -254,    0,   76,    0,   91,
  352,    0,  154,    0,  291,  357,  362,    0,    0,  274,
    0,    0,  106,    0,  138,  121,  363,  138,    0,  364,
    0,
};
final static short yyrindex[] = {                         0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,  -79,  430,  173,    0,    0,    0,
  168,    0,    0,    0,    0,    0,   20,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,   81,   13,    0,    0,    0,    0,    0,    0,
    0,    0,    0,  171,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,  -66,    0,  223,    0,    0,  223,
  -32,  372,    0,    0,  255,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0, -240,    0,    0,   43,   73,
  376,    0,    0,  177,    0,    0,  223,    0,    0,    0,
  223,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
  256,    0,    0,    0,  398,    0,    0,    0,  205,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,  -23,  389,    0,    0,    0,
    0,    0,    0,    0,  223,  223,    0,    0,    0,  231,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
  251,    0,    0,    0,  392,    0,    0,    0,    0,    0,
    0,    0,    0,    0,  389,    0,    0,  389,    0,    0,
    0,
};
final static short yygindex[] = {                         0,
   10,  218,    0,    0,    0,    0,    0,    0,    0,    0,
    2,    4,  -83,    0,    0,  295,    0,    0,    0,    0,
    0,    0,  132,  -37,    0,  335,    0,    0,    0,    0,
  360,  301,  297,  316,    0,    0,    0,    0,  303, -128,
    0,
};
final static int YYTABLESIZE=586;
static short yytable[];
static { yytable();}
static void yytable(){
yytable = new short[]{                        104,
  162,   45,   24,   45,   45,   81,   21,   21,   70,   70,
   70,   70,   70,    6,   70,   29,  132,  166,   97,   97,
   16,   27,  107,   35,   31,  134,   70,   70,  133,   70,
   45,   35,   64,   73,   45,   16,   97,   45,   28,   75,
  108,   24,   73,  155,   70,   45,   34,  159,   75,   86,
   30,   87,  192,   64,   76,   64,   64,   64,    1,    2,
   45,   70,   70,   76,   70,   88,   70,   35,   86,   60,
   87,   64,   64,    8,   64,   45,   35,   22,   70,   11,
   12,   10,   67,   65,   74,   65,   65,   65,   11,   12,
   45,   98,   35,   74,   13,   14,  158,  124,   92,  157,
   71,   65,   65,   93,   65,   45,  160,  168,   81,  157,
   77,  204,  205,   66,   78,   66,   66,   66,  175,   79,
   45,  157,   78,   78,   83,   78,  178,   78,  153,  157,
  180,   66,   66,  133,   66,   45,    8,    7,    8,   78,
  191,   84,    9,   81,   10,   86,   10,   87,   62,   63,
   45,   11,   12,   11,   12,  171,  169,   13,   14,   13,
   14,   94,  144,   85,  143,   45,   11,   12,   89,  179,
   90,  148,  170,   86,  187,   87,   48,   48,   91,  213,
   21,   48,  157,   95,  188,   48,    8,  190,   97,   21,
  199,   21,   48,   48,   10,   36,  109,  214,   48,   48,
  157,   11,   12,   65,   66,   36,   36,   13,   14,  215,
  125,   36,   36,   37,   38,  101,  101,  100,   39,   40,
   41,   21,  149,  220,   86,  152,   87,   86,  110,   87,
  117,   42,   43,   42,   42,   70,   70,   70,   70,   32,
   33,   44,  101,   44,   44,   36,   37,   38,   36,   37,
   38,   68,   40,   41,   69,   40,   41,  101,   16,  113,
   42,  197,  135,   28,   42,   43,   28,   42,   43,   72,
   44,   36,   37,   38,   44,   42,   80,   44,   40,   41,
   64,   64,   64,   64,  127,   44,   36,   37,   38,  131,
   42,   43,  150,   40,   41,   81,   82,  116,   81,   82,
   44,   36,   37,   38,  136,   42,   43,  200,   40,   41,
   65,   65,   65,   65,  137,   44,   36,   37,   38,  146,
   42,   43,  211,   40,   41,  164,  147,   86,  151,   87,
   44,   36,   37,   38,   35,   42,   43,  216,   40,   41,
   66,   66,   66,   66,  156,   44,   36,   37,   38,  177,
   42,   43,  217,   40,   41,  183,  227,  181,  182,  230,
   44,   36,   37,   38,  184,   42,   43,  225,   40,   41,
  139,  140,  141,  142,  185,   44,   36,   37,   38,  186,
   42,   43,  228,   40,   41,   53,  119,  120,  122,  123,
   44,   53,   53,  195,  189,   42,   43,  193,  194,  203,
  196,  206,   53,  207,  209,   44,  102,  105,  202,  212,
  218,  111,  114,  115,  219,  221,  208,  118,  210,  197,
  222,  229,  231,   15,   15,  121,   50,   50,   15,    6,
   60,   50,   15,   34,   61,   50,  223,   35,   88,   15,
   15,  226,   50,   50,  138,   15,   15,   45,   50,   50,
   46,  176,  163,  167,    0,    0,    0,    0,    0,    0,
   33,   33,    0,    0,    0,   33,    0,  161,    0,   33,
    0,  105,    0,    0,    0,   53,   33,   33,    0,  165,
    0,    0,   33,   33,    0,  111,   27,   27,    0,    0,
    0,   27,    0,    0,    0,   27,    0,    0,    0,    0,
    0,    0,   27,   27,    0,   53,   23,   23,   27,   27,
    8,   23,    0,    0,  201,   23,    0,    0,   10,    0,
    0,    0,   23,   23,    0,   11,   12,    0,   23,   23,
    8,   13,   14,    0,  224,    0,    0,    0,   10,    0,
   53,    0,    0,    0,    0,   11,   12,    0,   53,    0,
    0,   13,   14,    0,    0,    0,    0,    0,    0,    0,
    0,   53,    0,    0,    0,    0,    0,   53,    0,   53,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,   53,    0,    0,   53,
};
}
static short yycheck[];
static { yycheck(); }
static void yycheck() {
yycheck = new short[] {                         41,
   41,   45,  257,   45,   45,   43,    5,    6,   41,   42,
   43,   44,   45,    4,   47,   14,  100,  146,  259,  260,
   44,  257,  257,   44,   21,   41,   59,   60,   44,   62,
   45,   44,   29,   40,   45,   59,  277,   45,  266,   46,
  275,  257,   40,  127,  257,   45,   59,  131,   46,   43,
  266,   45,  181,   41,   61,   43,   44,   45,  256,  257,
   45,   42,   43,   61,   45,   59,   47,   44,   43,  280,
   45,   59,   60,  257,   62,   45,   44,  261,   59,  272,
  273,  265,   59,   41,   91,   43,   44,   45,  272,  273,
   45,   59,   44,   91,  278,  279,   41,   94,   42,   44,
   59,   59,   60,   47,   62,   45,   41,   59,  146,   44,
   40,  195,  196,   41,   40,   43,   44,   45,   41,   40,
   45,   44,   42,   43,   40,   45,   41,   47,  125,   44,
   41,   59,   60,   44,   62,   45,  257,  256,  257,   59,
  261,  275,  261,  181,  265,   43,  265,   45,  256,  257,
   45,  272,  273,  272,  273,  154,  262,  278,  279,  278,
  279,  281,   60,   59,   62,   45,  272,  273,   59,  160,
   59,   41,  278,   43,  171,   45,  256,  257,  267,   41,
  179,  261,   44,  261,  175,  265,  257,  178,   40,  188,
  261,  190,  272,  273,  265,  262,  257,   41,  278,  279,
   44,  272,  273,  256,  257,  272,  273,  278,  279,  206,
  284,  278,  256,  257,  258,  257,  257,   40,  262,  263,
  264,  220,   41,  214,   43,   41,   45,   43,   40,   45,
  277,  275,  276,  275,  275,  268,  269,  270,  271,   22,
   23,  285,  257,  285,  285,  256,  257,  258,  256,  257,
  258,  262,  263,  264,  262,  263,  264,  257,  282,  274,
  275,  282,   93,   41,  275,  276,   44,  275,  276,  267,
  285,  256,  257,  258,  285,  275,  261,  285,  263,  264,
  268,  269,  270,  271,   40,  285,  256,  257,  258,   40,
  275,  276,  262,  263,  264,   41,   41,   80,   44,   44,
  285,  256,  257,  258,   93,  275,  276,  262,  263,  264,
  268,  269,  270,  271,   40,  285,  256,  257,  258,   41,
  275,  276,  262,  263,  264,   41,   41,   43,   40,   45,
  285,  256,  257,  258,   44,  275,  276,  262,  263,  264,
  268,  269,  270,  271,  257,  285,  256,  257,  258,  261,
  275,  276,  262,  263,  264,   41,  225,  259,  260,  228,
  285,  256,  257,  258,   59,  275,  276,  262,  263,  264,
  268,  269,  270,  271,  257,  285,  256,  257,  258,  257,
  275,  276,  262,  263,  264,   26,   86,   87,   92,   93,
  285,   32,   33,   40,  177,  275,  276,   59,   59,  260,
   40,  283,   43,   59,   59,  285,   72,   73,  191,   59,
   59,   77,   78,   79,  261,   59,  199,   83,  201,  282,
   59,   59,   59,  256,  257,   91,  256,  257,  261,    0,
   59,  261,  265,  261,   59,  265,  219,  261,   41,  272,
  273,  224,  272,  273,  110,  278,  279,   59,  278,  279,
   59,  157,  137,  151,   -1,   -1,   -1,   -1,   -1,   -1,
  256,  257,   -1,   -1,   -1,  261,   -1,  133,   -1,  265,
   -1,  137,   -1,   -1,   -1,  116,  272,  273,   -1,  145,
   -1,   -1,  278,  279,   -1,  151,  256,  257,   -1,   -1,
   -1,  261,   -1,   -1,   -1,  265,   -1,   -1,   -1,   -1,
   -1,   -1,  272,  273,   -1,  146,  256,  257,  278,  279,
  257,  261,   -1,   -1,  261,  265,   -1,   -1,  265,   -1,
   -1,   -1,  272,  273,   -1,  272,  273,   -1,  278,  279,
  257,  278,  279,   -1,  261,   -1,   -1,   -1,  265,   -1,
  181,   -1,   -1,   -1,   -1,  272,  273,   -1,  189,   -1,
   -1,  278,  279,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,  202,   -1,   -1,   -1,   -1,   -1,  208,   -1,  210,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,  223,   -1,   -1,  226,
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
"declaracion_variables : tipo lista_identificadores ';'",
"declaracion_variables : tipo lista_identificadores",
"lista_identificadores : IDENTIFICADOR",
"lista_identificadores : lista_identificadores ',' IDENTIFICADOR",
"tipo : SHORTINT",
"tipo : SINGLEF",
"declaracion_funciones : tipo FUNCTION IDENTIFICADOR '(' lista_parametros_formales ')' sentencias_declarativas BEGIN sentencias_ejecutables END ';'",
"$$3 :",
"declaracion_funciones : tipo FUNCTION error $$3 '(' lista_parametros_formales ')' sentencias_declarativas BEGIN sentencias_ejecutables END ';'",
"declaracion_funciones : tipo FUNCTION IDENTIFICADOR '(' lista_parametros_formales ')' sentencias_declarativas BEGIN sentencias_ejecutables END",
"declaracion_funciones : AUTO FUNCTION IDENTIFICADOR '(' lista_parametros_formales ')' BEGIN sentencias_ejecutables END ';'",
"$$4 :",
"declaracion_funciones : AUTO FUNCTION error $$4 '(' lista_parametros_formales ')' sentencias_declarativas BEGIN sentencias_ejecutables END ';'",
"declaracion_funciones : AUTO FUNCTION IDENTIFICADOR '(' lista_parametros_formales ')' BEGIN sentencias_ejecutables END",
"lista_parametros_formales :",
"lista_parametros_formales : parametro_formal",
"lista_parametros_formales : lista_parametros_formales ',' parametro_formal",
"parametro_formal : tipo IDENTIFICADOR",
"declaracion_clase : CLASS IDENTIFICADOR importacion_opcional BEGIN herencia_opcional miembros_clase END ';'",
"declaracion_clase : CLASS IDENTIFICADOR importacion_opcional BEGIN herencia_opcional miembros_clase END",
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
"declaracion_objeto : IDENTIFICADOR lista_identificadores",
"declaracion_comptime : COMPTIME tipo lista_identificadores ';'",
"declaracion_comptime : COMPTIME tipo lista_identificadores",
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
"bloque : BEGIN sentencias_ejecutables",
"sentencia_repeat_until : REPEAT bloque UNTIL '(' condicion ')' ';'",
"sentencia_pout : POUT '(' CADENA ')'",
"sentencia_pout : POUT '(' expresion ')'",
"sentencia_ret : RET '(' expresion ')'",
};

//#line 342 "gram.y"

/* CODIGO DE SOPORTE                                                         */

static Parser parser;

static int cant_errores = 0;

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
//#line 540 "Parser.java"
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
//#line 89 "gram.y"
{ yyerror("Falta ';' al final de la declaracion de variables"); }
break;
case 21:
//#line 108 "gram.y"
{ yyerror("Falta nombre de funcion"); }
break;
case 23:
//#line 114 "gram.y"
{ yyerror("Falta ';' al final de la declaracion de funcion"); }
break;
case 25:
//#line 117 "gram.y"
{ yyerror("Falta nombre de funcion"); }
break;
case 27:
//#line 122 "gram.y"
{ yyerror("Falta ';' al final de la declaracion de funcion"); }
break;
case 33:
//#line 142 "gram.y"
{ yyerror("Falta ';' al final de la declaracion de clase"); }
break;
case 48:
//#line 187 "gram.y"
{ yyerror("Falta ';' al final de la declaracion de objeto"); }
break;
case 50:
//#line 195 "gram.y"
{ yyerror("Falta ';' al final de la declaracion comptime"); }
break;
case 59:
//#line 213 "gram.y"
{ yyerror("Sentencia ejecutable malformada o falta ';' previo"); }
break;
case 71:
//#line 243 "gram.y"
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
case 84:
//#line 285 "gram.y"
{
            int id_pos = val_peek(0).ival;
            String lexema_pos = TablaSimbolos.obtenerAtributo(id_pos, TablaSimbolos.LEXEMA);
            int id_neg = TablaSimbolos.convertirANegativo(lexema_pos);
            yyval.ival = id_neg;
        }
break;
case 97:
//#line 322 "gram.y"
{ yyerror("Falta el delimitador END en el bloque de sentencias"); }
break;
//#line 794 "Parser.java"
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
