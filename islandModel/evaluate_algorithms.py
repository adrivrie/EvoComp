from evaluator import Evaluator
import pandas as pd

# Initial Values specific to the problem instance
eval = Evaluator(10,65,60)
eval.setMetaData("./islandModel/evaluation_files/metaData.csv", names = ["algorithm", "runs", "islands", "population size"])
eval.evaluateAlgorithms()




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
