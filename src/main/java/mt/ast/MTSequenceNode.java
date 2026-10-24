package mt.ast;

import java.util.List;

import mt.runtime.MTContext;
import mt.runtime.MTObject;
import mt.runtime.MTScope;

public record MTSequenceNode(List<MTNode> statements) implements MTNode {

    @Override
    public MTObject eval(MTContext context, MTScope scope) {

        MTObject result = context.nil();

        for (MTNode statement : statements) {
            result = statement.eval(context, scope);
        }

        return result;
    }
}
