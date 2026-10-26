package mt.ast;

import java.util.List;

import mt.runtime.MTContext;
import mt.runtime.MTObject;
import mt.runtime.MTScope;

/**
 * Litteral tableau #(...).
 *
 * Divergence assumee avec Smalltalk : les elements sont des
 * variables, pas des symboles. Le tableau est reconstruit a
 * CHAQUE evaluation : les variables sont lues a ce moment-la,
 * jamais figees a la compilation (decision du 25/10).
 */
public record MTArrayNode(List<MTNode> elements) implements MTNode {

    @Override
    public MTObject eval(MTContext context, MTScope scope) {

        java.util.ArrayList<MTObject> values =
            new java.util.ArrayList<>();

        for (MTNode element : elements) {
            values.add(element.eval(context, scope));
        }

        return context.newArray(values);
    }
}
