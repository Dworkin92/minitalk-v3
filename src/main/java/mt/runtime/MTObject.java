package mt.runtime;

import mt.runtime.exceptions.MTException;

/**
 * Interface racine de tous les objets MiniTalk.
 */
public interface MTObject {

    /**
     * Retourne la classe MiniTalk de l'objet.
     */
    MTClass getMTClass();

    /**
     * Envoie un message a l'objet.
     */
    MTObject send(
            MTSymbol selector,
            MTObject... arguments //A CORRIGER
    ) throws MTException;

    /**
     * Comparaison d'identite.
     * Correspond au message == dans MiniTalk.
     */
    default boolean identityEquals(final MTObject other) {
        return this == other;
    }

    /**
     * Retourne une representation textuelle.
     */
    MTString asString() throws MTException;

    /**
     * true uniquement pour Nil.
     */
    default boolean isNil() {
        return false;
    }

    /**
     * true pour les objets consideres vrais.
     */
    default boolean isTrue() {
        return !isFalse();
    }

    /**
     * true uniquement pour False.
     */
    default boolean isFalse() {
        return false;
    }
}
