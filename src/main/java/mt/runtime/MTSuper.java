package mt.runtime;

import mt.runtime.exceptions.MTException;

/**
 * (28/11) La pseudo-variable super : une VUE de
 * dispatch, pas une valeur. Seme par le corps de
 * methode (MTBlockMethod.invoke), elle retient le
 * receveur REEL et la classe DEFINISSANTE.
 *
 * Semantique (design acte le 26/11) : la recherche
 * part de la superclasse de la classe ou la methode
 * est ECRITE -- jamais de la classe du receveur.
 * Le meme corps, herite, se comporte pareil partout :
 * si Etudiant >> maMethode dit super maMethode, un
 * Stagiaire qui l'herite part toujours de Personne,
 * jamais d'Etudiant -- pas de recursion possible.
 *
 * super traverse la table DU LANGAGE (qui contient
 * aussi les primitives deposees au bootstrap, double
 * depot du 19/11) : super printString dans une
 * surcharge trouve la primitive du parent. Le @ reste
 * la fenetre exacte sur le plancher Java (loi du
 * decorateur) ; les deux ne se concurrencent pas.
 *
 * super n'est pas une valeur : asString refuse,
 * l'affectation refuse (garde de MTAssignmentNode),
 * et hors d'une methode le nom n'existe pas
 * (refus propre de MTIdentifierNode).
 */
public final class MTSuper
        implements MTObject {

    /** Le receveur reel, self de la methode. */
    private final MTObject receiver;

    /** La classe ou la methode est ECRITE. */
    private final MTClass owner;

    /** true : vue de methode de classe. */
    private final boolean classSide;

    public MTSuper(
            MTObject receiver,
            MTClass owner,
            boolean classSide) {

        this.receiver = receiver;
        this.owner = owner;
        this.classSide = classSide;
    }

    @Override
    public MTClass getMTClass() {
        return receiver.getMTClass();
    }

    @Override
    public MTObject send(
            MTSymbol selector,
            MTObject... arguments) {

        // La recherche part du PARENT de la classe
        // definissante : la table de owner est sautee.
        MTClass start = owner.getSuperclass();

        if (start == null) {
            throw new MTException(
                "super : pas de superclasse ("
                + owner.getName()
                + " est a la racine)");
        }

        MTMethod method = classSide
            ? start.lookupClassMethod(selector)
            : start.lookupInstanceMethod(selector);

        // Cote classe, meme secours que MTClass.send :
        // le socle commun (superclass, new, ...)
        // vit chez Class en methodes d'instance.
        if (method == null && classSide) {
            method = start.getMTClass()
                .lookupInstanceMethod(selector);
        }

        if (method == null) {
            throw new MTException(
                "super : message non compris : "
                + owner.getName()
                + " >> " + selector);
        }

        return method.invoke(receiver, arguments);
    }

    @Override
    public MTString asString() {
        throw new MTException(
            "super : pseudo-variable, pas une valeur");
    }
}
