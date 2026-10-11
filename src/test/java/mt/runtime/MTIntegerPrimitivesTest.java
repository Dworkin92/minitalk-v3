package mt.runtime;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import mt.bootstrap.MTBootstrap;

public class MTIntegerPrimitivesTest {

    private static MTBootstrap bootstrap;

    @BeforeAll
    static void setup() {
        bootstrap = new MTBootstrap();
        bootstrap.initialize();
    }

    @Test
    void addition() {
        MTObject a = MTInteger.of(2);
        MTObject b = MTInteger.of(3);

        MTObject result = a.send(MTSymbol.intern("+"), b);

        assertEquals(5, ((MTInteger) result).javaValue());
    }

    @Test
    void multiplicationEtPriorite() {
        // (2 + 3) * 4 = 20, fait a la main :
        MTObject sum =
            MTInteger.of(2).send(MTSymbol.intern("+"), MTInteger.of(3));

        MTObject result =
            sum.send(MTSymbol.intern("*"), MTInteger.of(4));

        assertEquals(20, ((MTInteger) result).javaValue());
    }

    @Test
    void comparaison() {
        MTObject result =
            MTInteger.of(2).send(MTSymbol.intern("<"), MTInteger.of(3));

        assertSame(MTBoolean.TRUE, result);
    }

    @Test
    void egaliteValeur() {
        MTObject result =
            MTInteger.of(42).send(MTSymbol.intern("="), MTInteger.of(42));

        assertSame(MTBoolean.TRUE, result);
    }

    @Test
    void divisionParZeroLeveMTException() {
        assertThrows(mt.runtime.exceptions.MTException.class,
            () -> MTInteger.of(1).send(
                MTSymbol.intern("/"), MTInteger.of(0)));
    }

    @Test
    void asString() {
        MTObject result = MTInteger.of(42).send(
            MTSymbol.intern("asString"));

        assertEquals("42", result.toString());
    }
}
