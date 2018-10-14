from evaluator import Evaluator
import pandas as pd

eval = Evaluator(10,65,60)
eval.evaluateExperiment("./islandModel/experiment_output/oskar.csv")
print(eval.getMetaData())
#eval.evaluateExperiment("./islandModel/experiment_output/metaData.csv", ["Algorithm", "runs", "islands", "population size", "migration size", "cross-over rate"])

#eval.getMetaData()
eval.plotExperiment("islandAmount","islandSize",True)
eval.plotExperiment("islandSize","crossoverRate",True)
eval.plotExperiment("crossoverRate","migrationSize",False)
eval.plotSlice("islandSize","crossoverRate","migrationSize","islandAmount",300,0.5,10)
eval.plotSlice("islandSize","islandAmount","crossoverRate","migrationSize",300,15,0.7)
#eval.plotExperiment("migration size","cross-over rate")
#eval.plotSlice("migration size","cross-over rate",10)


#TODO: write 2d slice of such a ND mat
