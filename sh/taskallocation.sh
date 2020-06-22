#!/bin/bash

javac -sourcepath src -d bin src/Main.java
java -cp bin Main

python python/csvToGragh_area.py 
python python/csvToGragh_all.py
python python/LineAlert.py
