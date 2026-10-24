package mt.interpreter;

import mt.runtime.MTContext;
import mt.runtime.MTObject;
import mt.runtime.MTScope;
import mt.runtime.exceptions.MTException;

import mt.generated.MiniTalkLexer;
import mt.generated.MiniTalkParser;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import mt.ast.MTNode;

/**
 * Interprete du source MiniTalk.
 * Usage : MTInterpreter i = new MTInterpreter(context);
 *         i.evaluate("[ :x | x + 1 ] value: 41");   // -> 42
 */
public final class MTInterpreter {

    private final MTContext context;

    private final MTScope globalScope;

    public MTInterpreter(MTContext context) {
        this.context = context;
        this.globalScope = context.newScope();
    }

    public MTObject evaluate(String source) {

        MiniTalkLexer lexer =
            new MiniTalkLexer(CharStreams.fromString(source));

        CommonTokenStream tokens =
            new CommonTokenStream(lexer);

        MiniTalkParser parser = new MiniTalkParser(tokens);

        // Les erreurs de syntaxe deviennent des exceptions
        parser.removeErrorListeners();
        parser.setErrorHandler(
            new org.antlr.v4.runtime.BailErrorStrategy());

        ParseTree tree = parser.program();

        MTNode program =
            new AstBuilderVisitor().visit(tree);

        try {
            return program.eval(context, globalScope);
        }
        catch (MTException e) {
            throw e;
        }
        catch (RuntimeException e) {
            throw new MTException(
                "Erreur d'execution : " + e.getMessage(), e);
        }
    }
}
