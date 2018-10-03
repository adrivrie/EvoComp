public class Tester {

	public static void main(String[] args) {
		// TODO: define a run function that returns a results object 
		// 			-> plot best fitness, diversity, ...
		
		player64 player = new player64();
		long seed = 2345315L; //2936574329L -> pretty good on Katsuura; 3543L pretty bad
		
		int testFunctions = 15;
		
		if ((testFunctions & 1) == 1) {
			player.setSeed(seed);
			System.out.println("Sphere Evaluation");
			player.setEvaluation(new SphereEvaluation());
			player.run();
			System.out.println();
			System.out.println();
		}
		
		if ((testFunctions & 2) == 2) {
			player.setSeed(seed); 
			System.out.println("Bent-Cigar Function");
			player.setEvaluation(new BentCigarFunction());
			player.run();
			System.out.println();
			System.out.println();
		}
		
		if ((testFunctions & 4) == 4) {
			player.setSeed(seed); 
			System.out.println("Katsuura Evaluation");
			player.setEvaluation(new KatsuuraEvaluation());
			player.run();
			System.out.println();
			System.out.println();
		}
		
		if ((testFunctions & 8) == 8) {
			player.setSeed(seed); // 1452345243 -> pretty good
			System.out.println("Schaffers Evaluation");
			player.setEvaluation(new SchaffersEvaluation());
			player.run();
			System.out.println();
			System.out.println();
		}
	}

}
