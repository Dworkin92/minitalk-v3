package mt.ast;

import mt.runtime.MTContext;
import mt.runtime.MTObject;
import mt.runtime.MTScope;
import mt.runtime.exceptions.MTException;

public record MTLiteralSpecial(String name) implements MTNode {

    @Override
    public MTObject eval(MTContext context, MTScope scope) {

        return switch (name) {
            case "nil"   -> context.nil();
            case "true"  -> context.trueObject();
            case "false" -> context.falseObject();
            default -> throw new MTException(
                "Litteral inconnu : " + name);
        };
    }
}
