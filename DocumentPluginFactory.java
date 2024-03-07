import java.util.*;
import java.util.function.Supplier;

public class DocumentPluginFactory {
	private static final Map<String, Supplier<DocumentPlugin>> pluginMap = new HashMap<>();

	public void add(String name, Supplier<DocumentPlugin> supplier) {
		if (supplier == null ) {
			throw new IllegalArgumentException("supplier cannot be null");
		}
		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException("name cannot be null or blank");
		}
		pluginMap.put(name, supplier);
	}
	
	public DocumentPlugin get(String name) {
		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException("name cannot be null or blank");
		}
		if (!pluginMap.containsKey(name)) {
			throw new IllegalArgumentException("name doesn't exist");
		}
		return pluginMap.get(name).get();
	}
	public boolean has (String name) {
		
		return pluginMap.containsKey(name);
	}
}
