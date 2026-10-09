package mt.runtime;

import mt.runtime.exceptions.MTException;

public final class MTNil
        implements MTObject {

    private MTClass mtClass;

    private static final MTNil INSTANCE =
            new MTNil();

    private MTNil() {
    }

    public static MTNil instance() {
        return INSTANCE;
    }

    @Override
    public MTClass getMTClass() {
        return mtClass;
    }

    public void setMTClass(MTClass mtClass) {
        this.mtClass = mtClass;
    }

    @Override
    public MTObject send(
            MTSymbol selector,
            MTObject... arguments) {

        MTMethod method = mtClass.lookupInstanceMethod(selector);

        if (method == null) {
            throw new MTException(
                "Message non compris : Nil >> " + selector);
        }

        return method.invoke(this, arguments);
    }

    @Override
    public MTString asString() {
        // TODO : retourner une MTString "nil"
        return new MTString("nil");
    }

    @Override
    public boolean isNil() {
        return true;
    }
}
