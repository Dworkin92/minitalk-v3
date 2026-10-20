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
- Tests JUnit (4 verts)
- MTInteger (fabrique of(int))
    - + - * / %, negated
    - = == < > <= >=
    - asString, yourself
- 12 tests JUnit verts
- MTBlock / MTScope : vrais blocs (fermetures), invoquables par
  ifTrue:, ifFalse:, ifNil:, ifNotNil: ; corps = lambda Java
  en attendant l'AST. value / value: sur la classe Block.
  
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

## Prochain objectif

- whileTrue: / whileFalse: (boucles) sur Block, puis
  l'AST/interpreteur pour que les blocs viennent du langage.
- Puis : MTString en classe (registre du contexte).
- Reduire les null restants (MTClass.send, MTClass.asString,
  MTString.send).
  
  
