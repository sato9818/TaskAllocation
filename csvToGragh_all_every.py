# グラフ化に必要なものの準備
import matplotlib as mpl
import matplotlib.pyplot as plt
import math

# データの扱いに必要なライブラリ
import pandas as pd
import numpy as np
import datetime as dt

size = 9
hei = int(math.sqrt(size))
wid = int(math.sqrt(size))
plt.rcParams["font.size"] = 8

for i in range(2):
    path = "test2"+ str(i) +".csv"
    df = pd.read_csv(path, index_col=0)

    plt.figure()
    plt.xlabel('tick')
    plt.ylabel('execution task')
    df.plot(y=['excution task'])
    plt.tight_layout()
    plt.savefig('eps/change/execution_task'+ str(i) +'.png')

    # fig, axes = plt.subplots(nrows=hei, ncols=wid, figsize=(9, 6))

    # for i in range(hei):
    #     for j in range(wid):
    #         ax=axes[i, j]
    #         plt.axes(ax)
    #         plt.xlabel('tick')
    #         plt.ylabel('waste task')
    #         df.plot(y=[str(i + j + 1) + "w"],ax=axes[i, j], legend=False)
            
    # plt.tight_layout()
    # plt.savefig('eps/area_waste_task.eps')

    path = "ExecutedTime"+ str(i) +".csv"
    df = pd.read_csv(path, index_col=0)

    plt.figure()
    plt.xlabel('tick')
    plt.ylabel('execution time')
    df.plot(y=['ExecutedTime'])
    plt.tight_layout()
    plt.savefig('eps/change/execution_time'+ str(i) +'.png')
            

    path = "Agents"+ str(i) +".csv"
    df = pd.read_csv(path, index_col=0)

    plt.figure()
    plt.xlabel('tick')
    plt.ylabel('count')
    df.plot(y=["leader", "member"])
    plt.tight_layout()
    plt.savefig('eps/change/agents_count'+ str(i) +'.png')

    path = "communicationtime"+ str(i) +".csv"
    df = pd.read_csv(path, index_col=0)

    plt.figure()
    plt.xlabel('tick')
    plt.ylabel('Communication time')
    df.plot(y=["communication time"])
    plt.tight_layout()
    plt.savefig('eps/change/communication_time'+ str(i) +'.png')


plt.close('all')