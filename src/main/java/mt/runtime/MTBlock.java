package mt.runtime;

import java.util.List;

import mt.runtime.exceptions.MTException;

/**
 * Bloc MiniTalk : une fermeture.
 *
 * Implemente MTObject (c'est un objet : on peut lui envoyer
 * value) et MTMethod (on peut l'invoquer comme argument de
 * ifTrue:, ifNil:, ...). Les primitives existantes qui
 * testent "instanceof MTMethod" fonctionnent donc telles
 * quelles.
 *
 * Le corps du bloc est pour l'instant une lambda Java (Body) ;
 * il sera remplace par l'AST quand l'interpreteur existera.
 */
public final class MTBlock
        implements MTObject, MTMethod {

    /**
     * Corps d'un bloc : evalue dans une portee fille de la
     * portee de definition, ou les parametres sont lies.
     */
    @FunctionalInterface
    public interface Body {
        MTObject value(MTScope scope);
    }

    private final MTClass blockClass;

    private final MTScope definingScope;

    private final MTSymbol[] parameters;

    private final Body body;

    MTBlock(
            MTClass blockClass,
            MTScope definingScope,
            List<MTSymbol> parameters,
            Body body) {

        this.blockClass = blockClass;
        this.definingScope = definingScope;
        this.parameters = parameters.toArray(new MTSymbol[0]);
        this.body = body;
    }

    /**
     * Invocation : lie les parametres dans une portee fille
     * de la portee de definition, puis evalue le corps.
     *
     * Le receiver est ignore : un bloc est une fermeture,
     * pas une methode liee a un objet.
     */
    @Override
    public MTObject invoke(
            MTObject receiver,
            MTObject... arguments) {

        if (arguments.length != parameters.length) {
            throw new MTException(
                "Bloc : " + parameters.length
                + " parametre(s) attendu(s), "
                + arguments.length + " fourni(s)");
        }

        MTScope scope = definingScope.extend();

        for (int i = 0; i < parameters.length; i++) {
            scope.define(parameters[i], arguments[i]);
        }

        return body.value(scope);
    }

    @Override
    public MTClass getMTClass() {
        return blockClass;
    }

    @Override
    public MTObject send(
            MTSymbol selector,
            MTObject... arguments) {

        MTMethod method = blockClass.lookupInstanceMethod(selector);

        if (method == null) {
            throw new MTException(
                "Message non compris : Block >> " + selector);
        }

        return method.invoke(this, arguments);
    }

    @Override
    public MTString asString() {
        return blockClass.getContext().newString("aBlock");
    }

    @Override
    public String toString() {
        return "aBlock";
    }
}
