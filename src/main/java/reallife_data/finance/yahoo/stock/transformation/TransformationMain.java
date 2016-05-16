package reallife_data.finance.yahoo.stock.transformation;

import java.io.File;
import java.io.IOException;

public class TransformationMain {

	private static String dataBaseLocation = "D:\\Personal\\Documents\\Uni\\Master thesis\\Datasets\\Finance\\Low Level Data\\";
	private static String target = "D:\\Personal\\Documents\\Uni\\Master thesis\\Datasets\\Finance\\Annotated Data\\";
	
	public static void main(String[] args) throws IOException {
		LowToAnnotatedTransformator transformer = new LowToAnnotatedTransformator(new File(dataBaseLocation),new File(target));
		transformer.transform();
	}
}
