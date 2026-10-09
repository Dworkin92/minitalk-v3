package mt.runtime;

import mt.runtime.exceptions.MTException;

/**
 * Booleen MiniTalk. Deux singletons : TRUE et FALSE.
 * Rattaches a la classe Boolean par le bootstrap.
 */
public final class MTBoolean
        implements MTObject {

    public static final MTBoolean TRUE =
            new MTBoolean(true);

    public static final MTBoolean FALSE =
            new MTBoolean(false);

    private final boolean javaValue;

    private MTClass mtClass;

    private MTBoolean(boolean javaValue) {
        this.javaValue = javaValue;
    }

    public boolean javaValue() {
        return javaValue;
    }

    public static MTBoolean of(boolean javaValue) {
        return javaValue ? TRUE : FALSE;
    }

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
                "Message non compris : Boolean >> " + selector);
        }

        return method.invoke(this, arguments);
    }

    @Override
    public MTString asString() {
        return new MTString(javaValue ? "true" : "false");
    }

    @Override
    public String toString() {
        return javaValue ? "true" : "false";
    }

    @Override
    public boolean isNil() {
        return false;
    }
}
