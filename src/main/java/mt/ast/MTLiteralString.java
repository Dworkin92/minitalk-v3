package mt.ast;

import mt.runtime.MTContext;
import mt.runtime.MTObject;
import mt.runtime.MTScope;

public record MTLiteralString(String value) implements MTNode {

    @Override
    public MTObject eval(MTContext context, MTScope scope) {
        return context.newString(value);
    }
}
