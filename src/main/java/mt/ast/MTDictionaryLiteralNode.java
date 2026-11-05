package mt.ast;

import java.util.List;

import mt.runtime.MTContext;
import mt.runtime.MTDictionary;
import mt.runtime.MTObject;
import mt.runtime.MTScope;

/**
 * Litteral dictionnaire (05/11) : { 1 => 'a'. x => 2 + 3 }.
 * Divergence assumee avec #(...) : les paires sont des
 * expressions completes (decision du 05/11, facon Pharo
 * pour le contenu). Le dictionnaire est reconstruit a
 * chaque evaluation, comme #(...).
 */
public record MTDictionaryLiteralNode(
        List<Entry> entries) implements MTNode {

    public record Entry(MTNode key, MTNode value) {
    }

    @Override
    public MTObject eval(MTContext context, MTScope scope) {

        MTDictionary dictionary = context.newDictionary();

        for (Entry entry : entries) {
            dictionary.put(
                entry.key().eval(context, scope),
                entry.value().eval(context, scope));
        }

        return dictionary;
    }
}
