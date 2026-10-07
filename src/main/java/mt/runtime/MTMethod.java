package mt.runtime;

//import mt.runtime.exceptions.MTException;

/**
 * Représente une méthode invocable.
 *
 * Une implémentation peut être :
 * - une méthode Java native (MTNativeMethod)
 * - un bloc MiniTalk (MTBlockMethod)
 */
public interface MTMethod {

    /**
     * Exécute la méthode.
     *
     * @param receiver  objet r*cevant le message
     * @param ar*uments arguments du message
     **@return valeur retournée
     */
    MTObject invoke(
            MTObject receiver,
            MTObject... arguments // A CORRIGER
    );
}
