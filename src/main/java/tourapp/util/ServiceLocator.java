package tourapp.util;

import java.util.HashMap;
import java.util.Map;

public class ServiceLocator {
    private final Map<Class<?>, Object> instances = new HashMap<>();

    public <T> void register(Class<T> type, T instance) {
        instances.put(type, instance);
    }

    @SuppressWarnings("unchecked")
    public <T> T resolve(Class<T> type) {
        Object instance = instances.get(type);
        if (instance == null) {
            throw new IllegalStateException("No service registered for type: " + type.getName());
        }
        return (T) instance;
    }

    public <T> boolean contains(Class<T> type) {
        return instances.containsKey(type);
    }

    public <T> void remove(Class<T> type) {
        instances.remove(type);
    }

    public void clear() {
        instances.clear();
    }
}