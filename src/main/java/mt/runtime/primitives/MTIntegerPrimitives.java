package mt.runtime.primitives;

import mt.runtime.MTBoolean;
import mt.runtime.MTInteger;
import mt.runtime.MTNil;
import mt.runtime.MTObject;

public final class MTIntegerPrimitives {

    private MTIntegerPrimitives() {
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

        return MTInteger.of(self.javaValue() + other.javaValue());
    }

    @Primitive("-")
    public static MTObject minus(
            MTObject receiver,
            MTObject... arguments) {

        MTInteger self = (MTInteger) receiver;
        MTInteger other = (MTInteger) arguments[0];

        return MTInteger.of(self.javaValue() - other.javaValue());
    }

    @Primitive("*")
    public static MTObject times(
            MTObject receiver,
            MTObject... arguments) {

        MTInteger self = (MTInteger) receiver;
        MTInteger other = (MTInteger) arguments[0];

        return MTInteger.of(self.javaValue() * other.javaValue());
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

        return MTInteger.of(self.javaValue() / other.javaValue());
    }

    @Primitive("%")
    public static MTObject modulo(
            MTObject receiver,
            MTObject... arguments) {

        MTInteger self = (MTInteger) receiver;
        MTInteger other = (MTInteger) arguments[0];

        return MTInteger.of(self.javaValue() % other.javaValue());
    }

    @Primitive("negated")
    public static MTObject negated(
            MTObject receiver,
            MTObject... arguments) {

        MTInteger self = (MTInteger) receiver;

        return MTInteger.of(-self.javaValue());
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

        return MTBoolean.of(self.javaValue() == other.javaValue());
    }

    @Primitive("==")
    public static MTObject sameAs(
            MTObject receiver,
            MTObject... arguments) {

        // Identite : deux MTInteger de meme valeur ne sont
        // PAS le meme objet (pas de cache). == reste strict.
        return MTBoolean.of(receiver.identityEquals(arguments[0]));
    }

    @Primitive("<")
    public static MTObject lessThan(
            MTObject receiver,
            MTObject... arguments) {

        MTInteger self = (MTInteger) receiver;
        MTInteger other = (MTInteger) arguments[0];

        return MTBoolean.of(self.javaValue() < other.javaValue());
    }

    @Primitive(">")
    public static MTObject greaterThan(
            MTObject receiver,
            MTObject... arguments) {

        MTInteger self = (MTInteger) receiver;
        MTInteger other = (MTInteger) arguments[0];

        return MTBoolean.of(self.javaValue() > other.javaValue());
    }

    @Primitive("<=")
    public static MTObject lessOrEqual(
            MTObject receiver,
            MTObject... arguments) {

        MTInteger self = (MTInteger) receiver;
        MTInteger other = (MTInteger) arguments[0];

        return MTBoolean.of(self.javaValue() <= other.javaValue());
    }

    @Primitive(">=")
    public static MTObject greaterOrEqual(
            MTObject receiver,
            MTObject... arguments) {

        MTInteger self = (MTInteger) receiver;
        MTInteger other = (MTInteger) arguments[0];

        return MTBoolean.of(self.javaValue() >= other.javaValue());
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
