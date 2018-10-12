import numpy as np
import matplotlib.pyplot as plt
import pandas as pd
from scipy.stats import ttest_ind # Package needed for statistical T-test
import csv

#def plotBestFitness(algorithm):
#    fitness_cols = algorithm.filter(regex='fitness')
#    algorithm['mean fitness'] = fitness_cols.mean(axis=1)
#    print(fitness_cols)
#    print(algorithm)
#    print(algorithm['mean fitness'].mean())
#    algorithm.plot(kind='line',x='generation',y='mean fitness')
#    plt.show()

# Evaluates the algorithm for performance measures and speed, results are put in the metaData file
def evaluateAlgorithm(algorithm_name, run, islands):
    print(algorithm_name)
    global metaData
    global all_runs
    for x in range(1,run+1):
        print("run " + str(x))
        #algorithm = pd.read_csv("./islandModel/evaluation_files/" + str(algorithm_name) + "_" + str(x) + ".csv")
        column_names = ["evaluations"]
        for i in range(1,islands+1):
            column_names.append("BF island " + str(i))
        algorithm = pd.DataFrame(columns=column_names)
        for y in range(0,islands):

            #print("./islandModel/evaluation_files/" + str(algorithm_name) + "_" + str(x) + "_" + str(y) + ".csv")
            #island = pd.read_csv("./islandModel/evaluation_files/" + str(algorithm_name) + "_" + str(x) + "_" + str(y) + ".csv")
            column_names_island = ["generations", "evaluations"]
            for j in range(1,200):
                column_names_island.append("fitness_"+str(j))
            island = pd.read_csv("./islandModel/evaluation_files/" + str(algorithm_name) + "_" + str(x) + "_" + str(y) + ".csv", names=column_names_island)
            print(island)
            #best_fitness_island = island.iloc[1]

            algorithm["best fitness island " + str(y)] = getBestFitness(island)
        algorithm["best fitness"] = getBestFitness(algorithm)
        #algorithm["successful"] = isAlgorithmSuccessful(algorithm)
        #print(algorithm)
        metaData.loc[(metaData['Algorithm']==algorithm_name),"best fitness run" +str(x)] = algorithm["best fitness"].max()
        all_runs.loc[(all_runs['Algorithm']==algorithm_name)&(all_runs['run']==x),"successful"] = isAlgorithmSuccessful(algorithm)
        all_runs.loc[(all_runs['Algorithm']==algorithm_name)&(all_runs['run']==x),"evaluations until success"] = whenAlgorithmSuccessful(algorithm)


    #for index, row in metaData.iterrows():
    #    evaluateAlgorithm(row["Algorithm"], row["run"])



    #island = pd.read_csv("./islandModel/evaluation_files/" + str(algorithm_name) + "_" + str(run) + "_" + str(island) + ".csv")
    #island = evaluateIsland(island)
    #print(algorithm)
    #metaData.loc[(metaData['Algorithm']==algorithm_name) & (metaData['run']==run),"best fitness"] = getBestFitness(algorithm)
    #metaData.loc[(metaData['Algorithm']==algorithm_name) & (metaData['run']==run),"successful"] = isAlgorithmSuccessful(algorithm)
    #metaData.loc[(metaData['Algorithm']==algorithm_name) & (metaData['run']==run),"total number of evaluations"] = getTotalNumberOfEvaluations(algorithm)
    #metaData.loc[(metaData['Algorithm']==algorithm_name) & (metaData['run']==run),"total number of evaluations until success"] = whenAlgorithmSuccessful(algorithm)

### WORK IN PROGRESS

# Gets the best fitness of each row
def getBestFitness(algorithm):
    fitness_cols = algorithm.filter(regex='fitness')
    return(fitness_cols.values.max(axis=1))

# Checks if the algorithm is successful. True if less than epsilon from optimal value
def isAlgorithmSuccessful(algorithm):
    fitness_cols = algorithm.filter(regex='best fitness')
    return( fitness_cols.values.max() > (optimal_value - epsilon) )

# AES as time measure
def whenAlgorithmSuccessful(algorithm):
    successes = algorithm.loc[algorithm["best fitness"] > (optimal_value - epsilon), "evaluations"]
    if not successes.empty:
        return successes.iloc[0]

# Gets the MBF and std of the algorithm and adds it to the meta data
def getMeanBestFitness(metaData_list):
    fitness_cols = metaData_list.filter(regex='best fitness')
    uniqueAlgorithms = metaData_list['Algorithm'].unique()
    i = 0
    for x in uniqueAlgorithms:
        metaData.loc[metaData['Algorithm']==x, "MBF"] = fitness_cols.iloc[i].mean()
        metaData.loc[metaData['Algorithm']==x, "std (best fitness)"] = fitness_cols.iloc[i].std()
        i=i+1

