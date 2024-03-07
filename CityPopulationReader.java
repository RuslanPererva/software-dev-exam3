import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CityPopulationReader {

    public static int readPopulation(URI uri) {
    	String FCity = "";
        String FState = "";
    	String city = "";
        String state = "";
        int FPop = -1;
        System.out.println ("Reader uri:"+uri);
        if (uri == null) {
            throw new IllegalArgumentException("uri cannot be null");
        }
        String query = uri.getQuery();
        System.out.println(query);
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("missing query");
        }
        
        if (!query.contains("city")) {
        	throw new IllegalArgumentException("missing city");
        }
        if (!query.contains("state")) {
        	throw new IllegalArgumentException("missing state");
        }
        query=query.replace("city=", "");
        query=query.replace("state=", "");
        
        String [] temp = query.split("&");
        
        if (temp[0].length() == 2) {
        	state = temp[0];
        	city = temp[1].replace("+", " ");
        }
        else {
        	state = temp[1];
        	city = temp[0].replace("+", " ");
        }

        if (city == null || city.equals("")) {
            throw new IllegalArgumentException("missing city");
        }
        if (state == null || state.equals("")) {
            throw new IllegalArgumentException("missing state");
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(uri.getPath()))) {
            String row;
            while ((row = reader.readLine()) != null) {
                String[] splitRow = row.split(",");
                
                    FCity = splitRow[0].trim().toLowerCase();
                    FState = splitRow[1].trim().toLowerCase();
                    
                    if (FCity.equals(city) && FState.equals(state)) {
                    	FPop = Integer.parseInt(splitRow[2]);
                        return FPop;
                    
                }
            }
        } catch (IOException | NumberFormatException e) {
        	
        }
        return -1;
    }
}