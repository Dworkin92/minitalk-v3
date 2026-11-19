package mt.ast;

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
        String selector)
        implements MTNode {

    @Override
    public MTObject eval(MTContext context, MTScope scope) {
        // Malformed class name...
        MTClass maison =
            context.lookupClass(MTSymbol.intern(className));
        if (maison == null) {
            throw new MTException(
                "Classe inconnue : " + className);
        }

        MTMethod nativeMethod = maison.lookupNativeMethod(
            MTSymbol.intern(selector), false);

        if (nativeMethod == null) {
            throw new MTException(
                "Methode primitive absente : "
                + className + " >> " + selector);
        }

        return nativeMethod.invoke(
            receiver.eval(context, scope));
    }
}
