package mt.runtime;

import mt.runtime.exceptions.MTException;

/**
 * Entier MiniTalk. Pas de singleton : une instance par valeur.
 * Fabrique : MTInteger.of(int).
 */
public final class MTInteger
        implements MTObject {

    private final int javaValue;

    private MTClass mtClass;

    private static MTClass integerClass;


    private MTInteger(int javaValue) {
        this.javaValue = javaValue;
    }

    /**
     * Appele par le bootstrap : relie la fabrique
     * a la classe MiniTalk Integer.
     */
    public static void bindClass(MTClass mtClass) {
        integerClass = mtClass;
    }

    public int javaValue() {
        return javaValue;
    }

    public static MTInteger of(int javaValue) {
        MTInteger instance = new MTInteger(javaValue);
        instance.mtClass = integerClass;
        return instance;
    }
    /*

    public static MTInteger of(int javaValue) {
        return new MTInteger(javaValue);
    }
    */

    public void setMTClass(MTClass mtClass) {
        this.mtClass = mtClass;
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
