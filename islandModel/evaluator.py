import numpy as np
import matplotlib.pyplot as plt
import pandas as pd

### WORK IN PROGRESS

def getBestFitness(algorithm):
    fitness_cols = algorithm.filter(regex='fitness')
    algorithm['mean fitness'] = fitness_cols.mean(axis=1)
    print(fitness_cols)
    print(algorithm)
    print(algorithm['mean fitness'].mean())
    algorithm.plot(kind='line',x='generation',y='mean fitness')
    plt.show()

dummy = pd.read_csv("dummy.csv")
print(dummy.head())
print(dummy.shape)
print(dummy.iloc[:,1].mean())

getBestFitness(dummy)

#ax = plt.gca()

#dummy.plot(kind='line',x='generation',y="best_fitness",ax=ax)

#plt.show()
