import java.lang.reflect.Constructor;

public class PluginManager {
    private static PluginManager INSTANCE = new PluginManager();

    private PluginManager() {
    }

    public static PluginManager getInstance() {
        return INSTANCE;
    }

    public static void load(String classname) {
        try {
            Class<?> PinClass = Class.forName(classname);
            if (DocumentPlugin.class.isAssignableFrom(PinClass)) {
                Constructor<?> defcon = PinClass.getConstructor();
                DocumentPlugin plugin = (DocumentPlugin) PinClass.getDeclaredConstructor().newInstance();
                plugin.init();
            } else {
                throw new IllegalStateException(String.format("class '%s' is not of type DocumentPlugin", classname));
            }
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(String.format("error loading class '%s'", classname), e);
        } catch (IllegalStateException | ReflectiveOperationException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}
