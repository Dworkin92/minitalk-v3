package mt.runtime;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mt.runtime.exceptions.MTException;

public final class MTClass
        implements MTObject {

    private String name;

    private final MTContext context;

    private MTClass mtClass;
    private MTClass superclass;

    private List<MTSymbol> instVars;
    private Map<MTSymbol, MTObject> classVars;

    private Map<MTSymbol, MTMethod> instMethods;
    private Map<MTSymbol, MTMethod> classMethods;

    public MTClass(
        MTContext context,
        String name,
        MTClass superclass) {

            this.context = context;
            this.name = name;
            this.superclass = superclass;

            this.instVars = new ArrayList<>();
            this.classVars = new HashMap<>();

            this.instMethods = new HashMap<>();
            this.classMethods = new HashMap<>();
    }

    /**
     * Le contexte qui possede cette classe.
     * Permet aux primitives de retrouver nil, true, false,
     * et de fabriquer des objets sans aucun etat statique.
     */
    public MTContext getContext() {
        return context;
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

        // Cote classe : on cherche dans les methodes de classe
        MTMethod method = mtClass.lookupInstanceMethod(selector);

        if (method == null) {
            throw new MTException(
                "Message non compris : " + name
                + " (classe) >> " + selector);
        }

        return method.invoke(this, arguments);
    }

    @Override
    public MTString asString() {
        return context.newString(name);
    }

    @Override
    public String toString() {
        return name;
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

    public void addInstanceMethod(MTSymbol selector, MTMethod method) {
        instMethods.put(selector, method);
    }

    public void addClassMethod(MTSymbol selector, MTMethod method) {
        classMethods.put(selector, method);
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

    //
    // Reflexivite (07/11)
    //

    /**
     * Clefs brutes des methodes d'instance LOCALES, pour
     * la primitive instSelectors. Le tri vit dans la
     * primitive, pas ici.
     */
    public Set<MTSymbol> getInstSelectors() {
        return instMethods.keySet();
    }

    /**
     * Clefs brutes des methodes de classe LOCALES.
     */
    public Set<MTSymbol> getClassSelectors() {
        return classMethods.keySet();
    }

    /**
     * Retire une methode d'instance LOCALE (07/11).
     * Symetrique de addInstanceMethod. Rend la methode
     * retiree ; une methode absente est une erreur
     * explicite, pas un silence. Une methode modifiable
     * est une donnee -- ca commence par savoir l'enlever.
     */
    public MTMethod removeInstanceMethod(MTSymbol selector) {
        MTMethod removed = instMethods.remove(selector);

        if (removed == null) {
            throw new MTException(
                "methode absente : " + name
                + " >> " + selector);
        }

        return removed;
    }

    /**
     * Retire une methode de classe LOCALE (07/11).
     */
    public MTMethod removeClassMethod(MTSymbol selector) {
        MTMethod removed = classMethods.remove(selector);

        if (removed == null) {
            throw new MTException(
                "methode absente : " + name
                + " (classe) >> " + selector);
        }

        return removed;
    }

    /**
     * Methode d'instance LOCALE (07/11) : la REND, donc
     * en fait une valeur (reification). LOCALE, au
     * contraire de lookupInstanceMethod qui remonte la
     * chaine : le nom, pas ce que le monde comprend.
     */
    public MTMethod instanceMethodAt(MTSymbol selector) {
        MTMethod method = instMethods.get(selector);

        if (method == null) {
            throw new MTException(
                "methode absente : " + name
                + " >> " + selector);
        }

        return method;
    }

    /**
     * Methode de classe LOCALE (07/11).
     */
    public MTMethod classMethodAt(MTSymbol selector) {
        MTMethod method = classMethods.get(selector);

        if (method == null) {
            throw new MTException(
                "methode absente : " + name
                + " (classe) >> " + selector);
        }

        return method;
    }

    /**
     * Remplacement ATOMIQUE d'une methode d'instance
     * (07/11) : un seul put, jamais de fenetre ou la
     * methode n'existe pas -- c'est le dividende de
     * Map.put. Remplacer n'est pas ajouter : absent =
     * erreur explicite et la classe reste INCHANGEE
     * (le put est annule) ; l'ajout reste
     * addInstanceMethod, canal du bootstrap.
     */
    public MTMethod replaceInstanceMethod(
            MTSymbol selector,
            MTMethod method) {

        MTMethod previous = instMethods.put(selector, method);

        if (previous == null) {
            instMethods.remove(selector);
            throw new MTException(
                "methode absente : " + name
                + " >> " + selector);
        }

        return previous;
    }

    /**
     * Remplacement atomique d'une methode de classe
     * (07/11).
     */
    public MTMethod replaceClassMethod(
            MTSymbol selector,
            MTMethod method) {

        MTMethod previous = classMethods.put(selector, method);

        if (previous == null) {
            classMethods.remove(selector);
            throw new MTException(
                "methode absente : " + name
                + " (classe) >> " + selector);
        }

        return previous;
    }

    //
    // Alias pragmatiques (07/11) : le raccourci delegue,
    // la logique vit dans UN seul endroit.
    //

    /** Alias de addInstanceMethod. */
    public void addInstMethod(MTSymbol selector, MTMethod method) {
        addInstanceMethod(selector, method);
    }

    /** Alias de removeInstanceMethod. */
    public MTMethod removeInstMethod(MTSymbol selector) {
        return removeInstanceMethod(selector);
    }

    /** Alias de replaceInstanceMethod. */
    public MTMethod replaceInstMethod(
            MTSymbol selector, MTMethod method) {
        return replaceInstanceMethod(selector, method);
    }

    /** Alias de instanceMethodAt. */
    public MTMethod instMethodAt(MTSymbol selector) {
        return instanceMethodAt(selector);
    }

    /** Alias de lookupInstanceMethod. */
    public MTMethod lookupInstMethod(MTSymbol selector) {
        return lookupInstanceMethod(selector);
    }
}
