package mt.interpreter;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import mt.bootstrap.MTBootstrap;
import mt.runtime.MTInteger;
import mt.runtime.MTObject;
import mt.runtime.MTString;
import mt.runtime.exceptions.MTException;

public class MTInterpreterTest {

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
    // Le test de feu : la fermeture ecrite en MiniTalk
    //

    @Test
    void blocAvecParametre() {
        MTObject result = run(
            "[:x | x + 1] value: 41");

        assertEquals(42, ((MTInteger) result).javaValue());
    }

    //
    // Messages
    //

    @Test
    void messageBinaireGaucheADroite() {
        MTObject result = run("3 + 4 * 2");

        // (3 + 4) * 2, pas 3 + (4 * 2) !
        assertEquals(14, ((MTInteger) result).javaValue());
    }

    @Test
    void unairePlusPrioritaireQueBinaire() {
        MTObject result = run("'abc' size + 1");

        // ('abc' size) + 1
        assertEquals(4, ((MTInteger) result).javaValue());
    }

    @Test
    void messageAMotCle() {
        MTObject result = run(
            "false ifTrue: [1] ifFalse: [2]");

        assertEquals(2, ((MTInteger) result).javaValue());
    }

    @Test
    void cascadeDeMessagesBinairesSurChaine() {
        MTObject result = run(
            "'hello' , ' ' , 'world'");

        assertEquals("hello world", result.toString());
    }

    //
    // Litteraux
    //

    @Test
    void chaineAvecEchappement() {
        MTObject result = run("'l''eau' size");

        assertEquals(5, ((MTInteger) result).javaValue());
    }

    @Test
    void programmeVideRetourneNil() {
        MTObject result = run("");

        assertTrue(result.isNil());
    }

    //
    // Blocs
    //

    @Test
    void blocSansParametre() {
        MTObject result = run("[42] value");

        assertEquals(42, ((MTInteger) result).javaValue());
    }

    @Test
    void fermetureCaptureLaPorteeDeDefinition() {
        // Le bloc interieur capture x par fermeture
        MTObject result = run(
            "[:x | [:y | x + y] value: 1] value: 2");

        assertEquals(3, ((MTInteger) result).javaValue());
    }

    @Test
    void nilIfNilAvecUnVraiBloc() {
        MTObject result = run("nil ifNil: [99]");

        assertEquals(99, ((MTInteger) result).javaValue());
    }

    @Test
    void sequenceRetourneLaDerniereValeur() {
        MTObject result = run(
            "[1. 2. 3] value");

        assertEquals(3, ((MTInteger) result).javaValue());
    }

    //
    // Erreurs
    //

    @Test
    void variableNonDefinieLeveMTException() {
        assertThrows(MTException.class,
            () -> run("variableInconnue"));
    }

    @Test
    void messageNonComprisLeveMTException() {
        assertThrows(MTException.class,
            () -> run("1 messageQuiNExistePas"));
    }
}
