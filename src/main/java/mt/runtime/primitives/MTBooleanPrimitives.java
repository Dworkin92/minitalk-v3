package mt.runtime.primitives;

import mt.runtime.MTBoolean;
import mt.runtime.MTContext;
import mt.runtime.MTMethod;
import mt.runtime.MTObject;
import mt.runtime.exceptions.MTException;

public final class MTBooleanPrimitives {

    private MTBooleanPrimitives() {
    }

    private static MTContext context(MTObject receiver) {
        return receiver.getMTClass().getContext();
    }

    @Primitive("ifTrue:")
    public static MTObject ifTrue(
            MTObject receiver,
            MTObject... arguments) {

        MTBoolean self = (MTBoolean) receiver;
        return self.javaValue()
                ? evaluate(arguments[0], receiver)
                : context(self).nil();
    }

    @Primitive("ifFalse:")
    public static MTObject ifFalse(
            MTObject receiver,
            MTObject... arguments) {

        MTBoolean self = (MTBoolean) receiver;
        return self.javaValue()
                ? context(self).nil()
                : evaluate(arguments[0], receiver);
    }

    @Primitive("ifTrue:ifFalse:")
    public static MTObject ifTrueIfFalse(
            MTObject receiver,
            MTObject... arguments) {

        MTBoolean self = (MTBoolean) receiver;
        return evaluate(
            self.javaValue() ? arguments[0] : arguments[1],
            receiver);
    }

    /**
     * Paresseux (29/10) : false and: X retourne false
     * sans jamais toucher X -- meme si X est un bloc
     * qui ferait une division par zero. Sinon X est
     * evalue (bloc ou Boolean) et son resultat doit
     * etre strictement un Boolean.
     */
    @Primitive("and:")
    public static MTObject and(
            MTObject receiver,
            MTObject... arguments) {

        MTBoolean self = (MTBoolean) receiver;
        if (!self.javaValue()) {
            return context(self).falseObject();
        }
        return context(self).newBoolean(
            truth(evaluate(arguments[0], receiver)));
    }

    /**
     * Paresseux (29/10) : true or: X retourne true
     * sans jamais toucher X. Symetrique de and:.
     */
    @Primitive("or:")
    public static MTObject or(
            MTObject receiver,
            MTObject... arguments) {

        MTBoolean self = (MTBoolean) receiver;
        if (self.javaValue()) {
            return context(self).trueObject();
        }
        return context(self).newBoolean(
            truth(evaluate(arguments[0], receiver)));
    }

    @Primitive("not")
    public static MTObject not(
            MTObject receiver,
            MTObject... arguments) {

        MTBoolean self = (MTBoolean) receiver;
        return context(self).newBoolean(!self.javaValue());
    }

    //
    // Aides
    //

    /**
     * Evalue un argument de type condition :
     * un MTMethod (bloc) est invoque,
     * toute autre valeur est retournee telle quelle.
     */
    private static MTObject evaluate(
            MTObject value,
            MTObject receiver) {

        if (value instanceof MTMethod method) {
            return method.invoke(receiver);
        }

        return value;
    }

    /**
     * Verite MiniTalk : strictement un booleen
     * (decision du 24/10, appliquee a and:/or:).
     */
    private static boolean truth(MTObject value) {

        if (value instanceof MTBoolean b) {
            return b.javaValue();
        }

        throw new MTException(
            "Condition invalide : " + value
            + " n'est pas un Boolean");
    }
}
