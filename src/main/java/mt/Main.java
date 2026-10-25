package mt;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.antlr.v4.runtime.misc.ParseCancellationException;

import mt.bootstrap.MTBootstrap;
import mt.interpreter.MTInterpreter;
import mt.runtime.MTObject;
import mt.runtime.exceptions.MTException;

/**
 * REPL : Read Eval Print Loop. Lit un source
 * MiniTalk sur l'entree standard, l'evalue,
 * affiche le resultat, recommence.
 */
public class Main {

    /**
     * Etat d'un source MiniTalk : nombre de crochets
     * et parentheses ouverts, et chaine en cours.
     * Sert au REPL a savoir si l'entree est complete.
     */
    private static final class SourceState {
        int depth = 0;
        boolean inString = false;
    }

    /**
     * Met a jour l'etat en parcourant le source.
     * Les apostrophes echappees ('') sont
     * respectees : un '' ne referme pas une chaine.
     */
    private static void scan(String source, SourceState state) {

        for (int i = 0; i < source.length(); i++) {
            char c = source.charAt(i);

            if (state.inString) {
                if (c == '\'') {
                    if (i + 1 < source.length()
                            && source.charAt(i + 1) == '\'') {
                        i++;
                    } else {
                        state.inString = false;
                    }
                }
            } else if (c == '\'') {
                state.inString = true;
            } else if (c == '[' || c == '(') {
                state.depth++;
            } else if (c == ']' || c == ')') {
                state.depth--;
            }
        }
    }

    public static void main(String[] args) throws Exception {

        MTBootstrap bootstrap = new MTBootstrap();
        bootstrap.initialize();

        MTInterpreter interpreter =
            new MTInterpreter(bootstrap.getContext());

        BufferedReader in = new BufferedReader(
            new InputStreamReader(System.in));

        System.out.println("MiniTalk V3 -- 'quit' pour sortir");

        while (true) {
            StringBuilder source = new StringBuilder();
            String prompt = "mt> ";

            // Lecture multi-lignes : tant que crochets,
            // parentheses ou chaines ne sont pas fermes,
            // on continue avec l'invite '...>'.
            while (true) {
                System.out.print(prompt);
                System.out.flush();

                String line = in.readLine();

                if (line == null) {
                    System.out.println("Bye.");
                    return;
                }

                // quit/exit ne vivent qu'au sommet :
                // en continuation, c'est du contenu
                if (source.length() == 0
                        && (line.equals("quit")
                            || line.equals("exit"))) {
                    System.out.println("Bye.");
                    return;
                }

                // Une premiere ligne vide relance l'invite
                if (source.length() == 0 && line.isBlank()) {
                    break;
                }

                source.append(line).append('\n');

                SourceState state = new SourceState();
                scan(source.toString(), state);

                if (!state.inString && state.depth <= 0) {
                    break;
                }

                prompt = "...> ";
            }

            if (source.toString().isBlank()) {
                continue;
            }

            try {
                MTObject result =
                    interpreter.evaluate(source.toString());

                // print a pu laisser la ligne ouverte :
                // l'echo repart sur sa propre ligne
                if (mt.runtime.primitives
                        .MTObjectPrimitives.lineOpen) {
                    System.out.println();
                    mt.runtime.primitives
                        .MTObjectPrimitives.lineOpen = false;
                }

                System.out.println(result.asString());

            } catch (MTException e) {
                // Une erreur ne tue pas le REPL :
                // on l'affiche et on continue
                System.out.println("! " + e.getMessage());

            } catch (ParseCancellationException e) {
                // Erreur de syntaxe (BailErrorStrategy) :
                // meme traitement, le REPL survit
                System.out.println("! Erreur de syntaxe");
            }
        }
    }
}
