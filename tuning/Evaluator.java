import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import org.vu.contest.ContestEvaluation;

public class Evaluator {

	public static void main(String[] args) {
		paramSearch("silvan1.csv");
		
		
		//player64 model = new player64();
		//simpleTest(model, 8, 324798L); //93476678L
		//xRunsTest(model, 2, 324798L, 2000000);
	}
	
	private static void paramSearch() {
		SimpleDateFormat format = new SimpleDateFormat("dd_hh_mm_ss'.csv'");
		String filename = format.format(new Date());
		paramSearch(filename);
	}
	
	private static void paramSearch(String filename) {
		// search meta-parameters
		long maxEvals = 1000000;
		int nSeeds = 30;
		
		// create file and write column headers if still empty
		try{
			Path filePath = Data.check_file(filename);
			File file = new File(filePath.toString());

			// add headers
			boolean isEmpty = false;
			BufferedReader br = new BufferedReader(new FileReader(file));     
			isEmpty = (br.readLine() == null);
			br.close();
			if (isEmpty) {
				FileWriter writer = new FileWriter(file, true);
				writer.write(String.format("%s,%s,%s,%s,%s,%s,%s,%s\n",
						"islandAmount", "islandSize", "migrationSize", "crossoverRate", 
						"seed", "epochAmount", "fitnessMax", "genotypeBest"));
				writer.close();
			}
		} catch (IOException e){
			e.printStackTrace();
		}
		double bestFound = Double.NEGATIVE_INFINITY;
		
		
		Data data;

		while(true) {
			player64 model = new player64();
			// select params
			Random r = new Random();
			int islandAmount = (int)randomFromLogScale(1, 500);
			int islandSize = (int)randomFromLogScale(10, 2000);
			double migrationSize = randomFromLogScale(.01, .2);
			double crossoverRate = r.nextDouble();
			
			System.out.println("Run with\n\tislandAmount="+islandAmount+"\n\tislandSize="+islandSize+
					"\n\tmigrationSize="+migrationSize+"\n\tcrossoverRate="+crossoverRate);
			
			Long[] seeds = new Long[nSeeds];
			Double[] fitnesses = new Double[nSeeds];
			Double[][] bestGenotypes = new Double[nSeeds][10];
			Integer[] epochs = new Integer[nSeeds];
			for (int i=0; i<nSeeds; i++) {
				// set seed
				long seed = r.nextLong();
				// evaluate model
				model.setSeed(seed);
				model.setEvaluation(new EvaluateFitness(EvaluateFitness.FUNCTION_KATSUURA, maxEvals));
				model.setExperiment(islandAmount, islandSize, migrationSize, crossoverRate);
				data = model.runData();
				// get data
				seeds[i] = seed;
				fitnesses[i] = data.fitness;
				for(int j=0; j<10; j++) {
					bestGenotypes[i][j] = data.bestGenotype[j];
				}
				epochs[i] = data.epochs;
			}
			
			//print results
			for (double fitness : fitnesses) {
				if (fitness > bestFound) {bestFound = fitness;}
			}
			System.out.println("Results:\n\tFitnesses: "+arrayToString(fitnesses) +
					"\n\t\tBest fitness in search: "+bestFound+
					"\n\tNumber of epochs: "+arrayToString(epochs) +
					"\n\tBest genotypes: "+arrayArrayToString(bestGenotypes)+"\n");
			
			// write to csv
			try{
				Path filePath = Data.check_file(filename);
				File file = new File(filePath.toString());
				FileWriter writer = new FileWriter(file, true);

				// write all to file
				writer.write(String.format("%d,%d,%d,%f,%s,%s,%s,%s\n",
						islandAmount, islandSize, (int)(migrationSize*islandSize), crossoverRate,
						arrayToString(seeds), arrayToString(epochs), arrayToString(fitnesses), arrayArrayToString(bestGenotypes)));
				writer.close();
			} catch (IOException e){
				e.printStackTrace();
			}
		}
		
	}
	
	private static double randomFromLogScale(double min, double max) {
		Random r = new Random();
		double minPow = Math.log10(min);
		double maxPow = Math.log10(max);
		return Math.pow(10, minPow + r.nextDouble()*(maxPow - minPow));
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
	
	private static String arrayArrayToString(Object[][] arr) {
		String str = "";
		boolean isFirst = true;
		for(Object[] el : arr) {
			if (isFirst) {
				isFirst = false;
			}else {
				str += "|";
			}
			str += arrayToString(el);
		}
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
