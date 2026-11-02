# MiniTalk V3 — Perspective SGBD, persistance et mondes

Ouvert le 02/11/2026. Document de prospective : il formalise
une direction, pose les questions de conception dans l'ordre
où elles doivent être tranchées, et n'engage aucune
implémentation. Il est le pendant de ARCHITECTURE.md côté
langage.

## 1. La vision

Le fil qui relie persistance, concurrence et partage tient en
une phrase : **un contexte = un monde** (décision du 18/10),
et le monde est l'unité des trois :

- **Persistance** : on ne persiste pas des objets flottants,
  on persiste un monde — ses classes, ses variables, ses
  objets, son état.
- **Concurrence** : le monde est l'unité d'isolation. Deux
  mondes qui tournent en parallèle ne peuvent pas empiéter
  l'un sur l'autre — modèle acteur (Erlang/Elixir) : état privé,
  communication par frontière.
- **Partage** : le monde est l'unité de frontière. Partager,
  c'est faire traverser la frontière — et le SGBD est le
  point de passage.

## 2. Principe central : le SGBD comme zone neutre

Chaque MTContext persiste et lit dans la base, mais **aucun
monde ne pointe sur l'objet d'un autre monde**. Les lignes de
la base sont des données plates, sérialisées ; chaque monde
les *matérialise* en objets qui lui sont propres — exactement 
le rapport snapshot / runtime (l'image disque n'est pas l'objet
vivant, c'est sa trace, rechargée).

Ce principe résout les trois problèmes d'un coup, sans casser
l'isolation :

1. **Persistance** : un monde se sauve et se recharge.
2. **Partage local** : deux mondes dans la même JVM ne
   partagent rien en mémoire — ils partagent des lignes.
   La base fait tiers de confiance.
3. **Partage distant** : un monde ici, un monde ailleurs,
   un SGBD serveur multi-accès comme point de rendez-vous.

Le mot « image » reste réservé au snapshot disque d'un
runtime. Le mapping objet-SGBD est **autre chose** et
mérite son propre nom — à décider (voir §5, vocabulaire).

## 3. Questions de fond, dans l'ordre où elles se tranchent

### Q1 — L'identité stable (LA première décision)

`==` est l'identité stricte **dans** un monde. Après un
save/load, l'objet rechargé est-il « le même » ? Il faut un
concept d'**identité stable** (un oid stocké en base, cousin
du `identity` d'Object) distinct de l'identité mémoire.

La question précise : l'identité mémoire cède-t-elle la place
à une identité « logique » portée par l'objet ET la base ?
Tout le reste s'empile sur cette décision.

Q1 bis, qui en découle : les singletons. nil, true, false
sont uniques par monde — leur oid est-il résolu par
convention (lignes réservées de la base) ?

---

#### travaux en cours (02/11, réflexion Jean-Marc)

Décision de structure : les OID sont des UUID ; la bande basse
(0 – 40960) est réservée aux objets built-in du langage, tout
ce qui est au-dessus appartient aux mondes (UUID aléatoires,
disjoints de la bande basse par construction — un v4 porte
ses bits de version à 0100).

Plan d'adressage proposé :
   Bande | Contenu | Notes |
 |---|---|---|
 | 0 – 4095 | instances d'objets internes (nil, TRUE, FALSE) | 3 utilisés ; plage volontairement large |
 | 4096 – 8191 | classes | 11 utilisées aujourd'hui |
 | 8192 – 24575 | méthodes d'instance built-in (Java) | ~75 aujourd'hui ; large pour les futurs connecteurs Java |
 | 24576 – 40960 | méthodes de classe built-in (Java) | quasi vide aujourd'hui ; même élasticité |

Bornes rondes en hexa (0x0000, 0x1000, 0x2000, 0x6000,
0xA000) : lisibles dans une table SQL. Les bandes de méthodes
sont volontairement surdimensionnées : le jour où des
connecteurs vers des classes Java exposeront des API
entières, aucune renumérotation ne sera nécessaire — un plan
d'adressage qui oblige à décaler des bornes après coup est un
mauvais plan.

Deux points déjà tranchés au fil de la discussion :

- **Les objets de valeur n'ont pas besoin d'OID** (Integer,
  Float, Character, String) : pas de cache d'entiers
  (décision du 11/10) = pas d'identité observable entre
  instances = sérialisation **par valeur**, embarquée dans
  le graphe. L'absence de cache de 42 paie un dividende
  inattendu en 2026.
- **Les symboles** : leur OID se dérivera probablement de
  leur nom — l'identité transversale est déjà assumée par
  le pool (MTSymbol.intern, dernier static d'état).

Un piège identifié avant qu'il ne morde : **l'ordre de
`getDeclaredMethods()` en réflexion Java n'est pas
garanti**. Une assignation séquentielle à l'installation
donnerait des OID de primitives instables d'une JVM à
l'autre. Deux parades, à trancher :

1. **Table littérale sous contrôle Git** (favori) : chaque
   primitive a sa ligne — "Object>>identity = 8192",
   "String>>at: = 8231"... Les OID sont des décisions
   visibles, diffables en revue, jamais un artefact
   d'ordre. Coût : une ligne par primitive (~75, plus les
   connecteurs futurs).
2. **Énumération déterministe** : trier par (classe,
   sélecteur) avant assignation — stable entre JVM, mais
   une insertion au milieu décale tout le monde.

Reste ouvert : le choix entre les deux parades ; la
convention exacte des OID de symboles (dérivation du nom —
hash ? bande dédiée ?).

### Q2 — Qu'est-ce qui se sérialise ?

- Integer, Float, String, Character, Symbol, Array : faciles.
- **Les blocs ?** Aujourd'hui leur corps est une lambda
  Java — incasérable. Le jour où ils porteront l'AST
  (prévu de toute façon), un bloc devient *des données* :
  persistable, donc transmissible entre mondes. L'AST
  n'est pas qu'une étape d'interpréteur, c'est le billet
  d'entrée de la persistance.
- Les classes définies en MiniTalk (le jour où le langage
  saura en définir) : leur définition est-elle des
  données ?

### Q3 — La granularité du save

Monde entier (façon snapshot) ou objets modifiés (façon
transaction). Le SGBD appelle naturellement la deuxième :
le monde a une session, les objets modifiés sont écrits en
fin d'unité de travail — et la **cohérence
transactionnelle** entre mondes concurrents est obtenue
gratuitement.

### Q4 — La concurrence locale puis distante

- Locale (même JVM) : deux mondes, une base. Conflit
  d'écriture = conflit SQL, résolu par transactions.
- Locale, deuxième étage : mondes *en mémoire* qui
  communiquent par messages sans base (modèle acteur
  complet) — seule étape qui exige de traverser la
  frontière sans sérialisation. À garder pour plus tard.
- Distante : mondes sur des machines, SGBD serveur.

### Q5 — Le vocabulaire

Le mapping objet-SGBD a besoin d'un nom (l'image est prise).
Idéal lointain : que ce soit des mots du langage —
`world save.`, `world load:` ... mais ne brûlons pas
les étapes : le nom du concept d'abord, le sucre syntaxique
plus tard.

