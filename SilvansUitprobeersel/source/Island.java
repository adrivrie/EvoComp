import java.util.Random;
import java.util.stream.IntStream;
import java.util.ArrayList;
import java.util.Arrays;
public class Island implements Cloneable
{
	// fields relating to the model
	private player64 model; // model that this island is a part of
	private Random rnd_; // islands have their own random number generator
	public int nGenerations; // generation number of current run

	// fields about the population
	public ArrayList<Chromosome> population; // array of chromosomes
	// DEPRECATED public double[] populationFitnesses; // fitness per chromosome in population
	public double bestFitness; // best fitness of the current population
	public double currentBestFitness;
	public double worstFitness;
	public double currentWorstFitness;
	public double avgFitness;
	public Chromosome bestChromosome = new Chromosome(); // chromosome with highest fitness in the population
	// DEPRECATED (variable) public int populationSize;
	public ArrayList<Integer> evaluationsPerGeneration = new ArrayList<Integer>();

	// variables needed for migration
	public int convergenceThreshold;
	public int gensSinceImprovement;
	public boolean hasConverged;

	public String islandName;
	public Island(player64 model) {
		this.model = model;
		this.rnd_ = new Random(model.rnd_.nextLong()); // seed should differ for different model seeds
	}

	//// PUBLIC METHODS /////////////////////////////////////////////////////////////////////////////////

	// initialise the island population
	public void initialise() {
		// SET ALGORITHM PARAMETERS AND MEASURES
		nGenerations = 0;

		// INITIALISE POPULATION
		population = new ArrayList<Chromosome>();
		initialiseRandom();

        // FITNESS EVALUATION
		int nEvals = model.evaluateArray(population);
		evaluationsPerGeneration.add(nEvals);
		findBestFitness(population);
		updateFitnessStats(population);
		assignLifetime(population);
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
    	ArrayList<Chromosome> offspring = recombineDiscrete(matingPool);
		
    	// MUTATION
    	if (!model.withSelfAdaptation) {
    		// linear decay
    		offspring = mutateNonUniformGaussian(offspring);
    	} else {
    		// self-adaptation
    		offspring = mutateSelfAdaptation(offspring);
    	}

        // FITNESS EVALUATION
    	int nEvals = model.evaluateArray(offspring);
		evaluationsPerGeneration.add(nEvals);
    	findBestFitness(offspring);

    	// SURVIVOR SELECTION
    	if(model.GAVaPS){
    		updateFitnessStats(population);
    		selectLifetime(offspring);
    	} else{
        	selectSurvivorsMuCommaLambda(offspring);
		}
    	
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
	public void initialiseRandom(){
		for (int n = 0; n<model.initialPopulationSize; n++) {
			Chromosome chromosome = new Chromosome();
			for (int i=0; i<10; i++) {
				chromosome.object[i] = rnd_.nextDouble()*10-5;
				if (model.withSelfAdaptation) {
					double deviation = rnd_.nextGaussian()*model.initStepSizeStd;
					double stepSizeMin = model.initStepSizeAverage - model.initMaxDeviation;
					double stepSizeMax = model.initStepSizeAverage + model.initMaxDeviation;
					chromosome.mutationStepSizes[i] = Math.max(stepSizeMin,
							Math.min(stepSizeMax, model.initStepSizeAverage + deviation));
				}
			}
			population.add(chromosome);
		}
	}

	// compare the best fitness of the chromosomes in the array with the best fitness found so far.
	// if the array contains a fitter individual, returns his fitness and sets bestChromosome to this individual
	private void findBestFitness(ArrayList<Chromosome> population) {
		double currentBest;
		if (bestChromosome != null) {
			currentBest = bestChromosome.fitness;
		} else {
			currentBest = Double.NEGATIVE_INFINITY;
		}
		int indexBest = -1;

		for (Chromosome chromosome : population) {
			if (chromosome.fitness > currentBest) {
				try {
					bestChromosome = (Chromosome)chromosome.clone();
				} catch (CloneNotSupportedException e) {
					e.printStackTrace();
				}
				currentBest = bestChromosome.fitness;
			}
		}

		bestFitness = currentBest;
	}

	// selects pairs of individuals using a uniform distribution
	// n parents are used to form (ratio)*n/2 pairs
	private Tuple[] selectParentsUniformRandom() {
		int nPairs = (int)(population.size()*model.offspringRatio/2);
		Tuple[] matingPool = new Tuple[nPairs];
		for (int i=0; i<nPairs; i++) {
			Chromosome parent1, parent2;
			int index = rnd_.nextInt(population.size());
			parent1 = population.get(index);
			index = rnd_.nextInt(population.size());
			parent2 = population.get(index);
			matingPool[i] = new Tuple(parent1, parent2);
		}
		
		return matingPool;
	}

	// apply discrete recombination (selecting each gene from either parent with equal probability)
	private ArrayList<Chromosome> recombineDiscrete(Tuple[] matingPool){
		ArrayList<Chromosome> offspring = new ArrayList<Chromosome>();

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
			offspring.add(child1);
			offspring.add(child2);
		}
		return offspring;
	}

