package mt.runtime;

import mt.runtime.exceptions.MTException;

/**
 * Chaine MiniTalk. Fabrique : MTContext.newString(String).
 */
public final class MTString
        implements MTObject {

    private final MTClass mtClass;
    private final String value;

    MTString(MTClass mtClass, String value) {
        this.mtClass = mtClass;
        this.value = value;
    }

    public String javaValue() {
        return value;
    }

    @Override
    public MTClass getMTClass() {
        return mtClass;
    }

    @Override
    public MTObject send(
            MTSymbol selector,
            MTObject... arguments) {

        MTMethod method = mtClass.lookupInstanceMethod(selector);

        if (method == null) {
            throw new MTException(
                "Message non compris : String >> " + selector);
        }

        return method.invoke(this, arguments);
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
