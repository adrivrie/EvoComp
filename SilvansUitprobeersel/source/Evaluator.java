import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Random;

import org.vu.contest.ContestEvaluation;

public class Evaluator {





	public static void main(String[] args) {
		paramSearch();
		
		
		//player64 model = new player64();
		//simpleTest(model, 8, 324798L); //93476678L
		//xRunsTest(model, 2, 324798L, 2000000);
	}
	
	
	private static void paramSearch() {
		long maxEvals = 2000000;
		int nSeeds = 3;
		
		Data data;
		
		try{
			Path filePath = Data.check_file("testRun1.csv");
			File file = new File(filePath.toString());
			FileWriter writer = new FileWriter(file, true);

			// write all to file
			writer.write(String.format("%s,%s,%s,%s,%s,%s,%s\n",
					"lifetimeMethod", "nIslands", "initIslandSize", "migrationSize", "maxLifetime",
					"seeds", "fitnesses"));
			writer.close();
		} catch (IOException e){
			e.printStackTrace();
		}
		
		while(true) {
			player64 model = new player64();
			// select params
			Random r = new Random();
			int lifetimeMethod = r.nextInt(3);
			int nIslands = (int)Math.pow(10, r.nextDouble()*Math.log10(20)); // 1 - 250
			int initIslandSize = (int)(10*Math.pow(10, r.nextDouble()*Math.log10(60))); // 10 - 1000
			int migrationSize = (int)Math.pow(10, r.nextDouble()*Math.log10(initIslandSize/2-1)); 
			int maxLifetime = (int)(1*Math.pow(10, r.nextDouble()*Math.log10(10))); // 1 - 10
			
			System.out.println("Run with method="+lifetimeMethod+", nIslands="+nIslands+", initialSize="+initIslandSize+
					", migrationSize="+migrationSize+", maxLifetime="+maxLifetime);
			
			Long[] seeds = new Long[nSeeds];
			Double[] fitnesses = new Double[nSeeds];
			for (int i=0; i<3; i++) {
				// set seed
				long seed = r.nextLong();
				// evaluate model
				model.setSeed(seed);
				model.setEvaluation(new EvaluateFitness(EvaluateFitness.FUNCTION_KATSUURA, maxEvals));
				model.setExperiment(lifetimeMethod, nIslands, initIslandSize, migrationSize, maxLifetime);
				data = model.runData();
				// get data
				seeds[i] = seed;
				fitnesses[i] = data.fitness;
			}
			
			System.out.println("\tFitnesses: "+arrayToString(fitnesses));
			
			// write to csv
			try{
				Path filePath = Data.check_file("testRun1.csv");
				File file = new File(filePath.toString());
				FileWriter writer = new FileWriter(file, true);

				// write all to file
				writer.write(String.format("%d,%d,%d,%d,%d,%s,%s\n",
						lifetimeMethod, nIslands, initIslandSize, migrationSize, maxLifetime,
						arrayToString(seeds), arrayToString(fitnesses)));
				writer.close();
			} catch (IOException e){
				e.printStackTrace();
			}
		}
		
	}
	
	private static String arrayToString(Object[] arr) {
		String str = "";
		boolean fst = true;
		for (Object el : arr) {
			if (fst) {
				fst = false;
			} else {
				str += ";";
			}
			str += el.toString();
		}
		str += "";
		return str;
	}
	
	
	

	/**
	 * Example method that runs the model on different functions.
	 * @param model: player64 instance containing the model
	 * @param functionSelection: binary selector for the functions to evaluate (e.g. 4 -> 0100 -> only the 3rd function)
	 * @param seed: seed for the random number generator in the model
	 */
	private static void simpleTest(player64 model, int functionSelection, long seed) {
		long maxEvals = 200000L;

		if ((functionSelection & 1) == 1) {
			model.setSeed(seed);
			System.out.println("Sphere Evaluation");
			model.setEvaluation(new EvaluateFitness(EvaluateFitness.FUNCTION_SPHERE, maxEvals));
			model.runData();
			System.out.println();
			System.out.println();
			System.out.println();
			System.out.println();
		}

		if ((functionSelection & 2) == 2) {
			model.setSeed(seed);
			System.out.println("Bent-Cigar Function");
			model.setEvaluation(new EvaluateFitness(EvaluateFitness.FUNCTION_BENT_CIGAR, maxEvals));
			model.runData();
			System.out.println();
			System.out.println();
			System.out.println();
			System.out.println();
		}

		if ((functionSelection & 4) == 4) {
			model.setSeed(seed);
			System.out.println("Katsuura Evaluation");
			model.setEvaluation(new EvaluateFitness(EvaluateFitness.FUNCTION_KATSUURA, maxEvals));
			model.runData();
			System.out.println();
			System.out.println();
			System.out.println();
			System.out.println();
		}

		if ((functionSelection & 8) == 8) {
			model.setSeed(seed); // 1452345243 -> pretty good
			System.out.println("Schaffers Evaluation");
			model.setEvaluation(new EvaluateFitness(EvaluateFitness.FUNCTION_SCHAFFERS, maxEvals));
			model.runData();
			System.out.println();
			System.out.println();
			System.out.println();
			System.out.println();
		}
	}

	/**
	 * Example method that runs the model on different functions.
	 * @param model: player64 instance containing the model
	 * @param functionSelection: binary selector for the functions to evaluate (e.g. 4 -> 0100 -> only the 3rd function)
	 * @param seed: seed for the random number generator in the model
	 * @param numberOfRuns: amount of runs to be made with current settings
	 */
	private static void xRunsTest(player64 model, int functionSelection, long seed, int numberOfRuns){
		for(int runNumber=0;runNumber<numberOfRuns;runNumber++){
			// TODO get designator for which algorithm we use
			String runName = String.format("Algorithm%d_%d",functionSelection,numberOfRuns);
			Data data;
			// modelname consists of functionselector and runNumber
			if ((functionSelection & 1) == 1) {
				model.setSeed(seed);
				System.out.println("Sphere Evaluation");
				model.setEvaluation(new SphereEvaluation());
				model.runName = runName;
				model.runData();
				System.out.println();
				System.out.println();
				System.out.println();
				System.out.println();
			}

			if ((functionSelection & 2) == 2) {
				model.setSeed(seed);
				System.out.println("Bent-Cigar Function");
				model.setEvaluation(new BentCigarFunction());
				model.runName = runName;
				model.runData();
				System.out.println();
				System.out.println();
				System.out.println();
				System.out.println();
			}

			if ((functionSelection & 4) == 4) {
				model.setSeed(seed);
				System.out.println("Katsuura Evaluation");
				model.setEvaluation(new KatsuuraEvaluation());
				model.runName = runName;
				model.runData();
				System.out.println();
				System.out.println();
				System.out.println();
				System.out.println();
			}

			if ((functionSelection & 8) == 8) {
				model.setSeed(seed); // 1452345243 -> pretty good
				System.out.println("Schaffers Evaluation");
				model.setEvaluation(new SchaffersEvaluation());
				model.runName = runName;
				model.runData();
				System.out.println();
				System.out.println();
				System.out.println();
				System.out.println();
			}
			Data.check_file(runName+".csv");
		}
	}
}
