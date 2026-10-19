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
   22,    9,    9,   23,   23,   23,   24,   24,   24,   25,
   25,   26,   26,   27,   28,   28,   30,   31,   29,   29,
   29,   10,   10,   11,   11,   11,   11,    3,    3,   32,
   32,   32,   32,   32,   32,   32,   33,   33,   39,   39,
   39,   34,   34,   34,   34,   34,   34,   40,   40,   40,
   40,   40,   40,   40,   40,   40,   40,   41,   41,   41,
   41,   41,   41,   41,   41,   41,   41,   43,   43,   42,
   42,   44,   44,   45,   45,   46,   35,   35,   35,   49,
   49,   49,   49,   47,   50,   50,   50,   50,   50,   50,
   48,   48,   48,   36,   36,   36,   36,   36,   36,   51,
   51,   51,   51,   37,   37,   37,   38,
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
    3,    5,    5,    2,    2,    4,    6,    5,    4,    2,
    4,    1,    3,    3,    1,    1,    1,    1,    1,    1,
    3,    1,    2,    5,    4,    6,    5,    5,    4,    3,
    2,    2,    1,    4,    4,    3,    4,
};
final static short yydefred[] = {                         0,
    3,    1,    0,    8,    8,    0,    0,    5,    0,   68,
    0,   22,   23,    0,    0,    9,   10,   11,   12,   13,
   14,    0,    0,    0,    0,    0,   68,   68,   20,    0,
    0,   41,    0,    0,    0,    0,   17,    0,    0,    0,
    0,   68,    0,    0,    0,    0,   62,    0,    0,    0,
    0,    2,    0,    0,   99,  100,    0,    0,    0,    0,
    0,    0,   69,    0,    0,   72,   73,    0,    0,    0,
    0,   88,  103,  104,  105,  106,   32,   25,    0,   66,
   16,   28,   24,   19,   15,    0,    0,    0,    0,   36,
    0,    0,    0,    0,    0,    4,    6,   21,   76,    0,
    0,    0,    0,    0,    0,    0,    0,    0,  107,    0,
    0,    0,   68,    0,    0,  132,    0,    0,    0,  114,
  115,   93,   94,   70,    0,    0,   71,   74,   75,    0,
   95,   96,   97,    0,    0,    0,   64,    0,   18,   40,
   39,   38,    0,    8,   26,    0,    0,    0,   50,    0,
    0,  101,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,  125,  126,  127,  128,  129,  130,    0,    0,
    0,  146,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,   85,    0,   86,    0,    0,   91,   89,   92,
   90,    0,    0,   37,    0,   31,   49,    0,    0,    0,
    0,  102,   80,   79,   81,    0,    0,    0,    0,  122,
  119,    0,    0,    0,  144,  145,  147,  131,    0,  141,
  135,  139,    0,    0,    0,  116,    0,    0,   68,   48,
    0,    0,    0,   51,   52,   53,    0,    0,  113,    0,
  108,    0,  112,    0,  120,    0,  118,  140,  137,  134,
  138,    0,    8,    8,    0,   42,   58,    0,    0,    0,
    0,  109,    0,  123,  117,  136,    0,    0,    0,    0,
    0,    0,    0,  121,   68,   68,   27,    0,    0,   54,
    8,    0,    0,    0,    0,    0,   68,    0,    0,   68,
    0,   33,   29,    0,    0,    0,    0,    0,   56,   55,
};
final static short yydgoto[] = {                          3,
    4,    6,   31,    5,   28,   16,   17,   18,   19,   20,
   21,   88,   39,   30,   23,   24,   25,   89,  138,  136,
   90,   26,   44,  149,  199,  234,  235,  236,  271,  237,
  238,   63,   64,   65,   66,   67,   68,   69,  109,   71,
   72,  154,   73,   74,   75,   76,  177,  117,  211,  169,
  178,
};
final static short yysindex[] = {                         3,
    0,    0,    0,    0,    0,  354,  363,    0, -205,    0,
 -191,    0,    0, -192,  -95,    0,    0,    0,    0,    0,
    0, -173,   39,   52, -165, -176,    0,    0,    0,  -22,
  113,    0,   14, -205,   27,   63,    0,   35,  -32, -121,
 -121,    0, -223, -135,  128,  145,    0, -126,   77,   49,
  -12,    0,  117,  124,    0,    0,   25,  126,   12,   60,
  202,  202,    0,   94,  136,    0,    0,  132,  141,  -91,
   66,    0,    0,    0,    0,    0,    0,    0,   80,    0,
    0,    0,    0,    0,    0,  -85,  -53,  110,   -5,    0,
   19,  160, -205,  189,  -63,    0,    0,    0,    0,    8,
   78, -241,  -21,  198,  177,  121,  336,  547,    0,  208,
   64,  336,    0,   33,  336,    0,  -20,  336,   66,    0,
    0,    0,    0,    0,  448,  456,    0,    0,    0,  336,
    0,    0,    0,  140,  187,  214,    0,  232,    0,    0,
    0,    0, -121,    0,    0,  218,  189,  -48,    0,  336,
  172,    0,  172,   44,  186,  203,  207,  245,  336,  -17,
  275,  281,    0,    0,    0,    0,    0,    0,  336,  177,
  290,    0,  153,  156,  192,  336,  321,  269,  -15,   33,
  336,  264,    0,   66,    0,   66,  172,    0,    0,    0,
    0, -121, -121,    0,  574,    0,    0,  115, -129,  296,
  336,    0,    0,    0,    0,   87,  316,  177,  295,    0,
    0,  177,  172,  -17,    0,    0,    0,    0,  327,    0,
    0,    0,  323,  328,    2,    0,   73,  104,    0,    0,
  334,  123,  137,    0,    0,    0,  370,  372,    0,  172,
    0,  142,    0,  -52,    0,  -17,    0,    0,    0,    0,
    0,  360,    0,    0,  261,    0,    0,    0,  -36, -121,
 -121,    0,  361,    0,    0,    0,  583,  606,  378, -234,
  380,  182,  243,    0,    0,    0,    0, -205,  189,    0,
    0,  168,  287,  313,  189,  608,    0,  392,  401,    0,
  424,    0,    0,  439,  170,  170,  402,  406,    0,    0,
};
final static short yyrindex[] = {                         0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,  181,    0,    0,    0,  373,
  470,    0,    0,    0,  383,    0,    0,    0,    0,  273,
  273,    0,    0,    0,    0,    0,    0,    0,    0,  171,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,  462,
  469,    0,    0,    0,    0,    0,    0,    0,  393,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,  215,  133,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,   56,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,  495,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,  412,  219,    0,    0,    0,
  416,    0,  292,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,  -19,    0,  426,    0,    0,    0,
    0,    0,    0,  521,    0,  543,  435,    0,    0,    0,
    0,  273,  273,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,   28,    0,    0,    0,    0,    0,  437,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
  489,    0,    0,    0,    0,    0,    0,    0,    0,  304,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,  -34,  438,  273,
  273,    0,    0,    0,    0,    0,    0,    0,  564,    0,
    0,    0,    0,    0,    0,    0,    0,    0,  440,    0,
    0,    0,    0,    0,  441,    0,    0,    0,    0,    0,
    0,    0,    0,    0,  438,  438,    0,    0,    0,    0,
};
final static short yygindex[] = {                         0,
    0,   -3,  -23,    0,    0,    0,    0,    0,    0,    0,
    0,   -6,    0,   -2,    0,    0,    0,  299,    0,    0,
  340,    0,    0,    0,    0,    0,    0,    0,  -67,    0,
    0,  -43,    0,  669,    0,    0,    0,    0,  -28,  -44,
   76,  300,    0,    0,    0,    0,  -13,  -58, -190,    0,
  322,
};
final static int YYTABLESIZE=887;
static short yytable[];
static { yytable();}
static void yytable(){
yytable = new short[]{                         22,
   22,    7,   70,   45,   46,   57,  264,   48,   34,   20,
  197,   86,   35,  116,  119,  155,   70,   70,   92,  181,
  133,   48,   29,  247,   20,  223,   85,  107,   70,   61,
   59,   79,   60,   29,   62,  144,   47,  110,  143,  133,
   94,  210,  252,  222,  156,  157,  160,  150,  278,   61,
   59,   29,   60,   61,   62,  265,   60,   93,   62,  145,
  251,  116,  143,   70,  115,   32,   61,   59,  124,   60,
   48,   62,  176,   33,   61,   59,   70,   60,   40,   62,
  184,  186,   36,   37,  202,   80,  124,  201,  101,  175,
  147,   41,   38,  162,  103,   42,   98,   98,   98,   98,
   98,  179,   98,   43,  172,   61,   59,  134,   60,  104,
   62,  214,  135,  253,   98,   98,  143,   98,  152,   61,
   59,   81,   60,   48,   62,   95,  116,  241,   61,   59,
   98,   60,  231,   62,   87,   99,  122,  123,  137,  102,
  195,   70,   12,   13,  254,  198,   70,  143,  232,  244,
   12,   13,  124,  246,   61,   59,  111,   60,   48,   62,
  101,   29,  219,  112,  116,  118,  103,  225,  116,   61,
   59,  139,   60,  230,   62,  130,   12,   13,  125,   70,
  126,  104,  262,   70,   60,  201,   61,   59,   22,   60,
  128,   62,  233,  216,  127,  125,  217,  126,  125,  129,
  126,   61,   59,  140,   60,  255,   62,  263,   29,  189,
  191,  102,   98,   98,  125,   98,  126,   98,   61,   59,
  148,   60,  281,   62,   84,  143,   70,  297,  298,   98,
  259,   60,   48,   61,   59,  158,   60,  159,   62,  133,
  133,  208,  209,  105,  106,  270,   60,   20,  170,  267,
  268,  283,  284,  192,   70,   70,  180,  133,    1,    2,
   22,   22,   70,  291,  106,   70,  294,  279,  106,   77,
   78,  193,   58,   55,   56,  285,  196,  286,  203,   22,
   49,   50,   51,  282,  206,  113,  143,   53,   54,  106,
   82,   83,   58,   55,   56,  204,   58,   55,   56,  205,
   57,  114,   61,   59,  226,   60,  125,   62,  126,   58,
   55,   56,   98,   35,  150,  100,   35,   58,   55,   56,
  106,  212,  131,   98,   98,   98,   98,  221,   61,   59,
  215,   60,  110,   62,  106,  110,  239,  171,  125,   91,
  126,   98,   98,  106,  111,  120,  121,  111,   58,   55,
   56,  132,  133,  245,   61,   59,  243,   60,  125,   62,
  126,  220,   58,   55,   56,  141,  142,  248,   49,   50,
   51,   58,   55,   56,   52,   53,   54,   61,   59,  257,
   60,  249,   62,   49,   50,   51,  250,  161,   57,   96,
   53,   54,  256,  258,   47,  188,  106,   58,   55,   56,
   49,   50,   51,   57,   47,   47,   97,   53,   54,  260,
   47,  261,   58,   55,   56,   49,   50,   51,  266,  274,
   57,  146,   53,   54,   58,   55,   56,   98,  287,   58,
   55,   56,   49,   50,   51,   57,  277,  113,  280,   53,
   54,   44,  190,  106,   58,   55,   56,   49,   50,   51,
  292,  270,   57,  218,   53,   54,   98,   98,  106,  293,
  299,   58,   55,   56,  300,   61,   59,   57,   60,    7,
   62,   58,   55,   56,   77,   46,   58,   55,   56,   45,
   61,   59,  194,   60,  143,   62,   58,   55,   56,   61,
  227,  228,   60,   78,   62,  142,   59,   61,   61,   60,
   60,  224,   62,  107,  107,  242,  107,    0,  107,   82,
    0,   82,   82,   82,    0,    0,   49,   50,   51,    0,
  107,    0,  269,   53,   54,    0,    0,   82,   82,    0,
   82,    0,    0,    0,    0,   87,   57,   87,   87,   87,
    0,    0,   49,   50,   51,   58,   55,   56,  288,   53,
   54,    0,    0,   87,   87,    0,   87,    0,  272,  273,
    0,   83,   57,   83,   83,   83,    0,    0,   49,   50,
   51,   58,   55,   56,  289,   53,   54,    0,    0,   83,
   83,    0,   83,   84,    0,   84,   84,   84,   57,  125,
    0,  126,  106,    0,    0,    0,    0,   58,   55,   56,
    0,   84,   84,    0,   84,    0,  168,    0,  167,    8,
    9,    0,    0,    0,   10,    0,    0,    0,   11,    9,
   58,   55,   56,   27,    0,   12,   13,   11,   63,   63,
    0,   14,   15,   63,   12,   13,    0,   63,   67,   67,
   14,   15,    0,   67,   63,   63,    0,   67,   65,   65,
   63,   63,    0,   65,   67,   67,    0,   65,    0,    0,
   67,   67,    0,    0,   65,   65,    0,   34,   34,    0,
   65,   65,   34,    0,    0,    0,   34,    0,    0,   49,
   50,   51,    0,   34,   34,  295,   53,   54,    0,   34,
   34,    0,    0,    0,   49,   50,   51,    0,    0,   57,
  296,   53,   54,  183,  106,    0,    0,    0,   58,   55,
   56,  185,  106,    0,   57,    0,    0,    0,  107,  108,
    0,    0,    0,   58,   55,   56,    0,    0,    0,    0,
    0,    0,   58,   55,   56,    0,   82,   82,   82,   82,
   58,   55,   56,    0,   43,   43,    0,  107,  107,   43,
    0,    0,    0,   43,    0,    0,    0,    0,    0,    0,
   43,   43,   87,   87,   87,   87,   43,   43,  151,  153,
    0,    0,    0,    0,    0,  108,    0,    0,    0,  173,
  174,    0,  108,  108,    0,    0,  182,    0,   83,   83,
   83,   83,    0,    0,    0,    0,    0,    0,  187,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
   84,   84,   84,   84,  163,  164,  165,  166,  200,   30,
   30,    0,    0,    0,   30,    0,    0,  207,   30,    0,
    9,    0,    0,    0,  229,   30,   30,  213,   11,    9,
    0,   30,   30,  275,  108,   12,   13,   11,  108,  108,
    0,   14,   15,    0,   12,   13,    0,    0,    0,    0,
   14,   15,    9,    0,    9,    0,  276,    0,  290,  240,
   11,    0,   11,    0,  153,    0,    0,   12,   13,   12,
   13,    0,    0,   14,   15,   14,   15,
};
}
static short yycheck[];
static { yycheck(); }
static void yycheck() {
yycheck = new short[] {                          6,
    7,    5,   31,   27,   28,   40,   59,   44,   15,   44,
   59,   44,   15,   57,   59,  257,   45,   46,   42,   40,
   40,   44,  257,  214,   59,   41,   59,   40,   57,   42,
   43,   34,   45,  257,   47,   41,   59,   51,   44,   59,
   43,   59,   41,   59,  286,  287,  105,   40,  283,   42,
   43,  257,   45,   42,   47,  246,   45,  281,   47,   41,
   59,  105,   44,   92,   40,  257,   42,   43,   41,   45,
   44,   47,   40,  266,   42,   43,  105,   45,   40,   47,
  125,  126,  256,  257,   41,   59,   59,   44,   40,  113,
   93,   40,  266,  107,   46,  261,   41,   42,   43,   44,
   45,  115,   47,  280,   41,   42,   43,   42,   45,   61,
   47,  170,   47,   41,   59,   60,   44,   62,   41,   42,
   43,   59,   45,   44,   47,  261,  170,   41,   42,   43,
  257,   45,  262,   47,  256,   59,   61,   62,   59,   91,
  144,  170,  272,  273,   41,  148,  175,   44,  278,  208,
  272,  273,   59,  212,   42,   43,   40,   45,   44,   47,
   40,  257,  176,   40,  208,   40,   46,  181,  212,   42,
   43,  257,   45,   59,   47,  267,  272,  273,   43,  208,
   45,   61,   41,  212,   45,   44,   42,   43,  195,   45,
   59,   47,  199,   41,   59,   43,   41,   45,   43,   59,
   45,   42,   43,  257,   45,  229,   47,  260,  257,  134,
  135,   91,   42,   43,   43,   45,   45,   47,   42,   43,
  284,   45,   41,   47,  257,   44,  255,  295,  296,   59,
  233,   45,   44,   42,   43,  257,   45,   40,   47,  259,
  260,  259,  260,  256,  257,  282,   45,  282,   41,  253,
  254,  275,  276,   40,  283,  284,  277,  277,  256,  257,
  267,  268,  291,  287,  257,  294,  290,  270,  257,  256,
  257,   40,  285,  286,  287,  278,   59,  281,   93,  286,
  256,  257,  258,   41,   40,  261,   44,  263,  264,  257,
  256,  257,  285,  286,  287,   93,  285,  286,  287,   93,
  276,  277,   42,   43,   41,   45,   43,   47,   45,  285,
  286,  287,  257,   41,   40,  267,   44,  285,  286,  287,
  257,   41,  257,  268,  269,  270,  271,   59,   42,   43,
   41,   45,   41,   47,  257,   44,   41,  274,   43,   41,
   45,  286,  287,  257,   41,  286,  287,   44,  285,  286,
  287,  286,  287,   59,   42,   43,   41,   45,   43,   47,
   45,   41,  285,  286,  287,  256,  257,   41,  256,  257,
  258,  285,  286,  287,  262,  263,  264,   42,   43,  257,
   45,   59,   47,  256,  257,  258,   59,  267,  276,  262,
  263,  264,   59,  257,  262,  256,  257,  285,  286,  287,
  256,  257,  258,  276,  272,  273,  262,  263,  264,   40,
  278,   40,  285,  286,  287,  256,  257,  258,   59,   59,
  276,  262,  263,  264,  285,  286,  287,  257,  261,  285,
  286,  287,  256,  257,  258,  276,   59,  261,   59,  263,
  264,  261,  256,  257,  285,  286,  287,  256,  257,  258,
   59,  282,  276,  262,  263,  264,  286,  287,  257,   59,
   59,  285,  286,  287,   59,   42,   43,  276,   45,    0,
   47,  285,  286,  287,   59,  261,  285,  286,  287,  261,
   42,   43,  143,   45,   59,   47,  285,  286,  287,   42,
  192,  193,   45,   59,   47,   59,   59,   42,   59,   59,
   45,  180,   47,   42,   43,  206,   45,   -1,   47,   41,
   -1,   43,   44,   45,   -1,   -1,  256,  257,  258,   -1,
   59,   -1,  262,  263,  264,   -1,   -1,   59,   60,   -1,
   62,   -1,   -1,   -1,   -1,   41,  276,   43,   44,   45,
   -1,   -1,  256,  257,  258,  285,  286,  287,  262,  263,
  264,   -1,   -1,   59,   60,   -1,   62,   -1,  260,  261,
   -1,   41,  276,   43,   44,   45,   -1,   -1,  256,  257,
  258,  285,  286,  287,  262,  263,  264,   -1,   -1,   59,
   60,   -1,   62,   41,   -1,   43,   44,   45,  276,   43,
   -1,   45,  257,   -1,   -1,   -1,   -1,  285,  286,  287,
   -1,   59,   60,   -1,   62,   -1,   60,   -1,   62,  256,
  257,   -1,   -1,   -1,  261,   -1,   -1,   -1,  265,  257,
  285,  286,  287,  261,   -1,  272,  273,  265,  256,  257,
   -1,  278,  279,  261,  272,  273,   -1,  265,  256,  257,
  278,  279,   -1,  261,  272,  273,   -1,  265,  256,  257,
  278,  279,   -1,  261,  272,  273,   -1,  265,   -1,   -1,
  278,  279,   -1,   -1,  272,  273,   -1,  256,  257,   -1,
  278,  279,  261,   -1,   -1,   -1,  265,   -1,   -1,  256,
  257,  258,   -1,  272,  273,  262,  263,  264,   -1,  278,
  279,   -1,   -1,   -1,  256,  257,  258,   -1,   -1,  276,
  262,  263,  264,  256,  257,   -1,   -1,   -1,  285,  286,
  287,  256,  257,   -1,  276,   -1,   -1,   -1,  257,   51,
   -1,   -1,   -1,  285,  286,  287,   -1,   -1,   -1,   -1,
   -1,   -1,  285,  286,  287,   -1,  268,  269,  270,  271,
  285,  286,  287,   -1,  256,  257,   -1,  286,  287,  261,
   -1,   -1,   -1,  265,   -1,   -1,   -1,   -1,   -1,   -1,
  272,  273,  268,  269,  270,  271,  278,  279,  100,  101,
   -1,   -1,   -1,   -1,   -1,  107,   -1,   -1,   -1,  111,
  112,   -1,  114,  115,   -1,   -1,  118,   -1,  268,  269,
  270,  271,   -1,   -1,   -1,   -1,   -1,   -1,  130,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
  268,  269,  270,  271,  268,  269,  270,  271,  150,  256,
  257,   -1,   -1,   -1,  261,   -1,   -1,  159,  265,   -1,
  257,   -1,   -1,   -1,  261,  272,  273,  169,  265,  257,
   -1,  278,  279,  261,  176,  272,  273,  265,  180,  181,
   -1,  278,  279,   -1,  272,  273,   -1,   -1,   -1,   -1,
  278,  279,  257,   -1,  257,   -1,  261,   -1,  261,  201,
  265,   -1,  265,   -1,  206,   -1,   -1,  272,  273,  272,
  273,   -1,   -1,  278,  279,  278,  279,
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

//#line 828 "gram.y"


/* CODIGO DE SOPORTE                                                         */

static Parser parser;

static int cant_errores = 0;
static int cant_errores_semanticos = 0;
static int cant_retornos_auto = -1;

public static String ambitoActual = ":main";

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
//#line 718 "Parser.java"
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
{
            int ref = val_peek(0).ival;
            String nombre = TablaSimbolos.obtenerAtributo(ref, TablaSimbolos.LEXEMA);
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
            int ref = val_peek(0).ival;
            String lexema = TablaSimbolos.obtenerAtributo(ref, TablaSimbolos.LEXEMA);
            lista.add(lexema);
            ParserVal pv = new ParserVal();
            pv.obj = lista;
            yyval = pv;
        }
break;
case 18:
//#line 127 "gram.y"
{
            ArrayList<String> lista = (ArrayList<String>) val_peek(2).obj;
            int ref = val_peek(0).ival;
            String lexema = TablaSimbolos.obtenerAtributo(ref, TablaSimbolos.LEXEMA);
            lista.add(lexema);
            yyval = val_peek(2);
        }
break;
case 19:
//#line 134 "gram.y"
{ yyerror("Falta ',' entre los identificadores"); yyval = val_peek(1); }
break;
case 20:
//#line 139 "gram.y"
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
//#line 149 "gram.y"
{
            ArrayList<String> lista = (ArrayList<String>) val_peek(2).obj;
            int ref = val_peek(0).ival;
            String lexema = TablaSimbolos.obtenerAtributo(ref, TablaSimbolos.LEXEMA);
            lista.add(lexema);
            yyval = val_peek(2);
        }
break;
case 22:
//#line 159 "gram.y"
{ yyval = new ParserVal("SHORTINT"); }
break;
case 23:
//#line 160 "gram.y"
{ yyval = new ParserVal("SINGLEF"); }
break;
case 24:
//#line 167 "gram.y"
{
            int ref = val_peek(0).ival;
            String nombre = TablaSimbolos.obtenerAtributo(ref, TablaSimbolos.LEXEMA);
            TablaSimbolos.declararVariable(nombre, val_peek(2).sval, "funcion", ambitoActual);
            entrarAmbito(nombre);
            yyval = val_peek(0);
        }
break;
case 25:
//#line 178 "gram.y"
{
            int ref = val_peek(0).ival;
            String nombre = TablaSimbolos.obtenerAtributo(ref, TablaSimbolos.LEXEMA);
            TablaSimbolos.declararVariable(nombre, "auto", "funcion", ambitoActual);
            entrarAmbito(nombre);
            yyval = val_peek(0);
        }
break;
case 26:
//#line 189 "gram.y"
{ cant_retornos_auto = 0; }
break;
case 27:
//#line 196 "gram.y"
{
            salirAmbito();
        }
break;
case 28:
//#line 199 "gram.y"
{ yyerror("Falta nombre de funcion"); }
break;
case 30:
//#line 205 "gram.y"
{
            yyerror("Falta ';' al final de la declaracion de funcion");
            salirAmbito();
        }
break;
case 31:
//#line 210 "gram.y"
{
            if (cant_retornos_auto == 0) {
                yyerror("Ausencia de sentencia de retorno 'RET' en funcion AUTO");
            }
            cant_retornos_auto = -1;
            salirAmbito();
        }
break;
case 32:
//#line 217 "gram.y"
{ yyerror("Falta nombre de funcion"); }
break;
case 34:
//#line 221 "gram.y"
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
//#line 239 "gram.y"
{
            int ref = val_peek(0).ival;
            String nombre = TablaSimbolos.obtenerAtributo(ref, TablaSimbolos.LEXEMA);
            TablaSimbolos.declararVariable(nombre, val_peek(1).sval, "parametro", ambitoActual);
            imprimirRegla("parametro_formal");
        }
break;
case 39:
//#line 245 "gram.y"
{ yyerror("Falta de nombre de parametro formal en declaracion de funcion"); }
break;
case 40:
//#line 246 "gram.y"
{ yyerror("Falta de tipo del parametro formal en declaracion de funcion"); }
break;
case 41:
//#line 254 "gram.y"
{
            int ref = val_peek(0).ival;
            String nombre = TablaSimbolos.obtenerAtributo(ref, TablaSimbolos.LEXEMA);
            TablaSimbolos.declararVariable(nombre, "clase", "nombre_clase", ambitoActual);
            entrarAmbito(nombre);
            yyval = val_peek(0);
        }
break;
case 42:
//#line 266 "gram.y"
{
            salirAmbito();
        }
break;
case 43:
//#line 271 "gram.y"
{
            yyerror("Falta ';' al final de la declaracion de clase");
            salirAmbito();
        }
break;
case 45:
//#line 279 "gram.y"
{ imprimirRegla("importacion_opcional"); }
break;
case 46:
//#line 281 "gram.y"
{ yyerror("Falta la palabra clave 'FROM' en la declaracion IMPORT"); }
break;
case 48:
//#line 286 "gram.y"
{ imprimirRegla("herencia_opcional"); }
break;
case 49:
//#line 288 "gram.y"
{ yyerror("Falta nombre o lista de clases a heredar luego de 'EXTENDS'"); }
break;
case 54:
//#line 303 "gram.y"
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
//#line 319 "gram.y"
{
            salirAmbito();
        }
break;
case 56:
//#line 324 "gram.y"
{
            salirAmbito();
        }
break;
case 57:
//#line 331 "gram.y"
{
            int ref = val_peek(0).ival;
            String nombre = TablaSimbolos.obtenerAtributo(ref, TablaSimbolos.LEXEMA);
            TablaSimbolos.declararVariable(nombre, val_peek(1).sval, "metodo", ambitoActual);
            entrarAmbito(nombre);
            yyval = val_peek(0);
        }
break;
case 58:
//#line 342 "gram.y"
{
            int ref = val_peek(0).ival;
            String nombre = TablaSimbolos.obtenerAtributo(ref, TablaSimbolos.LEXEMA);
            TablaSimbolos.declararVariable(nombre, "auto", "metodo", ambitoActual);
            entrarAmbito(nombre);
            yyval = val_peek(0);
        }
break;
case 60:
//#line 353 "gram.y"
{ imprimirRegla("exportacion_opcional"); }
break;
case 61:
//#line 355 "gram.y"
{ yyerror("Falta la palabra clave 'TO' en la declaracion EXPORT"); }
break;
case 62:
//#line 361 "gram.y"
{ imprimirRegla("declaracion_de_objeto"); }
break;
case 63:
//#line 363 "gram.y"
{ yyerror("Falta ';' al final de la declaracion de objeto"); }
break;
case 64:
//#line 370 "gram.y"
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
//#line 384 "gram.y"
{ yyerror("Falta ';' al final de la declaracion comptime"); }
break;
case 66:
//#line 386 "gram.y"
{ yyerror("Falta el tipo de dato en la declaracion comptime"); }
break;
case 67:
//#line 388 "gram.y"
{
            yyerror("Falta el tipo de dato en la declaracion comptime");
            yyerror("Falta ';' al final de la declaracion comptime");
        }
break;
case 69:
//#line 398 "gram.y"
{ imprimirRegla("sentencia_ejecutable"); }
break;
case 76:
//#line 409 "gram.y"
{ yyerror("Sentencia ejecutable malformada o falta ';' previo"); }
break;
case 77:
//#line 414 "gram.y"
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
//#line 446 "gram.y"
{ yyerror("Acceso posicional invalido: solo se permite constante SHORTINT positiva o identificador"); yyval = new ParserVal(); }
break;
case 82:
//#line 452 "gram.y"
{
            yyval = val_peek(0);
        }
break;
case 83:
//#line 456 "gram.y"
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
//#line 478 "gram.y"
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
//#line 499 "gram.y"
{ yyerror("Falta un operando en la expresion"); yyval = new ParserVal(); }
break;
case 86:
//#line 500 "gram.y"
{ yyerror("Falta un operando en la expresion"); yyval = new ParserVal(); }
break;
case 87:
//#line 501 "gram.y"
{ yyerror("Falta un operando en la expresion"); yyval = new ParserVal(); }
break;
case 88:
//#line 505 "gram.y"
{
            yyval = val_peek(0);
        }
break;
case 89:
//#line 509 "gram.y"
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
//#line 531 "gram.y"
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
//#line 552 "gram.y"
{ yyerror("Falta un operando en la expresion"); yyval = new ParserVal(); }
break;
case 92:
//#line 553 "gram.y"
{ yyerror("Falta un operando en la expresion"); yyval = new ParserVal(); }
break;
case 93:
//#line 554 "gram.y"
{ yyerror("Falta un operando en la expresion"); yyval = new ParserVal(); }
break;
case 94:
//#line 555 "gram.y"
{ yyerror("Falta un operando en la expresion"); yyval = new ParserVal(); }
break;
case 95:
//#line 556 "gram.y"
{ yyerror("Falta operador en la expresion"); yyval = new ParserVal(); }
break;
case 96:
//#line 557 "gram.y"
{ yyerror("Falta operador en la expresion"); yyval = new ParserVal(); }
break;
case 97:
//#line 558 "gram.y"
{ yyerror("Falta operador en la expresion"); yyval = new ParserVal(); }
break;
case 98:
//#line 563 "gram.y"
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
//#line 580 "gram.y"
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
//#line 594 "gram.y"
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
//#line 611 "gram.y"
{
            yyval = val_peek(0);
        }
break;
case 105:
//#line 615 "gram.y"
{
            yyval = val_peek(0);
        }
break;
case 106:
//#line 619 "gram.y"
{
            yyval = val_peek(0);
        }
break;
case 108:
//#line 626 "gram.y"
{ imprimirRegla("invocacion_metodo"); }
break;
case 109:
//#line 627 "gram.y"
{ imprimirRegla("invocacion_metodo"); }
break;
case 112:
//#line 638 "gram.y"
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
//#line 675 "gram.y"
{
            yyerror("Uso del simbolo ':=' donde debe usarse '=' en asignacion dentro de expresion");
            yyval = new ParserVal();
        }
break;
case 114:
//#line 683 "gram.y"
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
//#line 694 "gram.y"
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
//#line 709 "gram.y"
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
case 118:
//#line 733 "gram.y"
{ yyerror("Falta '(' de apertura en condicion de seleccion"); }
break;
case 119:
//#line 735 "gram.y"
{ yyerror("Condicion de seleccion malformada o error en parentesis"); }
break;
case 122:
//#line 742 "gram.y"
{ yyerror("Falta palabra clave END_IF al final de la sentencia IF"); }
break;
case 123:
//#line 744 "gram.y"
{ yyerror("Falta palabra clave END_IF al final de la sentencia IF"); }
break;
case 124:
//#line 749 "gram.y"
{
            if (val_peek(2) != null && val_peek(2).sval != null && val_peek(0) != null && val_peek(0).sval != null) {
                String tipo1 = (String) val_peek(2).obj;
                String tipo2 = (String) val_peek(0).obj;
                if (tipo1 != null && tipo2 != null && !tipo1.equals(tipo2)) {
                    agregarErrorSemantico("Incompatibilidad de tipos en comparacion: no se puede comparar " + tipo1 + " con " + tipo2 + " (requiere conversion explicita 'tos')");
                }
            }
        }
break;
case 133:
//#line 773 "gram.y"
{ yyerror("Falta el delimitador END en el bloque de sentencias"); }
break;
case 135:
//#line 780 "gram.y"
{ yyerror("Falta el cuerpo de la iteracion REPEAT"); }
break;
case 136:
//#line 782 "gram.y"
{ yyerror("Falta palabra clave UNTIL en la iteracion REPEAT"); }
break;
case 137:
//#line 784 "gram.y"
{ yyerror("Falta palabra clave UNTIL y cuerpo en la iteracion REPEAT"); }
break;
case 138:
//#line 786 "gram.y"
{ yyerror("Falta palabra clave UNTIL y ')' en la iteracion REPEAT"); }
break;
case 139:
//#line 788 "gram.y"
{ yyerror("Falta palabra clave UNTIL, cuerpo y ')' en la iteracion REPEAT"); }
break;
case 141:
//#line 794 "gram.y"
{ yyerror("Falta '(' de apertura en condicion de iteracion"); }
break;
case 142:
//#line 796 "gram.y"
{ yyerror("Falta ')' de cierre en condicion de iteracion"); }
break;
case 143:
//#line 798 "gram.y"
{ yyerror("Faltan parentesis en condicion de iteracion"); }
break;
case 144:
//#line 804 "gram.y"
{
            int ref = val_peek(1).ival;
            String lexema = TablaSimbolos.obtenerAtributo(ref, TablaSimbolos.LEXEMA);
            GeneradorTerceto.crearTerceto("POUT", lexema, "-");
        }
break;
case 145:
//#line 810 "gram.y"
{
            if (val_peek(1) != null && val_peek(1).sval != null) {
                GeneradorTerceto.crearTerceto("POUT", val_peek(1).sval, "-", (String) val_peek(1).obj);
            }
        }
break;
case 146:
//#line 816 "gram.y"
{ yyerror("Falta argumento en sentencia pout"); }
break;
case 147:
//#line 821 "gram.y"
{
            if (cant_retornos_auto >= 0) {
                cant_retornos_auto++;
            }
        }
break;
//#line 1670 "Parser.java"
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
