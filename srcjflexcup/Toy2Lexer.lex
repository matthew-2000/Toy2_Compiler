import java_cup.runtime.*;
import unisa.compilatori.sym;

%%
%class Toy2Lexer
%unicode
%line
%column
%cupsym sym
%cup

/* Definizioni di pattern */
RelOp = ( < ( = | > ) ? ) | ( > ( = ) ? ) | =
Identifier = [A-Za-z_][A-Za-z0-9_]*
DecIntegerLiteral = 0 | [1-9][0-9]*
Number = {DecIntegerLiteral} (\.[0-9]+)? (E[+-]?[0-9]+)?
LineTerminator = \r\n|\r|\n
WhiteSpace = {LineTerminator} | [ \t\f]

%{
    private Symbol symbol(int type){
        return new Symbol(type, yyline + 1, yycolumn + 1);
    }

    private Symbol symbol(int type, Object o){
        return new Symbol(type, yyline + 1, yycolumn + 1, o);
    }

    private Symbol errorSymbol(String message){
        String errorMsg = "Error at line " + (yyline + 1) + ", column " + (yycolumn + 1) + ": " + message;
        return new Symbol(sym.EOF, yyline + 1, yycolumn + 1, errorMsg);
    }
%}

/* Stati per la gestione delle stringhe e dei commenti */
%x STRING COMMENT

%%

/* Parole chiave e simboli del linguaggio Toy2 */
<YYINITIAL> "var"         { return symbol(sym.VAR); }
<YYINITIAL> "proc"        { return symbol(sym.PROC); }
<YYINITIAL> "endproc"     { return symbol(sym.ENDPROC); }
<YYINITIAL> "func"        { return symbol(sym.FUNC); }
<YYINITIAL> "endfunc"     { return symbol(sym.ENDFUNC); }
<YYINITIAL> "if"          { return symbol(sym.IF); }
<YYINITIAL> "then"        { return symbol(sym.THEN); }
<YYINITIAL> "else"        { return symbol(sym.ELSE); }
<YYINITIAL> "elseif"      { return symbol(sym.ELIF); }
<YYINITIAL> "endif"       { return symbol(sym.ENDIF); }
<YYINITIAL> "while"       { return symbol(sym.WHILE); }
<YYINITIAL> "do"          { return symbol(sym.DO); }
<YYINITIAL> "endwhile"    { return symbol(sym.ENDWHILE); }
<YYINITIAL> "true"        { return symbol(sym.TRUE); }
<YYINITIAL> "false"       { return symbol(sym.FALSE); }
<YYINITIAL> "return"      { return symbol(sym.RETURN); }
<YYINITIAL> "-->"         { return symbol(sym.WRITE); }
<YYINITIAL> "-->!"        { return symbol(sym.WRITERETURN); }
<YYINITIAL> "<--"         { return symbol(sym.READ); }
<YYINITIAL> "real"        { return symbol(sym.REAL); }
<YYINITIAL> "integer"     { return symbol(sym.INTEGER); }
<YYINITIAL> "string"      { return symbol(sym.STRING); }
<YYINITIAL> ":"           { return symbol(sym.COLON); }
<YYINITIAL> ";"           { return symbol(sym.SEMI); }
<YYINITIAL> ","           { return symbol(sym.COMMA); }
<YYINITIAL> "@"           { return symbol(sym.REF); }
<YYINITIAL> "+"           { return symbol(sym.PLUS); }
<YYINITIAL> "-"           { return symbol(sym.MINUS); }
<YYINITIAL> "*"           { return symbol(sym.TIMES); }
<YYINITIAL> "/"           { return symbol(sym.DIV); }
<YYINITIAL> "="           { return symbol(sym.EQ); }
<YYINITIAL> "<>"          { return symbol(sym.NE); }
<YYINITIAL> "<"           { return symbol(sym.LT); }
<YYINITIAL> "<="          { return symbol(sym.LE); }
<YYINITIAL> ">"           { return symbol(sym.GT); }
<YYINITIAL> ">="          { return symbol(sym.GE); }
<YYINITIAL> "&&"          { return symbol(sym.AND); }
<YYINITIAL> "||"          { return symbol(sym.OR); }
<YYINITIAL> "!"           { return symbol(sym.NOT); }
<YYINITIAL> "^="          { return symbol(sym.ASSIGN); }  /* Operatore di assegnazione */
<YYINITIAL> "\\"          { return symbol(sym.ENDVAR); }  /* Simbolo di chiusura var */
<YYINITIAL> "boolean"     { return symbol(sym.BOOLEAN); }
<YYINITIAL> "->"          { return symbol(sym.TYPERETURN); }
<YYINITIAL> "out"         { return symbol(sym.OUT); }
<YYINITIAL> "-"           { return symbol(sym.UMINUS); }  // Se necessario per UMINUS

/* Identificatori, numeri e operatori */
<YYINITIAL> {
    {RelOp}         { return symbol(sym.RELOP, yytext()); }
    {Identifier}    { return symbol(sym.IDENTIFIER, yytext()); }
    {Number}        { return symbol(sym.NUMBER_LITERAL, yytext()); }
    "$"             { return symbol(sym.DOLLAR); }  /* Simbolo di concatenazione stringhe */
    "("             { return symbol(sym.LPAR); }
    ")"             { return symbol(sym.RPAR); }
    {WhiteSpace}    { /* Ignora spazi bianchi e linee vuote */ }
}

/* Gestione dei commenti: i commenti iniziano e finiscono con % */
<YYINITIAL> "%"           { yybegin(COMMENT); }
<COMMENT>  [^%]*          { /* Ignora tutto il contenuto del commento */ }
<COMMENT>  "%"            { yybegin(YYINITIAL); }

/* Gestione delle stringhe */
<YYINITIAL> \"            { yybegin(STRING); }
<STRING>   [^\"]+         { /* Accetta il contenuto della stringa */ }
<STRING>   \"             { yybegin(YYINITIAL); return symbol(sym.STRING_LITERAL, yytext()); }
<STRING>   \n             { throw new Error("Stringa costante non completata"); }

/* Gestione EOF*/
<<EOF>>  { return symbol(sym.EOF); }

/* Gestione errori */
.        { return errorSymbol("Invalid character or sequence: " + yytext()); }
