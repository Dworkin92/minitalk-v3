package mt.runtime;

import java.util.Map;

public final class MTInstance
        implements MTObject {

    private MTClass mtClass;

    private Map<MTSymbol, MTObject> properties;

    @Override
public MTClass getMTClass() {
return mtClass;
}

@Override
public MTObject send(
MTSymbol selector,
MTObject... arguments) {
return null;
}

@Override
public MTString asString() {
return null;
}
}
