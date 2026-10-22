package mt.runtime;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mt.runtime.MTClass;
import mt.runtime.primitives.MTBlockPrimitives;
import mt.runtime.primitives.MTBooleanPrimitives;
import mt.runtime.primitives.MTIntegerPrimitives;
import mt.runtime.primitives.MTNilPrimitives;
import mt.runtime.primitives.MTObjectPrimitives;
import mt.runtime.primitives.MTPrimitiveInstaller;
import mt.runtime.primitives.MTStringPrimitives;

/**
 * Contexte MiniTalk : possede les classes fondamentales, les
 * singletons et le registre des classes. Un contexte = un monde.
 *
 * Le noyau (les classes dont le runtime a besoin pour se
 * construire) est en acces direct ; toutes les classes, noyau
 * compris, vivent aussi dans le registre par nom, qui accueillera
 * les classes definies plus tard (String, Array, etc.).
 */
public final class MTContext {

    //
    // Noyau : acces direct
    //

    private final MTClass objectClass;
    private final MTClass classClass;
    private final MTClass nilClass;
    private final MTClass booleanClass;
    private final MTClass integerClass;
    private final MTClass blockClass;
    private final MTClass stringClass;

    //
    // Singletons du contexte
    //

    private final MTNil nil;
    private final MTBoolean trueObject;
    private final MTBoolean falseObject;

    //
    // Registre : toutes les classes, par nom
    //

    private final Map<MTSymbol, MTClass> classes;

    private MTContext() {

        classes = new HashMap<>();

        objectClass  = new MTClass(this, "Object", null);
        classClass   = new MTClass(this, "Class", objectClass);
        nilClass     = new MTClass(this, "Nil", objectClass);
        booleanClass = new MTClass(this, "Boolean", objectClass);
        integerClass = new MTClass(this, "Integer", objectClass);
        stringClass  = new MTClass(this, "String", objectClass);
        blockClass   = new MTClass(this, "Block", objectClass);


        nil         = new MTNil();
        trueObject  = new MTBoolean(true);
        falseObject = new MTBoolean(false);

        objectClass.setMTClass(classClass);
        classClass.setMTClass(classClass);

        nil.setMTClass(nilClass);
        trueObject.setMTClass(booleanClass);
        falseObject.setMTClass(booleanClass);

        registerClass(objectClass);
        registerClass(classClass);
        registerClass(nilClass);
        registerClass(booleanClass);
        registerClass(integerClass);
        registerClass(stringClass);
        registerClass(blockClass);
    }

    /**
     * Bootstrap standard : classes fondamentales + primitives.
     */
    public static MTContext newStandard() {
        MTContext context = new MTContext();

        context.installPrimitives();

        return context;
    }

    //
    // Installation des primitives
    // Chaque brique future = une ligne ici, rien d'autre.
    //

    private void installPrimitives() {
        MTPrimitiveInstaller.install(objectClass,   MTObjectPrimitives.class);
        MTPrimitiveInstaller.install(nilClass,      MTNilPrimitives.class);
        MTPrimitiveInstaller.install(booleanClass,  MTBooleanPrimitives.class);
        MTPrimitiveInstaller.install(integerClass,  MTIntegerPrimitives.class);
        MTPrimitiveInstaller.install(stringClass,   MTStringPrimitives.class);
        MTPrimitiveInstaller.install(blockClass,    MTBlockPrimitives.class); 
    }

    //
    // Fabriques : ce qui etait statique vit dans le contexte
    //

    public MTInteger newInteger(int javaValue) {
        return new MTInteger(integerClass, javaValue);
    }

    public MTBoolean newBoolean(boolean javaValue) {
        return javaValue ? trueObject : falseObject;
    }

    public MTString newString(String javaValue) {
        return new MTString(stringClass, javaValue);
    }
    
    public MTBlock newBlock(
        MTScope definingScope,
        List<MTSymbol> parameters,
        MTBlock.Body body) {
        return new MTBlock(blockClass, definingScope, parameters, body);
    }
    
    public MTNil nil() {
        return nil;
    }

    public MTBoolean trueObject() {
        return trueObject;
    }

    public MTBoolean falseObject() {
        return falseObject;
    }

    //
    // Registre des classes
    //

    public MTClass registerClass(MTClass mtClass) {
        classes.put(MTSymbol.intern(mtClass.getName()), mtClass);
        return mtClass;
    }

    public MTClass lookupClass(MTSymbol name) {
        return classes.get(name);
    }

    //
    // Acces au noyau
    //

    public MTClass getObjectClass() {
        return objectClass;
    }

    public MTClass getClassClass() {
        return classClass;
    }

    public MTClass getNilClass() {
        return nilClass;
    }

    public MTClass getBooleanClass() {
        return booleanClass;
    }

    public MTClass getIntegerClass() {
        return integerClass;
    }

    public MTClass getStringClass() {
        return stringClass;
    }

    public MTClass getBlockClass() {
        return blockClass;
    }
}

