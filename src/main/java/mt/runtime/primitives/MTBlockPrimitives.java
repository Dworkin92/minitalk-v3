package mt.runtime.primitives;

import mt.runtime.MTBlock;
import mt.runtime.MTObject;

public final class MTBlockPrimitives {

    private MTBlockPrimitives() {
    }

    @Primitive("value")
    public static MTObject value(
            MTObject receiver,
            MTObject... arguments) {

        return ((MTBlock) receiver).invoke(receiver);
    }

    @Primitive("value:")
    public static MTObject valueColon(
            MTObject receiver,
            MTObject... arguments) {

        return ((MTBlock) receiver).invoke(receiver, arguments[0]);
    }
}

