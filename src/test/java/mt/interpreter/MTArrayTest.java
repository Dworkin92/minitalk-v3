package mt.interpreter;

import static org.junit.jupiter.api.Assertions.*;

import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import mt.bootstrap.MTBootstrap;
import mt.runtime.MTBoolean;
import mt.runtime.MTInteger;
import mt.runtime.MTObject;
import mt.runtime.MTString;
import mt.runtime.exceptions.MTException;

public class MTArrayTest {

    private static MTBootstrap bootstrap;

    private static MTObject run(String source) {
        return new MTInterpreter(
            bootstrap.getContext()).evaluate(source);
    }

    @BeforeAll
    static void setup() {
        bootstrap = new MTBootstrap();
        bootstrap.initialize();
    }

    //
    // Litteral #(...)
    //

    @Test
    void tailleDuLitteral() {
        MTObject r = run("#(1 2 3) size");
        assertEquals(3, ((MTInteger) r).javaValue());
    }

    @Test
    void tableauVide() {
        MTObject r = run("#() isEmpty");
        assertTrue(((MTBoolean) r).javaValue());
    }

    @Test
    void atEstUnBased() {
        MTObject r = run("#(10 20 30) at: 2");
        assertEquals(20, ((MTInteger) r).javaValue());
    }

    @Test
    void atHorsBornesLeveMTException() {
        assertThrows(MTException.class,
            () -> run("#(10 20 30) at: 4"));
    }

    @Test
    void elementsSontDesVariables() {
        // Divergence assumee avec Smalltalk :
        // #(a b) evalue a et b, pas #a et #b.
        // Une temporaire de bloc declare x.
        MTObject r = run(
            "[:x | #(x x) at: 2] value: 5");

        assertEquals(5, ((MTInteger) r).javaValue());
    }

    @Test
    void tableauReconstruitAChaqueEvaluation() {
        // Le litteral n'est pas une constante : deux
        // evaluations construisent deux tableaux.
        MTObject r = run(
            "[| x y |"
            + " x := #(1)."
            + " y := #(1)."
            + " x == y] value");

        assertFalse(((MTBoolean) r).javaValue());
    }

    @Test
    void egaliteValeurDeuxLitteraux() {
        MTObject r = run("#(1 2 3) = #(1 2 3)");
        assertTrue(((MTBoolean) r).javaValue());
    }

    @Test
    void egaliteProfondeTableauxImbriques() {
        MTObject r = run(
            "#(#(1 2) 'abc') = #(#(1 2) 'abc')");

        assertTrue(((MTBoolean) r).javaValue());
    }

    @Test
    void egaliteRefuseeSiTaillesDifferentes() {
        MTObject r = run("#(1 2) = #(1 2 3)");
        assertFalse(((MTBoolean) r).javaValue());
    }

    @Test
    void tableauImbriqueDansLeLitteral() {
        // #( ) accepte un array imbrique comme element.
        MTObject r = run("#(#(9 8) 3) at: 1");

        mt.runtime.MTArray inner =
            (mt.runtime.MTArray) r;

        assertEquals(9,
            ((MTInteger) inner.javaValue().get(0))
                .javaValue());
        assertEquals(8,
            ((MTInteger) inner.javaValue().get(1))
                .javaValue());
    }

    @Test
    void expressionDansLeLitteralRefusee() {
        // #(1 + 2) : refuse par la grammaire, pas au
        // runtime. Pas de porte aux "petits marrants".
        assertThrows(ParseCancellationException.class,
            () -> run("#(1 + 2)"));
    }

    @Test
    void chaineDansLeLitteral() {
        MTObject r = run(
            "#('bonjour') at: 1");

        assertEquals("bonjour",
            ((MTString) r).javaValue());
    }

    //
    // Symboles
    //

    @Test
    void symboleLitteral() {
        MTObject r = run("#abc");
        assertTrue(r instanceof mt.runtime.MTSymbolObject);
    }

    @Test
    void symboleAsString() {
        // Comme en Smalltalk : le nom, sans le #.
        MTObject r = run("#abc asString");
        assertEquals("abc", ((MTString) r).javaValue());
    }

    @Test
    void deuxSymbolesIdentiquesSontIdentites() {
        // Cache du contexte : #a == #a dans le meme monde.
        MTObject r = run("#a == #a");
        assertTrue(((MTBoolean) r).javaValue());
    }

    @Test
    void symbolesDifferents() {
        MTObject r = run("#a = #b");
        assertFalse(((MTBoolean) r).javaValue());
    }

    @Test
    void tableauDeSymboles() {
        // Un tableau de symboles s'ecrit #(#a #b #c) :
        // moins joli, plus explicite, assume.
        MTObject r = run("#(#a #b) at: 2");

        assertEquals("b", r.asString().javaValue());
    }

    //
    // do: / collect: / select:
    //

    @Test
    void doSommeLesElements() {
        MTObject r = run(
            "[| total |"
            + " total := 0."
            + " #(1 2 3) do: [:x | total := total + x]."
            + " total"
            + "] value");

        assertEquals(6, ((MTInteger) r).javaValue());
    }

    @Test
    void doRetourneLeRecepteur() {
        MTObject r = run("#(1 2 3) do: [:x | x]");
        assertTrue(r instanceof mt.runtime.MTArray);
    }

    @Test
    void doAvecBlocSansParametreLeveMTException() {
        // Le bloc recoit un element par iteration.
        assertThrows(MTException.class,
            () -> run("#(1) do: [42]"));
    }

    @Test
    void collectDoubleLesElements() {
        MTObject r = run(
            "#(1 2 3) collect: [:x | x * 2]");

        MTObject deux = ((mt.runtime.MTArray) r)
            .javaValue().get(1);

        assertEquals(4, ((MTInteger) deux).javaValue());
    }

    @Test
    void selectGardeLesPairs() {
        MTObject r = run(
            "#(1 2 3 4) select: [:x | x % 2 = 0]");

        assertEquals(2,
            ((mt.runtime.MTArray) r).javaValue().size());
    }

    @Test
    void selectConditionStrictementBooleenne() {
        // Verite strictement booleenne (decision du
        // 24/10) : un bloc qui retourne un entier refuse.
        assertThrows(MTException.class,
            () -> run("#(1) select: [:x | x]"));
    }

    //
    // to:
    //

    @Test
    void toConstruitUnIntervalleInclusif() {
        MTObject r = run("1 to: 5");
        assertEquals(5,
            ((mt.runtime.MTArray) r).javaValue().size());
    }

    @Test
    void toTroisiemeElement() {
        MTObject r = run("(1 to: 5) at: 3");
        assertEquals(3, ((MTInteger) r).javaValue());
    }

    @Test
    void toInverseDonneTableauVide() {
        MTObject r = run("5 to: 1");
        assertTrue(
            ((mt.runtime.MTArray) r).javaValue().isEmpty());
    }
}
