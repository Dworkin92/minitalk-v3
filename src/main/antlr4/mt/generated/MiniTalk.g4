grammar MiniTalk;

//
// Programme : une suite de points, separees par des points.
// Le point final est optionnel.
//

program
    : (statement ('.' statement)* '.'?)? EOF
    ;

statement
    : expression
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

binaryExpression
    : unaryExpression (BINARY unaryExpression)*
    ;

unaryExpression
    : primary (IDENTIFIER)*
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
    : LBRACK blockParameters? statements? RBRACK
    ;

blockParameters
    : (COLON IDENTIFIER)+ BAR
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

INTEGER  : [0-9]+ ;

STRING   : '\'' ('\'' '\'' | ~'\'')* '\'' ;

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
