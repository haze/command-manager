package pw.rudy.example;

import pw.rudy.command.Command;
import pw.rudy.command.utility.DigitClamp;
import pw.rudy.command.utility.LengthClamp;

import java.util.Optional;

/**
 * @author Haze
 * @since 9/24/2015
 * A test module used for registering commands.
 */
public class SpeedModule {

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

}
