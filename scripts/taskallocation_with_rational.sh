#!/bin/bash

python3 python/line_alert.py start
javac -sourcepath src -d bin src/Main.java
java -cp bin Main RATIONAL

python3 python/area_csv_to_gragh.py Rational
python3 python/env_csv_to_gragh.py Rational
python3 python/line_alert.py finish
