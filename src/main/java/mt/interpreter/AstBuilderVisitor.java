package mt.interpreter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import mt.ast.MTArrayNode;
import mt.ast.MTAssignmentNode;
import mt.ast.MTBlockNode;
import mt.ast.MTDictionaryLiteralNode;
import mt.ast.MTLocalsNode;
import mt.ast.MTIdentifierNode;
import mt.ast.MTLiteralInteger;
import mt.ast.MTLiteralFloat;
import mt.ast.MTLiteralSpecial;
import mt.ast.MTLiteralString;
import mt.ast.MTLiteralCharacter;
import mt.ast.MTMessageSendNode;
import mt.ast.MTNode;
import mt.ast.MTSequenceNode;
import mt.ast.MTSymbolNode;
import mt.ast.MTNativeSendNode;
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

        MTSequenceNode body =
            new MTSequenceNode(statements);

        // Temporaires de programme : | a b | en tete,
        // comme un corps de methode Smalltalk. Elles
        // vivent dans la portee racine.
        if (ctx.blockLocals() != null) {
            List<String> names = new ArrayList<>();

            for (var name : ctx.blockLocals().IDENTIFIER()) {
                names.add(name.getText());
            }

            return new MTLocalsNode(names, body);
        }

        return body;
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
        boolean fenetre = false;

        for (int i = 0; i < ctx.getChildCount(); i++) {
            var child = ctx.getChild(i);

            if (child instanceof
                    MiniTalkParser.MultiplicativeExpressionContext m) {

                MTNode operand = m.accept(this);

                if (result == null) {
                    result = operand;
                }
                else if (fenetre) {
                    result = opBinaire(
                        result, operand, operator);
                }
                else {
                    result = new MTMessageSendNode(
                        result,
                        MTSymbol.intern(operator),
                        List.of(operand));
                }
            } else {
                String texte = child.getText();

                fenetre = texte.contains("@");
                operator = texte;
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
        boolean fenetre = false;

        for (int i = 0; i < ctx.getChildCount(); i++) {
            var child = ctx.getChild(i);

            if (child instanceof
                    MiniTalkParser.PowerExpressionContext p) {

                MTNode operand = p.accept(this);

                if (result == null) {
                    result = operand;
                }
                else if (fenetre) {
                    result = opBinaire(
                        result, operand, operator);
                }
                else {
                    result = new MTMessageSendNode(
                        result,
                        MTSymbol.intern(operator),
                        List.of(operand));
                }
                
            } else {
                String texte = child.getText();

                fenetre = texte.contains("@");
                operator = texte;
            }
        }

        return result;
    }

    @Override
    public MTNode visitPowerExpression(
            MiniTalkParser.PowerExpressionContext ctx) {

        MTNode base = ctx.signExpression().accept(this);

        if (ctx.POWER() == null && ctx.QUALIFIED_POWER() == null) {
            return base;
        }

        MTNode exposant =
            ctx.powerExpression().accept(this);

        if (ctx.QUALIFIED_POWER() != null) {
            return opBinaire(base, exposant,
                ctx.QUALIFIED_POWER().getText());
        }

        return new MTMessageSendNode(
            base, MTSymbol.intern("**"), List.of(exposant));
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

        for (int i = 1; i < ctx.getChildCount(); i++) {
            var child = ctx.getChild(i);

            if (child instanceof
                    MiniTalkParser.QualifiedSelectorContext q) {

                result = qualifiedSend(result, q);
            }
            else {
                // IDENTIFIER nu : message unaire ordinaire
                result = new MTMessageSendNode(
                    result,
                    MTSymbol.intern(child.getText()),
                    List.of());
            }
        }

        return result;
    }

    /**
     * p Integer@negated : la primitive NATIVE negated
     * de Integer, appliquee au receveur. Le nom de la
     * classe est le premier IDENTIFIER, l'arobase le
     * second enfant ; le selecteur est assemble apres
     * (meme geste que le symbolLiteral).
     */
    /**
     * (21/11) Les trois formes de la fenetre, reconnues
     * a la count d'enfants (le collage des selecteurs
     * mot-cle reprend le geste du symbolLiteral) :
     *   3 enfants  = unaire
     *   4 enfants   = binaire
     *   5 et plus   = mot-cle, triplets IDENT : arg
     */
    private MTNode qualifiedSend(
            MTNode receiver,
            MiniTalkParser.QualifiedSelectorContext ctx) {

        String classe = ctx.getChild(0).getText();

        // unaire : Classe@selecteur
        if (ctx.getChildCount() == 3) {
            return new MTNativeSendNode(
                receiver, classe,
                ctx.getChild(2).getText(),
                List.of());
        }

        // binaire : Classe@op operande
        /*
        if (ctx.getChildCount() == 4) {
            return new MTNativeSendNode(
                receiver, classe,
                ctx.getChild(2).getText(),
                List.of(ctx.getChild(3).accept(this)));
        }
        */

        // mot-cle : Classe@partie: arg partie: arg ...
        StringBuilder sel = new StringBuilder();
        List<MTNode> arguments = new ArrayList<>();

        for (int i = 2; i < ctx.getChildCount(); i += 3) {
            sel.append(ctx.getChild(i).getText())
               .append(':');

            arguments.add(
                ctx.getChild(i + 2).accept(this));
        }

        return new MTNativeSendNode(
            receiver, classe, sel.toString(), arguments);
    }

    /**
     * (21/11) Un token QUALIFIED_* ("Integer@+") :
     * la fenetre, l'operande comme argument.
     */
    private MTNode opBinaire(
            MTNode receiver, MTNode operand, String texte) {

        int arobase = texte.indexOf('@');

        return new MTNativeSendNode(
            receiver,
            texte.substring(0, arobase),
            texte.substring(arobase + 1),
            List.of(operand));
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
    public MTNode visitDictionaryLiteral(
            MiniTalkParser.DictionaryLiteralContext ctx) {

        List<MTDictionaryLiteralNode.Entry> entries =
            new ArrayList<>();

        for (var pairCtx : ctx.dictionaryPair()) {
            entries.add(new MTDictionaryLiteralNode.Entry(
                pairCtx.expression(0).accept(this),
                pairCtx.expression(1).accept(this)));
        }

        return new MTDictionaryLiteralNode(entries);
    }
    
    @Override
    public MTNode visitSymbolLiteral(
            MiniTalkParser.SymbolLiteralContext ctx) {

        // #greet, #nom:, #at:put:, #+
        // (16/11) : une ou plusieurs pieces,
        // on assemble le texte apres le '#'.
        StringBuilder nom = new StringBuilder();

        for (int i = 1; i < ctx.getChildCount(); i++) {
            nom.append(ctx.getChild(i).getText());
        }

        return new MTSymbolNode(
            nom.toString());
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

        if (ctx.FLOAT() != null) {
            return new MTLiteralFloat(
                Double.parseDouble(ctx.getText()));
        }

        if (ctx.CHARACTER() != null) {
            return new MTLiteralCharacter(
                ctx.getText().codePointAt(1));
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
