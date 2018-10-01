import org.vu.contest.ContestSubmission;

import org.vu.contest.ContestEvaluation;

import java.util.Random;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Properties;

public class player64 implements ContestSubmission
{
	Random rnd_;
	ContestEvaluation evaluation_;
    private int evaluations_limit_;
    
    private long seed;
    
    private boolean withMutationStepDecay;
    private boolean withSelfAdaptation;
    private double initStepSizeAverage;
	private double initMaxDeviation;
	private double initStepSizeStd;
	private double stepSizeBoundary;
	private double stepSizeGeneralLearningRate;
	private double stepSizeSpecificLearningRate;
	private double initStepSize;
    boolean withElitism;
	
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
		int evals = 0; //TODO: make member of object and check at each evaluation for more robustness
        double bestFitness = 0;
        Chromosome bestChromosome = new Chromosome();
        
        // return object that keeps data
        Data data = new Data();
		
        // init population
		Chromosome[] population; // -> array of Chromosome objects
		double[] populationFitness;
		if (!withSelfAdaptation) {
			population = initialiseRandom();
		} else {
			population = initialiseRandom(initStepSizeAverage, initStepSizeAverage - initMaxDeviation, 
					initStepSizeAverage + initMaxDeviation, initStepSizeStd);
		}
		
        // calculate fitness
		populationFitness = evaluateFitness(population);
		bestFitness = findBestFitness(population, bestChromosome); // assigns best chromosome to bestChromosome
		evals += 100;
		
		//WRITE TO DATA//////////////////////////////////////////////////////
		data.bestFitness.add(bestFitness);
		/////////////////////////////////////////////////////////////////////
		
		// status printing
        int printFreq = ((evaluations_limit_ - 100) / 400) / 5;
		int nGenerations = 0;
		System.out.println("Best fitness:");
		System.out.print("\tafter initialisation:\t\t\t\t");
		System.out.println(bestFitness == Double.MIN_VALUE ? "very small" : 
			String.format("%6.3e", bestFitness));
		System.out.println(bestFitness == Double.MIN_VALUE ? "very small" : 
			bestFitness);
		
        while(evals + 400 < evaluations_limit_){
            // Select parents (uniform random)
        	Tuple[] matingPool = selectParentsUniformRandom(population);
        	
            // Apply crossover operators
        	Chromosome[] offspring = recombineDiscrete(matingPool);
        	
        	// Apply mutation operators
        	if (!withSelfAdaptation) {
        		// linear decay
        		//double mutationStepSize = initStepSize / evaluations_limit_ + (initStepSize - initStepSize/evaluations_limit_/100)*(evaluations_limit_ - evals)/evaluations_limit_; 
        		double mutationStepSize = initStepSize / (withMutationStepDecay ? nGenerations : 1); // i.e. sigma 
        		offspring = mutateNonUniformGaussian(offspring, mutationStepSize);
        	} else {
        		offspring = mutateSelfAdaptation(offspring, stepSizeBoundary, stepSizeGeneralLearningRate, stepSizeSpecificLearningRate);
        	}
        	
            // Check fitness of unknown function
        	double[] offspringFitness = evaluateFitness(offspring);
        	bestFitness = findBestFitness(offspring, bestChromosome);
            evals += 400;
            nGenerations++;
            
        	// Select survivors
            Tuple populationWithFitness = selectSurvivorsMuCommaLambda(
            		offspring, offspringFitness, population, populationFitness, withElitism);
            population = (Chromosome[]) populationWithFitness.value1;
            populationFitness = (double[]) populationWithFitness.value2;
            
            // print status
			if (((evals - 100) / 400) % printFreq == 0) {
				System.out.print("\tafter "+evals+" evaluations / "+
					nGenerations+" generations:  \t");
        		System.out.println(bestFitness == Double.MIN_VALUE ? "very small" : 
        			bestFitness);
            }
			
			//WRITE TO DATA//////////////////////////////////////////////////////
			data.bestFitness.add(bestFitness);
			/////////////////////////////////////////////////////////////////////
        }
        
        //print final status
        System.out.println(
        		"Total evaluations: "+evals+" ("+nGenerations+
        		" generations). Best individual:");
        System.out.println(bestChromosome.toString());
        
