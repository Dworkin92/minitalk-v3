package mt.runtime;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class MTNilPrimitivesTest {

    private static MTContext context;

    @BeforeAll
    static void setup() {
        context = MTContext.newStandard();
    }

    @Test
    void nilRepondIsNil() {
        MTObject nil = context.nil();
        MTObject result = nil.send(MTSymbol.intern("isNil"));
        assertSame(context.trueObject(), result);
    }

    @Test
    void nilEstDeClasseNil() {
        assertEquals("Nil",
            context.nil().getMTClass().getName());
    }

    @Test
    void lookupRemonteJusquAObject() {
        // isNil est defini sur Nil, identity sur Object :
        MTMethod m = context.getNilClass()
            .lookupInstanceMethod(MTSymbol.intern("identity"));
        assertNotNull(m); // herite d'Object
    }

    @Test
    void messageNonComprisLeveUneException() {
        assertThrows(RuntimeException.class,
            () -> context.nil().send(MTSymbol.intern("kamoulox")));
    }
}
