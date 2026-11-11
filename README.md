# MiniTalk V3

MiniTalk est un langage objet expérimental, sous license
GNU GPL v3.0, inspiré de Smalltalk, et implémenté en Java.
Sa vocation ? un petit langage de scripting 
**facile à utiliser et à déchiffrer**, où tout est
message, pensé à terme comme un outil lié à une base de
données (pour la persistance des objets, ou une utilisation
personnelle).

C'est aussi un projet plaisir : un langage qui se construit
brique après brique, avec un journal de développement qui
témoigne de chaque décision.

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
1. tout est message : les opérations sur les nombres,
   les conditions de boucle, la répétition d'une chaîne
   (`'ab' * 3`), les intervalles (`1 to: 10 by: 2`).
2. Les conditions sont strictement booléennes,
3. les indices dans les tableaux commencent à 1,
4. une erreur d'argument se lit `'x' n'est pas un Integer (at:)` — pas
   une trace de pile réflexive.

Une remarque importante : ce langage, s'il est de la famille
des Smalltalk, n'est toutefois pas du Smalltalk : la syntaxe
de miniTalk s'éloigne par moment de celle de son illustre parent
pour adopter des fonctionalités plus modernes, ou plus
raisonnables pour ceux qui connaissent des langages plus classiques
comme Java, Python, Ruby.

## Ce qui existe déjà

- Noyau objet complet : Object, Class, Nil, Boolean,
  Integer, Float, Character, String, Symbol, Array,
  Dictionary, Block (vraies fermetures lexicales).
- Interpréteur complet (grammaire ANTLR, AST, REPL
  multi-lignes) : MiniTalk est Turing-complet.
- Dispatch de messages, littéraux `#(...)` (tableaux),
  `{ ... => ... }` (dictionnaires), `$h` (caractères),
  `#symbole`, temporaires de programme.
- la définition de nouvelles classes et méthodes
- 217 tests verts.

## Documentation

La documentation est minimale mais suffisante, je l'espère,
pour que les débutants puissent se lancer

- **`docs/MiniTalk-Reference.md`** — la référence du
  langage et du runtime : syntaxe, sémantique, protocole
  de chaque classe, décisions documentées. *Le document
  à lire pour écrire du MiniTalk.*

- **`docs/ARCHITECTURE.md`** — l'architecture du langage
  et ses choix fondateurs (tout est message, précédence,
  modèle de valeurs). *À lire si le sujet vous intéresse.*

- **`docs/Journal.md`** — le journal de développement,
  brique par brique, décision par décision. La lecture
  de ce document est, elle aussi, optionnelle.

- **`docs/PERSPECTIVE-SGBD.md`** — la prospective :
  expose ce qui est envisagé pour le futur de minitalk.
  Il y a beaucoup de chantiers :)

## Construire et essayer

Vous aurez besoin de :
- Java 25 (Temurin) : https://adoptium.net
- Maven : https://maven.apache.org

```powershell
mvn test

mvn compile exec\:java "-Dexec.mainClass=mt.Main"
```

Dans le REPL : 
- une ligne = une expression,
- les blocs multi-lignes sont acceptés.

Vous pouvez sortir de la boucle REPL en tapant `quit` ou `exit`
seul sur une ligne.

## Pourquoi V3 ?

Les versions 1 et 2 ont montré l'essentiel, puis sont
tombées dans le piège classique des prototypes : des
retours en arrière en boucle. V3 est née avec une méthode —
journal de développement, mémoire des décisions, tests
comme garde-fou, commits réguliers — pour ne faire
qu'avancer. Le journal en témoigne.
