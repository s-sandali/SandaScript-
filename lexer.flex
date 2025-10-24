/* lexer.flex - JFlex for TestLang++ */
package translator;

import java_cup.runtime.Symbol;

%%

%class Lexer
%public
%cup
%unicode
%line
%column

%{
  // helper to create Symbol with position
  private Symbol sym(int type) { return new Symbol(type, yyline+1, yycolumn+1); }
  private Symbol sym(int type, Object val) { return new Symbol(type, yyline+1, yycolumn+1, val); }

  // convert a string lexeme (including leading and trailing ") to Java unescaped value
  private String unquote(String s) {
    if (s == null || s.length() < 2) return "";
    String inner = s.substring(1, s.length()-1);
    // handle simple escapes: \" and \\ and \/
    StringBuilder sb = new StringBuilder();
    for (int i=0;i<inner.length();i++) {
      char c = inner.charAt(i);
      if (c == '\\' && i+1 < inner.length()) {
        char n = inner.charAt(i+1);
        if (n == '"' || n == '\\' || n == '/') { sb.append(n); i++; continue; }
        if (n == 'n') { sb.append('\n'); i++; continue; }
        if (n == 't') { sb.append('\t'); i++; continue; }
        // otherwise keep literal
      }
      sb.append(c);
    }
    return sb.toString();
  }
%}

/* Definitions */
WHITESPACE = [ \t\r\n]+
LINECOMMENT = "//" [^\n]*

IDENT = [A-Za-z_][A-Za-z0-9_]*
NUMBER = [0-9]+
STRING = \"([^\"\\\n]|\\[\"\\/bfnrt])*\"

%%

{WHITESPACE}      { /* skip */ }
{LINECOMMENT}     { /* skip */ }

// reserved keywords (case-sensitive)
"config"          { return sym(sym.CONFIG); }
"base_url"        { return sym(sym.BASE_URL); }    // treated as identifier in grammar; we provide token
"header"          { return sym(sym.HEADER); }
"let"             { return sym(sym.LET); }
"test"            { return sym(sym.TEST); }

"GET"             { return sym(sym.GET); }
"POST"            { return sym(sym.POST); }
"PUT"             { return sym(sym.PUT); }
"DELETE"          { return sym(sym.DELETE); }

"expect"          { return sym(sym.EXPECT); }
"status"          { return sym(sym.STATUS); }
"body"            { return sym(sym.BODY); }
"contains"        { return sym(sym.CONTAINS); }
"in"              { return sym(sym.IN); } // optional bonus

"="               { return sym(sym.EQ); }
"{"               { return sym(sym.LBRACE); }
"}"               { return sym(sym.RBRACE); }
";"               { return sym(sym.SEMI); }
// removed: "(" and ")" tokens (unused)
// removed: "/" token (paths are inside STRING)
".."              { return sym(sym.DOTDOT); }

{STRING}          { return sym(sym.STRING, unquote(yytext())); }
{NUMBER}          { return sym(sym.NUMBER, Integer.parseInt(yytext())); }
{IDENT}           { return sym(sym.IDENT, yytext()); }

// any other single char: helpful tokens for paths that may include '/'
// removed SLASH rule

.                 { System.err.println("Lexer error at " + (yyline+1) + ":" + (yycolumn+1) + " unknown char: " + yytext()); }