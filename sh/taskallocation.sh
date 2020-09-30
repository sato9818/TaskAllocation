#!/bin/bash

javac -sourcepath src -d bin src/Main.java
java -cp bin Main true
java -cp bin Main false


python3 python/csvToGragh_area.py 
python3 python/csvToGragh_all.py
python3 python/LineAlert.py
