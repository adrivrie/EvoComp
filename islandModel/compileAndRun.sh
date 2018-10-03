export LD_LIBRARY_PATH=~/path/to/this/folder/
javac -cp contest.jar:submission.jar:testrun.jar player64.java Evaluator.java Data.java Chromosome.java Island.java
java -cp contest.jar:submission.jar:testrun.jar Evaluator
