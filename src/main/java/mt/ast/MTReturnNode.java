package mt.ast;

import mt.runtime.MTContext;
import mt.runtime.MTObject;
import mt.runtime.MTScope;
import mt.runtime.exceptions.MTReturnSignal;

/**
 * (22/11) ^ expression : sortie anticipee du bloc
 * COURANT avec la valeur de l'expression (ou de
 * l'affectation : ^ x := e rend la valeur affectee).
 * Le signal est attrape a la frontiere d'invocation
 * du bloc ; ce qui suit le ^ n'est jamais evalue.
 */
public record MTReturnNode(MTNode expression)
        implements MTNode {

    @Override
    public MTObject eval(
            MTContext context,
            MTScope scope) {

        throw new MTReturnSignal(
            expression.eval(context, scope));
    }
}
