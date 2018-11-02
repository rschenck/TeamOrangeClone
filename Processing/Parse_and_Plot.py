import os
import sys
# import matplotlib.pyplot as plt
import numpy as np
import glob


def PullAndParseSource(fileName):
    with open(fileName, 'r') as inputFile:
        lines = [line.replace("\n","") for line in inputFile.readlines()]

    time = []
    tcrPops = []
    immunogenicPops = []
    totalPop = []
    totalInactive = []
    pdl1 = []
    for item in lines:
        item = item.split("\t")
        time.append( int(item[0]) )
        totalPop.append(int(item[3]))
        immunogenicPops.append([int(val) for val in item[2].replace("[","").replace("]","").split(",")])
        tcrPops.append([int(val) for val in item[1].replace("[","").replace("]","").split(",")])
        totalInactive.append([int(val) for val in item[4].replace("[","").replace("]","").split(",")])
        pdl1.append(int(item[5]))

    return(time, tcrPops, immunogenicPops, totalPop, totalInactive, pdl1)

def Plot(time, tcrPops, immunogenicPops, totalPop, totalInactive, pdl1):
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

def GetFiles(time, tcrPops, immunogenicPops, totalPop, totalInactive, pdl1, fileName):
    prefix = fileName.replace('.txt','')

    # TCR populations
    with open(prefix+'.TCRpops.txt', 'w') as outTCRs:
        outTCRs.write("Time\t%s\n" % ('\t'.join(["TCR%s" % (val) for val in range(1, len(tcrPops[0])+1)])))
        for i, item in enumerate(tcrPops):
            outTCRs.write("%s\t%s\n" % (time[i], '\t'.join([str(val) for val in item])))

    # immunogenicity
    with open(prefix+'.immunoPops.txt', 'w') as immunoOut:
        immunoOut.write("Time\t%s\n" % ('\t'.join(["ANT%s" % (val) for val in range(1, len(immunogenicPops[0])+1)])))
        for i, item in enumerate(immunogenicPops):
            immunoOut.write("%s\t%s\n" % (time[i], '\t'.join([str(val) for val in item])))

    # totalPop
    with open(prefix+'.totalPops.txt', 'w') as totalPopOut:
        totalPopOut.write("Time\tTotalPop\tTotalpdl1Pop\n")
        for i, item in enumerate(totalPop):
            totalPopOut.write("%s\t%s\t%s\n"%(time[i],totalPop[i],pdl1[i]))

    # totalInactive
    with open(prefix+'.totalInactivated.txt','w') as inactiveOut:
        inactiveOut.write("Time\t%s\n" % ('\t'.join(["inactive.TCR%s" % (val) for val in range(1, len(totalInactive[0])+1)])))
        for i, item in enumerate(totalInactive):
            inactiveOut.write("%s\t%s\n" % (time[i], '\t'.join([str(val) for val in item])))

def main():
    fileName = sys.argv[1]

    time, tcrPops, immunogenicPops, totalPop, totalInactive, pdl1 = PullAndParseSource(fileName)

    GetFiles(time, tcrPops, immunogenicPops, totalPop, totalInactive, pdl1, fileName)

    # Plot(time, tcrPops, immunogenicPops, totalPop, totalInactive, pdl1)

if __name__=="__main__":
    main()