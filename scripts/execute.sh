#!/bin/bash

for i in `seq $1 $2`
do 
    cd ../TaskAllocation$i
    nohup sh scripts/taskallocation_with_reciprocity.sh & 
done
