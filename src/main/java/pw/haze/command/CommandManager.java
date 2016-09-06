package pw.haze.command;


import pw.haze.command.utility.DigitClamp;
import pw.haze.command.utility.LengthClamp;
import pw.haze.management.MapManager;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Haze
 * @version 2.3BETA
 * @since 9/24/2015
 */
public class CommandManager extends MapManager<Method, Object> {


    /**
     * How the manager detects commands. Can be changed
     */
    private String catalyst;

    /**
     * Sets the default catalyst
     */
    public CommandManager() {
        this("-");
    }

    /**
     * Sets the catalyst to a user-defined one
     * @param catalyst the user-defined catalyst
     */
    public CommandManager(String catalyst) {
        this.catalyst = catalyst;
        this.contents = new HashMap<>();
    }


    /**
     * Gets a list of command annotations
     *
     * @return the list of command annotations
     */
    private List<Command> getCommands() {
        List<Command> commands = new ArrayList<>();
        this.contents.keySet().forEach(cmd -> commands.add(cmd.getAnnotation(Command.class)));
        return commands;
    }

    /**
     * Use this to get a method from a command.
     *
     * @param command the command
     * @return the commands method
     */
    public Method getMethodFromCommand(Command command) {
        for (Method m : this.contents.keySet())
            if (Arrays.equals(m.getAnnotation(Command.class).value(), command.value()))
                return m;
        return null;
    }


    /**
     * Call this with a message to execute a command
     *
     * @param message the message
     */
    public String execute(String message) {
        if (message.startsWith(catalyst)) {
            String[] arguments = getArguments(message);
            String cmd = arguments[0].substring(catalyst.length());
            for (Map.Entry<Method, Object> entry : this.contents.entrySet()) {
                Command annotation = entry.getKey().getAnnotation(Command.class);
                if (isValidAnnotation(annotation, cmd)) {
                    List<String> args = Arrays.stream(arguments).skip(1).collect(Collectors.toList());
                    return invokeCommand(entry.getKey(), entry.getValue(), args.toArray(new String[args.size()]));
                }
            }
            if (!Objects.equals(cmd, "")) {
                return String.format("Command not found, %s.\n", cmd);
            }
        }
        return "";
    }
    /**
     * Checks if the annotation is valid
     *
     * @param c         the command annotation
     * @param qualifier the string to qualify the command by
     * @return whether or not the annotation is valid
     */
    private boolean isValidAnnotation(Command c, String qualifier) {
        for (String str : c.value())
            if (qualifier.equalsIgnoreCase(str)) return true;
        return false;
    }

    /**
     * Uses a simple regex string to format arguments between spaces, and allows spaces in arguments with the use of quotes.
     *
     * @param message the message
     * @return a array of arguments
     */
    private String[] getArguments(String message) {
        List<String> arguments = new ArrayList<>();
        Matcher matcher = Pattern.compile("[^\\s\"']+|\"([^\"]*)\"|'([^']*)'").matcher(message);
        switch (matcher.groupCount()) {
            case 1: {
                arguments.add(toString().substring(this.catalyst.length()));
                break;
            }
            default: {
                while (matcher.find()) {
                    arguments.add(matcher.group().replaceAll("\"", ""));
                }
            }
        }
        return arguments.toArray(new String[arguments.size()]);
    }

