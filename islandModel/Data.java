import java.util.ArrayList;

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
	
}
