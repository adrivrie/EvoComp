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
attributes['bestFitness'] = [np.max([float(x) for x in x.split(';')]) for x in attributes['fitnessMax']]
attributes['islandSize'] = [int(x) for x in attributes['islandSize']]
attributes['islandAmount'] = [int(x) for x in attributes['islandAmount']]

print (attributes.keys())

inversefitness = [10-x for x in attributes['meanFitness']]
inversefitnessBest = [10-x for x in attributes['bestFitness']]

#for at in ['islandAmount', 'migrationSize', 'islandSize']:
#    attributes[at] = [int(x) for x in attributes[at]]
#    print attributes[at]
#    
#    plt.scatter(attributes[at], inversefitness)
#    plt.xlabel(at)
#    plt.xscale('log')
#    plt.yscale('log')
#    plt.ylabel('10 - fitness')
#    plt.show()

attributes['migrationSize'] = [int(x)+1 for x in attributes['migrationSize']]
attributes['migrationSizeRel'] = np.array([float(x) for x in attributes['migrationSize']])/np.array(attributes['islandSize'])
print attributes['migrationSizeRel']

plt.scatter(attributes['migrationSize'], inversefitness)
plt.xlabel('migrationSize Relative')
plt.xscale('log')
plt.yscale('log')
plt.ylabel('10 - fitness')
plt.show()
