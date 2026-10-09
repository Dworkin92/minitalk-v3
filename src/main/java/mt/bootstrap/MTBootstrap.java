package mt.bootstrap;

import mt.runtime.MTBoolean;
import mt.runtime.MTClass;
import mt.runtime.MTNil;
import mt.runtime.MTObject;
import mt.runtime.MTSymbol;
import mt.runtime.primitives.MTBooleanPrimitives;
import mt.runtime.primitives.MTNilPrimitives;
import mt.runtime.primitives.MTObjectPrimitives;
import mt.runtime.primitives.MTPrimitiveInstaller;

public final class MTBootstrap {

    private MTClass objectClass;

    private MTClass classClass;

    private MTClass nilClass;

    private MTClass booleanClass;

    public void initialize() {

        //
        // Relations fondamentales
        //

        objectClass = new MTClass("Object", null);
        classClass = new MTClass("Class", objectClass);
        nilClass   = new MTClass("Nil", objectClass);
        booleanClass = new MTClass("Boolean", objectClass);

        objectClass.setSuperclass(null);
        objectClass.setMTClass(classClass);

        classClass.setSuperclass(objectClass);
        classClass.setMTClass(classClass);

        MTBoolean.TRUE.setMTClass(booleanClass);
        MTBoolean.FALSE.setMTClass(booleanClass);

        MTNil.instance().setMTClass(nilClass);

        //
        // Installation des primitives
        // Chaque brique future = une ligne ici, rien d'autre.
        //

        MTPrimitiveInstaller.install(objectClass, MTObjectPrimitives.class);
        MTPrimitiveInstaller.install(nilClass,    MTNilPrimitives.class);
        MTPrimitiveInstaller.install(booleanClass, MTBooleanPrimitives.class);

        smokeTest();
    }

    private void smokeTest() {

        MTObject nil = MTNil.instance();
        MTObject obj = new mt.runtime.MTInstance(objectClass);

        System.out.println("Object            = " + objectClass.getName());
        System.out.println("Object.class      = " + objectClass.getMTClass().getName());
        System.out.println("nil isNil         = " + nil.send(MTSymbol.intern("isNil")));
        System.out.println("obj notNil        = " + obj.send(MTSymbol.intern("notNil")));
        System.out.println("nil ifNil: (42)   = " + nil.send(MTSymbol.intern("ifNil:"), obj));
    }

    public MTClass getObjectClass() {
        return objectClass;
    }

    public MTClass getClassClass() {
        return classClass;
    }

    public MTClass getNilClass() {
        return nilClass;
    }
}
