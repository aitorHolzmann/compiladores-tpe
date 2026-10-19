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
import java.util.ArrayList;
import java.util.Stack;
//#line 22 "Parser.java"




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
   22,    9,    9,   23,   23,   23,   24,   24,   24,   25,
   25,   26,   26,   27,   28,   28,   30,   31,   29,   29,
   29,   10,   10,   11,   11,   11,   11,    3,    3,   32,
   32,   32,   32,   32,   32,   32,   33,   33,   39,   39,
   39,   34,   34,   34,   34,   34,   34,   40,   40,   40,
   40,   40,   40,   40,   40,   40,   40,   41,   41,   41,
   41,   41,   41,   41,   41,   41,   41,   43,   43,   42,
   42,   44,   44,   45,   45,   46,   47,   35,   35,   35,
   51,   50,   50,   50,   50,   48,   52,   52,   52,   52,
   52,   52,   49,   49,   49,   53,   36,   36,   36,   36,
   36,   36,   54,   54,   54,   54,   37,   37,   37,   38,
};
final static short yylen[] = {                            2,
    1,    5,    0,    6,    0,    6,    4,    0,    2,    1,
    1,    1,    1,    1,    3,    3,    1,    3,    2,    1,
    3,    1,    1,    3,    3,    4,    9,    0,   12,    8,
    5,    0,   12,    4,    0,    1,    3,    2,    2,    2,
    2,    7,    6,    0,    3,    2,    0,    3,    2,    0,
    2,    1,    1,    4,   10,    9,    2,    2,    0,    3,
    2,    3,    2,    4,    3,    3,    2,    0,    2,    2,
    2,    1,    1,    2,    2,    2,    3,    3,    4,    4,
    4,    1,    3,    3,    3,    3,    2,    1,    3,    3,
    3,    3,    2,    2,    2,    2,    2,    1,    1,    1,
    3,    4,    1,    1,    1,    1,    1,    5,    6,    1,
    3,    5,    5,    2,    2,    4,    4,    3,    5,    4,
    1,    2,    4,    1,    3,    3,    1,    1,    1,    1,
    1,    1,    3,    1,    2,    1,    5,    4,    6,    5,
    5,    4,    3,    2,    2,    1,    4,    4,    3,    4,
};
final static short yydefred[] = {                         0,
    3,    1,    0,    8,    8,    0,    0,    5,    0,   68,
    0,   22,   23,    0,    0,    9,   10,   11,   12,   13,
   14,    0,    0,    0,    0,    0,   68,   68,   20,    0,
    0,   41,    0,    0,    0,    0,   17,    0,    0,    0,
    0,   68,    0,    0,    0,    0,   62,    0,    0,    0,
    0,    2,    0,    0,   99,  100,  136,    0,    0,    0,
    0,    0,   69,    0,    0,   72,   73,    0,    0,    0,
    0,   88,  103,  104,  105,  106,    0,    0,   32,   25,
    0,   66,   16,   28,   24,   19,   15,    0,    0,    0,
    0,   36,    0,    0,    0,    0,    0,    4,    6,   21,
   76,    0,    0,    0,    0,    0,    0,    0,    0,    0,
  107,    0,    0,    0,    0,    0,  114,  115,   93,   94,
   70,    0,    0,   71,   74,   75,    0,   95,   96,   97,
    0,    0,   68,  134,    0,    0,    0,    0,    0,   64,
    0,   18,   40,   39,   38,    0,    8,   26,    0,    0,
    0,   50,    0,    0,  101,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,  127,  128,  129,  130,  131,
  132,    0,    0,    0,  149,    0,    0,    0,   85,    0,
   86,    0,    0,   91,   89,   92,   90,    0,  121,    0,
  124,  118,    0,    0,    0,    0,    0,    0,    0,    0,
    0,   37,    0,   31,   49,    0,    0,    0,    0,  102,
   80,   79,   81,    0,    0,  120,  117,    0,    0,  147,
  148,  150,  116,  133,  122,    0,    0,  144,  138,  142,
    0,    0,    0,    0,    0,   68,   48,    0,    0,    0,
   51,   52,   53,    0,    0,  113,    0,  108,    0,  112,
  119,    0,  125,  143,  140,  137,  141,    0,    8,    8,
    0,   42,   58,    0,    0,    0,    0,  109,  123,  139,
    0,    0,    0,    0,    0,    0,    0,   68,   68,   27,
    0,    0,   54,    8,    0,    0,    0,    0,    0,   68,
    0,    0,   68,    0,   33,   29,    0,    0,    0,    0,
    0,   56,   55,
};
final static short yydgoto[] = {                          3,
    4,    6,   31,    5,   28,   16,   17,   18,   19,   20,
   21,   90,   39,   30,   23,   24,   25,   91,  141,  139,
   92,   26,   44,  152,  207,  241,  242,  243,  275,  244,
  245,   63,   64,   65,   66,   67,   68,   69,  111,   71,
   72,  157,   73,   74,   75,   76,   77,  195,  135,  192,
  193,  172,   78,  196,
};
final static short yysindex[] = {                      -107,
    0,    0,    0,    0,    0,  -92,  488,    0, -223,    0,
 -219,    0,    0, -212, -228,    0,    0,    0,    0,    0,
    0, -168,    3,    7, -202, -206,    0,    0,    0,   12,
  100,    0,  -72, -223,  104,   53,    0,  -52,  -31, -224,
 -224,    0, -240, -139,  115,  132,    0, -118,  117,   46,
   30,    0,  114,  119,    0,    0,    0,  159,  368,   18,
  282,  282,    0,  153,   74,    0,    0,  162,  165,  -25,
  -12,    0,    0,    0,    0,    0,  148,   21,    0,    0,
  112,    0,    0,    0,    0,    0,    0,  -28,  -21,   63,
   43,    0,  169,  164, -223,  214,  -16,    0,    0,    0,
    0,   15,   68, -234,    6,  200,  148,   92,  190,  486,
    0,  239,   52,  190,  190,  -12,    0,    0,    0,    0,
    0,  -39,  207,    0,    0,    0,  190,    0,    0,    0,
   95,  258,    0,    0,  -45,   36,  190,  -33,  248,    0,
  251,    0,    0,    0,    0, -224,    0,    0,  236,  214,
  -37,    0,  190,   84,    0,   84,  187,  218,  231,  235,
  294,  190,  -45,  295,  302,    0,    0,    0,    0,    0,
    0,  190,  148,  304,    0,  127,  155,  210,    0,  -12,
    0,  -12,   84,    0,    0,    0,    0,  180,    0,  301,
    0,    0,  148,  190,  320,  306,   55,   36,  190, -224,
 -224,    0,  579,    0,    0,  123,  -84,  269,  190,    0,
    0,    0,    0,   83,  445,    0,    0,   84,  -45,    0,
    0,    0,    0,    0,    0,  -44,  325,    0,    0,    0,
  308,  316,   77,  255,  292,    0,    0,  324,  135,  140,
    0,    0,    0,  344,  358,    0,   84,    0,  388,    0,
    0,  340,    0,    0,    0,    0,    0,  348,    0,    0,
  196,    0,    0,    0,  -32, -224, -224,    0,    0,    0,
  588,  607,  355, -241,  366,  404,  472,    0,    0,    0,
 -223,  214,    0,    0,  142,  247,  299,  214,  616,    0,
  371,  380,    0,  426,    0,    0,  442,  149,  149,  387,
  396,    0,    0,
};
final static short yyrindex[] = {                         0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,  201,    0,    0,    0,  372,
  441,    0,    0,    0,  400,    0,    0,    0,    0,  484,
  484,    0,    0,    0,    0,    0,    0,    0,    0,  453,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,  477,
  529,    0,    0,    0,    0,    0,    0,    0,    0,    0,
  402,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,  209,  -70,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,   61,    0,    0,
    0,    0,    0,    0,    0,  536,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,  436,  217,
    0,    0,    0,  398,    0,  494,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,  556,
    0,  561,  420,    0,    0,    0,    0,  334,    0,    0,
    0,    0,    0,    0,  421,    0,    0,    0,    0,  484,
  484,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,   93,    0,    0,
    0,    0,    0,    0,    0,    0,  432,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,  465,    0,    0,
    0,    0,    0,    0,    0,    0,  496,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,  -13,  438,  484,  484,    0,    0,    0,
    0,    0,  516,    0,    0,    0,    0,    0,    0,    0,
    0,  440,    0,    0,    0,    0,    0,  447,    0,    0,
    0,    0,    0,    0,    0,    0,    0,  438,  438,    0,
    0,    0,    0,
};
final static short yygindex[] = {                         0,
    0,   -3,  -17,    0,    0,    0,    0,    0,    0,    0,
    0,   -6,    0,  -10,    0,    0,    0,  382,    0,    0,
  315,    0,    0,    0,    0,    0,    0,    0,   51,    0,
    0,  -38,    0,  656,    0,    0,    0,    0,  -27,  -22,
  -41,  287,    0,    0,    0,    0,    0,  365,  -42, -137,
    0,    0,    0,  309,
};
final static int YYTABLESIZE=895;
static short yytable[];
static { yytable();}
static void yytable(){
yytable = new short[]{                         22,
   22,    7,   61,   70,   35,   60,  199,   62,   34,   45,
   46,   48,   88,  191,  253,   29,   29,   70,   70,  119,
  120,  205,  158,   81,   94,  216,   57,   87,   29,  131,
   20,   89,   96,   29,  132,  138,  116,   32,  134,  134,
   95,  281,   40,   12,   13,   20,   41,   12,   13,   70,
   70,  159,  160,   33,  153,   48,   61,   59,   42,   60,
  137,   62,   61,   59,  163,   60,   70,   62,  134,  109,
   47,   61,   59,   43,   60,  194,   62,   61,   59,   70,
   60,  251,   62,  147,  150,  103,  146,   36,   37,  185,
  187,  105,  175,   61,   59,  231,   60,   38,   62,  180,
  182,   98,   98,   98,   98,   98,  106,   98,  155,   61,
   59,   83,   60,  230,   62,  188,  122,  258,  123,   98,
   98,   97,   98,  248,   61,   59,  122,   60,  123,   62,
  219,  103,  124,  126,  134,  257,  104,  105,  100,   60,
  206,   61,   59,  203,   60,   70,   62,   48,    1,    2,
  226,  126,  106,  113,  134,   48,   61,   59,  114,   60,
   70,   62,   82,    8,    9,   70,   48,  221,   10,  122,
  140,  123,   11,   61,   59,  101,   60,  238,   62,   12,
   13,  237,  104,   79,   80,   14,   15,   12,   13,   61,
   59,   47,   60,  239,   62,  222,   22,  122,  115,  123,
  240,   47,   47,   84,   85,   61,   59,   47,   60,  148,
   62,  121,  146,  189,  190,  252,  179,  108,  261,   29,
  125,   61,   59,  126,   60,   86,   62,  210,  142,  265,
  209,   61,   59,   70,   60,  143,   62,   61,   59,  162,
   60,  127,   62,  198,  128,   58,   55,   56,   61,  274,
  223,   60,  122,   62,  123,  271,  272,   48,   70,   70,
  286,  287,  161,  282,   22,   22,   70,  151,   20,   70,
  288,  108,  294,  129,  130,  297,   49,   50,   51,  173,
  289,  133,   22,   53,   54,  107,  108,  200,   61,   59,
  201,   60,  108,   62,  204,  259,   57,  136,  146,   58,
   55,   56,   60,  117,  118,   58,   55,   56,  108,  246,
  211,  122,  102,  123,   58,   55,   56,   98,  144,  145,
   58,   55,   56,  212,  108,  174,   60,  213,   98,   98,
   98,   98,  260,  214,  153,  146,   58,   55,   56,  108,
   61,   59,  217,   60,  220,   62,   98,   98,  300,  301,
  184,  108,   58,   55,   56,   49,   50,   51,  164,  225,
  228,   52,   53,   54,  229,  254,  255,   58,   55,   56,
   49,   50,   51,  135,  256,   57,   98,   53,   54,   58,
   55,   56,  262,  266,   58,   55,   56,   49,   50,   51,
   57,  263,  135,   99,   53,   54,  264,  267,  269,   58,
   55,   56,  290,   49,   50,   51,  270,   57,  133,   61,
   53,   54,   60,  280,   62,  112,   58,   55,   56,   49,
   50,   51,   93,   57,  283,  149,   53,   54,  268,  295,
  274,  209,   58,   55,   56,   49,   50,   51,  296,   57,
    7,  224,   53,   54,  284,  302,  108,  146,   58,   55,
   56,   49,   50,   51,  303,   57,   77,  273,   53,   54,
  202,   44,  181,  108,   58,   55,   56,   61,   59,   46,
   60,   57,   62,  165,   58,   55,   56,   45,   78,  146,
   58,   55,   56,   61,   59,  250,   60,  122,   62,  123,
  145,   58,   55,   56,   98,   98,   59,   98,   61,   98,
  249,  197,   49,   50,   51,   60,  232,    0,  291,   53,
   54,   98,  285,  186,  108,  146,    0,    0,  107,  107,
    0,  107,   57,  107,   35,    0,    0,   35,  122,    0,
  123,   58,   55,   56,  110,  107,  111,  110,  108,  111,
    0,    0,   58,   55,   56,  171,    0,  170,    0,    0,
    0,    0,    0,    0,   49,   50,   51,    0,  227,    0,
  292,   53,   54,  233,    0,    0,   58,   55,   56,   82,
    0,   82,   82,   82,   57,    0,   87,    0,   87,   87,
   87,  234,  235,   58,   55,   56,    0,   82,   82,    0,
   82,    0,  135,  135,   87,   87,   83,   87,   83,   83,
   83,   84,    0,   84,   84,   84,    0,    0,    0,    0,
  135,    0,    0,    0,   83,   83,    0,   83,    0,   84,
   84,    0,   84,    0,  108,    0,    0,   63,   63,    0,
    0,    0,   63,    0,    0,    0,   63,    0,    0,    0,
    0,    0,    0,   63,   63,    0,    0,  276,  277,   63,
   63,    0,   58,   55,   56,   67,   67,   65,   65,    0,
   67,    0,   65,    0,   67,    0,   65,    0,    0,    0,
    0,   67,   67,   65,   65,    0,    0,   67,   67,   65,
   65,   49,   50,   51,    0,    0,    0,  298,   53,   54,
    0,   34,   34,    0,    0,    0,   34,   49,   50,   51,
   34,   57,    0,  299,   53,   54,  110,   34,   34,   98,
   58,   55,   56,   34,   34,    0,    0,   57,    0,    0,
   43,   43,    0,    0,    0,   43,   58,   55,   56,   43,
    0,    0,    0,  107,    0,    0,   43,   43,   98,   98,
    0,    0,   43,   43,    9,    0,    0,    0,   27,    0,
    0,    0,   11,  166,  167,  168,  169,  154,  156,   12,
   13,    0,  107,  107,  110,   14,   15,    0,  176,  177,
  178,   30,   30,    0,    0,    0,   30,    0,    0,    0,
   30,    0,  183,    0,    0,    0,    0,   30,   30,    0,
    0,  110,  110,   30,   30,    0,   82,   82,   82,   82,
    0,    0,    0,   87,   87,   87,   87,    0,  208,    0,
    0,    0,    0,    0,    0,    0,    0,  215,    0,    0,
    0,    0,    0,   83,   83,   83,   83,  218,   84,   84,
   84,   84,    0,    0,    0,    9,    0,    0,    0,  236,
    0,    0,    0,   11,    9,    0,    0,    0,  278,  110,
   12,   13,   11,  110,  110,    0,   14,   15,    0,   12,
   13,    0,    0,    9,  247,   14,   15,  279,    0,  156,
    0,   11,    9,    0,    0,    0,  293,    0,   12,   13,
   11,    0,    0,    0,   14,   15,    0,   12,   13,    0,
    0,    0,    0,   14,   15,
};
}
static short yycheck[];
static { yycheck(); }
static void yycheck() {
yycheck = new short[] {                          6,
    7,    5,   42,   31,   15,   45,   40,   47,   15,   27,
   28,   44,   44,   59,   59,  257,  257,   45,   46,   61,
   62,   59,  257,   34,   42,  163,   40,   59,  257,   42,
   44,  256,   43,  257,   47,   78,   59,  257,   77,   78,
  281,  283,   40,  272,  273,   59,   40,  272,  273,   77,
   78,  286,  287,  266,   40,   44,   42,   43,  261,   45,
   40,   47,   42,   43,  107,   45,   94,   47,  107,   40,
   59,   42,   43,  280,   45,   40,   47,   42,   43,  107,
   45,  219,   47,   41,   95,   40,   44,  256,  257,  131,
  132,   46,   41,   42,   43,   41,   45,  266,   47,  122,
  123,   41,   42,   43,   44,   45,   61,   47,   41,   42,
   43,   59,   45,   59,   47,  133,   43,   41,   45,   59,
   60,  261,   62,   41,   42,   43,   43,   45,   45,   47,
  173,   40,   59,   41,  173,   59,   91,   46,  257,   45,
  151,   42,   43,  147,   45,  173,   47,   44,  256,  257,
  193,   59,   61,   40,  193,   44,   42,   43,   40,   45,
  188,   47,   59,  256,  257,  193,   44,   41,  261,   43,
   59,   45,  265,   42,   43,   59,   45,  262,   47,  272,
  273,   59,   91,  256,  257,  278,  279,  272,  273,   42,
   43,  262,   45,  278,   47,   41,  203,   43,   40,   45,
  207,  272,  273,  256,  257,   42,   43,  278,   45,   41,
   47,   59,   44,  259,  260,  260,  256,  257,  236,  257,
   59,   42,   43,   59,   45,  257,   47,   41,  257,  240,
   44,   42,   43,  261,   45,  257,   47,   42,   43,   40,
   45,  267,   47,  277,  257,  285,  286,  287,   42,  282,
   41,   45,   43,   47,   45,  259,  260,   44,  286,  287,
  278,  279,  257,  274,  271,  272,  294,  284,  282,  297,
  281,  257,  290,  286,  287,  293,  256,  257,  258,   41,
  284,  261,  289,  263,  264,  256,  257,   40,   42,   43,
   40,   45,  257,   47,   59,   41,  276,  277,   44,  285,
  286,  287,   45,  286,  287,  285,  286,  287,  257,   41,
   93,   43,  267,   45,  285,  286,  287,  257,  256,  257,
  285,  286,  287,   93,  257,  274,   45,   93,  268,  269,
  270,  271,   41,   40,   40,   44,  285,  286,  287,  257,
   42,   43,   41,   45,   41,   47,  286,  287,  298,  299,
  256,  257,  285,  286,  287,  256,  257,  258,  267,   59,
   41,  262,  263,  264,   59,   41,   59,  285,  286,  287,
  256,  257,  258,   40,   59,  276,  262,  263,  264,  285,
  286,  287,   59,   40,  285,  286,  287,  256,  257,  258,
  276,  257,   59,  262,  263,  264,  257,   40,   59,  285,
  286,  287,  261,  256,  257,  258,   59,  276,  261,   42,
  263,  264,   45,   59,   47,   51,  285,  286,  287,  256,
  257,  258,   41,  276,   59,  262,  263,  264,   41,   59,
  282,   44,  285,  286,  287,  256,  257,  258,   59,  276,
    0,  262,  263,  264,   41,   59,  257,   44,  285,  286,
  287,  256,  257,  258,   59,  276,   59,  262,  263,  264,
  146,  261,  256,  257,  285,  286,  287,   42,   43,  261,
   45,  276,   47,  109,  285,  286,  287,  261,   59,   59,
  285,  286,  287,   42,   43,   41,   45,   43,   47,   45,
   59,  285,  286,  287,   42,   43,   59,   45,   59,   47,
  214,  137,  256,  257,  258,   59,  198,   -1,  262,  263,
  264,   59,   41,  256,  257,   44,   -1,   -1,   42,   43,
   -1,   45,  276,   47,   41,   -1,   -1,   44,   43,   -1,
   45,  285,  286,  287,   41,   59,   41,   44,  257,   44,
   -1,   -1,  285,  286,  287,   60,   -1,   62,   -1,   -1,
   -1,   -1,   -1,   -1,  256,  257,  258,   -1,  194,   -1,
  262,  263,  264,  199,   -1,   -1,  285,  286,  287,   41,
   -1,   43,   44,   45,  276,   -1,   41,   -1,   43,   44,
   45,  200,  201,  285,  286,  287,   -1,   59,   60,   -1,
   62,   -1,  259,  260,   59,   60,   41,   62,   43,   44,
   45,   41,   -1,   43,   44,   45,   -1,   -1,   -1,   -1,
  277,   -1,   -1,   -1,   59,   60,   -1,   62,   -1,   59,
   60,   -1,   62,   -1,  257,   -1,   -1,  256,  257,   -1,
   -1,   -1,  261,   -1,   -1,   -1,  265,   -1,   -1,   -1,
   -1,   -1,   -1,  272,  273,   -1,   -1,  266,  267,  278,
  279,   -1,  285,  286,  287,  256,  257,  256,  257,   -1,
  261,   -1,  261,   -1,  265,   -1,  265,   -1,   -1,   -1,
   -1,  272,  273,  272,  273,   -1,   -1,  278,  279,  278,
  279,  256,  257,  258,   -1,   -1,   -1,  262,  263,  264,
   -1,  256,  257,   -1,   -1,   -1,  261,  256,  257,  258,
  265,  276,   -1,  262,  263,  264,   51,  272,  273,  257,
  285,  286,  287,  278,  279,   -1,   -1,  276,   -1,   -1,
  256,  257,   -1,   -1,   -1,  261,  285,  286,  287,  265,
   -1,   -1,   -1,  257,   -1,   -1,  272,  273,  286,  287,
   -1,   -1,  278,  279,  257,   -1,   -1,   -1,  261,   -1,
   -1,   -1,  265,  268,  269,  270,  271,  102,  103,  272,
  273,   -1,  286,  287,  109,  278,  279,   -1,  113,  114,
  115,  256,  257,   -1,   -1,   -1,  261,   -1,   -1,   -1,
  265,   -1,  127,   -1,   -1,   -1,   -1,  272,  273,   -1,
   -1,  136,  137,  278,  279,   -1,  268,  269,  270,  271,
   -1,   -1,   -1,  268,  269,  270,  271,   -1,  153,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,  162,   -1,   -1,
   -1,   -1,   -1,  268,  269,  270,  271,  172,  268,  269,
  270,  271,   -1,   -1,   -1,  257,   -1,   -1,   -1,  261,
   -1,   -1,   -1,  265,  257,   -1,   -1,   -1,  261,  194,
  272,  273,  265,  198,  199,   -1,  278,  279,   -1,  272,
  273,   -1,   -1,  257,  209,  278,  279,  261,   -1,  214,
   -1,  265,  257,   -1,   -1,   -1,  261,   -1,  272,  273,
  265,   -1,   -1,   -1,  278,  279,   -1,  272,  273,   -1,
   -1,   -1,   -1,  278,  279,
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

//#line 927 "gram.y"


/* CODIGO DE SOPORTE                                                         */

static Parser parser;

static int cant_errores = 0;
static int cant_errores_semanticos = 0;
static int cant_retornos_auto = -1;

public static String ambitoActual = ":main";
public static Stack<Integer> pilaSaltos = new Stack<>();

public static void entrarAmbito(String nombre) {
    ambitoActual = ambitoActual + ":" + nombre;
}

public static void salirAmbito() {
    int index = ambitoActual.lastIndexOf(':');
    if (index > 0) {
        ambitoActual = ambitoActual.substring(0, index);
    } else {
        ambitoActual = ":main";
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
        ambitoActual = ":main";
        pilaSaltos.clear();
        cant_errores = 0;
        cant_errores_semanticos = 0;
        AnalizadorLexico.reader = new PushbackReader(new BufferedReader(
                new InputStreamReader(new FileInputStream(ruta), java.nio.charset.StandardCharsets.UTF_8)));
        parser = new Parser(debug);
        parser.yyparse(); 
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
//#line 729 "Parser.java"
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
            int ref = val_peek(0).ival;
            String nombre = TablaSimbolos.obtenerAtributo(ref, TablaSimbolos.LEXEMA);
            TablaSimbolos.declararVariable(nombre, "programa", "nombre_programa", ambitoActual);
            yyval = val_peek(0);
        }
break;
case 2:
//#line 62 "gram.y"
{ 
            if (cant_errores == 0 && cant_errores_semanticos == 0) {
                System.out.println("Programa reconocido correctamente"); 
            } else {
                System.out.println("Compilacion finalizada con " + cant_errores + " error(es) sintactico(s) y " + cant_errores_semanticos + " error(es) semantico(s)");
            }
        }
break;
case 3:
//#line 69 "gram.y"
{ yyerror("Falta el nombre del programa al inicio"); }
break;
case 4:
//#line 70 "gram.y"
{ 
            System.out.println("Compilacion finalizada con " + cant_errores + " error(es) sintactico(s) y " + cant_errores_semanticos + " error(es) semantico(s)");
        }
break;
case 5:
//#line 73 "gram.y"
{ yyerror("Falta el delimitador BEGIN de sentencias ejecutables"); }
break;
case 6:
//#line 74 "gram.y"
{
            System.out.println("Compilacion finalizada con " + cant_errores + " error(es) sintactico(s) y " + cant_errores_semanticos + " error(es) semantico(s)");
        }
break;
case 7:
//#line 78 "gram.y"
{
            yyerror("Falta el delimitador END al final del programa");
            System.out.println("Compilacion finalizada con " + cant_errores + " error(es) sintactico(s) y " + cant_errores_semanticos + " error(es) semantico(s)");
        }
break;
case 10:
//#line 92 "gram.y"
{ imprimirRegla("declaracion_variables"); }
break;
case 11:
//#line 93 "gram.y"
{ imprimirRegla("declaracion_funciones"); }
break;
case 12:
//#line 94 "gram.y"
{ imprimirRegla("declaracion_clase"); }
break;
case 13:
//#line 95 "gram.y"
{ imprimirRegla("declaracion_objeto"); }
break;
case 14:
//#line 96 "gram.y"
{ imprimirRegla("declaracion_comptime"); }
break;
case 15:
//#line 102 "gram.y"
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
//#line 113 "gram.y"
{ yyerror("Falta ';' al final de la declaracion de variables"); }
break;
case 17:
//#line 118 "gram.y"
{
            ArrayList<String> lista = new ArrayList<>();
            int ref = val_peek(0).ival;
            String lexema = TablaSimbolos.obtenerAtributo(ref, TablaSimbolos.LEXEMA);
            lista.add(lexema);
            ParserVal pv = new ParserVal();
            pv.obj = lista;
            yyval = pv;
        }
break;
case 18:
//#line 128 "gram.y"
{
            ArrayList<String> lista = (ArrayList<String>) val_peek(2).obj;
            int ref = val_peek(0).ival;
            String lexema = TablaSimbolos.obtenerAtributo(ref, TablaSimbolos.LEXEMA);
            lista.add(lexema);
            yyval = val_peek(2);
        }
break;
case 19:
//#line 135 "gram.y"
{ yyerror("Falta ',' entre los identificadores"); yyval = val_peek(1); }
break;
case 20:
//#line 140 "gram.y"
{
            ArrayList<String> lista = new ArrayList<>();
            int ref = val_peek(0).ival;
            String lexema = TablaSimbolos.obtenerAtributo(ref, TablaSimbolos.LEXEMA);
            lista.add(lexema);
            ParserVal pv = new ParserVal();
            pv.obj = lista;
            yyval = pv;
        }
break;
case 21:
//#line 150 "gram.y"
{
            ArrayList<String> lista = (ArrayList<String>) val_peek(2).obj;
            int ref = val_peek(0).ival;
            String lexema = TablaSimbolos.obtenerAtributo(ref, TablaSimbolos.LEXEMA);
            lista.add(lexema);
            yyval = val_peek(2);
        }
break;
case 22:
//#line 160 "gram.y"
{ yyval = new ParserVal("SHORTINT"); }
break;
case 23:
//#line 161 "gram.y"
{ yyval = new ParserVal("SINGLEF"); }
break;
case 24:
//#line 168 "gram.y"
{
            int ref = val_peek(0).ival;
            String nombre = TablaSimbolos.obtenerAtributo(ref, TablaSimbolos.LEXEMA);
            TablaSimbolos.declararVariable(nombre, val_peek(2).sval, "funcion", ambitoActual);
            entrarAmbito(nombre);
            yyval = val_peek(0);
        }
break;
case 25:
//#line 179 "gram.y"
{
            int ref = val_peek(0).ival;
            String nombre = TablaSimbolos.obtenerAtributo(ref, TablaSimbolos.LEXEMA);
            TablaSimbolos.declararVariable(nombre, "auto", "funcion", ambitoActual);
            entrarAmbito(nombre);
            yyval = val_peek(0);
        }
break;
case 26:
//#line 190 "gram.y"
{ cant_retornos_auto = 0; }
break;
case 27:
//#line 197 "gram.y"
{
            salirAmbito();
        }
break;
case 28:
//#line 200 "gram.y"
{ yyerror("Falta nombre de funcion"); }
break;
case 30:
//#line 206 "gram.y"
{
            yyerror("Falta ';' al final de la declaracion de funcion");
            salirAmbito();
        }
break;
case 31:
//#line 211 "gram.y"
{
            if (cant_retornos_auto == 0) {
                yyerror("Ausencia de sentencia de retorno 'RET' en funcion AUTO");
            }
            cant_retornos_auto = -1;
            salirAmbito();
        }
break;
case 32:
//#line 218 "gram.y"
{ yyerror("Falta nombre de funcion"); }
break;
case 34:
//#line 222 "gram.y"
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
//#line 240 "gram.y"
{
            int ref = val_peek(0).ival;
            String nombre = TablaSimbolos.obtenerAtributo(ref, TablaSimbolos.LEXEMA);
            TablaSimbolos.declararVariable(nombre, val_peek(1).sval, "parametro", ambitoActual);
            imprimirRegla("parametro_formal");
        }
break;
case 39:
//#line 246 "gram.y"
{ yyerror("Falta de nombre de parametro formal en declaracion de funcion"); }
break;
case 40:
//#line 247 "gram.y"
{ yyerror("Falta de tipo del parametro formal en declaracion de funcion"); }
break;
case 41:
//#line 255 "gram.y"
{
            int ref = val_peek(0).ival;
            String nombre = TablaSimbolos.obtenerAtributo(ref, TablaSimbolos.LEXEMA);
            TablaSimbolos.declararVariable(nombre, "clase", "nombre_clase", ambitoActual);
            entrarAmbito(nombre);
            yyval = val_peek(0);
        }
break;
case 42:
//#line 267 "gram.y"
{
            salirAmbito();
        }
break;
case 43:
//#line 272 "gram.y"
{
            yyerror("Falta ';' al final de la declaracion de clase");
            salirAmbito();
        }
break;
case 45:
//#line 280 "gram.y"
{ imprimirRegla("importacion_opcional"); }
break;
case 46:
//#line 282 "gram.y"
{ yyerror("Falta la palabra clave 'FROM' en la declaracion IMPORT"); }
break;
case 48:
//#line 287 "gram.y"
{ imprimirRegla("herencia_opcional"); }
break;
case 49:
//#line 289 "gram.y"
{ yyerror("Falta nombre o lista de clases a heredar luego de 'EXTENDS'"); }
break;
case 54:
//#line 304 "gram.y"
{
            String tipoDato = val_peek(3).sval;
            ArrayList<String> variables = (ArrayList<String>) val_peek(2).obj;
            if (variables != null) {
                for (int i = 0; i < variables.size(); i++) {
                    String nombreVar = variables.get(i);
                    TablaSimbolos.declararVariable(nombreVar, tipoDato, "atributo", ambitoActual);
                }
            }
        }
break;
case 55:
//#line 320 "gram.y"
{
            salirAmbito();
        }
break;
case 56:
//#line 325 "gram.y"
{
            salirAmbito();
        }
break;
case 57:
//#line 332 "gram.y"
{
            int ref = val_peek(0).ival;
            String nombre = TablaSimbolos.obtenerAtributo(ref, TablaSimbolos.LEXEMA);
            TablaSimbolos.declararVariable(nombre, val_peek(1).sval, "metodo", ambitoActual);
            entrarAmbito(nombre);
            yyval = val_peek(0);
        }
break;
case 58:
//#line 343 "gram.y"
{
            int ref = val_peek(0).ival;
            String nombre = TablaSimbolos.obtenerAtributo(ref, TablaSimbolos.LEXEMA);
            TablaSimbolos.declararVariable(nombre, "auto", "metodo", ambitoActual);
            entrarAmbito(nombre);
            yyval = val_peek(0);
        }
break;
case 60:
//#line 354 "gram.y"
{ imprimirRegla("exportacion_opcional"); }
break;
case 61:
//#line 356 "gram.y"
{ yyerror("Falta la palabra clave 'TO' en la declaracion EXPORT"); }
break;
case 62:
//#line 362 "gram.y"
{ imprimirRegla("declaracion_de_objeto"); }
break;
case 63:
//#line 364 "gram.y"
{ yyerror("Falta ';' al final de la declaracion de objeto"); }
break;
case 64:
//#line 371 "gram.y"
{
            String tipoDato = val_peek(2).sval;
            ArrayList<String> variables = (ArrayList<String>) val_peek(1).obj;
            if (variables != null) {
                for (int i = 0; i < variables.size(); i++) {
                    String nombreVar = variables.get(i);
                    int id = TablaSimbolos.declararVariable(nombreVar, tipoDato, "variable", ambitoActual);
                    if (id != TablaSimbolos.LEXEMA_NO_ENCONTRADO) {
                        TablaSimbolos.agregarAtributo(id, "COMPTIME", "true");
                    }
                }
            }
        }
break;
case 65:
//#line 385 "gram.y"
{ yyerror("Falta ';' al final de la declaracion comptime"); }
break;
case 66:
//#line 387 "gram.y"
{ yyerror("Falta el tipo de dato en la declaracion comptime"); }
break;
case 67:
//#line 389 "gram.y"
{
            yyerror("Falta el tipo de dato en la declaracion comptime");
            yyerror("Falta ';' al final de la declaracion comptime");
        }
break;
case 69:
//#line 399 "gram.y"
{ imprimirRegla("sentencia_ejecutable"); }
break;
case 76:
//#line 410 "gram.y"
{ yyerror("Sentencia ejecutable malformada o falta ';' previo"); }
break;
case 77:
//#line 415 "gram.y"
{
            if (val_peek(0) != null && val_peek(0).sval != null) {
                int ref = val_peek(2).ival;
                String lexema = TablaSimbolos.obtenerAtributo(ref, TablaSimbolos.LEXEMA);
                int idVar = TablaSimbolos.buscarIdentificador(lexema, ambitoActual);
                if (idVar != TablaSimbolos.LEXEMA_NO_ENCONTRADO) {
                    String mangling = TablaSimbolos.obtenerAtributo(idVar, TablaSimbolos.MANGLING);
                    String tipoVar = TablaSimbolos.obtenerAtributo(idVar, TablaSimbolos.TIPO);
                    String tipoExp = (String) val_peek(0).obj;
                    if (tipoExp != null) {
                        if (tipoVar != null && tipoVar.equals(tipoExp)) {
                            GeneradorTerceto.crearTerceto(":=", mangling, val_peek(0).sval, tipoVar);
                        } else if ("SHORTINT".equals(tipoVar) && "SINGLEF".equals(tipoExp)) {
                            agregarErrorSemantico("Incompatibilidad de tipos en la asignacion: no se puede asignar SINGLEF a la variable SHORTINT '" + lexema + "' sin conversion explicita 'tos'");
                        } else if ("SINGLEF".equals(tipoVar) && "SHORTINT".equals(tipoExp)) {
                            agregarErrorSemantico("Incompatibilidad de tipos en la asignacion: no se puede asignar SHORTINT a la variable SINGLEF '" + lexema + "'");
                        } else {
                            agregarErrorSemantico("Incompatibilidad de tipos en la asignacion a '" + lexema + "': se esperaba " + tipoVar + " pero se encontro " + tipoExp);
                        }
                    }
                } else {
                    agregarErrorSemantico("Variable '" + lexema + "' no declarada o no visible en el ambito actual (" + ambitoActual + ")");
                }
            }
        }
break;
case 81:
//#line 447 "gram.y"
{ yyerror("Acceso posicional invalido: solo se permite constante SHORTINT positiva o identificador"); yyval = new ParserVal(); }
break;
case 82:
//#line 453 "gram.y"
{
            yyval = val_peek(0);
        }
break;
case 83:
//#line 457 "gram.y"
{
            if (val_peek(2) != null && val_peek(2).sval != null && val_peek(0) != null && val_peek(0).sval != null) {
                String tipo1 = (String) val_peek(2).obj;
                String tipo2 = (String) val_peek(0).obj;
                if (tipo1 != null && tipo2 != null) {
                    if (tipo1.equals(tipo2)) {
                        int idTerceto = GeneradorTerceto.crearTerceto("+", val_peek(2).sval, val_peek(0).sval, tipo1);
                        ParserVal pv = new ParserVal("[" + idTerceto + "]");
                        pv.obj = tipo1;
                        yyval = pv;
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
case 84:
//#line 479 "gram.y"
{
            if (val_peek(2) != null && val_peek(2).sval != null && val_peek(0) != null && val_peek(0).sval != null) {
                String tipo1 = (String) val_peek(2).obj;
                String tipo2 = (String) val_peek(0).obj;
                if (tipo1 != null && tipo2 != null) {
                    if (tipo1.equals(tipo2)) {
                        int idTerceto = GeneradorTerceto.crearTerceto("-", val_peek(2).sval, val_peek(0).sval, tipo1);
                        ParserVal pv = new ParserVal("[" + idTerceto + "]");
                        pv.obj = tipo1;
                        yyval = pv;
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
case 85:
//#line 500 "gram.y"
{ yyerror("Falta un operando en la expresion"); yyval = new ParserVal(); }
break;
case 86:
//#line 501 "gram.y"
{ yyerror("Falta un operando en la expresion"); yyval = new ParserVal(); }
break;
case 87:
//#line 502 "gram.y"
{ yyerror("Falta un operando en la expresion"); yyval = new ParserVal(); }
break;
case 88:
//#line 506 "gram.y"
{
            yyval = val_peek(0);
        }
break;
case 89:
//#line 510 "gram.y"
{
            if (val_peek(2) != null && val_peek(2).sval != null && val_peek(0) != null && val_peek(0).sval != null) {
                String tipo1 = (String) val_peek(2).obj;
                String tipo2 = (String) val_peek(0).obj;
                if (tipo1 != null && tipo2 != null) {
                    if (tipo1.equals(tipo2)) {
                        int idTerceto = GeneradorTerceto.crearTerceto("*", val_peek(2).sval, val_peek(0).sval, tipo1);
                        ParserVal pv = new ParserVal("[" + idTerceto + "]");
                        pv.obj = tipo1;
                        yyval = pv;
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
case 90:
//#line 532 "gram.y"
{
            if (val_peek(2) != null && val_peek(2).sval != null && val_peek(0) != null && val_peek(0).sval != null) {
                String tipo1 = (String) val_peek(2).obj;
                String tipo2 = (String) val_peek(0).obj;
                if (tipo1 != null && tipo2 != null) {
                    if (tipo1.equals(tipo2)) {
                        int idTerceto = GeneradorTerceto.crearTerceto("/", val_peek(2).sval, val_peek(0).sval, tipo1);
                        ParserVal pv = new ParserVal("[" + idTerceto + "]");
                        pv.obj = tipo1;
                        yyval = pv;
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
case 91:
//#line 553 "gram.y"
{ yyerror("Falta un operando en la expresion"); yyval = new ParserVal(); }
break;
case 92:
//#line 554 "gram.y"
{ yyerror("Falta un operando en la expresion"); yyval = new ParserVal(); }
break;
case 93:
//#line 555 "gram.y"
{ yyerror("Falta un operando en la expresion"); yyval = new ParserVal(); }
break;
case 94:
//#line 556 "gram.y"
{ yyerror("Falta un operando en la expresion"); yyval = new ParserVal(); }
break;
case 95:
//#line 557 "gram.y"
{ yyerror("Falta operador en la expresion"); yyval = new ParserVal(); }
break;
case 96:
//#line 558 "gram.y"
{ yyerror("Falta operador en la expresion"); yyval = new ParserVal(); }
break;
case 97:
//#line 559 "gram.y"
{ yyerror("Falta operador en la expresion"); yyval = new ParserVal(); }
break;
case 98:
//#line 564 "gram.y"
{
            int ref = val_peek(0).ival;
            String lexema = TablaSimbolos.obtenerAtributo(ref, TablaSimbolos.LEXEMA);
            int idVar = TablaSimbolos.buscarIdentificador(lexema, ambitoActual);
            if (idVar != TablaSimbolos.LEXEMA_NO_ENCONTRADO) {
                String mangling = TablaSimbolos.obtenerAtributo(idVar, TablaSimbolos.MANGLING);
                String tipo = TablaSimbolos.obtenerAtributo(idVar, TablaSimbolos.TIPO);
                ParserVal pv = new ParserVal(mangling);
                pv.ival = idVar;
                pv.obj = tipo;
                yyval = pv;
            } else {
                agregarErrorSemantico("Variable '" + lexema + "' no declarada o no visible en el ambito actual (" + ambitoActual + ")");
                yyval = new ParserVal();
            }
        }
break;
case 99:
//#line 581 "gram.y"
{
            int id = val_peek(0).ival;
            String lexema = TablaSimbolos.obtenerAtributo(id, TablaSimbolos.LEXEMA);
            int valor = Integer.parseInt(lexema);
            if (valor > AnalizadorLexico.ValorMaximoInt) {
                yyerror("Constante shortint positiva fuera de rango (" + lexema + "). Rango permitido: [-128, 127]");
            }
            TablaSimbolos.agregarAtributo(id, TablaSimbolos.TIPO, "SHORTINT");
            ParserVal pv = new ParserVal(lexema);
            pv.ival = id;
            pv.obj = "SHORTINT";
            yyval = pv;
        }
break;
case 100:
//#line 595 "gram.y"
{
            int id = val_peek(0).ival;
            String lexema = TablaSimbolos.obtenerAtributo(id, TablaSimbolos.LEXEMA);
            double valor = Double.parseDouble(lexema);
            if (valor != 0.0 && (valor < AnalizadorLexico.ValorMinimoFloat || valor > AnalizadorLexico.ValorMaximoFloat)) {
                yyerror("Constante singlef fuera de rango (" + lexema + ")");
            }
            TablaSimbolos.agregarAtributo(id, TablaSimbolos.TIPO, "SINGLEF");
            ParserVal pv = new ParserVal(lexema);
            pv.ival = id;
            pv.obj = "SINGLEF";
            yyval = pv;
        }
break;
case 104:
//#line 612 "gram.y"
{
            yyval = val_peek(0);
        }
break;
case 105:
//#line 616 "gram.y"
{
            yyval = val_peek(0);
        }
break;
case 106:
//#line 620 "gram.y"
{
            yyval = val_peek(0);
        }
break;
case 108:
//#line 627 "gram.y"
{ imprimirRegla("invocacion_metodo"); }
break;
case 109:
//#line 628 "gram.y"
{ imprimirRegla("invocacion_metodo"); }
break;
case 112:
//#line 639 "gram.y"
{
            if (val_peek(1) != null && val_peek(1).sval != null) {
                int ref = val_peek(4).ival;
                String lexema = TablaSimbolos.obtenerAtributo(ref, TablaSimbolos.LEXEMA);
                int idVar = TablaSimbolos.buscarIdentificador(lexema, ambitoActual);
                if (idVar != TablaSimbolos.LEXEMA_NO_ENCONTRADO) {
                    String mangling = TablaSimbolos.obtenerAtributo(idVar, TablaSimbolos.MANGLING);
                    String tipoVar = TablaSimbolos.obtenerAtributo(idVar, TablaSimbolos.TIPO);
                    String tipoExp = (String) val_peek(1).obj;
                    if (tipoExp != null) {
                        if (tipoVar != null && tipoVar.equals(tipoExp)) {
                            int idTerceto = GeneradorTerceto.crearTerceto(":=", mangling, val_peek(1).sval, tipoVar);
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
                } else {
                    agregarErrorSemantico("Variable '" + lexema + "' no declarada o no visible en el ambito actual (" + ambitoActual + ")");
                    yyval = new ParserVal();
                }
            } else {
                yyval = new ParserVal();
            }
        }
break;
case 113:
//#line 676 "gram.y"
{
            yyerror("Uso del simbolo ':=' donde debe usarse '=' en asignacion dentro de expresion");
            yyval = new ParserVal();
        }
break;
case 114:
//#line 684 "gram.y"
{
            int id_pos = val_peek(0).ival;
            String lexema_pos = TablaSimbolos.obtenerAtributo(id_pos, TablaSimbolos.LEXEMA);
            int id_neg = TablaSimbolos.convertirANegativo(lexema_pos);
            TablaSimbolos.agregarAtributo(id_neg, TablaSimbolos.TIPO, "SHORTINT");
            ParserVal pv = new ParserVal("-" + lexema_pos);
            pv.ival = id_neg;
            pv.obj = "SHORTINT";
            yyval = pv;
        }
break;
case 115:
//#line 695 "gram.y"
{
            int id_pos = val_peek(0).ival;
            String lexema_pos = TablaSimbolos.obtenerAtributo(id_pos, TablaSimbolos.LEXEMA);
            int id_neg = TablaSimbolos.convertirANegativo(lexema_pos);
            TablaSimbolos.agregarAtributo(id_neg, TablaSimbolos.TIPO, "SINGLEF");
            ParserVal pv = new ParserVal("-" + lexema_pos);
            pv.ival = id_neg;
            pv.obj = "SINGLEF";
            yyval = pv;
        }
break;
case 116:
//#line 710 "gram.y"
{
            if (val_peek(1) != null && val_peek(1).sval != null) {
                String tipoExp = (String) val_peek(1).obj;
                if ("SINGLEF".equals(tipoExp)) {
                    int idTerceto = GeneradorTerceto.crearTerceto("tos", val_peek(1).sval, "-", "SHORTINT");
                    ParserVal pv = new ParserVal("[" + idTerceto + "]");
                    pv.obj = "SHORTINT";
                    yyval = pv;
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
case 117:
//#line 733 "gram.y"
{
            if (val_peek(1) != null && val_peek(1).sval != null) {
                int idBF = GeneradorTerceto.crearTerceto("BF", val_peek(1).sval, "?");
                pilaSaltos.push(idBF);
            }
        }
break;
case 119:
//#line 744 "gram.y"
{ yyerror("Falta '(' de apertura en condicion de seleccion"); }
break;
case 120:
//#line 746 "gram.y"
{ yyerror("Condicion de seleccion malformada o error en parentesis"); }
break;
case 121:
//#line 751 "gram.y"
{
            if (!pilaSaltos.isEmpty()) {
                int idBF = pilaSaltos.pop();
                int idBI = GeneradorTerceto.crearTerceto("BI", "?", "-");
                GeneradorTerceto.completarSalto(idBF, GeneradorTerceto.getProximoIndice());
                pilaSaltos.push(idBI);
            }
        }
break;
case 122:
//#line 763 "gram.y"
{
            if (!pilaSaltos.isEmpty()) {
                int idSalto = pilaSaltos.pop();
                GeneradorTerceto.completarSalto(idSalto, GeneradorTerceto.getProximoIndice());
            }
        }
break;
case 123:
//#line 770 "gram.y"
{
            if (!pilaSaltos.isEmpty()) {
                int idBI = pilaSaltos.pop();
                GeneradorTerceto.completarSalto(idBI, GeneradorTerceto.getProximoIndice());
            }
        }
break;
case 124:
//#line 777 "gram.y"
{
            if (!pilaSaltos.isEmpty()) {
                int idSalto = pilaSaltos.pop();
                GeneradorTerceto.completarSalto(idSalto, GeneradorTerceto.getProximoIndice());
            }
            yyerror("Falta palabra clave END_IF al final de la sentencia IF");
        }
break;
case 125:
//#line 785 "gram.y"
{
            if (!pilaSaltos.isEmpty()) {
                int idBI = pilaSaltos.pop();
                GeneradorTerceto.completarSalto(idBI, GeneradorTerceto.getProximoIndice());
            }
            yyerror("Falta palabra clave END_IF al final de la sentencia IF");
        }
break;
case 126:
//#line 796 "gram.y"
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
case 127:
//#line 818 "gram.y"
{ yyval = new ParserVal(">="); }
break;
case 128:
//#line 819 "gram.y"
{ yyval = new ParserVal("<="); }
break;
case 129:
//#line 820 "gram.y"
{ yyval = new ParserVal("=="); }
break;
case 130:
//#line 821 "gram.y"
{ yyval = new ParserVal("!="); }
break;
case 131:
//#line 822 "gram.y"
{ yyval = new ParserVal(">"); }
break;
case 132:
//#line 823 "gram.y"
{ yyval = new ParserVal("<"); }
break;
case 135:
//#line 830 "gram.y"
{ yyerror("Falta el delimitador END en el bloque de sentencias"); }
break;
case 136:
//#line 836 "gram.y"
{
            pilaSaltos.push(GeneradorTerceto.getProximoIndice());
        }
break;
case 137:
//#line 843 "gram.y"
{
            if (!pilaSaltos.isEmpty()) {
                int dirInicio = pilaSaltos.pop();
                if (val_peek(1) != null && val_peek(1).sval != null) {
                    GeneradorTerceto.crearTerceto("BF", val_peek(1).sval, "[" + dirInicio + "]");
                }
            }
        }
break;
case 138:
//#line 852 "gram.y"
{
            if (!pilaSaltos.isEmpty()) pilaSaltos.pop();
            yyerror("Falta el cuerpo de la iteracion REPEAT");
        }
break;
case 139:
//#line 857 "gram.y"
{
            if (!pilaSaltos.isEmpty()) pilaSaltos.pop();
            yyerror("Falta palabra clave UNTIL en la iteracion REPEAT");
        }
break;
case 140:
//#line 862 "gram.y"
{
            if (!pilaSaltos.isEmpty()) pilaSaltos.pop();
            yyerror("Falta palabra clave UNTIL y cuerpo en la iteracion REPEAT");
        }
break;
case 141:
//#line 867 "gram.y"
{
            if (!pilaSaltos.isEmpty()) pilaSaltos.pop();
            yyerror("Falta palabra clave UNTIL y ')' en la iteracion REPEAT");
        }
break;
case 142:
//#line 872 "gram.y"
{
            if (!pilaSaltos.isEmpty()) pilaSaltos.pop();
            yyerror("Falta palabra clave UNTIL, cuerpo y ')' en la iteracion REPEAT");
        }
break;
case 143:
//#line 880 "gram.y"
{
            yyval = val_peek(1);
        }
break;
case 144:
//#line 884 "gram.y"
{
            yyval = val_peek(1);
            yyerror("Falta '(' de apertura en condicion de iteracion");
        }
break;
case 145:
//#line 889 "gram.y"
{
            yyval = val_peek(0);
            yyerror("Falta ')' de cierre en condicion de iteracion");
        }
break;
case 146:
//#line 894 "gram.y"
{
            yyval = val_peek(0);
            yyerror("Faltan parentesis en condicion de iteracion");
        }
break;
case 147:
//#line 903 "gram.y"
{
            int ref = val_peek(1).ival;
            String lexema = TablaSimbolos.obtenerAtributo(ref, TablaSimbolos.LEXEMA);
            GeneradorTerceto.crearTerceto("POUT", lexema, "-");
        }
break;
case 148:
//#line 909 "gram.y"
{
            if (val_peek(1) != null && val_peek(1).sval != null) {
                GeneradorTerceto.crearTerceto("POUT", val_peek(1).sval, "-", (String) val_peek(1).obj);
            }
        }
break;
case 149:
//#line 915 "gram.y"
{ yyerror("Falta argumento en sentencia pout"); }
break;
case 150:
//#line 920 "gram.y"
{
            if (cant_retornos_auto >= 0) {
                cant_retornos_auto++;
            }
        }
break;
//#line 1812 "Parser.java"
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
