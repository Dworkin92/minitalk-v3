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
- MTCharacter (classe : permet d'obtenir/manipuler des caractères UTF )
- MTBlock (fermeture : implemente MTObject et MTMethod)
- MTScope (portee lexicale : lookup/assign remontent la chaine)
- MTBlockPrimitives (value, value:, value:value:)
- MTCharacterPrimitives ( concatenation, ajout multiples, asString, asInteger)
- MTDictionary :
  *  => = hash rocket de Ruby (pas -> l'association Smalltalk) —
     précédent Ruby assumé, comme * répétition ; et token réservé : => n'est plus 
    disponible comme sélecteur binaire.
  * Divergence #(...) vs {...} : le tableau refuse les expressions, 
    le dictionnaire les accepte — deux littéraux, deux règles,
    documentée comme telle.
  * La sonde n'est pas une comparaison : prober { 'a' => 1 } avec 1 est
    une absence, pas une MTException « n'est pas une String » — l'exigence
    du 02/11 s'applique aux comparaisons explicites, pas au
    fonctionnement interne d'une table de hachage.

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

### 26/10/2026

- Array : classe MTArray + litteral #(...). Divergence assumee
  avec Smalltalk : les elements de #(...) sont des VARIABLES,
  pas des symboles ; un tableau de symboles s'ecrit #(#a #b).
- Un element de #(...) = litteral, variable, symbole ou array
  imbrique. RIEN d'autre : #(1 + 2) refuse par la grammaire
  (pas de porte aux expressions imbriquees illisibles).
- #( ) n'est pas une constante de compilation : le tableau
  est reconstruit a chaque evaluation, variables lues a ce
  moment-la (evaluation facon Python, decision du 25/10).
- Symboles exposes au langage : litteral #ident (option A,
  pas de dette technique). MTSymbol reste le pool hors des
  mondes ; MTSymbolObject est son representant PAR CONTEXTE,
  mis en cache : #a == #a dans un monde, isolation preservee.
- Primitives Array : size, isEmpty, at: (1-based), = (profond,
  via le message '=' de chaque element), ==, do: (retourne le
  recepteur), collect:, select: (strictement booleen,
  decision du 24/10 conservee).
- Integer>>to: : 1 to: 10 -> #(1 2 ... 10) inclusif.
  "Tout est message" : pas de syntaxe dediee (decision 25/10).
  5 to: 1 -> tableau vide.
- Nn tests verts.

### 29/10/2026

- and:/or: paresseux : false and: X et true or: X court-circuitent
  (X n'est jamais evalue). Operande = bloc (MTMethod, pattern
  ifTrue:) ou Boolean nu. Resultat strictement booleen
  (decision du 24/10 conservee) : true and: [42] leve MTException.
- Float : MTFloat (double), litteral 3.5 (token FLOAT APRES
  INTEGER, AVANT DOT -- piege du 26/10 tenu). Promotion LOCALE :
  le type d'une operation ne depend que de ses deux operandes ;
  7 / 3 + 3.6 = 5.6, le quotient reel s'ecrit 7 asFloat / 3.
  asFloat / asInteger = conversions explicites. 2 = 2.0 vrai
  (egalite numerique), affichage "2.0" (type visible), 2.0 ** 0.5
  = racine (Math.pow sans cast), division par 0.0 = MTException.
  Lecon JDT : le motif de promotion en if/else, jamais en ternaire
  caste (types freres MTInteger/MTFloat).
- MTCharacter : codepoint int (pas "String de 1" : les codepoints
  supplementaires ne tiennent pas dans un char UTF-16). String>>at:
  rend un Character (tests mis a jour aux deux etages). Litteral $h
  (token '$' + caractere tel quel). Concatenation , polymorphe
  Character/String, fermee sur String. * = repetition ($a * 5 ->
  'aaaaa', precedent Ruby, dual de la multiplication). + REFUSE
  comme concatenation : + reste strictement arithmetique,
  orthogonalite des selecteurs (divergence assumee avec Ruby).
  Lecon : l'argument d'un mot-cle est une binaryExpression --
  ('hello' at: 1) = $h exige des parentheses.
- 129 tests verts. Piège REPL connu, non corrige : $' desequilibre
  le scanner multi-lignes.
- Backlog propose : durcissement des casts des primitives
  (ClassCastException brut -> MTException explicite).

### 30/10/2026

- Reference : sections Float et Character, promotion
  locale (7 / 3 + 3.6 = 5.6, real = 7 asFloat / 3),
  avertissement parentheses/argument-de-mot-cle.
- to:by: sur Integer : pas signe, bornes mal orientees
  -> tableau vide (comme Smalltalk), pas nul -> MTException.
- includes: sur Array : egalite profonde reutilisee ;
  l'egalite numerique inter-types s'y propage
  (#(1 2) includes: 2.0 est vrai).
- Temporaires de programme :   a b | en tete, comme un
  corps de methode Smalltalk. MTLocalsNode, define a nil
  dans la portee racine, visible des blocs par fermeture.
  Decision REPL : la portee racine persiste entre lignes,
  le garde-fou protege contre l'affectation SANS
  declaration prealable (pas entre lignes).
- 145 tests verts.

### 02/11/2026

- Durcissement des casts : format uniforme <valeur>
  n'est pas un <Type> (<selecteur>) -- 'x' n'est pas
  un Integer (at:). Helpers prives par classe (patron
  context() etendu), decision : pas de classe
  centralisee. Classes durcies : Integer, String,
  Character, Array, Block, Float (numeric au format
  uniforme). Casts du recepteur conserves : garantis
  par le dispatch. Garde-fous existants inchanges
  (Condition invalide, Index hors bornes, Division
  par zero, Pas nul).
- Lecon de hierarchie : MTBlock implemente MTMethod ;
  typer body en MTMethod (intention : invocable) et
  non MTBlock (implementation) dans whileTrue:/whileFalse:.
- MTPrimitiveInstaller : une MTException levee par une
  primitive passe telle quelle (erreur du langage) ;
  seules les erreurs Java restent reemballees en
  "Echec primitive". Observation pour plus tard : ce
  message nomme la methode Java, pas le selecteur.
- 8 tests sur le CONTENU des messages. 153 verts.
- Ouverture de docs/PERSPECTIVE-SGBD.md (Q1 en reflexion) :
  OID = UUID, bande basse 0-40960 reservee aux built-in
  (instances 0-4095, classes 4096-8191, primitives
  d'instance 8192-24575, primitives de classe 24576-40960,
  elastiques pour les futurs connecteurs Java). Objets de
  valeur SANS oid (serialisation par valeur -- dividende
  de l'absence de cache d'entiers). Piege note : l'ordre
  de getDeclaredMethods() n'est pas garanti -> table
  litterale Git ou enumeration deterministe, a trancher.

### 05/11/2026

- String>>includes: : appartenance d'un caractere,
  STRICT (une String en argument -> MTException, un
  selecteur un sens) ; parcours par codepoints, casse
  respectee. Symetrie complete : includes: = element,
  contains: = fragment.
- Dictionary : litteral { cle => valeur. ... }, vide { }.
  => = hash rocket de Ruby/Perl (familiarite assumee),
  token ROCKET avant BINARY donc reserve -- plus jamais
  un selecteur binaire (geste POWER/ASSIGN). >= > =
  intacts (suites de caracteres differentes).
  Divergence assumee avec #(...) : contenu = expressions
  COMPLETES (facon Pharo), le tableau reste strict.
  Cles PAR VALEUR : 2 et 2.0 = meme cle (coherent avec =),
  hash toujours d'accord avec l'egalite (valeur pour les
  objets de valeur, profond pour Array, identite pour le
  reste). Ordre d'insertion (LinkedHashMap).
  La sonde n'est pas une comparaison : prober avec un
  type incompatible = absence (false), jamais MTException.
  Primitives : size isEmpty at: at:put: includesKey:
  keys values do: (valeurs, convention Smalltalk)
  removeKey: = (profond) == asString. Affichage
  Dictionary(3), minimal, a affiner.
- Bugs de session : atPut avec de mauvais indices
  (le recepteur n'est pas dans le tableau) ; valueEquals/
  hashOf rendus public (package different).
- Trou revele par un test : == n'est PAS installe sur
  Object -- la Reference l'y inscrit a tort, chaque
  classe porte le sien. A trancher : monter =/== sur
  Object ou corriger la Reference. Frere de l'item
  reflexivite (protocole de la racine).
- 173 tests verts.


### 06/11/2026

- Perspective Q1, resolution en esprit : deux regimes
  d'identite. Table Git = la GENESIS du langage seule
  (OID 0-40960 figes, livree dans l'archive, seed de
  toute base, ne grossit jamais). Objets d'utilisateurs
  = UUID aleatoires, allocation decentralisee, aucune
  coordination ni table partagee. Topologie des bases
  = configuration (locale / reseau), la confidentialite
  nait de la topologie ; le multi-monde inclut un meme
  utilisateur sur plusieurs machines. Reste ouvert,
  beaucoup plus tard : les classes definies par les
  utilisateurs sont presque du SCHEMA -- divergence,
  pas collision -- la migration de schema aura son
  heure.

- Perspective point 4 : candidats SGBD revus (06/11) : Derby
  RETIRE -- derniere release 14/11/2023, retraite
  officielle en read-only le 10/10/2025, Java DB
  abandonne par Oracle en 2015. Attachement DB2 note,
  decision assume. Entree de H2 : pur Java, embarque/
  memoire/serveur TCP, UUID natif, actif -- il reprend
  le role de Derby (precedent : PaperCut fait cette
  migration). La couche MiniTalk n'emettra que du SQL
  plat genere par le mapping : la conformance qui
  compte est celle du noyau. Tete de liste :
  SQLite + H2, HSQLDB en observation.

- Perspective Q2, resolution en esprit (06/11) : une
  table par type de base (oid, world, value). Objets de
  valeur : OID DERIVE DU CONTENU (geste des symboles
  etendu) -> dedup, save idempotent, et MODELE DE
  REFERENCE UNIFORME : toute reference est un OID.
  Distingo sauveur : un bloc n'est pas un monde -- les
  portees se rangent en lignes (bindings), pas en
  fichiers. Fichier par monde en local, colonne world
  en reseau : les deux etages de la topologie. Blocs :
  lambda insécable -> non persistables en phase 1
  (refus explicite), tables blocks/bindings quand
  l'AST arrivera. L'AST = pivot de la strategie.

### 07/11/2026

- = / == montes sur Object : == = identite, herite par
  toutes les classes ; = par defaut = identite (convention
  Smalltalk-80), surcharge par les classes de valeur.
  Menage : les == redondants supprimes (Integer, Float,
  String, Character, Array, Dictionary) ; Nil et Boolean
  abandonnent aussi leur = (identite = valeur pour des
  singletons). Reparation au passage : true = true.
  La comparaison explicite est TOTALE : [1] = 42 rend
  false, plus de « message non compris ». 176 verts.

- Reflexivite, couche Java : removeInstanceMethod /
  removeClassMethod (symetriques des add, format
  « methode absente : X >> sel ») et replaceInstanceMethod
  / replaceClassMethod (ATOMIQUE : un seul put ; absent =
  erreur et classe INCHANGEE). Alias pragmatiques :
  addInstMethod, removeInstMethod, replaceInstMethod,
  instMethodAt, lookupInstMethod -- le raccourci delegue,
  la logique vit dans un seul endroit.
  instanceMethodAt / classMethodAt : LOCALES (au
  contraire du lookup qui remonte la chaine) -- la
  reification commence par savoir lire une table exacte.

- Bug decouvert par la reflexivite : le champ mtClass
  n'etait cible que pour Object et Class -- dix classes
  sur douze repondaient null et tout message a une
  classe-objet partait en NPE. Regle pour toute future
  primitive de creation de classe : setMTClass(classClass).

- DOUZIEME BRIQUE : MTClassPrimitives, la premiere
  installee sur Class. Tout message reflexif herite a
  toutes les classes. Vocabulaire (systeme Inst-miroir,
  arbitre du jour) : superclass (nil a la racine),
  instSelectors / classSelectors (LOCAUX, tries par nom
  pour le determinisme des tests), removeInstMethod: /
  removeClassMethod: (rend la classe). Regle : le prefixe
  inst/class dit le COTE, toujours explicite --
  contrepartie de la classe-unique.
  selectors nu REFUSE : ambigu (deux tables, un
  recepteur).

- Regle de vocabulaire pour la Reference : SELECTEUR =
  le NOM d'une methode (toujours un symbole) ; METHODE =
  le CODE, la donnee. En MiniTalk, on ne designe une
  methode qu'en la nommant. La carte n'est pas le
  territoire (Korzybski/Van Vogt, epigraphe retenue).

- Permission assumee : retirer une builtin est legal.
  Reponse a « et si on casse une builtin ? » : le monde
  boiteux se ferme et newStandard() en recree un neuf --
  la guerison par le monde neuf. Liste des SELECTEURS
  PORTEURS a documenter (leur disparition casse le
  mecanisme interne du monde) : =, value, value:,
  value:value:.

- Enrichissement par le langage lui-meme : le
  remplacement peut CAPTURER l'ancienne methode (rendue
  par le replace) et l'invoquer -- motif decorateur, en
  attente d'un exemple au journal. Piste pour la
  reification future : instMethodAt: au langage, un
  cousin du Block ; super ECARTE pour l'enrobage (il
  delegue au PARENT, pas au remplace).
  instMethodAt: langage IMPOSSIBLE aujourd'hui : une
  primitive doit rendre un MTObject, une MTMethod n'en
  est pas une (seul MTBlock est les deux).

- Premiere suite qui MUTTE volontairement son monde :
  MTReflexivityTest, monde a soi.

- Notes de fond : lookupClassMethod et les classMethods
  semblent dormir (le dispatch passe par la
  metaclasse-unique) -- a unifier un jour. Les cartes de
  MTClass sont des HashMap ; le jour des mondes
  partages (Q4), ConcurrentHashMap. Q1 : retirer une
  builtin ne touche jamais la genes Git, tout monde
  neuf repart entier.

### 08/11/2026

- complétion de la classe MTDictionary avec quelques méthodes
  supplémentaires : **at:ifAbsent:**, **at:ifAbsentPut:**,
  puis adaptation de **toString** et ajout de **printable**.

- Mise en Oeuvre du chantier classes, d'apres ARCHITECTURE.md
  relu AVANT toute proposition :
  * creation : Object subclass: #Person -- le nom seul ;
    l'enrichissement vient APRES, par messages.
  * << : FLUX ENTRANT d'informations vers le receveur
    (amendement de l'architecture par Jean-Marc) --
    formes simples ET agregation en bloc ; desucrage en
    cascade a recepteur implicite, PROMESSE D'ORDEN
    rendue explicite (on declare avant de generer).
  * >> : flux sortant, REMPLACE -> pour les pipelines ;
    couple shell complet <</>>. Decalages binaires :
    shl: / shr: (precedent assembleur), messages a
    mot-cle donc priorite usuelle (la plus basse) --
    2 + 8 shr: 1 = (2+8) shr: 1, parentheses a la
    Reference. Tokens a reserver avant BINARY le jour
    de la grammaire (meme geste que => et **).
  * Retour sans ^ : la valeur de la derniere
    expression evaluee avant la sortie (phrase ajoutee
    au chapitre ^ ; meme contrat que les blocs).
  * ACCES AUTOMATIQUES par message EXPLICITE :
    accessors: / getters: / setters: (banco les trois) ;
    la garde : variable DECLAREE prealablement sinon
    « variable inconnue : X >> var » ; La dernière
    mise à jour l'emporte : la génération peut écraser une
     methode existante (loi du flux).

- PHASE A implementee : subclass: (rend la classe,
  enregistre, setMTClass(classClass) -- regle du
  07/11 tenue a la naissance ; « Classe existante »),
  instVars: (symboles explicites, coherence 26/10),
  accessors:/getters:/setters: (getter = nil par
  defaut ; setter rend la valeur affectee), new
  (instance vide). MTInstance : getProperty/setProperty
  + correctif de l'article (« anPoint » -> a/an espace).
  Question encore ouverte, hors phase A : comment NOMMER
  une classe dans le source MiniTalk (portee racine,
  rabat du registre, ou dictionnaire global).

### 08/11/2026 (suite, Phase A)

- LECON DE METHODE : la session de design a débuté par la relecture
  du fichier ARCHITECTURE.md -- qui avait deja tranché 
  
    * subclass: seul,
    * l'enrichissement par messages,
    * la syntaxe des methodes [ en-tete [ corps ] ],
    * le ^ locale-bloc.

  Règle : le document AVANT toute proposition.

- Regle de localisation (née d'un collage dans le mauvais fichier) :
  
  MTClass = la MECANIQUE Java interne (détient les méthodes :
  addInstVar/hasInstVar, hasClassVar/putClassVar/
  classVarAt),
  
  MTClassPrimitives = la SURFACE LANGAGE miniTalk
  (messages, gardes, emballage).
  
  Toute erreur « asX undefined for MTClass » = bloc dans le
  mauvais fichier.

- PHASE A VERTE (vérifie les 195 tests) :
  subclass: (rend la classe,
  enregistre, setMTClass(classClass), « Classe
  existante : X »), instVars: (symboles explicites),
  classVars: (declare en creant les entrees A NIL --
  le dictionnaire est declaration ET stockage ;
  redeclarer = no-op, la valeur est GARDEE),
  accessors:/getters:/setters: (garde : variable
  declaree sinon « variable inconnue : X >> var » ;
  LA DERNIERE DECLARATION L'EMPORTE SUR LES
  PRECEDENTES -- la génération
  ecrase), new (instance vide, variables lues nil ;
  getter = nil par defaut, setter rend la valeur
  affectee). MTInstance : getProperty/setProperty,
  article a/an + espace (« anPoint » etait un
  accident).

- Lecons du jour : negated est un MESSAGE MiniTalk,
  pas une methode Java -- la fabrique du contexte
  (world.newInteger(-v)) dans les lambdas de test.

- OUVERTS : (1) canal de classe dormant -- le send()
  d'une classe devrait chercher SES classMethods
  d'abord puis le socle Class, exactement comme la
  « Resolution des messages » du document le
  specifie ; tests de regression a prevoir.
  (2) Accesseurs unifies (priorite instVars puis
  classVars, la premiere trouvee gagne) ou separes
  (classAccessors:) ; une classVar generee des deux
  cotes. (3) Nommage des classes dans le source
  (portee racine / rabat du registre / dictionnaire
  global) -- verrou REPL du kit. (4) <- d'affectation
  promis par le document, jamais implante.

### 09/11/2026

- Dettes d'hier soldees :
  * TESTS DU CANAL DE CLASSE : une methode de
    classe specifique repond (le canal
    lookupClassMethod, longtemps dormant, est
    desormais EMPRUNTE par la suite) ; elle BAT
    le socle (superclass redirige sur Integer,
    les autres classes gardent le socle). Le
    send() des classes n'a plus de branche
    morte. 197 verts.
  * ARCHITECTURE : derniere ligne -> restante
    (Etat des decisions) remplacee par >>.
  * JOURNAL : la formule d'entre-nous remplacee
    partout par LA DERNIERE DECLARATION L'EMPORTE
    SUR LES PRECEDENTES.
  * REFERENCE : shl:/shr: dans le protocole
    Integer -- CORRECTION d'une inversion du
    brouillon (shl: = gauche = x2 ; shr: =
    droite = /2, quotient entier) ; note de
    priorite : messages a mot-cle, la plus
    basse, parentheses avec l'arithmetique.

- accessors: UNIFIE : la table de declaration dit
  le COTE de l'installation (instVars -> instance,
  classVars -> classe seule). Le RECEVEUR
  desambiguise : un selecteur ne vit que d'un
  cote, la collision homonyme est impossible.
  Divergence assumee avec Smalltalk : les
  instances n'ont pas d'acces implicite aux
  variables de classe ; la voie explicite est
  p class Population. Plus de regle de priorite.
  Le canal de classe, eveille ce matin, travaille
  des aujourd'hui pour les variables de classe.

- Tests : la classVar repond au receveur classe
  (nee a nil, setter rend la valeur) ; p Population
  = Message non compris, la divergence testee
  comme une PROPRIETE ; la chimere (instVar a +
  classVar a) est tranchee par le receveur seul.
  200 verts.
  
- Poussieres : commentaire menteur du getter
  corrige, diablerie purgee du code (la phrase
  d'entre-nous remplacee partout par LA DERNIERE
  DECLARATION L'EMPORTE).

### 10/11/2026

- Famille to: COMPLETE : to:do: et to:by:do: --
  iteration par bloc SANS fabriquer de tableau.
  Convention do: (comme Array) : le RECEVEUR est
  rendu ; bornes mal orientees : le bloc n'est
  JAMAIS invoque (symetrique du tableau vide) ;
  pas nul : MTException explicite. Placement :
  to:do: entre to: et to:by: (la version simple
  avant la version a pas).
- MTIntegerPrimitivesTest REVEILLE : le fichier
  d'octobre (7 tests de nos premiers pas) devient
  LA suite d'Integer par messages, 13 tests.
  Regle d'etage des tests : la suite interpreter
  prouve la SURFACE (la grammaire), les suites
  runtime prouvent la SEMANTIQUE (les messages).
  206 verts.
  
## Prochain objectif

### Notes pour plus tard

- MTSymbol.intern est le dernier static d'etat du runtime.
  Decision assumee : les symboles sont l'identite du langage,
  pas l'etat d'un monde. (A differencier des fabriques, qui
  vivent dans MTContext.)

### Prochains objectifs

1. Le confort immédiat (petits chantiers, effets visibles chaque session)

  - Dictionary : 
    * at:ifAbsent:, at:ifAbsentPut:,
    * l'affichage complet (on en est à Dictionary(3) — un affichage lisible change le REPL),
    * peut-être keysDo:/associationsDo:.

  - Integer : 
    * abs, even/odd, min:/max:,
    * et surtout to:do: — itérer par bloc au lieu de fabriquer un tableau : 1 to: 10 do: [:i | ...]. C'est le genre de message qui rend le langage fluide.

  - String :
    * asUppercase/asLowercase, trim, 
    * sous-chaînes (copyFrom:to:), indexOf:, replaceAll:with:.
      Rien ne fatigue plus que de ne pas pouvoir tailler une chaîne.

  - Array :
    * detect:ifNone:,
    * inject:into: (le couteau suisse),
    *  with:do:, asSorted, la concaténation ,.

  - Les finitions promises au journal : 
    * le message « Echec primitive » qui nomme le sélecteur MiniTalk au lieu du nom Java,
    *  le piège REPL $',
    * ifNil: ifNotNil:.

2. Le grand chantier — les classes définies en MiniTalk

C'est LE chantier qui transforme le langage : aujourd'hui, tout vit en Java (primitives) ; un utilisateur ne peut pas créer Point. Il faudra :

- une syntaxe de création (Integer subclass: #Point ... façon Smalltalk, ou une autre forme à inventer) ;
- la définition de méthodes en MiniTalk — grammaire + compilation vers addInstMethod (et là, votre réflexivité d'aujourd'hui devient le moteur : replaceInstMethod sert à chaud) ;
- les variables d'instance (instVars existent déjà dans MTClass, elles attendent),
- self, super, le ^retour, les cascades.
- ajout de gestion et utilisation de bases de données sqlite3, h2, hsqldb,
  etc.
  
  **Note :** h2 et/ou hsqldb permettent l'usage de bases en fichiers locaux
  en mémoire, ou sur serveur distant gérant le multi accès.
  
C'est gros, c'est passionnant, et tout ce qu'on a semé y converge : l'AST (méthodes = données), la réflexivité (muter à chaud), la persistance (classes = schéma léger, la question ouverte de Q1).

3. L'infrastructure douce

- Charger un fichier : MTInterpreter.evaluate sait évaluer une source ; il manque juste un point d'entrée « exécute ce fichier .mt » et le REPL peut charger des scripts. Petit, et ça change la vie.

- La réification : instMethodAt: au langage + objet-méthode cousin du Block — le décorateur de vendredi s'écrit alors en MiniTalk.

- La Référence à jour : elle accuse le retard sur Dictionary, la réflexivité, la règle sélecteur/méthode (avec son épigraphe Van Vogt 😄).


4. L'horizon (déjà tracé)

La persistance SGBD (Q3, Q5 restantes), les blocs porteurs d'AST (transmissibilité), la VM bytecode de votre projet myVM.




