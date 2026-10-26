package mt.runtime;

import mt.runtime.exceptions.MTException;

/**
 * Symbole expose au langage : #ident.
 *
 * MTSymbol (le pool, identite du langage, hors des mondes)
 * reste ce qu'il est ; MTSymbolObject est son representant
 * DANS un contexte. Le contexte met chaque symbole en cache :
 * deux evaluations de #a dans le meme monde donnent le meme
 * objet, donc #a == #a est vrai. Deux mondes ont chacun
 * leurs representants : l'isolation est preservee.
 *
 * Fabrique : MTContext.symbolObject(MTSymbol).
 */
public final class MTSymbolObject
        implements MTObject {

    private final MTClass mtClass;
    private final MTSymbol symbol;

    MTSymbolObject(MTClass mtClass, MTSymbol symbol) {
        this.mtClass = mtClass;
        this.symbol = symbol;
    }

    public MTSymbol symbol() {
        return symbol;
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
                "Message non compris : Symbol >> " + selector);
        }

        return method.invoke(this, arguments);
    }

    @Override
    public MTString asString() {
        // Comme en Smalltalk : le nom, sans le #.
        return mtClass.getContext().newString(symbol.getValue());
    }

    @Override
    public String toString() {
        return "#" + symbol.getValue();
    }
}
