package mt.runtime;

import java.util.HashMap;
import java.util.Map;

import mt.runtime.exceptions.MTException;

public final class MTInstance
        implements MTObject {

    private MTClass mtClass;

    private Map<MTSymbol, MTObject> properties;

    public MTInstance(MTClass mtClass) {
        this.mtClass = mtClass;

        this.properties = new HashMap<>();
    }

    @Override
    public MTClass getMTClass() {
        return mtClass;
    }

    @Override
    public MTObject send(MTSymbol selector, MTObject... arguments) throws MTException {
        MTMethod method = mtClass.lookupInstanceMethod(selector);

        if (method == null) {
            throw new MTException("Method not found : " + selector);
        }

        return method.invoke(this, arguments);
    }

    @Override
    public MTString asString() {
        return null;
    }
}
