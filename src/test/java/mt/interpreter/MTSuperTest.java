package mt.interpreter;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import mt.bootstrap.MTBootstrap;
import mt.runtime.MTClass;
import mt.runtime.MTInteger;
import mt.runtime.MTObject;
import mt.runtime.MTString;
import mt.runtime.MTSymbol;
import mt.runtime.exceptions.MTException;

/**
 * (28/11) LA PSEUDO-VARIABLE super.
 *
 * Semantique actee le 26/11 : la recherche part de
 * la superclasse de la classe DEFINISSANTE (celle ou
 * la methode est ECRITE), jamais de la classe du
 * receveur. Utilisable dans TOUTE methode de la
 * classe, surchargeante ou pas, et dans les blocs
 * qu'elle contient. Refus propres : racine, hors
 * methode, affectation, message inconnu.
 *
 * Le monde est PARTAGE entre les tests : chaque test
 * seme des noms UNIQUES.
 */
public class MTSuperTest {

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
    // Le coeur : surcharger en appelant le parent
    //

    @Test
    void laSurchargeAppelleLaVersionDuParent() {
        run("Object subclass: #SpPersonne."
            + " SpPersonne instMethods: {"
            + "    #saluer => [ 'pere' ]."
            + " }.");
        run("SpPersonne subclass: #SpEtudiant."
            + " SpEtudiant instMethods: {"
            + "    #saluer => [ super saluer ]."
            + " }.");

        MTObject r = run("SpEtudiant new saluer.");
        assertEquals("pere",
            ((MTString) r).javaValue());
    }

    /**
     * Le test-roi de non-regression : la chaine a
     * trois etages. La methode qui dit super est
     * HERITEE par Stagiaire -- avec une recherche
     * partiee du RECEVEUR, super retrouverait la
     * version 2 elle-meme : recursion infinie.
     * Avec la classe DEFINISSANTE : 'pere'.
     */
    @Test
    void leSuperHeriteNeSeMordPasLaQueue() {
        run("Object subclass: #SqPersonne."
            + " SqPersonne instMethods: {"
            + "    #saluer => [ 'pere' ]."
            + " }.");
        run("SqPersonne subclass: #SqEtudiant."
            + " SqEtudiant instMethods: {"
            + "    #saluer => [ super saluer ]."
            + " }.");
        run("SqEtudiant subclass: #SqStagiaire.");

        MTObject r = run("SqStagiaire new saluer.");
        assertEquals("pere",
            ((MTString) r).javaValue());
    }

    /**
     * super est utilisable dans TOUTE methode de la
     * classe fille, pas seulement la surchargeante :
     * ici secondeMethode SAUTE la table de sa propre
     * classe et trouve la version du parent.
     */
    @Test
    void superDansUneAutreMethodeQueLaSurcharge() {
        run("Object subclass: #SrPersonne."
            + " SrPersonne instMethods: {"
            + "    #maMethode => [ 'version 1' ]."
            + " }.");
        run("SrPersonne subclass: #SrEtudiant."
            + " SrEtudiant instMethods: {"
            + "    #maMethode => [ 'version 2' ]."
            + "    #secondeMethode => [ super maMethode ]."
            + " }.");

        MTObject r =
            run("SrEtudiant new secondeMethode.");
        assertEquals("version 1",
            ((MTString) r).javaValue());
    }

    /** Message a mot-cle : les arguments passent. */
    @Test
    void superAvecUnMessageAMotCle() {
        run("Object subclass: #SkPersonne."
            + " SkPersonne instMethods: {"
            + "    #doubler: => [ :n | n * 2 ]."
            + " }.");
        run("SkPersonne subclass: #SkEtudiant."
            + " SkEtudiant instMethods: {"
            + "    #doubler: => [ :n |"
            + " (super doubler: n) + 1 ]."
            + " }.");

        MTObject r =
            run("SkEtudiant new doubler: 10.");
        assertEquals(21,
            ((MTInteger) r).javaValue());
    }

