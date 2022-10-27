import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;
import java.lang.reflect.Field;

import agent.Agent;
import analysis.Analyzer;

import static shared.Constants.*;

import environment.Area;
import environment.Environment;
import random.Seed;
import random.Sfmt;
import shared.Constants;

public class Main {
	static String csv_base_path;
	static int tick = 0;
	public static void main(String[] args){
		getConstants();
		String mode = args[0];
		Analyzer analyzer = null;
		if(mode.equals("RECIPROCITY")){
			RECIPROCITY = true;
			analyzer = new Analyzer("csv/Reciprocity");
		}else if(mode.equals("RATIONAL")){
			RECIPROCITY = false;
			analyzer = new Analyzer("csv/Rational");
		}else if(mode.equals("CNP")){
			CNP_MODE = true;
			analyzer = new Analyzer("csv/CNP");
		}
		long start = System.currentTimeMillis();
		
		for(int trial = 0;trial<TRIAL_COUNT;trial++){
			Environment e = new Environment(Seed._seeds[trial]);
			for(tick=0;tick<EXPERIMENTAL_DURATION;tick++){
				e.run(tick);
//				if(RECIPROCITY == true && (tick == CHANGE_SUBTASKS_TIME || tick == RESTORE_SUBTASKS_TIME || tick == FIRST_MEASURE_TIME)){
//					e.exportAgentConnection(tick);
//					e.exportForCytoscape(tick);
//				}
			} 
//			e.printArea();
//			e.printDeAgent();
//			e.exportAllocatedSubTask(RECIPROCITY);
//			e.exportOwnedSubTask(RECIPROCITY);
			
			
		}
		long end = System.currentTimeMillis();
		System.out.println((end - start)  + "ms");
		analyzer.exportAreaData();
		analyzer.exportEnvironmentData();
	}
	
	private static void getConstants() {
		try{
			FileWriter fw = new FileWriter("config/config.txt", false);
	        PrintWriter pw = new PrintWriter(new BufferedWriter(fw));

	        Field[] fields = Constants.class.getDeclaredFields();

	        for(Field field : fields){
	            field.setAccessible(true);
	            try {
					pw.println(field.getName() + " = " + field.get(field));
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
	        }
	        pw.close();
		}catch(IOException ex){
			System.out.println(ex);
		}
	}
}
