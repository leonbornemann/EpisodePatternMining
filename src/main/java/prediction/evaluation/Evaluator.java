package prediction.evaluation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import data.Change;
import prediction.util.StandardDateTimeFormatter;
import util.Pair;

public abstract class Evaluator {

	protected List<Pair<LocalDateTime, Change>> deserializePairList(File file) throws IOException{
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line = reader.readLine();
		List<Pair<LocalDateTime, Change>> list = new ArrayList<>();
		while(line!=null){
			String[] tokens = line.split(",");
			assert(tokens.length==2);
			list.add(new Pair<LocalDateTime, Change>(LocalDateTime.parse(tokens[0], StandardDateTimeFormatter.getStandardDateTimeFormatter()),Change.valueOf(tokens[1])));
			line = reader.readLine();
		}
		reader.close();
		return list;
	}

}
