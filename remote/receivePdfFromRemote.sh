#!/bin/bash

rsync -vr --exclude '.gitkeep' --exclude '.DS_Store' --include='config/***' --include='png/***' --include='csv/***' --include='pdf/***' --exclude='*' trashcan:/Users/k.sato/TaskAllocation$1/ ./
