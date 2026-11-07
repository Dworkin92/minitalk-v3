package mt.runtime.primitives;

import mt.runtime.MTContext;
import mt.runtime.MTCharacter;
import mt.runtime.MTInteger;
import mt.runtime.MTObject;
import mt.runtime.MTString;
import mt.runtime.exceptions.MTException;

public final class MTStringPrimitives {

    private MTStringPrimitives() {
    }

    private static MTContext context(MTObject receiver) {
        return receiver.getMTClass().getContext();
    }

    /**
     * Cast durci (02/11) : format uniforme
     * <valeur> n'est pas une String (<selecteur>).
     */
    private static MTString asString(
            MTObject value,
            String selector) {

        if (value instanceof MTString s) {
            return s;
        }

        throw new MTException(
            value + " n'est pas une String (" + selector + ")");
    }

    /** Cast durci : l'indice doit etre un Integer */
    private static MTInteger asInteger(
            MTObject value,
            String selector) {

        if (value instanceof MTInteger i) {
            return i;
        }

        throw new MTException(
            value + " n'est pas un Integer (" + selector + ")");
    }

    /** Cast durci : includes: attend un caractere */
    private static MTCharacter asCharacter(
            MTObject value,
            String selector) {

        if (value instanceof MTCharacter c) {
            return c;
        }

        throw new MTException(
            value + " n'est pas un Character ("
            + selector + ")");
    }

    /** Concatenation : 'abc' , 'def' ou 'abc' , $d */
    @Primitive(",")
    public static MTObject concatenate(
            MTObject receiver,
            MTObject... arguments) {

        MTString self = (MTString) receiver;
        MTObject other = arguments[0];

        StringBuilder result =
            new StringBuilder(self.javaValue());

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

        return context(self).newString(result.toString());
    }

    @Primitive("size")
    public static MTObject size(
            MTObject receiver,
            MTObject... arguments) {

        MTString self = (MTString) receiver;

        return context(self).newInteger(self.javaValue().length());
    }

    @Primitive("isEmpty")
    public static MTObject isEmpty(
            MTObject receiver,
            MTObject... arguments) {

        MTString self = (MTString) receiver;

        return context(self).newBoolean(self.javaValue().isEmpty());
    }

    @Primitive("=")
    public static MTObject equals(
            MTObject receiver,
            MTObject... arguments) {

        MTString self = (MTString) receiver;
        MTString other = asString(arguments[0], "=");

        return context(self).newBoolean(
            self.javaValue().equals(other.javaValue()));
    }


    /**
     * Acces 1-based : 'abc' at: 2 -> $b.
     * MTCharacter depuis le 29/10.
     */
    @Primitive("at:")
    public static MTObject at(
            MTObject receiver,
            MTObject... arguments) {

        MTString self = (MTString) receiver;
        int index =
            asInteger(arguments[0], "at:").javaValue();

        if (index < 1 || index > self.javaValue().length()) {
            throw new MTException(
                "Index hors bornes : " + self
                + " at: " + index);
        }

        return context(self).newCharacter(
            self.javaValue().codePointAt(index - 1));
    }

    /** Position 1-based de la premiere occurrence, 0 si absente */
    @Primitive("indexOf:")
    public static MTObject indexOf(
            MTObject receiver,
            MTObject... arguments) {

        MTString string = (MTString) receiver;
        MTString sought = asString(arguments[0], "indexOf:");

        return context(receiver).newInteger(
            string.javaValue().indexOf(
                sought.javaValue()) + 1);
    }

    @Primitive("contains:")
    public static MTObject contains(
            MTObject receiver,
            MTObject... arguments) {

        MTString string = (MTString) receiver;
        MTString sought = asString(arguments[0], "contains:");

        return context(receiver).newBoolean(
            string.javaValue()
                .contains(sought.javaValue()));
    }

    /**
     * Appartenance d'un caractere (05/11) :
     * 'hello' includes: $h -> true. Symetrique de
     * contains: (sous-chaine) et de l'Array>>includes:
     * (element). STRICT : une String en argument est
     * une erreur explicite -- un selecteur, un sens
     * (orthogonalite, comme pour + et ,).
     */
    @Primitive("includes:")
    public static MTObject includes(
            MTObject receiver,
            MTObject... arguments) {

        MTString self = (MTString) receiver;
        MTCharacter sought =
            asCharacter(arguments[0], "includes:");

        String chars = self.javaValue();

        // Parcours par codepoints : les caracteres
        // supplementaires tiennent en deux chars.
        for (int i = 0; i < chars.length(); ) {
            int cp = chars.codePointAt(i);

            if (cp == sought.javaValue()) {
                return context(self).newBoolean(true);
            }

            i += Character.charCount(cp);
        }

        return context(self).newBoolean(false);
    }

    /** 'ab' * 3 -> 'ababab' (repetition, cf. Character) */
    @Primitive("*")
    public static MTObject times(
            MTObject receiver,
            MTObject... arguments) {

        MTString self = (MTString) receiver;
        int count =
            asInteger(arguments[0], "*").javaValue();

        if (count < 0) {
            throw new MTException(
                "Repetition negative : " + count);
        }

        return context(self).newString(
            self.javaValue().repeat(count));
    }

    /** 'hello' reverse = 'olleh' */
    @Primitive("reverse")
    public static MTObject reverse(
            MTObject receiver,
            MTObject... arguments) {

        MTString string = (MTString) receiver;

        return context(receiver).newString(
            new StringBuilder(string.javaValue())
                .reverse().toString());
    }

    @Primitive("uppercase")
    public static MTObject uppercase(
            MTObject receiver,
            MTObject... arguments) {

        MTString string = (MTString) receiver;

        return context(receiver).newString(
            string.javaValue().toUpperCase());
    }

    @Primitive("lowercase")
    public static MTObject lowercase(
            MTObject receiver,
            MTObject... arguments) {

        MTString string = (MTString) receiver;

        return context(receiver).newString(
            string.javaValue().toLowerCase());
    }

    @Primitive("asString")
    public static MTObject asString(
            MTObject receiver,
            MTObject... arguments) {

        return receiver;
    }
}
