import java.util.ArrayList;

import org.vu.contest.ContestEvaluation;

public class Evaluator {





	public static void main(String[] args) {
		player64 model = new player64();


		simpleTest(model, 4, 324798L); //93476678L
	}

	/**
	 * Example method that runs the model on different functions.
	 * @param model: player64 instance containing the model
	 * @param functionSelection: binary selector for the functions to evaluate (e.g. 4 -> 0100 -> only the 3rd function)
	 * @param seed: seed for the random number generator in the model
	 */
	private static void simpleTest(player64 model, int functionSelection, long seed) {
		Data data;

		if ((functionSelection & 1) == 1) {
			model.setSeed(seed);
			System.out.println("Sphere Evaluation");
			model.setEvaluation(new SphereEvaluation());
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
			model.runData();
			System.out.println();
			System.out.println();
			System.out.println();
			System.out.println();
		}
	}

}
