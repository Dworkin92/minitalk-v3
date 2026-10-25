grammar MiniTalk;

//
// Programme : une suite de points, separees par des points.
// Le point final est optionnel.
//
// Warning : ANTLR n'a pas d'ordre priviliégé pour la prise en
//           compte des règles d'analyse syntaxique !

program
    : (statement ('.' statement)* '.'?)? EOF
    ;

statement
    : IDENTIFIER ASSIGN expression
    | expression
    ;

//
// Precedence Smalltalk :
//   unaire   >  binaire  >  mot-cle
// "3 factorial + 2 max: 5" se lit
//   ((3 factorial) + 2) max: 5
//

expression
    : keywordExpression
    ;

keywordExpression
    : binaryExpression (IDENTIFIER COLON binaryExpression)*
    ;

//
// Precedence AOS (ARCHITECTURE.md) :
//   **  >  * / %  >  + -  >  autres binaires
// Les autres binaires (, ~= < ...) restent au niveau
// le plus bas, associatifs a gauche.
//

binaryExpression
    : additiveExpression
    ;

additiveExpression
    : multiplicativeExpression
      ((PLUS | MINUS | BINARY) multiplicativeExpression)*
    ;

multiplicativeExpression
    : powerExpression
      ((STAR | SLASH | PERCENT) powerExpression)*
    ;

unaryExpression
    : primary (IDENTIFIER)*
    ;

// Signe unaire : le plus haut niveau (ARCHITECTURE.md).
// -6, +5.34, --6 (double negation), 3 * -4
// Signe unaire : le plus haut niveau (ARCHITECTURE.md).
// -6, +5.34, 3 * -4
//
// Un SEUL signe : --3 est refuse, ecrire -(-3).
signExpression
    : (MINUS | PLUS)? unaryExpression
    ;

// Droite associatif : 2 ** 3 ** 2 = 2 ** (3 ** 2)
powerExpression
    : signExpression (POWER powerExpression)?
    ;

primary
    : literal
    | block
    | identifier
    | '(' expression ')'
    ;

identifier
    : IDENTIFIER
    ;

//
// Blocs : [ :x :y | corps ]
// Parametres et corps optionnels : [] et [42] sont valides.
//

block
    : LBRACK blockParameters? blockLocals? statements? RBRACK
    ;

blockParameters
    : (COLON IDENTIFIER)+ BAR
    ;

blockLocals
    : BAR IDENTIFIER+ BAR
    ;

statements
    : statement ('.' statement)* '.'?
    ;

//
// Litteraux
//

literal
    : INTEGER
    | STRING
    | NIL
    | TRUE
    | FALSE
    ;

//
// Analyse lexicale
//
// Warning : ANTLR prend en compte l'ordre des règle d'analyse
//           lexicales. Ces règles sont toutes en majuscule
//

INTEGER  : [0-9]+ ;

STRING   : '\'' ('\'' '\'' | ~'\'')* '\'' ;

ASSIGN   : '<-' | ':=' ;
POWER    : '**' ;
PLUS     : '+' ;
MINUS    : '-' ;
STAR     : '*' ;
SLASH    : '/' ;
PERCENT  : '%' ;
BINARY   : [+\-*/=<>%~,&]+ ;

COLON    : ':' ;
BAR      : '|' ;
DOT      : '.' ;
LBRACK   : '[' ;
RBRACK   : ']' ;
LPAREN   : '(' ;
RPAREN   : ')' ;

NIL      : 'nil' ;
TRUE     : 'true' ;
FALSE    : 'false' ;

IDENTIFIER : [a-zA-Z_][a-zA-Z0-9_]* ;

COMMENT  : '"' ~'"'* '"' -> skip ;

WS       : [ \t\r\n]+ -> skip ;
