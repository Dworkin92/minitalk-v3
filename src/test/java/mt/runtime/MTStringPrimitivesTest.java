package mt.runtime;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import mt.bootstrap.MTBootstrap;

public class MTStringPrimitivesTest {

    private static MTBootstrap bootstrap;

    @BeforeAll
    static void setup() {
        bootstrap = new MTBootstrap();
        bootstrap.initialize();
    }

    @Test
    void concatenation() {
        MTObject a = bootstrap.getContext().newString("foo");
        MTObject b = bootstrap.getContext().newString("bar");

        MTObject result = a.send(MTSymbol.intern(","), b);

        assertEquals("foobar", result.toString());
    }

    @Test
    void taille() {
        MTObject result = bootstrap.getContext()
            .newString("hello")
            .send(MTSymbol.intern("size"));

        assertEquals(5,
            ((MTInteger) result).javaValue());
    }

    @Test
    void egaliteDeValeurPasIdentite() {
        MTContext c = bootstrap.getContext();

        MTObject egal = c.newString("abc")
            .send(MTSymbol.intern("="), c.newString("abc"));
        assertSame(c.trueObject(), egal);

        MTObject identique = c.newString("abc")
            .send(MTSymbol.intern("=="), c.newString("abc"));
        assertSame(c.falseObject(), identique);
    }

    @Test
    void atEstUnBasedUn() {
        // 29/10 : at: rend un MTCharacter, fini le
        // codepoint brut en MTInteger (decision
        // style Smalltalk, comme $a).
        MTObject result = bootstrap.getContext()
            .newString("abc")
            .send(MTSymbol.intern("at:"),
                  bootstrap.getContext().newInteger(2));

        assertEquals((int) 'b',
            ((MTCharacter) result).javaValue());
    }

    @Test
    void atHorsBornesLeveMTException() {
        MTContext c = bootstrap.getContext();

        assertThrows(mt.runtime.exceptions.MTException.class,
            () -> c.newString("abc")
                .send(MTSymbol.intern("at:"), c.newInteger(4)));
    }

    @Test
    void chaineEnChaine() {
        MTObject original = bootstrap.getContext().newString("hello");
        MTObject result = original.send(MTSymbol.intern("asString"));

        assertSame(original, result);
    }

    /** (22/11) trim coupe l'espace blanc UNICODE (U+00A0 = insecable) */
    @Test
    void trimCoupeLespaceUnicode() {
        MTContext c = bootstrap.getContext();
        MTObject result = c.newString("  bonjour\u00A0")
            .send(MTSymbol.intern("trim"));
        assertEquals("bonjour", result.toString());
    }

    /** (22/11) bornes inclusives, 1-based */
    @Test
    void copyFromToExtraitBornesInclusives() {
        MTContext c = bootstrap.getContext();
        MTObject result = c.newString("hello")
            .send(MTSymbol.intern("copyFrom:to:"),
                  c.newInteger(2), c.newInteger(4));
        assertEquals("ell", result.toString());
    }

    /** (22/11) from > to : chaine vide, pas d'erreur */
    @Test
    void copyFromToInverseRendVide() {
        MTContext c = bootstrap.getContext();
        MTObject result = c.newString("hello")
            .send(MTSymbol.intern("copyFrom:to:"),
                  c.newInteger(3), c.newInteger(1));
        assertEquals("", result.toString());
    }

    /** (22/11) to: trop grand -> ramene a la fin */
    @Test
    void copyFromToBorneHauteRamenee() {
        MTContext c = bootstrap.getContext();
        MTObject result = c.newString("hello")
            .send(MTSymbol.intern("copyFrom:to:"),
                  c.newInteger(2), c.newInteger(10));
        assertEquals("ello", result.toString());
    }

    /** (22/11) from: trop grand -> ramene, debut apres fin -> vide */
    @Test
    void copyFromToDebutApresFinRendVide() {
        MTContext c = bootstrap.getContext();
        MTObject result = c.newString("hello")
            .send(MTSymbol.intern("copyFrom:to:"),
                  c.newInteger(7), c.newInteger(10));
        assertEquals("", result.toString());
    }

    /** (22/11) toutes les occurrences, sans chevauchement */
    @Test
    void replaceAllSansChevauchement() {
        MTContext c = bootstrap.getContext();
        MTObject result = c.newString("aaaa")
            .send(MTSymbol.intern("replaceAll:with:"),
                  c.newString("aa"), c.newString("X"));
        assertEquals("XX", result.toString());
    }

    /** (22/11) premiere occurrence seulement (AWK sub) */
    @Test
    void replaceFirstPremiereOccurrence() {
        MTContext c = bootstrap.getContext();
        MTObject result = c.newString("aaaa")
            .send(MTSymbol.intern("replaceFirst:with:"),
                  c.newString("aa"), c.newString("X"));
        assertEquals("Xaa", result.toString());
    }

    /** (22/11) motif absent : recepteur inchangé, requete pas exigence */
    @Test
    void remplacementAbsentRendInchange() {
        MTContext c = bootstrap.getContext();
        MTObject result = c.newString("abc")
            .send(MTSymbol.intern("replaceAll:with:"),
                  c.newString("z"), c.newString("X"));
        assertEquals("abc", result.toString());
    }

    /** (22/11) motif vide : refus propre */
    @Test
    void remplacementMotifVideRefuse() {
        MTContext c = bootstrap.getContext();
        assertThrows(mt.runtime.exceptions.MTException.class,
            () -> c.newString("abc")
                .send(MTSymbol.intern("replaceAll:with:"),
                      c.newString(""), c.newString("X")));
    }
}
