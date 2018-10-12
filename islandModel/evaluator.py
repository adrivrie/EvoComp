import numpy as np
import matplotlib.pyplot as plt
import pandas as pd
from scipy.stats import ttest_ind # Package needed for statistical T-test
import scipy.interpolate
from mpl_toolkits.mplot3d import Axes3D


class Evaluator:
    #epsilon
    #optimal_value
    #time_threshold # how fast algorithm should be in AES

    def __init__(self, eps,optval,time):
        self.epsilon = eps
        self.optimal_value = optval
        self.time_threshold = time

    def setMetaData(self,file,list):
        self.metaData = pd.read_csv(file, names = list)

    def getMetaData(self):
        print(self.metaData)

    def evaluateExperiment(self,file,list):
        self.setMetaData(file,list)
        for index, row in self.metaData.iterrows():
            alg_name = str(row["Algorithm"])
            algorithm = pd.read_csv("./islandModel/experiment_output/" + alg_name +".csv",names = ["run","best fitness"])
            #print(algorithm)
            self.metaData.loc[(self.metaData['Algorithm']==alg_name),"MBF"] = algorithm["best fitness"].mean()

    def plotExperiment(self,xaxis,yaxis):
        # Create coordinate pairs
        x = self.metaData[xaxis]
        y = self.metaData[yaxis]
        z = self.metaData["MBF"]

        cartcoord = list(zip(x, y))
        X = np.linspace(min(x), max(x))
        Y = np.linspace(min(y), max(y))
        X, Y = np.meshgrid(X, Y)

        # Approach 1
        #interp = scipy.interpolate.LinearNDInterpolator(cartcoord, z, fill_value=0)
        #Z0 = interp(X, Y)

        func = scipy.interpolate.interp2d(x, y, z)
        Z = func(X[0, :], Y[:, 0])
        print(Z.shape)
        cmhot = plt.get_cmap("viridis")
        #plt.figure()
        #plt.pcolormesh(X, Y, Z0)
        #plt.colorbar() # Color Bar
        #plt.show()
        fig = plt.figure()
        ax = Axes3D(fig)
        #xx, yy = np.meshgrid(self.metaData[xaxis], self.metaData[yaxis])
        #ax.plot_trisurf(xx,yy,self.metaData["MBF"])
        #ax.scatter(self.metaData[xaxis], self.metaData[yaxis], self.metaData["MBF"])
        ax.plot_surface(X,Y,Z,cmap=cmhot)
        ax.set_xlabel(xaxis)
        ax.set_ylabel(yaxis)
        ax.set_zlabel("Mean Best Fitness")
        plt.show()

        #xx, yy = np.meshgrid(self.metaData[xaxis], self.metaData[yaxis])
        #ax.plot_trisurf(xx,yy,self.metaData["MBF"])
        #ax.scatter(self.metaData[xaxis], self.metaData[yaxis], self.metaData["MBF"])

    # Evaluates the algorithm for performance measures and speed, results are put in the metaData file
    def evaluateAlgorithm(self,algorithm_name, run, islands):
        print(algorithm_name)
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
                for j in range(1,100):
                    column_names_island.append("fitness_"+str(j))
                island = pd.read_csv("./islandModel/evaluation_files/" + str(algorithm_name) + "_" + str(x) + "_" + str(y) + ".csv", names=column_names_island)
                print(island)
                #best_fitness_island = island.iloc[1]

                algorithm["best fitness island " + str(y)] = self.getBestFitness(island)
            algorithm["best fitness"] = self.getBestFitness(algorithm)
            #algorithm["successful"] = isAlgorithmSuccessful(algorithm)
            #print(algorithm)
            metaData.loc[(metaData['Algorithm']==algorithm_name),"best fitness run" +str(x)] = algorithm["best fitness"].max()
            all_runs.loc[(all_runs['Algorithm']==algorithm_name)&(all_runs['run']==x),"successful"] = self.isAlgorithmSuccessful(algorithm)
            all_runs.loc[(all_runs['Algorithm']==algorithm_name)&(all_runs['run']==x),"evaluations until success"] = self.whenAlgorithmSuccessful(algorithm)


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
    def getBestFitness(self,algorithm):
        fitness_cols = algorithm.filter(regex='fitness')
        return(fitness_cols.values.max(axis=1))

    # Checks if the algorithm is successful. True if less than epsilon from optimal value
    def isAlgorithmSuccessful(self,algorithm):
        fitness_cols = algorithm.filter(regex='best fitness')
        return( fitness_cols.values.max() > (self.optimal_value - self.epsilon) )

    # AES as time measure
    def whenAlgorithmSuccessful(self,algorithm):
        successes = algorithm.loc[algorithm["best fitness"] > (self.optimal_value - self.epsilon), "evaluations"]
        if not successes.empty:
            return successes.iloc[0]

    # Gets the MBF and std of the algorithm and adds it to the meta data
    def getMeanBestFitness(self,metaData_list):
        fitness_cols = metaData_list.filter(regex='best fitness')
        uniqueAlgorithms = metaData_list['Algorithm'].unique()
        i = 0
        for x in uniqueAlgorithms:
            metaData.loc[metaData['Algorithm']==x, "MBF"] = fitness_cols.iloc[i].mean()
            metaData.loc[metaData['Algorithm']==x, "std (best fitness)"] = fitness_cols.iloc[i].std()
            i=i+1

    # Gets the average number of evaluations on termination with successful outcomes
    def getAES(self,list):
        global metaData
        uniqueAlgorithms = list['Algorithm'].unique()
        for x in uniqueAlgorithms:
            metaData.loc[metaData['Algorithm']==x, "AES"] = list.loc[(list['Algorithm']==x) & (list["successful"]==True), "evaluations until success"].mean()

    def getProportionInTimeSuccessful(self,list):
        uniqueAlgorithms = list['Algorithm'].unique()
        for x in uniqueAlgorithms:
             number_successful = list.loc[(list['Algorithm']==x) & (list["successful"]==True), "evaluations until success"].count()
             total = list.loc[list['Algorithm']==x,"Algorithm"].count()
             metaData.loc[metaData['Algorithm']==x, "percentage successful in time"] = number_successful / float(total) * 100

    # Gets the T-test for the means of two independent samples of scores.
    # See: https://docs.scipy.org/doc/scipy/reference/generated/scipy.stats.ttest_ind.html#scipy.stats.ttest_ind
    # About interpreting the P-value: https://stats.stackexchange.com/questions/31/what-is-the-meaning-of-p-values-and-t-values-in-statistical-tests#101
    def getTtestValue(self,algorithm1_values, algorithm2_values):
        return ttest_ind(algorithm1_values, algorithm2_values, equal_var = False)

    # Plot the best fitness for an algorithm over time. Input should be a list with tuples (algorithm, run), e.g.: [('Algorithm1','2'),('Algorithm2','1')]
    def plotBestFitness(self,listOfAlgorithmNamesAndRunsTuples):
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
    def plotDiversity(self,listOfAlgorithmNamesAndRunsTuples):
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
    def outputMeasures(self,metaData_list):
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
