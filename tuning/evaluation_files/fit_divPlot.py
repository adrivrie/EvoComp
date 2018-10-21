import numpy as np
import csv
import matplotlib.pyplot as plt
class Island():
    def __init__(self):
        self.generations = []
        self.nrOfGens = 0
    def addGen(self, generation):
        self.generations.append(generation)
        self.nrOfGens += 1

class Generation():
    def __init__(self):
        self.generation = 0
        self.evaluations = 0
        self.popSize = 0
        self.fitnesses = []
        self.chromosomes = []
        self.bestFitness = 0

    def addIsland(self,generation,evaluations,popSize,fitnessString,chromosomeString):
        if self.generation == 0:
            self.generation = int(generation)
        if self.evaluations == 0:
            self.evaluations = int(evaluations)
        else:
            self.evaluations += int(evaluations)
        if popSize == 0:
            self.popSize = int(popSize)
        else:
            self.popSize += int(popSize)
        self.fitnesses += [float(x) for x in fitnessString.split(";")]
        """
        print(chromosomeString.split(";"))
        for x in chromosomeString.split(";"):
            for y in x.split("|")[:-1]:
                print(y)
                float(y)
        """
        self.chromosomes += [[float(genes) for genes in chromosome.split("|")[:-1]] for chromosome in chromosomeString.split(";")]
        self.diversity = calc_diversity(self.chromosomes)
        self.bestFitness = max(self.fitnesses)
def calc_diversity(population):
    var = []
    for gen in range(len(population[0])):
        var.append(np.std([x[gen] for x in population])**2)
    return np.mean(var)

def read_files(filenames):
    filedata = []
    total = {}
    for file in filenames:
        with open(file,'r') as f:
            reader = csv.reader(f)
            island = Island()
            for rows in reader:
                if rows[0] in total:
                    #print(total)
                    total[rows[0]].addIsland(rows[0],rows[1],rows[2],rows[3],rows[4])
                else:
                    gen = Generation()
                    gen.addIsland(rows[0],rows[1],rows[2],rows[3],rows[4])
                    total[rows[0]] = gen
    cum_eval = 0
    div = total[list(total.keys())[0]].diversity
    divs = [div]
    evals = [cum_eval]
    bf = 10-total[list(total.keys())[0]].bestFitness
    bestFits = [bf]
    for x in total.keys():
        cum_eval += total[x].evaluations
        div = total[x].diversity
        bf = total[x].bestFitness
        print("{}|{}".format(cum_eval,total[x].diversity))
        evals.append(cum_eval)
        divs.append(div)
        bestFits.append(10-bf)
    plt.plot(evals,divs)
    #plt.yscale("log")
    plt.xlabel("evaluations")
    plt.ylabel("diversity")
    plt.show()
    plt.plot(evals,bestFits)
    plt.yscale("log")
    plt.xlabel("evaluations")
    plt.ylabel("10-bestFitness")
    plt.show()

if __name__ == '__main__':
    islands = range(20)
    filenames = []
    for i in islands:
        filenames.append("null_{}.csv".format(i))
    read_files(filenames)
