package mt.ast;

import java.util.List;

import mt.runtime.MTContext;
import mt.runtime.MTObject;
import mt.runtime.MTScope;
import mt.runtime.MTSymbol;

public record MTBlockNode(
        List<String> parameters,
        MTSequenceNode body) implements MTNode {

    @Override
    public MTObject eval(MTContext context, MTScope scope) {

        List<MTSymbol> params = parameters.stream()
            .map(MTSymbol::intern)
            .toList();

        // La portee actuelle devient la portee de DEFINITION
        // du bloc : c'est la fermeture.
        return context.newBlock(
            scope,
            params,
            blockScope -> body.eval(context, blockScope));
    }
}
