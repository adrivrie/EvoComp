import csv
import matplotlib.pyplot as plt
from collections import defaultdict
import numpy as np

filenames = ['adriaandataFixed.csv', 'silvan.csv']


attributes = defaultdict(list)

for fn in filenames:
    with open(fn) as csvfile:
        reader = csv.DictReader(csvfile)
        for row in reader:
            for att in row:
                attributes[att].append(row[att])

attributes['meanFitness'] = [np.mean([float(x) for x in x.split(';')]) for x in attributes['fitnessMax']]
attributes['islandSize'] = [int(x) for x in attributes['islandSize']]
attributes['islandAmount'] = [int(x) for x in attributes['islandAmount']]

print (attributes.keys())

inversefitness = [10-x for x in attributes['meanFitness']]

plt.scatter(attributes['islandAmount'], inversefitness)
plt.xlabel('islandAmoun')
plt.xscale('log')
plt.yscale('log')
plt.ylabel('10 - fitness')
plt.show()