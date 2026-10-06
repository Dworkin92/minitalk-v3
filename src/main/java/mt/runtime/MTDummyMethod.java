package mt.runtime;

public final class MTDummyMethod
        implements MTMethod {

    @Override
    public MTObject invoke(
            MTObject receiver,
            MTObject... arguments) {

        return null;
    }
}
