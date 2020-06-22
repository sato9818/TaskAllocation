
import matplotlib as mpl
import matplotlib.pyplot as plt
import math


import pandas as pd
import numpy as np
import datetime as dt

# plt.rcParams["font.size"] = 8
col = ["executed task", "wasted task", "communication time", "executed time", "waiting time", "all executed time", "leader count", "member count", "reciprocity leader count", "reciprocity member count", "message count", "overflowed task", "average subtask queue size"]

path = "csv/Environment.csv"

df = pd.read_csv(path, index_col=0)

for c in col:

    df.plot(y=[c], legend=False, title="Environment") 
    plt.xlabel('tick')
    plt.ylabel(c)
    plt.tight_layout()
    plt.savefig("pdf/all/"+ c +".pdf")

plt.close('all')