    /**
     * Used to get the usage of a command from the method itself.
     *
     * @param method the command method
     * @return the usage
     */
    @SuppressWarnings("unchecked")
    public String getUsage(Method method) {
        StringJoiner joiner = new StringJoiner(" ");
        try {
            for (Class<?> type : method.getParameterTypes()) {
                if (Optional.class.isAssignableFrom(type)) {
                    Class<Optional<?>> optionalClass = (Class<Optional<?>>) type;
                    joiner.add(String.format("Optional<%s>", optionalClass.getTypeName().getClass().getSimpleName()));
                } else {
                    joiner.add(String.format("(%s)", type.getSimpleName()));
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return joiner.toString();
    }

    /**
     * Used to parse argument types to their respective classes / object instances
     *
     * @param method           the command method
     * @param arguments        the arguments the user provided
     * @param excludeOptionals whether or not to use optionals
     * @return the fixed argument list.
     */
    private List<Object> getActualArguments(Method method, String[] arguments, Boolean excludeOptionals) {
        List<Object> actualArguments = new ArrayList<>();
        for (int i = 0; i < (excludeOptionals ? sizeOfParamsExcludingOptionals(method) : arguments.length); i++) {
            if (arguments[i] != null) {
                Class<?> type = method.getParameterTypes()[i];
                Parameter param = method.getParameters()[i];
                if (isBoolean(arguments[i]) && (Boolean.class.isAssignableFrom(type) || boolean.class.isAssignableFrom(type))) {
                    actualArguments.add(Boolean.parseBoolean(arguments[i]));
                } else if (isFloat(arguments[i]) && (Float.class.isAssignableFrom(type) || float.class.isAssignableFrom(type))) {
                    Float float0 = Float.parseFloat(arguments[i]);
                    if (param.isAnnotationPresent(DigitClamp.class)) {
                        DigitClamp clamp = param.getAnnotation(DigitClamp.class);
                        float0 = digitClamp(clamp, float0);
                    }
                    actualArguments.add(float0);
                } else if (isDigit(arguments[i]) && (Integer.class.isAssignableFrom(type) || int.class.isAssignableFrom(type))) {
                    Integer int0 = Integer.parseInt(arguments[i]);
                    if (param.isAnnotationPresent(DigitClamp.class)) {
                        DigitClamp clamp = param.getAnnotation(DigitClamp.class);
                        int0 = digitClamp(clamp, int0);
                    }
                    actualArguments.add(int0);
                } else if (String.class.isAssignableFrom(type)) {
                    String str = arguments[i];
                    if (param.isAnnotationPresent(LengthClamp.class)) {
                        LengthClamp clamp = param.getAnnotation(LengthClamp.class);
                        if (str.length() > clamp.value()) {
                            StringBuilder builder = new StringBuilder(str);
                            builder.replace(clamp.value(), str.length(), "");
                            str = builder.toString();
                        }
                    }
                    actualArguments.add(str);
                } else if (Optional.class.isAssignableFrom(type)) {
                    String str = arguments[i];
                    if (isBoolean(arguments[i])) {
                        actualArguments.add(Optional.of(Boolean.parseBoolean(str)));
                    } else if (isFloat(arguments[i])) {
                        Float digit = Float.parseFloat(arguments[i]);
                        if (param.isAnnotationPresent(DigitClamp.class)) {
                            digit = digitClamp(param.getAnnotation(DigitClamp.class), str);
                        }
                        actualArguments.add(Optional.of(digit));
                    } else if (isDigit(arguments[i])) {
                        Integer digit = Integer.parseInt(arguments[i]);
                        if (param.isAnnotationPresent(DigitClamp.class)) {
                            digit = Math.round(digitClamp(param.getAnnotation(DigitClamp.class), str));
                        }
                        actualArguments.add(Optional.of(digit));
                    } else {
                        if (param.isAnnotationPresent(LengthClamp.class)) {
                            str = strClamp(param.getAnnotation(LengthClamp.class), str);
                        }
                        actualArguments.add(Optional.of(str));
                    }
                }
            }
        }
        return actualArguments;
    }


    /**
     * Clamps the parsed string to the clamps specifications
     *
     * @param clamp the clamp annotation
     * @param str   the string to parse
     * @return the finalized (clamped) digit
     */
    public Float digitClamp(DigitClamp clamp, String str) {
        return digitClamp(clamp, Float.parseFloat(str));
    }

    private int digitClamp(DigitClamp clamp, int int0) {
        DecimalFormat df = new DecimalFormat("##");
        df.setRoundingMode(RoundingMode.DOWN);
        int min = Integer.parseInt(df.format(clamp.min()));
        int max = Integer.parseInt(df.format(clamp.max()));
        if (int0 > max)
            int0 = max;
        if (int0 < min)
            int0 = min;
        return int0;
    }

    private Float digitClamp(DigitClamp clamp, Float float0) {
        float min = clamp.min();
        float max = clamp.max();
        if (float0 > max)
            float0 = max;
        if (float0 < min)
            float0 = min;
        return float0;
    }

    /**
     * Clamps the string length
     *
     * @param clamp the clamp annotation
     * @param str   the string to parse
     * @return the finalized (clamped) string
     */
    public String strClamp(LengthClamp clamp, String str) {
        if (str.length() > clamp.value()) {
            StringBuilder builder = new StringBuilder(str);
            builder.replace(clamp.value(), str.length(), "");
            return builder.toString();
        }
        return str;
    }


    /**
     * Gets the generic super-type of a optional
     *
     * @param optional the optional
     * @return the generic super-type
     */
    private Class<?> getOptionalType(Optional<?> optional) {
        if (optional.isPresent())
            return optional.get().getClass();
        return null;
    }

    /**
     * Invokes a command.
     *
     * @param method    the method to invoke
     * @param instance  the class instance
     * @param arguments the arguments the user provided
     */
    private String invokeCommand(Method method, Object instance, String[] arguments) {
        String usage = getUsage(method);
        try {
            int sizeOfParamsExcludingOptionals = sizeOfParamsExcludingOptionals(method);
            List<Object> actualArguments = getActualArguments(method, arguments, method.getParameterCount() != arguments.length && (sizeOfParamsExcludingOptionals == arguments.length));
            if (method.getParameterCount() == arguments.length) {
                if (String.class.isAssignableFrom(method.getReturnType())) {
                    return method.invoke(instance, actualArguments.toArray()).toString();
                } else {
                    method.invoke(instance, actualArguments.toArray());
                }
            } else if (sizeOfParamsExcludingOptionals == arguments.length) {
                actualArguments = getActualArguments(method, arguments, true);
                if (String.class.isAssignableFrom(method.getReturnType())) {
                    return method.invoke(instance, fixArgumentsForOptionals(method, actualArguments).toArray()).toString();
                } else {
                    method.invoke(instance, fixArgumentsForOptionals(method, actualArguments).toArray());
                }
            } else {
                if (doParametersContainOptional(method)) {
                    return String.format("Expected %s or %s arguments(s), you turned in %s. Usage \"%s\"", sizeOfParamsExcludingOptionals(method), method.getParameterCount(), arguments.length, usage);
                } else {
                    return String.format("Expected %s argument(s), you turned in %s. Usage \"%s\"", method.getParameterCount(), arguments.length, usage);
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
            return String.format("Unexpected type(s), Expected usage: %s args = \"%s\"", usage, Arrays.toString(arguments));
        }
        return null;
    }

    /**
     * Returns the fixed argument list.
     *
     * @param method the method
     * @param given  the "actual" arguments
     * @return the arguments without optionals attached
     */
    private List<Object> fixArgumentsForOptionals(Method method, List<Object> given) {
        List<Object> newArguments = new ArrayList<>();
        Class<?>[] parameterTypes = method.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> type = parameterTypes[i];
            if (given.size() <= method.getParameterCount() && Optional.class.isAssignableFrom(type)) {
                newArguments.add(Optional.empty());
            } else if (given.size() <= method.getParameterCount()) {
                newArguments.add(given.get(i));
            }
        }
        return newArguments;
    }

    /**
     * returns the size of parameters in the method excluding the optionals
     *
     * @param method the method
     * @return the size of parameters in the method excluding the optionals
     */
    private int sizeOfParamsExcludingOptionals(Method method) {
        return Arrays.stream(method.getParameters()).filter(param -> !Optional.class.isAssignableFrom(param.getType())).collect(Collectors.toList()).size();
    }

    /**
     * Checks whether or not the parameters contains optionals
     *
     * @param method the method.
     * @return whether or not the parameters contains optionals
     */
    private boolean doParametersContainOptional(Method method) {
        return Arrays.stream(method.getParameters()).filter(param -> Optional.class.isAssignableFrom(param.getType())).findAny().isPresent();
    }

    /**
     * Determines whether or not a string is a proper boolean
     *
     * @param str the string
     * @return whether or not the string is a proper boolean
     */
    public boolean isBoolean(String str) {
        return str.equalsIgnoreCase("true") || str.equalsIgnoreCase("false");
    }

    /**
     * Determines whether or not a string is a proper digit
     *
     * @param str the string
     * @return whether or not the string is a proper digit
     */
    public boolean isDigit(String str) {
        boolean pass = true;
        for (char c : str.toCharArray()) {
            pass = Character.isDigit(c);
        }
        return pass;
    }

    /**
     * Determines whether or not a string is a proper float
     *
     * @param str the string
     * @return whether or not the string is a proper float
     */
    public boolean isFloat(String str) {
        boolean pass = true;
        for (char c : str.toCharArray()) {
            if (c != '.')
                pass = Character.isDigit(c);
        }
        return pass;
    }

    /**
     * Registers a class instance without debugging.
     *
     * @param o the class instance
     */
    public void register(Object o) {
        register(o, false);
    }

    /**
     * Registers a class instance with optional debug
     *
     * @param o     the class instance
     * @param debug whether or not to print debug messages to console.
     */
    public void register(Object o, Boolean debug) {
        if (debug)
            System.out.printf("Registering class %s.\n", o.getClass().getSimpleName());
        for (Method m : o.getClass().getDeclaredMethods()) {
            if (m.isAnnotationPresent(Command.class)) {
                if (!m.isAccessible()) {
                    m.setAccessible(true);
                }
                if (debug)
                    System.out.printf("Found command method %s, object instance = %s.\n", m.getName(), o);
                this.contents.put(m, o);
            }
        }
    }

}
