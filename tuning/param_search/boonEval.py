import numpy as np
import matplotlib.pyplot as plt
import csv
from matplotlib import cm
import sys
import math
import random
from scipy.stats import ttest_ind # Package needed for statistical T-test
import scipy.interpolate as intp
import scipy as sp
from mpl_toolkits.mplot3d import Axes3D
from scipy.stats import ttest_ind # Package needed for statistical T-test

class run():
    def __init__(self,islandAmount,islandSize,migrationSize,crossoverRate,seed,epochAmount,fitnessMax,genotypeBest):
        self.islandAmount = int(islandAmount)
        self.islandSize = int(islandSize)
        self.migrationSize = (float(migrationSize)/float(islandSize))
        self.crossoverRate = float(crossoverRate)
        self.seed = [int(x) for x in seed.split(";")]
        self.epochAmount = [int(x) for x in epochAmount.split(";")]
        self.fitnessMax = [float(x) for x in fitnessMax.split(";")]
        self.genotypeBest = [[float(y) for y in x.split(";")] for x in genotypeBest.split("|")]
        self.bestFitness = max(self.fitnessMax)
        self.meanBestFitness = sum(self.fitnessMax)/len(self.fitnessMax)
def read_files(filenames):
    filedata = []
    for file in filenames:
        with open(file,'r') as f:
            reader = csv.reader(f)
            for rows in reader:
                if not "islandAmount" in rows:
                    filedata.append(run(rows[0],rows[1],rows[2],rows[3],rows[4],rows[5],rows[6],rows[7]))

    params = ["islandAmount","islandSize","migrationSize"]
    plot3D(filedata,"islandSize","migrationSize")
    plot3D(filedata,"islandSize","islandAmount")
    plot3D(filedata,"islandAmount","migrationSize")
    #plotSlice(filedata,"islandSize","islandAmount","migrationSize",40,50)
    #plotSlice(filedata,"migrationSize","islandAmount","islandSize",12,100)
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
        p2s.append(10-getattr(run,param2))
    try:
        plt.scatter(p1s,p2s, c='orange')
        plt.xlabel(param1)
        plt.ylabel("10-{}".format(param2))
        if not param1 in ["crossoverRate","migrationSize"]:
            plt.xscale("log")
        plt.yscale("log")
        plt.savefig("param_search_{}_{}_plot.png".format(param1,param2))
        # If this doesn't happen it keeps saving plots over itself
        plt.show()
    except:
        print("welp")

def plot3D(data,param1,param2):
    fitness = []
    x,y = [],[]
    for run in data:
        x.append(getattr(run,param1))
        y.append(getattr(run,param2))
        fitness.append(getattr(run,"meanBestFitness"))
    #x4 = self.metaData[var4]
    z = np.array(fitness)
    x = np.array(x)
    y = np.array(y)
    X = np.linspace(min(x), max(x))
    Y = np.linspace(min(y), max(y))
    X, Y = np.meshgrid(X, Y)

    # Approach 1
    #interp = scipy.interpolate.LinearNDInterpolator(cartcoord, z, fill_value=0)
    #Z0 = interp(X, Y)
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
    ax.plot_surface(X,Y,z_grid,cmap=cmhot, vmin=0, vmax=10)
    ax.set_xlabel(param1)
    ax.set_ylabel(param2)
    ax.set_zlabel("Mean Best Fitness")
    plt.show()
if __name__ == "__main__":
    read_files(["silvan.csv","adriaandataFixed.csv","oskar2.csv"])
