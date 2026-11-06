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

### Q1 — résolution en esprit (06/11, réflexion Jean-Marc)

La tension « table Git ou énumération » se dissout dès qu'on
sépare les DEUX régimes d'identité, qui n'ont pas les
mêmes besoins :

**Régime 1 — les objets du langage : la table Git, figée.**
La table des OID built-in est LA GENÈSE : l'identité du
langage lui-même, livrée dans l'archive, identique pour
tous. Ces OID ne sont jamais alloués, ils sont le langage.
Toute base, où qu'elle naisse, démarre par le même seed :
la table de genèse (premier insert, rituel de fondation).
Elle ne grossit jamais avec l'usage. Le choix table
littérale s'impose : ses OID doivent traverser le temps,
or l'énumération déterministe décale tout à chaque
insertion au milieu — stable par construction > stable
par discipline.

**Régime 2 — les objets des utilisateurs : UUID
aléatoires, PAS de table.** Quand plusieurs personnes
pilotent leur langage et créent classes et objets, chacun
tire un UUIDv4 : 122 bits aléatoires, collision
pratiquement impossible, AUCUNE coordination, aucun
allocateur central, aucune table partagée à verrouiller.
L'allocation est décentralisée par construction — le
dividende du choix UUID dès le départ. La table Git ne
contient que le langage ; la croissance vit dans les
bases, en UUID sauvages.

**Topologie des bases : de la configuration, pas de la
conception.**

    archive du langage (Git)
       └─ table de genèse (OID 0-40960, figée)
              │  seed commun à chaque base
              ▼
       ┌──────────────────┐   ┌─────────────────────┐
       │ base locale      │   │ base réseau          │
       │ (un PC, un       │   │ (l'entreprise ou    │
       │  serveur)        │   │  les appareils      │
       │                  │   │  d'une personne)   │
       │ mondes de la     │   │ mondes qui partagent │
       │ machine          │   │ leurs données       │
       └──────────────────┘   └─────────────────────┘

La confidentialité naît de la topologie : chacun sa base,
ou une base partagée par ceux qui le veulent — le langage
n'impose rien. Le multi-monde n'est pas seulement
multi-utilisateurs : c'est aussi UN même utilisateur sur
plusieurs machines (le MiniTalk du bureau retrouve les
mondes du MiniTalk de la maison).

**Reste ouvert (une seule question, pour beaucoup plus
tard)** : les classes et méthodes DÉFINIES par les
utilisateurs. Un objet = une donnée, UUID sans souci.
Mais une classe créée par quelqu'un est presque du
SCHÉMA : deux personnes définissant « la même » classe
dans deux bases différentes auront deux OID distincts —
pas une collision (bénin), une DIVERGENCE (fusionner ou
coexister ?). Cette question a un nom connu en SGBD : la
migration de schéma. Elle ne se posera qu'au jour où des
classes définies en MiniTalk voyageront. En attendant :
chaque base est souveraine.

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

### Q2 — résolution en esprit (06/11, réflexion Jean-Marc)

**Une table par type de base d'objets**, trois colonnes :
un OID, éventuellement l'OID du monde, la valeur.
   Table | Contenu |
 |---|---|
 | integers | oid, world, value |
 | floats | oid, world, value |
 | strings | oid, world, value |
 | characters | oid, world, value |

**La clé de voûte : les objets de valeur reçoivent un
OID DÉRIVÉ DU CONTENU** (UUID déterministe calculé depuis
la valeur : tous les 42 du monde entier partagent le même
OID). C'est le geste des symboles (OID par nom) étendu aux
objets de valeur : l'identité logique d'une valeur, c'est
la valeur elle-même.

Cela réconcilie trois décisions déjà prises :
- en mémoire, rien ne change : chaque monde matérialise
  son propre MTInteger(42), jamais == entre eux (pas de
  cache, 11/10) ; l'invariant « un OID = une
  matérialisation par monde » tient ;
- en base, une seule ligne pour tous les 42 (dédup),
  save idempotent ;
- surtout : MODÈLE DE RÉFÉRENCE UNIFORME — toute
  référence vers quoi que ce soit est un OID, sans
  discriminer valeur/identité. Les éléments de tableau,
  les valeurs de dictionnaire, les variables capturées :
  tout pointe par OID.

Question de plomberie ouverte (secondaire) : comment
savoir quelle table un OID désigne — table d'indexage
objects(oid, kind), ou bandes réservées supplémentaires ?

**Monde vs portée — un distinguo qui sauve la
conception :** un bloc n'est PAS un monde. Un monde = un
MTContext (18/10), créé par newStandard() et rien
d'autre. Les blocs, même imbriqués par centaines,
habitent leur monde : ce qui se multiplie, ce sont les
PORTÉES, et une portée se range en LIGNES (bindings),
pas en fichiers. Conséquence : le nombre de mondes ne
grossit pas avec l'exécution du code (une session REPL
= un monde, un programme à mille blocs = ce monde).

