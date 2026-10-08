# Journal de développement MiniTalk V3

## Etat courant

- Bootstrap Object/Class
- MTSymbol
- Dispatch de messages
- MTObjectPrimitives
    - identity
    - class
- MTNil

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

### Prochain objectif

- Rattacher MTNil au système de classes MiniTalk.
- Réduire progressivement l'utilisation de null dans le runtime.