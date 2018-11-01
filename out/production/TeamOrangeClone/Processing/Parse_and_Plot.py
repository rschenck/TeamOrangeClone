import os
import sys
import matplotlib.pyplot as plt
import numpy as np


def PullAndParseSource():
    with open('test.txt', 'r') as inputFile:
        lines = [line.replace("\n","") for line in inputFile.readlines()]

    time = []
    tcrPops = []
    immunogenicPops = []
    totalPop = []
    for item in lines:
        item = item.split("\t")
        time.append( int(item[0]) )
        totalPop.append(int(item[3]))
        immunogenicPops.append([int(val) for val in item[2].replace("[","").replace("]","").split(",")])
        tcrPops.append([int(val) for val in item[1].replace("[","").replace("]","").split(",")])

    return(time, tcrPops, immunogenicPops, totalPop)

def Plot(time, tcrPops, immunogenicPops, totalPop):
    immunogenicPops = np.asarray(immunogenicPops)
    immunogenicSum=np.sum(immunogenicPops, axis=1)
    antigenic = np.asarray(totalPop)-immunogenicSum

    fig, ax = plt.subplots()
    ax.plot(time, immunogenicSum)
    ax.plot(time, antigenic)
    plt.semilogy()
    ax.set_xlim(left=0, right=max(time))
    plt.xlabel("Time (hrs.)")
    plt.ylabel("Population Size (log)")
    plt.title("Population Dynamics")
    ax.legend(['Immunogenic-Population','Non-immunogenic population'])
    plt.show()

    fig2, ax2 = plt.subplots()
    tcrPops = np.asarray(tcrPops)
    immunogenicPops = np.asarray(immunogenicPops)
    ax2.plot(time, tcrPops, linestyle="dashed")
    ax2.plot(time, immunogenicPops)
    ax2.set_xlim(left=450, right=max(time))
    plt.xlabel("Time (hrs.)")
    plt.ylabel("Population Size")
    plt.title("Immunogenicity Dynamics")
    plt.show()


def main():
    time, tcrPops, immunogenicPops, totalPop = PullAndParseSource()

    print("Time\t%s"%('\t'.join(["TCR%s"%(val) for val in range(1,101)])))
    for i,item in enumerate(tcrPops):
        print("%s\t%s"%(time[i],'\t'.join([str(val) for val in item])))

    Plot(time, tcrPops, immunogenicPops, totalPop)

if __name__=="__main__":
    main()