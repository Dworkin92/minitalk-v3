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
public class MTScope {

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

        if (variables.containsKey(name)) {
            return variables.get(name);
        }

        return (parent != null)
            ? parent.lookup(name)
            : null;
    }

    /**
     * Reassigne la variable la plus proche.
     * Retourne false si aucune portee ne la definit.
     */
    public boolean assign(MTSymbol name, MTObject value) {
        MTScope current = this;

        if (variables.containsKey(name)) {
            variables.put(name, value);
            return true;
        }

        return (parent != null)
            && parent.assign(name, value);
    }

    /**
     * La cible d'une affectation est-elle CONNUE ?
     * Sans effet de bord : la garde de l'echec
     * precoce la consulte AVANT d'evaluer le membre
     * droit (18/11). Delegation parent a parent --
     * une sous-classe-vue ajoute SES declarations.
     */
    public boolean canAssign(MTSymbol name) {
        if (variables.containsKey(name)) {
            return true;
        }

        return (parent != null)
            && parent.canAssign(name);
    }
}