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
}
