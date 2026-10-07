# Journal de développement MiniTalk V3

## 05/10/2026

Ouverture d'un journal pour noter l'état d'avancement du projet.

### ETAT ACTUEL


✅ Maven configuré

✅ Java 25 configuré

✅ VS Code opérationnel

✅ Git / GitHub / SSH opérationnels

✅ Bootstrap Object/Class

    Object.class = Class
    Class.class = Class

✅ MTSymbol

    intern("name")

    s1 == s2 => true

✅ Lookup

    MTClass.lookupInstanceMethod()

    recherche :
        classe courante
        -> superclasse
        -> ...
        -> null

    testé sur méthode absente


### PROCHAIN OBJECTIF

Enregistrer une méthode dans une classe
et vérifier que le lookup la retrouve.

Pas d'invocation.
Pas de parser.
Pas d'AST.
Pas de métaclasse.
Pas de streams.
Pas de process.

Seulement :

    MTClass
        addInstanceMethod()

    +
    
    lookupInstanceMethod()

retourne effectivement la méthode enregistrée.


### TEST ATTENDU

objectClass.addInstanceMethod(
    testSelector,
    ...)
    
lookupInstanceMethod(testSelector)

=> méthode trouvée


### OBJECTIF SUIVANT (pas maintenant)

Brancher MTObject.send()
sur le mécanisme de lookup.

## 06/10/2026

### ETAT ACTUEL

✅ Maven configuré

✅ Java 25 configuré

✅ VS Code opérationnel

✅ Git / GitHub / SSH opérationnels

✅ Bootstrap Object/Class

    Object.class = Class
    Class.class = Class

✅ MTSymbol

    intern("name")

    s1 == s2 => true

✅ Lookup

    lookupInstanceMethod()

    recherche dans :
        classe courante
        -> superclasse
        -> ...

✅ Enregistrement de méthodes

    addInstanceMethod()

✅ Lookup de méthodes enregistrées

    lookup(test) != null => true

✅ Invocation de méthodes

    MTMethod.invoke(...)

✅ Envoi de messages

    MTInstance.send(...)

✅ Retour de valeur

    result == receiver
        => true

Chaîne validée :

    send()
        -> lookup()
        -> invoke()
        -> return


### PROCHAIN OBJECTIF

Nettoyage du modèle d'exceptions.

Question ouverte :

    MTException
        extends Exception

ou

    MTException
        extends RuntimeException


### TEST ATTENDU

Les appels :

    send(...)
    invoke(...)

ne nécessitent plus de :

    try / catch

systématiques dans le runtime.


### OBJECTIF SUIVANT (pas maintenant)

Première méthode native utile.

Exemples possibles :

    identity

ou

    class


### ENSEIGNEMENTS

- MiniTalk n'est pas Smalltalk.
- Un objectif = un test = un commit.
- Construire le runtime avant le parser.
- Les symboles sont internés.
- Le dispatch de messages fonctionne :

      receiver
          -> send()
          -> lookup()
          -> invoke()
          -> return

## 07/10/2026

### ETAT ACTUEL

✅ Maven configuré

✅ Java 25 configuré

✅ VS Code opérationnel

✅ Git / GitHub / SSH opérationnels

✅ Bootstrap Object/Class

    Object.class = Class
    Class.class = Class

✅ MTSymbol

    intern("name")

    s1 == s2 => true

✅ Lookup

    lookupInstanceMethod()

    recherche dans :
        classe courante
        -> superclasse
        -> ...

✅ Enregistrement de méthodes

    addInstanceMethod()

✅ Lookup de méthodes enregistrées

    lookup(test) != null => true

✅ Invocation de méthodes

    MTMethod.invoke(...)

✅ Envoi de messages

    MTInstance.send(...)

✅ Retour de valeur

    result == receiver
        => true

Chaîne validée :

    send()
        -> lookup()
        -> invoke()
        -> return

✅ MTException

    hérite de RuntimeException

✅ Simplification du runtime

    suppression des throws MTException
    suppression des try/catch inutiles

✅ Première primitive native

    Object>>identity

      obj identity

      => obj


      result == obj
    
      => true
    
✅ Première primitive native

    Object>>identity

✅ Deuxième primitive native

    Object>>class
    
      obj identity == obj
      => true

      obj class == Object
      => true
