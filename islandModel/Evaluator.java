import java.util.ArrayList;

import org.vu.contest.ContestEvaluation;

public class Evaluator {





	public static void main(String[] args) {
		player64 model = new player64();


		simpleTest(model, 8, 324798L); //93476678L
		//xRunsTest(model, 2, 324798L, 2000000);
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
