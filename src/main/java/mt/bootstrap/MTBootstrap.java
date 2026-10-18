package mt.bootstrap;

import mt.runtime.MTClass;
import mt.runtime.MTInteger;
import mt.runtime.MTContext;
import mt.runtime.MTInstance;
import mt.runtime.MTObject;
import mt.runtime.MTSymbol;

/**
 * Bootstrap : construit un contexte standard et fait fumer
 * l'usine. Le contexte possede tout ; le bootstrap n'est
 * qu'un point d'entree.
 */
public final class MTBootstrap {

    private MTContext context;

    public void initialize() {
        context = MTContext.newStandard();
        smokeTest();
    }

    public MTContext getContext() {
        return context;
    }

    public MTClass getObjectClass() {
        return context.getObjectClass();
    }

    public MTClass getClassClass() {
        return context.getClassClass();
    }

    public MTClass getNilClass() {
        return context.getNilClass();
    }

    private void smokeTest() {

        MTObject nil = context.nil();
        MTObject obj = new MTInstance(context.getObjectClass());

        System.out.println("Object            = " + context.getObjectClass().getName());
        System.out.println("Object.class      = " + context.getObjectClass().getMTClass().getName());
        System.out.println("nil isNil         = " + nil.send(MTSymbol.intern("isNil")));
        System.out.println("obj notNil        = " + obj.send(MTSymbol.intern("notNil")));
        System.out.println("nil ifNil: (42)   = " + nil.send(MTSymbol.intern("ifNil:"), context.newInteger(42)));
    }
}
