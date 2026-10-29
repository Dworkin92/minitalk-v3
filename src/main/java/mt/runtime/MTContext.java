package mt.runtime;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mt.runtime.MTClass;
import mt.runtime.primitives.MTArrayPrimitives;
import mt.runtime.primitives.MTBlockPrimitives;
import mt.runtime.primitives.MTBooleanPrimitives;
import mt.runtime.primitives.MTIntegerPrimitives;
import mt.runtime.primitives.MTFloatPrimitives;
import mt.runtime.primitives.MTCharacterPrimitives;
import mt.runtime.primitives.MTNilPrimitives;
import mt.runtime.primitives.MTObjectPrimitives;
import mt.runtime.primitives.MTPrimitiveInstaller;
import mt.runtime.primitives.MTStringPrimitives;
import mt.runtime.primitives.MTSymbolPrimitives;

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
    private final MTClass floatClass;
    private final MTClass blockClass;
    private final MTClass stringClass;
    private final MTClass arrayClass;
    private final MTClass symbolClass;
    private final MTClass characterClass;

    //
    // Singletons du contexte
    //

    private final MTNil nil;
    private final MTBoolean trueObject;
    private final MTBoolean falseObject;

    //
    // Representants des symboles dans CE monde
    // (MTSymbol reste le pool, hors des mondes)
    //

    private final Map<MTSymbol, MTSymbolObject> symbolObjects;

    //
    // Registre : toutes les classes, par nom
    //

    private final Map<MTSymbol, MTClass> classes;

    private MTContext() {

        classes = new HashMap<>();
        symbolObjects = new HashMap<>();

        objectClass  = new MTClass(this, "Object", null);
        classClass   = new MTClass(this, "Class", objectClass);
        nilClass     = new MTClass(this, "Nil", objectClass);
        booleanClass = new MTClass(this, "Boolean", objectClass);
        integerClass = new MTClass(this, "Integer", objectClass);
        floatClass   = new MTClass(this, "Float", objectClass);
        stringClass  = new MTClass(this, "String", objectClass);
        blockClass   = new MTClass(this, "Block", objectClass);
        arrayClass   = new MTClass(this, "Array", objectClass);
        symbolClass  = new MTClass(this, "Symbol", objectClass);
        characterClass = new MTClass(this, "Character", objectClass);

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
        registerClass(floatClass);
        registerClass(stringClass);
        registerClass(blockClass);
        registerClass(arrayClass);
        registerClass(symbolClass);
        registerClass(characterClass);
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
        MTPrimitiveInstaller.install(objectClass,    MTObjectPrimitives.class);
        MTPrimitiveInstaller.install(nilClass,       MTNilPrimitives.class);
        MTPrimitiveInstaller.install(booleanClass,   MTBooleanPrimitives.class);
        MTPrimitiveInstaller.install(integerClass,   MTIntegerPrimitives.class);
        MTPrimitiveInstaller.install(floatClass,     MTFloatPrimitives.class);
        MTPrimitiveInstaller.install(stringClass,    MTStringPrimitives.class);
        MTPrimitiveInstaller.install(blockClass,     MTBlockPrimitives.class);
        MTPrimitiveInstaller.install(arrayClass,     MTArrayPrimitives.class);
        MTPrimitiveInstaller.install(symbolClass,    MTSymbolPrimitives.class);
        MTPrimitiveInstaller.install(characterClass, MTCharacterPrimitives.class);
    }

    //
    // Fabriques : ce qui etait statique vit dans le contexte
    //

    public MTScope newScope() {
        return new MTScope(null);
    }

    public MTInteger newInteger(int javaValue) {
        return new MTInteger(integerClass, javaValue);
    }

    public MTFloat newFloat(double javaValue) {
        return new MTFloat(floatClass, javaValue);
    }

    public MTCharacter newCharacter(int codepoint) {
        return new MTCharacter(characterClass, codepoint);
    }

    public MTBoolean newBoolean(boolean javaValue) {
        return javaValue ? trueObject : falseObject;
    }

    public MTString newString(String javaValue) {
        return new MTString(stringClass, javaValue);
    }

    public MTArray newArray(List<MTObject> elements) {
        return new MTArray(arrayClass, elements);
    }

    /**
     * Representant d'un symbole dans CE monde, mis en cache :
     * #a == #a est vrai au sein d'un contexte, sans etat
     * statique partage entre les mondes.
     */
    public MTSymbolObject symbolObject(MTSymbol symbol) {
        return symbolObjects.computeIfAbsent(
            symbol,
            s -> new MTSymbolObject(symbolClass, s));
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

    public MTClass getFloatClass() {
        return floatClass;
    }

    public MTClass getCharacterClass() {
        return characterClass;
    }

    public MTClass getStringClass() {
        return stringClass;
    }

    public MTClass getBlockClass() {
        return blockClass;
    }

    public MTClass getArrayClass() {
        return arrayClass;
    }

    public MTClass getSymbolClass() {
        return symbolClass;
    }
}