**Colonne world vs fichier par monde : les deux étages
de la topologie, pas des concurrents.**
- base LOCALE : un fichier par monde (quelques unités —
  isolation physique, confidentiel par construction) ;
- base RÉSEAU : une seule base, une colonne world
  (sessions nombreuses, mondes éphémères créés et
  détruits, partage des lignes de valeur à OID de
  contenu — créer/supprimer des fichiers à tout bout de
  champ serait insensé).

**Les blocs : insécables aujourd'hui, données demain.
La lambda Java est insécable** — un bloc de granit :
un pointeur d'exécution, aucun moyen d'aller lire le
code ou les variables capturées. Trois voies :
- sérialiser la lambda : impasse (opaque, et ne
  survivrait pas à un changement de version du code) ;
- stocker le source et re-parser au chargement :
  séduisant mais boiteux (le monde rechargé
  dépendrait du parseur d'aujourd'hui) ;
- LA BONNE, en deux étapes : phase 1, un bloc est NON
  PERSISTABLE (save le refuse explicitement, erreur
  honnête et visible) ; phase 2, quand les blocs
  porteront leur AST (prévu de toute façon), trois
  tables suffisent :
 | Table | Contenu |
 |---|---|
 | blocks | oid, world, ast (versionné) |
 | block_parameters | oid, position, symbol |
 | bindings | oid (portée), symbol, ref_oid |

La portée lexicale chaînée devient une chaîne de
références en base. Convergence notée : ce qui rend un
bloc persistable le rend transmissible (Q4) — l'AST
n'est pas une étape d'interpréteur, c'est le pivot de
toute la stratégie de persistance.

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

## 4. Les candidats SGBD (révisé le 06/11)

Derby est RETIRÉ des candidats (décision du 06/11) : la
dernière release date du 14/11/2023, et le 10/10/2025 les
développeurs ont voté la retraite du projet en état
read-only — plus aucune release, plus aucun correctif
de sécurité. Java DB avait déjà été abandonné par Oracle
en 2015 (hors JDK depuis le 9). L'attachement à Derby
(similitudes DB2, sa base au travail) est noté comme tel
: raison légitime, décision refusée quand même — on ne
fonde pas une couche de persistance sur un moteur mort.
Preuve que le mouvement est général : Spring déprécie le
support Derby, PaperCut migre son base embarquée de Derby
vers H2.
   SGBD | Modèle | Atout | Usage naturel |
 |---|---|---|---|
 | SQLite (Xerial, JDBC) | fichier unique, embarqué | zéro administration, type présent partout | le monde mono-poste, la base de poche |
 | **H2** | pur Java : embarqué, mémoire, ou serveur TCP | type UUID natif, très actif (2.x), 3 modes dans un seul jar | l'embarqué ET le multi-accès — il reprend le rôle de Derby |
 | HSQLDB | pur Java, fichier/serveur/mémoire | mode mémoire | candidat de repli ; maintenance lente, H2 est son successeur spirituel (même auteur fondateur) |

Sur la conformance SQL de H2 : le cœur standard est
confortablement couvert (dont le type UUID), mais le point
décisif est ailleurs — la couche MiniTalk n'écrira PAS de
SQL artisanal, seulement quelques requêtes plates générées
par le mapping. La conformance qui compte est celle du
noyau, pas celle des modes de compatibilité (émulations
partielles, sources de surprises en migration humaine).

Décision d'architecture en résultant : SQLite + H2 en
tête de liste, HSQLDB en observation. L'abstraction
java.sql reste la règle : le choix définitif n'a pas à
être fait avant l'étape 3 de la feuille de route.

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

