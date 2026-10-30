package mt.interpreter;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import mt.bootstrap.MTBootstrap;
import mt.runtime.MTBoolean;
import mt.runtime.MTFloat;
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
    // Temporaires de programme
    //

    @Test
    void programmeAvecTemporaires() {
        MTObject r = run("| t | t := 7. t");
        assertEquals(7, ((MTInteger) r).javaValue());
    }

    @Test
    void temporaireDeProgrammeInitialiseeANil() {
        MTObject r = run("| t | t isNil");
        assertTrue(((MTBoolean) r).javaValue());
    }

    @Test
    void plusieursTemporairesDeProgramme() {
        MTObject r =
            run("| a b | a := 1. b := 2. a + b");
        assertEquals(3, ((MTInteger) r).javaValue());
    }

    @Test
    void blocVoitLaTemporaireDeProgramme() {
        // Fermeture : le bloc capture la portee racine
        MTObject r =
            run("| t | t := 10. [t + 5] value");
        assertEquals(15, ((MTInteger) r).javaValue());
    }

    @Test
    void affectationSansDeclarationToujoursErreur() {
        // Le garde-fou tient : sans | x |, rien ne declare
        assertThrows(MTException.class,
            () -> run("x := 1"));
    }

    @Test
    void declarationsUniquementEnTete() {
        // | u | au milieu : refuse par la grammaire
        // (erreur de syntaxe, pas d'execution)
        assertThrows(
            org.antlr.v4.runtime.misc.
                ParseCancellationException.class,
            () -> run("x := 1. | u | u := 2"));
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
    void signeMoinsSurLitteral() {
        MTObject result = run("-6");

        assertEquals(-6, ((MTInteger) result).javaValue());
    }

    @Test
    void signePlusEstIdentite() {
        MTObject result = run("+6");

        assertEquals(6, ((MTInteger) result).javaValue());
    }

    @Test
    void doubleNegationEntreParentheses() {
        MTObject result = run("-(-3)");

        // --3 est refuse par la grammaire ; la forme
        // mathematique -(-3) est la facon canonique.
        assertEquals(3, ((MTInteger) result).javaValue());
    }

    @Test
    void signeApresOperateur() {
        MTObject result = run("3 * -4");

        assertEquals(-12, ((MTInteger) result).javaValue());
    }

    @Test
    void signePlusHautQuePuissance() {
        MTObject result = run("-2 ** 2");

        // ARCHITECTURE.md : le signe est le niveau le
        // plus haut, donc (-2) ** 2 = 4.
        // (Python aurait dit -(2 ** 2) = -4 !)
        assertEquals(4, ((MTInteger) result).javaValue());
    }

    @Test
    void moinsBinaireInchange() {
        MTObject result = run("0 - 6");

        assertEquals(-6, ((MTInteger) result).javaValue());
    }

    @Test
    void multiplicationAvantAdditionAOS() {
        MTObject result = run("3 + 4 * 2");

        // AOS (ARCHITECTURE.md) : 3 + (4 * 2)
        assertEquals(11, ((MTInteger) result).javaValue());
    }

    @Test
    void parenthesesSeules() {
        MTObject result = run("(3 + 4)");

        assertEquals(7, ((MTInteger) result).javaValue());
    }

    @Test
    void parenthesesForcentLeGroupe() {
        MTObject result = run("(3 + 4) * 2");

        assertEquals(14, ((MTInteger) result).javaValue());
    }

    @Test
    void puissanceDroiteAssociative() {
        MTObject result = run("2 ** 3 ** 2");
  
        // Droite associatif, convention math/Python :
        // 2 ** (3 ** 2) = 2 ** 9 = 512.
        // PAS (2 ** 3) ** 2 = 64 !
        assertEquals(512, ((MTInteger) result).javaValue());
    }

    @Test
    void puissanceEntreParenthesesForceLaGauche() {
        MTObject result = run("(2 ** 3) ** 2");

        assertEquals(64, ((MTInteger) result).javaValue());
    }

    @Test
    void modulo() {
        MTObject result = run("7 % 3");

        assertEquals(1, ((MTInteger) result).javaValue());
    }

    @Test
    void additionGaucheADroite() {
        MTObject result = run("10 - 4 - 3");

        // Au sein d'un niveau : gauche a droite = 3
        assertEquals(3, ((MTInteger) result).javaValue());
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
    // Float (brique du 29/10)
    //

    @Test
    void floatLitteral() {
        MTObject r = run("3.5");
        assertEquals(3.5, ((MTFloat) r).javaValue(), 1e-12);
    }

    @Test
    void promotionLocaleAddition() {
        // 1 + 2.5 : le Float a droite fait
        // passer l'operation en Float.
        MTObject r = run("1 + 2.5");
        assertEquals(3.5, ((MTFloat) r).javaValue(), 1e-12);
    }

    @Test
    void divisionEntiereResteEntiere() {
        // L'exemple type de la promotion LOCALE :
        // 7 / 3 est entier (2), puis 2 + 3.6 = 5.6.
        // Pour 5.9333... : 7 asFloat / 3 + 3.6.
        MTObject r = run("7 / 3 + 3.6");
        assertEquals(5.6, ((MTFloat) r).javaValue(), 1e-12);
    }

    @Test
    void asFloatForceLaDivisionReelle() {
        MTObject r = run("7 asFloat / 3");
        assertEquals(7.0 / 3, ((MTFloat) r).javaValue(), 1e-9);
    }

    @Test
    void puissanceRacineCarree() {
        // 2.0 ** 0.5 : racine, pas de cast (int)
        MTObject r = run("2.0 ** 0.5");
        assertEquals(Math.sqrt(2),
            ((MTFloat) r).javaValue(), 1e-12);
    }

    @Test
    void egaliteNumeriqueInterTypes() {
        // 2 = 2.0 : comparaison numerique (29/10)
        MTObject r = run("2 = 2.0");
        assertTrue(((MTBoolean) r).javaValue());
    }

    @Test
    void affichageFloatEntierGardeLeTypeVisible() {
        MTObject r = run("2.0 asString");
        assertEquals("2.0", ((MTString) r).javaValue());
    }

    @Test
    void negationUnaireSurFloat() {
        MTObject r = run("-2.5");
        assertEquals(-2.5, ((MTFloat) r).javaValue(), 1e-12);
    }

    @Test
    void divisionParZeroFloatLeveMTException() {
        assertThrows(MTException.class,
            () -> run("1.5 / 0.0"));
    }

    @Test
    void moduloFloat() {
        MTObject r = run("7.5 % 2");
        assertEquals(1.5, ((MTFloat) r).javaValue(), 1e-12);
    }

    @Test
    void asIntegerTronqueVersZero() {
        MTObject r = run("2.9 asInteger");
        assertEquals(2, ((MTInteger) r).javaValue());
    }

    @Test
    void floatDansUnArrayLitteral() {
        MTObject r = run("#(1 2.5) size");
        assertEquals(2, ((MTInteger) r).javaValue());
    }
    
    //
    // affichage
    //
    @Test
    void printRetourneLeRecepteur() {
        MTObject result = run("'hello' print");

        assertTrue(result instanceof mt.runtime.MTString);
    }

    @Test
    void printLnRetourneLeRecepteur() {
        MTObject result = run("(3 + 4) printLn");

        assertTrue(result instanceof mt.runtime.MTInteger);
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
    // Character (brique du 29/10)
    //

    @Test
    void characterLitteral() {
        MTObject r = run("$h");
        assertEquals(104,
            ((mt.runtime.MTCharacter) r).javaValue());
    }

    @Test
    void atRendUnCharacter() {
        MTObject r = run("'hello' at: 1");
        assertTrue(r instanceof mt.runtime.MTCharacter);
    }

    @Test
    void characterRoundTrip() {
        // at: est un mot-cle : son argument est une
        // binaryExpression, donc 1 = $h serait evalue
        // d'abord. Parentheses obligatoires.
        MTObject r = run("('hello' at: 1) = $h");
        assertTrue(((MTBoolean) r).javaValue());
    }

    @Test
    void characterAsString() {
        MTObject r = run("$h asString");
        assertEquals("h", ((MTString) r).javaValue());
    }

    @Test
    void characterAsInteger() {
        MTObject r = run("$h asInteger");
        assertEquals(104, ((MTInteger) r).javaValue());
    }

    @Test
    void characterDansUnArrayLitteral() {
        MTObject r = run("#($a $b) size");
        assertEquals(2, ((MTInteger) r).javaValue());
    }

    @Test
    void characterSpecialDollar() {
        // $$ est le caractere dollar lui-meme
        MTObject r = run("$$");
        assertEquals(36,
            ((mt.runtime.MTCharacter) r).javaValue());
    }

    //
    // Les chaines de caracteres
    //

    @Test
    void tailleDeChaine() {
        MTObject r = run("'hello' size");
        assertEquals(5, ((MTInteger) r).javaValue());
    }

    @Test
    void atPremierCaractere() {
        MTObject r = run("'hello' at: 1");
        // codepoint de 'h' = 104, porte par un MTCharacter
        assertEquals(104,
            ((mt.runtime.MTCharacter) r).javaValue());
    }

    @Test
    void egaliteValeurPasIdentite() {
        MTObject r = run("'abc' = 'abc'");
        assertTrue(((MTBoolean) r).javaValue()); // ou newBoolean(true) selon vos habitudes
    }

    @Test
    void renversement() {
        MTObject r = run("'hello' reverse");
        assertEquals("olleh", ((MTString) r).javaValue());
    }

    @Test
    void indexOfAbsentRetourneZero() {
        MTObject r = run("'hello' indexOf: 'z'");
        assertEquals(0, ((MTInteger) r).javaValue());
    }

    @Test
    void containsBooleen() {
        MTObject r = run("'hello' contains: 'ell'");
        assertTrue(((MTBoolean) r).javaValue());
    }

    @Test
    void concatenationDeuxCharactersDonneUneString() {
        MTObject r = run("$a , $b");
        assertEquals("ab", ((MTString) r).javaValue());
    }

    @Test
    void concatenationCharacterPuisString() {
        MTObject r = run("$a , 'bc'");
        assertEquals("abc", ((MTString) r).javaValue());
    }

    @Test
    void concatenationStringPuisCharacter() {
        MTObject r = run("'ab' , $c");
        assertEquals("abc", ((MTString) r).javaValue());
    }

    @Test
    void concatenationTroisCharactersChainee() {
        // Associatif a gauche : ($a , $b) donne 'ab',
        // puis 'ab' , $c. Le resultat reste une String.
        MTObject r = run("$a , $b , $c");
        assertEquals("abc", ((MTString) r).javaValue());
    }

    @Test
    void characterRepeteDonneUneString() {
        MTObject r = run("$a * 5");
        assertEquals("aaaaa",
            ((MTString) r).javaValue());
    }

    @Test
    void stringRepetee() {
        MTObject r = run("'ab' * 3");
        assertEquals("ababab",
            ((MTString) r).javaValue());
    }

    @Test
    void repetitionParZeroDonneChaineVide() {
        MTObject r = run("$a * 0");
        assertEquals("",
            ((MTString) r).javaValue());
    }

    @Test
    void repetitionNegativeLeveMTException() {
        assertThrows(MTException.class,
            () -> run("$a * -1"));
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
    // and:/or: paresseux
    //

    @Test
    void andCourtCircuitNeLevePasErreur() {
        // Le bloc n'est jamais evalue : la division
        // par zero dormante ne reveille pas.
        MTObject r = run("false and: [1 / 0]");
        assertFalse(((MTBoolean) r).javaValue());
    }

    @Test
    void orCourtCircuitNeLevePasErreur() {
        MTObject r = run("true or: [1 / 0]");
        assertTrue(((MTBoolean) r).javaValue());
    }

    @Test
    void andEvalueLeBlocQuandVrai() {
        MTObject r = run("true and: [1 + 1 = 2]");
        assertTrue(((MTBoolean) r).javaValue());
    }

    @Test
    void orEvalueLeBlocQuandFaux() {
        MTObject r = run("false or: [3 > 2]");
        assertTrue(((MTBoolean) r).javaValue());
    }

    @Test
    void andAvecBooleanNuResteValide() {
        // Retrocompatibilite : l'operande peut etre
        // un Boolean, pas seulement un bloc.
        MTObject r = run("true and: false");
        assertFalse(((MTBoolean) r).javaValue());
    }

    @Test
    void andBlocNonBooleenLeveMTException() {
        // Strict booleens : le bloc doit repondre
        // un Boolean, pas un entier.
        assertThrows(MTException.class,
            () -> run("true and: [42]"));
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

    @Test
    void boucleWhileFalseCompteSesIterations() {
        MTObject result = run(
            "[| i | i := 0."
            + " [i >= 3] whileFalse: [i := i + 1]] value");

        assertEquals(3, ((MTInteger) result).javaValue());
    }

    @Test
    void whileFalseJamaisExecuteeRetourneZero() {
        MTObject result = run(
            "[true] whileFalse: [1]");

        assertEquals(0, ((MTInteger) result).javaValue());
    }

    @Test
    void whileFalseConditionEntiereLeveMTException() {
        assertThrows(MTException.class,
            () -> run("[| i | i := 0."
                + " [i] whileFalse: [i := i + 1]] value"));
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

        //
    // to:by: et includes: (suite de la journee)
    //

    @Test
    void intervalleAvecPas() {
        MTObject r = run("(1 to: 10 by: 2) size");
        assertEquals(5, ((MTInteger) r).javaValue());
    }

    @Test
    void intervalleAvecPasDernierElement() {
        MTObject r = run("(1 to: 10 by: 2) at: 5");
        assertEquals(9, ((MTInteger) r).javaValue());
    }

    @Test
    void pasNegatifDescend() {
        // 10 to: 1 by: -3 -> #(10 7 4 1)
        MTObject r = run("(10 to: 1 by: -3) size");
        assertEquals(4, ((MTInteger) r).javaValue());
    }

    @Test
    void bornesMalOrienteesDonnentTableauVide() {
        MTObject r = run("(1 to: 10 by: -2) size");
        assertEquals(0, ((MTInteger) r).javaValue());
    }

    @Test
    void pasNulLeveMTException() {
        assertThrows(MTException.class,
            () -> run("1 to: 10 by: 0"));
    }

    @Test
    void includesPresent() {
        MTObject r = run("#(1 2 3) includes: 2");
        assertTrue(((MTBoolean) r).javaValue());
    }

    @Test
    void includesAbsent() {
        MTObject r = run("#(1 2 3) includes: 5");
        assertFalse(((MTBoolean) r).javaValue());
    }

    @Test
    void includesEgaliteProfonde() {
        // Les chaines se comparent par valeur
        MTObject r = run("#('a' 'b') includes: 'a'");
        assertTrue(((MTBoolean) r).javaValue());
    }

    @Test
    void includesTableauImbrique() {
        // Le = profond des tableaux sert includes: aussi
        MTObject r = run("#(#(1 2) 3) includes: #(1 2)");
        assertTrue(((MTBoolean) r).javaValue());
    }

    @Test
    void includesNumeriqueInterTypes() {
        // Consequence de l'egalite numerique (29/10) :
        // 2 = 2.0 est vrai, donc includes: 2.0 trouve 2.
        MTObject r = run("#(1 2) includes: 2.0");
        assertTrue(((MTBoolean) r).javaValue());
    }
}
