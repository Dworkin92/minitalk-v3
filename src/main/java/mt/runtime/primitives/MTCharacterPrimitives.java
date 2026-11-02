package mt.runtime.primitives;

import mt.runtime.MTBoolean;
import mt.runtime.MTCharacter;
import mt.runtime.MTContext;
import mt.runtime.MTInteger;
import mt.runtime.MTObject;
import mt.runtime.MTString;
import mt.runtime.exceptions.MTException;

public final class MTCharacterPrimitives {

    private MTCharacterPrimitives() {
    }

    private static MTContext context(MTObject receiver) {
        return receiver.getMTClass().getContext();
    }

    /** Cast durci (02/11), format uniforme */
    private static MTCharacter asCharacter(
            MTObject value,
            String selector) {

        if (value instanceof MTCharacter c) {
            return c;
        }

        throw new MTException(
            value + " n'est pas un Character (" + selector + ")");
    }

    /** Cast durci : le nombre de repetitions */
    private static MTInteger asInteger(
            MTObject value,
            String selector) {

        if (value instanceof MTInteger i) {
            return i;
        }

        throw new MTException(
            value + " n'est pas un Integer (" + selector + ")");
    }

    //
    // Concatenation
    //

    /**
     * $a , $b -> 'ab' ; $a , 'bc' -> 'abc'.
     * Le resultat d'une concatenation est TOUJOURS une
     * String -- le Character se fond dedans.
     */
    @Primitive(",")
    public static MTObject concatenate(
            MTObject receiver,
            MTObject... arguments) {

        MTCharacter self = (MTCharacter) receiver;
        MTObject other = arguments[0];

        StringBuilder result = new StringBuilder(
            String.valueOf(
                Character.toChars(self.javaValue())));

        if (other instanceof MTString s) {
            result.append(s.javaValue());
        } else if (other instanceof MTCharacter c) {
            result.append(
                Character.toChars(c.javaValue()));
        } else {
            throw new MTException(
                "Concatenation invalide : " + other
                + " n'est ni une String ni un Character (,)");
        }

        return context(self).newString(
            result.toString());
    }

    /**
     * $a * 5 -> 'aaaaa'. Repetition : le dual de la
     * multiplication entiere. n entier >= 0 ;
     * negatif = erreur explicite.
     */
    @Primitive("*")
    public static MTObject times(
            MTObject receiver,
            MTObject... arguments) {

        MTCharacter self = (MTCharacter) receiver;
        int count =
            asInteger(arguments[0], "*").javaValue();

        if (count < 0) {
            throw new MTException(
                "Repetition negative : " + count);
        }

        StringBuilder result = new StringBuilder();

        for (int i = 0; i < count; i++) {
            result.append(
                Character.toChars(self.javaValue()));
        }

        return context(self).newString(
            result.toString());
    }

    //
    // Comparaisons
    //

    /** Egalite par codepoint : $a = $a est vrai */
    @Primitive("=")
    public static MTObject equals(
            MTObject receiver,
            MTObject... arguments) {

        MTCharacter self = (MTCharacter) receiver;
        MTCharacter other = asCharacter(arguments[0], "=");

        return context(self).newBoolean(
            self.javaValue() == other.javaValue());
    }

    @Primitive("==")
    public static MTObject sameAs(
            MTObject receiver,
            MTObject... arguments) {

        // Identite stricte, comme Integer et Float.
        return context(receiver).newBoolean(
            receiver.identityEquals(arguments[0]));
    }

    //
    // Conversions
    //

    @Primitive("asString")
    public static MTObject asString(
            MTObject receiver,
            MTObject... arguments) {

        return ((MTCharacter) receiver).asString();
    }

    /** Codepoint : $h asInteger = 104 */
    @Primitive("asInteger")
    public static MTObject asInteger(
            MTObject receiver,
            MTObject... arguments) {

        MTCharacter self = (MTCharacter) receiver;
        return context(self).newInteger(self.javaValue());
    }

    @Primitive("yourself")
    public static MTObject yourself(
            MTObject receiver,
            MTObject... arguments) {

        return receiver;
    }
}
