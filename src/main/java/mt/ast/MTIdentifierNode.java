package mt.ast;

import mt.runtime.MTContext;
import mt.runtime.MTObject;
import mt.runtime.MTScope;
import mt.runtime.MTSymbol;
import mt.runtime.exceptions.MTException;

public record MTIdentifierNode(String name) implements MTNode {

    @Override
    public MTObject eval(MTContext context, MTScope scope) {

        MTObject value =
            scope.lookup(MTSymbol.intern(name));

        if (value == null) {
            throw new MTException(
                "Variable non definie : " + name);
        }

        return value;
    }
}
