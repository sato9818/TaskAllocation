#!/bin/bash

python3 python/line_alert.py start
javac -sourcepath src -d bin src/Main.java
java -cp bin Main RECIPROCITY
java -cp bin Main RATIONAL
java -cp bin Main CNP

python3 python/area_csv_to_gragh.py Reciprocity Rational CNP
python3 python/env_csv_to_gragh.py Reciprocity Rational CNP
python3 python/line_alert.py finish
