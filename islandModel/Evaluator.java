public class Evaluator {
	
	public static void main(String[] args) {
		player64 model = new player64();
		

		simpleTest(model, 4, 2142324L); //93476678L
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
			data = model.runData();
			System.out.println();
			System.out.println(data.toString());
			System.out.println();
			System.out.println();
			System.out.println();
		}
		
		if ((functionSelection & 2) == 2) {
			model.setSeed(seed); 
			System.out.println("Bent-Cigar Function");
			model.setEvaluation(new BentCigarFunction());
			data = model.runData();
			System.out.println();
			System.out.println(data.toString());
			System.out.println();
			System.out.println();
			System.out.println();
		}
		
		if ((functionSelection & 4) == 4) {
			model.setSeed(seed); 
			System.out.println("Katsuura Evaluation");
			model.setEvaluation(new KatsuuraEvaluation());
			data = model.runData();
			System.out.println();
			System.out.println(data.toString());
			System.out.println(data.history.getIsland(2, 6, 10).population[2].toString());
			System.out.println();
			System.out.println();
			System.out.println();
		}
		
		if ((functionSelection & 8) == 8) {
			model.setSeed(seed); // 1452345243 -> pretty good
			System.out.println("Schaffers Evaluation");
			model.setEvaluation(new SchaffersEvaluation());
			data = model.runData();
			System.out.println();
			System.out.println(data.toString());
			System.out.println();
			System.out.println();
			System.out.println();
		}
	}

}
