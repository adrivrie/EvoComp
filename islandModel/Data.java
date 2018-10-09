import java.util.ArrayList;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.io.*;
import java.io.IOException;
/**
 *
 * This class contains all information that Evaluator needs of one run of player64.
 *
 */
public class Data {
	// at index i, gives the best fitness of the population after generation i
	public ArrayList<Double> bestFitness = new ArrayList<Double>();

	// full history of the run:
	// 	islands :
	//		epoch : generation
	//			individuals
	//			parameters
	public IslandHistory history;

	public Data(int nIslands) {
		history = new IslandHistory(nIslands);
	}



	// adds current islands to the island history
	public void addIslandHistory(Island[] islands, int epochIndex) {
		for (int islandIndex=0; islandIndex<islands.length; islandIndex++) {
			history.addIsland(islands[islandIndex], islandIndex, epochIndex);
		}
	}


	@Override
	public String toString() {
		String str = "";
		str += String.format("dims: %d, %d, %d", history.epochHistory.size(),
				history.epochHistory.get(0).size(),
				history.epochHistory.get(0).get(0).size());


//		str += "Best fitness per generation:\n\t[";
//		for (double fitness : bestFitness) {
//			str += String.format("%.1e, ", fitness);
//		}
//		str += "]";

		return str;
	}

	// full history of the islands in one run
	public class IslandHistory {
		//  epochInd, islandInd, iterationInd -> island
		public ArrayList<ArrayList<ArrayList<Island>>> epochHistory;
		// number of islands
		private int nIslands;

		public IslandHistory(int nIslands) {
			this.nIslands = nIslands;
			epochHistory = new ArrayList<ArrayList<ArrayList<Island>>>();
		}


		// returns a reference to the island with given index as it was in a given epoch in a given iteration
		public Island getIsland(int epochIndex, int islandIndex, int generationIndex) {
			if (epochHistory.size() > epochIndex) {
				ArrayList<ArrayList<Island>> epochHist = epochHistory.get(epochIndex);
				if (epochHist.size() > islandIndex) {
					ArrayList<Island> islandHist = epochHist.get(islandIndex);
					if (islandHist.size() > generationIndex) {
						Island island = islandHist.get(generationIndex);
						return island;
					}
				}
			}
			return null;
		}

		// adds an island to the history at a certain epoch (if the island has already been added with the same generation number, it is assumed that it hasn't changed)
		public void addIsland(Island island, int islandIndex, int epochIndex) {
			if (epochHistory.size() == epochIndex) { // new epoch, add element to list
				epochHistory.add(new ArrayList<ArrayList<Island>>());
				for (int i=0; i<nIslands; i++) {
					epochHistory.get(epochIndex).add(new ArrayList<Island>());
				}
			}
			// don't add island if its generation was added before
			if (getIsland(epochIndex, islandIndex, island.nGenerations) == null) {
				try {
					epochHistory.get(epochIndex).get(islandIndex).add((Island)island.clone());
				} catch (CloneNotSupportedException e) {
					e.printStackTrace();
				}
			}
		}


	}
	public static Path check_file(String fileName){
		// Find correct path or create if it doesn't exist
		Path currentPath = Paths.get(System.getProperty("user.dir"));
		//System.out.println(currentPath.toString());
		Path filePath = Paths.get(currentPath.toString(), "evaluation_files",fileName);

		if (Files.notExists(filePath.getParent())){
			// Folder doesn't exist
			System.out.println(filePath.getParent().toString()+" being created");
			// Create folder and file
			try {
				Files.createDirectories(filePath.getParent());
				Files.createFile(filePath);
				FileWriter writer = new FileWriter(new File(filePath.toString()));
				writer.write("generation,population_size,best_fitness,all_fitness\n");
				writer.close();
			} catch (IOException e){
				e.printStackTrace();
			}
		} else if (Files.notExists(filePath)) {
			System.out.println(filePath.toString()+" being created");
			// Create file
			try{
				Files.createFile(filePath);
			} catch (IOException e){
				e.printStackTrace();
			}
		}
		return filePath;
	}
	public static void writeIteration(Island[] islands, int epochIndex) {
		int run = 1;
		String algorithm_name = "Alg1";
		// write to csv
		try{
			for (int i=0;i<islands.length;i++) {
				Island island = islands[i];
				Path filePath = check_file(algorithm_name+"_"+Integer.toString(run)+"_"+Integer.toString(i)+".csv");
				File file = new File(filePath.toString());
				// Wel n beetje kut dat je hier steeds de writer opent en sluit
				// Maar anders cramp je alle open buffers vast.
				FileWriter writer = new FileWriter(file, true);
				if (!island.hasConverged){
					// get a string of all population fitnesses
					String fitnessString = "";
					for (Chromosome individual: island.population){
						fitnessString += String.format(",%.0e",individual.fitness);
					}
					fitnessString += "\n";
					// write all to file
					writer.write(String.format("%d,%d,%.0e",island.nGenerations, island.population.size(), island.bestFitness)+fitnessString);
					//System.out.print(String.format("N=%d b=%.0e\t", island.population.size(), island.bestFitness));
				}
				writer.close();
			}
		} catch (IOException e){
			e.printStackTrace();
		}

		// PAS OP: check of een eiland nog runt, of al geconverged is (dan hoef je geen data op te slaan want er is niks veranderd)
	}

}
