package mt.ast;

import mt.runtime.MTContext;
import mt.runtime.MTObject;
import mt.runtime.MTScope;

public record MTLiteralFloat(double value) implements MTNode {

    @Override
    public MTObject eval(MTContext context, MTScope scope) {
        return context.newFloat(value);
    }
}
