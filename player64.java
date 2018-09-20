import org.vu.contest.ContestSubmission;
import org.vu.contest.ContestEvaluation;

import java.util.Random;
import java.util.Properties;

public class player64 implements ContestSubmission
{
	Random rnd_;
	ContestEvaluation evaluation_;
    private int evaluations_limit_;
	
	public player64()
	{
		rnd_ = new Random();
	}
	
	public void setSeed(long seed)
	{
		// Set seed of algortihms random process
		rnd_.setSeed(seed);
	}

	public void setEvaluation(ContestEvaluation evaluation)
	{
		// Set evaluation problem used in the run
		evaluation_ = evaluation;
		
		// Get evaluation properties
		Properties props = evaluation.getProperties();
        // Get evaluation limit
        evaluations_limit_ = Integer.parseInt(props.getProperty("Evaluations"));
		// Property keys depend on specific evaluation
		// E.g. double param = Double.parseDouble(props.getProperty("property_name"));
        boolean isMultimodal = Boolean.parseBoolean(props.getProperty("Multimodal"));
        boolean hasStructure = Boolean.parseBoolean(props.getProperty("Regular"));
        boolean isSeparable = Boolean.parseBoolean(props.getProperty("Separable"));

		// Do sth with property values, e.g. specify relevant settings of your algorithm
        if(isMultimodal){
            // Do sth
        }else{
            // Do sth else
        }
    }
    
	public void run()
	{
		System.out.println(evaluations_limit_);
		// Run your algorithm here
        
        int evals = 0;
        double[] best_child = {0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0};
        double best_fitness = (double) evaluation_.evaluate(best_child);
        evals++;
        // calculate fitness
        while(evals<evaluations_limit_){
            // Select parents
            // Apply crossover / mutation operators

            int evals_left = evaluations_limit_ - evals;
            double stddev = 1.0 * evals_left / evaluations_limit_;
            double[] child = mutate(best_child, stddev);
            // Check fitness of unknown fuction
            Double fitness = (double) evaluation_.evaluate(child);
            evals++;
            // Select survivors
            if (fitness > best_fitness) {
            	best_child = child;
            }

        }

	}

	private double[] mutate(double[] child, double stddev)
	{
		double[] new_child = new double[10];
		for(int i = 0; i < 10; i++){
			new_child[i] = Math.min(5, Math.max(-5, child[i] + stddev * rnd_.nextGaussian()));
		}
		return new_child;
	}
}
