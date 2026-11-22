package mt.runtime.exceptions;

import mt.runtime.MTObject;

/**
 * (22/11) Sortie anticipee du bloc courant : ^ valeur.
 *
 * Signal interne du runtime, PAS une erreur du langage.
 * Attrape a la frontiere de TOUTE invocation de bloc
 * (MTBlock.invoke et MTBlock.evaluateIn), donc un ^
 * ne traverse JAMAIS une frontiere de bloc : il sort
 * du bloc COURANT seulement -- divergence assumee avec
 * Smalltalk, dont le ^ remonte toute la chaine des
 * blocs (ARCHITECTURE.md, chapitre ^).
 *
 * Sans pile : le depliage est le geste frequent du
 * langage, et la pile Java ne dirait rien que le
 * source ne dise deja.
 */
public final class MTReturnSignal
        extends RuntimeException {

    private final MTObject value;

    public MTReturnSignal(MTObject value) {
        super(null, null, false, false);
        this.value = value;
    }

    public MTObject value() {
        return value;
    }
}
