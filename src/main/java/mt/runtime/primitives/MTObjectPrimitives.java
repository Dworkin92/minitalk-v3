package mt.runtime.primitives;

import mt.runtime.MTContext;
import mt.runtime.MTObject;

public final class MTObjectPrimitives {

    private MTObjectPrimitives() {
    }

    private static MTContext context(MTObject receiver) {
        return receiver.getMTClass().getContext();
    }

    @Primitive("identity")
    public static MTObject identity(MTObject receiver, MTObject... arguments) {
        return receiver;
    }

    @Primitive("class")
    public static MTObject mtClass(MTObject receiver, MTObject... args) {
        return receiver.getMTClass();
    }

    @Primitive("isNil")
    public static MTObject isNil(MTObject receiver, MTObject... arguments) {
        return context(receiver).newBoolean(receiver.isNil());
    }

    @Primitive("notNil")
    public static MTObject notNil(MTObject receiver, MTObject... arguments) {
        return context(receiver).newBoolean(!receiver.isNil());
    }
}
