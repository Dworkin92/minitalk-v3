package mt.runtime;

import mt.runtime.exceptions.MTException;

/**
 * Caractere MiniTalk : un codepoint Unicode. Fabrique :
 * MTContext.newCharacter(int).
 *
 * Affichage : le caractere seul ("h" pour $h) --
 * pas de marque $ a la Smalltalk, choix minimal
 * deliberé (29/10), a revisiter si besoin.
 */
public final class MTCharacter
        implements MTObject {

    private final int codepoint;

    private final MTClass mtClass;

    MTCharacter(MTClass mtClass, int codepoint) {
        this.mtClass = mtClass;
        this.codepoint = codepoint;
    }

    public int javaValue() {
        return codepoint;
    }

    @Override
    public MTClass getMTClass() {
        return mtClass;
    }

    @Override
    public MTObject send(
            MTSymbol selector,
            MTObject... arguments) {

        MTMethod method = mtClass.lookupInstanceMethod(selector);

        if (method == null) {
            throw new MTException(
                "Message non compris : Character >> " + selector);
        }

        return method.invoke(this, arguments);
    }

    @Override
    public MTString asString() {
        return mtClass.getContext().newString(
            String.valueOf(Character.toChars(codepoint)));
    }

    @Override
    public String toString() {
        return String.valueOf(Character.toChars(codepoint));
    }

    @Override
    public boolean isNil() {
        return false;
    }
}
