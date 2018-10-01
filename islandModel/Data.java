import java.util.ArrayList;

/**
 *
 * This class contains all information that Evaluator needs of one run of player64.
 *
 */
public class Data {

	// at index i, gives the best fitness of the population after generation i
	public ArrayList<Double> bestFitness = new ArrayList<Double>();
	/** NEW FIELD
	// at index i, gives the variance of the population after generation i
	public ArrayList<Double> populationVariance = new ArrayList<Double>();
	*/
	@Override
	public String toString() {
		String str = "";
		str += "Best fitness per generation:\n";
		for (double fitness : bestFitness) {
			str += String.format("\t%.3e", fitness);
		}

		return str;
	}

}
