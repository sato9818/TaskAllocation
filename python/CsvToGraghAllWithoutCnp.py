import matplotlib as mpl
import matplotlib.pyplot as plt
import math

import pandas as pd
import numpy as np
import datetime as dt

# plt.rcParams["font.size"] = 8
col = ["executed task", "wasted task", "communication time", "executed time", "waiting time", "all executed time", "average task completion time","leader count", "member count", "reciprocity leader count", "reciprocity member count", "message count", "overflowed task", "average subtask queue size", "average leader dependable agents type 0", "average leader dependable agents type 1", "average leader dependable agents type 2", "average member dependable agents", "rejected task", "success rate"]
ex_types= ["Rational", "Reciprocity"]
subtask_queue_size = ["subtask queue size 0", "subtask queue size 1", "subtask queue size 2", "subtask queue size 3", "subtask queue size 4", "subtask queue size 5"]

path = []
for ex_type in ex_types:
    path.append("csv/" + ex_type + "/Environment.csv")

dfs = []
for p in path:
    df = pd.read_csv(p, index_col=0)
    df["success rate"] = df["executed task"] / (df["executed task"] + df["wasted task"] + df["rejected task"] + df["overflowed task"])
    dfs.append(df)

for c in col:
    
    fig = plt.figure()
    ax = fig.add_subplot(1,1,1)

    for df in dfs:
        df.plot(ax=ax, y=[c])
    
    ax.legend(ex_types)
    plt.xlabel('tick')
    plt.ylabel(c)
    plt.title("Environment")
    plt.tight_layout()
    plt.savefig("pdf/all/"+ c +".pdf")
    plt.savefig("png/all/"+ c +".png")

ticks=[10000, 50000, 150000]
for tick in ticks:

    fig = plt.figure()
    ax = fig.add_subplot(1,1,1)
    combined_df = pd.DataFrame()
    for i, df in enumerate(dfs):
        combined_df[ex_types[i]] = df[subtask_queue_size].loc[tick]
    
    combined_df.plot(kind='bar')    
    plt.tight_layout()
    plt.savefig("png/all/" + str(tick) + ".png")

plt.close('all')
