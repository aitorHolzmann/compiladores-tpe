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
   28,   36,   36,   36,   36,   36,   36,   36,   36,   36,
   37,   37,   37,   37,   37,   37,   37,   37,   37,   40,
   40,   39,   39,   41,   41,   42,   38,   38,   34,   35,
   43,   29,   29,   29,   46,   46,   46,   46,   44,   47,
   47,   47,   47,   47,   47,   45,   45,   45,   30,   30,
   30,   30,   30,   30,   48,   48,   48,   48,   31,   31,
   31,   32,
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
    1,    1,    3,    4,    1,    1,    1,    1,    1,    5,
    6,    1,    3,    5,    5,    2,    1,    1,    1,    1,
    4,    6,    5,    4,    2,    4,    1,    3,    3,    1,
    1,    1,    1,    1,    1,    3,    1,    2,    5,    4,
    6,    5,    5,    4,    3,    2,    2,    1,    4,    4,
    3,    4,
};
final static short yydefred[] = {                         0,
    2,    7,    0,    7,    0,    0,    4,    0,   62,    0,
   21,   22,    0,    0,    8,    9,   10,   11,   12,   13,
    0,    0,   62,   62,   19,    0,    0,    0,    0,    0,
    0,    0,   16,    0,    0,   62,    0,    0,   56,    0,
    0,    0,    0,    1,    0,    0,  109,  110,    0,    0,
    0,    0,    0,    0,   63,    0,    0,   66,   67,    0,
    0,    0,  107,  108,    0,   82,   92,   95,   96,   97,
   98,    0,    0,   28,    0,    0,   60,   15,   24,    0,
   18,   14,    0,    0,    3,    5,   20,   70,    0,    0,
    0,    0,    0,    0,    0,    0,    0,   99,    0,    0,
    0,   62,    0,    0,  127,    0,    0,    0,  106,   87,
   88,   64,    0,    0,   65,   68,   69,    0,   89,    0,
    0,   90,    0,    0,    0,    0,    0,   58,    0,    0,
   17,    0,    0,    0,   93,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,  120,  121,  122,  123,  124,
  125,    0,    0,    0,  141,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,   79,    0,   80,    0,    0,
   85,   83,   86,   84,    0,    0,   46,    0,    0,    0,
    0,   33,    0,    0,   27,    0,    0,   94,   74,   73,
   75,    0,    0,    0,    0,  117,  114,    0,    0,    0,
  139,  140,  142,  126,    0,  136,  130,  134,    0,    0,
    0,  111,   45,    0,    0,    0,   37,   36,   35,    0,
   31,    0,    7,  105,    0,  100,    0,  104,    0,  115,
    0,  113,  135,  132,  129,  133,    0,   44,    0,    0,
    0,   47,   48,   49,    7,   34,    7,    0,  101,    0,
  118,  112,  131,   38,    0,    0,    0,    0,    0,   62,
  116,    0,    0,    0,    0,   62,   62,    0,    0,    0,
    0,    0,   50,    0,    0,    0,    0,    7,    0,    0,
    0,   23,   62,    0,   29,   25,    0,   62,    0,    0,
    0,    0,   52,    0,   51,
};
final static short yydgoto[] = {                          3,
    5,   27,    4,   24,   15,   16,   17,   18,   19,   20,
  180,   35,   26,  181,  129,   22,  126,  182,   73,  177,
  215,  242,  243,  244,  265,   55,   56,   57,   58,   59,
   60,   61,   98,   63,   64,   65,   66,   67,  137,   68,
   69,   70,   71,  160,  106,  197,  152,  161,
};
final static short yysindex[] = {                      -143,
    0,    0,    0,    0, -110,  591,    0, -232,    0, -215,
    0,    0, -222, -140,    0,    0,    0,    0,    0,    0,
  -63, -189,    0,    0,    0,   78,   83, -206,  -86, -232,
  113,   43,    0,  -18,  -33,    0,  107,  131,    0, -179,
   68,  -34,   -7,    0,   95,  108,    0,    0,   26,  119,
  195,    8,  218,  218,    0,   94,   -6,    0,    0,  116,
  133, -101,    0,    0,   48,    0,    0,    0,    0,    0,
    0, -226, -173,    0,  159,  121,    0,    0,    0,  164,
    0,    0,  -49,  155,    0,    0,    0,    0,    3,   58,
  -67,  -45,  188,  178,  -32,  189,  538,    0,  177,   51,
  189,    0,   44,  189,    0,  -21,  189,   48,    0,    0,
    0,    0,  267,  411,    0,    0,    0,  189,    0,  216,
  259,    0, -232,  211,  -10,  232, -112,    0,  245, -112,
    0,  208,  189,   19,    0,   19,   41,  193,  207,  213,
  273,  189,  -46,  286,  287,    0,    0,    0,    0,    0,
    0,  189,  178,  291,    0,  166,  174,  249,  189,  292,
  268,    6,   44,  189,  252,    0,   48,    0,   48,   19,
    0,    0,    0,    0,  211,  -31,    0, -112,   85,   62,
   80,    0, -112,  143,    0,  317,  189,    0,    0,    0,
    0,   65,  333,  178,  285,    0,    0,  178,   19,  -46,
    0,    0,    0,    0,  320,    0,    0,    0,  307,  308,
   17,    0,    0,  123, -133,  186,    0,    0,    0, -112,
    0,  227,    0,    0,   19,    0,  266,    0,  -44,    0,
  -46,    0,    0,    0,    0,    0,  327,    0,  331,  134,
  141,    0,    0,    0,    0,    0,    0,  611,    0,  349,
    0,    0,    0,    0,  360,  370,  -20,  620,  639,    0,
    0, -112, -112, -234,  355,    0,    0,  306,  313,  340,
 -232,  211,    0,  330,  354,  356,  161,    0,  211,  371,
  373,    0,    0,  658,    0,    0,  378,    0,  142,  402,
  374,  142,    0,  379,    0,
};
final static short yyrindex[] = {                         0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,  416,  437,  182,    0,    0,
  497,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,  450,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,  500,    0,    0,  512,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,  532,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,  440,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,  657,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,  187,  -77,    0,  341,    0,    0,  341,
    0,  560,    0,  396,    0,  361,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,   75,    0,  398,
    0,    0,    0,    0,    0,    0,  683,    0,  688,  400,
    0,    0,    0,    0,  199,    0,    0,  341,    0,    0,
    0,    0,  341,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,   22,    0,
    0,    0,    0,    0,  405,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,  365,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,  562,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,  -37,  406,    0,    0,    0,
    0,  341,  341,    0,    0,    0,    0,    0,    0,    0,
    0,  407,    0,    0,    0,  588,    0,    0,  410,    0,
    0,    0,    0,    0,    0,    0,    0,    0,  406,    0,
    0,  406,    0,    0,    0,
};
final static short yygindex[] = {                         0,
   -1,  -19,    0,    0,    0,    0,    0,    0,    0,    0,
   -5,    0,  -12,  -74,    0,    0,    0,  250,    0,    0,
    0,    0,    0,    0,  137,  -15,    0,  695,    0,    0,
    0,    0,  -17,  380,  385,   29,   -2,  -11,  294,    0,
    0,    0,    0,   27,  -78, -170,    0,  314,
};
final static int YYTABLESIZE=959;
static short yytable[];
static { yytable();}
static void yytable(){
yytable = new short[]{                         21,
   21,   31,    6,   37,   38,   90,   19,   90,   30,   62,
   83,   92,  196,   92,  251,  143,   84,   76,  164,   62,
   62,   19,   25,   40,   25,   82,   93,  213,   93,  232,
   25,   62,   96,  105,   53,   51,  113,   52,  114,   54,
  109,   28,  133,   29,   53,   51,  209,   52,  271,   54,
  110,  111,  115,  122,  123,  184,   91,  237,   91,  124,
  252,  113,  119,  114,  208,  104,   62,   53,   51,   99,
   52,   36,   54,   72,  200,  236,   62,   87,  105,  108,
  119,  188,  158,  159,  187,   53,   51,  125,   52,  120,
   54,  155,   53,   51,  121,   52,  122,   54,  135,   53,
   51,   78,   52,  216,   54,  226,   53,   51,  222,   52,
  175,   54,    1,    2,  128,  229,   25,  172,  174,  231,
  221,   40,  145,  220,   53,   51,   88,   52,  239,   54,
  162,   11,   12,  128,  100,   62,   39,  105,   11,   12,
   62,  167,  169,  179,  240,    7,    8,  101,   53,   51,
    9,   52,  112,   54,   10,  122,   40,  122,  107,   11,
   12,   11,   12,  214,   40,  118,   40,   13,   14,   74,
   75,   77,   53,   51,  116,   52,   62,   54,  105,  128,
   62,  238,  105,  223,   43,  205,  220,  269,  270,  138,
  211,  117,   32,   33,   43,   43,   53,   51,  127,   52,
   43,   54,   34,  130,   47,   48,  202,  131,  113,  241,
  114,  141,  194,  195,  203,  250,  113,  153,  114,   53,
   51,  248,   52,   81,   54,   25,  245,  142,  257,  220,
   53,   51,   89,   52,  144,   54,   53,   79,   80,   52,
  268,   54,   21,  258,   19,  259,  274,  275,   94,   95,
   62,  272,   21,   21,   40,  163,   62,   62,  279,   95,
   52,  264,   52,  287,   47,   48,  185,  247,  290,   62,
  220,  178,   62,  176,   47,   48,  284,   50,   21,   47,
   48,   41,   42,   43,  183,  189,  102,   50,   45,   46,
   53,   51,  212,   52,  113,   54,  114,   47,   48,  190,
   95,   49,  103,   52,  119,  191,  249,   95,   53,  187,
   50,   52,  192,   54,   95,   47,   48,  218,  219,   47,
   48,   95,   47,   48,  154,  133,  207,  198,   50,   47,
   48,  201,  206,  128,  128,   50,   47,   48,   41,   42,
   43,  217,   50,  230,   44,   45,   46,   53,   51,   50,
   52,  128,   54,  277,   47,   48,  220,  224,   49,  113,
  233,  114,   41,   42,   43,  234,  235,   50,   85,   45,
   46,   53,   51,  228,   52,  113,   54,  114,   47,   48,
  278,   32,   49,  220,   32,  253,   41,   42,   43,  254,
  255,   50,   86,   45,   46,   53,   51,  256,   52,  262,
   54,  102,   47,   48,  102,  103,   49,  261,  103,  263,
   41,   42,   43,  273,  282,   50,  132,   45,   46,   53,
   51,  283,   52,  264,   54,  291,   47,   48,  294,  285,
   49,  286,  293,   41,   42,   43,    6,  295,  102,   50,
   45,   46,   40,   53,   51,   95,   52,   42,   54,   47,
   48,   95,   53,   49,   71,   52,  138,   54,   72,   41,
   47,   48,   50,  137,   53,   55,   47,   48,   54,  246,
  139,  171,   95,   50,   95,  140,  210,    0,    0,   50,
   91,   91,   91,   91,   91,  227,   91,   47,   48,   47,
   48,   91,   91,    0,   91,    0,   91,    0,   91,   91,
   50,   91,   50,    0,   41,   42,   43,    0,   91,    0,
  204,   45,   46,    0,  173,   95,    0,    0,    0,    0,
   47,   48,  166,   95,   49,    0,    0,    0,    0,    0,
   47,   48,    0,   50,    0,    0,    0,    0,   47,   48,
    0,   99,   99,   50,   99,    0,   99,    0,    0,    0,
    0,   50,   76,    0,   76,   76,   76,    0,   99,    0,
    0,   41,   42,   43,    0,    0,    0,  276,   45,   46,
   76,   76,    0,   76,    0,    0,    0,   47,   48,    0,
  113,   49,  114,    0,    0,   41,   42,   43,    0,    0,
   50,  280,   45,   46,    0,    0,    0,  151,    0,  150,
    0,   47,   48,    0,    0,   49,    0,    0,    0,   41,
   42,   43,    0,    0,   50,  281,   45,   46,    0,    0,
    0,    0,    0,    0,    0,   47,   48,    0,    0,   49,
    0,    0,    0,   41,   42,   43,    0,    0,   50,  289,
   45,   46,    0,    0,    0,    0,    0,    0,    0,   47,
   48,    0,    0,   49,    0,    0,    0,   41,   42,   43,
    0,    0,   50,  292,   45,   46,  168,   95,    0,    0,
    0,   57,   57,   47,   48,    0,   57,   49,    0,    0,
   57,    0,   47,   48,    0,    0,   50,   57,   57,    0,
    0,    0,    0,   57,   57,   50,   91,   81,    0,   81,
   81,   81,    0,    0,    0,    0,   91,   91,   91,   91,
   91,   91,   91,    0,    0,   81,   81,    0,   81,    0,
    0,   91,   91,   77,    0,   77,   77,   77,   78,    0,
   78,   78,   78,    0,    0,    0,    0,   97,    0,    0,
    0,   77,   77,    0,   77,    0,   78,   78,    0,   78,
    0,    0,   61,   61,    0,    0,   99,   61,    0,    0,
    0,   61,    0,    0,    0,    0,    0,    0,   61,   61,
    0,   99,   99,    0,   61,   61,    0,    0,    0,   76,
   76,   76,   76,  134,  136,    0,    0,   59,   59,    0,
   97,    0,   59,    0,  156,  157,   59,   97,   97,    0,
    0,  165,    0,   59,   59,  146,  147,  148,  149,   59,
   59,    0,  170,    0,    0,   30,   30,   39,   39,    0,
   30,    0,   39,    0,   30,    0,   39,  186,    0,    0,
    0,   30,   30,   39,   39,    0,  193,   30,   30,   39,
   39,    0,    0,   26,   26,    0,  199,    8,   26,    0,
    0,   23,   26,   97,    0,   10,    0,   97,   97,   26,
   26,    0,   11,   12,    0,   26,   26,    8,   13,   14,
    0,  260,    0,    0,    0,   10,    8,    0,    0,    0,
  266,  225,   11,   12,   10,    0,  136,    0,   13,   14,
    0,   11,   12,    0,    0,    8,    0,   13,   14,  267,
    0,    0,    0,   10,    0,    0,    0,    0,    0,    0,
   11,   12,    0,    0,    8,    0,   13,   14,  288,    0,
    0,    0,   10,    0,   81,   81,   81,   81,    0,   11,
   12,    0,    0,    0,    0,   13,   14,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
   77,   77,   77,   77,    0,   78,   78,   78,   78,
};
}
static short yycheck[];
static { yycheck(); }
static void yycheck() {
yycheck = new short[] {                          5,
    6,   14,    4,   23,   24,   40,   44,   40,   14,   27,
   44,   46,   59,   46,   59,   94,   36,   30,   40,   37,
   38,   59,  257,   44,  257,   59,   61,   59,   61,  200,
  257,   49,   40,   49,   42,   43,   43,   45,   45,   47,
   52,  257,   40,  266,   42,   43,   41,   45,  283,   47,
   53,   54,   59,   65,  281,  130,   91,   41,   91,   72,
  231,   43,   41,   45,   59,   40,   84,   42,   43,   43,
   45,  261,   47,  280,  153,   59,   94,  257,   94,   51,
   59,   41,  102,   40,   44,   42,   43,  261,   45,   42,
   47,   41,   42,   43,   47,   45,  108,   47,   41,   42,
   43,   59,   45,  178,   47,   41,   42,   43,  183,   45,
  123,   47,  256,  257,   40,  194,  257,  120,  121,  198,
   41,   44,   96,   44,   42,   43,   59,   45,  262,   47,
  104,  272,  273,   59,   40,  153,   59,  153,  272,  273,
  158,  113,  114,  256,  278,  256,  257,   40,   42,   43,
  261,   45,   59,   47,  265,  167,   44,  169,   40,  272,
  273,  272,  273,  176,   44,  267,   44,  278,  279,  256,
  257,   59,   42,   43,   59,   45,  194,   47,  194,   59,
  198,   59,  198,   41,  262,  159,   44,  262,  263,  257,
  164,   59,  256,  257,  272,  273,   42,   43,   40,   45,
  278,   47,  266,   40,  272,  273,   41,  257,   43,  215,
   45,  257,  259,  260,   41,  260,   43,   41,   45,   42,
   43,  223,   45,  257,   47,  257,   41,   40,  241,   44,
   42,   43,  267,   45,  267,   47,   42,  256,  257,   45,
  260,   47,  248,  245,  282,  247,  266,  267,  256,  257,
  268,  264,  258,  259,   44,  277,  274,  275,  271,  257,
   45,  282,   45,  283,  272,  273,   59,   41,  288,  287,
   44,   40,  290,  284,  272,  273,  278,  285,  284,  272,
  273,  256,  257,  258,   40,   93,  261,  285,  263,  264,
   42,   43,   41,   45,   43,   47,   45,  272,  273,   93,
  257,  276,  277,   45,  257,   93,   41,  257,   42,   44,
  285,   45,   40,   47,  257,  272,  273,  256,  257,  272,
  273,  257,  272,  273,  274,   40,   59,   41,  285,  272,
  273,   41,   41,  259,  260,  285,  272,  273,  256,  257,
  258,  257,  285,   59,  262,  263,  264,   42,   43,  285,
   45,  277,   47,   41,  272,  273,   44,   41,  276,   43,
   41,   45,  256,  257,  258,   59,   59,  285,  262,  263,
  264,   42,   43,   41,   45,   43,   47,   45,  272,  273,
   41,   41,  276,   44,   44,   59,  256,  257,  258,   59,
  257,  285,  262,  263,  264,   42,   43,  257,   45,   40,
   47,   41,  272,  273,   44,   41,  276,   59,   44,   40,
  256,  257,  258,   59,   59,  285,  262,  263,  264,   42,
   43,  261,   45,  282,   47,  289,  272,  273,  292,   59,
  276,   59,   59,  256,  257,  258,    0,   59,  261,  285,
  263,  264,  261,   42,   43,  257,   45,  261,   47,  272,
  273,  257,   42,  276,   59,   45,   59,   47,   59,  261,
  272,  273,  285,   59,   59,   59,  272,  273,   59,  220,
   91,  256,  257,  285,  257,   91,  163,   -1,   -1,  285,
   41,   42,   43,   44,   45,  192,   47,  272,  273,  272,
  273,   42,   43,   -1,   45,   -1,   47,   -1,   59,   60,
  285,   62,  285,   -1,  256,  257,  258,   -1,   59,   -1,
  262,  263,  264,   -1,  256,  257,   -1,   -1,   -1,   -1,
  272,  273,  256,  257,  276,   -1,   -1,   -1,   -1,   -1,
  272,  273,   -1,  285,   -1,   -1,   -1,   -1,  272,  273,
   -1,   42,   43,  285,   45,   -1,   47,   -1,   -1,   -1,
   -1,  285,   41,   -1,   43,   44,   45,   -1,   59,   -1,
   -1,  256,  257,  258,   -1,   -1,   -1,  262,  263,  264,
   59,   60,   -1,   62,   -1,   -1,   -1,  272,  273,   -1,
   43,  276,   45,   -1,   -1,  256,  257,  258,   -1,   -1,
  285,  262,  263,  264,   -1,   -1,   -1,   60,   -1,   62,
   -1,  272,  273,   -1,   -1,  276,   -1,   -1,   -1,  256,
  257,  258,   -1,   -1,  285,  262,  263,  264,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,  272,  273,   -1,   -1,  276,
   -1,   -1,   -1,  256,  257,  258,   -1,   -1,  285,  262,
  263,  264,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  272,
  273,   -1,   -1,  276,   -1,   -1,   -1,  256,  257,  258,
   -1,   -1,  285,  262,  263,  264,  256,  257,   -1,   -1,
   -1,  256,  257,  272,  273,   -1,  261,  276,   -1,   -1,
  265,   -1,  272,  273,   -1,   -1,  285,  272,  273,   -1,
   -1,   -1,   -1,  278,  279,  285,  257,   41,   -1,   43,
   44,   45,   -1,   -1,   -1,   -1,  257,  268,  269,  270,
  271,  272,  273,   -1,   -1,   59,   60,   -1,   62,   -1,
   -1,  272,  273,   41,   -1,   43,   44,   45,   41,   -1,
   43,   44,   45,   -1,   -1,   -1,   -1,   43,   -1,   -1,
   -1,   59,   60,   -1,   62,   -1,   59,   60,   -1,   62,
   -1,   -1,  256,  257,   -1,   -1,  257,  261,   -1,   -1,
   -1,  265,   -1,   -1,   -1,   -1,   -1,   -1,  272,  273,
   -1,  272,  273,   -1,  278,  279,   -1,   -1,   -1,  268,
  269,  270,  271,   89,   90,   -1,   -1,  256,  257,   -1,
   96,   -1,  261,   -1,  100,  101,  265,  103,  104,   -1,
   -1,  107,   -1,  272,  273,  268,  269,  270,  271,  278,
  279,   -1,  118,   -1,   -1,  256,  257,  256,  257,   -1,
  261,   -1,  261,   -1,  265,   -1,  265,  133,   -1,   -1,
   -1,  272,  273,  272,  273,   -1,  142,  278,  279,  278,
  279,   -1,   -1,  256,  257,   -1,  152,  257,  261,   -1,
   -1,  261,  265,  159,   -1,  265,   -1,  163,  164,  272,
  273,   -1,  272,  273,   -1,  278,  279,  257,  278,  279,
   -1,  261,   -1,   -1,   -1,  265,  257,   -1,   -1,   -1,
  261,  187,  272,  273,  265,   -1,  192,   -1,  278,  279,
   -1,  272,  273,   -1,   -1,  257,   -1,  278,  279,  261,
   -1,   -1,   -1,  265,   -1,   -1,   -1,   -1,   -1,   -1,
  272,  273,   -1,   -1,  257,   -1,  278,  279,  261,   -1,
   -1,   -1,  265,   -1,  268,  269,  270,  271,   -1,  272,
  273,   -1,   -1,   -1,   -1,  278,  279,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
  268,  269,  270,  271,   -1,  268,  269,  270,  271,
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
"IGUALIGUAL","DISTINTO","SHORTINT","SINGLEF","CADENA",null,"REPEAT","UNTIL",
"AUTO","COMPTIME","IMPORT","FROM","EXPORT","TO","EXTENDS","TOS",
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
"acceso_posicional : IDENTIFICADOR '[' constante_entera ']'",
"acceso_posicional : IDENTIFICADOR '[' IDENTIFICADOR ']'",
"acceso_posicional : IDENTIFICADOR '[' constante_flotante ']'",
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
"termino : termino constante",
"factor : IDENTIFICADOR",
"factor : constante",
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
"numero_negativo : '-' constante",
"constante : constante_entera",
"constante : constante_flotante",
"constante_entera : SHORTINT",
"constante_flotante : SINGLEF",
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

//#line 441 "gram.y"


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
//#line 688 "Parser.java"
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
//#line 48 "gram.y"
{ 
            if (cant_errores == 0) {
                System.out.println("Programa reconocido correctamente"); 
            } else {
                System.out.println("Compilacion finalizada con " + cant_errores + " error(es) sintactico(s)");
            }
        }
break;
case 2:
//#line 55 "gram.y"
{ yyerror("Falta el nombre del programa al inicio"); }
break;
case 3:
//#line 56 "gram.y"
{ 
            System.out.println("Compilacion finalizada con " + cant_errores + " error(es) sintactico(s)");
        }
break;
case 4:
//#line 59 "gram.y"
{ yyerror("Falta el delimitador BEGIN de sentencias ejecutables"); }
break;
case 5:
//#line 60 "gram.y"
{
            System.out.println("Compilacion finalizada con " + cant_errores + " error(es) sintactico(s)");
        }
break;
case 6:
//#line 64 "gram.y"
{
            yyerror("Falta el delimitador END al final del programa");
            System.out.println("Compilacion finalizada con " + cant_errores + " error(es) sintactico(s)");
        }
break;
case 9:
//#line 78 "gram.y"
{ imprimirRegla("declaracion_variables"); }
break;
case 10:
//#line 79 "gram.y"
{ imprimirRegla("declaracion_funciones"); }
break;
case 11:
//#line 80 "gram.y"
{ imprimirRegla("declaracion_clase"); }
break;
case 12:
//#line 81 "gram.y"
{ imprimirRegla("declaracion_objeto"); }
break;
case 13:
//#line 82 "gram.y"
{ imprimirRegla("declaracion_comptime"); }
break;
case 15:
//#line 89 "gram.y"
{ yyerror("Falta ';' al final de la declaracion de variables"); }
break;
case 18:
//#line 95 "gram.y"
{ yyerror("Falta ',' entre los identificadores"); }
break;
case 24:
//#line 115 "gram.y"
{ yyerror("Falta nombre de funcion"); }
break;
case 26:
//#line 121 "gram.y"
{ yyerror("Falta ';' al final de la declaracion de funcion"); }
break;
case 27:
//#line 123 "gram.y"
{
            if (cant_retornos_auto == 0) {
                yyerror("Ausencia de sentencia de retorno 'RET' en funcion AUTO");
            }
            cant_retornos_auto = -1;
        }
break;
case 28:
//#line 129 "gram.y"
{ yyerror("Falta nombre de funcion"); }
break;
case 30:
//#line 133 "gram.y"
{
            if (cant_retornos_auto == 0) {
                yyerror("Ausencia de sentencia de retorno 'RET' en funcion AUTO");
            }
            cant_retornos_auto = -1;
            yyerror("Falta ';' al final de la declaracion de funcion");
        }
break;
case 31:
//#line 144 "gram.y"
{ cant_retornos_auto = 0; }
break;
case 35:
//#line 154 "gram.y"
{ imprimirRegla("parametro_formal"); }
break;
case 36:
//#line 155 "gram.y"
{ yyerror("Falta de nombre de parametro formal en declaracion de funcion"); }
break;
case 37:
//#line 156 "gram.y"
{ yyerror("Falta de tipo del parametro formal en declaracion de funcion"); }
break;
case 39:
//#line 167 "gram.y"
{ yyerror("Falta ';' al final de la declaracion de clase"); }
break;
case 41:
//#line 172 "gram.y"
{ imprimirRegla("importacion_opcional"); }
break;
case 42:
//#line 174 "gram.y"
{ yyerror("Falta la palabra clave 'FROM' en la declaracion IMPORT"); }
break;
case 44:
//#line 179 "gram.y"
{ imprimirRegla("herencia_opcional"); }
break;
case 45:
//#line 181 "gram.y"
{ yyerror("Falta nombre o lista de clases a heredar luego de 'EXTENDS'"); }
break;
case 54:
//#line 208 "gram.y"
{ imprimirRegla("exportacion_opcional"); }
break;
case 55:
//#line 210 "gram.y"
{ yyerror("Falta la palabra clave 'TO' en la declaracion EXPORT"); }
break;
case 56:
//#line 216 "gram.y"
{ imprimirRegla("declaracion_de_objeto"); }
break;
case 57:
//#line 218 "gram.y"
{ yyerror("Falta ';' al final de la declaracion de objeto"); }
break;
case 59:
//#line 226 "gram.y"
{ yyerror("Falta ';' al final de la declaracion comptime"); }
break;
case 60:
//#line 228 "gram.y"
{ yyerror("Falta el tipo de dato en la declaracion comptime"); }
break;
case 61:
//#line 230 "gram.y"
{
            yyerror("Falta el tipo de dato en la declaracion comptime");
            yyerror("Falta ';' al final de la declaracion comptime");
        }
break;
case 63:
//#line 240 "gram.y"
{ imprimirRegla("sentencia_ejecutable"); }
break;
case 70:
//#line 251 "gram.y"
{ yyerror("Sentencia ejecutable malformada o falta ';' previo"); }
break;
case 75:
//#line 263 "gram.y"
{ yyerror("El indice de un acceso posicional debe ser una constante shortint o un identificador"); }
break;
case 79:
//#line 271 "gram.y"
{ yyerror("Falta un operando en la expresion"); }
break;
case 80:
//#line 272 "gram.y"
{ yyerror("Falta un operando en la expresion"); }
break;
case 81:
//#line 273 "gram.y"
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
{ yyerror("Falta un operando en la expresion"); }
break;
case 89:
//#line 283 "gram.y"
{ yyerror("Falta operador en la expresion"); }
break;
case 90:
//#line 284 "gram.y"
{ yyerror("Falta operador en la expresion"); }
break;
case 92:
//#line 290 "gram.y"
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
case 100:
//#line 316 "gram.y"
{ imprimirRegla("invocacion_metodo"); }
break;
case 101:
//#line 317 "gram.y"
{ imprimirRegla("invocacion_metodo"); }
break;
case 105:
//#line 329 "gram.y"
{ yyerror("Uso del simbolo ':=' donde debe usarse '=' en asignacion dentro de expresion"); }
break;
case 106:
//#line 334 "gram.y"
{
            int id_pos = val_peek(0).ival;
            String lexema_pos = TablaSimbolos.obtenerAtributo(id_pos, TablaSimbolos.LEXEMA);
            int id_neg = TablaSimbolos.convertirANegativo(lexema_pos);
            yyval.ival = id_neg;
        }
break;
case 113:
//#line 365 "gram.y"
{ yyerror("Falta '(' de apertura en condicion de seleccion"); }
break;
case 114:
//#line 367 "gram.y"
{ yyerror("Condicion de seleccion malformada o error en parentesis"); }
break;
case 117:
//#line 374 "gram.y"
{ yyerror("Falta palabra clave END_IF al final de la sentencia IF"); }
break;
case 118:
//#line 376 "gram.y"
{ yyerror("Falta palabra clave END_IF al final de la sentencia IF"); }
break;
case 128:
//#line 396 "gram.y"
{ yyerror("Falta el delimitador END en el bloque de sentencias"); }
break;
case 130:
//#line 403 "gram.y"
{ yyerror("Falta el cuerpo de la iteracion REPEAT"); }
break;
case 131:
//#line 405 "gram.y"
{ yyerror("Falta palabra clave UNTIL en la iteracion REPEAT"); }
break;
case 132:
//#line 407 "gram.y"
{ yyerror("Falta palabra clave UNTIL y cuerpo en la iteracion REPEAT"); }
break;
case 133:
//#line 409 "gram.y"
{ yyerror("Falta palabra clave UNTIL y ')' en la iteracion REPEAT"); }
break;
case 134:
//#line 411 "gram.y"
{ yyerror("Falta palabra clave UNTIL, cuerpo y ')' en la iteracion REPEAT"); }
break;
case 136:
//#line 417 "gram.y"
{ yyerror("Falta '(' de apertura en condicion de iteracion"); }
break;
case 137:
//#line 419 "gram.y"
{ yyerror("Falta ')' de cierre en condicion de iteracion"); }
break;
case 138:
//#line 421 "gram.y"
{ yyerror("Faltan parentesis en condicion de iteracion"); }
break;
case 141:
//#line 429 "gram.y"
{ yyerror("Falta argumento en sentencia pout"); }
break;
case 142:
//#line 434 "gram.y"
{
            if (cant_retornos_auto >= 0) {
                cant_retornos_auto++;
            }
        }
break;
//#line 1152 "Parser.java"
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
