package mt.runtime;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import mt.runtime.exceptions.MTException;

/**
 * Reflexivite (07/11). MONDE A SOI : chaque test retire
 * ou remplace une methode -- jamais dans le contexte
 * partage des autres suites.
 */
public class MTReflexivityTest {

    private final MTContext world = MTContext.newStandard();

    //
    // Couche Java
    //

    @Test
    void retirerUneMethodeLaRendIncomprise() {
        MTClass integerClass = world.getIntegerClass();

        MTMethod removed = integerClass.removeInstanceMethod(
            MTSymbol.intern("negated"));

        assertNotNull(removed);

        MTException e = assertThrows(MTException.class,
            () -> world.newInteger(5)
                        .send(MTSymbol.intern("negated")));

        assertEquals(
            "Message non compris : Integer >> negated",
            e.getMessage());
    }

    @Test
    void retirerUneMethodeAbsenteEstUneErreurExplicite() {
        MTException e = assertThrows(MTException.class,
            () -> world.getIntegerClass()
                        .removeInstanceMethod(
                            MTSymbol.intern("foo:")));

        assertEquals(
            "methode absente : Integer >> foo:",
            e.getMessage());
    }

    @Test
    void remplacerEstAtomiqueEtEffectif() {
        MTClass integerClass = world.getIntegerClass();

        MTMethod zero = (receiver, arguments) ->
            integerClass.getContext().newInteger(0);

        MTMethod previous = integerClass.replaceInstanceMethod(
            MTSymbol.intern("negated"), zero);

        assertNotNull(previous);

        assertEquals(0,
            ((MTInteger) world.newInteger(5)
                .send(MTSymbol.intern("negated")))
                .javaValue());
    }

    @Test
    void remplacerUneMethodeAbsenteLaisseLaClasseInchangee() {
        MTClass integerClass = world.getIntegerClass();

        MTMethod phantom = (receiver, arguments) ->
            integerClass.getContext().newInteger(0);

        MTException e = assertThrows(MTException.class,
            () -> integerClass.replaceInstanceMethod(
                MTSymbol.intern("foo:"), phantom));

        assertEquals(
            "methode absente : Integer >> foo:",
            e.getMessage());

        assertNull(integerClass.lookupInstanceMethod(
            MTSymbol.intern("foo:")));
    }

    @Test
    void retirerUneMethodeDeClasse() {
        MTClass stringClass = world.getStringClass();

        stringClass.addClassMethod(
            MTSymbol.intern("greeting"),
            (receiver, arguments) ->
                stringClass.getContext().newString("bonjour"));

        MTMethod removed = stringClass.removeClassMethod(
            MTSymbol.intern("greeting"));

        assertNotNull(removed);
        assertNull(stringClass.lookupClassMethod(
            MTSymbol.intern("greeting")));
    }

    //
    // Couche langage (les messages de MTClassPrimitives)
    //

    @Test
    void superclassAuNiveauLangage() {
        // Integer superclass -> Object
        assertEquals(
            world.getObjectClass(),
            world.getIntegerClass()
                .send(MTSymbol.intern("superclass")));

        // Object superclass -> nil (la racine n'a pas de mere)
        assertEquals(
            world.nil(),
            world.getObjectClass()
                .send(MTSymbol.intern("superclass")));
    }

    @Test
    void instSelectorsTrieEtContientLesPrimitives() {
        MTArray selectors = (MTArray) world.getIntegerClass()
            .send(MTSymbol.intern("instSelectors"));

        List<MTObject> names = selectors.javaValue();

        // INVARIANT : la liste est TRIEE par nom. On
        // verifie l'ordre sur les adjacents, pas une
        // valeur figee -- les futurs selecteurs
        // d'Integer ne casseront pas ce test.
        for (int i = 0; i < names.size() - 1; i++) {
            String a = ((MTSymbolObject) names.get(i))
                .symbol().getValue();
            String b = ((MTSymbolObject) names.get(i + 1))
                .symbol().getValue();

            assertTrue(a.compareTo(b) < 0,
                "instSelectors mal trie : " + a + " >= " + b);
        }

        boolean found = false;
        for (MTObject name : names) {
            if (((MTSymbolObject) name)
                    .symbol().getValue().equals("negated")) {
                found = true;
            }
        }
        assertTrue(found);
    }

    @Test
    void removeInstMethodAuNiveauLangage() {
        world.getIntegerClass().send(
            MTSymbol.intern("removeInstMethod:"),
            world.symbolObject(MTSymbol.intern("negated")));

        MTException e = assertThrows(MTException.class,
            () -> world.newInteger(5)
                        .send(MTSymbol.intern("negated")));

        assertEquals(
            "Message non compris : Integer >> negated",
            e.getMessage());
    }
}
