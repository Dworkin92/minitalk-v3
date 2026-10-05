package mt.runtime;

import java.util.HashMap;
import java.util.Map;

public final class MTSymbol {

    private static final Map<String, MTSymbol>
            INTERN_POOL = new HashMap<>();

    private final String value;

    private MTSymbol(String value) {
        this.value = value;
    }

    public static synchronized MTSymbol intern(
            String value) {

        return INTERN_POOL.computeIfAbsent(
                value,
                MTSymbol::new);
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj;
    }

}
