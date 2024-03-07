import java.util.function.Supplier;

public class PluginReplaceText implements DocumentPlugin {
	private String find;
	private String replace;
	

	@Override
	public String getName() {
		return "replace_text";
	}

	@Override
	public Supplier<DocumentPlugin> getSupplier() {
		return () -> new PluginReplaceText();
	}
	
	public String apply (Document d) {
		if (d == null) {
			throw new IllegalArgumentException("document cannot be null");
		}
		if (find == null || replace == null) {
			throw new IllegalArgumentException("find/replace values not set");
		}
		
		
		String temp = d.getContents();
		temp = temp.replace(find, replace);
		return temp;
	}
	public void setReplace (String f, String r) {
		if (f ==null) {
				throw new IllegalArgumentException("find cannot be null");
		}
		if (r ==null) {
			throw new IllegalArgumentException("replace cannot be null");
	}
		this.find=f;
		this.replace=r;
//		System.out.println(f);
//		System.out.println(r);
//		System.out.println(find);
//		System.out.println(replace);
	}

}
