library(ggplot2)
library(reshape2)
# library(EvoFreq)
library(colorspace)
library(colormap)
library(gridExtra)
library(gganimate)
library(scales)
library(ineq)

# library(devtools)
# library(RCurl)
# library(httr)
# set_config( config( ssl_verifypeer = 0L ) )
# devtools::install_github("dgrtwo/gganimate")

yCumulativeFrequency <- function(x){
  x <- sort(x, decreasing=T)
  y = data.frame(matrix(NA, ncol=2, nrow=0))
  val = 0
  for(i in 1:length(x)){
    val=val+x[i]
    y = rbind(y,data.frame(i, val))
  }
  return(y)
}

# Step 1 pull in the data
# args = commandArgs(trailingOnly=TRUE)
# prefix <- args[1] prefix of the files (leave off the number if ind =="ind")
# ind <- args[2] whether to give individual plots (ind)

prefix <- "/Users/rschenck/Desktop/IMO_WORKSHOP_8/TeamOrangeClone/Processing/Data/CASE3.666ant.50immuno.rep_1."
prefix <- "/Users/rschenck/Desktop/IMO_WORKSHOP_8/TeamOrangeClone/NoPDL1."
ind <- "no"

# Step 2 Get the files.
totalPops <- read.csv(paste(prefix, "totalPops.txt",sep=""), header = T, sep="\t")
immunoPops <- read.csv(paste(prefix, "immunoPops.txt",sep=""), header = T, sep="\t")
TCRpops <- read.csv(paste(prefix, "TCRpops.txt",sep=""), header = T, sep="\t")
totalInactive <- read.csv(paste(prefix, "totalInactivated.txt",sep=""), header = T, sep="\t")

# Population Size Plot
immunogenicPop <- rowSums(immunoPops[,seq(2,length(immunoPops))])
totalPops$Immunogenic <- immunogenicPop
totalPops$NonImmunogenic <- totalPops$TotalPop-immunogenicPop
dfPlot <- melt(totalPops, id.vars=c("Time"))
pops <- ggplot(dfPlot, aes(x=Time/24, y=value, colour=variable)) + geom_line(size=1) +
  scale_y_log10(labels = trans_format("log10", math_format(10^.x))) +
  scale_x_continuous(expand=c(0,0)) + 
  theme_bw() + guides(colour=guide_legend(title="Population")) + theme(panel.grid=element_blank()) +
  # scale_colour_manual(values=c("Red","Blue","Green","grey"),labels=c("Total","Immunogenic","Non-Immunogenic","PDL1")) + 
  xlab("Time (Days)") + ylab("Population Size") + scale_color_brewer(palette = "Set2") + theme(text = element_text(size=18))
pops

# Population of TCRs
dfImmunoPlot <- melt(immunoPops, id.vars=c('Time'))
set.seed(42)
c_pallete <- sample(colormap(nshades = length(unique(dfImmunoPlot$variable)),colormap = 'rainbow-soft'))
immunogenicPlot <- ggplot(dfImmunoPlot, aes(x=Time/24, y=value, colour=variable)) + geom_line() +
  scale_y_log10(labels = trans_format("log10", math_format(10^.x))) +
  scale_x_continuous(expand=c(0,0)) + 
  theme_bw() + guides(colour=guide_legend(title="Population")) + theme(panel.grid=element_blank()) +
  # scale_colour_manual(values=c("Red","Blue","Green","grey"),labels=c("Total","Immunogenic","Non-Immunogenic","PDL1")) + 
  xlab("Time (Days)") + ylab("Population Size") + guides(colour=F) + scale_color_manual(values=c_pallete) + theme(text = element_text(size=18))
immunogenicPlot

dfTCRPops <- melt(TCRpops, id.vars=c('Time'))
TCRplot <- ggplot(dfTCRPops, aes(x=Time/24, y=value, colour=variable), linetype=3) + geom_line(linetype = "longdash") +
  scale_y_log10(labels = trans_format("log10", math_format(10^.x))) +
  scale_x_continuous(expand=c(0,0)) + 
  theme_bw() + guides(colour=guide_legend(title="Population")) + theme(panel.grid=element_blank()) +
  # scale_colour_manual(values=c("Red","Blue","Green","grey"),labels=c("Total","Immunogenic","Non-Immunogenic","PDL1")) + 
  xlab("Time (Days)") + ylab("Population Size") + guides(colour=F) + scale_color_manual(values=c_pallete) + theme(text = element_text(size=18))
