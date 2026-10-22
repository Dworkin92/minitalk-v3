package mt.runtime;

import java.util.HashMap;
import java.util.Map;

/**
 * Portee lexicale. Chaine de portees : lookup et assign
 * remontent vers la portee englobante.
 *
 * Un bloc est evalue dans une portee fille de sa portee
 * de definition (fermeture).
 */
public final class MTScope {

    private final MTScope parent;

    private final Map<MTSymbol, MTObject> variables;

    public MTScope(MTScope parent) {
        this.parent = parent;
        this.variables = new HashMap<>();
    }

    /**
     * Portee fille, pour l'evaluation d'un bloc.
     */
    public MTScope extend() {
        return new MTScope(this);
    }

    /**
     * Definit une variable dans cette portee.
     */
    public void define(MTSymbol name, MTObject value) {
        variables.put(name, value);
    }

    /**
     * Remonte la chaine des portees ; null si introuvable.
     */
    public MTObject lookup(MTSymbol name) {
        MTScope current = this;

        while (current != null) {
            if (current.variables.containsKey(name)) {
                return current.variables.get(name);
            }

            current = current.parent;
        }

        return null;
    }

    /**
     * Reassigne la variable la plus proche.
     * Retourne false si aucune portee ne la definit.
     */
    public boolean assign(MTSymbol name, MTObject value) {
        MTScope current = this;

        while (current != null) {
            if (current.variables.containsKey(name)) {
                current.variables.put(name, value);
                return true;
            }

            current = current.parent;
        }

        return false;
    }
}