package mt.runtime;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class MTIntegerPrimitivesTest {

    private static MTContext context;

    @BeforeAll
    static void setup() {
        context = MTContext.newStandard();
    }

    @Test
    void addition() {
        MTObject a = context.newInteger(2);
        MTObject b = context.newInteger(3);

        MTObject result = a.send(MTSymbol.intern("+"), b);

        assertEquals(5, ((MTInteger) result).javaValue());
    }

    @Test
    void multiplicationEtPriorite() {
        // (2 + 3) * 4 = 20, fait a la main :
        MTObject sum =
            context.newInteger(2).send(MTSymbol.intern("+"), context.newInteger(3));

        MTObject result =
            sum.send(MTSymbol.intern("*"), context.newInteger(4));

        assertEquals(20, ((MTInteger) result).javaValue());
    }

    @Test
    void comparaison() {
        MTObject result =
            context.newInteger(2).send(MTSymbol.intern("<"), context.newInteger(3));

        assertSame(context.trueObject(), result);
    }

    @Test
    void egaliteValeur() {
        MTObject result =
            context.newInteger(42).send(MTSymbol.intern("="), context.newInteger(42));

        assertSame(context.trueObject(), result);
    }

    @Test
    void divisionParZeroLeveMTException() {
        assertThrows(mt.runtime.exceptions.MTException.class,
            () -> context.newInteger(1).send(
                MTSymbol.intern("/"), context.newInteger(0)));
    }

    @Test
    void asString() {
        MTObject result = context.newInteger(42).send(
            MTSymbol.intern("asString"));

        assertEquals("42", result.toString());
    }

    @Test
    void deuxContextsSontIndependants() {
        MTContext autre = MTContext.newStandard();

        MTObject ici   = context.newInteger(7);
        MTObject laBas = autre.newInteger(7);

        // Meme valeur, mais classes et singletons distincts :
        assertTrue(ici.send(MTSymbol.intern("="), laBas)
            instanceof MTBoolean);
        assertNotSame(context.trueObject(), autre.trueObject());
        assertNotSame(context.nil(), autre.nil());
    }
}
