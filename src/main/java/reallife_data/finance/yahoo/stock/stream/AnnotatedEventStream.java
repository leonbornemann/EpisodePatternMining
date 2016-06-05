package reallife_data.finance.yahoo.stock.stream;

public interface AnnotatedEventStream {

	public StreamWindow getBackwardsWindow(int d);
}
