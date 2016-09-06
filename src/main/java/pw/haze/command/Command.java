package pw.haze.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Haze
 * @version 2.3BETA
 * @since 9/24/2015
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Command {
    /**
     * The annotation that describes what the command catalyst will be.
     *
     * @return The catalyst
     */
    String[] value() default "command";
}
