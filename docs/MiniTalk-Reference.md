# MiniTalk V3 — Référence du langage et du runtime

"La  carte n'est pas le territoire"
   Le Monde du non-A, A.E. Van Vogt

MiniTalk est un Smalltalk-like :
- tout est objet,
- tout calcul est un message,
- les blocs sont des fermetures.
- les sélecteurs ne sont pas les méthodes

Cette page décrit la syntaxe supportée par
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
    FLOAT      chiffres, point, chiffres : [0-9]+ '.' [0-9]+

Un entier est toujours positif ; il n'existe pas de
littéral négatif (écrire `0 - 5`). Un flottant littéral
est un `double` IEEE 754 (`3.5`, `2.0`) ; le point est
décimal, il n'y a pas de notation exposante pour l'instant.

#### Chaînes, caractères et commentaires

    STRING     '...' ; le guillemet simple est
              échappé en le doublant : 'l''eau'
    CHARACTER  '$' suivi d'un caractère pris tel
              quel : $h, $$, $' ($ seul prend le
              caractère suivant, même un espace)

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

` ` et `:` n'en font pas partie (réservés : voir les
signes de ponctuation).

#### Signes de ponctuation

    :=  et  <-    affectation (synonymes)
    :              deux-points des mots-clés (`max:`)
 |              barre des paramètres `[:x | ...]`
                   et des temporaires `[| t | ...]`
    [  ]           crochets des blocs
    (  )           parenthèses de groupement :
                   `(3 + 4) * 2`
    .              point de séquence (et point décimal
                   dans un littéral flottant)
    #              marque des littéraux tableau `#( )`
                   et symbole `#ident`
    $              marque d'un littéral caractère

### 1.2 Littéraux

    42              entier
    3.5             flottant (double)
    'hello'         chaîne ; '' échappe un guillemet :
                    'l''eau' -> l'eau
    $h              caractère (codepoint Unicode)
    nil             l'objet vide
    true / false    booléens
    #(1 2 3)        tableau ; un élément est un littéral,
                    une variable, un symbole ou un tableau
                    imbriqué — rien d'autre (voir 2)
    #abc            symbole (voir 2)

Les littéraux tableaux et symboles sont détaillés dans la
section « Tableaux et symboles » : leur sémantique est
une divergence assumée avec Smalltalk.

Les commentaires vont de `"` jusqu'au `"` suivant :

    " ceci est un commentaire "

### 1.3 Messages

Trois formes, par priorité décroissante (comme en
Smalltalk) :

    size                    unaire  (aucun argument)
    3 + 4                   binaire (un argument)
    x max: y                clé    (n arguments)

L'évaluation des priorités suit l'A.O.S qui est la suivante :
1. messages unaires
2. signe unaire `-`, `+` (sur l'expression entière :
   `-2 ** 2` = `(-2) ** 2` = 4)
3. `**`
4. `*`, `/`, `%`
5. `+`, `-` et autres messages binaires

Exemples :

    3 + 4 * 2        = 3 + (4 * 2)       = 11
    'abc' size + 1   = ('abc' size) + 1 = 4

Quand deux opérateurs ont la même priorité : **évaluation de gauche à droite**

    10 - 4 - 3     = (10 - 4) - 3     = 3
    'a' , 'b' , 'c' = ('a' , 'b') , 'c'

**Exception : la puissance `**` est droite associative**
(comme en mathématiques et en Python) :

    2 ** 3 ** 2    = 2 ** (3 ** 2)    = 2 ** 9 = 512

et non (2 ** 3) ** 2 = 64. C'est l'unique opérateur
qui se groupe depuis la droite — les parenthèses
restent la parade en cas de doute : (2 ** 3) ** 2.

Le signe unaire est unique : `--3` est refusé par la
grammaire. La double négation s'écrit `-(-3)`, à la
manière mathématique.

**Attention — l'argument d'un message à mot-clé est une
expression binaire complète.** Tout ce qui suit le
deux-points est évalué d'abord :

    'hello' at: 1 = $h     se lit :  'hello' at: (1 = $h)   !

Pour comparer le caractère rendu par `at:`, il faut
parenthéser :

    ('hello' at: 1) = $h

C'est le comportement Smalltalk standard ; la grammaire
est saine, mais l'oubli des parenthèses est l'erreur
classique ici.

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
affectée. Il n'existe pas encore de déclaration de
variables au niveau du programme : seuls les blocs
déclarent (paramètres et temporaires).

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

