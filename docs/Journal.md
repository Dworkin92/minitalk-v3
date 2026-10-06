# 05/10/2026

Ouverture d'un journal pour noter l'état d'avancement du projet.

## ETAT ACTUEL


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


## PROCHAIN OBJECTIF

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


## TEST ATTENDU

objectClass.addInstanceMethod(
    testSelector,
    ...)
    
lookupInstanceMethod(testSelector)

=> méthode trouvée


## OBJECTIF SUIVANT (pas maintenant)

Brancher MTObject.send()
sur le mécanisme de lookup.