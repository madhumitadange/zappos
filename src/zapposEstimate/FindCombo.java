// finds combinations 
package zapposEstimate;

import java.util.ArrayList;

@SuppressWarnings("rawtypes")
public class FindCombo implements Comparable{
	
	private double closeness;					//how close sum is to total
	private final double TOL = Math.pow(10, -7);//tolerance 
	private ArrayList<Product> comboProds;		//prices to sort, etc.
	private double sum;							//sum of the prices
	private double idealTotal; 					//total prices should be near
	
	
	public FindCombo(ArrayList<Product> productsForCombo, double total) {
		comboProds = productsForCombo;
		sum = 0;
		idealTotal = total;
		for(Product x:comboProds) sum += x.getPrice(); 
		closeness = Math.abs(idealTotal - sum);
	}
	
	
	public double getPrice(int index) {
		return comboProds.get(index).getPrice();
	}
	
	
	public double getSum() {
		return sum;
	}
	
	
	public int getProductComboLength() {
		return comboProds.size();
	}
	
	
	public double getCloseness() {
		return closeness;
	}
	
	
	public double getTotal() {
		return idealTotal;
	}

	
	@Override
	public int compareTo(Object o) {
		FindCombo other = (FindCombo) o;
		if(this.equals(other)) return 0;
		else if(this.closeness < other.getCloseness()) return -1;
		else return 1;
	}
	
	
	public boolean equals(FindCombo other) {
		if(this.comboProds.size() != other.getProductComboLength()) {
			return false;
		}
		if(this.idealTotal != other.getTotal()) {
			return false;
		}
		for(int i = 0; i < comboProds.size(); i++){
			if(Math.abs(this.comboProds.get(i).getPrice() - other.getPrice(i)) > TOL) {
				return false;
			}
		}
		return true;
	}
	
	
	public String toString() {
		String toReturn = "Products with sum $" + sum + "\n";
		for(int i = 0; i < comboProds.size(); i ++) {
			toReturn += (i+1) + ": " + comboProds.get(i).toString() + "\n";
		}
		return toReturn;
	}
	
}
