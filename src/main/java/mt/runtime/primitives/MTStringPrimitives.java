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

    /**
     * (22/11) '  ab\u00A0' trim -> 'ab'. Coupe
     * l'espace blanc UNICODE, y compris les
     * INSECABLES (U+00A0...) : semantique Python
     * str.strip -- le strip() de Java les laisse
     * (isWhitespace les exclut par definition).
     */
    @Primitive("trim")
    public static MTObject trim(
            MTObject receiver,
            MTObject... arguments) {

        MTString string = (MTString) receiver;

        return context(receiver).newString(
            unicodeStrip(string.javaValue()));
    }

    /** L'espace blanc vrai : l'union des deux criteres Java */
    private static boolean estBlanc(int codepoint) {
        return Character.isWhitespace(codepoint)
            || Character.isSpaceChar(codepoint);
    }

    /** Coupe a gauche puis a droite, par codepoints */
    private static String unicodeStrip(String chars) {
        int debut = 0;
        int fin = chars.length();

        while (debut < fin
                && estBlanc(chars.codePointAt(debut))) {
            debut += Character.charCount(
                chars.codePointAt(debut));
        }

        while (fin > debut
                && estBlanc(chars.codePointBefore(fin))) {
            fin -= Character.charCount(
                chars.codePointBefore(fin));
        }

        return chars.substring(debut, fin);
    }

    /**
     * (22/11) Extraction : bornes INCLUSES, 1-based, en
     * CODEPOINTS. Semantique TOLERANTE : toute borne
     * hors de la chaine est RAMENEE dedans ; debut
     * apres fin -> chaine vide. 'hello' copyFrom: 2
     * to: 10 -> 'ello'. Aucune erreur de bornes.
     */
    @Primitive("copyFrom:to:")
    public static MTObject copyFromTo(
            MTObject receiver,
            MTObject... arguments) {

        MTString self = (MTString) receiver;
        String chars = self.javaValue();

        int n = chars.codePointCount(0, chars.length());

        int from =
            asInteger(arguments[0], "copyFrom:to:").javaValue();
        int to =
            asInteger(arguments[1], "copyFrom:to:").javaValue();

        int debut = Math.max(0, Math.min(from - 1, n));
        int fin = Math.max(0, Math.min(to, n));

        if (debut >= fin) {
            return context(self).newString("");
        }

        int begin =
            chars.offsetByCodePoints(0, debut);
        int end =
            chars.offsetByCodePoints(0, fin);

        return context(self).newString(
            chars.substring(begin, end));
    }

    /**
     * (22/11) Remplacement LITTERAL, sans expression
     * reguliere (divergence assumee avec Java
     * replaceAll, assumee aussi pour Python replace).
     * Toutes les occurrences, gauche a droite, sans
     * chevauchement : 'aaaa' replaceAll: 'aa' with: 'X'
     * -> 'XX' (le fragment remplace n'est pas re-ba le).
     * Motif absent -> recepteur INCHANGE.
     */
    @Primitive("replaceAll:with:")
    public static MTObject replaceAll(
            MTObject receiver,
            MTObject... arguments) {

        return replace(receiver, arguments,
            true, "replaceAll:with:");
    }

    /**
     * (22/11) Premiere occurrence seulement, le
     * couple AWK sub/gsub :
     * 'aaaa' replaceFirst: 'aa' with: 'X' -> 'Xaa'.
     */
    @Primitive("replaceFirst:with:")
    public static MTObject replaceFirst(
            MTObject receiver,
            MTObject... arguments) {

        return replace(receiver, arguments,
            false, "replaceFirst:with:");
    }

    /**
     * Le drapeau vit ICI, en prive -- pas dans le
     * langage : deux contrats, deux selecteurs
     * (doctrine at:ifAbsent:/at:ifAbsentPut:).
     * Garde : motif vide refuse (rien a chercher,
     * insertion universelle refusee).
     */
    private static MTObject replace(
            MTObject receiver,
            MTObject[] arguments,
            boolean all,
            String selector) {

        MTString self = (MTString) receiver;
        String chars = self.javaValue();

        String sought = asString(
            arguments[0], selector).javaValue();
        String replacement = asString(
            arguments[1], selector).javaValue();

        if (sought.isEmpty()) {
            throw new MTException(
                "Chaine vide a remplacer (" + selector + ")");
        }

        String result;

        if (all) {
            // Litteral : replace de Java, pas replaceAll
            result = chars.replace(sought, replacement);
        }
        else {
            int i = chars.indexOf(sought);
            result = i < 0
                ? chars
                : chars.substring(0, i) + replacement
                    + chars.substring(i + sought.length());
        }

        return context(self).newString(result);
    }

    @Primitive("asString")
    public static MTObject asString(
            MTObject receiver,
            MTObject... arguments) {

        return receiver;
    }
}
