package prediction.evaluation;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class InvestmentTracker {
	
	private BigDecimal price;
	private BigDecimal numStocks;
	private BigDecimal moneyAmount;
	private BigDecimal startingInvestment;
	private int roundingScale = 100;

	public InvestmentTracker(BigDecimal initialPrice) {
		price = initialPrice;
		numStocks = BigDecimal.TEN;
		moneyAmount = BigDecimal.ZERO;
		startingInvestment = netWorth();
	}
	
	public InvestmentTracker(BigDecimal initalPrice,BigDecimal money){
		price = initalPrice;
		numStocks = BigDecimal.ZERO;
		moneyAmount = money;
		startingInvestment = netWorth();
	}

	public BigDecimal netWorth(){
		return moneyAmount.add(price.multiply(numStocks));
	}
	
	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public void buyIfPossible() {
		if(numStocks.equals(BigDecimal.ZERO)){
			numStocks = BigDecimal.TEN;
			moneyAmount = moneyAmount.subtract(numStocks.multiply(price));
		}
	}

	public void sellIfPossible(){
		if(numStocks.equals(BigDecimal.TEN)){
			moneyAmount = moneyAmount.add(numStocks.multiply(price));
			numStocks=BigDecimal.ZERO;
		}
	}

	public BigDecimal getPrice() {
		return price;
	}
	
	public BigDecimal rateOfReturn(){
		return (netWorth().subtract(startingInvestment)).divide(startingInvestment,roundingScale ,RoundingMode.FLOOR);
	}

	public BigDecimal rateOfReturn(BigDecimal startingInvestment) {
		return (netWorth().subtract(startingInvestment)).divide(startingInvestment,roundingScale ,RoundingMode.FLOOR);
	}
}
