#!/bin/sh

set -eu

targetdir=target

if [ ! -d "$targetdir" ]; then mkdir $targetdir; fi

javac -sourcepath src -source 8 -target 8 -d $targetdir -cp /usr/share/java/junit.jar -extdirs lib/ src/core/*.java src/movement/*.java src/report/*.java src/routing/*.java src/gui/*.java src/input/*.java src/applications/*.java src/interfaces/*.java src/test/*.java
java -cp target:/usr/share/java/junit.jar:/usr/share/java/hamcrest-library.jar:/usr/share/java/hamcrest-core.jar org.junit.runner.JUnitCore test.AllTests
