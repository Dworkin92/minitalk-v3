package mt;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MainTest {

    @Test
    void leCharApostropheNeFaussePasLeBalayage() {
        Main.SourceState state = new Main.SourceState();

        // $' est un CARACTERE, pas une chaine ouverte :
        Main.scan("x printAt: $'", state);

        assertFalse(state.inString);
        assertEquals(0, state.depth);
    }

    @Test
    void leCharDollarNeFaussePasLeBalayage() {
        Main.SourceState state = new Main.SourceState();

        Main.scan("a := $$.", state);

        assertFalse(state.inString);
    }

    @Test
    void uneVraieChaineOuverteResteOuverte() {
        Main.SourceState state = new Main.SourceState();

        Main.scan("'abc", state);

        // La contregarde : le balayage ordinaire
        // fonctionne toujours.
        assertTrue(state.inString);
    }
}
