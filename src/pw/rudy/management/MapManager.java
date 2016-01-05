package pw.rudy.management;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Haze
 * @since 9/24/2015
 */
public abstract class MapManager<K, V> {
    /**
     * The map contents, with generified
     */
    protected Map<K, V> contents;

    /**
     * A getter for the map
     *
     * @return The Map
     */
    public Map<K, V> getContents() {
        return contents;
    }

    /**
     * A setter for the map
     *
     * @param contents The Map
     */
    public void setContents(Map<K, V> contents) {
        this.contents = contents;
    }

    /**
     * A method that can either be
     * 1. Overridden and reset the default setup of the list
     * or
     * 2. Left alone and let it use the default hashmap.
     */
    public void setup() {
        this.contents = new HashMap<>();
    }
}
