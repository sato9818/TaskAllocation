import matplotlib.pyplot as plt
import pandas as pd
import os
import shutil

shutil.rmtree('png/all')
os.mkdir('png/all')
shutil.rmtree('pdf/all')
os.mkdir('pdf/all')

col = [ \
    "Num of completed task", "Num of team formation failure", "Average communication time", "Average subtask completion time from member perspective", "Average time of subtask being in subtask queue", \
    "Average subtask completion time from leader perspective", "Average task completion time","Num of leaders", "Num of members", \
    "Num of reciprocal leaders", "Num of reciprocal members", "Num of sent messages", "Num of overflowed task from task queue", \
    "Average subtask queue size", "Average leader dependable agents type 0", "Average leader dependable agents type 1", \
    "Average leader dependable agents type 2", "Average member dependable agents", "Num of rejected task", "Task completion success rate", "Average of leader threshold", "Average of member threshold" \
    ]
ex_types= ["Rational", "Reciprocity"]
subtask_queue_size = ["Num of members whose subtask queue size is 0", "Num of members whose subtask queue size is 1", "Num of members whose subtask queue size is 2", "Num of members whose subtask queue size is 3", "Num of members whose subtask queue size is 4", "Num of members whose subtask queue size is 5"]

path = []
for ex_type in ex_types:
    path.append("csv/" + ex_type + "/Environment.csv")

dfs = []
for p in path:
    df = pd.read_csv(p, index_col=0)
    # df["success rate"] = df["executed task"] / (df["executed task"] + df["wasted task"] + df["rejected task"] + df["overflowed task"])
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

plt.close('all')

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