        return data;
	}
	
	
	// initialise a random population of 100 individuals
	// TODO: find convention on mutationStepSize initialisation
	private Chromosome[] initialiseRandom(){
		Chromosome[] pop = new Chromosome[100];
		
		for (int n = 0; n<100; n++) {
			pop[n] = new Chromosome();
			for (int i=0; i<10; i++) {
				pop[n].object[i] = rnd_.nextDouble()*10-5;
			}
		}
		
		return pop;
	}
	
	// initialise a random population of 100 individuals
		// TODO: find convention on mutationStepSize initialisation
		private Chromosome[] initialiseRandom(double stepSizeAverage, double stepSizeMin, 
				double stepSizeMax, double stepSizeStd){
			Chromosome[] pop = new Chromosome[100];
			
			for (int n = 0; n<100; n++) {
				pop[n] = new Chromosome();
				for (int i=0; i<10; i++) {
					pop[n].object[i] = rnd_.nextDouble()*10-5;
					if (withSelfAdaptation) {
						double deviation = rnd_.nextGaussian()*stepSizeStd;
						pop[n].mutationStepSizes[i] = Math.max(stepSizeMin, Math.min(stepSizeMax, 
								stepSizeAverage + deviation));
					}
				}
			}
			
			return pop;
		}
	
	// evaluate the fitness of each individual in a population
	private double[] evaluateFitness(Chromosome[] population) {
		double fitnesses[] = new double[population.length];
		int i = 0;
		
		for (Chromosome individual : population) {
			double fitness = (double)evaluation_.evaluate(individual.object);
			fitnesses[i++] = fitness;
			individual.fitness = fitness;
		}
		return fitnesses;
	}
	
	// selects pairs of individuals using a uniform distribution
	// 100 parents are used to form 200 pairs
	private Tuple[] selectParentsUniformRandom(Chromosome[] parents) {
		Tuple[] matingPool = new Tuple[200];
		for (int i=0; i<200; i++) {
			Chromosome parent1, parent2;
			int index = rnd_.nextInt(100);
			parent1 = parents[index];
			index = rnd_.nextInt(100);
			parent2 = parents[index];
			matingPool[i] = new Tuple(parent1, parent2);
		}
		return matingPool;
	}
	
	// apply discrete recombination (selecting each gene from either parent with equal probability)
	private Chromosome[] recombineDiscrete(Tuple[] matingPool){
		Chromosome[] offspring = new Chromosome[400];
		
		int offspringIndex = 0;
		for (Tuple couple : matingPool) {
			Chromosome child1 = new Chromosome();
			Chromosome child2 = new Chromosome();
			Chromosome parent1 = (Chromosome) couple.v1();
			Chromosome parent2 = (Chromosome) couple.v2();
			
			// randomly assign genes from parents to children
			for (int index=0; index<10; index++) {
				double gene1 = parent1.object[index];
				double gene2 = parent2.object[index];
				if (rnd_.nextBoolean()) {
					child1.object[index] = gene1;
					child2.object[index] = gene2;
				} else {
					child1.object[index] = gene2;
					child2.object[index] = gene1;
				}
			}
			
			// randomly assign mutation step sizes from parents to children
			for (int index=0; index<10; index++) {
				double size1 = parent1.mutationStepSizes[index];
				double size2 = parent2.mutationStepSizes[index];
				if (rnd_.nextBoolean()) {
					child1.mutationStepSizes[index] = size1;
					child2.mutationStepSizes[index] = size2;
				} else {
					child1.mutationStepSizes[index] = size2;
					child2.mutationStepSizes[index] = size1;
				}
			}
			
			// add children to list of offspring
			offspring[offspringIndex++] = child1;
			offspring[offspringIndex++] = child2;
		}
		return offspring;
	}
	
	// apply non-uniform mutation to every gene of every genotype using a Gaussian distribution
	private Chromosome[] mutateNonUniformGaussian(Chromosome[] population, double stepSize){
		int nGenes = population[0].object.length;
		
		// loop over every gene of every individual and add some deviation, 
		// curtailing the result to the problem domain
		for (Chromosome individual : population) {
			for (int i=0; i<nGenes; i++) {
				double deviation = rnd_.nextGaussian()*stepSize;
				individual.object[i] = Math.max(-5, Math.min(5, individual.object[i] + deviation));
			}
		}
		
		return population;
	}
	
	// apply uncorrelated self-adaptive mutation to each chromosome in the population
	private Chromosome[] mutateSelfAdaptation(Chromosome[] population, double stepBoundary, 
			double generalLearningRate, double specificLearningRate) {
		int nGenes = population[0].object.length;
		
		// loop over every gene of every individual, adapt mutation step size, then the object,
		// curtailing the result to the problem domain
		for (Chromosome individual : population) {
			double rndGeneral = rnd_.nextGaussian();
			double rndSpecific;
			for (int i=0; i<nGenes; i++) {
				// adapt mutation step size 
				rndSpecific = rnd_.nextGaussian();
				double factor = Math.exp(generalLearningRate * rndGeneral +
						specificLearningRate * rndSpecific);
				individual.mutationStepSizes[i] = Math.max(stepBoundary, 
						factor * individual.mutationStepSizes[i]);
				// adapt object
				rndSpecific = rnd_.nextGaussian();
				double deviation = individual.mutationStepSizes[i] * rndSpecific; 
				individual.object[i] = Math.max(-5, Math.min(5, individual.object[i] + deviation));
			}
		}

		return population;
	}
	
	// apply (mu, lambda) survivor selection, replacing the population by the best-ranked offspring
	// apply elitism as well: if a member of the old population has the best fitness, he is kept in the population
	private Tuple selectSurvivorsMuCommaLambda(Chromosome[] offspring, 
			double[] offspringFitness, Chromosome[] population, double[] populationFitness,
			boolean withElitism){
		
		int nOffspring = offspring.length;
		int nIndividuals = population.length;
		
		Chromosome[] populationNew = new Chromosome[nIndividuals];
		double[] populationFitnessNew = new double[nIndividuals];
		
		// rank the offspring and parents
		int[] offspringRank = arraySortIndicesDouble(offspringFitness);
		int[] populationRank = arraySortIndicesDouble(populationFitness);
		
		int index = 0;
		int offspringIndex = nOffspring-1;
		int populationRankLast = populationRank[nIndividuals-1];
		
		// apply elitism explicitly if a parent is the fittest individual
		if (withElitism) {
			if (populationFitness[populationRankLast] > offspringFitness[offspringRank[offspringIndex]]) {
				populationNew[index] = population[populationRankLast];
				populationFitnessNew[index++] = populationFitness[populationRankLast];
			}
		}
		
		// fill the remainder of the new population with the fittest offspring
		while (index < nIndividuals) {
			populationNew[index] = offspring[offspringRank[offspringIndex]];
			populationFitnessNew[index++] = offspringFitness[offspringRank[offspringIndex--]];
		}
		
		return new Tuple(populationNew, populationFitnessNew);
	}
	
	// determine the best fitness found so far, and assign the chromosome that has it to bestChromosome
	private double findBestFitness(Chromosome[] population, Chromosome bestChromosome) {
		double bestFitness = bestChromosome.fitness;
		
		int indexBest = -1;
		double currentBest = bestChromosome.fitness;
		
		for (int i = 0; i<population.length; i++) {
			if (population[i].fitness > currentBest) {
				indexBest = i;
				currentBest = population[i].fitness;
			}
		}
		
		if (indexBest > -1) {
			 population[indexBest].copy(bestChromosome);
		}
		
		return currentBest;
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
	
	private class Chromosome
	{
		public double[] object;
		public double[] mutationStepSizes;
		// angles
		public double fitness;
		
		public Chromosome(){
			object = new double[10];
			mutationStepSizes = new double[10];
			fitness = Double.MIN_VALUE; // TODO: wrong value (this is close to 0) -> Double.NEGATIVE_INFINITY
		}
		
		// copies the fields of this object to another
		public void copy(Chromosome c) {
			c.object = new double[object.length];
			System.arraycopy(object, 0, c.object, 0, object.length);
			
			c.mutationStepSizes = new double[mutationStepSizes.length];
			System.arraycopy(mutationStepSizes, 0, c.mutationStepSizes, 0, mutationStepSizes.length);
			
			c.fitness = fitness;
		}
		
		@Override
		public String toString() {
			String out = "Chromosome:\n-fitness: " + fitness;
			
			out += "\n-object: ";
			for (double o : object) {
				out += String.format("%6.3e", o) + "|";
			}
			
			out += "\n-mutationStepSizes: ";
			for (double m : mutationStepSizes) {
				out += String.format("%6.3e", m) + "|";
			}
			
			return out;
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
	
	// TODO: make more general and cleaner
	private int[] arraySortIndicesDouble(double[] array) {
		Double[] a = new Double[array.length];
		for (int i=0; i<array.length; i++) {
			a[i] = array[i];
		}
		return IntStream.range(0, a.length)
                .boxed().sorted((i, j) -> a[i].compareTo(a[j]) )
                .mapToInt(ele -> ele).toArray();
	}
	
	// TODO: doesn't work yet
	private String format3decimals(double d) {
		int intPart = (int)d;
		int decimal1 = (int)Math.abs((d - intPart) * 10);
		int decimal2 = (int)Math.abs((d - intPart - decimal1 / 10)*100);
		int decimal3 = (int)Math.abs((d - intPart - decimal1 / 10 - decimal2 / 100)*1000);
		
		return intPart + "." + decimal1 + decimal2 + decimal3;
	}
}
