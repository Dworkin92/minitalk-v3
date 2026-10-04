package mt.runtime;

public final class MTString
        implements MTObject {

    @Override
    public MTClass getMTClass() {
        return null;
    }

    @Override
    public MTObject send(
            MTSymbol selector,
            MTObject... arguments) {
        return null;
    }

    @Override
    public MTString asString() {
        return this;
    }
}
