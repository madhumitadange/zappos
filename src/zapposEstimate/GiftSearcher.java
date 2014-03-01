
package zapposEstimate;

import static java.awt.image.ImageObserver.WIDTH;
import java.io.*;
import java.util.*;
import javax.swing.JOptionPane;
import org.json.simple.*;
import org.json.simple.parser.*;

public class GiftSearcher {
	private int numItems;			//number of items
	private double totalPrice;		//total price of items
	private double maxPrice;		//max feasible price
	private int page;				//page of results
	private JSONArray products;		//products in range
	private ArrayList<Product> productObjects; //JSON->Product list
	private ArrayList<FindCombo> FindCombos; //list of product combos
	private final double TOL = Math.pow(10, -7);  //tolerance for subtracting doubles
	private final int MAXCOMBOS = 30;
	
	
	public GiftSearcher(int num, double total) {
		numItems = num;
		totalPrice = total;
		maxPrice = Integer.MAX_VALUE; 	//will set later
		page = 1;					//will pull at least one page of results
		products = new JSONArray();
		productObjects = new ArrayList<Product>();
		FindCombos = new ArrayList<FindCombo>();
	}
	
	
	private Double getPrice(Object item){
		return Double.parseDouble(((String) ((JSONObject) item).get("price")).substring(1));
	}
	
	
	@SuppressWarnings("unchecked")
	private void setProductsInRange() throws IOException, ParseException {
		//get maximum amount of products (100), starting at lowest price, and pull out results
		String reply = Parsing.httpGet(Parsing.BASEURL + "&term=&limit=10&sort={\"price\":\"asc\"}");
		JSONObject replyObject = Parsing.parseReply(reply);
		JSONArray resultArray = Parsing.getResults(replyObject);
		
		//get the first product's price (substring(1) to skip the $)
		double firstPrice = getPrice(resultArray.get(0));
		
		//if cheapest n items still over total price, then return empty string
		if( (firstPrice * numItems) > totalPrice) {
			products = null;
		}
		
		//otherwise, figure out what maximum price is given the minimum price
		maxPrice = totalPrice - (numItems - 1)*(firstPrice);
		
		//increment page, since we've already pulled the first page of results
		page++;
		
		//get the last product's price (substring(1) to skip the $)
		Double lastPrice = getPrice(resultArray.get(resultArray.size() - 1));
		
		//while the last price in returned page of results is less than max price,
		//pull another page of results
		while(lastPrice < maxPrice) { 
			//System.out.println("Last price: " + lastPrice);
			String nextPage = Parsing.httpGet(Parsing.BASEURL + "&term=&limit=10&sort={\"price\":\"asc\"}&page=" + page);
			//System.out.println("Pulling page " + page + "...");
			JSONObject nextObject = Parsing.parseReply(nextPage);
			JSONArray nextArray = Parsing.getResults(nextObject);
			
			//append new page of results to original array
			resultArray.addAll(nextArray);
			
			//get new last product and price
			lastPrice = getPrice(nextArray.get(nextArray.size() - 1));
			
			page++;
		}

		//return resultArray.toString();
		products = resultArray;
	}
	
	
	private void setSearchableProducts() {
		//add the first (smallest price) object
		productObjects.add(new Product((JSONObject)products.get(0)));
		
		//count how many times a price has already shown up
		int already = 1;
		int numPrices = 1;
		//go through the whole 
		for(int i = 1; i < products.size() && getPrice(products.get(i)) < maxPrice; i++) {
			double currentPrice = getPrice(products.get(i));
			if( currentPrice > productObjects.get(numPrices-1).getPrice()) {
				productObjects.add(new Product((JSONObject)products.get(i)));
				numPrices++;
				already = 1;
			} else if(Math.abs(currentPrice - productObjects.get(numPrices-1).getPrice()) < TOL && already < numItems){
				productObjects.add(new Product((JSONObject)products.get(i)));
				numPrices++;
				already++;
			} else {
				while(i < products.size() && Math.abs(currentPrice - productObjects.get(numPrices-1).getPrice()) < TOL) {
					i++;
					currentPrice = getPrice(products.get(i));
				}
				i++;
				already = 0;
			}
		}
	}

	
	private void setFindCombos() {
		setFindCombosRecursive(productObjects, totalPrice, new ArrayList<Product>());
	}
	
	
	private void setFindCombosRecursive(ArrayList<Product> productList, double target, ArrayList<Product> partial) {
		int priceWithinAmount = 1;
		
		//if partial size > numItems, you already have too many items, so stop
		if(partial.size() > numItems) { return; }
		
		double sum = 0;
		for(Product x : partial) sum += x.getPrice();
		
		//if sum is within $1 of target, and partial size is numItems, and you don't already have too many product 
		//combos, then add another product combo
		if(Math.abs(sum - target) < priceWithinAmount && partial.size() == numItems && FindCombos.size() < MAXCOMBOS) {
			//if no price combos yet, just add it on
			if(FindCombos.size() == 0) {	FindCombos.add(new FindCombo(partial, totalPrice)); }
			//otherwise, check it against the most recent product combo to make sure you're not repeating
			//TODO: check all product combos
			else{
				FindCombo testerCombo = FindCombos.get(FindCombos.size() -1);
				FindCombo partialCombo = new FindCombo(partial, totalPrice);
				if(!partialCombo.equals(testerCombo)) {
					FindCombos.add(partialCombo);
				}
			}
		}
		//if sum is at or within $1 of target, then stop - done!
		if(sum >= target + priceWithinAmount) {
			return;
		}
		
		//otherwise, recursively continue adding another product to combo and test it
		for(int i = 0; i < productList.size() && !(partial.size() == numItems && sum < target); i++){
			ArrayList<Product> remaining = new ArrayList<Product>();
			Product n = productList.get(i);
			for(int j=i+1; j < productList.size(); j++) {remaining.add(productList.get(j)); }
			ArrayList<Product> partial_rec = new ArrayList<Product>(partial);
			partial_rec.add(n);
			setFindCombosRecursive(remaining, target, partial_rec);
		}
	}
	
	
	@SuppressWarnings("unchecked")
	private void sortFindCombos() {
		Collections.sort(FindCombos);
	}
	
	
	public String getGiftCombos() {
            try{
		//get products from API
		System.out.println("Searching....");
		this.setProductsInRange();
		
		System.out.println("Finding combinations...");
		//convert to Products
		this.setSearchableProducts();
		//find combinations that work
		this.setFindCombos();
		//sort combos by how close they are to given total
		this.sortFindCombos();
		
		//see if you have any combos
		if(FindCombos.size() != 0) {
			String toPrint = "\nDone!\n";
			for(FindCombo x:FindCombos) {
				toPrint += x.toString() + "\n";
			}
			return toPrint;
		}
		else {
			return "We couldn't find a set of items matching your criteria. " +
					"Please try again with fewer items or a larger dollar amount.";
		}
	}catch(Exception e){
            
                            JOptionPane.showMessageDialog(null, "Something went wrong", "Error", WIDTH);
	return null;		
                        
        }

}
	
}