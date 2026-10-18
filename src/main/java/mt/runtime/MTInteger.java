package mt.runtime;

import mt.runtime.exceptions.MTException;

/**
 * Entier MiniTalk. Pas de singleton : une instance par valeur.
 * Fabrique : MTContext.newInteger(int).
 */
public final class MTInteger
        implements MTObject {

    private final int javaValue;

    private final MTClass mtClass;

    MTInteger(MTClass mtClass, int javaValue) {
        this.mtClass = mtClass;
        this.javaValue = javaValue;
    }

    public int javaValue() {
        return javaValue;
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
                "Message non compris : Integer >> " + selector);
        }

        return method.invoke(this, arguments);
    }

    @Override
    public MTString asString() {
        return new MTString(String.valueOf(javaValue));
    }

    @Override
    public String toString() {
        return String.valueOf(javaValue);
    }
}
