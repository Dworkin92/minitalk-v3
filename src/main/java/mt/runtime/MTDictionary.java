package mt.runtime;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import mt.runtime.exceptions.MTException;

/**
 * Dictionnaire MiniTalk. Fabrique : MTContext.newDictionary().
 *
 * Les cles se comparent PAR VALEUR, comme partout dans le
 * langage (decision du 05/11) : { 2 => 'a' } at: 2.0 rend
 * 'a' parce que 2 = 2.0. Le hachage suit l'egalite (hash
 * par valeur pour les objets de valeur, profond pour les
 * tableaux, par identite pour le reste) : hash et egalite
 * sont toujours d'accord.
 *
 * Prober la table avec une cle de type incompatible
 * ('a' cherche parmi des cles entieres) est une ABSENCE,
 * pas une erreur -- contrairement a l'egalite stricte du
 * 02/11, la table sonde, elle ne compare pas.
 *
 * Le litteral {...} reconstruit un dictionnaire a chaque
 * evaluation, comme #(...).
 *
 * Affichage : Dictionary(3) -- minimal, a affiner (les
 * tableaux ont commence pareil).
 */
public final class MTDictionary
        implements MTObject {

    private final MTClass mtClass;
    private final Map<Key, Pair> entries;

    MTDictionary(MTClass mtClass) {
        this.mtClass = mtClass;
        this.entries = new LinkedHashMap<>();
    }

    //
    // API Java pour les primitives et l'AST
    //

    public void put(MTObject key, MTObject value) {
        entries.put(new Key(key), new Pair(key, value));
    }

    /** Rend null si absente (les primitives traduisent) */
    public MTObject get(MTObject key) {
        Pair pair = entries.get(new Key(key));
        return pair == null ? null : pair.value();
    }

    /** Rend null si absente */
    public MTObject removeKey(MTObject key) {
        Pair pair = entries.remove(new Key(key));
        return pair == null ? null : pair.value();
    }

    public int size() {
        return entries.size();
    }

    /** Ordre d'insertion (LinkedHashMap) */
    public List<MTObject> keys() {
        List<MTObject> keys = new ArrayList<>();
        for (Pair pair : entries.values()) {
            keys.add(pair.key());
        }
        return keys;
    }

    /** Ordre d'insertion, en accord avec keys */
    public List<MTObject> values() {
        List<MTObject> values = new ArrayList<>();
        for (Pair pair : entries.values()) {
            values.add(pair.value());
        }
        return values;
    }

    //
    // Cles de hachage : hash PRECALCULE (par valeur, en
    // accord avec l'egalite) + egalite par valeur. La sonde
    // d'une table n'est pas une comparaison de langage :
    // un type incompatible est une absence, jamais une
    // MTException.
    //

    private record Pair(MTObject key, MTObject value) {
    }

    private record Key(MTObject object, int hash) {

        Key(MTObject object) {
            this(object, MTDictionary.hashOf(object));
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof Key k
                && hash == k.hash
                && MTDictionary.valueEquals(
                    object, k.object);
        }

        @Override
        public int hashCode() {
            return hash;
        }
    }

    /**
     * Hash par valeur pour les objets de valeur : 2 et
     * 2.0 ont le MEME hash (meme cle). Profond pour les
     * tableaux. Par identite pour le reste (singletons,
     * symboles uniques par monde, blocs, classes).
     */
    public static int hashOf(MTObject object) {

        if (object instanceof MTInteger i) {
            return Double.valueOf(
                (double) i.javaValue()).hashCode();
        }

        if (object instanceof MTFloat f) {
            return Double.valueOf(
                f.javaValue()).hashCode();
        }

        if (object instanceof MTCharacter c) {
            return Integer.hashCode(c.javaValue());
        }

        if (object instanceof MTString s) {
            return s.javaValue().hashCode();
        }

        if (object instanceof MTArray a) {
            int result = 1;
            for (MTObject element : a.javaValue()) {
                result = 31 * result + hashOf(element);
            }
            return result;
        }

        return System.identityHashCode(object);
    }

    /**
     * Egalite par valeur, miroir exact des primitives '='
     * de chaque classe (jeu ferme d'objets de valeur).
     * Un type incompatible est une ABSENCE : false,
     * jamais une MTException -- la table sonde.
     */
    public static boolean valueEquals(
            MTObject left,
            MTObject right) {

        if (left.identityEquals(right)) {
            return true;
        }

        // Egalite numerique inter-types : 2 = 2.0
        if (left instanceof MTInteger a
                && right instanceof MTInteger b) {
            return a.javaValue() == b.javaValue();
        }

        if (left instanceof MTFloat a
                && right instanceof MTFloat b) {
            return a.javaValue() == b.javaValue();
        }

        if (left instanceof MTInteger a
                && right instanceof MTFloat b) {
            return a.javaValue() == b.javaValue();
        }

        if (left instanceof MTFloat a
                && right instanceof MTInteger b) {
            return a.javaValue() == b.javaValue();
        }

        if (left instanceof MTCharacter a
                && right instanceof MTCharacter b) {
            return a.javaValue() == b.javaValue();
        }

        if (left instanceof MTString a
                && right instanceof MTString b) {
            return a.javaValue().equals(b.javaValue());
        }

        // Profond pour les tableaux, comme Array>>=
        if (left instanceof MTArray a
                && right instanceof MTArray b) {

            if (a.javaValue().size()
                    != b.javaValue().size()) {
                return false;
            }

            for (int i = 0;
                    i < a.javaValue().size(); i++) {

                if (!valueEquals(
                        a.javaValue().get(i),
                        b.javaValue().get(i))) {
                    return false;
                }
            }

            return true;
        }

        // Types differents ou objets a identite :
        // pas la meme cle.
        return false;
    }

    //
    // MTObject
    //

    @Override
    public MTClass getMTClass() {
        return mtClass;
    }

    @Override
    public MTObject send(
            MTSymbol selector,
            MTObject... arguments) {

        MTMethod method =
            mtClass.lookupInstanceMethod(selector);

        if (method == null) {
            throw new MTException(
                "Message non compris : Dictionary >> "
                + selector);
        }

        return method.invoke(this, arguments);
    }

    @Override
    public MTString asString() {
        return mtClass.getContext().newString(toString());
    }

    @Override
    public String toString() {
        return "Dictionary(" + entries.size() + ")";
    }
}
