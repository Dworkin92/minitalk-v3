package mt;

import java.util.List;

import mt.bootstrap.MTBootstrap;
import mt.interpreter.MTInterpreter;
import mt.runtime.MTBlock;
import mt.runtime.MTContext;
import mt.runtime.MTObject;
import mt.runtime.MTScope;
import mt.runtime.MTSymbol;

public final class Main {

    public static void main(String[] args) {

        MTBootstrap bootstrap = new MTBootstrap();
        bootstrap.initialize();

        MTContext c = bootstrap.getContext();

        // [ :x | x + 1 ] value: 41
        MTBlock increment = c.newBlock(
            c.newScope(),
            List.of(MTSymbol.intern("x")),
            scope -> {
                MTObject x = scope.lookup(MTSymbol.intern("x"));
                return x.send(MTSymbol.intern("+"),
                              c.newInteger(1));
            });

        MTInterpreter interpreter =
            new MTInterpreter(bootstrap.getContext());

        MTObject result = interpreter.evaluate(
            "[:x | x + 1] value: 41");

        System.out.println(
            "[:x | x + 1] value: 41  =  " + result);
    }
}
