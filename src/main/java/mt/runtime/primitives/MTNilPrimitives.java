package mt.runtime.primitives;

import mt.runtime.MTContext;
import mt.runtime.MTMethod;
import mt.runtime.MTObject;
import mt.runtime.MTString;

public final class MTNilPrimitives {

    private MTNilPrimitives() {
    }

    private static MTContext context(MTObject receiver) {
        return receiver.getMTClass().getContext();
    }

    @Primitive("isNil")
    public static MTObject isNil(
            MTObject receiver,
            MTObject... arguments) {

        return context(receiver).newBoolean(receiver.isNil());
    }

    @Primitive("notNil")
    public static MTObject notNil(
            MTObject receiver,
            MTObject... arguments) {

        return context(receiver).newBoolean(!receiver.isNil());
    }

    /**
     * ifNil: unArgument
     *
     * Un MTMethod (futur MTBlock) est invoque,
     * toute autre valeur est retournee telle quelle.
     */
    @Primitive("ifNil:")
    public static MTObject ifNil(
            MTObject receiver,
            MTObject... arguments) {

        if (receiver.isNil()) {
            MTObject argument = arguments[0];

            if (argument instanceof MTMethod method) {
                return method.invoke(receiver);
            }

            return argument;
        }

        return receiver;
    }

    /**
     * ifNotNil: unArgument - symetrique de ifNil:
     */
    @Primitive("ifNotNil:")
    public static MTObject ifNotNil(
            MTObject receiver,
            MTObject... arguments) {

        if (receiver.isNil()) {
            return receiver;
        }

        MTObject argument = arguments[0];

        if (argument instanceof MTMethod method) {
            return method.invoke(receiver);
        }

        return argument;
    }

    /**
     * = et == : nil n'est egal qu'a lui-meme.
     */
    @Primitive("=")
    @Primitive("==")
    public static MTObject equals(
            MTObject receiver,
            MTObject... arguments) {

        return context(receiver).newBoolean(
            receiver.identityEquals(arguments[0]));
    }

    @Primitive("asString")
    public static MTObject asString(
            MTObject receiver,
            MTObject... arguments) {

        return new MTString("nil");
    }
}
