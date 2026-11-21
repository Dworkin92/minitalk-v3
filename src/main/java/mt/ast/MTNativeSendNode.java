package mt.ast;

import java.util.List;

import mt.runtime.MTClass;
import mt.runtime.MTContext;
import mt.runtime.MTMethod;
import mt.runtime.MTObject;
import mt.runtime.MTSymbol;
import mt.runtime.MTScope;
import mt.runtime.exceptions.MTException;

/**
 * (19/11) p Integer@negated : envoie au RECEVEUR la
 * primitive NATIVE de la classe nommee. La fenetre @ :
 * le socle Java reste joignable meme si la table du
 * langage a ete ecrasee par le flux.
 */
public record MTNativeSendNode(
        MTNode receiver,
        String className,
        String selector,
        List<MTNode> arguments)
        implements MTNode {

    @Override
    public MTObject eval(MTContext context, MTScope scope) {

        MTClass cible =
            context.lookupClass(
                MTSymbol.intern(className));

        if (cible == null) {
            throw new MTException(
                "Classe inconnue : " + className);
        }

        MTMethod natif = cible.lookupNativeMethod(
            MTSymbol.intern(selector), false);

        if (natif == null) {
            throw new MTException(
                "Methode primitive absente : "
                + className + " >> " + selector);
        }

        MTObject[] evalues =
            new MTObject[arguments.size()];

        for (int i = 0; i < arguments.size(); i++) {
            evalues[i] =
                arguments.get(i).eval(context, scope);
        }

        return natif.invoke(
            receiver.eval(context, scope), evalues);
    }
}
