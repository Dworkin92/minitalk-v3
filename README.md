# MiniTalk V3

MiniTalk est un langage objet expérimental, inspiré de
Smalltalk, implémenté en Java. Sa vocation : un petit
langage **facile à utiliser et à déchiffrer**, où tout est
message, pensé à terme comme un outil lié à une base de
données (persistance des objets, mondes isolés, partage).

C'est aussi un projet de plaisir : un langage se construit
brique par brique, avec un journal de développement qui
témoigne de chaque décision.

## À quoi ça ressemble

```smalltalk
" Les commentaires sont entre guillemets, comme en Smalltalk "
   total | total := 0.

(1 to: 10) do: [\:i | total := total + i].
total printLn.                                " 55 "
 | ages | ages := { 'anne' => 41. 'brian' => 39 }.
(ages at: 'anne') printLn.                    " 41 "

[total < 100] whileTrue: [total := total + total].
total printLn.
```

Tout est message : l'addition, la condition de boucle, la
répétition d'une chaîne (`'ab' * 3`), l'intervalle
(`1 to: 10 by: 2`). Les conditions sont strictement
booléennes, les indices commencent à 1, et une erreur
d'argument se lit `'x' n'est pas un Integer (at:)` — pas
une trace de pile réflexive.

## Ce qui existe déjà

- Noyau objet complet : Object, Class, Nil, Boolean,
  Integer, Float, Character, String, Symbol, Array,
  Dictionary, Block (vraies fermetures lexicales).
- Interpréteur complet (grammaire ANTLR, AST, REPL
  multi-lignes) : MiniTalk est Turing-complet.
- Dispatch de messages, littéraux `#(...)` (tableaux),
  `{ ... => ... }` (dictionnaires), `$h` (caractères),
  `#symbole`, temporaires de programme.
- 173 tests verts.

## Documentation

- **`docs/MiniTalk-Reference.md`** — la référence du
  langage et du runtime : syntaxe, sémantique, protocole
  de chaque classe, décisions documentées. *Le document
  à lire pour écrire du MiniTalk.*
- **`docs/ARCHITECTURE.md`** — l'architecture du langage
  et ses choix fondateurs (tout est message, précédence,
  modèle de valeurs). *À lire si le sujet vous intéresse.*
- **`docs/Journal.md`** — le journal de développement,
  brique par brique, décision par décision.
- **`docs/PERSPECTIVE-SGBD.md`** — la prospective :
  persistance des objets, mondes et partage.

## Construire et essayer

Java 25 (Temurin) et Maven.

```powershell
mvn test

mvn compile exec\:java "-Dexec.mainClass=mt.Main"
```

Au REPL : une ligne = une expression, les blocs
multi-lignes sont acceptés, `quit` pour sortir.

## Pourquoi V3 ?

Les versions 1 et 2 ont montré l'essentiel, puis sont
tombées dans le piège classique des prototypes : des
retours en arrière en boucle. V3 est née avec une méthode —
journal de développement, mémoire des décisions, tests
comme garde-fou, commits réguliers — pour ne faire
qu'avancer. Le journal en témoigne.

