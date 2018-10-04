import java.util.Random;
import java.util.stream.IntStream;
import java.util.Arrays;
public class Island implements Cloneable
{
	// fields relating to the model
	private player64 model; // model that this island is a part of
	private Random rnd_; // islands have their own random number generator
	public int nGenerations; // generation number of current run
	
	// fields about the population
	public Chromosome[] population; // array of chromosomes
	public double[] populationFitnesses; // fitness per chromosome in population
	public double bestFitness; // best fitness of the current population
	public Chromosome bestChromosome = new Chromosome(); // chromosome with highest fitness in the population
	public int populationSize;
	
	// variables needed for migration
	public int convergenceThreshold;
	public int gensSinceImprovement;
	public boolean hasConverged;
	
	public Island(player64 model) {
		this.model = model;
		this.rnd_ = new Random(model.rnd_.nextLong()); // seed should differ for different model seeds
	}
	
	//// PUBLIC METHODS /////////////////////////////////////////////////////////////////////////////////
	
	// initialise the island population
	public void initialise() {
		// SET ALGORITHM PARAMETERS AND MEASURES
		populationSize = model.initialPopulationSize;
		nGenerations = 0;
		
		// INITIALISE POPULATION
		initialiseRandom();
		
        // FITNESS EVALUATION
		populationFitnesses = model.evaluateArray(population);
		findBestFitness(population);

		// INITIALIZE CONVERGENCE
		gensSinceImprovement = 0;
		hasConverged = false;
		convergenceThreshold = model.convergenceThreshold;
	}
	
	// generate one new generation
	public void runCycle() {

		// CONVERGENCE CHECK
		if (hasConverged){
			return;
		}

		double prevBestFitness = bestFitness;

		// ADAPT ALGORITHM MEASURES
		nGenerations++;
		
		// PARENT SELECTION
		// adapt population size
		Tuple[] matingPool = selectParentsUniformRandom();
    	
        // RECOMBINATION
    	Chromosome[] offspring = recombineDiscrete(matingPool);
    	
    	// MUTATION
    	if (!model.withSelfAdaptation) {
    		// linear decay
    		offspring = mutateNonUniformGaussian(offspring);
    	} else {
    		// self-adaptation
    		offspring = mutateSelfAdaptation(offspring);
    	}
    	
        // FITNESS EVALUATION
    	double[] offspringFitnesses = model.evaluateArray(offspring);
    	findBestFitness(offspring);
        
    	// SURVIVOR SELECTION
        selectSurvivorsMuCommaLambda(offspring, offspringFitnesses);

        // check if fitness has improved
        if (bestFitness > prevBestFitness){
        	gensSinceImprovement = 0;
        } else {
        	gensSinceImprovement++;
        	if (gensSinceImprovement >= convergenceThreshold){
        		hasConverged = true;
        	}
        }
	}
	
	
	
	//// PRIVATE EA METHODS /////////////////////////////////////////////////////////////////////////////
	
	// initialise a random population with the object from a uniform distribution and
	// the mutation step sizes (if applicable) from a normal distribution
	// TODO: find convention on mutationStepSize initialisation
	private void initialiseRandom(){
		population = new Chromosome[populationSize];
		
		for (int n = 0; n<populationSize; n++) {
			population[n] = new Chromosome();
			for (int i=0; i<10; i++) {
				population[n].object[i] = rnd_.nextDouble()*10-5;
				if (model.withSelfAdaptation) {
					double deviation = rnd_.nextGaussian()*model.initStepSizeStd;
					double stepSizeMin = model.initStepSizeAverage - model.initMaxDeviation;
					double stepSizeMax = model.initStepSizeAverage + model.initMaxDeviation;
					population[n].mutationStepSizes[i] = Math.max(stepSizeMin, 
							Math.min(stepSizeMax, model.initStepSizeAverage + deviation));
				}
			}
		}
	}
	
	// compare the best fitness of the chromosomes in the array with the best fitness found so far.
	// if the array contains a fitter individual, returns his fitness and sets bestChromosome to this individual
	private void findBestFitness(Chromosome[] population) {
		double currentBest;
		if (bestChromosome != null) {
			currentBest = bestChromosome.fitness;
		} else {
			currentBest = Double.NEGATIVE_INFINITY;
		}
		int indexBest = -1;
		
		for (int i = 0; i<population.length; i++) {
			if (population[i].fitness > currentBest) {
				indexBest = i;
				currentBest = population[i].fitness;
			}
		}
		
		if (indexBest > -1) {
			 population[indexBest].copy(bestChromosome);
		}
		bestFitness = currentBest;
	}
	
	// selects pairs of individuals using a uniform distribution
	// n parents are used to form (ratio)*n/2 pairs
	private Tuple[] selectParentsUniformRandom() {
		int nPairs = (int)(populationSize*model.offspringRatio/2);
		Tuple[] matingPool = new Tuple[nPairs];
		for (int i=0; i<nPairs; i++) {
			Chromosome parent1, parent2;
			int index = rnd_.nextInt(populationSize);
			parent1 = population[index];
			index = rnd_.nextInt(populationSize);
			parent2 = population[index];
			matingPool[i] = new Tuple(parent1, parent2);
		}
		return matingPool;
	}
	