	// apply non-uniform mutation to every gene of every genotype using a Gaussian distribution
	// TODO: can we adjust the population parameter directly, instead of returning a new object?
	private ArrayList<Chromosome> mutateNonUniformGaussian(ArrayList<Chromosome> population){
		int nGenes = population.get(0).object.length;
		double stepSize = model.initStepSize / (model.withMutationStepDecay ? nGenerations : 1); // i.e. sigma


		// loop over every gene of every individual and add some deviation,
		// curtailing the result to the problem domain
		for (Chromosome individual : population) {
			for (int i=0; i<nGenes; i++) {
				double deviation = rnd_.nextGaussian()*stepSize;
				individual.object[i] = Math.max(-5, Math.min(5, individual.object[i] + deviation));
			}
		}

		return population; // TODO: do we need to return, or is there a reference to population?
	}

	// apply uncorrelated self-adaptive mutation with n step sizes to each chromosome in the population
	// TODO: can we adjust the population parameter directly, instead of returning a new object?
	private ArrayList<Chromosome> mutateSelfAdaptation(ArrayList<Chromosome> population) {
		int nGenes = population.get(0).object.length;

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
	private void selectSurvivorsMuCommaLambda(ArrayList<Chromosome> offspring){
		int nOffspring = offspring.size();
		int nIndividuals = population.size();
		double[] offspringFitness = getPopulationFitnesses(offspring);
		double[] populationFitness = getPopulationFitnesses(population);

		ArrayList<Chromosome> populationNew = new ArrayList<Chromosome>();

		// rank the offspring and parents
		int[] offspringRank = arraySortIndicesDouble(offspringFitness);
		int[] populationRank = arraySortIndicesDouble(populationFitness);

		int index = 0;
		int offspringIndex = nOffspring-1;
		int populationRankLast = populationRank[nIndividuals-1];

		// apply elitism explicitly if a parent is the fittest individual
		if (model.withElitism) {
			if (populationFitness[populationRankLast] > offspringFitness[offspringRank[offspringIndex]]) {
				populationNew.add(population.get(populationRankLast));
			}
		}

		// fill the remainder of the new population with the fittest offspring
		while (populationNew.size() < nIndividuals) {
			populationNew.add(offspring.get(offspringRank[offspringIndex]));
		}

		population = populationNew;
	}

	// ages the old population, gives lifetimes to the new offspring,
    // culls old part of old population, combines into new population
    private void selectLifetime(ArrayList<Chromosome> offspring) {
        ArrayList<Chromosome> newPop = new ArrayList<Chromosome>();
        for(Chromosome individual : population) {
            individual.age += 1;
            if (individual.age <= individual.lifetime){
                newPop.add(individual);
            }
        }
        int meth = model.lifetimeAssignmentMethod;
        assignLifetime(offspring);
        
        
        
        for(Chromosome individual : offspring){
            newPop.add(individual);
        }
        population = newPop;
        updateFitnessStats(population);
        
//        System.out.print("{");
//        for (Chromosome c : population) {
//        	System.out.print(c.lifetime + " ");
//        }
//        System.out.println("}");
    }

    private void assignLifetime(ArrayList<Chromosome> offspring){
    	int meth = model.lifetimeAssignmentMethod;
        for(Chromosome individual : offspring){
            if (meth == 0){
                giveLifetimeProportional(individual);
            } else if (meth == 1) {
                giveLifetimeLinear(individual);
            } else if (meth == 2) {
                giveLifetimeBilinear(individual);
            } else {
            	int a = 1/0;
            }
         }
    }

    private void giveLifetimeProportional(Chromosome child) {
        double eta = 0.5 * (model.maxLifetime - model.minLifetime);
        double ans = model.minLifetime + child.fitness * eta / avgFitness;
        if (ans > model.maxLifetime) {
            ans = model.maxLifetime;
        }
        child.age = 0;
        child.lifetime = (int)ans;
    }

    private void giveLifetimeLinear(Chromosome child) {
        double eta = 0.5 * (model.maxLifetime - model.minLifetime);
        double ans = model.minLifetime + 2 * eta * (child.fitness - worstFitness) / (bestFitness - worstFitness);
        if (ans > model.maxLifetime) {
            ans = model.maxLifetime;
        }
        child.age = 0;
        child.lifetime = (int)ans;
    }

    private void giveLifetimeBilinear(Chromosome child) {
        double eta = 0.5 * (model.maxLifetime - model.minLifetime);
        double ans;
        if (child.fitness <= avgFitness){
            ans = model.minLifetime + eta * (child.fitness - currentWorstFitness) / (avgFitness - currentWorstFitness);
        } else {
            ans = 0.5 * (model.minLifetime + model.maxLifetime) + eta * (child.fitness - avgFitness) / (currentBestFitness - avgFitness);
        }
        if (ans > model.maxLifetime) {
            ans = model.maxLifetime;
        }
        child.age = 0;
        child.lifetime = (int)ans;
    }


    private void updateFitnessStats(ArrayList<Chromosome> population){
        double[] fits = getPopulationFitnesses(population);
        Arrays.sort(fits);
        currentWorstFitness = Math.max(0, fits[0]);
        currentBestFitness = Math.max(0, fits[fits.length-1]);
        if (currentWorstFitness < worstFitness){
            worstFitness = currentWorstFitness;
        }
        if (currentBestFitness > bestFitness){
            bestFitness = currentBestFitness;
        }
        double sumOfFits = 0.0;
        for(double d : fits){
            sumOfFits += d;
        }
        avgFitness = sumOfFits / fits.length;

    }

	//// MIGRATION METHODS

	public Chromosome[] getTopHalfEmigrants(int amount){
		Chromosome[] emigrators = new Chromosome[amount];			//this
		double[] fitnessesDeepCopy = getPopulationFitnesses(population);	//
														//lies
		Arrays.sort(fitnessesDeepCopy);								//
		double median = fitnessesDeepCopy[population.size() / 2];		//madness

		int emigs = 0;
		while (emigs < amount){
			int ind = rnd_.nextInt(population.size());
			if (population.get(ind) == null){
				continue;
			}
			if (population.get(ind).fitness >= median){
				emigrators[emigs] = population.get(ind);
				emigs++;
				population.set(ind, null);
			}
		}
		return emigrators;

	}

	public void takeInImmigrants(Chromosome[] immigrants){
		int ind = 0;
		for (int i = 0; i < population.size(); i++){
			if (population.get(i) == null) {
				population.set(i, immigrants[ind]);
				if (bestFitness < immigrants[ind].fitness) {
					try {
						bestChromosome = (Chromosome)immigrants[ind].clone();
					} catch (CloneNotSupportedException e) {
						e.printStackTrace();
					}
					bestFitness = bestChromosome.fitness;
				}
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


	private double[] getPopulationFitnesses(ArrayList<Chromosome> pop) {
		double[] fitnesses = new double[pop.size()];
		int index = 0;
		for (Chromosome chr : pop) {
			fitnesses[index++] = chr.fitness;
		}
		return fitnesses;
	}


	@Override
	protected Object clone() throws CloneNotSupportedException {
	    Island cloned = (Island)super.clone();

	    cloned.population = new ArrayList<Chromosome>();
	    for (Chromosome chr : population) {
	    	cloned.population.add((Chromosome)chr.clone());
	    }
	    cloned.bestChromosome = (Chromosome)bestChromosome.clone();

	    return cloned;
	}

}
