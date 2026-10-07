package mt.runtime.primitives;

import mt.runtime.MTObject;

public final class MTObjectPrimitives {

    private MTObjectPrimitives() {
    }

    public static MTObject identity(MTObject receiver, MTObject... arguments) {
        return receiver;
    }

    public static MTObject mtClass(MTObject receiver, MTObject... args) {
        return receiver.getMTClass();
    }
}
