package mt.bootstrap;

import mt.runtime.MTClass;

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
    }

    public MTClass getObjectClass() {
        return objectClass;
    }

    public MTClass getClassClass() {
        return classClass;
    }
}
