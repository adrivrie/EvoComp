#!/bin/bash

javac -cp contest.jar player64.java
jar cmf MainClass.txt submission.jar player64.class
java -jar testrun.jar -submission=player64 -evaluation=SchaffersEvaluation -seed=42