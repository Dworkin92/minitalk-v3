package mt.interpreter;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import mt.bootstrap.MTBootstrap;
import mt.runtime.MTBoolean;
import mt.runtime.MTInteger;
import mt.runtime.MTObject;
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
    // Blocs et fermetures
    //

    @Test
    void blocAvecParametre() {
        MTObject result = run(
            "[:x | x + 1] value: 41");

        assertEquals(42, ((MTInteger) result).javaValue());
    }

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
    void sequenceRetourneLaDerniereValeur() {
        MTObject result = run(
            "[1. 2. 3] value");

        assertEquals(3, ((MTInteger) result).javaValue());
    }

    @Test
    void nilIfNilAvecUnVraiBloc() {
        MTObject result = run("nil ifNil: [99]");

        assertEquals(99, ((MTInteger) result).javaValue());
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
    // Affectation et temporaires
    //

    @Test
    void affectationEtTemporaire() {
        MTObject result = run(
            "[:x | | t | t := x + 1. t] value: 41");

        assertEquals(42, ((MTInteger) result).javaValue());
    }

    @Test
    void affectationASaValeur() {
        MTObject result = run(
            "[| t | t := 7] value");

        assertEquals(7, ((MTInteger) result).javaValue());
    }

    @Test
    void affectationAvecLaFleche() {
        MTObject result = run(
            "[| t | t <- 7. t] value");

        assertEquals(7, ((MTInteger) result).javaValue());
    }

    @Test
    void reaffectationAccumule() {
        MTObject result = run(
            "[| t | t := 1. t := t + 1. t] value");

        assertEquals(2, ((MTInteger) result).javaValue());
    }

    @Test
    void temporaireInitialiseeANil() {
        MTObject result = run(
            "[| t | t isNil] value");

        assertTrue(((MTBoolean) result).javaValue());
    }

    @Test
    void variableNonDeclareeLeveMTException() {
        assertThrows(MTException.class,
            () -> run("[:x | y := 1] value: 1"));
    }

    @Test
    void fermetureCaptureUneTemporaire() {
        // Le bloc interne capture t par fermeture
        MTObject result = run(
            "[| t | t := 10. [t + 5] value] value");

        assertEquals(15, ((MTInteger) result).javaValue());
    }

    //
    // whileTrue:
    //

    @Test
    void boucleSommeDe1A10() {
        MTObject result = run(
            "[| i total |"
            + " i := 1. total := 0."
            + " [i <= 10] whileTrue: ["
            + "     total := total + i."
            + "     i := i + 1"
            + " ]."
            + " total"
            + "] value");

        assertEquals(55, ((MTInteger) result).javaValue());
    }

    @Test
    void boucleJamaisExecuteeRetourneZero() {
        MTObject result = run(
            "[false] whileTrue: [1]");

        assertEquals(0, ((MTInteger) result).javaValue());
    }

    @Test
    void boucleCompteSesIterations() {
        MTObject result = run(
            "[| i | i := 0."
            + " [i < 3] whileTrue: [i := i + 1]] value");

        // La valeur de whileTrue: : 3 iterations,
        // derniere instruction du bloc englobant
        assertEquals(3, ((MTInteger) result).javaValue());
    }

    @Test
    void conditionInvalideLeveMTException() {
        // Une condition doit etre un booleen,
        // ni une chaine...
        assertThrows(MTException.class,
            () -> run("['abc'] whileTrue: [1]"));
    }

    @Test
    void conditionEntiereLeveMTException() {
        // ... ni un entier (pas de verite a la C :
        // decision du 24/10, strict booleens)
        assertThrows(MTException.class,
            () -> run("[| i | i := 3."
                + " [i] whileTrue: [i := i - 1]] value"));
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
