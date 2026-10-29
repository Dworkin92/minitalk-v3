package mt.runtime.primitives;

import java.util.ArrayList;
import java.util.List;

import mt.runtime.MTArray;
import mt.runtime.MTBoolean;
import mt.runtime.MTContext;
import mt.runtime.MTFloat;
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
        MTObject other = arguments[0];

        // Promotion locale : un Float a droite fait
        // passer le resultat en Float (decision 29/10).
        if (other instanceof MTFloat f) {
            return context(self).newFloat(
                self.javaValue() + f.javaValue());
        }

        MTInteger o = (MTInteger) other;
        return context(self).newInteger(
            self.javaValue() + o.javaValue());
    }

    @Primitive("-")
    public static MTObject minus(
            MTObject receiver,
            MTObject... arguments) {

        MTInteger self = (MTInteger) receiver;
        MTObject other = arguments[0];

        if (other instanceof MTFloat f) {
            return context(self).newFloat(
                self.javaValue() - f.javaValue());
        }

        MTInteger o = (MTInteger) other;
        return context(self).newInteger(
            self.javaValue() - o.javaValue());
    }

    @Primitive("*")
    public static MTObject times(
            MTObject receiver,
            MTObject... arguments) {

        MTInteger self = (MTInteger) receiver;
        MTObject other = arguments[0];

        if (other instanceof MTFloat f) {
            return context(self).newFloat(
                self.javaValue() * f.javaValue());
        }

        MTInteger o = (MTInteger) other;
        return context(self).newInteger(
            self.javaValue() * o.javaValue());
    }

    /**
     * Division entiere (quotient) quand les deux
     * operandes sont entiers : 7 / 3 = 2.
     * Promotion en Float si l'argument est un Float :
     * 7 / 3.0 = 2.333... -- c'est au developpeur d'ecrire
     * 7 asFloat / 3 s'il veut le quotient reel d'une
     * division d'entiers (promotion LOCALE, 29/10).
     */
    @Primitive("/")
    public static MTObject divide(
            MTObject receiver,
            MTObject... arguments) {

        MTInteger self = (MTInteger) receiver;
        MTObject other = arguments[0];

        if (other instanceof MTFloat f) {
            if (f.javaValue() == 0.0) {
                throw new mt.runtime.exceptions.MTException(
                    "Division par zero : " + self + " / 0.0");
            }
            return context(self).newFloat(
                self.javaValue() / f.javaValue());
        }

        MTInteger o = (MTInteger) other;

        if (o.javaValue() == 0) {
            throw new mt.runtime.exceptions.MTException(
                "Division par zero : " + self + " / 0");
        }

        return context(self).newInteger(
            self.javaValue() / o.javaValue());
    }

    /** Modulo : reste de la division (promotion Float idem) */
    @Primitive("%")
    public static MTObject modulo(
            MTObject receiver,
            MTObject... arguments) {

        MTInteger self = (MTInteger) receiver;
        MTObject other = arguments[0];

        if (other instanceof MTFloat f) {
            if (f.javaValue() == 0.0) {
                throw new mt.runtime.exceptions.MTException(
                    "Division par zero : " + self + " % 0.0");
            }
            return context(self).newFloat(
                self.javaValue() % f.javaValue());
        }

        MTInteger o = (MTInteger) other;

        if (o.javaValue() == 0) {
            throw new mt.runtime.exceptions.MTException(
                "Division par zero : " + self + " % 0");
        }

        return context(self).newInteger(
            self.javaValue() % o.javaValue());
    }

    /**
     * Puissance : 2 ** 7 en Integer ; 2 ** 0.5 en Float
     * (racine carree) des qu'un Float est present -- pas
     * de cast (int) Math.pow dans ce cas (decision 29/10).
     */
    @Primitive("**")
    public static MTObject power(
            MTObject receiver,
            MTObject... arguments) {

        MTInteger self = (MTInteger) receiver;
        MTObject other = arguments[0];

        if (other instanceof MTFloat f) {
            return context(self).newFloat(
                Math.pow(self.javaValue(), f.javaValue()));
        }

        MTInteger o = (MTInteger) other;
        return context(self).newInteger(
            (int) Math.pow(self.javaValue(), o.javaValue()));
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
                i <= last.javaValue(); i++) {
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
        MTObject other = arguments[0];

        // Egalite numerique : 2 = 2.0 est vrai
        // (decision du 29/10, comme Smalltalk).
        if (other instanceof MTFloat f) {
            return context(self).newBoolean(
                self.javaValue() == f.javaValue());
        }

        MTInteger o = (MTInteger) other;
        return context(self).newBoolean(
            self.javaValue() == o.javaValue());
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
        MTObject other = arguments[0];

        if (other instanceof MTFloat f) {
            return context(self).newBoolean(
                self.javaValue() < f.javaValue());
        }

        MTInteger o = (MTInteger) other;
        return context(self).newBoolean(
            self.javaValue() < o.javaValue());
    }

    @Primitive(">")
    public static MTObject greaterThan(
            MTObject receiver,
            MTObject... arguments) {

        MTInteger self = (MTInteger) receiver;
        MTObject other = arguments[0];

        if (other instanceof MTFloat f) {
            return context(self).newBoolean(
                self.javaValue() > f.javaValue());
        }

        MTInteger o = (MTInteger) other;
        return context(self).newBoolean(
            self.javaValue() > o.javaValue());
    }

    @Primitive("<=")
    public static MTObject lessOrEqual(
            MTObject receiver,
            MTObject... arguments) {

        MTInteger self = (MTInteger) receiver;
        MTObject other = arguments[0];

        if (other instanceof MTFloat f) {
            return context(self).newBoolean(
                self.javaValue() <= f.javaValue());
        }

        MTInteger o = (MTInteger) other;
        return context(self).newBoolean(
            self.javaValue() <= o.javaValue());
    }

    @Primitive(">=")
    public static MTObject greaterOrEqual(
            MTObject receiver,
            MTObject... arguments) {

        MTInteger self = (MTInteger) receiver;
        MTObject other = arguments[0];

        if (other instanceof MTFloat f) {
            return context(self).newBoolean(
                self.javaValue() >= f.javaValue());
        }

        MTInteger o = (MTInteger) other;
        return context(self).newBoolean(
            self.javaValue() >= o.javaValue());
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
     * Conversion explicite : 7 asFloat = 7.0.
     * C'est LE message pour demander une division
     * reelle d'entiers : a asFloat / b.
     */
    @Primitive("asFloat")
    public static MTObject asFloat(
            MTObject receiver,
            MTObject... arguments) {

        return context(receiver).newFloat(
            ((MTInteger) receiver).javaValue());
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
