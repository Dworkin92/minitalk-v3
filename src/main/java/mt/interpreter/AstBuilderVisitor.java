package mt.interpreter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import mt.ast.MTArrayNode;
import mt.ast.MTAssignmentNode;
import mt.ast.MTBlockNode;
import mt.ast.MTIdentifierNode;
import mt.ast.MTLiteralInteger;
import mt.ast.MTLiteralSpecial;
import mt.ast.MTLiteralString;
import mt.ast.MTMessageSendNode;
import mt.ast.MTNode;
import mt.ast.MTSequenceNode;
import mt.ast.MTSymbolNode;
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
        return ctx.additiveExpression().accept(this);
    }

    @Override
    public MTNode visitAdditiveExpression(
            MiniTalkParser.AdditiveExpressionContext ctx) {

        // Associatif a gauche : 3 + 4 + 5 = (3 + 4) + 5
        MTNode result = null;
        String operator = null;

        for (int i = 0; i < ctx.getChildCount(); i++) {
            var child = ctx.getChild(i);

            if (child instanceof
                    MiniTalkParser.MultiplicativeExpressionContext m) {

                MTNode operand = m.accept(this);

                result = (result == null)
                    ? operand
                    : new MTMessageSendNode(
                        result,
                        MTSymbol.intern(operator),
                        List.of(operand));
            } else {
                operator = child.getText();
            }
        }

        return result;
    }

    @Override
    public MTNode visitMultiplicativeExpression(
            MiniTalkParser.MultiplicativeExpressionContext ctx) {

        // Associatif a gauche : 3 * 4 * 5 = (3 * 4) * 5
        MTNode result = null;
        String operator = null;

        for (int i = 0; i < ctx.getChildCount(); i++) {
            var child = ctx.getChild(i);

            if (child instanceof
                    MiniTalkParser.PowerExpressionContext p) {

                MTNode operand = p.accept(this);

                result = (result == null)
                    ? operand
                    : new MTMessageSendNode(
                        result,
                        MTSymbol.intern(operator),
                        List.of(operand));
            } else {
                operator = child.getText();
            }
        }

        return result;
    }

    @Override
    public MTNode visitPowerExpression(
            MiniTalkParser.PowerExpressionContext ctx) {

        // Droite associatif : 2 ** 3 ** 2 = 2 ** (3 ** 2)
        MTNode base = ctx.signExpression().accept(this);

        if (ctx.POWER() == null) {
            return base;
        }

        return new MTMessageSendNode(
            base,
            MTSymbol.intern("**"),
            List.of(ctx.powerExpression().accept(this)));
    }

    @Override
    public MTNode visitSignExpression(
            MiniTalkParser.SignExpressionContext ctx) {

        MTNode result = ctx.unaryExpression().accept(this);

        // Un seul signe eventuel. Le moins unaire
        // envoie 'negated' (tout est message) ;
        // le plus unaire est l'identite.
        if (ctx.getChildCount() == 2
                && ctx.getChild(0).getText().equals("-")) {
            return new MTMessageSendNode(
                result,
                MTSymbol.intern("negated"),
                List.of());
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

        // '(' expression ')' : c'est l'expression
        // interieure qui compte, pas le token '('
        if (ctx.expression() != null) {
            return ctx.expression().accept(this);
        }

        // literal | arrayLiteral | symbolLiteral
        // | block | identifier
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
    // Tableaux et symboles
    //

    @Override
    public MTNode visitArrayLiteral(
            MiniTalkParser.ArrayLiteralContext ctx) {

        List<MTNode> elements = new ArrayList<>();

        for (var elementCtx : ctx.arrayElement()) {
            elements.add(elementCtx.accept(this));
        }

        return new MTArrayNode(elements);
    }

    @Override
    public MTNode visitArrayElement(
            MiniTalkParser.ArrayElementContext ctx) {

        // literal | identifier | symbolLiteral | arrayLiteral :
        // une seule alternative, un seul enfant.
        return ctx.getChild(0).accept(this);
    }

    @Override
    public MTNode visitSymbolLiteral(
            MiniTalkParser.SymbolLiteralContext ctx) {

        return new MTSymbolNode(
            ctx.IDENTIFIER().getText());
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
