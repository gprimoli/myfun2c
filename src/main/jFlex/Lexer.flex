//UserCode
import it.util.LexicalAnalysis.Exception.LexicalException;
import java_cup.runtime.*;
import it.*;

%%
//Options and declarations
//%debug
%public
%class Lexer
%line
%column
%unicode
%cup

LineTerminator = \r | \n | \r\n
InputCharacter = [^\r\n]
WhiteSpace     = [ \t\f] | {LineTerminator}

Sign           = ("+" | "-")
Digit          = [0-9]
Int            = {Sign}? (0 | [1-9]{Digit}*)
Real           = {Int} (\.{Digit}+)
Character      = [a-zA-Z]
Id             = ({Character} | [$_])  ({Digit} | {Character} | [$_])*
NotEqual       = ("!=" | "<>")

//Comment = {TraditionalComment} | {EndOfLineComment}
//TraditionalComment   = "#*" [^#] ~ "#" | "#*"  "#"
//EndOfLineComment     = "#" {InputCharacter}* {LineTerminator}

%state STRINGSINGLE
%state STRINGDOUBLE
%state COMMENTSINGLELINE
%state COMMENTMULTIPLELINE

%{
    private int line = 0, column = 0;
    private StringBuilder string = new StringBuilder();

    private void savePosition(int l, int c){ line = l; column = c; }
    private String resetString(){
        String tmp = string.toString();
        string.setLength(0);
        return tmp;
    }

    private Symbol symbol(int type) { return new Symbol(type, yyline, yycolumn); }
    private Symbol symbol(int type, Object value) { return new Symbol(type, yyline, yycolumn, value); }
%}

%%
// Lexical rules
<YYINITIAL> {
        "and"           { return symbol(sym.AND); }
        "bool"          { return symbol(sym.BOOL); }
        "div"           { return symbol(sym.DIVINT); }
        "else"          { return symbol(sym.ELSE); }
        "end"           { return symbol(sym.END); }
        "false"         { return symbol(sym.BOOL_CONST, "FALSE"); }
        "fun"           { return symbol(sym.FUN); }
        "if"            { return symbol(sym.IF); }
        "integer"       { return symbol(sym.INTEGER); }
        "loop"          { return symbol(sym.LOOP); }
        "main"          { return symbol(sym.MAIN); }
        "not"           { return symbol(sym.NOT); }
        "null"          { return symbol(sym.NULL); }
        "or"            { return symbol(sym.OR); }
        "out"           { return symbol(sym.OUT); }
        "real"          { return symbol(sym.REAL); }
        "return"        { return symbol(sym.RETURN); }
        "string"        { return symbol(sym.STRING); }
        "then"          { return symbol(sym.THEN); }
        "true"          { return symbol(sym.BOOL_CONST, "TRUE"); }
        "var"           { return symbol(sym.VAR); }
        "while"         { return symbol(sym.WHILE); }

        {Id}            { return symbol(sym.ID, yytext()); }
        {Int}           { return symbol(sym.INTEGER_CONST, yytext()); }
        {Real}          { return symbol(sym.REAL_CONST, yytext()); }

        ":="            { return symbol(sym.ASSIGN); }
        "?."            { return symbol(sym.WRITELN); }
        "?,"            { return symbol(sym.WRITEB); }
        "?:"            { return symbol(sym.WRITET); }
        "<="            { return symbol(sym.LE); }
        ">="            { return symbol(sym.GE); }
        {NotEqual}      { return symbol(sym.NE); }

        "("             { return symbol(sym.LPAR); }
        ")"             { return symbol(sym.RPAR); }
        ":"             { return symbol(sym.COLON); }
        "?"             { return symbol(sym.WRITE); }
        "+"             { return symbol(sym.PLUS); }
        "-"             { return symbol(sym.MINUS); }
        "*"             { return symbol(sym.TIMES); }
        "/"             { return symbol(sym.DIV); }
        "^"             { return symbol(sym.POW); }
        "&"             { return symbol(sym.STR_CONCAT); }
        "%"             { return symbol(sym.READ); }
        "="             { return symbol(sym.EQ); }
        \"              { savePosition(yyline, yycolumn); yybegin(STRINGDOUBLE); }
        \'              { savePosition(yyline, yycolumn); yybegin(STRINGSINGLE); }
        ";"             { return symbol(sym.SEMI); }
        ","             { return symbol(sym.COMMA); }
        "@"             { return symbol(sym.OUTPAR); }
        "<"             { return symbol(sym.LT); }
        ">"             { return symbol(sym.GT); }

        #               { savePosition(yyline, yycolumn); yybegin(COMMENTSINGLELINE); }
        "#*"            { savePosition(yyline, yycolumn); yybegin(COMMENTMULTIPLELINE); }
        {WhiteSpace}    { /* ignora spazzibianchi */ }
}

 <COMMENTSINGLELINE, COMMENTMULTIPLELINE> <<EOF>> {throw new LexicalException("Commento non completo", line, column);}
 <COMMENTSINGLELINE> {
      {LineTerminator}               { yybegin(YYINITIAL); }
      {InputCharacter}*              {/* ignora */}
 }
 <COMMENTMULTIPLELINE> {
      #                              { yybegin(YYINITIAL); }
      [^#]                           {/* ignora */}
 }

 <STRINGSINGLE, STRINGDOUBLE> <<EOF>> {throw new LexicalException("Stringa non completa", line, column);}
 <STRINGDOUBLE> {
      \"                             { yybegin(YYINITIAL); return symbol(sym.STRING_CONST, resetString()); }
      [^\n\r\"\\]*                   { string.append( yytext() ); }
      \\t                            { string.append('\t'); }
      \\n                            { string.append('\n'); }
      \\r                            { string.append('\r'); }
      \\\"                           { string.append('\"'); }
      \\                             { string.append('\\'); }
      \n                             {/* ignora */}
      \r                             {/* ignora */}
      \t                             {/* ignora */}
 }
 <STRINGSINGLE> {
      \'                             { yybegin(YYINITIAL); return symbol(sym.STRING_CONST, resetString()); }
      [^\n\r\'\\]*                   { string.append( yytext() ); }
      \\t                            { string.append('\t'); }
      \\n                            { string.append('\n'); }
      \\r                            { string.append('\r'); }
      \\\"                           { string.append('\"'); }
      \\                             { string.append('\\'); }
      \n                             {/* ignora */}
      \r                             {/* ignora */}
      \t                             {/* ignora */}
 }

[^] {throw new LexicalException("Errore carattere non riconosciuto " + yytext(), yyline, yycolumn);}
