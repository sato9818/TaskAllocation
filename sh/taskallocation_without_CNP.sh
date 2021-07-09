#!/bin/bash

javac -sourcepath src -d bin src/Main.java
java -cp bin Main RECIPROCITY
java -cp bin Main RATIONAL


python3 python/csvToGragh_area.py 
python3 python/CsvToGraghAllWithoutCnp.py
python3 python/LineAlert.py
