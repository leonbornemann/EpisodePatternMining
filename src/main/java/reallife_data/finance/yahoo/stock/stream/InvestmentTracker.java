package reallife_data.finance.yahoo.stock.stream;

public class InvestmentTracker {

	private double relativeDelta;
	private double price;
	private int numStocks;
	private double moneyAmount;

	public InvestmentTracker(double relativeDelta) {
		this.relativeDelta = relativeDelta;
		price = 100.0; //arbitrary 
		numStocks = 10;
		moneyAmount = 0.0;
	}

	public double netWorth(){
		return moneyAmount + numStocks*price;
	}
	
	public void up() {
		price = price +price*relativeDelta;
	}

	public void down() {
		price = price - price*relativeDelta;
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
}
