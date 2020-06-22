
import matplotlib as mpl
import matplotlib.pyplot as plt
import math


import pandas as pd
import numpy as np
import datetime as dt

col = ["executed task", "wasted task", "communication time", "executed time", "waiting time", "all executed time", "leader count", "member count", "reciprocity leader count", "reciprocity member count", "message count", "overflowed task"]


size = 4
hei = int(math.sqrt(size))
wid = int(math.sqrt(size))
plt.rcParams["font.size"] = 8

path = []
for i in range(size):
    path.append("csv/Area" + str(i) + ".csv")

df = []
for i in range(size):
    df.append(pd.read_csv(path[i], index_col=0))

allocate = []
for i in range(size):
    allocate.append("allocate area " + str(i) + " member")


for c in col:
    fig, axes = plt.subplots(nrows=hei, ncols=wid, figsize=(9, 6))
    count=0
    for i in range(hei):
        for j in range(wid):        
            ax=axes[i, j]
            plt.axes(ax)

            df[count].plot(y=[c], ax=axes[i, j], legend=False, title="Area" + str(count + 1)) 
            plt.xlabel('tick')
            plt.ylabel(c)

            count = count + 1
            
    plt.tight_layout()
    plt.savefig("pdf/area/"+ c +".pdf")

count=0
for i in range(hei):
    for j in range(wid):
        col = []
        ax=axes[i, j]
        plt.axes(ax)
        plt.xlabel('tick')
        plt.ylabel('allocated member')
    
    
        df[count].plot(y=allocate, ax=axes[i, j], title="Area" + str(count + 1))
    
        plt.ylim(0,1200)
        count = count + 1
plt.tight_layout()
plt.savefig("pdf/area/allocated member.pdf")

plt.close('all')



