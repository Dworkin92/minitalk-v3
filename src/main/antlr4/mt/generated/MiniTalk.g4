grammar MiniTalk;

//
// Programme : une suite de points, separees par des points.
// Le point final est optionnel.
//
// Warning : ANTLR n'a pas d'ordre priviliégié pour la prise en
//           compte des règles d'analyse syntaxique !

program
    : blockLocals? (statement ('.' statement)* '.'?)? EOF
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
      ((PLUS | MINUS | BINARY | QUALIFIED_ADD | QUALIFIED_OTHER)
       multiplicativeExpression)*
    ;

multiplicativeExpression
    : powerExpression
      ((STAR | SLASH | PERCENT | QUALIFIED_MUL)
       powerExpression)*
    ;

unaryExpression
    : primary (IDENTIFIER | qualifiedSelector)*
    ;

// (19/11) p Integer@negated : la primitive NATIVE
// negated de Integer, appliquee au receveur p.
// La fenetre @ : le socle reste joignable meme
// si la table du langage a ete ecrasee.
// La boucle essaie le qualifie D'ABORD : IDENTIFIER
// suivi de AT ne peut pas etre confondu avec une
// invite unaire ordinaire.

// (21/11) Formes unaire et mot-cle de la fenetre.
// Le mot-cle est glouton : il absorbe les parties
// IDENT: arg qui suivent ; pour rendre la main au
// message ordinaire, parentheser le receveur.
qualifiedSelector
    : IDENTIFIER AT IDENTIFIER
    | IDENTIFIER AT IDENTIFIER COLON binaryExpression
      (IDENTIFIER COLON binaryExpression)*
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
    : signExpression ((POWER | QUALIFIED_POWER) powerExpression)?
    ;

primary
    : literal
    | arrayLiteral
    | dictionaryLiteral
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
// Dictionnaires : { 1 => 'a'. 2 => 'b' }
//
// La marque {} est reservee depuis le 25/10 ; la fleche =>
// est le hash rocket de Ruby (divergence assumee avec
// Smalltalk, dont l'association s'ecrit ->).
//
// Divergence assumee avec #(...) : les cles et les valeurs
// sont des EXPRESSIONS completes, facon Pharo (decision du
// 05/11). Le dictionnaire est reconstruit a chaque
// evaluation, comme #(...).
//

dictionaryLiteral
    : LBRACE (dictionaryPair (DOT dictionaryPair)* DOT?)? RBRACE
    ;

dictionaryPair
    : expression ROCKET expression
    ;
    
//
// Symbole : #ident. Le pool MTSymbol.intern existe deja
// cote runtime ; le langage l'expose enfin.
//

// (21/11) Symboles : mots (unaires, mots-cles) OU
// operateurs binaires. #+ et #~= etaient refuses
// depuis le 16/11 : les methodes binaires ne
// pouvaient pas etre surchargees.
symbolLiteral
    : HASH IDENTIFIER (COLON IDENTIFIER)* COLON?
    | HASH symboleOperateur+
    ;

symboleOperateur
    : PLUS | MINUS | STAR | SLASH | PERCENT
    | POWER | BINARY
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
    | FLOAT
    | STRING
    | CHARACTER
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

// AVANT DOT, sinon 3.5 se lirait "3 . 5"
// (piege consigne le 26/10). Le match le plus
// long fait le reste : FLOAT gagne sur INTEGER.
FLOAT    : [0-9]+ '.' [0-9]+ ([eE] [+-]? [0-9]+)? ;

STRING   : '\'' ('\'' '\'' | ~'\'')* '\'' ;

// Litteral Character : $h, $$, $' ...
// Le caractere qui suit $ est pris tel quel,
// meme un espace ou une apostrophe.
CHARACTER : '$' ~[\r\n] ;

// (21/11) La fenetre binaire : tokens UNIQUES, collés.
// Chaque type vit au niveau AOS de son operation :
// QUALIFIED_ADD avec + et -, QUALIFIED_MUL avec * / %,
// QUALIFIED_POWER avec **, QUALIFIED_OTHER avec les
// autres binaires (= < > ~ = ...).
// Le match le plus long l'emporte sur IDENTIFIER seul.
QUALIFIED_POWER : [a-zA-Z_][a-zA-Z0-9_]* '@' '**' ;
QUALIFIED_ADD   : [a-zA-Z_][a-zA-Z0-9_]* '@' [+\-] ;
QUALIFIED_MUL   : [a-zA-Z_][a-zA-Z0-9_]* '@' [*/%] ;
QUALIFIED_OTHER : [a-zA-Z_][a-zA-Z0-9_]* '@' [=<>%~,&]+ ;

ASSIGN   : '<-' | ':=' ;
AT       : '@' ;
POWER    : '**' ;
PLUS     : '+' ;
MINUS    : '-' ;
STAR     : '*' ;
SLASH    : '/' ;
PERCENT  : '%' ;
ROCKET   : '=>' ;
BINARY   : [+\-*/=<>%~,&]+ ;

HASH     : '#' ;

COLON    : ':' ;
BAR      : '|' ;
DOT      : '.' ;
LBRACE   : '{' ;
RBRACE   : '}' ;
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
