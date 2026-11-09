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
            throw new MTException("Message non compris : " + mtClass.getName() + " >> " + selector);
        }

        return method.invoke(this, arguments);
    }

    /** (08/11) Lecture d'une variable d'instance.
     * null = jamais assignee : l'appelant rendra nil. */
    public MTObject getProperty(MTSymbol name) {
        return properties.get(name);
    }

    /** (08/11) Ecriture d'une variable d'instance. */
    public void setProperty(MTSymbol name, MTObject value) {
        properties.put(name, value);
    }

    @Override
    public MTString asString() {
        // (08/11) « an » devant voyelle, « a » sinon,
        // AVEC l'espace -- anPoint etait un accident.
        String article =
            "AEIOUaeiou".indexOf(
                mtClass.getName().charAt(0)) >= 0
            ? "an " : "a ";

        return mtClass.getContext()
            .newString(article + mtClass.getName());
    }
    
}
