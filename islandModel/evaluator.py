import numpy as np
import matplotlib.pyplot as plt
import pandas as pd

### WORK IN PROGRESS

def plotBestFitness(algorithm):
    fitness_cols = algorithm.filter(regex='fitness')
    algorithm['mean fitness'] = fitness_cols.mean(axis=1)
    print(fitness_cols)
    print(algorithm)
    print(algorithm['mean fitness'].mean())
    algorithm.plot(kind='line',x='generation',y='mean fitness')
    plt.show()

# Evaluates the algorithm for performance measures and speed, results are put in the metaData file
def evaluateAlgorithm(algorithm_name, run):
    global metaData
    algorithm = pd.read_csv("./islandModel/evaluation_files/" + str(algorithm_name) + "_" + str(run) + ".csv")
    print(algorithm)
    metaData.loc[(metaData['Algorithm']==algorithm_name) & (metaData['run']==run),"best fitness"] = getBestFitness(algorithm)

# Gets the best fitness in the whole run and adds it to the meta data
def getBestFitness(algorithm):
    fitness_cols = algorithm.filter(regex='fitness')
    return(fitness_cols.values.max())

def getMeanBestFitness(metaData_list):
    uniqueAlgorithms = metaData_list['Algorithm'].unique()
    for x in uniqueAlgorithms:
        metaData.loc[metaData['Algorithm']==x, "MBF"] = metaData.loc[metaData['Algorithm']==x, "best fitness"].mean()

def getBestFitnessSTD(metaData_list):
    uniqueAlgorithms = metaData_list['Algorithm'].unique()
    for x in uniqueAlgorithms:
        metaData.loc[metaData['Algorithm']==x, "best fitness std"] = metaData.loc[metaData['Algorithm']==x, "best fitness"].std()


metaData = pd.read_csv("./islandModel/evaluation_files/metaData.csv")
metaData["best fitness"] = 0
print(metaData)

for index, row in metaData.iterrows():
    evaluateAlgorithm(row["Algorithm"], row["run"])

getMeanBestFitness(metaData)
getBestFitnessSTD(metaData)

print(metaData)


#dummy = pd.read_csv("./islandModel/evaluation_files/dummy.csv")
#print(dummy.head())
#print(dummy.shape)
#print(dummy.iloc[:,1].mean())

#getBestFitness(dummy)


#ax = plt.gca()

#dummy.plot(kind='line',x='generation',y="best_fitness",ax=ax)

#plt.show()
