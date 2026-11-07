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
import java.util.ArrayList;
import java.util.Stack;
//#line 21 "Parser.java"




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
    1,    0,    4,    0,    5,    0,    0,    2,    2,    6,
    6,    6,    6,    6,    7,    7,   13,   13,   13,   14,
   14,   12,   12,   15,   16,   17,    8,   19,    8,    8,
    8,   20,    8,    8,   18,   18,   18,   21,   21,   21,
   21,   22,    9,    9,   23,   23,   23,   24,   24,   24,
   25,   25,   26,   26,   27,   28,   28,   30,   31,   29,
   29,   29,   10,   10,   11,   11,   11,   11,    3,    3,
   32,   32,   32,   32,   32,   32,   32,   33,   33,   39,
   39,   39,   34,   34,   34,   34,   34,   34,   40,   40,
   40,   40,   40,   40,   40,   40,   40,   40,   41,   41,
   41,   41,   41,   41,   41,   41,   41,   41,   43,   43,
   42,   42,   44,   44,   45,   45,   46,   47,   35,   35,
   35,   51,   50,   50,   50,   50,   48,   52,   52,   52,
   52,   52,   52,   49,   49,   49,   53,   36,   36,   36,
   36,   36,   36,   54,   54,   54,   54,   37,   37,   37,
   38,
};
final static short yylen[] = {                            2,
    1,    5,    0,    6,    0,    6,    4,    0,    2,    1,
    1,    1,    1,    1,    3,    3,    1,    3,    2,    1,
    3,    1,    1,    3,    3,    4,    9,    0,   12,    8,
    5,    0,   12,    4,    0,    1,    3,    2,    2,    2,
    2,    2,    7,    6,    0,    3,    2,    0,    3,    2,
    0,    2,    1,    1,    4,   10,    9,    2,    2,    0,
    3,    2,    3,    2,    4,    3,    3,    2,    0,    2,
    2,    2,    1,    1,    2,    2,    2,    3,    3,    4,
    4,    4,    1,    3,    3,    3,    3,    2,    1,    3,
    3,    3,    3,    2,    2,    2,    2,    2,    1,    1,
    1,    3,    4,    1,    1,    1,    1,    1,    5,    6,
    1,    3,    5,    5,    2,    2,    4,    4,    3,    5,
    4,    1,    2,    4,    1,    3,    3,    1,    1,    1,
    1,    1,    1,    3,    1,    2,    1,    5,    4,    6,
    5,    5,    4,    3,    2,    2,    1,    4,    4,    3,
    4,
};
final static short yydefred[] = {                         0,
    3,    1,    0,    8,    8,    0,    0,    5,    0,   69,
    0,   22,   23,    0,    0,    9,   10,   11,   12,   13,
   14,    0,    0,    0,    0,    0,   69,   69,   20,    0,
    0,   42,    0,    0,    0,    0,   17,    0,    0,    0,
    0,   69,    0,    0,    0,    0,   63,    0,    0,    0,
    0,    2,    0,    0,  100,  101,  137,    0,    0,    0,
    0,    0,   70,    0,    0,   73,   74,    0,    0,    0,
    0,   89,  104,  105,  106,  107,    0,    0,   32,   25,
    0,   67,   16,   28,   24,   19,   15,    0,    0,    0,
    0,    0,   36,    0,    0,    0,    0,    0,    4,    6,
   21,   77,    0,    0,    0,    0,    0,    0,    0,    0,
    0,  108,    0,    0,    0,    0,    0,  115,  116,   94,
   95,   71,    0,    0,   72,   75,   76,    0,   96,   97,
   98,    0,    0,   69,  135,    0,    0,    0,    0,    0,
   65,    0,   18,   41,   39,   40,   38,    0,    8,   26,
    0,    0,    0,   51,    0,    0,  102,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,  128,  129,  130,
  131,  132,  133,    0,    0,    0,  150,    0,    0,    0,
   86,    0,   87,    0,    0,   92,   90,   93,   91,    0,
  122,    0,  125,  119,    0,    0,    0,    0,    0,    0,
    0,    0,    0,   37,    0,   31,   50,    0,    0,    0,
    0,  103,   81,   80,   82,    0,    0,  121,  118,    0,
    0,  148,  149,  151,  117,  134,  123,    0,    0,  145,
  139,  143,    0,    0,    0,    0,    0,   69,   49,    0,
    0,    0,   52,   53,   54,    0,    0,  114,    0,  109,
    0,  113,  120,    0,  126,  144,  141,  138,  142,    0,
    8,    8,    0,   43,   59,    0,    0,    0,    0,  110,
  124,  140,    0,    0,    0,    0,    0,    0,    0,   69,
   69,   27,    0,    0,   55,    8,    0,    0,    0,    0,
    0,   69,    0,    0,   69,    0,   33,   29,    0,    0,
    0,    0,    0,   57,   56,
};
final static short yydgoto[] = {                          3,
    4,    6,   31,    5,   28,   16,   17,   18,   19,   20,
   21,   91,   39,   30,   23,   24,   25,   92,  142,  140,
   93,   26,   44,  154,  209,  243,  244,  245,  277,  246,
  247,   63,   64,   65,   66,   67,   68,   69,  112,   71,
   72,  159,   73,   74,   75,   76,   77,  197,  136,  194,
  195,  174,   78,  198,
};
final static short yysindex[] = {                      -157,
    0,    0,    0,    0,    0,  324,  497,    0, -214,    0,
 -208,    0,    0, -205, -113,    0,    0,    0,    0,    0,
    0,  -64,   34,   36, -156, -158,    0,    0,    0,  -13,
  108,    0,  -32, -214,   19,   69,    0,   -7,  -15, -116,
 -116,    0, -227, -109,  124,  140,    0, -103,  103,   86,
   -5,    0,  141,  144,    0,    0,    0,  170,   17,   37,
   56,   56,    0,  105,  129,    0,    0,  153,  158,   10,
  -39,    0,    0,    0,    0,    0,  -30,   30,    0,    0,
   53,    0,    0,    0,    0,    0,    0,  -28,  -22,    3,
   99,  101,    0,  117,  166, -214,  210,  -19,    0,    0,
    0,    0,   13,   73, -148,   14,  239,  -30,  486,   88,
  177,    0,  243,   61,   88,   88,  -39,    0,    0,    0,
    0,    0,  133,  149,    0,    0,    0,   88,    0,    0,
    0,   76,  193,    0,    0,  -45,   40,   88,  -24,  256,
    0,  261,    0,    0,    0,    0,    0, -116,    0,    0,
  246,  210,  -52,    0,   88,   26,    0,   26,  132,  218,
  241,  247,  289,   88,  -45,  296,  303,    0,    0,    0,
    0,    0,    0,   88,  -30,  310,    0,   -9,  425,  462,
    0,  -39,    0,  -39,   26,    0,    0,    0,    0,  198,
    0,  298,    0,    0,  -30,   88,  342,  326,  -18,   40,
   88, -116, -116,    0,  557,    0,    0,   90,  -83,  465,
   88,    0,    0,    0,    0,   82,  501,    0,    0,   26,
  -45,    0,    0,    0,    0,    0,    0,  -37,  358,    0,
    0,    0,  348,  349,  -14,  156,  157,    0,    0,  355,
  176,  183,    0,    0,    0,  361,  419,    0,   26,    0,
  163,    0,    0,  385,    0,    0,    0,    0,    0,  412,
    0,    0,  267,    0,    0,    0,  -38, -116, -116,    0,
    0,    0,  559,  586,  417, -236,  427,  175,  248,    0,
    0,    0, -214,  210,    0,    0,  202,  307,  370,  210,
  605,    0,  434,  439,    0,  396,    0,    0,  422,  199,
  199,  443,  452,    0,    0,
};
final static short yyrindex[] = {                         0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,  253,    0,    0,    0,  344,
  509,    0,    0,    0,  364,    0,    0,    0,    0,  287,
  287,    0,    0,    0,    0,    0,    0,    0,    0,  430,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,  445,
  456,    0,    0,    0,    0,    0,    0,    0,    0,    0,
  432,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,  266,  159,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,   51,    0,
    0,    0,    0,    0,    0,    0,  476,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
  457,  272,    0,    0,    0,  463,    0,  335,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,  496,    0,  516,  469,    0,    0,    0,    0,  236,
    0,    0,    0,    0,    0,    0,  475,    0,    0,    0,
    0,  287,  287,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,   -2,
    0,    0,    0,    0,    0,    0,    0,    0,  489,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,  532,
    0,    0,    0,    0,    0,    0,    0,    0,  450,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    8,  490,  287,  287,    0,
    0,    0,    0,    0,  534,    0,    0,    0,    0,    0,
    0,    0,    0,  491,    0,    0,    0,    0,    0,  492,
    0,    0,    0,    0,    0,    0,    0,    0,    0,  490,
  490,    0,    0,    0,    0,
};
final static short yygindex[] = {                         0,
    0,   -3,  -17,    0,    0,    0,    0,    0,    0,    0,
    0,   -6,    0,  -10,    0,    0,    0,  189,    0,    0,
  397,    0,    0,    0,    0,    0,    0,    0,   77,    0,
    0,   11,    0,  645,    0,    0,    0,    0,  -27,  -33,
    4,  346,    0,    0,    0,    0,    0,  -31,  404, -137,
    0,    0,    0,  366,
};
final static int YYTABLESIZE=884;
static short yytable[];
static { yytable();}
static void yytable(){
yytable = new short[]{                         22,
   22,    7,  132,   70,   35,   48,  207,  133,   34,   45,
   46,   61,   59,  193,   60,  201,   62,   70,   70,  113,
   29,  255,  233,   81,   95,  117,  260,  218,   88,   29,
   48,  223,   97,  123,  110,  124,   61,   59,  127,   60,
  232,   62,   29,   87,  259,   47,  283,   58,   32,   70,
   70,   20,  155,   96,   61,   59,  127,   60,   61,   62,
   33,   60,   48,   62,  120,  121,   20,   70,  123,  138,
  124,   61,   59,   40,   60,   41,   62,   82,  167,  196,
   70,   61,   59,  253,   60,  152,   62,  135,  135,  182,
  184,   99,   99,   99,   99,   99,   48,   99,    1,    2,
   60,  177,   61,   59,   42,   60,  199,   62,  160,   99,
   99,  141,   99,  157,   61,   59,  190,   60,  135,   62,
   60,   43,  250,   61,   59,  104,   60,   83,   62,   61,
   59,  106,   60,   48,   62,  187,  189,  161,  162,   89,
   90,  149,  208,   29,  148,  205,  107,   70,  239,   61,
   59,   98,   60,  101,   62,   12,   13,  150,   12,   13,
  148,  102,   70,  122,  229,   61,   59,   70,   60,  235,
   62,  123,  212,  124,   61,  211,  105,   60,  240,   62,
  114,   61,   59,  115,   60,  135,   62,  125,   12,   13,
   61,   36,   37,   60,  241,   62,  261,  262,   22,  148,
  148,   38,  242,  270,   29,  135,  211,   61,   59,  116,
   60,  126,   62,  191,  192,  286,  127,  129,  148,  123,
  263,  124,  254,   79,   80,   49,   50,   51,  143,   94,
  134,  267,   53,   54,  144,   70,  173,   60,  172,   61,
   59,   86,   60,  276,   62,   57,  130,  131,   84,   85,
  108,  109,  200,   48,   58,   55,   56,  273,  274,  145,
   70,   70,  288,  289,  153,  284,   22,   22,   70,  109,
  163,   70,  290,  109,  296,  136,  128,  299,  164,   58,
   55,   56,  291,  175,   22,   49,   50,   51,  287,   20,
  134,  148,   53,   54,  136,  202,  109,   58,   55,   56,
  203,   58,   55,   56,  206,   57,  137,   99,   61,   59,
  213,   60,  109,   62,   58,   55,   56,  109,   99,   99,
   99,   99,  118,  119,   58,   55,   56,   35,  216,  109,
   35,  186,  109,  214,  176,  155,   99,   99,  109,  215,
   58,   55,   56,  219,  109,   58,   55,   56,   61,   59,
  222,   60,  103,   62,  146,  147,  227,   58,   55,   56,
   58,   55,   56,   49,   50,   51,   58,   55,   56,   52,
   53,   54,   58,   55,   56,  111,  302,  303,  111,   49,
   50,   51,  230,   57,  231,   99,   53,   54,  181,  109,
  236,  237,   58,   55,   56,   49,   50,   51,  256,   57,
  268,  100,   53,   54,  183,  109,  257,  258,   58,   55,
   56,   61,   59,  264,   60,   57,   62,   58,   55,   56,
   48,   49,   50,   51,   58,   55,   56,  151,   53,   54,
   48,   48,  265,   58,   55,   56,   48,   61,   59,  266,
   60,   57,   62,  271,  168,  169,  170,  171,  188,  109,
   58,   55,   56,   49,   50,   51,  278,  279,  269,  226,
   53,   54,  292,   61,   59,  224,   60,  123,   62,  124,
  272,   99,   99,   57,   99,  282,   99,   58,   55,   56,
  276,  139,   58,   55,   56,  285,  108,  108,   99,  108,
  112,  108,  297,  112,  136,  136,   83,  298,   83,   83,
   83,  304,  225,  108,  123,  248,  124,  123,    7,  124,
  305,  165,  136,   45,   83,   83,   88,   83,   88,   88,
   88,   78,   49,   50,   51,  104,   47,   79,  275,   53,
   54,  106,   46,  147,   88,   88,   84,   88,   84,   84,
   84,  252,   57,  123,  204,  124,  107,  146,   60,   62,
   61,   58,   55,   56,   84,   84,   85,   84,   85,   85,
   85,  251,   49,   50,   51,  234,    0,    0,  293,   53,
   54,    0,    0,    0,   85,   85,  105,   85,  221,    8,
    9,    0,   57,    0,   10,    0,    0,    0,   11,    0,
    0,   58,   55,   56,    0,   12,   13,    0,  228,   64,
   64,   14,   15,    0,   64,    0,    0,    0,   64,    0,
    0,    0,    0,    0,    0,   64,   64,    0,    0,   68,
   68,   64,   64,    0,   68,   49,   50,   51,   68,    0,
    0,  294,   53,   54,    0,   68,   68,    0,    0,    0,
    0,   68,   68,    0,    0,   57,    0,    0,    0,    0,
    0,   49,   50,   51,   58,   55,   56,  300,   53,   54,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,   57,    0,    0,    0,    0,    0,   49,   50,   51,
   58,   55,   56,  301,   53,   54,   99,   66,   66,    0,
    0,    0,   66,    0,    0,  111,   66,   57,    0,    0,
    0,  108,    0,   66,   66,    0,   58,   55,   56,   66,
   66,    0,   34,   34,    0,   99,   99,   34,    0,    0,
    0,   34,    0,   83,   83,   83,   83,    0,   34,   34,
  108,  108,    0,    0,   34,   34,    0,    0,    0,    0,
    0,    0,    0,   88,   88,   88,   88,  156,  158,    0,
    0,    0,  166,    9,  111,    0,    0,   27,  178,  179,
  180,   11,    0,   84,   84,   84,   84,    0,   12,   13,
    0,    0,  185,    0,   14,   15,    0,    0,    0,    0,
    0,  111,  111,   85,   85,   85,   85,   44,   44,   30,
   30,    0,   44,    0,   30,    0,   44,    0,   30,  210,
    0,    0,    0,   44,   44,   30,   30,    0,  217,   44,
   44,   30,   30,    9,    0,    9,    0,  238,  220,  280,
    0,   11,    0,   11,    0,    0,    0,    0,   12,   13,
   12,   13,    0,    0,   14,   15,   14,   15,    0,    0,
  111,    0,    9,    0,  111,  111,  281,    0,    0,    0,
   11,    0,    0,    0,    0,  249,    0,   12,   13,    0,
  158,    9,    0,   14,   15,  295,    0,    0,    0,   11,
    0,    0,    0,    0,    0,    0,   12,   13,    0,    0,
    0,    0,   14,   15,
};
}
static short yycheck[];
static { yycheck(); }
static void yycheck() {
yycheck = new short[] {                          6,
    7,    5,   42,   31,   15,   44,   59,   47,   15,   27,
   28,   42,   43,   59,   45,   40,   47,   45,   46,   51,
  257,   59,   41,   34,   42,   59,   41,  165,   44,  257,
   44,   41,   43,   43,   40,   45,   42,   43,   41,   45,
   59,   47,  257,   59,   59,   59,  283,   40,  257,   77,
   78,   44,   40,  281,   42,   43,   59,   45,   42,   47,
  266,   45,   44,   47,   61,   62,   59,   95,   43,   40,
   45,   42,   43,   40,   45,   40,   47,   59,  110,   40,
  108,   42,   43,  221,   45,   96,   47,   77,   78,  123,
  124,   41,   42,   43,   44,   45,   44,   47,  256,  257,
   45,   41,   42,   43,  261,   45,  138,   47,  257,   59,
   60,   59,   62,   41,   42,   43,  134,   45,  108,   47,
   45,  280,   41,   42,   43,   40,   45,   59,   47,   42,
   43,   46,   45,   44,   47,  132,  133,  286,  287,  256,
  257,   41,  153,  257,   44,  149,   61,  175,   59,   42,
   43,  261,   45,  257,   47,  272,  273,   41,  272,  273,
   44,   59,  190,   59,  196,   42,   43,  195,   45,  201,
   47,   43,   41,   45,   42,   44,   91,   45,  262,   47,
   40,   42,   43,   40,   45,  175,   47,   59,  272,  273,
   42,  256,  257,   45,  278,   47,   41,   41,  205,   44,
   44,  266,  209,   41,  257,  195,   44,   42,   43,   40,
   45,   59,   47,  259,  260,   41,   59,  257,   44,   43,
  238,   45,  260,  256,  257,  256,  257,  258,  257,   41,
  261,  242,  263,  264,  257,  263,   60,   45,   62,   42,
   43,  257,   45,  282,   47,  276,  286,  287,  256,  257,
  256,  257,  277,   44,  285,  286,  287,  261,  262,  257,
  288,  289,  280,  281,  284,  276,  273,  274,  296,  257,
  257,  299,  283,  257,  292,   40,  267,  295,   40,  285,
  286,  287,  286,   41,  291,  256,  257,  258,   41,  282,
  261,   44,  263,  264,   59,   40,  257,  285,  286,  287,
   40,  285,  286,  287,   59,  276,  277,  257,   42,   43,
   93,   45,  257,   47,  285,  286,  287,  257,  268,  269,
  270,  271,  286,  287,  285,  286,  287,   41,   40,  257,
   44,  256,  257,   93,  274,   40,  286,  287,  257,   93,
  285,  286,  287,   41,  257,  285,  286,  287,   42,   43,
   41,   45,  267,   47,  256,  257,   59,  285,  286,  287,
  285,  286,  287,  256,  257,  258,  285,  286,  287,  262,
  263,  264,  285,  286,  287,   41,  300,  301,   44,  256,
  257,  258,   41,  276,   59,  262,  263,  264,  256,  257,
  202,  203,  285,  286,  287,  256,  257,  258,   41,  276,
   40,  262,  263,  264,  256,  257,   59,   59,  285,  286,
  287,   42,   43,   59,   45,  276,   47,  285,  286,  287,
  262,  256,  257,  258,  285,  286,  287,  262,  263,  264,
  272,  273,  257,  285,  286,  287,  278,   42,   43,  257,
   45,  276,   47,   59,  268,  269,  270,  271,  256,  257,
  285,  286,  287,  256,  257,  258,  268,  269,   40,  262,
  263,  264,  261,   42,   43,   41,   45,   43,   47,   45,
   59,   42,   43,  276,   45,   59,   47,  285,  286,  287,
  282,   78,  285,  286,  287,   59,   42,   43,   59,   45,
   41,   47,   59,   44,  259,  260,   41,   59,   43,   44,
   45,   59,   41,   59,   43,   41,   45,   43,    0,   45,
   59,  108,  277,  261,   59,   60,   41,   62,   43,   44,
   45,   59,  256,  257,  258,   40,  261,   59,  262,  263,
  264,   46,  261,   59,   59,   60,   41,   62,   43,   44,
   45,   41,  276,   43,  148,   45,   61,   59,   59,   59,
   59,  285,  286,  287,   59,   60,   41,   62,   43,   44,
   45,  216,  256,  257,  258,  200,   -1,   -1,  262,  263,
  264,   -1,   -1,   -1,   59,   60,   91,   62,  175,  256,
  257,   -1,  276,   -1,  261,   -1,   -1,   -1,  265,   -1,
   -1,  285,  286,  287,   -1,  272,  273,   -1,  195,  256,
  257,  278,  279,   -1,  261,   -1,   -1,   -1,  265,   -1,
   -1,   -1,   -1,   -1,   -1,  272,  273,   -1,   -1,  256,
  257,  278,  279,   -1,  261,  256,  257,  258,  265,   -1,
   -1,  262,  263,  264,   -1,  272,  273,   -1,   -1,   -1,
   -1,  278,  279,   -1,   -1,  276,   -1,   -1,   -1,   -1,
   -1,  256,  257,  258,  285,  286,  287,  262,  263,  264,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,  276,   -1,   -1,   -1,   -1,   -1,  256,  257,  258,
  285,  286,  287,  262,  263,  264,  257,  256,  257,   -1,
   -1,   -1,  261,   -1,   -1,   51,  265,  276,   -1,   -1,
   -1,  257,   -1,  272,  273,   -1,  285,  286,  287,  278,
  279,   -1,  256,  257,   -1,  286,  287,  261,   -1,   -1,
   -1,  265,   -1,  268,  269,  270,  271,   -1,  272,  273,
  286,  287,   -1,   -1,  278,  279,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,  268,  269,  270,  271,  103,  104,   -1,
   -1,   -1,  267,  257,  110,   -1,   -1,  261,  114,  115,
  116,  265,   -1,  268,  269,  270,  271,   -1,  272,  273,
   -1,   -1,  128,   -1,  278,  279,   -1,   -1,   -1,   -1,
   -1,  137,  138,  268,  269,  270,  271,  256,  257,  256,
  257,   -1,  261,   -1,  261,   -1,  265,   -1,  265,  155,
   -1,   -1,   -1,  272,  273,  272,  273,   -1,  164,  278,
  279,  278,  279,  257,   -1,  257,   -1,  261,  174,  261,
   -1,  265,   -1,  265,   -1,   -1,   -1,   -1,  272,  273,
  272,  273,   -1,   -1,  278,  279,  278,  279,   -1,   -1,
  196,   -1,  257,   -1,  200,  201,  261,   -1,   -1,   -1,
  265,   -1,   -1,   -1,   -1,  211,   -1,  272,  273,   -1,
  216,  257,   -1,  278,  279,  261,   -1,   -1,   -1,  265,
   -1,   -1,   -1,   -1,   -1,   -1,  272,  273,   -1,   -1,
   -1,   -1,  278,  279,
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
"nombre_programa : IDENTIFICADOR",
"programa : nombre_programa sentencias_declarativas BEGIN sentencias_ejecutables END",
"$$1 :",
"programa : error $$1 sentencias_declarativas BEGIN sentencias_ejecutables END",
"$$2 :",
"programa : nombre_programa sentencias_declarativas error $$2 sentencias_ejecutables END",
"programa : nombre_programa sentencias_declarativas BEGIN sentencias_ejecutables",
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
"encabezado_funcion : tipo FUNCTION IDENTIFICADOR",
"inicio_auto_funcion : AUTO FUNCTION IDENTIFICADOR",
"encabezado_auto_funcion : inicio_auto_funcion '(' lista_parametros_formales ')'",
"declaracion_funciones : encabezado_funcion '(' lista_parametros_formales ')' sentencias_declarativas BEGIN sentencias_ejecutables END ';'",
"$$3 :",
"declaracion_funciones : tipo FUNCTION error $$3 '(' lista_parametros_formales ')' sentencias_declarativas BEGIN sentencias_ejecutables END ';'",
"declaracion_funciones : encabezado_funcion '(' lista_parametros_formales ')' sentencias_declarativas BEGIN sentencias_ejecutables END",
"declaracion_funciones : encabezado_auto_funcion BEGIN sentencias_ejecutables END ';'",
"$$4 :",
"declaracion_funciones : AUTO FUNCTION error $$4 '(' lista_parametros_formales ')' sentencias_declarativas BEGIN sentencias_ejecutables END ';'",
"declaracion_funciones : encabezado_auto_funcion BEGIN sentencias_ejecutables END",
"lista_parametros_formales :",
"lista_parametros_formales : parametro_formal",
"lista_parametros_formales : lista_parametros_formales ',' parametro_formal",
"parametro_formal : tipo IDENTIFICADOR",
"parametro_formal : IDENTIFICADOR IDENTIFICADOR",
"parametro_formal : tipo error",
"parametro_formal : error IDENTIFICADOR",
"encabezado_clase : CLASS IDENTIFICADOR",
"declaracion_clase : encabezado_clase importacion_opcional BEGIN herencia_opcional miembros_clase END ';'",
"declaracion_clase : encabezado_clase importacion_opcional BEGIN herencia_opcional miembros_clase END",
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
"metodo_clase : encabezado_metodo '(' lista_parametros_formales ')' sentencias_declarativas BEGIN sentencias_ejecutables END exportacion_opcional ';'",
"metodo_clase : encabezado_metodo_auto '(' lista_parametros_formales ')' BEGIN sentencias_ejecutables END exportacion_opcional ';'",
"encabezado_metodo : tipo IDENTIFICADOR",
"encabezado_metodo_auto : AUTO IDENTIFICADOR",
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
"encabezado_if : IF '(' condicion ')'",
"sentencia_if : encabezado_if bloque resto_if",
"sentencia_if : IF condicion ')' bloque resto_if",
"sentencia_if : IF error bloque resto_if",
"inicio_else : ELSE",
"resto_if : END_IF ';'",
"resto_if : inicio_else bloque END_IF ';'",
"resto_if : ';'",
"resto_if : inicio_else bloque ';'",
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
"inicio_repeat : REPEAT",
"sentencia_repeat_until : inicio_repeat bloque UNTIL condicion_iteracion ';'",
"sentencia_repeat_until : inicio_repeat UNTIL condicion_iteracion ';'",
"sentencia_repeat_until : inicio_repeat bloque '(' condicion ')' ';'",
"sentencia_repeat_until : inicio_repeat '(' condicion ')' ';'",
"sentencia_repeat_until : inicio_repeat bloque '(' condicion ';'",
"sentencia_repeat_until : inicio_repeat '(' condicion ';'",
"condicion_iteracion : '(' condicion ')'",
"condicion_iteracion : condicion ')'",
"condicion_iteracion : '(' condicion",
"condicion_iteracion : condicion",
"sentencia_pout : POUT '(' CADENA ')'",
"sentencia_pout : POUT '(' expresion ')'",
"sentencia_pout : POUT '(' ')'",
"sentencia_ret : RET '(' expresion ')'",
};

//#line 1557 "gram.y"

/* CODIGO DE SOPORTE                                                         */

static Parser parser;

static int cant_errores = 0;
static int cant_errores_semanticos = 0;
static int cant_retornos_auto = -1;
static boolean claseActualValida = false;

public static String ambitoActual = TablaSimbolos.AMBITO_GLOBAL;
public static Stack<Integer> pilaSaltos = new Stack<>();

public static String obtenerFuncionActualMangled() {
    if (ambitoActual == null || ambitoActual.equals(TablaSimbolos.AMBITO_GLOBAL)) {
        return null;
    }
    return ambitoActual;
}

public static String obtenerClaseDeAmbito(String ambito) {
    if (ambito == null) return null;
    String[] partes = ambito.split(TablaSimbolos.SEPARADOR);
    if (partes.length >= 2 && TablaSimbolos.AMBITO_GLOBAL.equals(partes[0])) {
        String posibleClase = partes[1];
        String claveClase = TablaSimbolos.AMBITO_GLOBAL + TablaSimbolos.SEPARADOR + posibleClase;
        if ("nombre_clase".equals(TablaSimbolos.obtenerUso(claveClase))) {
            return posibleClase;
        }
    }
    return null;
}

public static void entrarAmbito(String nombre) {
    ambitoActual = ambitoActual + TablaSimbolos.SEPARADOR + nombre;
}

public static void salirAmbito() {
    pilaSaltos.clear();
    int index = ambitoActual.lastIndexOf(TablaSimbolos.SEPARADOR);
    if (index > 0) {
        ambitoActual = ambitoActual.substring(0, index);
    } else {
        ambitoActual = TablaSimbolos.AMBITO_GLOBAL;
    }
}

public static void agregarErrorSemantico(String s) {
    cant_errores_semanticos++;
    System.out.println("Error semantico (linea " + AnalizadorLexico.getLineaActual() + "): " + s);
}

// Muestra por pantalla la regla reconocida y la linea donde termino (al reducir)
static void imprimirRegla(String regla) {
    System.out.println("Regla " + regla + " en linea " + AnalizadorLexico.getLineaActual());
}

 // Soporte para variables COMPTIME y evaluacion estatica (Tema 22)
public static boolean esConstante(ParserVal pv) {
    if (pv == null || pv.sval == null) {
        return false;
    }
    return !pv.sval.startsWith("[") && !pv.sval.contains(TablaSimbolos.SEPARADOR);
}

public static String evaluarOperacionEstatica(String op, String val1, String val2, String tipo) {
    try {
        if ("SHORTINT".equals(tipo)) {
            int v1 = Integer.parseInt(val1);
            int v2 = Integer.parseInt(val2);
            int res = 0;
            if (op.equals("+")) res = v1 + v2;
            else if (op.equals("-")) res = v1 - v2;
            else if (op.equals("*")) res = v1 * v2;
            else if (op.equals("/")) {
                if (v2 == 0) {
                    agregarErrorSemantico("Division por cero en tiempo de compilacion");
                    return "0";
                }
                res = v1 / v2;
            }
            if (res < AnalizadorLexico.ValorMinimoInt || res > AnalizadorLexico.ValorMaximoInt) {
                agregarErrorSemantico("Resultado de operacion en tiempo de compilacion fuera de rango SHORTINT [-128, 127]: " + res);
            }
            return String.valueOf(res);
        } else if ("SINGLEF".equals(tipo)) {
            double v1 = Double.parseDouble(val1);
            double v2 = Double.parseDouble(val2);
            double res = 0.0;
            if (op.equals("+")) res = v1 + v2;
            else if (op.equals("-")) res = v1 - v2;
            else if (op.equals("*")) res = v1 * v2;
            else if (op.equals("/")) {
                if (v2 == 0.0) {
                    agregarErrorSemantico("Division por cero en tiempo de compilacion");
                    return "0.0";
                }
                res = v1 / v2;
            }
            double absRes = Math.abs(res);
            if (absRes != 0.0 && (absRes < AnalizadorLexico.ValorMinimoFloat || absRes > AnalizadorLexico.ValorMaximoFloat)) {
                agregarErrorSemantico("Resultado de operacion en tiempo de compilacion fuera de rango SINGLEF: " + res);
            }
            return String.valueOf(res);
        }
    } catch (NumberFormatException e) {
        return val1;
    }
    return val1;
}

public static void main(String[] args) {
    boolean argumentosValidos = args.length == 1 || (args.length == 2 && args[1].equals("-v"));
    if (!argumentosValidos) {
        System.out.println("Uso: java -jar compilador.jar <archivo_a_compilar> [-v]");
        System.out.println("Se compila un solo archivo por ejecucion.");
        System.exit(1);
    }

    String ruta = args[0];
    boolean debug = args.length == 2;

    if (!new File(ruta).isFile()) {
        System.out.println("No se encontro el archivo a compilar: " + ruta);
        System.exit(1);
    }

    System.out.println("Compilando archivo: " + ruta);
    try {
        ambitoActual = TablaSimbolos.AMBITO_GLOBAL;
        TablaSimbolos.reiniciar();
        GeneradorTerceto.reiniciar();
        GeneradorTerceto.iniciarAmbito(ambitoActual);
        claseActualValida = false;
        pilaSaltos.clear();
        cant_retornos_auto = -1;
        cant_errores = 0;
        cant_errores_semanticos = 0;
        AnalizadorLexico.reader = new PushbackReader(new BufferedReader(
                new InputStreamReader(new FileInputStream(ruta), java.nio.charset.StandardCharsets.UTF_8)));
        parser = new Parser(debug);
        parser.yyparse(); 
        TablaSimbolos.purgarIdentificadoresSinDeclarar();
        TablaSimbolos.imprimirTabla();
        GeneradorTerceto.imprimirTercetos();
    } catch (Exception e) {
        e.printStackTrace();
    }
}

int yylex() {
    int token = AnalizadorLexico.yylex();
    if (token <= 0) {
        return 0; // EOF para byacc/j
    }
    if (AnalizadorLexico.referenciaTablaSimbolos != null) {
        yylval = new ParserVal(AnalizadorLexico.referenciaTablaSimbolos);
    } else {
        yylval = new ParserVal();
    }
    return token;
}

void yyerror(String s) {
    if (s.equals("syntax error")) {
        return; // Se omite el mensaje generico de byacc/j para usar las descripciones especificas
    }
    cant_errores++;
    System.out.println("Error sintactico (linea " + AnalizadorLexico.getLineaActual() + "): " + s);
}
//#line 814 "Parser.java"
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
//#line 52 "gram.y"
{
            String nombre = val_peek(0).sval;
            TablaSimbolos.declararVariable(nombre, "programa", "nombre_programa", ambitoActual);
            yyval = val_peek(0);
        }
break;
case 2:
//#line 61 "gram.y"
{ 
            if (cant_errores == 0 && cant_errores_semanticos == 0) {
                System.out.println("Programa reconocido correctamente"); 
            } else {
                System.out.println("Compilacion finalizada con " + cant_errores + " error(es) sintactico(s) y " + cant_errores_semanticos + " error(es) semantico(s)");
            }
        }
break;
case 3:
//#line 68 "gram.y"
{ yyerror("Falta el nombre del programa al inicio"); }
break;
case 4:
//#line 69 "gram.y"
{ 
            System.out.println("Compilacion finalizada con " + cant_errores + " error(es) sintactico(s) y " + cant_errores_semanticos + " error(es) semantico(s)");
        }
break;
case 5:
//#line 72 "gram.y"
{ yyerror("Falta el delimitador BEGIN de sentencias ejecutables"); }
break;
case 6:
//#line 73 "gram.y"
{
            System.out.println("Compilacion finalizada con " + cant_errores + " error(es) sintactico(s) y " + cant_errores_semanticos + " error(es) semantico(s)");
        }
break;
case 7:
//#line 77 "gram.y"
{
            yyerror("Falta el delimitador END al final del programa");
            System.out.println("Compilacion finalizada con " + cant_errores + " error(es) sintactico(s) y " + cant_errores_semanticos + " error(es) semantico(s)");
        }
break;
case 10:
//#line 91 "gram.y"
{ imprimirRegla("declaracion_variables"); }
break;
case 11:
//#line 92 "gram.y"
{ imprimirRegla("declaracion_funciones"); }
break;
case 12:
//#line 93 "gram.y"
{ imprimirRegla("declaracion_clase"); }
break;
case 13:
//#line 94 "gram.y"
{ imprimirRegla("declaracion_objeto"); }
break;
case 14:
//#line 95 "gram.y"
{ imprimirRegla("declaracion_comptime"); }
break;
case 15:
//#line 101 "gram.y"
{
            String tipoDato = val_peek(2).sval;
            ArrayList<String> variables = (ArrayList<String>) val_peek(1).obj;
            if (variables != null) {
                for (int i = 0; i < variables.size(); i++) {
                    String nombreVar = variables.get(i);
                    TablaSimbolos.declararVariable(nombreVar, tipoDato, "variable", ambitoActual);
                }
            }
        }
break;
case 16:
//#line 112 "gram.y"
{ yyerror("Falta ';' al final de la declaracion de variables"); }
break;
case 17:
//#line 117 "gram.y"
{
            ArrayList<String> lista = new ArrayList<>();
            lista.add(val_peek(0).sval);
            ParserVal pv = new ParserVal();
            pv.obj = lista;
            yyval = pv;
        }
break;
case 18:
//#line 125 "gram.y"
{
            ArrayList<String> lista = (ArrayList<String>) val_peek(2).obj;
            lista.add(val_peek(0).sval);
            yyval = val_peek(2);
        }
break;
case 19:
//#line 130 "gram.y"
{ yyerror("Falta ',' entre los identificadores"); yyval = val_peek(1); }
break;
case 20:
//#line 135 "gram.y"
{
            ArrayList<String> lista = new ArrayList<>();
            lista.add(val_peek(0).sval);
            ParserVal pv = new ParserVal();
            pv.obj = lista;
            yyval = pv;
        }
break;
case 21:
//#line 143 "gram.y"
{
            ArrayList<String> lista = (ArrayList<String>) val_peek(2).obj;
            lista.add(val_peek(0).sval);
            yyval = val_peek(2);
        }
break;
case 22:
//#line 151 "gram.y"
{ yyval = new ParserVal("SHORTINT"); }
break;
case 23:
//#line 152 "gram.y"
{ yyval = new ParserVal("SINGLEF"); }
break;
case 24:
//#line 159 "gram.y"
{
            String nombre = val_peek(0).sval;
            String claveFunc = TablaSimbolos.declararVariable(nombre, val_peek(2).sval, "funcion", ambitoActual);
            if (claveFunc != null) {
                TablaSimbolos.agregarAtributo(claveFunc, "TIENE_RET", Boolean.FALSE);
            }
            entrarAmbito(nombre);
            GeneradorTerceto.iniciarAmbito(ambitoActual);
            yyval = val_peek(0);
        }
break;
case 25:
//#line 173 "gram.y"
{
            String nombre = val_peek(0).sval;
            String claveFunc = TablaSimbolos.declararVariable(nombre, "auto", "funcion", ambitoActual);
            if (claveFunc != null) {
                TablaSimbolos.agregarAtributo(claveFunc, "TIENE_RET", Boolean.FALSE);
            }
            entrarAmbito(nombre);
            GeneradorTerceto.iniciarAmbito(ambitoActual);
            yyval = val_peek(0);
        }
break;
case 26:
//#line 187 "gram.y"
{ cant_retornos_auto = 0; }
break;
case 27:
//#line 194 "gram.y"
{
            String mangling = obtenerFuncionActualMangled();
            if (mangling != null) {
                Boolean tieneRet = (Boolean) TablaSimbolos.obtenerAtributo(mangling, "TIENE_RET");
                if (!Boolean.TRUE.equals(tieneRet)) {
                    String tipo = TablaSimbolos.obtenerTipo(mangling);
                    String valDef = "SINGLEF".equals(tipo) ? "0.0" : "0$s";
                    GeneradorTerceto.crearTerceto("RET", valDef, "-", tipo);
                }
            }
            salirAmbito();
        }
break;
case 28:
//#line 206 "gram.y"
{ yyerror("Falta nombre de funcion"); }
break;
case 30:
//#line 212 "gram.y"
{
            String mangling = obtenerFuncionActualMangled();
            if (mangling != null) {
                Boolean tieneRet = (Boolean) TablaSimbolos.obtenerAtributo(mangling, "TIENE_RET");
                if (!Boolean.TRUE.equals(tieneRet)) {
                    String tipo = TablaSimbolos.obtenerTipo(mangling);
                    String valDef = "SINGLEF".equals(tipo) ? "0.0" : "0$s";
                    GeneradorTerceto.crearTerceto("RET", valDef, "-", tipo);
                }
            }
            yyerror("Falta ';' al final de la declaracion de funcion");
            salirAmbito();
        }
break;
case 31:
//#line 226 "gram.y"
{
            if (cant_retornos_auto == 0) {
                yyerror("Ausencia de sentencia de retorno 'RET' en funcion AUTO");
            }
            cant_retornos_auto = -1;
            salirAmbito();
        }
break;
case 32:
//#line 233 "gram.y"
{ yyerror("Falta nombre de funcion"); }
break;
case 34:
//#line 237 "gram.y"
{
            if (cant_retornos_auto == 0) {
                yyerror("Ausencia de sentencia de retorno 'RET' en funcion AUTO");
            }
            cant_retornos_auto = -1;
            yyerror("Falta ';' al final de la declaracion de funcion");
            salirAmbito();
        }
break;
case 38:
//#line 255 "gram.y"
{
            String nombre = val_peek(0).sval;
            TablaSimbolos.declararVariable(nombre, val_peek(1).sval, "parametro", ambitoActual);
            String funcMangled = obtenerFuncionActualMangled();
            if (funcMangled != null) {
                TablaSimbolos.agregarParametro(funcMangled, nombre);
            }
            imprimirRegla("parametro_formal");
        }
break;
case 39:
//#line 265 "gram.y"
{
            String nomClase = val_peek(1).sval;
            String claveClase = TablaSimbolos.AMBITO_GLOBAL + TablaSimbolos.SEPARADOR + nomClase;
            if (!TablaSimbolos.existeSimbolo(claveClase) || !"nombre_clase".equals(TablaSimbolos.obtenerUso(claveClase))) {
                agregarErrorSemantico("Tipo de parametro formal '" + nomClase + "' no declarado o no es una clase valida");
            }
            String nombre = val_peek(0).sval;
            TablaSimbolos.declararVariable(nombre, nomClase, "parametro", ambitoActual);
            String funcMangled = obtenerFuncionActualMangled();
            if (funcMangled != null) {
                TablaSimbolos.agregarParametro(funcMangled, nombre);
            }
            imprimirRegla("parametro_formal");
        }
break;
case 40:
//#line 279 "gram.y"
{ yyerror("Falta de nombre de parametro formal en declaracion de funcion"); }
break;
case 41:
//#line 280 "gram.y"
{ yyerror("Falta de tipo del parametro formal en declaracion de funcion"); }
break;
case 42:
//#line 288 "gram.y"
{
            String nombre = val_peek(0).sval;
            String claveClase = TablaSimbolos.declararVariable(nombre, "clase", "nombre_clase", ambitoActual);
            claseActualValida = (claveClase != null);
            entrarAmbito(nombre);
            yyval = val_peek(0);
        }
break;
case 43:
//#line 300 "gram.y"
{
            claseActualValida = false;
            salirAmbito();
        }
break;
case 44:
//#line 306 "gram.y"
{
            yyerror("Falta ';' al final de la declaracion de clase");
            claseActualValida = false;
            salirAmbito();
        }
break;
case 45:
//#line 315 "gram.y"
{
            yyval = new ParserVal();
        }
break;
case 46:
//#line 319 "gram.y"
{
            String nomClase = obtenerClaseDeAmbito(ambitoActual);
            if (nomClase != null) {
                ArrayList<String> imports = (ArrayList<String>) val_peek(0).obj;
                if (imports != null) {
                    ArrayList<String> importsValidos = new ArrayList<>();
                    for (int i = 0; i < imports.size(); i++) {
                        String imp = imports.get(i);
                        String claveImp = TablaSimbolos.AMBITO_GLOBAL + TablaSimbolos.SEPARADOR + imp;
                        if (!TablaSimbolos.existeSimbolo(claveImp) || !"nombre_clase".equals(TablaSimbolos.obtenerUso(claveImp))) {
                            agregarErrorSemantico("Clase '" + imp + "' especificada en IMPORT FROM no declarada");
                        } else {
                            importsValidos.add(imp);
                        }
                    }
                    if (claseActualValida) {
                        String claveClase = TablaSimbolos.AMBITO_GLOBAL + TablaSimbolos.SEPARADOR + nomClase;
                        TablaSimbolos.registrarImports(claveClase, importsValidos);
                    }
                }
            }
            imprimirRegla("importacion_opcional");
            yyval = val_peek(0);
        }
break;
case 47:
//#line 344 "gram.y"
{
            yyerror("Falta la palabra clave 'FROM' en la declaracion IMPORT");
            yyval = val_peek(0);
        }
break;
case 48:
//#line 352 "gram.y"
{
            yyval = new ParserVal();
        }
break;
case 49:
//#line 356 "gram.y"
{
            String nomClase = obtenerClaseDeAmbito(ambitoActual);
            if (nomClase != null) {
                ArrayList<String> padres = (ArrayList<String>) val_peek(1).obj;
                if (padres != null) {
                    ArrayList<String> padresValidos = new ArrayList<>();
                    for (int i = 0; i < padres.size(); i++) {
                        String padre = padres.get(i);
                        String clavePadre = TablaSimbolos.AMBITO_GLOBAL + TablaSimbolos.SEPARADOR + padre;
                        if (!TablaSimbolos.existeSimbolo(clavePadre) || !"nombre_clase".equals(TablaSimbolos.obtenerUso(clavePadre))) {
                            agregarErrorSemantico("Clase base '" + padre + "' especificada en EXTENDS no declarada");
                        } else if (TablaSimbolos.hayCicloHerencia(nomClase, padre)) {
                            agregarErrorSemantico("Herencia circular detectada entre la clase '" + nomClase + "' y '" + padre + "'");
                        } else if (padresValidos.contains(padre)) {
                            agregarErrorSemantico("Clase base '" + padre + "' duplicada en EXTENDS de la clase '" + nomClase + "'");
                        } else {
                            padresValidos.add(padre);
                        }
                    }
                    if (claseActualValida) {
                        String claveClase = TablaSimbolos.AMBITO_GLOBAL + TablaSimbolos.SEPARADOR + nomClase;
                        TablaSimbolos.registrarHerencia(claveClase, padresValidos);
                    }
                }
            }
            imprimirRegla("herencia_opcional");
            yyval = val_peek(1);
        }
break;
case 50:
//#line 385 "gram.y"
{
            yyerror("Falta nombre o lista de clases a heredar luego de 'EXTENDS'");
            yyval = new ParserVal();
        }
break;
case 55:
//#line 403 "gram.y"
{
            String tipoDato = val_peek(3).sval;
            ArrayList<String> variables = (ArrayList<String>) val_peek(2).obj;
            ArrayList<String> exportTo = (val_peek(1) != null && val_peek(1).obj != null) ? (ArrayList<String>) val_peek(1).obj : null;
            String nomClase = obtenerClaseDeAmbito(ambitoActual);
            String claseClave = (nomClase != null) ? TablaSimbolos.AMBITO_GLOBAL + TablaSimbolos.SEPARADOR + nomClase : null;

            if (variables != null) {
                for (int i = 0; i < variables.size(); i++) {
                    String nombreVar = variables.get(i);
                    String claveAtr = TablaSimbolos.declararVariable(nombreVar, tipoDato, "atributo", ambitoActual);
                    if (claveAtr != null) {
                        if (claseClave != null) {
                            TablaSimbolos.agregarAtributoAClase(claseClave, nombreVar);
                        }
                        if (exportTo != null) {
                            TablaSimbolos.registrarExport(claveAtr, exportTo);
                        }
                    }
                }
            }
        }
break;
case 56:
//#line 431 "gram.y"
{
            String mangling = obtenerFuncionActualMangled();
            if (mangling != null) {
                Boolean tieneRet = (Boolean) TablaSimbolos.obtenerAtributo(mangling, "TIENE_RET");
                if (!Boolean.TRUE.equals(tieneRet)) {
                    String tipo = TablaSimbolos.obtenerTipo(mangling);
                    String valDef = "SINGLEF".equals(tipo) ? "0.0" : "0$s";
                    GeneradorTerceto.crearTerceto("RET", valDef, "-", tipo);
                }
                if (val_peek(1) != null && val_peek(1).obj != null) {
                    ArrayList<String> exports = (ArrayList<String>) val_peek(1).obj;
                    TablaSimbolos.registrarExport(mangling, exports);
                }
            }
            salirAmbito();
        }
break;
case 57:
//#line 449 "gram.y"
{
            String mangling = obtenerFuncionActualMangled();
            if (mangling != null) {
                if (val_peek(1) != null && val_peek(1).obj != null) {
                    ArrayList<String> exports = (ArrayList<String>) val_peek(1).obj;
                    TablaSimbolos.registrarExport(mangling, exports);
                }
            }
            salirAmbito();
        }
break;
case 58:
//#line 463 "gram.y"
{
            String nombre = val_peek(0).sval;
            String claveMet = TablaSimbolos.declararVariable(nombre, val_peek(1).sval, "metodo", ambitoActual);
            if (claveMet != null) {
                TablaSimbolos.agregarAtributo(claveMet, "TIENE_RET", Boolean.FALSE);
            }
            entrarAmbito(nombre);
            GeneradorTerceto.iniciarAmbito(ambitoActual);
            yyval = val_peek(0);
        }
break;
case 59:
//#line 477 "gram.y"
{
            String nombre = val_peek(0).sval;
            String claveMet = TablaSimbolos.declararVariable(nombre, "auto", "metodo", ambitoActual);
            if (claveMet != null) {
                TablaSimbolos.agregarAtributo(claveMet, "TIENE_RET", Boolean.FALSE);
            }
            entrarAmbito(nombre);
            GeneradorTerceto.iniciarAmbito(ambitoActual);
            yyval = val_peek(0);
        }
break;
case 60:
//#line 491 "gram.y"
{
            yyval = new ParserVal();
        }
break;
case 61:
//#line 495 "gram.y"
{
            imprimirRegla("exportacion_opcional");
            yyval = val_peek(0);
        }
break;
case 62:
//#line 500 "gram.y"
{
            yyerror("Falta la palabra clave 'TO' en la declaracion EXPORT");
            yyval = val_peek(0);
        }
break;
case 63:
//#line 510 "gram.y"
{
            String nomClase = val_peek(2).sval;
            String claveClase = TablaSimbolos.AMBITO_GLOBAL + TablaSimbolos.SEPARADOR + nomClase;
            if (!TablaSimbolos.existeSimbolo(claveClase) || !"nombre_clase".equals(TablaSimbolos.obtenerUso(claveClase))) {
                agregarErrorSemantico("Clase '" + nomClase + "' no declarada o no es una clase valida");
            } else {
                ArrayList<String> variables = (ArrayList<String>) val_peek(1).obj;
                if (variables != null) {
                    for (int i = 0; i < variables.size(); i++) {
                        String nombreObj = variables.get(i);
                        TablaSimbolos.declararVariable(nombreObj, nomClase, "objeto", ambitoActual);
                    }
                }
            }
            imprimirRegla("declaracion_de_objeto");
        }
break;
case 64:
//#line 527 "gram.y"
{ yyerror("Falta ';' al final de la declaracion de objeto"); }
break;
case 65:
//#line 534 "gram.y"
{
            String tipoDato = val_peek(2).sval;
            ArrayList<String> variables = (ArrayList<String>) val_peek(1).obj;
            if (variables != null) {
                for (int i = 0; i < variables.size(); i++) {
                    String nombreVar = variables.get(i);
                    String clave = TablaSimbolos.declararVariable(nombreVar, tipoDato, "variable", ambitoActual);
                    if (clave != null) {
                        TablaSimbolos.agregarAtributo(clave, "COMPTIME", Boolean.TRUE);
                    }
                }
            }
        }
break;
case 66:
//#line 548 "gram.y"
{ yyerror("Falta ';' al final de la declaracion comptime"); }
break;
case 67:
//#line 550 "gram.y"
{ yyerror("Falta el tipo de dato en la declaracion comptime"); }
break;
case 68:
//#line 552 "gram.y"
{
            yyerror("Falta el tipo de dato en la declaracion comptime");
            yyerror("Falta ';' al final de la declaracion comptime");
        }
break;
case 70:
//#line 562 "gram.y"
{ imprimirRegla("sentencia_ejecutable"); }
break;
case 77:
//#line 573 "gram.y"
{ yyerror("Sentencia ejecutable malformada o falta ';' previo"); }
break;
case 78:
//#line 578 "gram.y"
{
            if (val_peek(0) != null && val_peek(0).sval != null) {
                String lexema = val_peek(2).sval;
                String claveVar = TablaSimbolos.buscarIdentificador(lexema, ambitoActual);
                if (claveVar != null) {
                    boolean esComp = TablaSimbolos.esComptime(claveVar);
                    String tipoVar = TablaSimbolos.obtenerTipo(claveVar);
                    String tipoExp = (String) val_peek(0).obj;

                    if (esComp) {
                        if (!esConstante(val_peek(0))) {
                            agregarErrorSemantico("La variable comptime '" + lexema + "' no puede recibir una expresion dependiente de tiempo de ejecucion");
                        } else {
                            if (tipoExp != null) {
                                if (tipoVar != null && tipoVar.equals(tipoExp)) {
                                    TablaSimbolos.guardarValorComptime(claveVar, val_peek(0).sval);
                                } else if ("SHORTINT".equals(tipoVar) && "SINGLEF".equals(tipoExp)) {
                                    agregarErrorSemantico("Incompatibilidad de tipos en la asignacion: no se puede asignar SINGLEF a la variable SHORTINT '" + lexema + "' sin conversion explicita 'tos'");
                                } else if ("SINGLEF".equals(tipoVar) && "SHORTINT".equals(tipoExp)) {
                                    agregarErrorSemantico("Incompatibilidad de tipos en la asignacion: no se puede asignar SHORTINT a la variable SINGLEF '" + lexema + "'");
                                } else {
                                    agregarErrorSemantico("Incompatibilidad de tipos en la asignacion a '" + lexema + "': se esperaba " + tipoVar + " pero se encontro " + tipoExp);
                                }
                            }
                        }
                    } else {
                        if (tipoExp != null) {
                            if (tipoVar != null && tipoVar.equals(tipoExp)) {
                                GeneradorTerceto.crearTerceto(":=", claveVar, val_peek(0).sval, tipoVar);
                            } else if ("SHORTINT".equals(tipoVar) && "SINGLEF".equals(tipoExp)) {
                                agregarErrorSemantico("Incompatibilidad de tipos en la asignacion: no se puede asignar SINGLEF a la variable SHORTINT '" + lexema + "' sin conversion explicita 'tos'");
                            } else if ("SINGLEF".equals(tipoVar) && "SHORTINT".equals(tipoExp)) {
                                agregarErrorSemantico("Incompatibilidad de tipos en la asignacion: no se puede asignar SHORTINT a la variable SINGLEF '" + lexema + "'");
                            } else {
                                agregarErrorSemantico("Incompatibilidad de tipos en la asignacion a '" + lexema + "': se esperaba " + tipoVar + " pero se encontro " + tipoExp);
                            }
                        }
                    }
                } else {
                    agregarErrorSemantico("Variable '" + lexema + "' no declarada o no visible en el ambito actual (" + ambitoActual + ")");
                }
            }
        }
break;
case 79:
//#line 622 "gram.y"
{
            if (val_peek(2) != null && val_peek(2).sval != null && val_peek(0) != null && val_peek(0).sval != null) {
                String tipoVar = (String) val_peek(2).obj;
                String tipoExp = (String) val_peek(0).obj;

                if (tipoExp != null && tipoVar != null) {
                    if (tipoVar.equals(tipoExp)) {
                        GeneradorTerceto.crearTerceto(":=", val_peek(2).sval, val_peek(0).sval, tipoVar);
                    } else if ("SHORTINT".equals(tipoVar) && "SINGLEF".equals(tipoExp)) {
                        agregarErrorSemantico("Incompatibilidad de tipos en la asignacion a '" + val_peek(2).sval + "': no se puede asignar SINGLEF a SHORTINT sin conversion explicita 'tos'");
                    } else if ("SINGLEF".equals(tipoVar) && "SHORTINT".equals(tipoExp)) {
                        agregarErrorSemantico("Incompatibilidad de tipos en la asignacion a '" + val_peek(2).sval + "': no se puede asignar SHORTINT a SINGLEF");
                    } else {
                        agregarErrorSemantico("Incompatibilidad de tipos en la asignacion a '" + val_peek(2).sval + "': se esperaba " + tipoVar + " pero se encontro " + tipoExp);
                    }
                }
            }
        }
break;
case 80:
//#line 644 "gram.y"
{
            String nomObj = val_peek(3).sval;
            String claveObj = TablaSimbolos.buscarIdentificador(nomObj, ambitoActual);

            if (claveObj == null) {
                agregarErrorSemantico("Objeto '" + nomObj + "' no declarado o no visible en el ambito actual (" + ambitoActual + ")");
                yyval = new ParserVal();
            } else {
                String uso = TablaSimbolos.obtenerUso(claveObj);
                String nomClase = TablaSimbolos.obtenerTipo(claveObj);
                String claveClase = TablaSimbolos.AMBITO_GLOBAL + TablaSimbolos.SEPARADOR + nomClase;
                boolean esObjeto = ("objeto".equals(uso) || "parametro".equals(uso)) && 
                    (TablaSimbolos.existeSimbolo(claveClase) && "nombre_clase".equals(TablaSimbolos.obtenerUso(claveClase)));

                if (!esObjeto) {
                    agregarErrorSemantico("El identificador '" + nomObj + "' no es un objeto (uso actual: " + uso + ")");
                    yyval = new ParserVal();
                } else {
                    String lexConst = val_peek(1).sval;
                    int idx = Integer.parseInt(lexConst);
                    ArrayList<String> atrs = TablaSimbolos.obtenerAtributosClase(claveClase);
                    int cantAtr = atrs.size();

                    if (idx < 0 || idx >= cantAtr) {
                        agregarErrorSemantico("Indice posicional [" + idx + "$s] fuera de rango para la clase '" + nomClase + "' (cantidad de atributos: " + cantAtr + ")");
                        yyval = new ParserVal();
                    } else {
                        String nomAtr = atrs.get(idx);
                        String claveAtr = claveClase + TablaSimbolos.SEPARADOR + nomAtr;
                        String tipoAtr = TablaSimbolos.obtenerTipo(claveAtr);

                        /* Chequeo de visibilidad (Tema 26)*/
                        String claseActual = obtenerClaseDeAmbito(ambitoActual);
                        boolean accesoValido = true;
                        if (claseActual != null && !claseActual.equals(nomClase)) {
                            String claveClaseActual = TablaSimbolos.AMBITO_GLOBAL + TablaSimbolos.SEPARADOR + claseActual;
                            ArrayList<String> imports = TablaSimbolos.obtenerImports(claveClaseActual);
                            if (imports == null || !imports.contains(nomClase)) {
                                agregarErrorSemantico("La clase '" + claseActual + "' no importa a la clase '" + nomClase + "'");
                                accesoValido = false;
                            } else if (!TablaSimbolos.estaExportadoA(claveAtr, claseActual)) {
                                agregarErrorSemantico("El atributo '" + nomAtr + "' de la clase '" + nomClase + "' no esta exportado a la clase '" + claseActual + "'");
                                accesoValido = false;
                            }
                        }

                        if (accesoValido) {
                            ParserVal pv = new ParserVal(claveObj + "[" + idx + "$s]");
                            pv.obj = tipoAtr;
                            yyval = pv;
                        } else {
                            yyval = new ParserVal();
                        }
                    }
                }
            }
        }
break;
case 81:
//#line 702 "gram.y"
{
            String nomObj = val_peek(3).sval;
            String claveObj = TablaSimbolos.buscarIdentificador(nomObj, ambitoActual);

            if (claveObj == null) {
                agregarErrorSemantico("Objeto '" + nomObj + "' no declarado o no visible en el ambito actual (" + ambitoActual + ")");
                yyval = new ParserVal();
            } else {
                String uso = TablaSimbolos.obtenerUso(claveObj);
                String nomClase = TablaSimbolos.obtenerTipo(claveObj);
                String claveClase = TablaSimbolos.AMBITO_GLOBAL + TablaSimbolos.SEPARADOR + nomClase;
                boolean esObjeto = ("objeto".equals(uso) || "parametro".equals(uso)) && 
                    (TablaSimbolos.existeSimbolo(claveClase) && "nombre_clase".equals(TablaSimbolos.obtenerUso(claveClase)));

                if (!esObjeto) {
                    agregarErrorSemantico("El identificador '" + nomObj + "' no es un objeto (uso actual: " + uso + ")");
                    yyval = new ParserVal();
                } else {
                    String nomIdx = val_peek(1).sval;
                    String claveVarIdx = TablaSimbolos.buscarIdentificador(nomIdx, ambitoActual);

                    if (claveVarIdx == null) {
                        agregarErrorSemantico("Variable de indice '" + nomIdx + "' no declarada");
                        yyval = new ParserVal();
                    } else {
                        String tipoIdx = TablaSimbolos.obtenerTipo(claveVarIdx);
                        if (!"SHORTINT".equals(tipoIdx)) {
                            agregarErrorSemantico("El indice de acceso posicional debe ser de tipo SHORTINT (se encontro: " + tipoIdx + ")");
                            yyval = new ParserVal();
                        } else {
                            ArrayList<String> atrs = TablaSimbolos.obtenerAtributosClase(claveClase);
                            String tipoDefecto = (!atrs.isEmpty()) ? TablaSimbolos.obtenerTipo(claveClase + TablaSimbolos.SEPARADOR + atrs.get(0)) : "SHORTINT";
                            if (tipoDefecto == null) tipoDefecto = "SHORTINT";

                            ParserVal pv = new ParserVal(claveObj + "[" + claveVarIdx + "]");
                            pv.obj = tipoDefecto;
                            yyval = pv;
                        }
                    }
                }
            }
        }
break;
case 82:
//#line 745 "gram.y"
{ yyerror("Acceso posicional invalido: solo se permite constante SHORTINT positiva o identificador"); yyval = new ParserVal(); }
break;
case 83:
//#line 751 "gram.y"
{
            yyval = val_peek(0);
        }
break;
case 84:
//#line 755 "gram.y"
{
            if (val_peek(2) != null && val_peek(2).sval != null && val_peek(0) != null && val_peek(0).sval != null) {
                String tipo1 = (String) val_peek(2).obj;
                String tipo2 = (String) val_peek(0).obj;
                if (tipo1 != null && tipo2 != null) {
                    if (tipo1.equals(tipo2)) {
                        if (esConstante(val_peek(2)) && esConstante(val_peek(0))) {
                            String res = evaluarOperacionEstatica("+", val_peek(2).sval, val_peek(0).sval, tipo1);
                            ParserVal pv = new ParserVal(res);
                            pv.obj = tipo1;
                            yyval = pv;
                        } else {
                            int idTerceto = GeneradorTerceto.crearTerceto("+", val_peek(2).sval, val_peek(0).sval, tipo1);
                            ParserVal pv = new ParserVal("[" + idTerceto + "]");
                            pv.obj = tipo1;
                            yyval = pv;
                        }
                    } else {
                        agregarErrorSemantico("Incompatibilidad de tipos en operacion '+': no se puede operar " + tipo1 + " con " + tipo2 + " (requiere conversion explicita 'tos')");
                        yyval = new ParserVal();
                    }
                } else {
                    yyval = new ParserVal();
                }
            } else {
                yyval = new ParserVal();
            }
        }
break;
case 85:
//#line 784 "gram.y"
{
            if (val_peek(2) != null && val_peek(2).sval != null && val_peek(0) != null && val_peek(0).sval != null) {
                String tipo1 = (String) val_peek(2).obj;
                String tipo2 = (String) val_peek(0).obj;
                if (tipo1 != null && tipo2 != null) {
                    if (tipo1.equals(tipo2)) {
                        if (esConstante(val_peek(2)) && esConstante(val_peek(0))) {
                            String res = evaluarOperacionEstatica("-", val_peek(2).sval, val_peek(0).sval, tipo1);
                            ParserVal pv = new ParserVal(res);
                            pv.obj = tipo1;
                            yyval = pv;
                        } else {
                            int idTerceto = GeneradorTerceto.crearTerceto("-", val_peek(2).sval, val_peek(0).sval, tipo1);
                            ParserVal pv = new ParserVal("[" + idTerceto + "]");
                            pv.obj = tipo1;
                            yyval = pv;
                        }
                    } else {
                        agregarErrorSemantico("Incompatibilidad de tipos en operacion '-': no se puede operar " + tipo1 + " con " + tipo2 + " (requiere conversion explicita 'tos')");
                        yyval = new ParserVal();
                    }
                } else {
                    yyval = new ParserVal();
                }
            } else {
                yyval = new ParserVal();
            }
        }
break;
case 86:
//#line 812 "gram.y"
{ yyerror("Falta un operando en la expresion"); yyval = new ParserVal(); }
break;
case 87:
//#line 813 "gram.y"
{ yyerror("Falta un operando en la expresion"); yyval = new ParserVal(); }
break;
case 88:
//#line 814 "gram.y"
{ yyerror("Falta un operando en la expresion"); yyval = new ParserVal(); }
break;
case 89:
//#line 818 "gram.y"
{
            yyval = val_peek(0);
        }
break;
case 90:
//#line 822 "gram.y"
{
            if (val_peek(2) != null && val_peek(2).sval != null && val_peek(0) != null && val_peek(0).sval != null) {
                String tipo1 = (String) val_peek(2).obj;
                String tipo2 = (String) val_peek(0).obj;
                if (tipo1 != null && tipo2 != null) {
                    if (tipo1.equals(tipo2)) {
                        if (esConstante(val_peek(2)) && esConstante(val_peek(0))) {
                            String res = evaluarOperacionEstatica("*", val_peek(2).sval, val_peek(0).sval, tipo1);
                            ParserVal pv = new ParserVal(res);
                            pv.obj = tipo1;
                            yyval = pv;
                        } else {
                            int idTerceto = GeneradorTerceto.crearTerceto("*", val_peek(2).sval, val_peek(0).sval, tipo1);
                            ParserVal pv = new ParserVal("[" + idTerceto + "]");
                            pv.obj = tipo1;
                            yyval = pv;
                        }
                    } else {
                        agregarErrorSemantico("Incompatibilidad de tipos en operacion '*': no se puede operar " + tipo1 + " con " + tipo2 + " (requiere conversion explicita 'tos')");
                        yyval = new ParserVal();
                    }
                } else {
                    yyval = new ParserVal();
                }
            } else {
                yyval = new ParserVal();
            }
        }
break;
case 91:
//#line 851 "gram.y"
{
            if (val_peek(2) != null && val_peek(2).sval != null && val_peek(0) != null && val_peek(0).sval != null) {
                String tipo1 = (String) val_peek(2).obj;
                String tipo2 = (String) val_peek(0).obj;
                if (tipo1 != null && tipo2 != null) {
                    if (tipo1.equals(tipo2)) {
                        if (esConstante(val_peek(2)) && esConstante(val_peek(0))) {
                            String res = evaluarOperacionEstatica("/", val_peek(2).sval, val_peek(0).sval, tipo1);
                            ParserVal pv = new ParserVal(res);
                            pv.obj = tipo1;
                            yyval = pv;
                        } else {
                            int idTerceto = GeneradorTerceto.crearTerceto("/", val_peek(2).sval, val_peek(0).sval, tipo1);
                            ParserVal pv = new ParserVal("[" + idTerceto + "]");
                            pv.obj = tipo1;
                            yyval = pv;
                        }
                    } else {
                        agregarErrorSemantico("Incompatibilidad de tipos en operacion '/': no se puede operar " + tipo1 + " con " + tipo2 + " (requiere conversion explicita 'tos')");
                        yyval = new ParserVal();
                    }
                } else {
                    yyval = new ParserVal();
                }
            } else {
                yyval = new ParserVal();
            }
        }
break;
case 92:
//#line 879 "gram.y"
{ yyerror("Falta un operando en la expresion"); yyval = new ParserVal(); }
break;
case 93:
//#line 880 "gram.y"
{ yyerror("Falta un operando en la expresion"); yyval = new ParserVal(); }
break;
case 94:
//#line 881 "gram.y"
{ yyerror("Falta un operando en la expresion"); yyval = new ParserVal(); }
break;
case 95:
//#line 882 "gram.y"
{ yyerror("Falta un operando en la expresion"); yyval = new ParserVal(); }
break;
case 96:
//#line 883 "gram.y"
{ yyerror("Falta operador en la expresion"); yyval = new ParserVal(); }
break;
case 97:
//#line 884 "gram.y"
{ yyerror("Falta operador en la expresion"); yyval = new ParserVal(); }
break;
case 98:
//#line 885 "gram.y"
{ yyerror("Falta operador en la expresion"); yyval = new ParserVal(); }
break;
case 99:
//#line 890 "gram.y"
{
            String lexema = val_peek(0).sval;
            String claveVar = TablaSimbolos.buscarIdentificador(lexema, ambitoActual);
            if (claveVar != null) {
                String tipo = TablaSimbolos.obtenerTipo(claveVar);
                boolean esComp = TablaSimbolos.esComptime(claveVar);
                if (esComp) {
                    if (TablaSimbolos.tieneValorComptime(claveVar)) {
                        String val = TablaSimbolos.obtenerValorComptime(claveVar);
                        ParserVal pv = new ParserVal(val);
                        pv.obj = tipo;
                        yyval = pv;
                    } else {
                        agregarErrorSemantico("La variable comptime '" + lexema + "' se utiliza antes de ser inicializada con un valor estatico");
                        yyval = new ParserVal();
                    }
                } else {
                    ParserVal pv = new ParserVal(claveVar);
                    pv.obj = tipo;
                    yyval = pv;
                }
            } else {
                agregarErrorSemantico("Variable '" + lexema + "' no declarada o no visible en el ambito actual (" + ambitoActual + ")");
                yyval = new ParserVal();
            }
        }
break;
case 100:
//#line 917 "gram.y"
{
            String lexema = val_peek(0).sval;
            int valor = Integer.parseInt(lexema);
            if (valor > AnalizadorLexico.ValorMaximoInt) {
                yyerror("Constante shortint positiva fuera de rango (" + lexema + "). Rango permitido: [-128, 127]");
            }
            TablaSimbolos.agregarAtributo(lexema, TablaSimbolos.TIPO, "SHORTINT");
            ParserVal pv = new ParserVal(lexema);
            pv.obj = "SHORTINT";
            yyval = pv;
        }
break;
case 101:
//#line 929 "gram.y"
{
            String lexema = val_peek(0).sval;
            double valor = Double.parseDouble(lexema);
            if (valor != 0.0 && (valor < AnalizadorLexico.ValorMinimoFloat || valor > AnalizadorLexico.ValorMaximoFloat)) {
                yyerror("Constante singlef fuera de rango (" + lexema + ")");
            }
            TablaSimbolos.agregarAtributo(lexema, TablaSimbolos.TIPO, "SINGLEF");
            ParserVal pv = new ParserVal(lexema);
            pv.obj = "SINGLEF";
            yyval = pv;
        }
break;
case 102:
//#line 941 "gram.y"
{
            String lexema = val_peek(2).sval;
            String claveFunc = TablaSimbolos.buscarIdentificador(lexema, ambitoActual);
            if (claveFunc == null) {
                agregarErrorSemantico("Funcion '" + lexema + "' no declarada o no visible en el ambito actual (" + ambitoActual + ")");
                yyval = new ParserVal();
            } else {
                String uso = TablaSimbolos.obtenerUso(claveFunc);
                if (!"funcion".equals(uso)) {
                    agregarErrorSemantico("El identificador '" + lexema + "' no es una funcion (uso actual: " + uso + ")");
                    yyval = new ParserVal();
                } else {
                    String tipoRetorno = TablaSimbolos.obtenerTipo(claveFunc);
                    ArrayList<String> paramsFormales = TablaSimbolos.obtenerParametros(claveFunc);
                    int cantEsperada = paramsFormales.size();

                    if (cantEsperada != 0) {
                        agregarErrorSemantico("Cantidad incorrecta de parametros en invocacion a '" + lexema + "': se esperaban " + cantEsperada + " pero se recibieron 0");
                        yyval = new ParserVal();
                    } else {
                        int idCall = GeneradorTerceto.crearTerceto("CALL", claveFunc, "-", tipoRetorno);
                        ParserVal pv = new ParserVal("[" + idCall + "]");
                        pv.obj = tipoRetorno;
                        yyval = pv;
                    }
                }
            }
        }
break;
case 103:
//#line 970 "gram.y"
{
            String lexema = val_peek(3).sval;
            String claveFunc = TablaSimbolos.buscarIdentificador(lexema, ambitoActual);
            if (claveFunc == null) {
                agregarErrorSemantico("Funcion '" + lexema + "' no declarada o no visible en el ambito actual (" + ambitoActual + ")");
                yyval = new ParserVal();
            } else {
                String uso = TablaSimbolos.obtenerUso(claveFunc);
                if (!"funcion".equals(uso)) {
                    agregarErrorSemantico("El identificador '" + lexema + "' no es una funcion (uso actual: " + uso + ")");
                    yyval = new ParserVal();
                } else {
                    String tipoRetorno = TablaSimbolos.obtenerTipo(claveFunc);
                    ArrayList<String> paramsFormales = TablaSimbolos.obtenerParametros(claveFunc);
                    int cantEsperada = paramsFormales.size();
                    ArrayList<ParserVal> paramsReales = (val_peek(1) != null && val_peek(1).obj != null) ? (ArrayList<ParserVal>) val_peek(1).obj : new ArrayList<>();
                    int cantRecibida = paramsReales.size();

                    if (cantRecibida != cantEsperada) {
                        agregarErrorSemantico("Cantidad incorrecta de parametros en invocacion a '" + lexema + "': se esperaban " + cantEsperada + " pero se recibieron " + cantRecibida);
                        yyval = new ParserVal();
                    } else {
                        boolean paramsOk = true;
                        for (int i = 0; i < cantEsperada; i++) {
                            String nomFormal = paramsFormales.get(i);
                            String claveFormal = claveFunc + TablaSimbolos.SEPARADOR + nomFormal;
                            String tipoFormal = TablaSimbolos.obtenerTipo(claveFormal);
                            ParserVal pReal = paramsReales.get(i);
                            String tipoReal = (pReal != null) ? (String) pReal.obj : null;

                            if (tipoFormal != null && tipoReal != null && !tipoFormal.equals(tipoReal)) {
                                agregarErrorSemantico("Incompatibilidad de tipos en parametro " + (i + 1) + " al invocar a '" + lexema + "': se esperaba " + tipoFormal + " pero se recibio " + tipoReal);
                                paramsOk = false;
                            } else if (pReal != null && pReal.sval != null) {
                                GeneradorTerceto.crearTerceto(":=", claveFormal, pReal.sval, tipoFormal);
                            }
                        }

                        if (paramsOk) {
                            int idCall = GeneradorTerceto.crearTerceto("CALL", claveFunc, "-", tipoRetorno);
                            ParserVal pv = new ParserVal("[" + idCall + "]");
                            pv.obj = tipoRetorno;
                            yyval = pv;
                        } else {
                            yyval = new ParserVal();
                        }
                    }
                }
            }
        }
break;
case 105:
//#line 1022 "gram.y"
{
            yyval = val_peek(0);
        }
break;
case 106:
//#line 1026 "gram.y"
{
            yyval = val_peek(0);
        }
break;
case 107:
//#line 1030 "gram.y"
{
            yyval = val_peek(0);
        }
break;
case 108:
//#line 1034 "gram.y"
{
            yyval = val_peek(0);
        }
break;
case 109:
//#line 1041 "gram.y"
{
            String nomObj = val_peek(4).sval;
            String claveObj = TablaSimbolos.buscarIdentificador(nomObj, ambitoActual);

            if (claveObj == null) {
                agregarErrorSemantico("Objeto '" + nomObj + "' no declarado o no visible en el ambito actual (" + ambitoActual + ")");
                yyval = new ParserVal();
            } else {
                String uso = TablaSimbolos.obtenerUso(claveObj);
                String nomClase = TablaSimbolos.obtenerTipo(claveObj);
                String claveClase = TablaSimbolos.AMBITO_GLOBAL + TablaSimbolos.SEPARADOR + nomClase;
                boolean esObjeto = ("objeto".equals(uso) || "parametro".equals(uso)) && 
                    (TablaSimbolos.existeSimbolo(claveClase) && "nombre_clase".equals(TablaSimbolos.obtenerUso(claveClase)));

                if (!esObjeto) {
                    agregarErrorSemantico("El identificador '" + nomObj + "' no es un objeto (uso actual: " + uso + ")");
                    yyval = new ParserVal();
                } else {
                    String nomMetodo = val_peek(2).sval;

                    /* Buscar metodo con herencia multiple (Tema 30 - Desambiguado Opcion 1)*/
                    String claveMet = TablaSimbolos.buscarMiembroEnJerarquia(nomClase, nomMetodo);
                    if (claveMet == null) {
                        agregarErrorSemantico("Metodo '" + nomMetodo + "' no encontrado en la clase '" + nomClase + "' ni en su jerarquia de herencia");
                        yyval = new ParserVal();
                    } else {
                        /* Chequeo de visibilidad (Tema 26)*/
                        String claseActual = obtenerClaseDeAmbito(ambitoActual);
                        boolean accesoValido = true;
                        String claseDueniaMetodo = obtenerClaseDeAmbito(TablaSimbolos.obtenerAmbito(claveMet));
                        if (claseActual != null && claseDueniaMetodo != null && !claseActual.equals(claseDueniaMetodo)) {
                            String claveClaseActual = TablaSimbolos.AMBITO_GLOBAL + TablaSimbolos.SEPARADOR + claseActual;
                            ArrayList<String> imports = TablaSimbolos.obtenerImports(claveClaseActual);
                            if (imports == null || !imports.contains(claseDueniaMetodo)) {
                                agregarErrorSemantico("La clase '" + claseActual + "' no importa a la clase '" + claseDueniaMetodo + "'");
                                accesoValido = false;
                            } else if (!TablaSimbolos.estaExportadoA(claveMet, claseActual)) {
                                agregarErrorSemantico("El metodo '" + nomMetodo + "' de la clase '" + claseDueniaMetodo + "' no esta exportado a la clase '" + claseActual + "'");
                                accesoValido = false;
                            }
                        }

                        if (accesoValido) {
                            String tipoRetorno = TablaSimbolos.obtenerTipo(claveMet);
                            ArrayList<String> paramsFormales = TablaSimbolos.obtenerParametros(claveMet);
                            int cantEsperada = paramsFormales.size();

                            if (cantEsperada != 0) {
                                agregarErrorSemantico("Cantidad incorrecta de parametros en invocacion a metodo '" + nomMetodo + "': se esperaban " + cantEsperada + " pero se recibieron 0");
                                yyval = new ParserVal();
                            } else {
                                int idCall = GeneradorTerceto.crearTerceto("CALL", claveMet, "-", tipoRetorno);
                                ParserVal pv = new ParserVal("[" + idCall + "]");
                                pv.obj = tipoRetorno;
                                yyval = pv;
                            }
                        } else {
                            yyval = new ParserVal();
                        }
                    }
                }
            }
        }
break;
case 110:
//#line 1105 "gram.y"
{
            String nomObj = val_peek(5).sval;
            String claveObj = TablaSimbolos.buscarIdentificador(nomObj, ambitoActual);

            if (claveObj == null) {
                agregarErrorSemantico("Objeto '" + nomObj + "' no declarado o no visible en el ambito actual (" + ambitoActual + ")");
                yyval = new ParserVal();
            } else {
                String uso = TablaSimbolos.obtenerUso(claveObj);
                String nomClase = TablaSimbolos.obtenerTipo(claveObj);
                String claveClase = TablaSimbolos.AMBITO_GLOBAL + TablaSimbolos.SEPARADOR + nomClase;
                boolean esObjeto = ("objeto".equals(uso) || "parametro".equals(uso)) && 
                    (TablaSimbolos.existeSimbolo(claveClase) && "nombre_clase".equals(TablaSimbolos.obtenerUso(claveClase)));

                if (!esObjeto) {
                    agregarErrorSemantico("El identificador '" + nomObj + "' no es un objeto (uso actual: " + uso + ")");
                    yyval = new ParserVal();
                } else {
                    String nomMetodo = val_peek(3).sval;

                    String claveMet = TablaSimbolos.buscarMiembroEnJerarquia(nomClase, nomMetodo);
                    if (claveMet == null) {
                        agregarErrorSemantico("Metodo '" + nomMetodo + "' no encontrado en la clase '" + nomClase + "' ni en su jerarquia de herencia");
                        yyval = new ParserVal();
                    } else {
                        /* Chequeo de visibilidad (Tema 26)*/
                        String claseActual = obtenerClaseDeAmbito(ambitoActual);
                        boolean accesoValido = true;
                        String claseDueniaMetodo = obtenerClaseDeAmbito(TablaSimbolos.obtenerAmbito(claveMet));
                        if (claseActual != null && claseDueniaMetodo != null && !claseActual.equals(claseDueniaMetodo)) {
                            String claveClaseActual = TablaSimbolos.AMBITO_GLOBAL + TablaSimbolos.SEPARADOR + claseActual;
                            ArrayList<String> imports = TablaSimbolos.obtenerImports(claveClaseActual);
                            if (imports == null || !imports.contains(claseDueniaMetodo)) {
                                agregarErrorSemantico("La clase '" + claseActual + "' no importa a la clase '" + claseDueniaMetodo + "'");
                                accesoValido = false;
                            } else if (!TablaSimbolos.estaExportadoA(claveMet, claseActual)) {
                                agregarErrorSemantico("El metodo '" + nomMetodo + "' de la clase '" + claseDueniaMetodo + "' no esta exportado a la clase '" + claseActual + "'");
                                accesoValido = false;
                            }
                        }

                        if (accesoValido) {
                            String tipoRetorno = TablaSimbolos.obtenerTipo(claveMet);
                            ArrayList<String> paramsFormales = TablaSimbolos.obtenerParametros(claveMet);
                            int cantEsperada = paramsFormales.size();
                            ArrayList<ParserVal> paramsReales = (val_peek(1) != null && val_peek(1).obj != null) ? (ArrayList<ParserVal>) val_peek(1).obj : new ArrayList<>();
                            int cantRecibida = paramsReales.size();

                            if (cantRecibida != cantEsperada) {
                                agregarErrorSemantico("Cantidad incorrecta de parametros en invocacion a metodo '" + nomMetodo + "': se esperaban " + cantEsperada + " pero se recibieron " + cantRecibida);
                                yyval = new ParserVal();
                            } else {
                                boolean paramsOk = true;
                                for (int i = 0; i < cantEsperada; i++) {
                                    String nomFormal = paramsFormales.get(i);
                                    String claveFormal = claveMet + TablaSimbolos.SEPARADOR + nomFormal;
                                    String tipoFormal = TablaSimbolos.obtenerTipo(claveFormal);
                                    ParserVal pReal = paramsReales.get(i);
                                    String tipoReal = (pReal != null) ? (String) pReal.obj : null;

                                    if (tipoFormal != null && tipoReal != null && !tipoFormal.equals(tipoReal)) {
                                        agregarErrorSemantico("Incompatibilidad de tipos en parametro " + (i + 1) + " al invocar metodo '" + nomMetodo + "': se esperaba " + tipoFormal + " pero se recibio " + tipoReal);
                                        paramsOk = false;
                                    } else if (pReal != null && pReal.sval != null) {
                                        GeneradorTerceto.crearTerceto(":=", claveFormal, pReal.sval, tipoFormal);
                                    }
                                }

                                if (paramsOk) {
                                    int idCall = GeneradorTerceto.crearTerceto("CALL", claveMet, "-", tipoRetorno);
                                    ParserVal pv = new ParserVal("[" + idCall + "]");
                                    pv.obj = tipoRetorno;
                                    yyval = pv;
                                } else {
                                    yyval = new ParserVal();
                                }
                            }
                        } else {
                            yyval = new ParserVal();
                        }
                    }
                }
            }
        }
break;
case 111:
//#line 1193 "gram.y"
{
            ArrayList<ParserVal> lista = new ArrayList<>();
            lista.add(val_peek(0));
            ParserVal pv = new ParserVal();
            pv.obj = lista;
            yyval = pv;
        }
break;
case 112:
//#line 1201 "gram.y"
{
            ArrayList<ParserVal> lista = (ArrayList<ParserVal>) val_peek(2).obj;
            lista.add(val_peek(0));
            yyval = val_peek(2);
        }
break;
case 113:
//#line 1211 "gram.y"
{
            if (val_peek(1) != null && val_peek(1).sval != null) {
                String lexema = val_peek(4).sval;
                String claveVar = TablaSimbolos.buscarIdentificador(lexema, ambitoActual);
                if (claveVar != null) {
                    boolean esComp = TablaSimbolos.esComptime(claveVar);
                    String tipoVar = TablaSimbolos.obtenerTipo(claveVar);
                    String tipoExp = (String) val_peek(1).obj;

                    if (esComp) {
                        if (!esConstante(val_peek(1))) {
                            agregarErrorSemantico("La variable comptime '" + lexema + "' no puede recibir una expresion dependiente de tiempo de ejecucion");
                            yyval = new ParserVal();
                        } else {
                            if (tipoExp != null) {
                                if (tipoVar != null && tipoVar.equals(tipoExp)) {
                                    TablaSimbolos.guardarValorComptime(claveVar, val_peek(1).sval);
                                    ParserVal pv = new ParserVal(val_peek(1).sval);
                                    pv.obj = tipoVar;
                                    yyval = pv;
                                } else if ("SHORTINT".equals(tipoVar) && "SINGLEF".equals(tipoExp)) {
                                    agregarErrorSemantico("Incompatibilidad de tipos en la asignacion: no se puede asignar SINGLEF a la variable SHORTINT '" + lexema + "' sin conversion explicita 'tos'");
                                    yyval = new ParserVal();
                                } else if ("SINGLEF".equals(tipoVar) && "SHORTINT".equals(tipoExp)) {
                                    agregarErrorSemantico("Incompatibilidad de tipos en la asignacion: no se puede asignar SHORTINT a la variable SINGLEF '" + lexema + "'");
                                    yyval = new ParserVal();
                                } else {
                                    agregarErrorSemantico("Incompatibilidad de tipos en la asignacion a '" + lexema + "': se esperaba " + tipoVar + " pero se encontro " + tipoExp);
                                    yyval = new ParserVal();
                                }
                            } else {
                                yyval = new ParserVal();
                            }
                        }
                    } else {
                        if (tipoExp != null) {
                            if (tipoVar != null && tipoVar.equals(tipoExp)) {
                                int idTerceto = GeneradorTerceto.crearTerceto(":=", claveVar, val_peek(1).sval, tipoVar);
                                ParserVal pv = new ParserVal("[" + idTerceto + "]");
                                pv.obj = tipoVar;
                                yyval = pv;
                            } else if ("SHORTINT".equals(tipoVar) && "SINGLEF".equals(tipoExp)) {
                                agregarErrorSemantico("Incompatibilidad de tipos en la asignacion: no se puede asignar SINGLEF a la variable SHORTINT '" + lexema + "' sin conversion explicita 'tos'");
                                yyval = new ParserVal();
                            } else if ("SINGLEF".equals(tipoVar) && "SHORTINT".equals(tipoExp)) {
                                agregarErrorSemantico("Incompatibilidad de tipos en la asignacion: no se puede asignar SHORTINT a la variable SINGLEF '" + lexema + "'");
                                yyval = new ParserVal();
                            } else {
                                agregarErrorSemantico("Incompatibilidad de tipos en la asignacion a '" + lexema + "': se esperaba " + tipoVar + " pero se encontro " + tipoExp);
                                yyval = new ParserVal();
                            }
                        } else {
                            yyval = new ParserVal();
                        }
                    }
                } else {
                    agregarErrorSemantico("Variable '" + lexema + "' no declarada o no visible en el ambito actual (" + ambitoActual + ")");
                    yyval = new ParserVal();
                }
            } else {
                yyval = new ParserVal();
            }
        }
break;
case 114:
//#line 1275 "gram.y"
{
            yyerror("Uso del simbolo ':=' donde debe usarse '=' en asignacion dentro de expresion");
            yyval = new ParserVal();
        }
break;
case 115:
//#line 1283 "gram.y"
{
            String lexemaPos = val_peek(0).sval;
            String claveNeg = TablaSimbolos.convertirANegativo(lexemaPos);
            TablaSimbolos.agregarAtributo(claveNeg, TablaSimbolos.TIPO, "SHORTINT");
            ParserVal pv = new ParserVal("-" + lexemaPos);
            pv.obj = "SHORTINT";
            yyval = pv;
        }
break;
case 116:
//#line 1292 "gram.y"
{
            String lexemaPos = val_peek(0).sval;
            String claveNeg = TablaSimbolos.convertirANegativo(lexemaPos);
            TablaSimbolos.agregarAtributo(claveNeg, TablaSimbolos.TIPO, "SINGLEF");
            ParserVal pv = new ParserVal("-" + lexemaPos);
            pv.obj = "SINGLEF";
            yyval = pv;
        }
break;
case 117:
//#line 1305 "gram.y"
{
            if (val_peek(1) != null && val_peek(1).sval != null) {
                String tipoExp = (String) val_peek(1).obj;
                if ("SINGLEF".equals(tipoExp)) {
                    if (esConstante(val_peek(1))) {
                        double d = Double.parseDouble(val_peek(1).sval);
                        int entero = (int) d;
                        if (entero < AnalizadorLexico.ValorMinimoInt || entero > AnalizadorLexico.ValorMaximoInt) {
                            agregarErrorSemantico("Resultado de conversion 'tos' fuera de rango SHORTINT [-128, 127]: " + entero);
                        }
                        ParserVal pv = new ParserVal(String.valueOf(entero));
                        pv.obj = "SHORTINT";
                        yyval = pv;
                    } else {
                        int idTerceto = GeneradorTerceto.crearTerceto("tos", val_peek(1).sval, "-", "SHORTINT");
                        ParserVal pv = new ParserVal("[" + idTerceto + "]");
                        pv.obj = "SHORTINT";
                        yyval = pv;
                    }
                } else if (tipoExp != null) {
                    agregarErrorSemantico("La funcion de conversion 'tos' requiere una expresion de tipo SINGLEF (se recibio: " + tipoExp + ")");
                    yyval = new ParserVal();
                } else {
                    yyval = new ParserVal();
                }
            } else {
                yyval = new ParserVal();
            }
        }
break;
case 118:
//#line 1339 "gram.y"
{
            if (val_peek(1) != null && val_peek(1).sval != null) {
                int idBF = GeneradorTerceto.crearTerceto("BF", val_peek(1).sval, "?");
                pilaSaltos.push(idBF);
            }
        }
break;
case 120:
//#line 1350 "gram.y"
{ yyerror("Falta '(' de apertura en condicion de seleccion"); }
break;
case 121:
//#line 1352 "gram.y"
{ yyerror("Condicion de seleccion malformada o error en parentesis"); }
break;
case 122:
//#line 1357 "gram.y"
{
            if (!pilaSaltos.isEmpty()) {
                int idBF = pilaSaltos.pop();
                int idBI = GeneradorTerceto.crearTerceto("BI", "?", "-");
                GeneradorTerceto.completarSalto(idBF, GeneradorTerceto.getProximoIndice());
                pilaSaltos.push(idBI);
            }
        }
break;
case 123:
//#line 1369 "gram.y"
{
            if (!pilaSaltos.isEmpty()) {
                int idSalto = pilaSaltos.pop();
                GeneradorTerceto.completarSalto(idSalto, GeneradorTerceto.getProximoIndice());
            }
        }
break;
case 124:
//#line 1376 "gram.y"
{
            if (!pilaSaltos.isEmpty()) {
                int idBI = pilaSaltos.pop();
                GeneradorTerceto.completarSalto(idBI, GeneradorTerceto.getProximoIndice());
            }
        }
break;
case 125:
//#line 1383 "gram.y"
{
            if (!pilaSaltos.isEmpty()) {
                int idSalto = pilaSaltos.pop();
                GeneradorTerceto.completarSalto(idSalto, GeneradorTerceto.getProximoIndice());
            }
            yyerror("Falta palabra clave END_IF al final de la sentencia IF");
        }
break;
case 126:
//#line 1391 "gram.y"
{
            if (!pilaSaltos.isEmpty()) {
                int idBI = pilaSaltos.pop();
                GeneradorTerceto.completarSalto(idBI, GeneradorTerceto.getProximoIndice());
            }
            yyerror("Falta palabra clave END_IF al final de la sentencia IF");
        }
break;
case 127:
//#line 1402 "gram.y"
{
            if (val_peek(2) != null && val_peek(2).sval != null && val_peek(0) != null && val_peek(0).sval != null) {
                String tipo1 = (String) val_peek(2).obj;
                String tipo2 = (String) val_peek(0).obj;
                if (tipo1 != null && tipo2 != null) {
                    if (tipo1.equals(tipo2)) {
                        int idTerceto = GeneradorTerceto.crearTerceto(val_peek(1).sval, val_peek(2).sval, val_peek(0).sval);
                        yyval = new ParserVal("[" + idTerceto + "]");
                    } else {
                        agregarErrorSemantico("Incompatibilidad de tipos en comparacion: no se puede comparar " + tipo1 + " con " + tipo2 + " (requiere conversion explicita 'tos')");
                        yyval = new ParserVal();
                    }
                } else {
                    yyval = new ParserVal();
                }
            } else {
                yyval = new ParserVal();
            }
        }
break;
case 128:
//#line 1424 "gram.y"
{ yyval = new ParserVal(">="); }
break;
case 129:
//#line 1425 "gram.y"
{ yyval = new ParserVal("<="); }
break;
case 130:
//#line 1426 "gram.y"
{ yyval = new ParserVal("=="); }
break;
case 131:
//#line 1427 "gram.y"
{ yyval = new ParserVal("!="); }
break;
case 132:
//#line 1428 "gram.y"
{ yyval = new ParserVal(">"); }
break;
case 133:
//#line 1429 "gram.y"
{ yyval = new ParserVal("<"); }
break;
case 136:
//#line 1436 "gram.y"
{ yyerror("Falta el delimitador END en el bloque de sentencias"); }
break;
case 137:
//#line 1442 "gram.y"
{
            pilaSaltos.push(GeneradorTerceto.getProximoIndice());
        }
break;
case 138:
//#line 1449 "gram.y"
{
            if (!pilaSaltos.isEmpty()) {
                int dirInicio = pilaSaltos.pop();
                if (val_peek(1) != null && val_peek(1).sval != null) {
                    GeneradorTerceto.crearTerceto("BF", val_peek(1).sval, "[" + dirInicio + "]");
                }
            }
        }
break;
case 139:
//#line 1458 "gram.y"
{
            if (!pilaSaltos.isEmpty()) pilaSaltos.pop();
            yyerror("Falta el cuerpo de la iteracion REPEAT");
        }
break;
case 140:
//#line 1463 "gram.y"
{
            if (!pilaSaltos.isEmpty()) pilaSaltos.pop();
            yyerror("Falta palabra clave UNTIL en la iteracion REPEAT");
        }
break;
case 141:
//#line 1468 "gram.y"
{
            if (!pilaSaltos.isEmpty()) pilaSaltos.pop();
            yyerror("Falta palabra clave UNTIL y cuerpo en la iteracion REPEAT");
        }
break;
case 142:
//#line 1473 "gram.y"
{
            if (!pilaSaltos.isEmpty()) pilaSaltos.pop();
            yyerror("Falta palabra clave UNTIL y ')' en la iteracion REPEAT");
        }
break;
case 143:
//#line 1478 "gram.y"
{
            if (!pilaSaltos.isEmpty()) pilaSaltos.pop();
            yyerror("Falta palabra clave UNTIL, cuerpo y ')' en la iteracion REPEAT");
        }
break;
case 144:
//#line 1486 "gram.y"
{
            yyval = val_peek(1);
        }
break;
case 145:
//#line 1490 "gram.y"
{
            yyval = val_peek(1);
            yyerror("Falta '(' de apertura en condicion de iteracion");
        }
break;
case 146:
//#line 1495 "gram.y"
{
            yyval = val_peek(0);
            yyerror("Falta ')' de cierre en condicion de iteracion");
        }
break;
case 147:
//#line 1500 "gram.y"
{
            yyval = val_peek(0);
            yyerror("Faltan parentesis en condicion de iteracion");
        }
break;
case 148:
//#line 1509 "gram.y"
{
            GeneradorTerceto.crearTerceto("POUT", val_peek(1).sval, "-");
        }
break;
case 149:
//#line 1513 "gram.y"
{
            if (val_peek(1) != null && val_peek(1).sval != null) {
                GeneradorTerceto.crearTerceto("POUT", val_peek(1).sval, "-", (String) val_peek(1).obj);
            }
        }
break;
case 150:
//#line 1519 "gram.y"
{ yyerror("Falta argumento en sentencia pout"); }
break;
case 151:
//#line 1524 "gram.y"
{
            String funcionActual = obtenerFuncionActualMangled();
            if (funcionActual == null) {
                agregarErrorSemantico("Sentencia RET fuera del cuerpo de una funcion");
            } else {
                String claveFunc = funcionActual;
                String tipoDeclarado = TablaSimbolos.obtenerTipo(claveFunc);
                String tipoExp = (val_peek(1) != null) ? (String) val_peek(1).obj : null;
                TablaSimbolos.agregarAtributo(claveFunc, "TIENE_RET", Boolean.TRUE);

                if (cant_retornos_auto >= 0) {
                    cant_retornos_auto++;
                    if ("auto".equals(tipoDeclarado)) {
                        TablaSimbolos.agregarAtributo(claveFunc, TablaSimbolos.TIPO, tipoExp);
                    } else if (tipoExp != null && !tipoDeclarado.equals(tipoExp)) {
                        TablaSimbolos.agregarAtributo(claveFunc, TablaSimbolos.TIPO, "SINGLEF");
                    }
                } else {
                    if (tipoDeclarado != null && tipoExp != null && !tipoDeclarado.equals(tipoExp)) {
                        agregarErrorSemantico("Tipo de retorno incompatible en funcion '" + 
                            TablaSimbolos.obtenerNombre(claveFunc) + 
                            "': se esperaba " + tipoDeclarado + " pero se retorno " + tipoExp);
                    }
                }

                if (val_peek(1) != null && val_peek(1).sval != null) {
                    GeneradorTerceto.crearTerceto("RET", val_peek(1).sval, "-", tipoExp);
                }
            }
        }
break;
//#line 2555 "Parser.java"
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
