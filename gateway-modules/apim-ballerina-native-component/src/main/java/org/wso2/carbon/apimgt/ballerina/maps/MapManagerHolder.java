package org.wso2.carbon.apimgt.ballerina.maps;

import org.ballerinalang.model.values.BValue;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MapManager holder class to hold Maps
 */
public final class MapManagerHolder {
    private static volatile MapManagerHolder instance = null;


    /**
     * Get cache manager instance for cache access
     *
     * @return {@link Map} which holds cache manager object
     */
    public Map<String, BValue> getMapManager() {
        return mapManager;
    }

    private Map<String, BValue> mapManager;

    /**
     * Private cache manager holder constructor
     */
    private MapManagerHolder() {
        this.mapManager = new ConcurrentHashMap<>();
    }

    /**
     * Static method to get cache manager
     *
     * @return {@link MapManagerHolder} instance which holds cache manager reference.
     */
    public static MapManagerHolder getInstance() {
        if (instance == null) {
            synchronized (MapManagerHolder.class) {
                if (instance == null) {
                    instance = new MapManagerHolder();
                }
            }
        }
        return instance;
    }
}
