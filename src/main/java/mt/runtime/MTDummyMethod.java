package mt.runtime;

public final class MTDummyMethod
        implements MTMethod {

    @Override
    public MTObject invoke(
            MTObject receiver,
            MTObject... arguments) {

        //System.out.println("Receiver class = " + receiver.getMTClass().getName());

        return receiver;
    }
}
