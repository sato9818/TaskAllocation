
import matplotlib as mpl
import matplotlib.pyplot as plt
import math


import pandas as pd
import numpy as np
import datetime as dt

col = ["executed task", "wasted task", "communication time", "executed time", "waiting time", "all executed time", "leader count", "member count", "reciprocity leader count", "reciprocity member count", "message count", "overflowed task", "rejected task"]
ex_types= ["Rational", "Reciprocity"]

size = 1
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
    allocate.append("allocate area " + str(i) + " member")

names = []
for ex_type in ex_types:
    for a in allocate:
        names.append(ex_type + ": " + a)


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

fig, axes = plt.subplots(nrows=hei, ncols=wid, figsize=(9, 6))
count=0
for i in range(hei):
    for j in range(wid):
        
        ax=axes[i, j]
        
        plt.xlabel('tick')
        plt.ylabel('allocated member')
    
        for k in range(len(df)):
            df[k][count].plot(y=allocate, ax=ax, title="Area" + str(count + 1))
        ax.legend(names)
        # plt.ylim(0,1200)
        count = count + 1
plt.tight_layout()
plt.savefig("pdf/area/allocated member.pdf")

plt.close('all')



