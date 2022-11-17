#!/bin/bash

rsync -vr --exclude '.gitkeep' --exclude '.DS_Store' src/ python/ scripts/ config/ execute.sh .env trashcan2:/Users/k.sato/TaskAllocation$1/
