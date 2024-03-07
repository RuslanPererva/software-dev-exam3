import java.util.function.Supplier;
import java.util.*;

public class PluginCountWords implements DocumentPlugin {
	

	@Override
	public String getName() {
		return "count_words";
	}

	@Override
	public Supplier<DocumentPlugin> getSupplier() {
		return () -> new PluginCountWords();
	}
	
	public Integer apply (Document d) {
		if (d == null) {
			throw new IllegalArgumentException("document cannot be null");
		}
		String temp = d.getContents();
		System.out.println(temp);
		if (temp.isBlank()) {
			return 0;
		}
		String [] templist = temp.split(" ");
		return templist.length;
	}

}
