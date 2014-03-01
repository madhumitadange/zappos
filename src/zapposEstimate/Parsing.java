package zapposEstimate;

import java.io.*;
import java.net.*;

import org.json.simple.*;
import org.json.simple.parser.*;

public class Parsing {
	public static final String BASEURL = "http://api.zappos.com/Search?key=12c3302e49b9b40ab8a222d7cf79a69ad11ffd78";
	
	
	
	
	public static String httpGet(String urlStr) throws IOException {
		URL url = new URL(urlStr);
		HttpURLConnection conn =
		      (HttpURLConnection) url.openConnection();
	
		if (conn.getResponseCode() != 200) {
		    throw new IOException(conn.getResponseMessage());
		}
	
		// Buffer the result into a string
		BufferedReader rd = new BufferedReader(
	      new InputStreamReader(conn.getInputStream()));
		StringBuilder sb = new StringBuilder();
		String line;
		while ((line = rd.readLine()) != null) {
			sb.append(line);
		}
		rd.close();
	
		conn.disconnect();
		return sb.toString();
	}
	
	/**
	 * Parses the search API's JSON response into a JSON object
	 
	 */
	public static JSONObject parseReply(String reply) throws ParseException{
		JSONParser parser = new JSONParser();
		Object obj = parser.parse(reply);
		JSONObject object = (JSONObject)obj;
		return object;
	}
	
	/**
	 * Gets the "results" array out of the JSON object the server returns
	 * @param reply The JSON object form of the server's response
	 * @return The JSONArray of the results portion
	 */
	public static JSONArray getResults(JSONObject reply){
		Object resultObject = reply.get("results");
		JSONArray resultArray = (JSONArray)resultObject;
		return resultArray;
	}
}
