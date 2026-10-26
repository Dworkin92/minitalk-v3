package mt.runtime;

import java.util.List;
import java.util.stream.Collectors;

import mt.runtime.exceptions.MTException;

/**
 * Tableau MiniTalk. Fabrique : MTContext.newArray(List).
 *
 * Le litteral #(...) reconstruit un tableau a chaque
 * evaluation : deux litteraux identiques ne sont PAS le
 * meme objet (== faux) mais sont egaux par valeur (= vrai).
 *
 * Affichage : #(1 2 3), tableaux imbriques inclus.
 * Les chaines s'affichent sans guillemets a l'interieur
 * (affichage minimal, a affiner le moment venu).
 */
public final class MTArray
        implements MTObject {

    private final MTClass mtClass;
    private final List<MTObject> elements;

    MTArray(MTClass mtClass, List<MTObject> elements) {
        this.mtClass = mtClass;
        this.elements = List.copyOf(elements);
    }

    public List<MTObject> javaValue() {
        return elements;
    }

    @Override
    public MTClass getMTClass() {
        return mtClass;
    }

    @Override
    public MTObject send(
            MTSymbol selector,
            MTObject... arguments) {

        MTMethod method = mtClass.lookupInstanceMethod(selector);

        if (method == null) {
            throw new MTException(
                "Message non compris : Array >> " + selector);
        }

        return method.invoke(this, arguments);
    }

    @Override
    public MTString asString() {
        return mtClass.getContext().newString(toString());
    }

    @Override
    public String toString() {
        return "#("
            + elements.stream()
                .map(Object::toString)
                .collect(Collectors.joining(" "))
            + ")";
    }
}
