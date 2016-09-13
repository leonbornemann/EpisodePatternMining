package prediction.evaluation;

public class InvestmentTracker {
	
	private double price;
	private int numStocks;
	private double moneyAmount;
	private double startingInvestment;

	public InvestmentTracker(double initialPrice) {
		price = initialPrice; //arbitrary 
		numStocks = 10;
		moneyAmount = 0;
		startingInvestment = netWorth();
	}
	
	public InvestmentTracker(double initalPrice,double money){
		price = initalPrice;
		numStocks = 0;
		moneyAmount = money;
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

	public double rateOfReturn(double startingInvestment) {
		return (netWorth()-startingInvestment)/startingInvestment;
	}
}