TCRinactive <- melt(totalInactive, id.vars=c('Time'))
TCRinactiveplot <- ggplot(TCRinactive, aes(x=Time/24, y=value, colour=variable), linetype=3) + geom_line(linetype = "longdash") +
  scale_y_log10(labels = trans_format("log10", math_format(10^.x))) +
  scale_x_continuous(expand=c(0,0)) + 
  theme_bw() + guides(colour=guide_legend(title="Population")) + theme(panel.grid=element_blank()) +
  # scale_colour_manual(values=c("Red","Blue","Green","grey"),labels=c("Total","Immunogenic","Non-Immunogenic","PDL1")) + 
  xlab("Time (Days)") + ylab("Population Size") + guides(colour=F) + scale_color_manual(values=c_pallete) + theme(text = element_text(size=18))
grid.arrange(TCRplot,TCRinactiveplot,ncol=1)

# Get Frequency Dynamics OverTime
dfMelt <- melt(TCRpops,id.vars=c("Time"))
dfMelt$Time2 <- dfMelt$Time
p1 <- ggplot(data=dfMelt, aes(x=Time,y=value, fill=variable)) + geom_bar(stat="identity",position="fill") +
  scale_fill_manual(values=c_pallete) + scale_y_continuous(expand=c(0,0)) + scale_x_continuous(expand=c(0,0), breaks=seq(0,max(dfMelt$Time),100)) +
  xlab("Time (hrs.)") + ylab("Frequency of TCR") + theme_bw() + guides(fill=F, colour=F) + theme(text = element_text(size=18))

# Get Cumulative Frequency Dynamics
plotDf <- as.data.frame(matrix(NA, ncol=3))
colnames(plotDf) <- c("Species", "Cumulative", "Time")
for(i in 1:length(unique(dfMelt$Time))){
  dfTimepoint = subset(dfMelt, dfMelt$Time==i)
  sumTCRs = dfTimepoint$value/sum(dfTimepoint$value)
  measureDiff <- data.frame(Species=seq(0,50),Cumulative=c(0,yCumulativeFrequency(sumTCRs)$val), Time=rep(i,51))
  plotDf <- rbind(plotDf, measureDiff)
}

plotDf <- plotDf[seq(2,length(plotDf$Species)),]
p2<-ggplot(data=plotDf, aes(x=Species, y=Cumulative)) + geom_segment(aes(x = 0, y = 0, xend = 50, yend = 1), size=1.5, color="maroon", linetype=2) + 
  geom_point(size=1) + theme_minimal() + scale_y_continuous(expand = c(0,0)) + 
  scale_x_continuous(expand = c(0,0)) + ylab("Cumulative Frequency") + transition_time(time=plotDf$Time) + theme(text = element_text(size=18))
animate(p2)

#Gini Index
ginivalues <- list()
for(i in 1:length(unique(plotDf$Time))){
  tmp <- subset(plotDf, plotDf$Time==unique(plotDf$Time)[i])
  ginivalues[i] <- 1-ineq(tmp$Cumulative, type="Gini")
}

giniDf <- data.frame(Time = TCRpops$Time , Gini_Index=unlist(ginivalues)/50)

p3 <- ggplot(giniDf, aes(x=Time, y=Gini_Index)) + geom_line() + theme_bw() + scale_y_continuous(expand=c(0,0)) + scale_x_continuous(expand=c(0,0), breaks=seq(0,max(dfMelt$Time),100)) +
  xlab("Time (hrs.)") + ylab("1-(Gini_Coefficient)") + theme(text = element_text(size=18))

grid.arrange(p1, p3, ncol=1)


# Example Plots
dfTimepoint = subset(dfMelt, dfMelt$Time==1310)
sumTCRs = dfTimepoint$value/sum(dfTimepoint$value)
measureDiff <- data.frame(Species=seq(0,50),Cumulative=c(0,yCumulativeFrequency(sumTCRs)$val), Time=rep(530,51))
plotDf <- plotDf[seq(2,length(plotDf$Species)),]
p2<-ggplot(data=measureDiff, aes(x=Species, y=Cumulative)) + geom_segment(aes(x = 0, y = 0, xend = 50, yend = 1), size=1.5, color="maroon", linetype=2) + 
  geom_line(size=1) + theme_bw() + scale_y_continuous(expand = c(0,0)) + theme(text = element_text(size=18))+
  scale_x_continuous(expand = c(0,0)) + ylab("Cumulative Frequency")


