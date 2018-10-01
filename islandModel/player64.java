import org.vu.contest.ContestSubmission;

import org.vu.contest.ContestEvaluation;

import java.util.Random;
import java.util.stream.IntStream;
import java.util.Properties;

public class player64 implements ContestSubmission
{
	Random rnd_;
	ContestEvaluation evaluation_;
    private int evaluations_limit_;
    private int evals;
    
    private long seed;
    
    public boolean withMutationStepDecay;
    public boolean withSelfAdaptation;
    public double initStepSizeAverage;
    public double initMaxDeviation;
	public double initStepSizeStd;
	public double stepSizeBoundary;
	public double stepSizeGeneralLearningRate;
	public double stepSizeSpecificLearningRate;
	public double initStepSize;
    public boolean withElitism;
    public int initialPopulationSize;
    public double offspringRatio;
	
	public player64()
	{
		rnd_ = new Random();
	}
	
	public static void main(String[] a) {
		System.out.println("starting...");
	}
	
	public void setSeed(long seed)
	{
		// Set seed of algorithms random process
		rnd_.setSeed(seed);
		this.seed = seed;
	}

	public void setEvaluation(ContestEvaluation evaluation)
	{
		// Set evaluation problem used in the run
		evaluation_ = evaluation;
		
		// Get evaluation properties
		Properties props = evaluation.getProperties();
        evaluations_limit_ = Integer.parseInt(props.getProperty("Evaluations"));
        boolean isMultimodal = Boolean.parseBoolean(props.getProperty("Multimodal"));
        boolean hasStructure = Boolean.parseBoolean(props.getProperty("Regular"));
        boolean isSeparable = Boolean.parseBoolean(props.getProperty("Separable"));
        boolean isKatsuura = isMultimodal && !hasStructure && !isSeparable;

		// Determine algorithm parameters
        // mutation step size
        withSelfAdaptation = !isKatsuura; // apply self-adaptation of mutation step size (n)
        if (withSelfAdaptation) { // initialisation of mutation step size
			initStepSizeAverage = 0.01;
			initMaxDeviation = 0.005;
			initStepSizeStd = 0.003;
    		stepSizeBoundary = 1e-9; // minimum mutation step size (1.0/20000)
    		stepSizeGeneralLearningRate = 1 / Math.sqrt(2 * 100);
    		stepSizeSpecificLearningRate = 1 / Math.sqrt(2 * Math.sqrt(100));
        } else {
        	initStepSize = 0.1;
        	withMutationStepDecay = isKatsuura;
        }
        // parent selection
        withElitism = !isMultimodal; // seems to have a bad influence on the multimodal functions
        // initial population size of islands
        initialPopulationSize = 100;
        offspringRatio = 4; // 4 times as much offspring as population
        
        System.out.println("The evaluated function is " + 
        		(isMultimodal ? "" : "not ") + "multimodal, " + 
        		(hasStructure ? "" : "not ") + "regular and " +
        		(isSeparable ? "" : "not ") + "separable. " + 
        		"The algorithm will perform " + 
        		evaluations_limit_ + 
        		" evaluations with seed " +
        		seed);
    }
    
	public void run() {}
	public Data runData()
	{
		evals = 0;
		int nGenerations = 0;
        
        // return object that keeps data
        Data data = new Data();
		
        // INITIALISATION AND FITNESS EVALUATION
		Island island = new Island(this);
		island.initialise();
        
		//WRITE TO DATA//////////////////////////////////////////////////////
		data.bestFitness.add(island.bestFitness);
		/////////////////////////////////////////////////////////////////////
		
		// status printing
        int printFreq = ((evaluations_limit_ - initialPopulationSize) / initialPopulationSize/(int)offspringRatio) / 5;
		System.out.println("Best fitness:\n\tafter initialisation:\t\t\t\t" +
			(island.bestFitness == Double.NEGATIVE_INFINITY ? "very small" : 
					String.format("%6.3e", island.bestFitness)));
		
        while(evals < evaluations_limit_){
            // RUN EVOLUTION CYCLE ON ISLANDS
        	island.runCycle();
        	nGenerations++;
        	
            // print status
			if (nGenerations % printFreq == 0) {
				System.out.println(String.format(
    				"\tafter %d evaluations / %d generations:   \t", evals, nGenerations) +
    				(island.bestFitness == Double.NEGATIVE_INFINITY ? "very small" : 
					String.format("%6.3e", island.bestFitness)));
            }
			
			//WRITE TO DATA//////////////////////////////////////////////////////
			data.bestFitness.add(island.bestFitness);
			/////////////////////////////////////////////////////////////////////
        }
        
        //print final status
        System.out.println(
        		"Total evaluations: "+evals+" ("+nGenerations+
        		" generations). Best individual:");
        System.out.println(island.bestChromosome.toString());
        
        return data;
	}
	
	
	
	
	// evaluate the fitness of each individual in an array
	// (if the eval. limit is exceeded, only the evaluated fitnesses are returned)
	public double[] evaluateArray(Chromosome[] population) {
		double fitnesses[] = new double[population.length];
		int i = 0;
		
		for (Chromosome individual : population) {
			if (evals < evaluations_limit_) {
				evals++;
				double fitness = (double)evaluation_.evaluate(individual.object);
				fitnesses[i++] = fitness;
				individual.fitness = fitness;
			} else {
				System.out.println("Evaluations limit reached!");
				break;
			}
		}
		return fitnesses;
	}
	// evaluate fitness of one individual (return null if eval. limit is exceeded)
	public Double evaluate(Chromosome individual){
		if (evals >= evaluations_limit_) {
			return null;
		} else {
			evals++;
			double fitness = (double)evaluation_.evaluate(individual.object);
			individual.fitness = fitness;
			return fitness;
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	// Helper classes and methods
	
	private class Tuple
	{
		private Object value1, value2;
		
		public Tuple(Object value1, Object value2) {
			this.value1 = value1;
			this.value2 = value2;
		}
		
		public Object v1() {
			return value1;
		}
		
		public Object v2() {
			return value2;
		}
	}
	
	private double arrayMax(double[] a) {
		double mx = Double.MIN_VALUE; //TODO: wrong value, this is close to 0
		for (double e : a) {
			if (e > mx) {
				mx = e;
			}
		}
		return mx;
	}
	
	
	
}
