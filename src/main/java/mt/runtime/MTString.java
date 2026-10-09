package mt.runtime;

public final class MTString
        implements MTObject {

    private final String value;

    public MTString(String value) {
        this.value = value;
    }

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

    @Override
    public String toString() {
        return value;
    }
}

