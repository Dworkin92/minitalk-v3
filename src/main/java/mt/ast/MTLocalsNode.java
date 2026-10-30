package mt.ast;

import java.util.List;

import mt.runtime.MTContext;
import mt.runtime.MTObject;
import mt.runtime.MTScope;
import mt.runtime.MTSymbol;

/**
 * Declarations de temporaires en tete d'un programme :
 * | a b | ... Elles vivent dans la portee racine (le
 * programme est a la portee de bloc ce qu'un corps de
 * methode est a Smalltalk). Initialisees a nil, comme
 * les temporaires de bloc.
 */
public record MTLocalsNode(
        List<String> names,
        MTNode body) implements MTNode {

    @Override
    public MTObject eval(MTContext context, MTScope scope) {

        for (String name : names) {
            scope.define(
                MTSymbol.intern(name),
                context.nil());
        }

        return body.eval(context, scope);
    }
}
