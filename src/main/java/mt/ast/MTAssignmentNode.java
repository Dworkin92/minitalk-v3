package mt.ast;

import mt.runtime.MTContext;
import mt.runtime.MTObject;
import mt.runtime.MTScope;
import mt.runtime.MTSymbol;
import mt.runtime.exceptions.MTException;

/**
 * var := expression
 * Reaffecte la variable la plus proche dans la chaine
 * des portees ; refuse une variable non declaree.
 * Valeur de l'affectation : celle de l'expression.
 */
public record MTAssignmentNode(String name, MTNode value) implements MTNode {

    @Override
    public MTObject eval(MTContext context, MTScope scope) {

        MTSymbol target = MTSymbol.intern(name);

        // ECHEC PRECOCE (18/11) : la cible est
        // verifiee AVANT l'evaluation du membre
        // droit -- une ligne refusee ne produit
        // AUCUN effet de bord.
        if (!scope.canAssign(target)) {
            throw new MTException(
                "Variable non declaree : " + name);
        }

        MTObject result = value.eval(context, scope);

        boolean found = scope.assign(target, result);

        if (!found) {
            throw new MTException(
                "Variable non declaree : " + name);
        }

        return result;
    }
}
