import java.util.ArrayList;

/**
 * 
 * This class contains all information that Evaluator needs of one run of player64.
 *
 */
public class Data {

	// at index i, gives the best fitness of the population after generation i
	public ArrayList<Double> bestFitness = new ArrayList<Double>();
	
	
	@Override
	public String toString() {
		String str = "";
		str += "Best fitness per generation:\n\t[";
		for (double fitness : bestFitness) {
			str += String.format("%.1e, ", fitness);
		}
		str += "]";
		
		return str;
	}
	
}