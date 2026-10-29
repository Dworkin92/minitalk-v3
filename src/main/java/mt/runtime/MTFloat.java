package mt.runtime;

import mt.runtime.exceptions.MTException;

/**
 * Flottant MiniTalk (double IEEE 754). Pas de singleton :
 * une instance par valeur. Fabrique : MTContext.newFloat(double).
 *
 * Affichage : Double.toString -- 2.0 reste "2.0" (le type
 * reste visible, style Squeak ; decision du 29/10).
 */
public final class MTFloat
        implements MTObject {

    private final double javaValue;

    private final MTClass mtClass;

    MTFloat(MTClass mtClass, double javaValue) {
        this.mtClass = mtClass;
        this.javaValue = javaValue;
    }

    public double javaValue() {
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
                "Message non compris : Float >> " + selector);
        }

        return method.invoke(this, arguments);
    }

    @Override
    public MTString asString() {
        return mtClass.getContext().newString(
            Double.toString(javaValue));
    }

    @Override
    public String toString() {
        return Double.toString(javaValue);
    }

    @Override
    public boolean isNil() {
        return false;
    }
}
