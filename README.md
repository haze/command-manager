# command-manager
I used this for my minecraft cheats. Good for anything really... Java8+

## SpeedModule.java
'''java
private boolean nocheatMode;
    private float speed;

    /**
     * A example command featuring optionals.
     *
     * @param state the optional state to set a command to.
     */
    @Command("nocheat")
    public void switchNocheat(Optional<Boolean> state) {
        nocheatMode = (state.isPresent() ? state.get() : !nocheatMode);
        System.out.println(String.format("Set nocheat mode %s.", state.isPresent() ? "on" : "off"));
    }

    /**
     * A test Command
     * @param newSpeed the speed clamped by 0 and 10.
     * @return the text to be displayed by the
     */
    @Command({"speed", "ss"})
    public String setSpeed(@DigitClamp(min = 0, max = 10) Optional<Float> newSpeed) {
        speed = newSpeed.isPresent() ? newSpeed.get() : 1F;
        return String.format("Set the Speed to %s.", speed);
    }

    /**
     * a test command.
     * @param str1 a string.
     * @param str2 a string.
     * @param str3 a string/
     */
    @Command("test")
    public void testing(@LengthClamp(15) String str1, String str2, Optional<String> str3) {
        System.out.println(String.format("1 = %s, 2 = %s, 3 = %s", str1, str2, (str3.isPresent() ? str3 : "str3 not present.")));
    }
'''

## Main.java
```java
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
```
