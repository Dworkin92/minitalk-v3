package mt.runtime.primitives;

import mt.runtime.MTObject;

public final class MTBlockPrimitives {

    private MTBlockPrimitives() {
    }

    /** Evaluation sans argument : [ ... ] value */
    @Primitive("value")
    @Primitive("value:")
    @Primitive("value:value:")
    public static MTObject value(
            MTObject receiver,
            MTObject... arguments) {

        return ((mt.runtime.MTBlock) receiver)
            .invoke(receiver, arguments);
    }

    /** Un bloc ne sait pas encore se decrire : asString */
    @Primitive("asString")
    public static MTObject asString(
            MTObject receiver,
            MTObject... arguments) {

        return receiver.asString();
    }
}
