# MiniTalk V3

MiniTalk est un langage objet expérimental, sous license
GNU GPL v3.0, inspiré de Smalltalk, et implémenté en Java.
Sa vocation ? un petit langage de scripting 
**facile à utiliser et à déchiffrer**, où tout est
message, pensé à terme comme un outil lié à une base de
données (pour la persistance des objets, ou une utilisation
personnelle).

C'est aussi un projet sans autre prétention qu'être un terrain éducatif
pour perfectionner mes connaissances en développement Java, et également
en développement accompagné par l'IA (j'utilise Mistral AI version Pro),
avec un journal de développement qui témoigne de chaque décision.

## Ce à quoi miniTalk ressemble

```smalltalk
" Les commentaires sont entre guillemets, comme en Smalltalk "
   total | total := 0.

(1 to: 10) do: [:i | total := total + i].
total printLn.                                " 55 "
 | ages | ages := { 'anne' => 41. 'brian' => 39 }.
(ages at: 'anne') printLn.                    " 41 "

[total < 100] whileTrue: [total := total + total].
total printLn.
```

Pour résumer :
1. A l'image du smalltalk dont il s'inspire, tout est message :
   les opérations sur les nombres, les conditions de boucle,
   la répétition d'une chaîne (`'ab' * 3`),
   les intervalles (`1 to: 10 by: 2`).
2. Les conditions sont des objets booléens stricts,
3. les indices dans les tableaux commencent à 1,
4. une erreur d'argument se traduit par un message de type :
   `'x' n'est pas un Integer (at:)` et non toute
   une trace de pile réflexive.
5. vous disposez d'un langage parfaitement réflexif, capable de
   se redéfinir entièrement lui-même à volonté.
   Les primitives Java éventuellement écrasées sont toujours accessibles
   via un simple appel de type `uneClasse@opérateur` en lieu et place
   de l'`opérateur` remplacé.

le tout dans un jar de quelques centaines de Ko pour le langage de base pur.

**Remarque importante :** ce langage, s'il est de la famille
des Smalltalk, n'est toutefois pas du Smalltalk : la syntaxe
de miniTalk s'éloigne par moment de celle de son illustre parent
pour adopter des fonctionalités plus modernes, ou plus
raisonnables pour ceux qui connaissent des langages plus classiques
comme Java, Python, Ruby.

## Ce qui existe déjà

- Noyau objet complet de classes avec leurs méthodes : 
  Object, Class, Nil, Boolean, Integer, Float, Character,
  String, Symbol, Array, Dictionary, Block (vraies
  fermetures lexicales).
- Interpréteur complet (grammaire ANTLR, AST, REPL
  multi-lignes) : MiniTalk est Turing-complet.
- Dispatch de messages, littéraux `#(...)` (tableaux),
  `{ ... => ... }` (dictionnaires), `$h` (caractères),
  `#symbole`, `| var1 ... |` variables temporaires.
- la définition de nouvelles classes et méthodes
- le rappel des primitives Java

## Documentation

La documentation est minimale mais suffisante, je l'espère,
pour que les débutants puissent se lancer

- **`docs/MiniTalk-Reference.md`** — la référence du
  langage et du runtime : syntaxe, sémantique, protocole
  de chaque classe, décisions documentées. *Le document
  à lire pour écrire du MiniTalk.*

- **`docs/ARCHITECTURE.md`** — l'architecture du langage
  et ses choix fondateurs (tout est message, précédence,
  modèle de valeurs ...). 
  *À lire seulement si le sujet vous intéresse.*

- **`docs/Journal.md`** — le journal de développement,
  brique par brique, décision par décision. La lecture
  de ce document est, elle aussi, optionnelle.

- **`docs/PERSPECTIVE-SGBD.md`** — la prospective :
  expose ce qui est envisagé pour le futur de minitalk.
  Il y a beaucoup de chantiers :)

## Construire et essayer miniTalk

Vous aurez besoin de :
- Java 25. J'indique Adoptium Temurin mais n'importe quel JDK 25 devrait fonctionner : https://adoptium.net
- Maven : https://maven.apache.org

```powershell
mvn test

mvn compile exec:java "-Dexec.mainClass=mt.Main"
```

Dans le REPL : 
- une ligne = une expression,
- les blocs multi-lignes sont acceptés.

Vous pouvez sortir de la boucle REPL en tapant le mot-clé `quit` ou `exit`
seul sur une ligne.

## Pourquoi V3 ?

Les versions 1 et 2 ont fait la démonstration de la faisabilité d'un tel langage poser les jalons pour l'essentiel,
puis sont tombées dans le piège classique des prototypes : des retours en arrière en boucle, des verrues peu compréhensibles.

V3 est née avec la volonté de rester le plus simple possible et une méthode :
- journal de développement,
- une mémoire des décisions,
- de nombreux tests comme garde-fou,
- des branches, commits et pushes réguliers, pour retourner facilement en arrière si plus rien ne fonctionne

Cette démarche a fait ses preuves depuis plus d'un mois car le langage avance régulièrement, et le journal en témoigne.

