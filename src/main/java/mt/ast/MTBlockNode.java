package mt.ast;

import java.util.List;

import mt.runtime.MTContext;
import mt.runtime.MTObject;
import mt.runtime.MTScope;
import mt.runtime.MTSymbol;

public record MTBlockNode(
        List<String> parameters,
        List<String> locals,
        MTSequenceNode body) implements MTNode {

    @Override
    public MTObject eval(MTContext context, MTScope scope) {

        List<MTSymbol> params = parameters.stream()
            .map(MTSymbol::intern)
            .toList();

        return context.newBlock(
            scope,
            params,
            blockScope -> {

                // Temporaires : initialisees a nil
                for (String local : locals) {
                    blockScope.define(
                        MTSymbol.intern(local),
                        context.nil());
                }

                return body.eval(context, blockScope);
            });
    }
}
