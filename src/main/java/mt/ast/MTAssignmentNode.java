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

        MTObject result = value.eval(context, scope);

        boolean found = scope.assign(
            MTSymbol.intern(name), result);

        if (!found) {
            throw new MTException(
                "Variable non declaree : " + name);
        }

        return result;
    }
}
