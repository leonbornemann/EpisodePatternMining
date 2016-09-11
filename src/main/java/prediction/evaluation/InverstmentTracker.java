package prediction.evaluation;

public class InverstmentTracker {
	
	private double price;
	private int numStocks;
	private double moneyAmount;
	private double startingInvestment;

	public InverstmentTracker(double initialPrice) {
		price = initialPrice; //arbitrary 
		numStocks = 10;
		moneyAmount = 0;
		startingInvestment = netWorth();
	}

	public double netWorth(){
		return moneyAmount + numStocks*price;
	}
	
	public void setPrice(double price) {
		this.price = price;
	}

	public void buyIfPossible() {
		if(numStocks==0){
			numStocks = 10;
			moneyAmount -= numStocks*price;
		}
	}

	public void sellIfPossible(){
		if(numStocks==10){
			moneyAmount += numStocks*price;
			numStocks=0;
		}
	}

	public double getPrice() {
		return price;
	}
	
	public double rateOfReturn(){
		return (netWorth()-startingInvestment)/startingInvestment;
	}
}
