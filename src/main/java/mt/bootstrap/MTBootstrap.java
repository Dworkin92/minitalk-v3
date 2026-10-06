package mt.bootstrap;

import mt.runtime.MTClass;
import mt.runtime.MTDummyMethod;
import mt.runtime.MTMethod;
import mt.runtime.MTSymbol;

public final class MTBootstrap {

    private MTClass objectClass;

    private MTClass classClass;

    public void initialize() {

        objectClass =
                new MTClass("Object", null);

        classClass =
                new MTClass("Class", objectClass);

        //
        // Relation fondamentale
        //

        objectClass.setSuperclass(null);
        objectClass.setMTClass(classClass);

        classClass.setSuperclass(objectClass);
        classClass.setMTClass(classClass);

        System.out.println("Object = " + objectClass.getName());
        System.out.println("Object.class = " + objectClass.getMTClass().getName());

        System.out.println("Class = " + classClass.getName());
        System.out.println("Class.class = " + classClass.getMTClass().getName());

        MTSymbol selector = MTSymbol.intern("test");

        objectClass.addInstanceMethod(selector, new MTDummyMethod());

        MTMethod method = objectClass.lookupInstanceMethod(selector);
        System.out.println("lookup(test) != null : " + (method != null));
    }

    public MTClass getObjectClass() {
        return objectClass;
    }

    public MTClass getClassClass() {
        return classClass;
    }
}
