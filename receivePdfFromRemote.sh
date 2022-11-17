#!/bin/bash

rsync -vr --exclude '.gitkeep' --exclude '.DS_Store' trashcan2:/Users/k.sato/TaskAllocation$1/pdf ./
rsync -vr --exclude '.gitkeep' --exclude '.DS_Store' trashcan2:/Users/k.sato/TaskAllocation$1/csv ./
rsync -vr --exclude '.gitkeep' --exclude '.DS_Store' trashcan2:/Users/k.sato/TaskAllocation$1/png ./ 
rsync -vr --exclude '.gitkeep' --exclude '.DS_Store' trashcan2:/Users/k.sato/TaskAllocation$1/config ./