## 4. Les candidats SGBD

Grille de lecture (note du 02/11) : derby et hsqldb
permettent des bases en fichiers locaux OU sur serveur
distant gérant le multi-accès.
   SGBD | Modèle | Atout | Usage naturel |
 |---|---|---|---|
 | SQLite (Xerial, JDBC) | fichier unique, embarqué | zéro administration | le poste de travail, le monde mono-utilisateur |
 | Derby | pur Java, embarqué **ou** client-serveur | même jar pour les deux modes | la montée vers le multi-accès |
 | HSQLDB | pur Java, fichier/serveur/mémoire | mode mémoire | les tests unitaires de persistance |

Tous trois JDBC : l'abstraction `java.sql` suffit à
commencer, le choix concret n'a pas à être définitif tant
que la couche de mapping (Q1-Q3) n'est pas décidée.

## 5. Feuille de route indicative

1. Décider Q1 (identité stable/oid) — session de
   discussion, rien d'autre.
2. Sérialisation des objets de base (Q2) : le format
   logique d'abord (qu'est-ce qu'un Integer dans la
   base ?), le schéma SQL ensuite.
3. Save/load d'un monde entier (Q3, granularité
   grossière) — l'équivalent « image » en base.
4. Deux mondes, une base : partage local (Q4).
5. Granularité transactionnelle (Q3 fin) : unité de
   travail, écriture des objets modifiés.
6. Le serveur distant (Q4 distante) — derby/hsqldb.
7. Et seulement alors : le vocabulaire du langage (Q5),
   quand les concepts sont stables.

Chaque étape livre une valeur utilisable seule ; aucune
n'exige la suivante pour être utile.

## 6. Ce que ce document n'est pas

- Pas un engagement d'implémentation : MiniTalk est
  aujourd'hui Turing-complet, testé à 153 verts, et la
  priorité reste au bonheur de la brique suivante.
- Pas un design figé : les décisions Q1-Q5 seront prises
  en session, avec le journal comme témoin, comme
  toujours.