# Gets the average number of evaluations on termination with successful outcomes
def getAES(list):
    global metaData
    uniqueAlgorithms = list['Algorithm'].unique()
    for x in uniqueAlgorithms:
        metaData.loc[metaData['Algorithm']==x, "AES"] = list.loc[(list['Algorithm']==x) & (list["successful"]==True), "evaluations until success"].mean()

def getProportionInTimeSuccessful(list):
    uniqueAlgorithms = list['Algorithm'].unique()
    for x in uniqueAlgorithms:
         number_successful = list.loc[(list['Algorithm']==x) & (list["successful"]==True), "evaluations until success"].count()
         total = list.loc[list['Algorithm']==x,"Algorithm"].count()
         metaData.loc[metaData['Algorithm']==x, "percentage successful in time"] = number_successful / float(total) * 100

# Gets the T-test for the means of two independent samples of scores.
# See: https://docs.scipy.org/doc/scipy/reference/generated/scipy.stats.ttest_ind.html#scipy.stats.ttest_ind
# About interpreting the P-value: https://stats.stackexchange.com/questions/31/what-is-the-meaning-of-p-values-and-t-values-in-statistical-tests#101
def getTtestValue(algorithm1_values, algorithm2_values):
    return ttest_ind(algorithm1_values, algorithm2_values, equal_var = False)

# Plot the best fitness for an algorithm over time. Input should be a list with tuples (algorithm, run), e.g.: [('Algorithm1','2'),('Algorithm2','1')]
def plotBestFitness(listOfAlgorithmNamesAndRunsTuples):
    ax = plt.gca()
    list_of_names = []
    for x in listOfAlgorithmNamesAndRunsTuples:
        list_of_names.append(x[0] + ", run " + x[1])
        islands = metaData.loc[(metaData["Algorithm"]==x[0]),"islands"]
        algorithm = pd.read_csv("./islandModel/evaluation_files/" + x[0] + "_" + x[1] + ".csv")
        for y in range(1,islands+1):
            island = pd.read_csv("./islandModel/evaluation_files/" + x[0] + "_" + x[1] + "_" + str(y) + ".csv")
            algorithm["best fitness island " + str(y)] = getBestFitness(island)
        algorithm["best fitness"] = getBestFitness(algorithm)
        algorithm.plot(kind='line',x='evaluations',y='best fitness',ax=ax)
    ax.legend(list_of_names);
    plt.ylabel('best fitness')
    plt.show()

# Plot the diversity for an algorithm over time. Input should be a list with tuples (algorithm, run), e.g.: [('Algorithm1','2'),('Algorithm2','1')]
def plotDiversity(listOfAlgorithmNamesAndRunsTuples):
    ax = plt.gca()
    list_of_names = []
    for x in listOfAlgorithmNamesAndRunsTuples:
        list_of_names.append(x[0] + ", run " + x[1])
        algorithm = pd.read_csv("./islandModel/evaluation_files/" + x[0] + "_" + x[1] + ".csv")
        algorithm.plot(kind='line',x='evaluations',y='diversity',ax=ax)
    ax.legend(list_of_names);
    plt.ylabel('diversity')
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

####################################################################

# Initial Values specific to the problem instance
epsilon = 10
optimal_value = 65
time_threshold = 60 # how fast algorithm should be in AES


metaData = pd.read_csv("./islandModel/evaluation_files/metaData.csv", names = ["Algorithm", "runs", "islands", "population size", "param_lifetime"])
print(metaData)

all_runs = pd.DataFrame(columns=["Algorithm","run"])

i = 0
for index, row in metaData.iterrows():
    for x in (1,row['runs']):
        all_runs.loc[i] = [row["Algorithm"], x]
        i=i+1


for index, row in metaData.iterrows():
    evaluateAlgorithm(row["Algorithm"], row["runs"], row["islands"])

print(all_runs)
"""
getMeanBestFitness(metaData)
#getBestFitnessSTD(metaData)
getAES(all_runs)
getProportionInTimeSuccessful(all_runs)

print(metaData)

plotBestFitness([('Algorithm1','2'),('Algorithm2','1')])
plotDiversity([('Algorithm1','2'),('Algorithm2','1')])


#alg1_bestfitness = metaData.loc[metaData["Algorithm"]=="Algorithm1","best fitness"]
#alg2_bestfitness = metaData.loc[metaData["Algorithm"]=="Algorithm2","best fitness"]
#print("T-test value:")
#print(getTtestValue(alg1_bestfitness,alg2_bestfitness))
#outputMeasures(metaData)

#plotCPUtime_bestFitness([('Algorithm1','2'),('Algorithm2','1')])


#dummy = pd.read_csv("./islandModel/evaluation_files/dummy.csv")
#print(dummy.head())
#print(dummy.shape)
#print(dummy.iloc[:,1].mean())

#getBestFitness(dummy)


#ax = plt.gca()

#dummy.plot(kind='line',x='generation',y="best_fitness",ax=ax)

#plt.show()
"""
