package mt.runtime;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class MTClass
        implements MTObject {

    private String name;

    private MTClass mtClass;
    private MTClass superclass;

    private List<MTSymbol> instVars;
    private Map<MTSymbol, MTObject> classVars;

    private Map<MTSymbol, MTMethod> instMethods;
    private Map<MTSymbol, MTMethod> classMethods;

    public MTClass(
        String name,
        MTClass superclass) {

            this.name = name;
            this.superclass = superclass;

            this.instVars = new ArrayList<>();
            this.classVars = new HashMap<>();

            this.instMethods = new HashMap<>();
            this.classMethods = new HashMap<>();
    }

    @Override
    public MTClass getMTClass() {
        return mtClass;
    }

    public void setMTClass(MTClass mtClass) {
        this.mtClass = mtClass;
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

    public String getName() {
        return name;
    }

    public MTClass getSuperclass() {
        return superclass;
    }
    
    public void setSuperclass(MTClass superclass) {
        this.superclass = superclass;
    }

    public MTMethod lookup(MTSymbol selector, boolean classSide) {
        MTClass current = this;

        while (current != null) {
            MTMethod method;

            if (classSide) {
                method = current.classMethods.get(selector);
            }
            else {
                method = current.instMethods.get(selector);
            }

            if (method != null) {
                return method;
            }

            current = current.superclass;
        }

        return null;
    }

    public void addInstanceMethod(MTSymbol selector,MTMethod method) {
        instMethods.put(selector, method);
    }

    public MTMethod lookupInstanceMethod(MTSymbol selector) {
        MTClass current = this;

        while (current != null) {
            MTMethod method = current.instMethods.get(selector);

            if (method != null) {
                return method;
            }
            current = current.superclass;
        }
        return null;
    }

    public MTMethod lookupClassMethod(MTSymbol selector) {
        return lookup(selector, true);
    }



}
