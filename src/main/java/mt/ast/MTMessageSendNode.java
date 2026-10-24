package mt.ast;

import java.util.List;

import mt.runtime.MTContext;
import mt.runtime.MTObject;
import mt.runtime.MTScope;
import mt.runtime.MTSymbol;

public record MTMessageSendNode(
        MTNode receiver,
        MTSymbol selector,
        List<MTNode> arguments) implements MTNode {

    @Override
    public MTObject eval(MTContext context, MTScope scope) {

        MTObject receiverObject =
            receiver.eval(context, scope);

        MTObject[] argumentObjects =
            new MTObject[arguments.size()];

        for (int i = 0; i < argumentObjects.length; i++) {
            argumentObjects[i] =
                arguments.get(i).eval(context, scope);
        }

        return receiverObject.send(selector, argumentObjects);
    }
}
