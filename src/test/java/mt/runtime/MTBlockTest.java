package mt.runtime;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class MTBlockTest {

    private static MTContext context;

    @BeforeAll
    static void setup() {
        context = MTContext.newStandard();
    }

    @Test
    void valueSansParametreEvalueLeCorps() {
        MTBlock block = context.newBlock(
            new MTScope(null),
            List.of(),
            scope -> context.newInteger(42));

        MTObject result = block.send(MTSymbol.intern("value"));

        assertEquals(42, ((MTInteger) result).javaValue());
    }

    @Test
    void valueColonLieLeParametre() {
        MTBlock block = context.newBlock(
            new MTScope(null),
            List.of(MTSymbol.intern("x")),
            scope -> scope.lookup(MTSymbol.intern("x")));

        MTObject result = block.send(
            MTSymbol.intern("value:"),
            context.newInteger(7));

        assertEquals(7, ((MTInteger) result).javaValue());
    }

    @Test
    void leBlocCaptureSaPorteeDeDefinition() {
        // x est defini hors du bloc ; le bloc le voit :
        MTScope outer = new MTScope(null);
        outer.define(MTSymbol.intern("x"), context.newInteger(10));

        MTBlock block = context.newBlock(
            outer,
            List.of(),
            scope -> scope.lookup(MTSymbol.intern("x")));

        MTObject result = block.invoke(block);

        assertEquals(10, ((MTInteger) result).javaValue());
    }

    @Test
    void laPorteeDuBlocNEcrasePasCelleDeDefinition() {
        // definir x dans le bloc n'ecrase pas le x exterieur :
        MTScope outer = new MTScope(null);
        outer.define(MTSymbol.intern("x"), context.newInteger(1));

        MTBlock block = context.newBlock(
            outer,
            List.of(MTSymbol.intern("x")),
            scope -> {
                scope.define(MTSymbol.intern("x"), context.newInteger(99));
                return scope.lookup(MTSymbol.intern("x"));
            });

        block.invoke(block, context.newInteger(2));

        MTObject outerX = outer.lookup(MTSymbol.intern("x"));
        assertEquals(1, ((MTInteger) outerX).javaValue());
    }

    @Test
    void assignModifieLaVariableEnglobante() {
        MTScope outer = new MTScope(null);
        outer.define(MTSymbol.intern("compteur"), context.newInteger(0));

        MTBlock incr = context.newBlock(
            outer,
            List.of(),
            scope -> {
                int valeur =
                    ((MTInteger) scope.lookup(
                        MTSymbol.intern("compteur"))).javaValue();

                scope.assign(
                    MTSymbol.intern("compteur"),
                    context.newInteger(valeur + 1));

                return scope.lookup(MTSymbol.intern("compteur"));
            });

        incr.invoke(incr);
        incr.invoke(incr);
        incr.invoke(incr);

        MTObject compteur = outer.lookup(MTSymbol.intern("compteur"));
        assertEquals(3, ((MTInteger) compteur).javaValue());
    }

    @Test
    void mauvaisNombreDArgumentsLeveMTException() {
        MTBlock block = context.newBlock(
            new MTScope(null),
            List.of(MTSymbol.intern("a"), MTSymbol.intern("b")),
            scope -> context.nil());

        assertThrows(mt.runtime.exceptions.MTException.class,
            () -> block.invoke(block, context.newInteger(1)));
    }

    @Test
    void ifTrueInvoqueUnVraiBloc() {
        MTBlock block = context.newBlock(
            new MTScope(null),
            List.of(),
            scope -> context.newInteger(42));

        MTObject result = context.newBoolean(true)
            .send(MTSymbol.intern("ifTrue:"), block);

        assertEquals(42, ((MTInteger) result).javaValue());
    }

    @Test
    void ifTrueFauxNeInvoquePasLeBloc() {
        MTBlock block = context.newBlock(
            new MTScope(null),
            List.of(),
            scope -> context.newInteger(42));

        MTObject result = context.newBoolean(false)
            .send(MTSymbol.intern("ifTrue:"), block);

        assertSame(context.nil(), result);
    }

    @Test
    void ifNilInvoqueUnVraiBloc() {
        MTBlock block = context.newBlock(
            new MTScope(null),
            List.of(),
            scope -> context.newInteger(42));

        MTObject result = context.nil()
            .send(MTSymbol.intern("ifNil:"), block);

        assertEquals(42, ((MTInteger) result).javaValue());
    }
}
