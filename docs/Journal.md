# Journal de développement MiniTalk V3

## Etat courant

- MTContext : les classes fondamentales, les singletons et le registre des classes vivent dans un contexte instanciable (MTContext.newStandard()). Plus aucun état statique dans les classes du runtime ; MTBootstrap n'est qu'un point d'entrée. 
- MTSymbol
- Dispatch de messages
- MTPrimitiveInstaller (@Primitive, réflexion au bootstrap)
- MTObjectPrimitives
    - identity
    - class
    - isNil
    - notNil
- MTNil (singleton, rattache à la classe Nil)
    - ifNil:, ifNotNil:, = / ==, asString
- MTBoolean (singletons TRUE / FALSE)
    - ifTrue:, ifFalse:, ifTrue:ifFalse:, and:, or:, not
- MTString (champ + constructeur, toString)
- MTInteger (fabrique of(int))
    - + - * / %, negated
    - = == < > <= >=
    - asString, yourself
- MTBlock / MTScope : vrais blocs (fermetures), invoquables par
  ifTrue:, ifFalse:, ifNil:, ifNotNil: ; corps = lambda Java
  en attendant l'AST. value / value: sur la classe Block.
- MTContext (kernel : possede classes, singletons, fabriques, registre)
- MTString (classe : concatenation, size, at:, comparaisons)
- MTBlock (fermeture : implemente MTObject et MTMethod)
- MTScope (portee lexicale : lookup/assign remontent la chaine)
- MTBlockPrimitives (value, value:, value:value:)

### Retour a l'AOS

- Relu ARCHITECTURE.md : la precedence AOS y etait
  specifiee depuis l'origine (avec ** et %). Le passage
  en Smalltalk strict de la veille etait une erreur
  documentaire de l'assistant, pas une decision du
  projet. Retablissement : ** > * / % > + - ,
  autres binaires au niveau bas, gauche a droite.
