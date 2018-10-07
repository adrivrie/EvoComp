import numpy as np
import matplotlib.pyplot as plt
import pandas as pd
from scipy.stats import ttest_ind # Package needed for statistical T-test

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
    #print(algorithm)
    metaData.loc[(metaData['Algorithm']==algorithm_name) & (metaData['run']==run),"best fitness"] = getBestFitness(algorithm)
    metaData.loc[(metaData['Algorithm']==algorithm_name) & (metaData['run']==run),"successful"] = isAlgorithmSuccessful(algorithm)
    metaData.loc[(metaData['Algorithm']==algorithm_name) & (metaData['run']==run),"total number of evaluations"] = getTotalNumberOfEvaluations(algorithm)

### WORK IN PROGRESS

# Gets the best fitness in the whole run and adds it to the meta data
def getBestFitness(algorithm):
    fitness_cols = algorithm.filter(regex='fitness')
    return(fitness_cols.values.max())

# Checks if the algorithm is successful. True if less than epsilon from optimal value
def isAlgorithmSuccessful(algorithm):
    return(getBestFitness(algorithm) > (optimal_value - epsilon))

def getTotalNumberOfEvaluations(algorithm):
    return(algorithm["nr_of_evals"].sum())


# Gets the MBF of the algorithm and adds it to the meta data
def getMeanBestFitness(metaData_list):
    uniqueAlgorithms = metaData_list['Algorithm'].unique()
    for x in uniqueAlgorithms:
        metaData.loc[metaData['Algorithm']==x, "MBF"] = metaData.loc[metaData['Algorithm']==x, "best fitness"].mean()

# Gets the std of the best fitness of all runs of a particular algorithm and adds it to the meta data
def getBestFitnessSTD(metaData_list):
    uniqueAlgorithms = metaData_list['Algorithm'].unique()
    for x in uniqueAlgorithms:
        metaData.loc[metaData['Algorithm']==x, "std (best fitness)"] = metaData.loc[metaData['Algorithm']==x, "best fitness"].std()

# Gets the average number of evaluations on termination with successful outcomes
def getAES(metaData_list):
    uniqueAlgorithms = metaData_list['Algorithm'].unique()
    for x in uniqueAlgorithms:
        metaData.loc[metaData['Algorithm']==x, "AES"] = metaData.loc[(metaData['Algorithm']==x) & (metaData["successful"]==True), "total number of evaluations"].mean()

# Gets the T-test for the means of two independent samples of scores.
# See: https://docs.scipy.org/doc/scipy/reference/generated/scipy.stats.ttest_ind.html#scipy.stats.ttest_ind
# About interpreting the P-value: https://stats.stackexchange.com/questions/31/what-is-the-meaning-of-p-values-and-t-values-in-statistical-tests#101
def getTtestValue(algorithm1_values, algorithm2_values):
    return ttest_ind(algorithm1_values, algorithm2_values, equal_var = False)

# Plot the best fitness for an algorithm over the CPU time. Input should be a list with tuples (algorithm, run), e.g.: [(Algorithm1,1),(Algorithm2,1)]
def plotCPUtime_bestFitness(listOfAlgorithmNamesAndRunsTuples):
    ax = plt.gca()
    list_of_names = []
    for x in listOfAlgorithmNamesAndRunsTuples:
        list_of_names.append(x[0] + ", run " + x[1])
        algorithm = pd.read_csv("./islandModel/evaluation_files/" + x[0] + "_" + x[1] + ".csv")
        fitness_cols = algorithm.filter(regex='fitness')
        algorithm["best fitness"] = fitness_cols.max(axis=1)
        algorithm.plot(kind='line',x='CPU_time',y='best fitness',ax=ax)
    ax.legend(list_of_names);
    plt.ylabel('best fitness')
    plt.show()

# For getting the relevant performance and speed measures in a clear table
def outputMeasures(metaData_list):
    uniqueAlgorithms = metaData_list['Algorithm'].unique()
    output = metaData[["Algorithm"]].copy()
    output = output.drop_duplicates()
    for x in uniqueAlgorithms:
        output.loc[output["Algorithm"]==x,"Population size"] = metaData.loc[metaData['Algorithm']==x,"population_size"].iloc[0]
        output.loc[output["Algorithm"]==x,"Best fitness"] = metaData.loc[metaData['Algorithm']==x,"best fitness"].iloc[0]
        output.loc[output["Algorithm"]==x,"MBF"] = metaData.loc[metaData['Algorithm']==x,"MBF"].iloc[0]
        output.loc[output["Algorithm"]==x,"std (best fitness)"] = metaData.loc[metaData['Algorithm']==x,"std (best fitness)"].iloc[0]
        output.loc[output["Algorithm"]==x,"AES"] = metaData.loc[metaData['Algorithm']==x,"AES"].iloc[0]
    print("Performance and Speed measures:")
    print(output)
    #print("")
    #print("################################")
    #print("In LaTeX format:")
    #print("################################")
    #print(output.to_latex())

# Initial Values specific to the problem instance
epsilon = 10
optimal_value = 60


metaData = pd.read_csv("./islandModel/evaluation_files/metaData.csv")
metaData["best fitness"] = 0
#print(metaData)

for index, row in metaData.iterrows():
    evaluateAlgorithm(row["Algorithm"], row["run"])

getMeanBestFitness(metaData)
getBestFitnessSTD(metaData)
getAES(metaData)

print(metaData)
alg1_bestfitness = metaData.loc[metaData["Algorithm"]=="Algorithm1","best fitness"]
alg2_bestfitness = metaData.loc[metaData["Algorithm"]=="Algorithm2","best fitness"]
print("T-test value:")
print(getTtestValue(alg1_bestfitness,alg2_bestfitness))
#outputMeasures(metaData)

plotCPUtime_bestFitness([('Algorithm1','2'),('Algorithm2','1')])


#dummy = pd.read_csv("./islandModel/evaluation_files/dummy.csv")
#print(dummy.head())
#print(dummy.shape)
#print(dummy.iloc[:,1].mean())

#getBestFitness(dummy)


#ax = plt.gca()

#dummy.plot(kind='line',x='generation',y="best_fitness",ax=ax)

#plt.show()
