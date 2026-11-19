package mt.runtime;

import mt.runtime.exceptions.MTException;

/**
 * Une methode MiniTalk : un bloc que la classe garde.
 * Le bloc vient d'un litteral ; seule change la portee
 * d'evaluation : une methode appartient a sa classe,
 * pas au site de definition.
 *
 * Chaine d'invocation :
 *   [ self + parametres + locales ]
 *      -> vue instVars (ou classVars) -> noms du monde.
 */
public final class MTBlockMethod
        implements MTMethod {

    private final MTClass owner;

    private final MTSymbol selector;

    private final MTBlock block;

    private final boolean classSide;

    public MTBlockMethod(
            MTClass owner,
            MTSymbol selector,
            MTBlock block,
            boolean classSide) {

        this.owner = owner;
        this.selector = selector;
        this.block = block;
        this.classSide = classSide;
    }

    @Override
    public MTObject invoke(
            MTObject receiver,
            MTObject... arguments) {

        MTSymbol[] parameters = block.parameters();

        if (arguments.length
                != parameters.length) {

            throw new MTException(
                "Arity mismatch : "
                + owner.getName() + " >> "
                + selector + " attend "
                + parameters.length
                + " parametre(s), "
                + arguments.length
                + " fourni(s)");
        }

        // Racine : les noms du monde
        MTScope chain = owner.getContext().globals();

        if (classSide) {
            if (!(receiver instanceof MTClass rcv)) {
                throw new MTException(
                    "classMethods : receveur de classe requis");
            }

            chain = new MTClassVarScope(
                chain, rcv);
        }
        else {
            // (19/11) Un receveur natif (MTInteger,
            // MTString, ...) n'est PAS une MTInstance :
            // pas de vue d'instVars a poser, mais self
            // doit vivre. La surcharge des builtins
            // -- le reve du 07/11 -- passe par ici.
            if (receiver instanceof MTInstance inst) {
                chain = new MTInstanceScope(
                    chain, inst);
            }
        }

        // self + parametres
        chain = chain.extend();

        chain.define(
            MTSymbol.intern("self"),
            receiver);

        for (int i = 0; i < parameters.length; i++) {
            chain.define(parameters[i], arguments[i]);
        }

        return block.evaluateIn(chain);
    }
}