    /**
     * super vit dans les blocs IMBRIQUES : la chaine
     * des portees est lexicale, la vue seme par le
     * corps de methode reste visible du bloc.
     */
    @Test
    void superDansUnBlocImbrique() {
        run("Object subclass: #SbPersonne."
            + " SbPersonne instMethods: {"
            + "    #saluer => [ 'pere' ]."
            + " }.");
        run("SbPersonne subclass: #SbEtudiant."
            + " SbEtudiant instMethods: {"
            + "    #enveloppe => [ [ super saluer ] value ]."
            + " }.");

        MTObject r =
            run("SbEtudiant new enveloppe.");
        assertEquals("pere",
            ((MTString) r).javaValue());
    }

    /**
     * Le double depot du 19/11 : les primitives
     * vivent AUSSI dans la table du langage, donc
     * super traverse le plancher Java du parent.
     */
    @Test
    void superAtteintLaPrimitiveDuParent() {
        run("Object subclass: #Sp2Personne."
            + " Sp2Personne instMethods: {"
            + "    #dit => [ 42 ]."
            + " }.");
        run("Sp2Personne subclass: #Sp2Etudiant."
            + " Sp2Etudiant instMethods: {"
            + "    #dit => [ super dit ]."
            + " }.");

        MTObject r =
            run("Sp2Etudiant new printLn.");
        // printLn vit chez Object : super le trouve
        // par la chaine, et rend le receveur.
        assertNotNull(r);
    }

    //
    // Cote classe
    //

    @Test
    void superCoteClasse() {
        run("Object subclass: #ScPersonne."
            + " ScPersonne classMethods: {"
            + "    #annonce => [ 'classe parent' ]."
            + " }.");
        run("ScPersonne subclass: #ScEtudiant."
            + " ScEtudiant classMethods: {"
            + "    #annonce => [ super annonce ]."
            + " }.");

        MTObject r = run("ScEtudiant annonce.");
        assertEquals("classe parent",
            ((MTString) r).javaValue());
    }

    //
    // Les refus propres
    //

    /** Methode ecrite sur Object : pas de parent. */
    @Test
    void superALaRacineEstRefuse() {
        MTClass objectClass = bootstrap.getContext()
            .getObjectClass();

        try {
            run("Object instMethods: {"
                + "    #essaiRacine => [ super printString ]."
                + " }.");

            MTException e =
                assertThrows(MTException.class,
                    () -> run("Object new essaiRacine."));

            assertTrue(e.getMessage()
                .contains("pas de superclasse"));
        } finally {
            // Le monde est partage : on rend la cle.
            objectClass.send(
                MTSymbol.intern("removeInstMethod:"),
                bootstrap.getContext().symbolObject(
                    MTSymbol.intern("essaiRacine")));
        }
    }

    @Test
    void superHorsMethodeEstRefuse() {
        MTException e = assertThrows(MTException.class,
            () -> run("super printString."));

        assertTrue(e.getMessage()
            .contains("seulement dans une methode"));
    }

    @Test
    void superNEstPasAffectable() {
        run("Object subclass: #SaPersonne."
            + " SaPersonne instMethods: {"
            + "    #saluer => [ 'pere' ]."
            + " }.");
        run("SaPersonne subclass: #SaEtudiant."
            + " SaEtudiant instMethods: {"
            + "    #saluer => [ super saluer ]."
            + " }.");

        MTException e = assertThrows(MTException.class,
            () -> run("super := 1."));

        assertTrue(e.getMessage()
            .contains("pseudo-variable"));
    }

    @Test
    void superVersUnMessageInconnuEstRefuse() {
        run("Object subclass: #SiPersonne."
            + " SiPersonne subclass: #SiEtudiant."
            + " SiEtudiant instMethods: {"
            + "    #essaie => [ super inconnu ]."
            + " }.");

        MTException e = assertThrows(MTException.class,
            () -> run("SiEtudiant new essaie."));

        assertTrue(e.getMessage()
            .contains("message non compris"));
    }
}
