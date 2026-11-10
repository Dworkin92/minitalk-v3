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

    @Test
    void toDoInvoqueLeBlocDeUnACinq() {
        int[] somme = { 0 };

        MTBlock block = context.newBlock(
            new MTScope(null),
            java.util.List.of(MTSymbol.intern("i")),
            scope -> {
                somme[0] += ((MTInteger)
                    scope.lookup(MTSymbol.intern("i"))).javaValue();
                return scope.lookup(MTSymbol.intern("i"));
            });

        // Le recepteur garde son identite :
        MTInteger un = context.newInteger(1);

        MTObject result = un.send(
            MTSymbol.intern("to:do:"),
            context.newInteger(5), block);

        // Somme des 1..5, sans aucun tableau :
        assertEquals(15, somme[0]);

        // Convention do: : le RECEVEUR est rendu.
        assertSame(un, result);
    }

    @Test
    void toDoBornesInverseesNInvoqueJamaisLeBloc() {
        boolean[] invoque = { false };

        MTBlock block = context.newBlock(
            new MTScope(null),
            java.util.List.of(MTSymbol.intern("i")),
            scope -> {
                invoque[0] = true;
                return scope.lookup(MTSymbol.intern("i"));
            });

        MTObject result = context.newInteger(5).send(
            MTSymbol.intern("to:do:"),
            context.newInteger(1), block);

        assertFalse(invoque[0]);
        // Symetrique du tableau vide de to: : le
        // recepteur rendu tel quel.
        assertEquals(5,
            ((MTInteger) result).javaValue());
    }

    @Test
    void toByDoMonteDeDeuxEnDeux() {
        int[] somme = { 0 };

        MTBlock block = context.newBlock(
            new MTScope(null),
            java.util.List.of(MTSymbol.intern("i")),
            scope -> {
                somme[0] += ((MTInteger)
                    scope.lookup(MTSymbol.intern("i"))).javaValue();
                return scope.lookup(MTSymbol.intern("i"));
            });

        MTInteger un = context.newInteger(1);

        MTObject result = un.send(
            MTSymbol.intern("to:by:do:"),
            context.newInteger(9),
            context.newInteger(2), block);

        // 1 + 3 + 5 + 7 + 9 :
        assertEquals(25, somme[0]);
        assertSame(un, result);
    }

    @Test
    void toByDoDescendAvecUnPasNegatif() {
        int[] somme = { 0 };

        MTBlock block = context.newBlock(
            new MTScope(null),
            java.util.List.of(MTSymbol.intern("i")),
            scope -> {
                somme[0] += ((MTInteger)
                    scope.lookup(MTSymbol.intern("i"))).javaValue();
                return scope.lookup(MTSymbol.intern("i"));
            });

        context.newInteger(10).send(
            MTSymbol.intern("to:by:do:"),
            context.newInteger(1),
            context.newInteger(-3), block);

        // 10 + 7 + 4 + 1 :
        assertEquals(22, somme[0]);
    }

    @Test
    void toByDoRefuseLePasNul() {
        mt.runtime.exceptions.MTException e =
            assertThrows(
                mt.runtime.exceptions.MTException.class,
                () -> context.newInteger(1).send(
                    MTSymbol.intern("to:by:do:"),
                    context.newInteger(5),
                    context.newInteger(0),
                    context.newBlock(
                        new MTScope(null),
                        java.util.List.of(),
                        scope -> context.nil())));

        assertTrue(e.getMessage().contains("Pas nul"));
    }

    @Test
    void toDoRefuseUnNonBloc() {
        mt.runtime.exceptions.MTException e =
            assertThrows(
                mt.runtime.exceptions.MTException.class,
                () -> context.newInteger(1).send(
                    MTSymbol.intern("to:do:"),
                    context.newInteger(5),
                    context.newInteger(42)));

        assertTrue(e.getMessage()
            .contains("n'est pas un Block"));
    }
}
