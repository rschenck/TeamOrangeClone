library(ggplot2)
library(reshape2)
library(EvoFreq)

df <- read.csv("~/Desktop/TCR_OverTime.txt",head=T,sep="\t")
clone_pops <- data.frame(t(df[,seq(2,11)]))
colnames(clone_pops) <- df$Time

parents = rep(0,10)
clone_list <- row.names(clone_pops)
time_pts <- df$Time
c_pallete <- colormap(nshades = length(clone_list),colormap = 'rainbow')
c_pallete <- sample(c_pallete)
specplot(c_pallete)

tcr_pos_df <- get_freq_dynamics(clone_pops, clones = seq(1,length(clone_list)), parents = rep(0,10), scale_by_sizes_at_time = T)




dfMelt <- melt(df,id.vars=c("Time"))
ggplot(data=dfMelt, aes(x=Time,y=value, fill=variable)) + geom_bar(stat="identity",position="fill") + 
  scale_fill_manual(values=c_pallete) + scale_y_continuous(expand=c(0,0)) + scale_x_continuous(expand=c(0,0), breaks=seq(0,max(time_pts),50)) +
  xlab("Time (hrs.)") + ylab("Frequency of TCR") + theme_minimal() + geom_vline(xintercept=530)


# Subset for diversity
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

dfDefault = subset(dfMelt, dfMelt$Time==1)
sumTCRs = dfDefault$value/sum(dfDefault$value)
measureEven <- data.frame(Species=seq(0,10),Cumulative=c(0,yCumulativeFrequency(sumTCRs)$val))
ggplot(data=measureEven, aes(x=Species, y=Cumulative)) + geom_segment(aes(x = 0, y = 0, xend = 10, yend = 1), size=1.5, color="maroon", linetype=2) + geom_line(size=1) +
  theme_bw() + scale_y_continuous(expand = c(0,0)) + scale_x_continuous(expand = c(0,0)) + ylab("Cumulative Frequency")

dfTimepoint = subset(dfMelt, dfMelt$Time==530)
sumTCRs = dfTimepoint$value/sum(dfTimepoint$value)
measureDiff <- data.frame(Species=seq(0,10),Cumulative=c(0,yCumulativeFrequency(sumTCRs)$val))
ggplot(data=measureDiff, aes(x=Species, y=Cumulative)) + geom_segment(aes(x = 0, y = 0, xend = 10, yend = 1), size=1.5, color="maroon", linetype=2) + geom_line(size=1) +
  theme_bw() + scale_y_continuous(expand = c(0,0)) + scale_x_continuous(expand = c(0,0)) + ylab("Cumulative Frequency")

dfTimepoint = subset(dfMelt, dfMelt$Time==700)
sumTCRs = dfTimepoint$value/sum(dfTimepoint$value)
measureDiff <- data.frame(Species=seq(0,10),Cumulative=c(0,yCumulativeFrequency(sumTCRs)$val))
ggplot(data=measureDiff, aes(x=Species, y=Cumulative)) + geom_segment(aes(x = 0, y = 0, xend = 10, yend = 1), size=1.5, color="maroon", linetype=2) + geom_line(size=1) +
  theme_bw() + scale_y_continuous(expand = c(0,0)) + scale_x_continuous(expand = c(0,0)) + ylab("Cumulative Frequency")

