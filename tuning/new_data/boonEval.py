import numpy as np
import scipy
import matplotlib.pyplot as plt
import csv
import sys
import math
from scipy.stats import ttest_ind # Package needed for statistical T-test

class run():
    def __init__(self,islandAmount,islandSize,migrationSize,crossoverRate,seed,epochAmount,fitnessMax,genotypeBest):
        self.islandAmount = int(islandAmount)
        self.islandSize = int(islandSize)
        self.migrationSize = int(migrationSize)
        self.crossoverRate = float(crossoverRate)
        self.seed = [int(x) for x in seed.split(";")]
        self.epochAmount = [int(x) for x in epochAmount.split(";")]
        self.fitnessMax = [float(x) for x in fitnessMax.split(";")]
        self.genotypeBest = [[float(y) for y in x.split(";")] for x in genotypeBest.split("|")]
        self.bestFitness = max(self.fitnessMax)
        self.getMeanBestFitness = sum(self.fitnessMax)/len(self.fitnessMax)
        self.Log_fit_10 = math.log10(10-self.bestFitness)
def read_files(filenames):
    filedata = []
    for file in filenames:
        with open(file,'r') as f:
            reader = csv.reader(f)
            for rows in reader:
                if not "islandAmount" in rows:
                    filedata.append(run(rows[0],rows[1],rows[2],rows[3],rows[4],rows[5],rows[6],rows[7]))
    params = filedata[0].__dict__.keys()
    for param1 in params:
        for param2 in params:
            if param2 != param1:
                plot_2D(filedata,param1,param2)
"""
        filedatas.append(filedata)
    for filedata in filedatas:
        for key,value in filedata.items():
            if key in data.keys():
                data[key].append(value)
            else:
                data[key] = [value]
    #print(data.keys())
"""
def plot_2D(data,param1,param2):
    p1s, p2s = [],[]
    for run in data:
        p1s.append(getattr(run,param1))
        p2s.append(getattr(run,param2))
    try:
        plt.plot(p1s,p2s,"bo")
        plt.xlabel(param1)
        plt.ylabel(param2)
        plt.savefig("{}_{}_plot.png".format(param1,param2))
    except:
        print("cant make plot")
if __name__ == "__main__":
    read_files(["silvanNew.csv","AdriaanNew.csv","maartenNew.csv"])
