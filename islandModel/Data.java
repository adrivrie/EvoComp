import java.util.ArrayList;

/**
 *
 * This class contains all information that Evaluator needs of one run of player64.
 *
 */
public class Data {

	/** NEw FIELD
	# Argumentation for this over the seperate fields:
	# PRO: With this batch we can calculate/find all measures we might want that
	# make use of fitness and generation/population level.
	# CON: It is a larger bulk of data that has to be passed over...
	// at index i, gives the fitness of all m individuals in the population after generation i
	public ArrayList<ArrayLost<Double>> populationFitness = new ArrayList<ArrayList<Double>>();
	*/
	// at index i, gives the best fitness of the population after generation i
	public ArrayList<Double> bestFitness = new ArrayList<Double>();
	/** NEW FIELD (in case we decide not to implement the populationFitness)
	// at index i, gives the variance of fitness for the population after generation i
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
