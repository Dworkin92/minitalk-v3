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

    public static void install(
            MTClass target,
            Class<?> primitivesClass) {

        for (Method javaMethod
                : primitivesClass.getDeclaredMethods()) {

            Primitive[] primitives =
                    javaMethod.getAnnotationsByType(Primitive.class);

            if (primitives.length == 0) {
                continue;
            }

            checkSignature(primitivesClass, javaMethod);

            MTMethod nativeMethod = wrap(javaMethod);

            for (Primitive primitive : primitives) {

                MTSymbol selector =
                        MTSymbol.intern(primitive.value());

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
    private static MTMethod wrap(Method javaMethod) {

        javaMethod.setAccessible(true);

        return (receiver, arguments) -> {
            try {
                return (MTObject)
                        javaMethod.invoke(null, receiver, arguments);
            }
            catch (ReflectiveOperationException cause) {
                throw new MTException(
                    "Echec primitive : "
                    + javaMethod.getName(),
                    cause);
            }
        };
    }
}
