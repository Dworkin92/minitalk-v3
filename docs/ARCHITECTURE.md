# MiniTalk V3 - Architecture



## Présentation



MiniTalk est un langage de scripting orienté objets inspiré de Smalltalk.



Son objectif principal est l'automatisation, l'orchestration de processus et la manipulation de flux tout en conservant un modèle objet simple fondé sur l'envoi de messages.



MiniTalk n'est pas :


- un clone de Smalltalk ;

- un clone de Java ;

- un shell Unix amélioré.


MiniTalk est un langage moderne utilisant les concepts fondamentaux de Smalltalk tout en simplifiant sa syntaxe et son architecture.



---



# Objectifs



Les objectifs principaux de MiniTalk sont :



- simplicité ;

- lisibilité ;

- extensibilité ;

- intégration naturelle avec Java ;

- manipulation aisée des processus et pipelines ;

- modèle objet cohérent.



Les objectifs non retenus pour la V3 :


- métaclasses ;

- compilation native ;

- machine virtuelle dédiée ;

- système de types complexe ;

- optimisation prématurée.


---


# Plateforme technique


## Langage d'implémentation


```text

Java 25

```



## Parsing



```text

ANTLR v4

```



## Build



```text

Maven

```



---



# Structure du projet



Dans un premier temps, MiniTalk est un projet Maven simple (mono-module).

<img src="arborescence-projet.png" alt="Arborescence minitalk" width="500"/>



```text

minitalk

│
├── pom.xml
│
└── src
     ├── main
     │   ├── antlr4
     │   │   └── mt
     │   │       ├── MiniTalkLexer.g4
     │   │       └── MiniTalkParser.g4
     │   │
     │   ├── java
     │   │   └── mt
     │   │       ├── ast
     │   │       ├── parser
     │   │       ├── runtime
     │   │       ├── interpreter
     │   │       ├── process
     │   │       └── cli
     │   │
     │   └── resources
     │
     └── test
          └── java

```



---



# Architecture générale


Le code source suit le chemin suivant :



```text

Source MiniTalk
       ↓
  ANTLR Lexer
       ↓
  ANTLR Parser
       ↓
  Parse Tree
       ↓
  AST Builder
       ↓
  AST immutable
       ↓
  Interpreter
       ↓
    Runtime

```



Le Parse Tree ANTLR ne doit jamais être exécuté directement.



Un AST spécifique à MiniTalk est construit avant l'interprétation.



---



# Convention Java



Toutes les classes internes du runtime sont préfixées par :



```text

MT

```



Exemples :



```java

MTObject

MTClass

MTInstance

MTMethod



MTInteger

MTFloat

MTString

MTSymbol



MTArray

MTDictionary



MTCommand

MTProcess

MTPipeline

```



Cette convention évite les conflits avec le JDK :



```java

Object

Class

Process

Runtime

Module

Package

```



Le préfixe MT n'est jamais visible dans le langage MiniTalk.



---



# Modèle objet



MiniTalk est entièrement orienté objets : tout y est objet.

Une classe est elle-même un objet spécial.

Les métaclasses ne sont pas supportées.



---


## Une classe contient


### Métadonnées



```text

name

superclass

```



### Variables d'instance



```text

instVars

```

Référence les variables d'instance à valoriser dans l'objet, stockées sous forme d'un array de symboles.



Exemple :



```smalltalk

#pid

#stdout

#stderr

```



### Variables de classe



```text

classVars

```

Référence les variables de classe, valorisées dans la classe elle-même, stockées en interne sous la forme d'un dictionnaire.


### Méthodes d'instance



```text

instMethods

```

Référence les méthodes d'instances


### Méthodes de classe



```text

classMethods

```

Référence les méthodes d'instances

---

## Définition des classes

MiniTalk conserve l'esprit Smalltalk : les classes sont créées et enrichies par envoi de messages.

### Création d'une classe

```smalltalk
Person <- Object subclass: #Person.
```

ou avec héritage explicite :

```smalltalk
Employee <- Person subclass: #Employee.
```

La création d'une classe retourne un objet classe.

### Enrichissement d'une classe

Une classe peut être enrichie progressivement par envoi de messages.

#### Variables d'instance

```smalltalk
Person << instVars: #( #name #age ).
```

#### Variables de classe

```smalltalk
Person << classVars: #( #Population ).
```

#### Méthodes d'instance

```smalltalk
Person << instMethods: [

    greet [
        ('Bonjour ' + name) println.
    ]

    name: newName [
        name <- newName trim.
    ]

].
```

#### Méthodes de classe

```smalltalk
Person << classMethods: [

    newNamed: aName [

        p <- self new.
        p name: aName.
        ^p

    ]

].
```

### Philosophie

Le message `<<` est utilisé pour enrichir une classe existante.

Il ne représente pas une métaclasse et n'introduit aucun mécanisme spécial dans le langage.

Conceptuellement :

```smalltalk
Person << instVars: ...
Person << classVars: ...
Person << instMethods: ...
Person << classMethods: ...
```

sont simplement des envois de messages à un objet classe.


---

# Classes fondamentales



La V3 fournit les classes suivantes :