	// apply discrete recombination (selecting each gene from either parent with equal probability)
	private Chromosome[] recombineDiscrete(Tuple[] matingPool){
		Chromosome[] offspring = new Chromosome[2*matingPool.length];
		
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
			// TODO: try artihmetic/intermediate recombination (book: "recombination, usually intermediate, of strategy parameters")
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
	// TODO: can we adjust the population parameter directly, instead of returning a new object?
	private Chromosome[] mutateNonUniformGaussian(Chromosome[] population){
		int nGenes = population[0].object.length;
		double stepSize = model.initStepSize / (model.withMutationStepDecay ? nGenerations : 1); // i.e. sigma 
		
		
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
	
	// apply uncorrelated self-adaptive mutation with n step sizes to each chromosome in the population
	// TODO: can we adjust the population parameter directly, instead of returning a new object?
	private Chromosome[] mutateSelfAdaptation(Chromosome[] population) {
		int nGenes = population[0].object.length;
		
		// loop over every gene of every individual, adapt mutation step size, then the object,
		// curtailing the result to the problem domain
		for (Chromosome individual : population) {
			double rndGeneral = rnd_.nextGaussian();
			double rndSpecific;
			for (int i=0; i<nGenes; i++) {
				// adapt mutation step size 
				rndSpecific = rnd_.nextGaussian();
				double factor = Math.exp(model.stepSizeGeneralLearningRate * rndGeneral +
						model.stepSizeSpecificLearningRate * rndSpecific);
				individual.mutationStepSizes[i] = Math.max(model.stepSizeBoundary, 
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
	// TODO: elitism with multiple chromosomes
	private void selectSurvivorsMuCommaLambda(Chromosome[] offspring, double[] offspringFitness){
		int nOffspring = offspringFitness.length;
		int nIndividuals = populationFitnesses.length;
		
		Chromosome[] populationNew = new Chromosome[nIndividuals];
		double[] populationFitnessNew = new double[nIndividuals];
		
		// rank the offspring and parents
		int[] offspringRank = arraySortIndicesDouble(offspringFitness);
		int[] populationRank = arraySortIndicesDouble(populationFitnesses);
		
		int index = 0;
		int offspringIndex = nOffspring-1;
		int populationRankLast = populationRank[nIndividuals-1];
		
		// apply elitism explicitly if a parent is the fittest individual
		if (model.withElitism) {
			if (populationFitnesses[populationRankLast] > offspringFitness[offspringRank[offspringIndex]]) {
				populationNew[index] = population[populationRankLast];
				populationFitnessNew[index++] = populationFitnesses[populationRankLast];
			}
		}
		
		// fill the remainder of the new population with the fittest offspring
		while (index < nIndividuals) {
			populationNew[index] = offspring[offspringRank[offspringIndex]];
			populationFitnessNew[index++] = offspringFitness[offspringRank[offspringIndex--]];
		}
		
		population = populationNew;
		populationFitnesses = populationFitnessNew;
	}
	
	
	//// MIGRATION METHODS

	public Chromosome[] getTopHalfEmigrants(int amount){
		Chromosome[] emigrators = new Chromosome[amount];			//this
		double[] fitnessesDeepCopy = new double[populationSize];	//
		for (int i = 0; i < populationSize; i++){					//way
			fitnessesDeepCopy[i] = population[i].fitness;			//
		}															//lies
		Arrays.sort(fitnessesDeepCopy);								//
		double median = fitnessesDeepCopy[populationSize / 2];		//madness

		int emigs = 0;
		while (emigs < amount){
			int ind = rnd_.nextInt(populationSize);
			if (population[ind] == null){
				continue;
			}
			if (population[ind].fitness >= median){
				emigrators[emigs] = population[ind];
				emigs++;
				population[ind] = null;
			}
		}
		return emigrators;

	}

	public void takeInImmigrants(Chromosome[] immigrants){
		int ind = 0;
		for (int i = 0; i < populationSize; i++){
			if (population[i] == null) {
				population[i] = immigrants[ind];
				populationFitnesses[i] = immigrants[ind].fitness;
				if (bestFitness < immigrants[ind].fitness)
					bestFitness = immigrants[ind].fitness;
				ind++;
				if (ind == immigrants.length)
					break;
			}
		}
	}


	//// HELPER METHODS ////////////////////////////////////////////////////////////////////////////////
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
	
	
	
	@Override
	protected Object clone() throws CloneNotSupportedException {
	    Island cloned = (Island)super.clone();
	    
	    for (int i=0; i<cloned.population.length; i++) {
	    	cloned.population[i] = (Chromosome)cloned.population[i].clone();
	    }
	    cloned.bestChromosome = (Chromosome)cloned.bestChromosome.clone();
	    cloned.populationFitnesses = cloned.populationFitnesses.clone();

	    return cloned;
	}
	
}