La boucle est réitérée tant que la condition est vérifiée
(tant qu'elle est vraie).

Valeur de retour : le
**nombre d'itérations** (divergence MiniTalk, utile au
débogage).

    [condition] whileFalse: [corps]

Symétrique : réitéré tant que la condition n'est pas
vérifiée (tant qu'elle est fausse), même sémantique
strictement booléenne, même valeur de retour (nombre
d'itérations).

## 2. Primitives par classe

### Object
    - identity class print printLn

    - isNil notNil

    - `=` — égalité par défaut : IDENTITÉ (convention
      Smalltalk-80). Les classes à sémantique de valeur
      (Integer, Float, Character, String, Array, Dictionary)
      surchargent. La comparaison est TOTALE : entre types
      différents, elle rend false, jamais une erreur.
    - `==` — identité stricte, définie ICI et héritée par
      toutes les classes (07/11) : plus aucune classe ne la
      redéfinit.

### Class

Les messages de cette section sont envoyés À UNE CLASSE
(le récepteur est un objet classe). Résolution : SES
méthodes de classe d'abord, puis le socle commun
(`superclass`, `new`, ...) ; une méthode de classe
homonyme d'un message du socle GAGNE (09/11).

Règle de vocabulaire : le SÉLECTEUR est le nom d'une
méthode (toujours un symbole) ; la MÉTHODE est le code,
la donnée. On ne désigne une méthode qu'en la nommant.

#### Naissance et instances (08/11)
 | Message | Sémantique |
 |---|---|
 | `subclass: #Nom` | crée la classe fille, l'enregistre et lie la classe au monde courant en créant une variable de même nom. Retourne la classe ; si le nom est déjà pris : `Classe existante : Nom` |
 | `new` | instance vide : les variables déclarées se lisent `nil` tant que rien n'a écrit |
 | `instVars: #(#a #b)` | déclare des variables d'instance (symboles EXPLICITES) |
 | `classVars: #(#C)` | déclare des variables de classe EN CRÉANT LES ENTRÉES À NIL ; re-déclarer est un no-op, la valeur est gardée |
 | `accessors: #(#a)` | génère le getter (`a`) et le setter (`a:`) — le setter rend la valeur affectée |
 | `getters: #(#a)` | génère le getter seul |
 | `setters: #(#a)` | génère le setter seul |

- La génération exige la déclaration préalable, sinon
  `variable inconnue : X >> a` (le flux est ordonné :
  on déclare, puis on génère).
- LA DERNIERE DECLARATION L'EMPORTE SUR LES
  PRECEDENTES : la génération écrase une méthode
  existante.
- une classe nommée provoque la création automatique d'une
  variable globale de même nom dans le monde courant.

  exemple: `Object subclass: #Person` crée une variable `Person`
  portant la classe dans le monde courant.

#### Réflexivité (07/11)
 | Message | Sémantique |
 |---|---|
 | `superclass` | la classe mère ; `nil` à la racine (Object) |
 | `instSelectors` | sélecteurs d'instance LOCAUX à la classe, triés par nom |
 | `classSelectors` | sélecteurs de classe LOCAUX, triés par nom |
 | `removeInstMethod: #s` | retire la méthode d'instance, rend la classe ; absente : `methode absente : X >> s` |
 | `removeClassMethod: #s` | retire la méthode de classe, rend la classe ; absente : idem |

- Le préfixe `inst`/`class` dit le CÔTÉ, toujours
  explicite (contrepartie de la classe-unique : pas de
  métaclasse par classe, `X class` répond toujours
  `Class`). `selectors` nu est refusé : ambigu.
- Retirer une primitive du bootstrap est LÉGAL : le
  monde boiteux se ferme, `newStandard()` en recrée un
  neuf. Sélecteurs PORTEURS (leur disparition casse la
  mécanique interne) : `=`, `value`, `value:`,
  `value:value:`.

### Boolean
    ifTrue: ifFalse: ifTrue:ifFalse:
    and: or: not asString

    `and:` et `or:` sont **paresseux** (29/10) :

    false and: [un block]    -> false, le bloc
                                n'est JAMAIS évalué
    true or: [un block]      -> true, idem

L'opérande est un **bloc** (évalué seulement si nécessaire)
ou un booléen nu (`true and: false` reste valide). Le
résultat de l'opérande doit être strictement un booléen :
`true and: [42]` lève `MTException` (pas de vérité à la
C, décision du 24/10 étendue ici).

### Integer
    + - * / % ** negated
    = < > <= >=
    abs, even, odd, min:, max:
    shr: shl:
    to:
    asString asFloat yourself

`abs` :
  retourne la valeur absolue dy receveur.
  exemple : `-7 abs` retournera `7`, `7 abs` retournera `7`.

`even`, `odd` :

  indique si le receveur est pair ou impair. Ces deux méthodes
  retourne un Boolean.

`min:`, `max:` :

  retourne la plus petite ou la plus grande des deux valeurs
  entre le receveur et l'argument.

  Attention : pour les nombres négatifs, plus leur valeur absolue
  est grande, plus ils sont petits (-8 < -5) !

`shl:`, `shr:` :

  ce sont les opérateurs de décalage binaire à gauche (`shl:`,
  shift left) ou à droite (`shr:`, shift right).
  
  * `shl: 1` correspond à « multiplié par 2 », tandis que
  * `shr: 1` correspond à « divisé par 2» (quotient entier,
    bits perdus).

> **Attention** : messages à mot-clé, donc priorité usuelle
> (la plus basse). `2 + 8 shr: 1` = `(2 + 8) shr: 1` = 5,
> pas 6 ; avec l'arithmétique, mettre des parenthèses :
> `2 + (8 shr: 1)`.
  
`to:`,`to:by:` :

  `1 to: 10` → `#(1 2 ... 10)`, génère un array avec les n entiers entre 1 et 10, bornes incluses.

  `1 to: 10 by: 2` → `#(1 3 5 7 9)` :
        le pas est signé, des bornes mal orientées donnent un tableau vide
        (`1 to: 10 by: -2` → `#()`), un pas nul lève `MTException`. Pas de syntaxe
        dédiée : « tout est message ».

`to:do:`, `to:by:do:` :
  `1 to: 20 do: [ :i | aBlock ]`,
    exécution du block aBlock 20 fois (bornes incluses, donc).
    L'indice d'exécution est fourni au bloc, qui doit déclarer
    un paramètre pour le recevoir — l'arité est un contrat,
    un bloc muet lève une exception (le paramètre peut rester
    inutilisé dans le corps).
    Il n'y a pas de génération d'un array intermédiaire.

`1 to: 20 by: 2 do: [ :i | aBlock ]`
    exécution du block aBlock pour tous les entiers impairs,
    bornes incluses. Même loi pour le paramètre.
    Il n'y a pas de génération d'un array intermédiaire.

Des bornes mal orientées n'invoquent JAMAIS le bloc
(5 to: 1 do: [...] ne fait rien, comme 5 to: 1 rend #()).
Le pas nul est une erreur explicite.
Rend le récepteur (convention `do:`).


### Float
    + - * / % ** negated
    = < > <= >=
    asString asInteger yourself

Un `Float` est un `double` IEEE 754. Affichage : `2.0`
reste affiché `2.0` (le type reste visible, style Squeak).

**Promotion locale (29/10) :** le type d'une opération ne
dépend que de ses **deux opérandes**. Une opération entre
deux `Integer` est entière ; dès qu'un `Float` apparaît
d'un côté, l'opération est flottante :

    1 + 2.5        -> 3.5      (promotion)
    7 / 3          -> 2        (division entière)
    7 / 3.0        -> 2.333...  (promotion)

> **Attention.** La promotion est locale à CHAQUE
> opération, pas globale à l'expression :
>
>     7 / 3 + 3.6    -> 2 + 3.6  -> 5.6
>
> Pour un quotient réel, la conversion est explicite et à
> la charge du développeur :
>
>     7 asFloat / 3 + 3.6    -> 5.933...
>
> C'est le comportement Java/C/Python ; une « contagion »
> globale serait indéfinissable avec des variables (le
> type de `a` n'est connu qu'à l'exécution).

Autres règles :

- Égalité **numérique** inter-types : `2 = 2.0` est vrai
  (comparaison par valeur, comme Smalltalk) ; `==`
  reste l'identité stricte.
- Puissance sans cast : `2.0 ** 0.5` est la racine carrée
  de 2 ; `2 ** 0.5` promeut en flottant et répond aussi
  la racine.
- Division et modulo par zéro (`0` ou `0.0`) :
  `MTException` — MiniTalk n'embrasse pas l'infini IEEE,
  par cohérence avec Integer.
- `asInteger` **tronque vers zéro** : `2.9 asInteger = 2`,
  `-2.9 asInteger = -2`.

### String
    , * size at: isEmpty
    = 
    reverse uppercase lowercase
    indexOf: contains:
    asString

`at:` rend un **Character** (29/10) — plus un codepoint
brut. `times` : la répétition `*` (voir Character).
La concaténation `,` accepte un `Character` à droite :
`'ab' , $c` -> `'abc'`. Le résultat d'une concaténation
est toujours une `String`.

### Character
    , *
    =
    asString asInteger yourself

Un caractère est un **codepoint Unicode** (type distinct,
29/10 : un caractère supplémentaire tel que 𝄞 ne tient pas
dans un `char` UTF-16, une « String de longueur 1 » ne
pourrait pas le représenter).

- Littéral : `$h`, `$$` (dollar), `$'` (apostrophe) ;
  le caractère qui suit `$` est pris tel quel.
- `=` compare les codepoints : `$a = $a` est vrai.
- `asInteger` rend le codepoint : `$h asInteger = 104`.
- `asString` rend la chaîne d'un caractère : `$h asString`
  -> `'h'`.
- Concaténation `,` : `$a , $b` -> `'ab'` ;
  `$a , 'bc'` -> `'abc'`. **Fermée sur String** : le
  résultat d'une concaténation est toujours une String.
- Répétition `*` : `$a * 5` -> `'aaaaa'`. Dual de la
  multiplication entière (l'addition répétée a pour
  symétrique la concaténation répétée). L'entier doit
  être positif ou nul : `$a * -1` lève `MTException` ;
  `$a * 0` rend la chaîne vide.

> **Pourquoi pas `+` pour la concaténation ?** `+` reste
> strictement arithmétique (Integer, Float) : un seul
> sélecteur par opération, sans surcharge implicite
> (divergence assumée avec Ruby). La concaténation
> s'écrit `,` et rien d'autre.

### Nil
    isNil notNil ifNil: ifNotNil:
    =
    asString

### Dictionary

Littéral : `{ cle => valeur. ... }`, vide `{ }`. La
`=>` est la hash rocket de Ruby/Perl (token réservé :
plus jamais un sélecteur binaire). Divergence assumée
avec `#(...)` : les valeurs sont des expressions
COMPLÈTES. Clés PAR VALEUR (`2` et `2.0` = même clé),
ordre d'insertion. La sonde n'est pas une comparaison :
sonder avec un type incompatible est une absence, jamais
une erreur.
 | Message | Sémantique |
 |---|---|
 | `size` | nombre d'associations |
 | `isEmpty` | vide ou non |
 | `at: uneCle` | la valeur ; clé absente : `MTException` |
 | `at: uneCle put: uneValeur` | écrit, rend la valeur |
 | `includesKey: uneCle` | vrai si la clé existe |
 | `keys` / `values` | tableaux des clés / des valeurs |
 | `removeKey: uneCle` | retire et rend la valeur ; absente : `MTException` |
 | `do: unBloc` | parcourt les VALEURS (convention Smalltalk), rend le récepteur |
 | `at: uneCle ifAbsent: unBloc` | la valeur, ou le bloc évalué si la clé manque (bloc PARESSEUX, jamais touché si la clé existe) |
 | `at: uneCle ifAbsentPut: unBloc` | la valeur ; si absente, le bloc est évalué, INSÉRÉ sous la clé, et rendu |
 | `=` / `==` | valeur (profonde) / identité |
 | `asString` | forme littérale ré-évaluable |

Affichage RE-ÉVALUABLE : `asString` rend la forme
littérale — `{ 1 => 2. 'a' => $b }` — chaînes quotées
(`''` échappé), caractères en `$`. L'écho du REPL est
du MiniTalk valide.

### Tableaux et symboles

#### Littéral tableau `#(...)`

`#(1 2 3)` construit un tableau. **Divergence assumée avec Smalltalk** :
les éléments sont évalués comme des **variables**, pas comme des symboles.
Un tableau de symboles s'écrit `#(#a #b #c)` — moins joli, plus explicite.

Un élément de `#(...)` peut être : un littéral (y compris
flottant et caractère), une variable, un symbole,
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
 | `=` / `==` | valeur (profonde, via le message `=` de chaque élément) / identité |
 | `do: unBloc` | parcourt, invoque le bloc avec chaque élément, retourne le récepteur |
 | `collect: unBloc` | tableau des valeurs du bloc |
 | `select: unBloc` | éléments pour lesquels le bloc retourne vrai — **strictement booléen**, sinon `MTException` |
 | `includes: unObjet` | vrai si un élément est égal par son message `=` (égalité profonde) |

Affichage : `#(1 2 3)`, tableaux imbriqués compris ; les chaînes
s'y affichent sans guillemets (affichage minimal, à affiner).

#### Protocole Symbol
 | Message | Sémantique |
 |---|---|
 | `=` / `==` | identité (les symboles sont uniques par nom dans un monde) |
 | `asString` | le nom, sans le `#` |

#### Intervalles

`1 to: 10` → `#(1 2 ... 10)`, inclusif. Pas de syntaxe dédiée : « tout est
message ». `5 to: 1` → tableau vide. `to:by:` viendra plus tard.

### Block
    value value: value:value:
    whileTrue: whileFalse:
    asString

## 3. Runtime — architecture Java

Un contexte = un monde : toute classe, tout objet connaît
son `MTContext`, qui possède les fabriques.

    mt.runtime
      MTContext     le monde : classes et fabriques
                    (newInteger, newFloat, newString,
                    newCharacter, newArray, newBlock,
                    newScope, symbolObject,
                    nil, true, false)
      MTObject      interface : send, getMTClass,
                    asString, isNil
      MTClass       classe d'objets, table de méthodes
      MTSymbol      identifiant, intern() — le pool,
                    identité du langage, hors des mondes
                    (dernier static d'état assumé)
      MTSymbolObject représentant d'un symbole DANS un
                    contexte, mis en cache par MTContext
      MTScope       portée lexicale chaînée :
                    define / lookup / assign
      MTBlock       fermeture : paramètres + corps
                    (implémente MTObject et MTMethod)
      MTInteger, MTFloat, MTString, MTCharacter,
      MTBoolean, MTNil, MTArray

    mt.runtime.primitives
      MTPrimitiveInstaller   connecte @Primitive aux
                             classes
      @Primitive("sélecteur")
      MTIntegerPrimitives, MTFloatPrimitives,
      MTStringPrimitives, MTCharacterPrimitives,
      MTArrayPrimitives, MTSymbolPrimitives,
      MTNilPrimitives, MTBooleanPrimitives,
      MTBlockPrimitives

    mt.bootstrap
      MTBootstrap   installe un monde complet :
                    initialize() -> getContext()

    mt.interpreter
      MTInterpreter.evaluate(source) : texte -> valeur
      AstBuilderVisitor   parse -> AST (records mt.ast)

Messages non compris et erreurs : `MTException`.
Une erreur de syntaxe remonte en `ParseCancellationException`
(stratégie Bail du parseur) ; le REPL l'attrape sans mourir.

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
- 25/10 : éléments de `#(...)` = variables (divergence
  assumée avec Smalltalk) ; éléments limités à littéral,
  variable, symbole, tableau imbriqué ; le tableau est
  reconstruit à chaque évaluation ; intervalles par
  message `to:`, pas de syntaxe dédiée (« tout est
  message »).
- 26/10 : symboles exposés au langage (`#ident`) ;
  `MTSymbol` reste le pool hors des mondes,
  `MTSymbolObject` en est le représentant par contexte ;
  `select:` strictement booléen ; `do:` retourne le
  récepteur ; pas encore de déclaration de variables au
  niveau programme (seuls les blocs déclarent).
- 29/10 : `and:`/`or:` paresseux (court-circuit, opérande
  bloc ou booléen, résultat strictement booléen) ;
  Float avec **promotion locale** (`7 / 3 + 3.6` = 5.6,
  le réel s'écrit `7 asFloat / 3`) ; égalité numérique
  inter-types (`2 = 2.0` vrai) ; MTCharacter = codepoint
  (type distinct, les codepoints supplémentaires ne
  tiennent pas dans un char) ; `String>>at:` rend un
  Character ; concaténation `,` polymorphe Character/String
  fermée sur String ; `*` = répétition ; `+` refusé comme
  concaténation (sélecteurs orthogonaux, divergence
  assumée avec Ruby) ; division par `0.0` = `MTException`.

- 02/11 : casts durcis, format uniforme <valeur> n'est
  pas un <Type> (<sélecteur>) ;

- 05/11 : String>>includes: strict ; Dictionary 
  (littéral, =>, clés par valeur, ordre d'insertion) ;

- 07/11 : =/== montés sur Object ; réflexivité ; règle
  sélecteur/méthode ; sélecteurs porteurs ;

- 08/11 : classes définies en MiniTalk (subclass:,
  flux de déclarations, accès automatiques par message) ;
  shl:/shr: ; retour sans ^ = dernière expression.
