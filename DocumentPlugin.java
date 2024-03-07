import java.util.*;
import java.util.function.Supplier;
public interface DocumentPlugin {
	default void init() {
		new DocumentPluginFactory().add(getName(),getSupplier());
	}
	String getName();
	Supplier<DocumentPlugin> getSupplier();
	Object apply(Document d);
	
}
