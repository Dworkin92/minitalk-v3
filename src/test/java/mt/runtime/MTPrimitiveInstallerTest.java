package mt.runtime;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import mt.runtime.exceptions.MTException;
import mt.runtime.primitives.MTPrimitiveInstaller;
import mt.runtime.primitives.Primitive;

public class MTPrimitiveInstallerTest {

    /** Une primitive qui crashe en Java pur : le
     * remballage doit nommer le SELECTEUR MiniTalk. */
    static class Bombes {

        @Primitive("boum")
        public static MTObject boum(
                MTObject receiver, MTObject... arguments) {
            throw new IllegalStateException("pan");
        }
    }

    @Test
    void echecPrimitiveNommeLeSelecteurMiniTalk() {
        MTContext world = MTContext.newStandard();
        MTClass cible = world.getObjectClass();

        MTPrimitiveInstaller.install(cible, Bombes.class);

        MTException e = assertThrows(MTException.class,
            () -> cible.send(MTSymbol.intern("boum")));

        // Le LANGAGE d'abord, la mecanique ensuite :
        assertTrue(e.getMessage()
            .contains("Echec primitive : boum"));
        assertTrue(e.getMessage()
            .contains("(boum)"));
    }
}
