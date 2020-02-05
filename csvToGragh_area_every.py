# グラフ化に必要なものの準備
import matplotlib as mpl
import matplotlib.pyplot as plt
import math

# データの扱いに必要なライブラリ
import pandas as pd
import numpy as np
import datetime as dt

size = 4
hei = int(math.sqrt(size))
wid = int(math.sqrt(size))
plt.rcParams["font.size"] = 8

path = "test2.csv"
df = pd.read_csv(path, index_col=0)

fig, axes = plt.subplots(nrows=hei, ncols=wid, figsize=(9, 6))

for i in range(hei):
    for j in range(wid):
        ax=axes[i, j]
        plt.axes(ax)
        plt.xlabel('tick')
        plt.ylabel('execution task')
        
        df.plot(y=[str(i + j + 1)],ax=axes[i, j], legend=False)
        
        
plt.tight_layout()
plt.savefig('eps/area/area_execution_task.png')

fig, axes = plt.subplots(nrows=hei, ncols=wid, figsize=(9, 6))

for i in range(hei):
    for j in range(wid):
        ax=axes[i, j]
        plt.axes(ax)
        plt.xlabel('tick')
        plt.ylabel('waste task')
        df.plot(y=[str(i + j + 1) + "w"],ax=axes[i, j], legend=False)
        
plt.tight_layout()
plt.savefig('eps/area/area_waste_task.png')

path = "ExecutedTime.csv"
df = pd.read_csv(path, index_col=0)

fig, axes = plt.subplots(nrows=hei, ncols=wid, figsize=(9, 6))

for i in range(hei):
    for j in range(wid):
        ax=axes[i, j]
        plt.axes(ax)
        plt.xlabel('tick')
        plt.ylabel('execution time')
        
        df.plot(y=[str(i + j + 1)],ax=axes[i, j], legend=False)
        
        
plt.tight_layout()
plt.savefig('eps/area/area_execution_time.png')

path = "Agents.csv"
df = pd.read_csv(path, index_col=0)

fig, axes = plt.subplots(nrows=hei, ncols=wid, figsize=(9, 6))

for i in range(hei):
    for j in range(wid):
        ax=axes[i, j]
        plt.axes(ax)
        plt.xlabel('tick')
        plt.ylabel('count')
        
        df.plot(y=["leader:" + str(i + j + 1), "member:" + str(i + j + 1)],ax=axes[i, j], legend=True)
        
        
plt.tight_layout()
plt.savefig('eps/area/area_agents_count.png')

path = "communicationtime.csv"
df = pd.read_csv(path, index_col=0)

fig, axes = plt.subplots(nrows=hei, ncols=wid, figsize=(9, 6))

for i in range(hei):
    for j in range(wid):
        ax=axes[i, j]
        plt.axes(ax)
        plt.xlabel('tick')
        plt.ylabel('Communication time')
        
        df.plot(y=[str(i + j + 1)],ax=axes[i, j], legend=True)
        
        
plt.tight_layout()
plt.savefig('eps/area/area_communication_time.png')

path = "LeaderMember.csv"
df = pd.read_csv(path, index_col=0)

fig, axes = plt.subplots(nrows=hei, ncols=wid, figsize=(11, 6))


count = 0
for i in range(hei):
    for j in range(wid):
        col = []
        for k in range(size):
            col.append(str(count) + "_" + str(k))
        ax=axes[i, j]
        plt.axes(ax)
        plt.xlabel('tick')
        plt.ylabel('count')
        
        
        df.plot(y=col,ax=axes[i, j])
        plt.legend(bbox_to_anchor=(1.05, 1), loc='upper left', borderaxespad=0, fontsize=8)
        count = count + 1
        
        
plt.tight_layout()
plt.savefig('eps/area/area_member_count.png')

plt.close('all')

