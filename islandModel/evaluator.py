import numpy as np
import matplotlib.pyplot as plt
import pandas as pd
from scipy.stats import ttest_ind # Package needed for statistical T-test
import scipy.interpolate as intp
import scipy as sp
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

    def setMetaData(self,file):
        self.metaData = pd.read_csv(file, sep=',', engine='python')
        #print(self.metaData.drop('epochAmount', 1))
        fitness = self.metaData.fitnessMax.str.split(";", expand=True)
        column_names = []
        for i in range(1,len(fitness.columns)+1):
            column_names.append("Fitness " +str(i))
        fitness.columns = column_names
        fitness = fitness.apply(pd.to_numeric, errors='coerce')
        #print(fitness.dtypes)
        self.metaData = pd.concat([self.metaData.drop('fitnessMax', 1), fitness], axis=1)

    def getMetaData(self):
        return self.metaData

    def evaluateExperiment(self,file):
        self.setMetaData(file)
        self.metaData["Mean Best Fitness"] = self.getMeanBestFitness(self.metaData,True)
        print(self.metaData["Mean Best Fitness"].values.min())
        #for index, row in self.metaData.iterrows():
            #alg_name = str(row["Algorithm"])
            #algorithm = pd.read_csv("./islandModel/experiment_output/" + alg_name +".csv",names = ["run","best fitness"])
            #print(algorithm)
            #self.metaData.loc[(self.metaData[index,"MBF"] = algorithm["best fitness"].mean()


    def plotExperiment(self,xaxis,yaxis,surface_plot):
        x = self.metaData[xaxis]
        y = self.metaData[yaxis]
        z = self.metaData["Mean Best Fitness"]
        X = np.linspace(min(x), max(x))
        Y = np.linspace(min(y), max(y))
        X, Y = np.meshgrid(X, Y)

        # Approach 1
        #interp = scipy.interpolate.LinearNDInterpolator(cartcoord, z, fill_value=0)
        #Z0 = interp(X, Y)
        if surface_plot:
            z_grid = sp.interpolate.griddata(np.array([x.ravel(),y.ravel()]).T,z.ravel(),(X,Y))#,method='nearest') #method='cubic')   # default method is linear
        #func = sp.interpolate.interp2d(x, y, z)
        #Z = func(X[0, :], Y[:, 0])
        cmhot = plt.get_cmap("viridis")
        #plt.figure()
        #plt.pcolormesh(X, Y, Z0)
        #plt.colorbar() # Color Bar
        #plt.show()
        fig = plt.figure()
        ax = Axes3D(fig)
        #xx, yy = np.meshgrid(self.metaData[xaxis], self.metaData[yaxis])
        #ax.plot_trisurf(x,y,z)
        if surface_plot:
            ax.plot_surface(X,Y,z_grid,cmap=cmhot, vmin=0, vmax=10)
        else:
            ax.scatter(x, y, z,c=z,cmap=cmhot, vmin=0, vmax=10)
        ax.set_xlabel(xaxis)
        ax.set_ylabel(yaxis)
        ax.set_zlabel("Mean Best Fitness")
        plt.show()

    # The x3 axis is not fixed
    def plotSlice(self,var1,var2,var3,fix1,fix2):
        x1 = self.metaData[var1]
        x2 = self.metaData[var2]
        x3 = self.metaData[var3]
        #x4 = self.metaData[var4]
        z = self.metaData["Mean Best Fitness"]
        yq = np.linspace(min(x3), max(x3))
        Z = sp.interpolate.griddata(np.array([x1.ravel(),x2.ravel(),x3.ravel()]).T,
                                          z.ravel(),
                                          (fix1,fix2,yq),method='nearest') #method='cubic')   # default method is linea
        #cmhot = plt.get_cmap("viridis")
        fig = plt.figure()
        plt.plot(yq,Z)
        plt.xlabel(var3)
        plt.ylabel("Mean Best Fitness")
        plt.show()

    def evaluateAlgorithms(self):
        self.all_runs = pd.DataFrame(columns=["Algorithm","run"])
        i = 0
        for index, row in self.metaData.iterrows():
            for x in (1,row['runs']):
                all_runs.loc[i] = [row["Algorithm"], x]
                i=i+1
        for index, row in self.metaData.iterrows():
            self.evaluateAlgorithm(row["Algorithm"], row["runs"], row["islands"])
        getMeanBestFitness(all_runs)
        getAES(all_runs)
        getProportionInTimeSuccessful(all_runs)

    # Evaluates the algorithm for performance measures and speed, results are put in the metaData file
    def evaluateAlgorithm(self,algorithm_name, run, islands):
        print(algorithm_name)
        for x in range(1,run+1):
            print("run " + str(x))
            algorithm = pd.read_csv("./islandModel/evaluation_files/" + str(algorithm_name) + "_" + str(x) + ".csv")
            algorithm["Best Fitness Algorithm"] = getBestFitness(algorithm)
            self.all_runs.loc[(self.all_runs['Algorithm']==algorithm_name)&(self.all_runs['run']==x),"successful"] = self.isAlgorithmSuccessful(algorithm)
            self.all_runs.loc[(self.all_runs['Algorithm']==algorithm_name)&(self.all_runs['run']==x),"evaluations until success"] = self.whenAlgorithmSuccessful(algorithm)
            self.all_runs.loc[(self.all_runs['Algorithm']==algorithm_name)&(self.all_runs['run']==x),"best fitness"] = algorithm["Best Fitness Algorithm"]
            #column_names = ["evaluations"]
            #for i in range(1,islands+1):
            #    column_names.append("BF island " + str(i))
            #algorithm = pd.DataFrame(columns=column_names)
            #for y in range(0,islands):

                #print("./islandModel/evaluation_files/" + str(algorithm_name) + "_" + str(x) + "_" + str(y) + ".csv")
                #island = pd.read_csv("./islandModel/evaluation_files/" + str(algorithm_name) + "_" + str(x) + "_" + str(y) + ".csv")
                #column_names_island = ["generations", "evaluations"]
                #for j in range(1,100):
                #    column_names_island.append("fitness_"+str(j))
                #island = pd.read_csv("./islandModel/evaluation_files/" + str(algorithm_name) + "_" + str(x) + "_" + str(y) + ".csv", names=column_names_island)
                #print(island)
                #best_fitness_island = island.iloc[1]

                #algorithm["best fitness island " + str(y)] = self.getBestFitness(island)
            #algorithm["best fitness"] = self.getBestFitness(algorithm)
            #algorithm["successful"] = isAlgorithmSuccessful(algorithm)
            #print(algorithm)
            #metaData.loc[(metaData['Algorithm']==algorithm_name),"best fitness run" +str(x)] = algorithm["best fitness"].max()
            #all_runs.loc[(all_runs['Algorithm']==algorithm_name)&(all_runs['run']==x),"successful"] = self.isAlgorithmSuccessful(algorithm)
            #all_runs.loc[(all_runs['Algorithm']==algorithm_name)&(all_runs['run']==x),"evaluations until success"] = self.whenAlgorithmSuccessful(algorithm)


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
        fitness_cols = algorithm.filter(regex='Fitness')
        return(fitness_cols.values.max(axis=1))

    # Checks if the algorithm is successful. True if less than epsilon from optimal value
    def isAlgorithmSuccessful(self,algorithm):
        fitness_cols = algorithm.filter(regex='Best Fitness Island')
        return( fitness_cols.values.max() > (self.optimal_value - self.epsilon) )

    # AES as time measure
    def whenAlgorithmSuccessful(self,algorithm):
        successes = algorithm.loc[algorithm["Best Fitness Island"] > (self.optimal_value - self.epsilon), "evaluations"]
        if not successes.empty:
            return successes.iloc[0]

    # Gets the MBF and std of the algorithm and adds it to the meta data
    def getMeanBestFitness(self,metaData_list,experiment):
        #fitness_cols = metaData_list.filter(regex='best fitness')
        fitness_cols = metaData_list.filter(regex='Fitness')
        if experiment:
            #print(fitness_cols)
            #fitness_cols = pd.to_numeric(fitness_cols)
            return(fitness_cols.values.mean(axis=1))
        else:
            uniqueAlgorithms = metaData_list['Algorithm'].unique()
            i = 0
            for x in uniqueAlgorithms:
                self.metaData.loc[self.metaData['Algorithm']==x, "MBF"] = fitness_cols.iloc[i].mean()
                self.metaData.loc[self.metaData['Algorithm']==x, "std (best fitness)"] = fitness_cols.iloc[i].std()
                i=i+1

    # Gets the average number of evaluations on termination with successful outcomes
    def getAES(self,list):
        uniqueAlgorithms = list['Algorithm'].unique()
        for x in uniqueAlgorithms:
            self.metaData.loc[self.metaData['Algorithm']==x, "AES"] = list.loc[(list['Algorithm']==x) & (list["successful"]==True), "evaluations until success"].mean()

    def getProportionInTimeSuccessful(self,list):
        uniqueAlgorithms = list['Algorithm'].unique()
        for x in uniqueAlgorithms:
             number_successful = list.loc[(list['Algorithm']==x) & (list["successful"]==True), "evaluations until success"].count()
             total = list.loc[list['Algorithm']==x,"Algorithm"].count()
             self.metaData.loc[self.metaData['Algorithm']==x, "percentage successful in time"] = number_successful / float(total) * 100

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
