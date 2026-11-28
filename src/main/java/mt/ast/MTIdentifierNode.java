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
            if (name.equals("super")) {
                // (28/11) refus propre, plus clair que
                // l'absence de variable : super ne vit
                // que dans un corps de methode.
                throw new MTException(
                    "super : seulement dans une methode");
            }
            throw new MTException(
                "Variable non definie : " + name);
        }

        return value;
    }
}
