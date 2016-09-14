package prediction.data.transformation;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;

public class TransformationMain {

	private static String databaseLocationLaptop = "C:\\Users\\LeonBornemann\\Documents\\Uni\\Master thesis\\data\\Low Level Data\\";
	private static String targetLaptop = "C:\\Users\\LeonBornemann\\Documents\\Uni\\Master thesis\\data\\Annotated Data\\";
	private static String illegalFormatDirLaptop = "C:\\Users\\LeonBornemann\\Documents\\Uni\\Master thesis\\data\\Low Level Bad Format\\";
	
	private static String dataBaseLocation = "D:\\Personal\\Documents\\Uni\\Master thesis\\Datasets\\Finance\\Low Level Data\\";
	private static String target = "D:\\Personal\\Documents\\Uni\\Master thesis\\Datasets\\Finance\\Annotated Data\\";
	private static String illegalFormatDir = "D:\\Personal\\Documents\\Uni\\Master thesis\\Datasets\\Finance\\Illegally Formatted";
	
	public static void main(String[] args) throws IOException {
		//laptop();
		desktop();
	}

	private static void laptop() throws IOException {
		LowToAnnotatedTransformator transformer = new LowToAnnotatedTransformator(new File(databaseLocationLaptop),new File(targetLaptop),new File(illegalFormatDirLaptop),new BigDecimal("0.001"));
		transformer.transform();
	}

	private static void desktop() throws IOException {
		LowToAnnotatedTransformator transformer = new LowToAnnotatedTransformator(new File(dataBaseLocation),new File(target),new File(illegalFormatDir));
		transformer.transform();
	}
}
