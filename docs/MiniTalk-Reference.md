# MiniTalk V3 — Référence du langage et du runtime

État au 24/10/2026. MiniTalk est un Smalltalk-like : tout
est objet, tout calcul est un message, les blocs sont des
fermetures. Cette page décrit la syntaxe supportée par
l'interpréteur, la sémantique, puis les classes Java du
runtime.

## 1. Syntaxe

### 1.1 Unités lexicales

L'analyseur lexical (généré par ANTLR) produit les unités
suivantes. Les blancs (espaces, tabulations, retours à la
ligne) séparent les unités et sont ignorés ; il n'y a pas
de signification particulière aux fins de ligne.

#### Nombres

    INTEGER    suite de chiffres : [0-9]+

Un entier est toujours positif ; il n'existe pas de
littéral négatif (écrire `0 - 5`). Pas encore de
virgule flottante.

#### Chaînes et commentaires

    STRING     '...' ; le guillemet simple est
              échappé en le doublant : 'l''eau'

Une chaîne tient sur une ligne. Le guillemet double est
réservé aux commentaires :

    COMMENT    " ... " — ignoré

#### Mots-clés

    nil, true, false

Contrairement à Smalltalk, ce sont de **vrais mots-clés**
(tokens NIL, TRUE, FALSE), pas des variables
d'environnement : ils ne peuvent être ni masqués ni
réaffectés.

#### Identifiants

    IDENTIFIER  lettre puis lettres, chiffres, ou
               underscore : [a-zA-Z_][a-zA-Z0-9_]*

Les noms de messages (`value`, `size`) ne sont pas des
mots-clés du langage : de simples identifiants.

#### Sélecteurs binaires

    BINARY     suite d'au moins un caractère pris
               parmi : + - * / = < > % ~ , &

`|` et `:` n'en font pas partie (réservés : voir les
signes de ponctuation).

#### Signes de ponctuation

    :=  et  <-    affectation (synonymes)
    :              deux-points des mots-clés (`max:`)
    |              barre des paramètres `[:x | ...]`
                   et des temporaires `[| t | ...]`
    [  ]           crochets des blocs
    (  )           parenthèses de groupement :
                   `(3 + 4) * 2`
    .              point de séquence

### 1.2 Littéraux

    42              entier
    'hello'         chaîne ; '' échappe un guillemet :
                    'l''eau' -> l'eau
    nil             l'objet vide
    true / false    booléens

Les commentaires vont de `"` jusqu'au `"` suivant :

    " ceci est un commentaire "

### 1.3 Messages

Trois formes, par priorité décroissante (comme en
Smalltalk) :

    size                    unaire  (aucun argument)
    3 + 4                   binaire (un argument)
    x max: y                clé    (n arguments)

L'évaluation des priorités suit l'A.O.S qui est la suivante :
1. plus prioritaires : messages unaires
2. signe unaire '-<nombre>', '+<nombre>'
2. '\*\*'
3. '\*','\/','\%'
4. '\+','\-' et  messages binaires 

Exemples :

    3 + 4 * 2        = 3 + (4 * 2)       = 11
    'abc' size + 1   = ('abc' size) + 1 = 4

Quand deux opérateurs ont la même priorité : **évaluation de gauche à droite**

    10 - 4 - 3     = (10 - 4) - 3     = 3
    'a' , 'b' , 'c' = ('a' , 'b') , 'c'

**Exception : la puissance '\*\*' est droite associative**
(comme en mathématiques et en Python) :

    2 ** 3 ** 2    = 2 ** (3 ** 2)    = 2 ** 9 = 512

et non (2 ** 3) ** 2 = 64. C'est l'unique opérateur
qui se groupe depuis la droite — les parenthèses
restent la parade en cas de doute : (2 ** 3) ** 2.

Le signe unaire est unique : `--3` est refusé par la
grammaire. La double négation s'écrit `-(-3)`, à la
manière mathématique.

Sélecteurs binaires : `+ - * / = ~= < > <= >= ,` et
combinaisons des caractères `+ - * / = < > % ~ , &`.

Sont interdits : `|` (réservé aux paramètres et
temporaires, comme en Smalltalk-80), et `:`.

### 1.4 Blocs et fermetures

    [ corps ]                      sans paramètre
    [:x | x + 1]                   avec paramètre
    [:x :y | x max: y]             plusieurs paramètres
    [:x | | t | t := x. t]         avec temporaires

- Un bloc capture la **portée de définition** : il voit
  les variables vivantes là où il est écrit.
- Les temporaires sont initialisées à `nil`.
- Invocation par message unaire : `[41] value`,
  `[:x | x + 1] value: 41`.

### 1.5 Affectation

    var := expression
    var <- expression        (synonyme)

L'affectation est une **instruction**. La variable doit
être déclarée (paramètre ou temporaire) ; sinon
`Variable non déclarée`. L'affectation vaut l'expression
affectée.

### 1.6 Séquences et programmes

