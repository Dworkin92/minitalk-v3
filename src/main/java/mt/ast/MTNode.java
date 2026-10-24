package mt.ast;

import mt.runtime.MTContext;
import mt.runtime.MTObject;
import mt.runtime.MTScope;

/**
 * Un noeud d'AST. Chaque noeud sait s'evaluer dans un
 * contexte (le monde) et une portee (les variables visibles).
 */
public interface MTNode {

    MTObject eval(MTContext context, MTScope scope);
}
