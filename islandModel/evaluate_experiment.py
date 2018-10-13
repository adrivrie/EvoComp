from evaluator import Evaluator
import pandas as pd

eval = Evaluator(10,65,60)
eval.evaluateExperiment("./islandModel/experiment_output/metaData.csv", ["Algorithm", "runs", "islands", "population size", "migration size", "cross-over rate"])

eval.getMetaData()
eval.plotExperiment("islands","population size")
eval.plotSlice("islands","population size",300)
eval.plotExperiment("migration size","cross-over rate")
eval.plotSlice("migration size","cross-over rate",10)


#TODO: write 2d slice of such a ND mat
