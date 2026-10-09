package mt.runtime.primitives;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marque une methode statique comme primitive MiniTalk.
 *
 * La valeur est le selecteur du message.
 * Repeatable : permet les synonymes (@Primitive("==") @Primitive("isSameAs:")).
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Repeatable(Primitive.Selectors.class)
public @interface Primitive {

    String value();

    boolean classSide() default false;

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface Selectors {
        Primitive[] value();
    }
}
