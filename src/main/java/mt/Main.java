package mt;

import mt.bootstrap.MTBootstrap;
import mt.runtime.MTDummyMethod;
import mt.runtime.MTSymbol;

public final class Main {

    public static void main(String[] args) {

        MTBootstrap bootstrap
                = new MTBootstrap();

        bootstrap.initialize();

        System.out.println(
                "MiniTalk bootstrap OK");

        MTSymbol s1 = MTSymbol.intern("name");

        MTSymbol s2 = MTSymbol.intern("name");

        MTSymbol selector = MTSymbol.intern("test");

        System.out.println("s1 == s2 : " + (s1 == s2));


    }
}
