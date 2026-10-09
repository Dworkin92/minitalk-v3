# Journal de développement MiniTalk V3

## Etat courant

- Bootstrap Object/Class
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

### Prochain objectif


- MTInteger : singletons inutiles, MTInteger.of(int), primitives arithmétiques
  (+ - * / %, comparaisons = == < > <= >=, asString).
- Puis : MTString en classe, MTScope/MTBlock (ifNil: invoquera de vrais blocs).
- Réduire progressivement l'utilisation de null dans le runtime
  (MTClass.send, MTClass.asString, MTString.send restent a null).
