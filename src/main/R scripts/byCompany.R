library(lattice)
setwd("C:\\Users\\Leon Bornemann\\git\\EpisodePatternMining\\resources\\AveragedResults\\")

#by company

fbswc = read.table("FBSWC_byCompany.csv",header=T,sep=",")
perms = read.table("PERMS_byCompany.csv",header=T,sep=",")
fbswc$method = "fbswc"
perms$method = "perms"
totalTable = rbind(fbswc,perms)

colors = c("purple","blue")

barchart(return*100~company,
         data=totalTable,
         groups=method,
         par.settings=list(superpose.polygon=list(col=colors)),
         origin = 0,
         xlab = "companies",
         ylab = "total Return [%]",
         ylim = c(0,200),
         auto.key=list(space="top", columns=2, cex.title=1),
         scales=list(x=list(rot=90)),
         main="Return by Company"
         )

barchart(Precision_UP*100~company,
         data=totalTable,
         groups=method,
         par.settings=list(superpose.polygon=list(col=colors)),
         origin = 50,
         xlab = "companies",
         ylab = "Precision {UP} [%]",
         auto.key=list(space="top", columns=2, cex.title=1),
         scales=list(x=list(rot=90)),
         main="Precision {UP} by Company"
)

barchart(Precision_DOWN*100~company,
         data=totalTable,
         groups=method,
         par.settings=list(superpose.polygon=list(col=colors)),
         origin = 50,
         xlab = "companies",
         ylab = "Precision {DOWN} [%]",
         auto.key=list(space="top", columns=2, cex.title=1),
         scales=list(x=list(rot=90)),
         main="Precision {DOWN} by Company"
)

barchart(PrecisionIgnoreEqual_UP*100~company,
         data=totalTable,
         groups=method,
         par.settings=list(superpose.polygon=list(col=colors)),
         origin = 50,
         xlab = "companies",
         ylab = "Precision Ignore Equal {UP} [%]",
         auto.key=list(space="top", columns=2, cex.title=1),
         scales=list(x=list(rot=90)),
         main="Precision Ignore Equal {UP} by Company"
)

barchart(PrecisionIgnoreEqual_DOWN*100~company,
         data=totalTable,
         groups=method,
         par.settings=list(superpose.polygon=list(col=colors)),
         origin = 50,
         xlab = "companies",
         ylab = "Precision Ignore Equal {DOWN} [%]",
         auto.key=list(space="top", columns=2, cex.title=1),
         scales=list(x=list(rot=90)),
         main="Precision Ignore Equal {DOWN} by Company"
)

#by day

fbswc = read.table("FBSWC.csv",header=T,sep=",")
perms = read.table("PERMS.csv",header=T,sep=",")
fbswc$method = "fbswc"
perms$method = "perms"
totalTable = rbind(fbswc,perms)

barchart(avgReturn*100~date,
         data=totalTable,
         groups=method,
         par.settings=list(superpose.polygon=list(col=colors)),
         origin = 0,
         xlab = "time [days]",
         ylab = "average Return [%]",
         auto.key=list(space="top", columns=2, cex.title=1),
         scales=list(x=list(rot=90)),
         main="Average Return per Day"
)

#prepare the moving averages:
windowSize = 5
permsmA = as.numeric(ma(totalTable[totalTable$method == "perms",]$avgReturn,windowSize))*100
permsDate = totalTable$date[totalTable$method == "perms"]
fbswcmA = as.numeric(ma(totalTable[totalTable$method == "fbswc",]$avgReturn,windowSize))*100
fbswcmDate = totalTable$date[totalTable$method == "fbswc"]

fbswcMatrix = data.frame("ma" = fbswcmA,"date" = fbswcmDate, "method" = "fbswc")
permsMatrix = data.frame("ma" = permsmA,"date" = permsDate, "method" = "perms")
maDf = rbind(permsMatrix,fbswcMatrix)

xyplot(ma~date,
  data = maDf,
  groups=method,
  par.settings=list(superpose.polygon=list(col=colors)),
  lwd = 3,
  origin = 0,
  xlab = "time [days]",
  ylab = "moving avg of Return [%]",
  auto.key=list(space="top", columns=2, cex.title=1),
  scales=list(x=list(rot=90)),
  main="Average Daily Return decay over time",
  type = "l"
)
