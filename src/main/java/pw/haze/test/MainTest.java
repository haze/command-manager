package pw.haze.test;

import pw.haze.command.Command;
import pw.haze.command.CommandManager;

import java.util.Optional;

/**
 * @author: Haze
 * @since: 9/5/2016
 */
public class MainTest {

    private CommandManager manager;

    public MainTest() {
        manager = new CommandManager();
        manager.register(this);
        System.out.println("|--- start ---|");
        System.out.println(manager.execute("-a b"));
        System.out.println(manager.execute("-a b c"));
        System.out.println(manager.execute("-b b"));
        System.out.println("|--- done ---|");
    }


    public static void main(String... args) {
        new MainTest();
    }

    @Command("a")
    public String test(String a, Optional<String> b) {
        return String.format("a=%s, b=%s", a, b.isPresent() ? b.get() : "<empty>");
    }

    @Command("b")
    public String testTwo(String a) {
        return String.format("a2=%s", a);
    }



}
