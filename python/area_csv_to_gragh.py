
import matplotlib as mpl
import matplotlib.pyplot as plt
import math

import pandas as pd
import numpy as np
import datetime as dt

import os
import shutil
import sys

shutil.rmtree('png/area')
os.mkdir('png/area')
shutil.rmtree('pdf/area')
os.mkdir('pdf/area')

col = [ \
    "Num of completed task", "Num of team formation failure", "Average communication time", "Average subtask completion time from member perspective", "Average time of subtask being in subtask queue", \
    "Average subtask completion time from leader perspective", "Average task completion time","Num of leaders", "Num of members", \
    "Num of reciprocal leaders", "Num of reciprocal members", "Num of sent messages", "Num of overflowed task from task queue", \
    "Average subtask queue size", "Average leader dependable agents type 0", "Average leader dependable agents type 1", \
    "Average leader dependable agents type 2", "Average member dependable agents", "Num of rejected task", "Task completion success rate", "Average of leader threshold", "Average of member threshold", "Average subtask queue size from leader perspective" \
    ]
args = sys.argv
ex_types = args[1:]

size = 4
hei = int(math.sqrt(size))
wid = int(math.sqrt(size))
plt.rcParams["font.size"] = 8

paths = []
for ex_type in ex_types:
    sublist = []
    for i in range(size):
        sublist.append("csv/" + ex_type + "/Area" + str(i) + ".csv")
    paths.append(sublist)

df = []
for path in paths:
    subdf = []
    for p in path:
        subdf.append(pd.read_csv(p, index_col=0))
    df.append(subdf)

allocate = []
for i in range(size):
    allocate.append("Num of allocated subtasks to member in area " + str(i))

names = []
for ex_type in ex_types:
    for i in range(size):
        names.append(ex_type[:3] + ": to area " + str(i))


for c in col:
    fig, axes = plt.subplots(nrows=hei, ncols=wid, figsize=(9, 6))
    count=0
    for i in range(hei):
        for j in range(wid):        
            ax=axes[i, j]
            
            for k in range(len(df)):
                df[k][count].plot(y=[c], ax=ax, title="Area" + str(count + 1)) 
            ax.legend(ex_types)
            plt.xlabel('tick')
            plt.ylabel(c)

            count = count + 1
            
    plt.tight_layout()
    plt.savefig("pdf/area/"+ c +".pdf")
    plt.savefig("png/area/"+ c +".png")

fig, axes = plt.subplots(nrows=hei, ncols=wid, figsize=(9, 6))
count=0
for i in range(hei):
    for j in range(wid):
        
        ax=axes[i, j]
        
        plt.xlabel('tick')
        plt.ylabel('Num of allocated subtasks to members in each area')
    
        for k in range(len(df)):
            df[k][count].plot(y=allocate, ax=ax, title="Num of allocated subtasks to members in each area" + str(count + 1))
        ax.legend(names)
        # plt.ylim(0,1200)
        count = count + 1
plt.tight_layout()
plt.savefig("pdf/area/Num of allocated subtasks to members in each area.pdf")
plt.savefig("png/area/Num of allocated subtasks to members in each area.png")

plt.close('all')



