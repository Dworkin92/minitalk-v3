package mt.runtime.primitives;

import mt.runtime.MTBoolean;
import mt.runtime.MTContext;
import mt.runtime.MTFloat;
import mt.runtime.MTInteger;
import mt.runtime.MTObject;
import mt.runtime.exceptions.MTException;

/**
 * Primitives Float. Promotion locale (decision du 29/10) :
 * un operande Integer est converti en double ; l'operation
 * est flottante des qu'un Float est present d'un cote.
 *
 * Pas de verite a la C : les comparaisons repondent un
 * Boolean, strictement.
 */
public final class MTFloatPrimitives {

    private MTFloatPrimitives() {
    }

    private static MTContext context(MTObject receiver) {
        return receiver.getMTClass().getContext();
    }

    /**
     * Operande numerique : Integer ou Float, promu
     * en double. Tout le reste est une erreur explicite.
     */
    private static double numeric(MTObject value) {

        if (value instanceof MTInteger i) {
            return i.javaValue();
        }

        if (value instanceof MTFloat f) {
            return f.javaValue();
        }

        throw new MTException(
            "Operande invalide : " + value
            + " n'est ni un Integer ni un Float");
    }

    //
    // Arithmetique
    //

    @Primitive("+")
    public static MTObject plus(
            MTObject receiver,
            MTObject... arguments) {

        MTFloat self = (MTFloat) receiver;
        return context(self).newFloat(
            self.javaValue() + numeric(arguments[0]));
    }

    @Primitive("-")
    public static MTObject minus(
            MTObject receiver,
            MTObject... arguments) {

        MTFloat self = (MTFloat) receiver;
        return context(self).newFloat(
            self.javaValue() - numeric(arguments[0]));
    }

    @Primitive("*")
    public static MTObject times(
            MTObject receiver,
            MTObject... arguments) {

        MTFloat self = (MTFloat) receiver;
        return context(self).newFloat(
            self.javaValue() * numeric(arguments[0]));
    }

    /**
     * Division flottante. La division par zero (0.0 ou 0)
     * leve une MTException -- MiniTalk n'embrasse pas
     * l'infini IEEE, coherence avec Integer>>/.
     */
    @Primitive("/")
    public static MTObject divide(
            MTObject receiver,
            MTObject... arguments) {

        MTFloat self = (MTFloat) receiver;
        double divisor = numeric(arguments[0]);

        if (divisor == 0.0) {
            throw new MTException(
                "Division par zero : " + self + " / 0.0");
        }

        return context(self).newFloat(
            self.javaValue() / divisor);
    }

    /** Modulo flottant (reste IEEE, comme % de Java) */
    @Primitive("%")
    public static MTObject modulo(
            MTObject receiver,
            MTObject... arguments) {

        MTFloat self = (MTFloat) receiver;
        double divisor = numeric(arguments[0]);

        if (divisor == 0.0) {
            throw new MTException(
                "Division par zero : " + self + " % 0.0");
        }

        return context(self).newFloat(
            self.javaValue() % divisor);
    }

    /**
     * Puissance flottante : Math.pow sans cast, donc
     * 2.0 ** 0.5 est bien la racine carree
     * (decision du 29/10, pas de cast (int)).
     */
    @Primitive("**")
    public static MTObject power(
            MTObject receiver,
            MTObject... arguments) {

        MTFloat self = (MTFloat) receiver;
        return context(self).newFloat(
            Math.pow(self.javaValue(),
                     numeric(arguments[0])));
    }

    @Primitive("negated")
    public static MTObject negated(
            MTObject receiver,
            MTObject... arguments) {

        MTFloat self = (MTFloat) receiver;
        return context(self).newFloat(-self.javaValue());
    }

    //
    // Comparaisons (numeriques, decision du 29/10 :
    // 2.0 = 2 repond true)
    //

    @Primitive("=")
    public static MTObject equals(
            MTObject receiver,
            MTObject... arguments) {

        MTFloat self = (MTFloat) receiver;
        return context(self).newBoolean(
            self.javaValue() == numeric(arguments[0]));
    }

    @Primitive("==")
    public static MTObject sameAs(
            MTObject receiver,
            MTObject... arguments) {

        // Identite stricte : deux MTFloat de meme valeur
        // ne sont PAS le meme objet. == reste strict.
        return context(receiver).newBoolean(
            receiver.identityEquals(arguments[0]));
    }

    @Primitive("<")
    public static MTObject lessThan(
            MTObject receiver,
            MTObject... arguments) {

        MTFloat self = (MTFloat) receiver;
        return context(self).newBoolean(
            self.javaValue() < numeric(arguments[0]));
    }

    @Primitive(">")
    public static MTObject greaterThan(
            MTObject receiver,
            MTObject... arguments) {

        MTFloat self = (MTFloat) receiver;
        return context(self).newBoolean(
            self.javaValue() > numeric(arguments[0]));
    }

    @Primitive("<=")
    public static MTObject lessOrEqual(
            MTObject receiver,
            MTObject... arguments) {

        MTFloat self = (MTFloat) receiver;
        return context(self).newBoolean(
            self.javaValue() <= numeric(arguments[0]));
    }

    @Primitive(">=")
    public static MTObject greaterOrEqual(
            MTObject receiver,
            MTObject... arguments) {

        MTFloat self = (MTFloat) receiver;
        return context(self).newBoolean(
            self.javaValue() >= numeric(arguments[0]));
    }

    //
    // Conversions
    //

    @Primitive("asString")
    public static MTObject asString(
            MTObject receiver,
            MTObject... arguments) {

        return ((MTFloat) receiver).asString();
    }

    /**
     * Conversion explicite : troncature vers zero
     * (2.9 asInteger = 2, -2.9 asInteger = -2).
     */
    @Primitive("asInteger")
    public static MTObject asInteger(
            MTObject receiver,
            MTObject... arguments) {

        MTFloat self = (MTFloat) receiver;
        return context(self).newInteger(
            (int) self.javaValue());
    }

    @Primitive("yourself")
    public static MTObject yourself(
            MTObject receiver,
            MTObject... arguments) {

        return receiver;
    }
}
