package mt.runtime.primitives;

import mt.runtime.MTObject;

public final class MTBlockPrimitives {

    private MTBlockPrimitives() {
    }

    /** Evaluation sans argument : [ ... ] value */
    @Primitive("value")
    @Primitive("value:")
    @Primitive("value:value:")
    public static MTObject value(
            MTObject receiver,
            MTObject... arguments) {

        return ((mt.runtime.MTBlock) receiver)
            .invoke(receiver, arguments);
    }

    /**
     * Boucle : [condition] whileTrue: [corps]
     * La condition est reevaluee a chaque tour.
     * Retourne le nombre d'iterations effectuees
     * (0 si le corps n'a jamais tourne) -- libre au
     * developpeur d'ignorer cette valeur.
     */
    /**
     * Verite MiniTalk : strictement un booleen.
     * Tout autre objet est une erreur explicite.
     */
    private static boolean truth(MTObject value) {

        if (value instanceof mt.runtime.MTBoolean b) {
            return b.javaValue();
        }

        throw new mt.runtime.exceptions.MTException(
            "Condition invalide : " + value
            + " n'est pas un Boolean");
    }

    /**
     * Boucle : [condition] whileTrue: [corps]
     * La condition est reevaluee a chaque tour.
     * Retourne le nombre d'iterations effectuees.
     */
    @Primitive("whileTrue:")
    public static MTObject whileTrue(
            MTObject receiver,
            MTObject... arguments) {

        mt.runtime.MTBlock condition =
            (mt.runtime.MTBlock) receiver;

        mt.runtime.MTBlock body =
            (mt.runtime.MTBlock) arguments[0];

        int iterations = 0;

        while (truth(condition.invoke(receiver))) {
            body.invoke(receiver);
            iterations++;
        }

        return condition.getMTClass()
            .getContext()
            .newInteger(iterations);
    }
    

    /** Un bloc ne sait pas encore se decrire : asString */
    @Primitive("asString")
    public static MTObject asString(
            MTObject receiver,
            MTObject... arguments) {

        return receiver.asString();
    }
}
