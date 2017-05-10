import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
//You need to download this jar file in order to use JSON objects in Java:
//http://www.java2s.com/Code/JarDownload/java/java-json.jar.zip

public class BackEndDevelopmentProblem {

  private static String readAll(Reader rd) throws IOException {
    StringBuilder sb = new StringBuilder();
    int cp;
    while ((cp = rd.read()) != -1) {
      sb.append((char) cp);
    }
    return sb.toString();
  }

  public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
    InputStream is = new URL(url).openStream();
    try {
      BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
      String jsonText = readAll(rd);
      JSONObject json = new JSONObject(jsonText);
      return json;
    } finally {
      is.close();
    }
  }

  public static void main(String[] args) throws IOException, JSONException {
    JSONArray allOrders = new JSONArray(); //JSON array that stores all orders
    JSONArray unfulfilledCookieOrders = new JSONArray(); //Holds all the unfufilled cookie orders
    JSONArray customerProducts = new JSONArray(); //Holds all the info about a specific customer's order
    JSONObject jsonObjPageInfo = null;
    int pageCounter = 1;
    int cookies = 0;
    boolean finished = false;
    
    //This loop grabs all the unfufilled cookie orders from the paginated API
    while (!finished)
    {
    	if (pageCounter == 1)
    	{
    		jsonObjPageInfo = readJsonFromUrl("https://backend-challenge-fall-2017.herokuapp.com/orders.json");
    		cookies = (int)jsonObjPageInfo.get("available_cookies");
    	}

    	else
    		jsonObjPageInfo = readJsonFromUrl("https://backend-challenge-fall-2017.herokuapp.com/orders.json?page=" + pageCounter);
    	allOrders = jsonObjPageInfo.getJSONArray("orders");
    	
    	if (allOrders.length() == 0)
    		finished = true; 	
    	else
    	{
    		//Get rid of all orders that have already been fufilled
    		for (int a = 0; a < allOrders.length(); a++)
    		{
    			if (allOrders.getJSONObject(a).get("fulfilled").toString().equals("false"))
    			{
    				customerProducts = allOrders.getJSONObject(a).getJSONArray("products");
    				boolean hasCookies = false;
    				for (int b = 0; b < customerProducts.length() && !hasCookies; b++)
    				{
    					//Make sure the customer has a cookie in their "products" section
    					if (customerProducts.getJSONObject(b).get("title").toString().equals("Cookie"))
    						unfulfilledCookieOrders.put(allOrders.getJSONObject((a)));
    				}
    			}
    			
    		}
    	}

    	pageCounter++; //Determine which page of the API to read from
    }//while
    
    JSONArray customerCookieProducts = new JSONArray();
    int biggestOrderId; //The id of the order with the biggest cookie order that we can fufill
    int biggestAmount;
    int biggestOrderNumber = 0;
    int biggestProductNumber = 0;
    boolean moreOrders = true; //While we still have more orders we can fufill
    
    while (moreOrders)
    {
    	biggestOrderId = -1;
    	biggestAmount = 0;
        for (int orderNumber = 0; orderNumber < unfulfilledCookieOrders.length(); orderNumber++)
        {
        	customerCookieProducts = unfulfilledCookieOrders.getJSONObject(orderNumber).getJSONArray("products");
        	for (int productNumber = 0; productNumber < customerCookieProducts.length(); productNumber++)
        	{
        		String productName = customerCookieProducts.getJSONObject(productNumber).get("title").toString();
        		int productAmount = Integer.parseInt(customerCookieProducts.getJSONObject(productNumber).get("amount").toString());
        		String fulfilled = unfulfilledCookieOrders.getJSONObject(orderNumber).get("fulfilled").toString();
        		
        		//The the current product has a bigger amount of cookies that can be fufilled, replace the highest priority order
        		if (productName.equals("Cookie") && productAmount > biggestAmount && cookies >= productAmount && fulfilled.equals("false"))
        		{
        			biggestOrderNumber = orderNumber;
        			biggestProductNumber = productNumber;
        			biggestOrderId = Integer.parseInt(unfulfilledCookieOrders.getJSONObject(orderNumber).get("id").toString());
        			biggestAmount = productAmount;
        		}
        	}
        }
        
        if (biggestOrderId == -1)
        	moreOrders = false;
        else
        {
        	unfulfilledCookieOrders.getJSONObject(biggestOrderNumber).put("fulfilled", "true");
        	cookies -= biggestAmount;
        }
        	
    }//while
    
    JSONArray remainingOrders = new JSONArray();
    
    for (int e = 0; e < unfulfilledCookieOrders.length(); e++)
    {
    	if (unfulfilledCookieOrders.getJSONObject(e).get("fulfilled").toString().equals("false"))
    	{
    		remainingOrders.put((int)unfulfilledCookieOrders.getJSONObject(e).get("id"));
    	}	
    }
    
    JSONObject results = new JSONObject();
    results.put("remaining_cookies", cookies);
    results.put("unfulfilled_orders", remainingOrders);
    System.out.println(results);
  }//main
  
}//BackEndDevelopmentProblem















