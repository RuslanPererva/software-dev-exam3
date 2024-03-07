import java.io.File;
import java.net.URI;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class CityPopulationAdapter {

    private Map<String, Integer> cache;
    private CityPopulationReader CReader;
    private File file;

    public CityPopulationAdapter(File f) {
        if (f == null) {
            throw new IllegalArgumentException("file cannot be null");
        }
        this.cache = new HashMap<>();
        this.CReader = new CityPopulationReader();
        this.file = f;
    }

    public int getPopulation(String city, String state) {
        if (city == null || city.isBlank()) {
            throw new IllegalArgumentException("city cannot be null");
        }

        if (state == null || state.isBlank()) {
            throw new IllegalArgumentException("state cannot be null");
        }

        String key = city.toLowerCase() + state.toLowerCase();
        if (cache.containsKey(key)) {
            return cache.get(key);
        } else {
            
            String uriSTR="";
            uriSTR += file.toURI();
            uriSTR += String.format("?city="+city.toLowerCase()+"&state="+state.toLowerCase());
            try {
            	URI uri = URI.create(uriSTR);
            	//System.out.println(uri);
                int pop = CityPopulationReader.readPopulation(uri);
                //System.out.println ("pop "+pop);
                if (pop > -1) {
                    cache.put(key, pop);
                    return pop;
                }
                
            }
            catch (IllegalArgumentException e) {
            	throw e;
            }
            
        }
		return -1;
    }
}
