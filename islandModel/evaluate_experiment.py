from evaluator import Evaluator
import pandas as pd

eval = Evaluator(10,65,60)
eval.evaluateExperiment("./islandModel/experiment_output/metaData.csv", ["Algorithm", "runs", "islands", "population size", "migration size", "cross-over rate"])

eval.getMetaData()
eval.plotExperiment("islands","population size")
