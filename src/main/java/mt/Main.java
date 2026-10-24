package mt;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import mt.bootstrap.MTBootstrap;
import mt.interpreter.MTInterpreter;
import mt.runtime.MTObject;
import mt.runtime.exceptions.MTException;

/**
 * REPL : Read Eval Print Loop. Lit une ligne
 * MiniTalk sur l'entree standard, l'evalue,
 * affiche le resultat, recommence.
 */
public class Main {

    public static void main(String[] args) throws Exception {

        MTBootstrap bootstrap = new MTBootstrap();
        bootstrap.initialize();

        MTInterpreter interpreter =
            new MTInterpreter(bootstrap.getContext());

        BufferedReader in = new BufferedReader(
            new InputStreamReader(System.in));

        System.out.println("MiniTalk V3 -- 'quit' pour sortir");

        while (true) {
            System.out.print("mt> ");
            System.out.flush();

            String line = in.readLine();

            if (line == null || line.equals("quit")) {
                break;
            }

            if (line.isBlank()) {
                continue;
            }

            try {
                MTObject result =
                    interpreter.evaluate(line);

                System.out.println(result.asString());

            } catch (MTException e) {
                // Une erreur ne tue pas le REPL :
                // on l'affiche et on continue
                System.out.println("! " + e.getMessage());
            }
        }

        System.out.println("Bye.");
    }
}
