package mt.runtime;

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
        return null;
    }

    public void setMTClass(MTClass mtClass) {
        this.mtClass = mtClass;
    }

    @Override
    public MTObject send(
            MTSymbol selector,
            MTObject... arguments) {

        return null;
    }

    @Override
    public MTString asString() {
        return null;
    }

    @Override
    public boolean isNil() {
        return true;
    }
}