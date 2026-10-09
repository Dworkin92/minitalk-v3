package mt.runtime;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import mt.bootstrap.MTBootstrap;

public class MTNilPrimitivesTest {

    private static MTBootstrap bootstrap;

    @BeforeAll
    static void setup() {
        bootstrap = new MTBootstrap();
        bootstrap.initialize();
    }

    @Test
    void nilRepondIsNil() {
        MTObject nil = MTNil.instance();
        MTObject result = nil.send(MTSymbol.intern("isNil"));
        // assert depends de la brique Boolean — voir option (a)/(b)
    }

    @Test
    void nilEstDeClasseNil() {
        assertEquals("Nil",
            MTNil.instance().getMTClass().getName());
    }

    @Test
    void lookupRemonteJusquAObject() {
        // isNil est defini sur Nil, identity sur Object :
        MTMethod m = bootstrap.getNilClass()
            .lookupInstanceMethod(MTSymbol.intern("identity"));
        assertNotNull(m); // herite d'Object
    }

    @Test
    void messageNonComprisLeveUneException() {
        assertThrows(RuntimeException.class,
            () -> MTNil.instance().send(MTSymbol.intern("kamoulox")));
    }
}
