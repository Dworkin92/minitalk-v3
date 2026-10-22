package mt.runtime.primitives;

import mt.runtime.MTObject;

public final class MTBlockPrimitives {

    private MTBlockPrimitives() {
    }

    /** Evaluation sans argument : [ ... ] value */
    @Primitive("value")
    public static MTObject value(
            MTObject receiver,
            MTObject... arguments) {

        return ((mt.runtime.MTBlock) receiver)
            .invoke(receiver);
    }

    /** Evaluation a un argument : [ :x | ... ] value: arg */
    @Primitive("value:")
    public static MTObject valueColon(
            MTObject receiver,
            MTObject... arguments) {

        return ((mt.runtime.MTBlock) receiver)
            .invoke(receiver, arguments[0]);
    }

    /** [ :x :y | ... ] value: a value: b — ecrit [a] value: a with: b */
    @Primitive("value:value:")
    public static MTObject valueValue(
            MTObject receiver,
            MTObject... arguments) {

        return ((mt.runtime.MTBlock) receiver)
            .invoke(receiver, arguments[0], arguments[1]);
    }

    /** Un bloc ne sait pas encore se decrire : asString */
    @Primitive("asString")
    public static MTObject asString(
            MTObject receiver,
            MTObject... arguments) {

        return receiver.asString();
    }
}
