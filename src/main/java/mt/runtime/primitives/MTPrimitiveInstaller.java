package mt.runtime.primitives;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import mt.runtime.MTClass;
import mt.runtime.MTMethod;
import mt.runtime.MTObject;
import mt.runtime.MTSymbol;
import mt.runtime.exceptions.MTException;

/**
 * Enregistre sur une classe MiniTalk toutes les methodes
 * statiques annotees @Primitive d'une classe de primitives Java.
 *
 * Le cout de la reflexion est paye une seule fois, au bootstrap.
 * Ensuite les primitives sont des MTMethod ordinaires.
 */
public final class MTPrimitiveInstaller {

    private MTPrimitiveInstaller() {
    }

    public static void install(MTClass target, Class<?> primitivesClass) {

        for (Method javaMethod
                : primitivesClass.getDeclaredMethods()) {

            Primitive[] primitives =
                    javaMethod.getAnnotationsByType(Primitive.class);

            if (primitives.length == 0) {
                continue;
            }

            checkSignature(primitivesClass, javaMethod);

            for (Primitive primitive : primitives) {

                MTSymbol selector =
                        MTSymbol.intern(primitive.value());

                // (10/11) Enveloppe PAR SELECTEUR : l'echec
                // nommera le message MiniTalk, pas le nom
                // Java de la methode.
                MTMethod nativeMethod =
                        wrap(javaMethod, primitive.value());

                if (primitive.classSide()) {
                    target.addClassMethod(selector, nativeMethod);
                }
                else {
                    target.addInstanceMethod(selector, nativeMethod);
                }
            }
        }
    }

    /**
     * Signature attendue, verifiee au bootstrap :
     *   public static MTObject nom(MTObject receiver, MTObject... arguments)
     */
    private static void checkSignature(
            Class<?> primitivesClass,
            Method javaMethod) {

        String where =
                primitivesClass.getSimpleName()
                + "." + javaMethod.getName();

        if (!Modifier.isStatic(javaMethod.getModifiers())) {
            throw new MTException(
                "Primitive non statique : " + where);
        }

        Class<?>[] parameters = javaMethod.getParameterTypes();

        boolean twoParameters =
                parameters.length == 2
                && parameters[0] == MTObject.class
                && parameters[1] == MTObject[].class;

        if (!twoParameters) {
            throw new MTException(
                "Signature invalide : " + where
                + " — attendu (MTObject, MTObject...)");
        }

        if (javaMethod.getReturnType() != MTObject.class) {
            throw new MTException(
                "Type de retour invalide : " + where
                + " — attendu MTObject");
        }
    }

    /**
     * Emballe la methode Java reflechie en MTMethod.
     * Le boxing des varargs est gere par invoke().
     */
    private static MTMethod wrap(
            Method javaMethod,
            String selector) {

        javaMethod.setAccessible(true);

        return (receiver, arguments) -> {
            try {
                return (MTObject)
                        javaMethod.invoke(null, receiver, arguments);
            }
            catch (ReflectiveOperationException cause) {

                // (02/11) Une MTException qui vient de la
                // primitive est deja une erreur du langage,
                // propre et situee : elle passe telle quelle.
                // Seules les erreurs Java qui ne sont pas
                // des erreurs MiniTalk sont reemballees.
                if (cause.getCause()
                        instanceof MTException mtException) {
                    throw mtException;
                }

                // (10/11) Le message parle le LANGAGE :
                // le selecteur MiniTalk d'abord, le nom
                // Java en second, entre parentheses,
                // pour la chasse.
                throw new MTException(
                    "Echec primitive : "
                    + selector
                    + " (" + javaMethod.getName() + ")",
                    cause);
            }
        };
    }
}