Les instructions se séparent par des points :

    i := 1. total := 0. total := total + i

Une séquence vaut sa dernière instruction. Un programme
est une séquence ; vide, il vaut `nil`.

### 1.7 Boucles

    [condition] whileTrue: [corps]

La condition est un bloc, réévaluée à chaque tour ; elle
doit produire un **booléen** (pas de vérité à la C :
`[3] whileTrue:` est une erreur).

La boucle est réitérée dans que la condition est vérifiée
(tant qu'elle est vraie).

Valeur de retour : le
**nombre d'itérations** (divergence MiniTalk, utile au
débogage).

    [condition] whileFalse: [corps]

La condition est un bloc, réévaluée à chaque tour ; elle
doit produire un **booléen** (pas de vérité à la C :
`[3] whileTrue:` est une erreur).

La boucle est réitérée tant que la condition n'est pas
vérifée (tant qu'elle est fausse).

Valeur de retour : le
**nombre d'itérations** (divergence MiniTalk, utile au
débogage).

## 2. Primitives par classe

### Object
    == ~= class printString

### Boolean
    ifTrue: ifFalse: ifTrue:ifFalse:
    and: or: not asString

### Integer
    + - * / = ~= < > <= >=
    asString printString

### String
    , size at: asString printString
    = ~=

### Nil
    isNil notNil ifNil: asString printString

### Tableaux et symboles

#### Littéral tableau `#(...)`

`#(1 2 3)` construit un tableau. **Divergence assumée avec Smalltalk** :
les éléments sont évalués comme des **variables**, pas comme des symboles.
Un tableau de symboles s'écrit `#(#a #b #c)` — moins joli, plus explicite.

Un élément de `#(...)` peut être : un littéral, une variable, un symbole,
ou un tableau imbriqué. **Rien d'autre** : `#(1 + 2)` est refusé par la
grammaire, et aucun parenthésage ne peut forcer une expression.

`#(...)` n'est pas une constante : le tableau est **reconstruit à chaque
évaluation** et les variables sont lues à ce moment-là. Deux évaluations
du même littéral donnent deux tableaux distincts (`==` faux), égaux par
valeur (`=` vrai, comparaison profonde).

#### Littéral symbole `#ident`

`#abc` s'évalue en symbole. Les symboles sont identifiés par nom dans un
monde donné : `#a == #a` est vrai. `asString` retourne le nom sans le `#`.

#### Protocole Array

| Message | Sémantique |
|---|---|
| `size`, `isEmpty` | évidents |
| `at: unEntier` | accès 1-based, `MTException` hors bornes |
| `=` / `==` | valeur (profonde) / identité |
| `do: unBloc` | parcourt, invoque le bloc avec chaque élément, retourne le récepteur |
| `collect: unBloc` | tableau des valeurs du bloc |
| `select: unBloc` | éléments pour lesquels le bloc retourne vrai — **strictement booléen**, sinon `MTException` |

#### Intervalles

`1 to: 10` → `#(1 2 ... 10)`, inclusif. Pas de syntaxe dédiée : « tout est
message ». `5 to: 1` → tableau vide. `to:by:` viendra plus tard.

### Block
    value value: value:value:
    whileTrue:
    asString

## 3. Runtime — architecture Java

Un contexte = un monde : toute classe, tout objet connaît
son `MTContext`, qui possède les fabriques.

    mt.runtime
      MTContext     le monde : classes et fabriques
                    (newInteger, newString, newBlock,
                    newScope, nil, true, false)
      MTObject      interface : send, getMTClass,
                    asString, isNil
      MTClass       classe d'objets, table de méthodes
      MTSymbol      identifiant, intern()
      MTScope       portée lexicale chaînée :
                    define / lookup / assign
      MTBlock       fermeture : paramètres + corps
                    (implémente MTObject et MTMethod)
      MTInteger, MTString, MTBoolean, MTNil

    mt.runtime.primitives
      MTPrimitiveInstaller   connecte @Primitive aux
                             classes
      @Primitive("sélecteur")
      MTIntegerPrimitives, MTStringPrimitives,
      MTNilPrimitives, MTBooleanPrimitives,
      MTBlockPrimitives

    mt.bootstrap
      MTBootstrap   installe un monde complet :
                    initialize() -> getContext()

    mt.interpreter
      MTInterpreter.evaluate(source) : texte -> valeur
      AstBuilderVisitor   parse -> AST (records mt.ast)

Messages non compris et erreurs : `MTException`.

## 4. Chaîne d'exécution

    source -> lexer/parser ANTLR (mt.generated)
           -> AST (records mt.ast, eval(context, scope))
           -> runtime (send / primitives)
           -> MTObject

## 5. Décisions retenues

- 21/10 : un contexte = un monde ; vocabulaire MTContext.
- 24/10 : `|` réservé ; conditions strictement
  booléennes ; `whileTrue:` retourne le nombre
  d'itérations ; affectation exige une variable déclarée.
