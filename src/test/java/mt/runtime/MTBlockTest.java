package mt.runtime;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import mt.bootstrap.MTBootstrap;

public class MTBlockTest {

    private static MTBootstrap bootstrap;

    @BeforeAll
    static void setup() {
        bootstrap = new MTBootstrap();
        bootstrap.initialize();
    }

    @Test
    void nilIfNilEvalueUnVraiBloc() {
        MTContext c = bootstrap.getContext();

        // [ 42 ] — un bloc sans parametre, portee racine
        MTBlock block = c.newBlock(
            new MTScope(null),
            List.of(),
            scope -> c.newInteger(42));

        MTObject result = c.nil()
            .send(MTSymbol.intern("ifNil:"), block);

        assertEquals(42, ((MTInteger) result).javaValue());
    }

    @Test
    void valueAvecUnParametreLieLaPortee() {
        MTContext c = bootstrap.getContext();

        // [ :x | x + 1 ] — capture le parametre dans la portee fille
        MTBlock block = c.newBlock(
            new MTScope(null),
            List.of(MTSymbol.intern("x")),
            scope -> {
                MTInteger x =
                    (MTInteger) scope.lookup(MTSymbol.intern("x"));
                return x.send(MTSymbol.intern("+"),
                              c.newInteger(1));
            });

        MTObject result = block.send(
            MTSymbol.intern("value:"),
            c.newInteger(41));

        assertEquals(42, ((MTInteger) result).javaValue());
    }

    @Test
    void fermetureCaptureSaPorteeDeDefinition() {
        MTContext c = bootstrap.getContext();

        // var facteur = 10, dans la portee de definition
        MTScope portee = new MTScope(null);
        portee.define(MTSymbol.intern("facteur"),
                      c.newInteger(10));

        // [ :x | x * facteur ] — capture facteur par clôture
        MTBlock block = c.newBlock(
            portee,
            List.of(MTSymbol.intern("x")),
            scope -> {
                MTInteger x =
                    (MTInteger) scope.lookup(MTSymbol.intern("x"));
                MTInteger facteur =
                    (MTInteger) scope.lookup(MTSymbol.intern("facteur"));
                return x.send(MTSymbol.intern("*"), facteur);
            });

        MTObject result = block.send(
            MTSymbol.intern("value:"),
            c.newInteger(4));

        assertEquals(40, ((MTInteger) result).javaValue());
    }

    @Test
    void valueSansParametre() {
        MTContext c = bootstrap.getContext();

        MTBlock block = c.newBlock(
            new MTScope(null),
            List.of(),
            scope -> c.newString("Bonjour !"));

        MTObject result =
            block.send(MTSymbol.intern("value"));

        assertEquals("Bonjour !", result.toString());
    }

    @Test
    void mauvaiseArretLeveMTException() {
        MTContext c = bootstrap.getContext();

        MTBlock block = c.newBlock(
            new MTScope(null),
            List.of(MTSymbol.intern("x")),
            scope -> c.nil());

        assertThrows(mt.runtime.exceptions.MTException.class,
            () -> block.send(MTSymbol.intern("value")));
    }
}
