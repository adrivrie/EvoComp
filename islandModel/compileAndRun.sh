#DOE DIT EERST IN DE TERMINAL: export LD_LIBRARY_PATH=~/path/to/this/dir/
#EN DIT: cd ~/path/to/this/dir
#NU KUN JE DIT RUNNEN
javac -cp testrun.jar:submission.jar:contest.jar player64.java Evaluator.java Data.java Chromosome.java Island.java
java -cp testrun.jar:submission.jar:contest.jar Evaluator

