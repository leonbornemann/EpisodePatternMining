package reallife_data.finance.yahoo.stock.transformation;

import java.io.File;
import java.io.IOException;

public class TransformationMain {

	private static String databaseLocationLaptop = "C:\\Users\\LeonBornemann\\Documents\\Uni\\Master thesis\\data\\Low Level Data\\";
	private static String targetLaptop = "C:\\Users\\LeonBornemann\\Documents\\Uni\\Master thesis\\data\\Annotated Data\\";
	
	private static String dataBaseLocation = "D:\\Personal\\Documents\\Uni\\Master thesis\\Datasets\\Finance\\Low Level Data\\";
	private static String target = "D:\\Personal\\Documents\\Uni\\Master thesis\\Datasets\\Finance\\Annotated Data\\";
	
	public static void main(String[] args) throws IOException {
		laptop();
		//desktop();
	}

	private static void laptop() throws IOException {
		LowToAnnotatedTransformator transformer = new LowToAnnotatedTransformator(new File(databaseLocationLaptop),new File(targetLaptop),0.001);
		transformer.transform();
	}

	private static void desktop() throws IOException {
		LowToAnnotatedTransformator transformer = new LowToAnnotatedTransformator(new File(dataBaseLocation),new File(target),0.001);
		transformer.transform();
	}
}
