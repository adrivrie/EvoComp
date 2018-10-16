from evaluator import Evaluator
import pandas as pd

eval = Evaluator(1,10,10)
eval.evaluateExperiment("./islandModel/experiment_output/oskar2.csv")
print(eval.getMetaData())
#eval.evaluateExperiment("./islandModel/experiment_output/metaData.csv", ["Algorithm", "runs", "islands", "population size", "migration size", "cross-over rate"])

#eval.getMetaData()
#eval.plotExperiment("islandAmount","islandSize",False)
#eval.plotExperiment("islandSize","crossoverRate",False)
#eval.plotExperiment("crossoverRate","migrationSize",False)
eval.plotSlice("islandSize","migrationSize","islandAmount",300,12)
eval.plotSlice("islandSize","islandAmount","migrationSize",40,50)
eval.plotSlice("migrationSize","islandAmount","islandSize",12,100)
