package mt.runtime;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import mt.runtime.exceptions.MTException;

/**
 * Classes definies en MiniTalk, phase A (08/11) :
 * subclass:, instVars:, accessors:/getters:/setters:,
 * new. MONDE A SOI : chaque test cree ses classes.
 */
public class MTClassesTest {

    private final MTContext world = MTContext.newStandard();

    private MTClass declarePerson() {
        MTClass person = (MTClass) world.getObjectClass()
            .send(MTSymbol.intern("subclass:"),
                world.symbolObject(MTSymbol.intern("Person")));

        person.send(MTSymbol.intern("instVars:"),
            world.newArray(java.util.List.of(
                world.symbolObject(MTSymbol.intern("name")),
                world.symbolObject(MTSymbol.intern("age")))));

        return person;
    }

    @Test
    void laNaissanceNeFaitQueNommer() {
        MTClass person = declarePerson();

        assertEquals("Person", person.getName());
        assertEquals(world.getObjectClass(),
            person.send(MTSymbol.intern("superclass")));
        assertEquals(person,
            world.lookupClass(MTSymbol.intern("Person")));

        // Regle du 07/11 tenue des la naissance :
        assertEquals(world.getClassClass(),
            person.getMTClass());

        // Pas d'accessibilite sans generation :
        MTObject p = person.send(MTSymbol.intern("new"));
        MTException e = assertThrows(MTException.class,
            () -> p.send(MTSymbol.intern("name")));
        assertEquals(
            "Message non compris : Person >> name",
            e.getMessage());
    }

    @Test
    void unNomDejaPrisEstRefuse() {
        declarePerson();

        MTException e = assertThrows(MTException.class,
            () -> world.getObjectClass().send(
                MTSymbol.intern("subclass:"),
                world.symbolObject(
                    MTSymbol.intern("Person"))));

        assertEquals("Classe existante : Person",
            e.getMessage());
    }

    @Test
    void accessorsGenereGetterEtSetter() {
        MTClass person = declarePerson();

        person.send(MTSymbol.intern("accessors:"),
            world.newArray(java.util.List.of(
                world.symbolObject(
                    MTSymbol.intern("name")))));

        MTObject p = person.send(MTSymbol.intern("new"));

        // Defaut : nil tant qu'on n'ecrit pas.
        assertEquals(world.nil(),
            p.send(MTSymbol.intern("name")));

        // Le setter rend la valeur affectee...
        assertEquals(42, ((MTInteger) p.send(
            MTSymbol.intern("name:"),
            world.newInteger(42))).javaValue());

        // ...et le getter la relit.
        assertEquals(42, ((MTInteger) p.send(
            MTSymbol.intern("name"))).javaValue());
    }

    @Test
    void gettersSeulNinstallePasLeSetter() {
        MTClass person = declarePerson();

        person.send(MTSymbol.intern("getters:"),
            world.newArray(java.util.List.of(
                world.symbolObject(
                    MTSymbol.intern("age")))));

        MTObject p = person.send(MTSymbol.intern("new"));

        assertEquals(world.nil(),
            p.send(MTSymbol.intern("age")));

        MTException e = assertThrows(MTException.class,
            () -> p.send(MTSymbol.intern("age:"),
                world.newInteger(1)));
        assertEquals(
            "Message non compris : Person >> age:",
            e.getMessage());
    }

    @Test
    void laGenerationExigeLaDeclarationPrealable() {
        MTClass person = declarePerson();

        MTException e = assertThrows(MTException.class,
            () -> person.send(MTSymbol.intern("accessors:"),
                world.newArray(java.util.List.of(
                    world.symbolObject(
                        MTSymbol.intern("salary"))))));

        assertEquals(
            "variable inconnue : Person >> salary",
            e.getMessage());
    }

    @Test
    void leDernierQuiCauseARaison() {
        MTClass person = declarePerson();

        // Un setter ECRIT A LA MAIN, plus fin que la
        // machine : il negativise.
        person.addInstanceMethod(MTSymbol.intern("age:"),
            (receiver, arguments) -> {
                ((MTInstance) receiver)
                    .setProperty(MTSymbol.intern("age"),
                        world.newInteger(
                            -((MTInteger) arguments[0])
                                .javaValue()));
                return arguments[0];
            });

        // La generation BALAIE la main :
        person.send(MTSymbol.intern("setters:"),
            world.newArray(java.util.List.of(
                world.symbolObject(
                    MTSymbol.intern("age")))));

        MTObject p = person.send(MTSymbol.intern("new"));
        p.send(MTSymbol.intern("age:"), world.newInteger(10));

        MTObject stored = ((MTInstance) p)
            .getProperty(MTSymbol.intern("age"));

        // La machine a gagne : 10, pas -10.
        assertEquals(10,
            ((MTInteger) stored).javaValue());
    }

    @Test
    void deuxInstancesSontIndependantes() {
        MTClass person = declarePerson();
        person.send(MTSymbol.intern("accessors:"),
            world.newArray(java.util.List.of(
                world.symbolObject(
                    MTSymbol.intern("name")))));

        MTObject a = person.send(MTSymbol.intern("new"));
        MTObject b = person.send(MTSymbol.intern("new"));

        a.send(MTSymbol.intern("name:"),
            world.newString("Alice"));

        assertEquals(world.nil(),
            b.send(MTSymbol.intern("name")));
    }
}
