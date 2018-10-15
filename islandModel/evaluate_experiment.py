from evaluator import Evaluator
import pandas as pd

eval = Evaluator(1,10,10)
eval.evaluateExperiment("./islandModel/experiment_output/silvan1.csv")
print(eval.getMetaData())
#eval.evaluateExperiment("./islandModel/experiment_output/metaData.csv", ["Algorithm", "runs", "islands", "population size", "migration size", "cross-over rate"])

#eval.getMetaData()
eval.plotExperiment("islandAmount","islandSize",False)
eval.plotExperiment("islandSize","crossoverRate",False)
eval.plotExperiment("crossoverRate","migrationSize",False)
eval.plotSlice("islandSize","crossoverRate","migrationSize","islandAmount",300,0.7,12)
eval.plotSlice("islandSize","islandAmount","crossoverRate","migrationSize",300,15,0.7)
eval.plotSlice("islandSize","islandAmount","migrationSize","crossoverRate",300,10,50)
eval.plotSlice("crossoverRate","migrationSize","islandAmount","islandSize",0.5,12,100)
