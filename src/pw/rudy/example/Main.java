package pw.rudy.example;

import pw.rudy.command.CommandManager;

/**
 * @author Haze
 * @since 9/24/2015
 * A test class.
 */
public class Main {
    public static void main(String... args) {
        // make a new command manager
        CommandManager manager = new CommandManager();
        // setup the list
        manager.setup();
        // register the class
        manager.register(new SpeedModule());
        // execute a command
        // this works because floats can be both digits and floats.
        // 52 = 52.0
        manager.execute("-ss -101");
        // execute another command
        // i can also use quotes here to give an argument spacing
        // notice how there isn't a third argument, this works because in the method
        // the third argument is a optional, hence why it isn't really needed
        manager.execute("-test this_must_be_over_15_characters_by_now \"test with spaces\"");
        // this is also why both
        manager.execute("-nocheat");
        // and
        manager.execute("-nocheat true");
        // work.
    }
}
