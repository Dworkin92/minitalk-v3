package mt.interpreter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import mt.ast.MTAssignmentNode;
import mt.ast.MTBlockNode;
import mt.ast.MTIdentifierNode;
import mt.ast.MTLiteralInteger;
import mt.ast.MTLiteralSpecial;
import mt.ast.MTLiteralString;
import mt.ast.MTMessageSendNode;
import mt.ast.MTNode;
import mt.ast.MTSequenceNode;
import mt.generated.MiniTalkBaseVisitor;
import mt.generated.MiniTalkParser;
import mt.runtime.MTSymbol;

/**
 * Traduit l'arbre ANTLR en noeuds d'AST mt.ast.
 * Le resultat de chaque visit() est un MTNode.
 */
public final class AstBuilderVisitor
        extends MiniTalkBaseVisitor<MTNode> {

    @Override
    public MTNode visitProgram(MiniTalkParser.ProgramContext ctx) {

        List<MTNode> statements = new ArrayList<>();

        for (var statementCtx : ctx.statement()) {
            statements.add(statementCtx.accept(this));
        }

        return new MTSequenceNode(statements);
    }

    @Override
    public MTNode visitStatement(MiniTalkParser.StatementContext ctx) {

        // var := expression
        if (ctx.IDENTIFIER() != null) {
            return new MTAssignmentNode(
                ctx.IDENTIFIER().getText(),
                ctx.expression().accept(this));
        }

        return ctx.expression().accept(this);
    }

    @Override
    public MTNode visitStatements(MiniTalkParser.StatementsContext ctx) {

        List<MTNode> statements = new ArrayList<>();

        for (var statementCtx : ctx.statement()) {
            statements.add(statementCtx.accept(this));
        }

        return new MTSequenceNode(statements);
    }

    //
    // Expression : les trois formes de message
    //

    @Override
    public MTNode visitKeywordExpression(
            MiniTalkParser.KeywordExpressionContext ctx) {

        // Recepteur : la premiere sous-expression binaire
        MTNode result = ctx.binaryExpression(0).accept(this);

        var identifiers = ctx.IDENTIFIER();

        // Pas de mot-cle : l'expression binaire se suffit
        if (identifiers.isEmpty()) {
            return result;
        }

        // Puis les paires motCle : argument
        List<String> parts = new ArrayList<>();
        List<MTNode> arguments = new ArrayList<>();

        for (int i = 0; i < identifiers.size(); i++) {
            parts.add(identifiers.get(i).getText() + ":");
            arguments.add(
                ctx.binaryExpression(i + 1).accept(this));
        }

        return new MTMessageSendNode(
            result,
            MTSymbol.intern(
                parts.stream().collect(Collectors.joining())),
            arguments);
    }

    @Override
    public MTNode visitBinaryExpression(
            MiniTalkParser.BinaryExpressionContext ctx) {

        // Associatif a gauche : 3 + 4 + 5 = (3 + 4) + 5
        MTNode result = ctx.unaryExpression(0).accept(this);

        for (int i = 0; i < ctx.BINARY().size(); i++) {
            result = new MTMessageSendNode(
                result,
                MTSymbol.intern(ctx.BINARY(i).getText()),
                List.of(
                    ctx.unaryExpression(i + 1).accept(this)));
        }

        return result;
    }

    @Override
    public MTNode visitUnaryExpression(
            MiniTalkParser.UnaryExpressionContext ctx) {

        MTNode result = ctx.primary().accept(this);

        for (var selector : ctx.IDENTIFIER()) {
            result = new MTMessageSendNode(
                result,
                MTSymbol.intern(selector.getText()),
                List.of());
        }

        return result;
    }

    @Override
    public MTNode visitPrimary(MiniTalkParser.PrimaryContext ctx) {
        // literal | block | identifier | ( expression )
        return ctx.getChild(0).accept(this);
    }

    @Override
    public MTNode visitIdentifier(MiniTalkParser.IdentifierContext ctx) {
        return new MTIdentifierNode(ctx.getText());
    }

    //
    // Blocs
    //

    @Override
    public MTNode visitBlock(MiniTalkParser.BlockContext ctx) {

        List<String> parameters = new ArrayList<>();

        if (ctx.blockParameters() != null) {
            for (var name : ctx.blockParameters().IDENTIFIER()) {
                parameters.add(name.getText());
            }
        }

        List<String> locals = new ArrayList<>();

        if (ctx.blockLocals() != null) {
            for (var name : ctx.blockLocals().IDENTIFIER()) {
                locals.add(name.getText());
            }
        }

        MTSequenceNode body = ctx.statements() != null
            ? (MTSequenceNode) ctx.statements().accept(this)
            : new MTSequenceNode(List.of());

        return new MTBlockNode(parameters, locals, body);
    }

    //
    // Litteraux
    //

    @Override
    public MTNode visitLiteral(MiniTalkParser.LiteralContext ctx) {

        if (ctx.INTEGER() != null) {
            return new MTLiteralInteger(
                Integer.parseInt(ctx.getText()));
        }

        if (ctx.STRING() != null) {
            String raw = ctx.getText();

            // Retire les apostrophes externes, double '' -> '
            String value = raw
                .substring(1, raw.length() - 1)
                .replace("''", "'");

            return new MTLiteralString(value);
        }

        return new MTLiteralSpecial(ctx.getText());
    }
}
