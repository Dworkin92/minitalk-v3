package mt.ast;

import mt.runtime.MTContext;
import mt.runtime.MTObject;
import mt.runtime.MTScope;
import mt.runtime.MTSymbol;

/**
 * Litteral symbole : #ident.
 * Le contexte met chaque symbole en cache : deux evaluations
 * de #a dans le meme monde donnent le meme objet.
 */
public record MTSymbolNode(String name) implements MTNode {

    @Override
    public MTObject eval(MTContext context, MTScope scope) {
        return context.symbolObject(
            MTSymbol.intern(name));
    }
}
