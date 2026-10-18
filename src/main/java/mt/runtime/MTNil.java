package mt.runtime;

import mt.runtime.exceptions.MTException;

/**
 * Nil MiniTalk. Singleton possede par le contexte
 * (MTContext.nil()), rattache a la classe Nil au bootstrap.
 */
public final class MTNil
        implements MTObject {

    private MTClass mtClass;

    MTNil() {
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
                "Message non compris : Nil >> " + selector);
        }

        return method.invoke(this, arguments);
    }

    @Override
    public MTString asString() {
        return new MTString("nil");
    }

    @Override
    public boolean isNil() {
        return true;
    }
}