library(ggplot2)
library(reshape2)
# library(EvoFreq)
library(colorspace)
library(colormap)
library(gridExtra)
library(gganimate)
library(scales)

# library(devtools)
# library(RCurl)
# library(httr)
# set_config( config( ssl_verifypeer = 0L ) )
# devtools::install_github("dgrtwo/gganimate")

# Step 1 pull in the data
# args = commandArgs(trailingOnly=TRUE)
# prefix <- args[1] prefix of the files (leave off the number if ind =="ind")
# ind <- args[2] whether to give individual plots (ind)

prefix <- "/Users/rschenck/Desktop/IMO_WORKSHOP_8/TeamOrangeClone/Processing/Data/CASE1.666ant.50immuno.rep_2."
ind <- "no"

c_pallete <- c("#3874b1", "#c6382c","#4f9f39", "#bdbe3a","#8e66ba","#f08627","#53bbce", "#d67bbf","#85584c", "#b2c5e6","#f39c97", "#a6de90","#dcdc93","#c2aed3","#f6bf7e","#a9d8e4","#eeb8d1","#be9d92","#c7c7c7","#7f7f7f")

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
  xlab("Time (Days)") + ylab("Population Size") + scale_color_brewer(palette = "Set2")
pops

# Population of TCRs
dfImmunoPlot <- melt(immunoPops, id.vars=c('Time'))
c_pallete <- sample(colormap(nshades = length(unique(dfImmunoPlot$variable)),colormap = 'rainbow-soft'))
immunogenicPlot <- ggplot(dfImmunoPlot, aes(x=Time/24, y=value, colour=variable)) + geom_line() +
  scale_y_log10(expand=c(0,0), limit=c(1,100)) + 
  scale_x_continuous(expand=c(0,0)) + 
  theme_bw() + guides(colour=guide_legend(title="Population")) + theme(panel.grid=element_blank()) +
  # scale_colour_manual(values=c("Red","Blue","Green","grey"),labels=c("Total","Immunogenic","Non-Immunogenic","PDL1")) + 
  xlab("Time (Days)") + ylab("Population Size") + guides(colour=F) + scale_color_manual(values=c_pallete)
immunogenicPlot




airq <- airquality
airq$Month <- format(ISOdate(2004,1:12,1),"%B")[airq$Month]
ggplot(airq, aes(Day, Temp, group = Month)) + 
  geom_line() + 
  geom_segment(aes(xend = 31, yend = Temp), linetype = 2, colour = 'grey') + 
  geom_point(size = 2) + 
  geom_text(aes(x = 31.1, label = Month), hjust = 0) + 
  transition_reveal(Month, Day) + 
  coord_cartesian(clip = 'off') + 
  labs(title = 'Temperature in New York', y = 'Temperature (°F)') + 
  theme_minimal() + 
  theme(plot.margin = margin(5.5, 40, 5.5, 5.5))

