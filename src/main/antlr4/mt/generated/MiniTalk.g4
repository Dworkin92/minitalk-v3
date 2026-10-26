grammar MiniTalk;

//
// Programme : une suite de points, separees par des points.
// Le point final est optionnel.
//
// Warning : ANTLR n'a pas d'ordre priviliégié pour la prise en
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
    | arrayLiteral
    | symbolLiteral
    | block
    | identifier
    | '(' expression ')'
    ;

identifier
    : IDENTIFIER
    ;

//
// Tableaux : #(1 2 3)
//
// Divergence assumee avec Smalltalk : les elements sont
// evalues comme des VARIABLES, pas comme des symboles.
// Un tableau de symboles s'ecrit #(#a #b #c).
//
// Un element = litteral, variable, symbole ou array imbrique.
// RIEN d'autre : pas d'expression (pas de #(1 + 2)), pas de
// parenthesage force. Decision du 25/10 : pas de porte
// ouverte aux expressions imbriquees illisibles.
//
// #( ) n'est PAS une constante de compilation : le tableau
// est reconstruit a chaque evaluation (variables lues au
// moment de traiter l'array).
//

arrayLiteral
    : HASH LPAREN arrayElement* RPAREN
    ;

arrayElement
    : literal
    | identifier
    | symbolLiteral
    | arrayLiteral
    ;

//
// Symbole : #ident. Le pool MTSymbol.intern existe deja
// cote runtime ; le langage l'expose enfin.
//

symbolLiteral
    : HASH IDENTIFIER
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

HASH     : '#' ;

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
