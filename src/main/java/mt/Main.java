package mt;

import mt.bootstrap.MTBootstrap;

public final class Main {

    public static void main(String[] args) {

        MTBootstrap bootstrap
                = new MTBootstrap();

        bootstrap.initialize();

        System.out.println(
                "MiniTalk bootstrap OK");
    }
}
