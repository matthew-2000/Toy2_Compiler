public class sym {
    /* Parole chiave */
    public static final int VAR = 1;
    public static final int PROC = 2;
    public static final int ENDPROC = 3;
    public static final int FUNC = 4;
    public static final int ENDFUNC = 5;
    public static final int IF = 6;
    public static final int THEN = 7;
    public static final int ELSE = 8;
    public static final int ELIF = 9;
    public static final int ENDIF = 10;
    public static final int WHILE = 11;
    public static final int DO = 12;
    public static final int ENDWHILE = 13;
    public static final int TRUE = 14;
    public static final int FALSE = 15;
    public static final int RETURN = 16;
    public static final int WRITE = 17;
    public static final int WRITERETURN = 18;
    public static final int READ = 19;
    public static final int REAL = 20;
    public static final int INTEGER = 21;
    public static final int STRING = 22;

    /* Operatori */
    public static final int PLUS = 23;
    public static final int MINUS = 24;
    public static final int TIMES = 25;
    public static final int DIV = 26;
    public static final int EQ = 27;
    public static final int NE = 28;
    public static final int LT = 29;
    public static final int LE = 30;
    public static final int GT = 31;
    public static final int GE = 32;
    public static final int AND = 33;
    public static final int OR = 34;
    public static final int NOT = 35;
    public static final int ASSIGN = 36;  // ^=

    /* Simboli */
    public static final int COLON = 37;
    public static final int SEMI = 38;
    public static final int COMMA = 39;
    public static final int LPAR = 40;      // (
    public static final int RPAR = 41;      // )
    public static final int LBRACE = 42;    // {
    public static final int RBRACE = 43;    // }
    public static final int REF = 44;       // @
    public static final int DOLLAR = 45;    // $
    public static final int ENDVAR = 46;    // \

    /* Costanti */
    public static final int IDENTIFIER = 47;
    public static final int NUMBER_LITERAL = 48;
    public static final int STRING_LITERAL = 49;

    /* Fine del file */
    public static final int EOF = 0;

    /* Errore */
    public static final int ERROR = -1;
}
