library(ggplot2)
library(reshape2)
library(EvoFreq)
library(colorspace)
library(colormap)
library(gridExtra)
library(gganimate)

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

df <- read.csv("~/Desktop/TCR_OverTime.txt",head=T,sep="\t")
clone_pops <- data.frame(t(df[,seq(1,length(df))]))
colnames(clone_pops) <- df$Time

clone_pops[1,] <- 0
fudgefactor <- rep(0,101)
parents = rep(1,101)
clone_list <- row.names(clone_pops)
time_pts <- df$Time
c_pallete <- colormap(nshades = length(clone_list),colormap = 'rainbow')
c_pallete <- sample(c_pallete)
specplot(c_pallete)

# tcr_pos_df <- get_freq_dynamics(clone_pops, clones = clone_list, parents = parents, scale_by_sizes_at_time = F)


dfMelt <- melt(df,id.vars=c("Time"))
p1 <- ggplot(data=dfMelt, aes(x=Time,y=value, fill=variable)) + geom_bar(stat="identity",position="fill") +
  scale_fill_manual(values=c_pallete) + scale_y_continuous(expand=c(0,0)) + scale_x_continuous(expand=c(0,0), breaks=seq(0,max(time_pts),50)) +
  xlab("Time (hrs.)") + ylab("Frequency of TCR") + theme_minimal() + guides(fill=F, colour=F)


# Subset for diversity
plotDf <- as.data.frame(matrix(NA, ncol=3))
colnames(plotDf) <- c("Species", "Cumulative", "Time")
for(i in 1:length(unique(dfMelt$Time))){
  dfTimepoint = subset(dfMelt, dfMelt$Time==i)
  sumTCRs = dfTimepoint$value/sum(dfTimepoint$value)
  measureDiff <- data.frame(Species=seq(0,100),Cumulative=c(0,yCumulativeFrequency(sumTCRs)$val), Time=rep(i,101))
  plotDf <- rbind(plotDf, measureDiff)
}
dfTimepoint = subset(dfMelt, dfMelt$Time==530)
sumTCRs = dfTimepoint$value/sum(dfTimepoint$value)
measureDiff <- data.frame(Species=seq(0,100),Cumulative=c(0,yCumulativeFrequency(sumTCRs)$val), Time=rep(530,101))

p2 <- ggplot(data=plotDf, aes(x=Species, y=Cumulative)) + geom_segment(aes(x = 0, y = 0, xend = 100, yend = 1), size=1.5, color="maroon", linetype=2) + 
  geom_line(size=1) + theme_bw() + scale_y_continuous(expand = c(0,0)) + 
  scale_x_continuous(expand = c(0,0)) + ylab("Cumulative Frequency") + transition_time(Time)

gganimate(p2)
gganimate::animate(p2)

lay = rbind(c(1,1,1,1,1,1,1,1),
            c(NA,NA,NA,2,2,NA,NA,NA))

grid.arrange(p1, p2, layout_matrix=lay)

