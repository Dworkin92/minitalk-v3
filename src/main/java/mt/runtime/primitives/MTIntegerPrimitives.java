package mt.runtime.primitives;

import java.util.ArrayList;
import java.util.List;

import mt.runtime.MTArray;
import mt.runtime.MTBoolean;
import mt.runtime.MTContext;
import mt.runtime.MTInteger;
import mt.runtime.MTObject;

public final class MTIntegerPrimitives {

    private MTIntegerPrimitives() {
    }

    private static MTContext context(MTObject receiver) {
        return receiver.getMTClass().getContext();
    }

    //
    // Arithmetique
    //

    @Primitive("+")
    public static MTObject plus(
            MTObject receiver,
            MTObject... arguments) {

        MTInteger self = (MTInteger) receiver;
        MTInteger other = (MTInteger) arguments[0];

        return context(self).newInteger(self.javaValue() + other.javaValue());
    }

    @Primitive("-")
    public static MTObject minus(
            MTObject receiver,
            MTObject... arguments) {

        MTInteger self = (MTInteger) receiver;
        MTInteger other = (MTInteger) arguments[0];

        return context(self).newInteger(self.javaValue() - other.javaValue());
    }

    @Primitive("*")
    public static MTObject times(
            MTObject receiver,
            MTObject... arguments) {

        MTInteger self = (MTInteger) receiver;
        MTInteger other = (MTInteger) arguments[0];

        return context(self).newInteger(self.javaValue() * other.javaValue());
    }

    /**
     * Division entiere (quotient), comme en Smalltalk.
     * La division par zero leve une MTException.
     */
    @Primitive("/")
    public static MTObject divide(
            MTObject receiver,
            MTObject... arguments) {

        MTInteger self = (MTInteger) receiver;
        MTInteger other = (MTInteger) arguments[0];

        if (other.javaValue() == 0) {
            throw new mt.runtime.exceptions.MTException(
                "Division par zero : " + self + " / 0");
        }

        return context(self).newInteger(self.javaValue() / other.javaValue());
    }

    /** Modulo : reste de la division entiere */
    @Primitive("%")
    public static MTObject modulo(
            MTObject receiver, MTObject... arguments) {

        MTInteger left = (MTInteger) receiver;
        MTInteger right = (MTInteger) arguments[0];

        if (right.javaValue() == 0) {
            throw new mt.runtime.exceptions.MTException(
                "Division par zero");
        }

        return context(receiver).newInteger(
            left.javaValue() % right.javaValue());
    }

    /** Puissance : 2 ** 7 */
    @Primitive("**")
    public static MTObject power(
            MTObject receiver, MTObject... arguments) {

        MTInteger left = (MTInteger) receiver;
        MTInteger right = (MTInteger) arguments[0];

        return context(receiver).newInteger(
            (int) Math.pow(left.javaValue(),
                           right.javaValue()));
    }

    @Primitive("negated")
    public static MTObject negated(
            MTObject receiver,
            MTObject... arguments) {

        MTInteger self = (MTInteger) receiver;

        return context(self).newInteger(-self.javaValue());
    }

    //
    // Intervalles
    //

    /**
     * 1 to: 10 -> #(1 2 ... 10), inclusif.
     * "Tout est message" : pas de syntaxe dediee (decision
     * du 25/10). 5 to: 1 donne un tableau vide.
     * to:by: viendra plus tard, par le meme canal.
     */
    @Primitive("to:")
    public static MTObject to(
            MTObject receiver,
            MTObject... arguments) {

        MTInteger self = (MTInteger) receiver;
        MTInteger last = (MTInteger) arguments[0];

        List<MTObject> elements = new ArrayList<>();

        for (int i = self.javaValue();
                i <= last.javaValue();
                i++) {
            elements.add(
                context(receiver).newInteger(i));
        }

        return context(receiver).newArray(elements);
    }

    //
    // Comparaisons
    //

    @Primitive("=")
    public static MTObject equals(
            MTObject receiver,
            MTObject... arguments) {

        MTInteger self = (MTInteger) receiver;
        MTInteger other = (MTInteger) arguments[0];

        return context(self).newBoolean(self.javaValue() == other.javaValue());
    }

    @Primitive("==")
    public static MTObject sameAs(
            MTObject receiver,
            MTObject... arguments) {

        // Identite : deux MTInteger de meme valeur ne sont
        // PAS le meme objet (pas de cache). == reste strict.
        return context(receiver).newBoolean(receiver.identityEquals(arguments[0]));
    }

    @Primitive("<")
    public static MTObject lessThan(
            MTObject receiver,
            MTObject... arguments) {

        MTInteger self = (MTInteger) receiver;
        MTInteger other = (MTInteger) arguments[0];

        return context(self).newBoolean(self.javaValue() < other.javaValue());
    }

    @Primitive(">")
    public static MTObject greaterThan(
            MTObject receiver,
            MTObject... arguments) {

        MTInteger self = (MTInteger) receiver;
        MTInteger other = (MTInteger) arguments[0];

        return context(self).newBoolean(self.javaValue() > other.javaValue());
    }

    @Primitive("<=")
    public static MTObject lessOrEqual(
            MTObject receiver,
            MTObject... arguments) {

        MTInteger self = (MTInteger) receiver;
        MTInteger other = (MTInteger) arguments[0];

        return context(self).newBoolean(self.javaValue() <= other.javaValue());
    }

    @Primitive(">=")
    public static MTObject greaterOrEqual(
            MTObject receiver,
            MTObject... arguments) {

        MTInteger self = (MTInteger) receiver;
        MTInteger other = (MTInteger) arguments[0];

        return context(self).newBoolean(self.javaValue() >= other.javaValue());
    }

    //
    // Conversions
    //

    @Primitive("asString")
    public static MTObject asString(
            MTObject receiver,
            MTObject... arguments) {

        return ((MTInteger) receiver).asString();
    }

    /**
     * ramasse-miettes prevu pour plus tard ; retourne le
     * recepteur pour l'instant.
     */
    @Primitive("yourself")
    public static MTObject yourself(
            MTObject receiver,
            MTObject... arguments) {

        return receiver;
    }
}
