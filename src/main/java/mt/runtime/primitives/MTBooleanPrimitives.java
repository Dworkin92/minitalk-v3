package mt.runtime.primitives;

import mt.runtime.MTBoolean;
import mt.runtime.MTMethod;
import mt.runtime.MTObject;
import mt.runtime.MTNil;

public final class MTBooleanPrimitives {

    private MTBooleanPrimitives() {
    }

    @Primitive("ifTrue:")
    public static MTObject ifTrue(
            MTObject receiver,
            MTObject... arguments) {

        MTBoolean self = (MTBoolean) receiver;
        return self.javaValue()
                ? evaluate(arguments[0], receiver)
                : MTNil.instance();
    }

    @Primitive("ifFalse:")
    public static MTObject ifFalse(
            MTObject receiver,
            MTObject... arguments) {

        MTBoolean self = (MTBoolean) receiver;
        return self.javaValue()
                ? MTNil.instance()
                : evaluate(arguments[0], receiver);
    }

    @Primitive("ifTrue:ifFalse:")
    public static MTObject ifTrueIfFalse(
            MTObject receiver,
            MTObject... arguments) {

        MTBoolean self = (MTBoolean) receiver;
        return evaluate(
            self.javaValue() ? arguments[0] : arguments[1],
            receiver);
    }

    @Primitive("and:")
    public static MTObject and(
            MTObject receiver,
            MTObject... arguments) {

        MTBoolean self = (MTBoolean) receiver;
        MTBoolean other = (MTBoolean) arguments[0];
        return MTBoolean.of(
            self.javaValue() && other.javaValue());
    }

    @Primitive("or:")
    public static MTObject or(
            MTObject receiver,
            MTObject... arguments) {

        MTBoolean self = (MTBoolean) receiver;
        MTBoolean other = (MTBoolean) arguments[0];
        return MTBoolean.of(
            self.javaValue() || other.javaValue());
    }

    @Primitive("not")
    public static MTObject not(
            MTObject receiver,
            MTObject... arguments) {

        MTBoolean self = (MTBoolean) receiver;
        return MTBoolean.of(!self.javaValue());
    }

    //
    // Aides
    //

    /**
     * Evalue un argument de type condition :
     * un MTMethod (futur MTBlock) est invoque,
     * toute autre valeur est retournee telle quelle.
     */
    private static MTObject evaluate(
            MTObject value,
            MTObject receiver) {

        if (value instanceof MTMethod method) {
            return method.invoke(receiver);
        }

        return value;
    }
}
