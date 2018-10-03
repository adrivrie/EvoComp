#!/bin/bash
cd ~/eclipse/eclipse-workspace/ECA/original/oskar0/

echo "Compile"
javac -cp contest.jar player0.java
echo "Make jar"
jar cmf MainClass.txt submission.jar player0.java



echo "Run test (BentCigarFunction)"
export LD_LIBRARY_PATH=~/eclipse/eclipse-workspace/ECA/backup/original/
java -jar testrun.jar -submission=player0 -evaluation=BentCigarFunction -seed=1