```text

Object

Class



Boolean

Nil



Integer

Float



String

Symbol



Array

Dictionary



Block



Command

Process

Stream

```



---



# Interface MTObject



Interface racine de tous les objets du runtime.



```java

public interface MTObject {
    MTClass getMTClass();
      MTObject send(
      MTSymbol selector,
      MTObject... arguments
    ) throws MTException;

    boolean identityEquals(
      MTObject other);

    MTString asString()
      throws MTException;

    default boolean isNil() {
      return false;
    }

    default boolean isTrue() {
      return true;
    }

    default boolean isFalse() {
      return false;
    }

}

```



---



# Syntaxe


## Affectation

MiniTalk accepte deux syntaxes équivalentes.

```smalltalk
name <- 'Bob'.
count <- 42.
```

ou

```smalltalk
name := 'Bob'.
count := 42.
```

Les deux syntaxes produisent exactement le même AST.


---



## Messages unaires



```smalltalk

person name



process run



array size

```



---



## Messages binaires



```smalltalk

1 + 2



10 - 4

```



---



## Messages à mots-clés



```smalltalk

dict at: #name



dict at: #name put: 'Bob'

```



---



# Accès pointé



MiniTalk accepte une notation moderne basée sur le point.



Exemple :



```smalltalk

proc.stdout



proc.stderr



proc.exitCode

```



Cette notation est un sucre syntaxique transformé par le parser en :



```smalltalk

proc stdout



proc stderr



proc exitCode

```



---



# Blocs



## Sans argument



```smalltalk

[
   'hello' println.
]

```



## Avec arguments



```smalltalk

[:line |
   line println.
]

```


Les blocs sont des objets de première classe.


Ils sont utilisés notamment pour :



- traitements de flux ;

- callbacks ;

- gestion d'erreur ;

- itérations.



---



# Gestion des erreurs



Toutes les erreurs MiniTalk dérivent de :



```java

MTException

```



---



## Hiérarchie actuelle



```text

MTException
   |
   +-- MTMessageNotUnderstoodException
   |
   +-- MTTypeException
   |
   +-- MTArithmeticException
   |
   +-- MTProcessException
   |
   +-- MTIOException
   |
   +-- MTParseException
   |
   +-- MTInterpreterException

```



---



## Syntaxe retenue



La V3 utilise un message :



```smalltalk

catch:

```



Exemple :



```smalltalk

[
   dangerousOperation.
]
catch: [:e |
   e message println.
].

```

La syntaxe historique de Smalltalk :


```smalltalk

on:do:

```

n'est pas retenue.



---



# Collections



## Array



```smalltalk

#(1 2 3)

```



ou



```smalltalk

Array with: 1 with: 2 with: 3.

```



---



## Dictionary



```smalltalk

{
   #name => 'Bob'.
   #age  => 42.
}

```

ou


```smalltalk

dict at: #name put: 'Bob'.

```

L'opérateur => représente une association clé/valeur.


---



# Symboles



Les symboles sont internés.



Exemple :



```smalltalk

#name == #name

```



retourne :



```smalltalk

true

```



---



# Commandes



Création d'une commande :



```smalltalk

cmd := 'ls -la' asCommand.

```

ou


```smalltalk

cmd := Command named: 'ls'.

```


---



# Processus



Exécution :



```smalltalk

proc := cmd run.

```



Accès aux flux :



```smalltalk

proc.stdin

proc.stdout

proc.stderr

```



Code de retour :



```smalltalk

proc.exitCode

```



---



# Pipelines



MiniTalk introduit l'opérateur :



```smalltalk

->

```



Utilisé pour relier des flux de commandes.



Exemple :



```smalltalk

'find . -name \*.java' asCommand
   -> ('grep Service' asCommand)
   -> ('sort' asCommand)

```



Cette syntaxe représente explicitement le déplacement des données entre commandes.



---



# Flux



Traitement ligne par ligne :



```smalltalk

proc.stdout onLine: [:line |
   line println.
].

```



Traitement des erreurs :


```smalltalk

proc.stderr onLine: [:err |
   err println.
].

```



---



# Runtime Process



L'implémentation utilise directement les classes Java :



```java

ProcessBuilder

Process

InputStream

OutputStream

BufferedReader

```



MiniTalk encapsule ces objets dans :



```java

MTCommand

MTProcess

MTStream

```



---



# Philosophie du langage



MiniTalk privilégie :



- les objets ;

- les messages ;

- les blocs ;

- les processus ;

- les flux ;

- les pipelines ;

- la lisibilité.



MiniTalk évite :



- les métaclasses ;

- les hiérarchies compliquées ;

- les opérateurs ésotériques ;

- les optimisations prématurées ;

- le dogmatisme Smalltalk.



---



# État actuel des décisions



✅ Java 25


✅ Maven


✅ ANTLR v4


✅ Runtime préfixé MT


✅ Pas de métaclasses


✅ Messages Smalltalk


✅ Syntaxe pointée moderne


✅ Gestion des erreurs via `catch:`


✅ Pipelines via `->`


✅ Projet Maven mono-module pour démarrer


✅ Processus et flux comme concepts centraux

