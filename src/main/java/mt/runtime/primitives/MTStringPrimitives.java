package mt.runtime.primitives;

import mt.runtime.MTObject;
import mt.runtime.MTString;
import mt.runtime.exceptions.MTException;

public final class MTStringPrimitives {

    private MTStringPrimitives() {
    }

    private static mt.runtime.MTContext context(MTObject receiver) {
        return receiver.getMTClass().getContext();
    }

    /** Concatenation : 'abc' , 'def' */
    @Primitive(",")
    public static MTObject concatenate(
            MTObject receiver,
            MTObject... arguments) {

        MTString self = (MTString) receiver;
        MTString other = (MTString) arguments[0];

        return context(self).newString(
            self.javaValue() + other.javaValue());
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
        MTString other = (MTString) arguments[0];

        return context(self).newBoolean(
            self.javaValue().equals(other.javaValue()));
    }

    @Primitive("==")
    public static MTObject sameAs(
            MTObject receiver,
            MTObject... arguments) {

        return context(receiver).newBoolean(
            receiver.identityEquals(arguments[0]));
    }

    /**
     * Acces 1-based : 'abc' at: 2 -> codepoint de 'b'.
     * (MTCharacter viendra plus tard.)
     */
    @Primitive("at:")
    public static MTObject at(
            MTObject receiver,
            MTObject... arguments) {

        MTString self = (MTString) receiver;
        int index = ((mt.runtime.MTInteger) arguments[0]).javaValue();

        if (index < 1 || index > self.javaValue().length()) {
            throw new MTException(
                "Index hors bornes : " + self
                + " at: " + index);
        }

        return context(self).newInteger(
            self.javaValue().codePointAt(index - 1));
    }

    /** Position 1-based de la premiere occurrence, 0 si absente */
    @Primitive("indexOf:")
    public static MTObject indexOf(
            MTObject receiver,
            MTObject... arguments) {

        MTString string = (MTString) receiver;
        MTString sought = (MTString) arguments[0];

        return context(receiver).newInteger(
            string.javaValue().indexOf(
                sought.javaValue()) + 1);
    }

    @Primitive("contains:")
    public static MTObject contains(
            MTObject receiver,
            MTObject... arguments) {

        MTString string = (MTString) receiver;
        MTString sought = (MTString) arguments[0];

        return context(receiver).newBoolean(
            string.javaValue()
                .contains(sought.javaValue()));
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