- Primitives ** et % sur Integer. 54 tests verts.
- Restent en attente (mentions d'ARCHITECTURE.md) :
  unaire de signe (-6), Float.
- Confirme : ** droite associatif (math/Python),
  unique exception au gauche-a-droite ; documente
  dans ARCHITECTURE.md et la reference.

## Chronologie

### 05/10/2026

- Création du projet Maven.
- Mise en place Git / GitHub / SSH.
- Bootstrap Object/Class.
- MTSymbol.
- Premier lookup.

### 06/10/2026

- Enregistrement de méthodes.
- invoke().
- send().
- Retour de valeur.
- MTException transformée en RuntimeException.

### 07/10/2026

- Introduction de MTObjectPrimitives.
- Object>>identity.
- Object>>class.

### 08/10/2026

- Création du singleton MTNil.
- Validation :

      MTNil.instance().isNil()
          => true


### 09/10/2026

- MTNil rattache à la classe Nil : vrai send() avec lookup.
- Annotation @Primitive (repeatable, flag classSide) + MTPrimitiveInstaller :
  installation réflexive des primitives au bootstrap, une classe de
  primitives par brique, le bootstrap devient une liste stable.
- MTBoolean : singletons TRUE/FALSE, of(), ifTrue:/ifFalse:/and:/or:/not.
- Semantique Smalltalk : isNil/notNil deplaces sur Object.
- MTString : champ, constructeur(String), toString().
- Ajout de JUnit 5 (pom.xml), tests dans src/test/java.
- Premier build complet vert : BUILD SUCCESS, 4 tests OK.
- Session en binome avec Vibe (revue d'erreurs au compile : addClassMethod,
  champ booleanClass, placement MTString, signature des primitives).

### 11/10/2026

- MTInteger : une instance par valeur, fabrique of(int), primitives
  arithmetiques (+ - * / %, negated), comparaisons (= == < > <= >=),
  asString, yourself.
- Division par zero : MTException.
- '==' sur identité stricte assumée (pas de cache d'entiers).
- Decision de design : a terme, plus aucun static d'etat ; toutes les
  classes vivront dans un MTKernel (scope global objet, comme imagine
  en v1). Refactoring prevu au moment de la brique Block (MTScope).

### 18/10/2026

- Refactoring MTContext : les classes fondamentales et les
  singletons (nil, TRUE, FALSE) quittent les champs statiques
  (MTInteger.bindClass, MTBoolean.TRUE, MTNil.instance) pour
  vivre dans un MTContext instanciable. Un contexte = un monde ;
  deux contexts coexistent sans interference.
- Noyau en acces direct (objectClass, classClass, nilClass,
  booleanClass, integerClass), toutes les classes dans un registre
  Map<MTSymbol, MTClass> (registerClass / lookupClass) qui
  accueillera les classes futures (String, Array, etc.).
- MTClass porte une reference vers son contexte : les primitives
  retrouvent nil / TRUE / FALSE et fabriquent les objets via le
  contexte du receiver, plus aucun etat statique.
- Fabriques : MTContext.newInteger(int), newBoolean(boolean),
  nil(), trueObject(), falseObject().
- MTBootstrap allege : il construit un MTContext standard et
  delegue (getContext(), getObjectClass(), ...).
- Suppression du code mort dans MTInteger (ancien of() commente).
- Tests passes au contexte (MTContext.newStandard() en @BeforeAll)
  + nouveau test : deux contexts independants (nil et trueObject
  distincts entre contexts).
- Vocabulaire : le mot "image" est reserve au snapshot disque
  d'un runtime ; notre contexte n'a aucune notion de persistance.
  Decision notee : MTContext, pas MTImage.

### 20/10/2026

- MTScope : portee lexicale chainee (define, lookup, assign,
  extend). Un bloc est evalue dans une portee fille de sa
  portee de definition.
- MTBlock : fermeture, a la fois MTObject et MTMethod. Les
  primitives existantes (ifTrue:, ifNil:, ...) invoquent donc
  de vrais blocs sans aucune modification.
- Corps du bloc : lambda Java (MTBlock.Body) pour l'instant,
  remplacee par l'AST quand l'interpreteur existera.
- Nouvelle classe Block dans le noyau du contexte + registre,
  primitives value / value:, fabrique MTContext.newBlock(...).
- Branchement 20/10 : travail sur la branche mtblock-mtscope
  (workflow : switch -c, merge dans main si tests verts).
- Tests : 9 nouveaux (parametres, fermeture, isolation des
  portees, assign, arite, ifTrue:/ifNil: avec vrais blocs).

### 22/10/2026

- MTString en classe : MTContext.newString(), concatenation (,),
  size, at: (1-based, codepoint), = / ==, print.
- Refactoring MTKernel realise sous forme de MTContext : toutes
  les fabriques et singletons vivent dans le contexte, plus
  aucun static d'etat. Un contexte = un monde.
- MTBlock : fermeture implementant MTObject ET MTMethod — les
  primitives existantes (ifTrue:, ifNil:) acceptent de vrais blocs
  sans modification. Corps = lambda Java (Body), AST plus tard.
- MTScope : portee lexicale chainee, extend()/define()/lookup()/assign().
- Premier test de fermeture : un bloc capture sa portee de definition.
- MTClass : send() et asString() implementes (fini les null) ;
  toString() retourne le nom de la classe.
- MTInstance : asString() implemente ("an" + nom de classe).
- MTBlockPrimitives : value / value: / value:value: fusionnes
  en une seule methode (annotations repetables).
- Suppression de MTDummyMethod et MTNativeMethod (fossiles).
- Main.java : vraie demo du langage (bloc + value:).
- Message d'erreur unifies en "Message non compris : X >> sel".
- Notes ajoutees : and:/or: paresseux plus tard ; intern() seul
  static assume.

### 24/10/2026

- Interpreteur complet : grammaire ANTLR (MiniTalk.g4),
  AST (records mt.ast), visiteur (mt.interpreter), entree
  MTInterpreter.evaluate(source).
- Grammaire noyau : litteraux (entiers, chaines avec
  echappement '', nil/true/false), messages unaires /
  binaires / a mot-cle, blocs avec parametres, sequences
  a points.   reserve (exclu des binaires, comme en
  Smalltalk-80).
- AST : un record par construction, interface MTNode avec
  eval(context, scope). MTBlockNode transforme un bloc du
  langage en MTBlock du runtime : la portee d'ecriture
  devient la portee de definition (fermeture).
- Debogue multi-etages : 3 causes pour 11 erreurs
  (separateur BINARY vs BAR, program non optionnel,
  envoi avec selecteur vide quand aucun mot-cle).
- whileTrue: sur Block : condition reevaluee a chaque
  tour. Divergence MiniTalk : retourne le nombre
  d'iterations (0 si jamais executee) -- contretemps
  assumé avec Smalltalk, utile au debogage.
- whileFalse: sur Block, symetrique de whileTrue:.
  Meme semantique : conditions strictement booleennes,
  retour du nombre d'iterations. 50 tests verts.
- Decisions : conditions strictement booleennes. Verite a
  la C ecartee (entier non nul comme vrai) apres debat.
  markdown
- Bug dormant corrige : les parentheses n'etaient
  jamais evaluees (visitPrimary rendait le token '('
  au lieu de l'expression interieure). Depiste par le
  test parenthesesForcentLeGroupe.
- retour de l'AOS, pour se conformer au document d'architecture
- Confirme : ** droite associatif (math/Python),
  unique exception au gauche-a-droite ; documente
  dans ARCHITECTURE.md et la reference.
- Relu ARCHITECTURE.md : la precedence AOS y etait
  specifiee depuis l'origine, avec ** et %. Le passage
  en Smalltalk strict de la veille etait une erreur
  documentaire de l'assistant, pas une decision du
  projet.
- Retabli : ** > * / % > + - ; autres binaires au
  niveau bas, gauche a droite. ** droite associatif.
  Primitives ** et % sur Integer.
- Bug dormant corrige : les parentheses n'etaient
  jamais evaluees (visitPrimary rendait le token '('
  au lieu de l'expression interieure). Depiste par le
  test parenthesesForcentLeGroupe.
- 56 tests verts.
- Restent en attente (mentions d'ARCHITECTURE.md) :
  unaire de signe (-6), Float.

### 25/10/2026

- Signe unaire implémente : plus haut niveau de
  precedence (ARCHITECTURE.md enfin honore en
  entier cote entiers). -6, +6, 3 * -4, -2 ** 2.
- Decisions : signe prioritaire sur tout (-2 ** 2
  = (-2) ** 2 = 4) ; signe UNIQUE, --3 refuse, la
  double negation s'ecrit -(-3), facon
  mathematique. Moins unaire = message negated.
- Lecon ANTLR : l'ordre des regles de parseur est
  indifferent, celui du lexer est sacre (pense-bete
  laisse dans le g4 ; doublon powerExpression
  chasse par erreur 51).
- Lecture multi-lignes au REPL : invite '...>'
  tant que crochets/parentheses/chaines ne sont
  pas equilibres (scan respectant '' echappe).
  quit/exit ne vivent qu'au niveau zero.
- ParseCancellationException attrapee : une erreur
  de syntaxe ne tue plus le REPL.
- Drapeau lineOpen : print signale la ligne
  ouverte ; l'echo repart sur sa propre ligne
  seulement si necessaire (pas de ligne vide
  parasite pour les expressions muettes).
- 70 tests verts. Liste 'confort' terminee.

## Prochain objectif

### Notes pour plus tard

- `and:` / `or:` sont evaluatifs (recoivent un MTBoolean).
  Le vrai Smalltalk est paresseux : des que l'interpreteur
  existera, accepter un MTMethod et l'invoquer conditionnellement,
  comme ifTrue:.

- MTSymbol.intern est le dernier static d'etat du runtime.
  Decision assumee : les symboles sont l'identite du langage,
  pas l'etat d'un monde. (A differencier des fabriques, qui
  vivent dans MTContext.)

### Prochain objectif

- print/printLn,
- REPL confortable (blocs multi-lignes),
- Array, MTCharacter (at: retourne un codepoint en MTInteger), and:/or: paresseux (accepter un MTMethod, comme ifTrue:).
- ajouter la gestion des Float

