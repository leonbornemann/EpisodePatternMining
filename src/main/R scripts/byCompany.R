filterOutSectors = function(x){
  sectorCodes = c("Miscellaneous","Finance","Transportation","Consumer Services","Capital Goods","Public Utilities",
                  "Basic Industries","Health Care","Energy","Consumer Durables","Technology","Consumer Non-Durables")
  result = logical(length(x))
  for(i in 1:length(x)){
    result[i] = all(x[i] != sectorCodes)
  }
  return(result)
}

library(lattice)
setwd("C:\\Users\\Leon Bornemann\\Desktop\\base Folder\\Final Runs\\Support Serial Modified\\Run 2\\resources\\AveragedResults\\")

#by company

fbswc = read.table("FBSWC_byCompany.csv",header=T,sep=",")
perms = read.table("PERMS_byCompany.csv",header=T,sep=",")
fbswc$method = "fbswc"
perms$method = "perms"
totalTable = rbind(fbswc,perms)
totalTable = totalTable[filterOutSectors(totalTable$company),]

colors = c("purple","blue")

mean(fbswc$return)
mean(fbswc$smoothedReturn)
mean(perms$smoothedReturn)
mean(perms$return)

sd(fbswc$return)
sd(fbswc$smoothedReturn)
sd(perms$smoothedReturn)
sd(perms$return)

boxplot(return*100~method,data=totalTable,
        ylab="Total Return [%]",ylim = c(-250,500))
boxplot(AccuracyIngoreEqual*100~method,data=totalTable,
        ylab="Accuracy [%]")

barchart(AccuracyIngoreEqual*100~company,
         data=totalTable,
         groups=method,
         par.settings=list(superpose.polygon=list(col=colors)),
         origin = 50,
         ylab = list(label="Accuracy [%]",cex = 1.5),
         auto.key=list(space="top", columns=2, cex.title=1),
         scales=list(x=list(rot=90,cex = 1.1),y=list(rot=0,cex=1.2)),
         main=list(label = "Accuracy by Company",cex = 1.25)
)

barchart(return*100~company,
         data=totalTable,
         groups=method,
         par.settings=list(superpose.polygon=list(col=colors)),
         origin = 0,
         #xlab = list(label="companies",cex = 1.5),
         ylab = list(label="Total Return [%]",cex = 1.5),
         auto.key=list(space="top", columns=2, cex.title=1.25),
         scales=list(x=list(rot=90,cex = 1.1),y=list(rot=0,cex=1.2)),
         main=list(label = "Return by Company",cex = 1.25),
         ylim = c(-260,550)
         )

barchart(smoothedReturn*100~company,
         data=totalTable,
         groups=method,
         par.settings=list(superpose.polygon=list(col=colors)),
         origin = 0,
         #xlab = list(label="companies",cex = 1.5),
         ylab = list(label="Total Corrected Return [%]",cex = 1.5),
         auto.key=list(space="top", columns=2, cex.title=1.25),
         scales=list(x=list(rot=90,cex = 1.1),y=list(rot=0,cex=1.2)),
         main=list(label = "Corrected Return by Company",cex = 1.25),
         ylim = c(-260,550)
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
         main="A - Precision {UP} by Company"
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
         main="A - Precision {DOWN} by Company"
)

barchart(Accuracy*100~company,
         data=totalTable,
         groups=method,
         par.settings=list(superpose.polygon=list(col=colors)),
         origin = 50,
         xlab = "companies",
         ylab = "Accuracy [%]",
         auto.key=list(space="top", columns=2, cex.title=1),
         scales=list(x=list(rot=90)),
         main="A - Accuracy by Company"
)

barchart(PrecisionIgnoreEqual_UP*100~company,
         data=totalTable,
         groups=method,
         par.settings=list(superpose.polygon=list(col=colors)),
         origin = 50,
         xlab = "companies",
         ylab = "Precision {UP} [%]",
         auto.key=list(space="top", columns=2, cex.title=1),
         scales=list(x=list(rot=90)),
         main="IE_A - Precision {UP} by Company"
)

barchart(PrecisionIgnoreEqual_DOWN*100~company,
         data=totalTable,
         groups=method,
         par.settings=list(superpose.polygon=list(col=colors)),
         origin = 50,
         xlab = "companies",
         ylab = "Precision {DOWN} [%]",
         auto.key=list(space="top", columns=2, cex.title=1),
         scales=list(x=list(rot=90)),
         main="IE_A - Precision {DOWN} by Company"
)

barchart(AccuracyIngoreEqual*100~company,
         data=totalTable,
         groups=method,
         par.settings=list(superpose.polygon=list(col=colors)),
         origin = 50,
         xlab = "companies",
         ylab = "Accuracy (Ignore Equal) [%]",
         auto.key=list(space="top", columns=2, cex.title=1),
         scales=list(x=list(rot=90)),
         main="IE_A - Accuracy by Company"
)
#improved metrics:

barchart(ImprovedPrecision_UP*100~company,
         data=totalTable,
         groups=method,
         par.settings=list(superpose.polygon=list(col=colors)),
         origin = 50,
         xlab = "companies",
         ylab = "Precision {UP} [%]",
         auto.key=list(space="top", columns=2, cex.title=1),
         scales=list(x=list(rot=90)),
         main="B - Precision {UP} by Company"
)

barchart(ImprovedPrecision_DOWN*100~company,
         data=totalTable,
         groups=method,
         par.settings=list(superpose.polygon=list(col=colors)),
         origin = 50,
         xlab = "companies",
         ylab = "Precision {DOWN} [%]",
         auto.key=list(space="top", columns=2, cex.title=1),
         scales=list(x=list(rot=90)),
         main="B - Precision {DOWN} by Company"
)

barchart(ImprovedAccuracy*100~company,
         data=totalTable,
         groups=method,
         par.settings=list(superpose.polygon=list(col=colors)),
         origin = 50,
         xlab = "companies",
         ylab = "B - Accuracy [%]",
         auto.key=list(space="top", columns=2, cex.title=1),
         scales=list(x=list(rot=90)),
         main="B - Accuracy by Company"
)

barchart(ImprovedPrecisionIgnoreEqual_UP*100~company,
         data=totalTable,
         groups=method,
         par.settings=list(superpose.polygon=list(col=colors)),
         origin = 50,
         xlab = "companies",
         ylab = "Improved Precision Ignore Equal {UP} [%]",
         auto.key=list(space="top", columns=2, cex.title=1),
         scales=list(x=list(rot=90)),
         main="Improved Precision Ignore Equal {UP} by Company"
)

barchart(ImprovedPrecisionIgnoreEqual_DOWN*100~company,
         data=totalTable,
         groups=method,
         par.settings=list(superpose.polygon=list(col=colors)),
         origin = 50,
         xlab = "companies",
         ylab = "Improved Precision Ignore Equal {DOWN} [%]",
         auto.key=list(space="top", columns=2, cex.title=1),
         scales=list(x=list(rot=90)),
         main="Improved Precision Ignore Equal {DOWN} by Company"
)

barchart(ImprovedAccuracyIngoreEqual*100~company,
         data=totalTable,
         groups=method,
         par.settings=list(superpose.polygon=list(col=colors)),
         origin = 50,
         xlab = "companies",
         ylab = "Improved Accuracy (Ignore Equal) [%]",
         auto.key=list(space="top", columns=2, cex.title=1),
         scales=list(x=list(rot=90)),
         main="Improved Accuracy (Ignore Equal) by Company"
)



#by day

fbswc = read.table("FBSWC.csv",header=T,sep=",")
perms = read.table("PERMS.csv",header=T,sep=",")
colors = c("purple","blue")
fbswc$method = "fbswc"
perms$method = "perms"
totalTable = rbind(fbswc,perms)
lowDiscrepancyCompanies = c("ON","VA","TECH","FCS","CTSH","AAPL","ISSC","INTC","CSCO","SNDK") #,"VOD","MSFT","ELNK","EBAY","TXN","YHOO","CGEN"

barchart(avgAccuracyIngoreEqual*100~date,
         data=totalTable,
         groups=method,
         par.settings=list(superpose.polygon=list(col=colors)),
         origin = 50,
         xlab = "time [days]",
         ylab = "average Return [%]",
         auto.key=list(space="top", columns=2, cex.title=1),
         scales=list(x=list(rot=90)),
         main="Average Return per Day"
)

#prepare the moving averages:
library(zoo)
library(timeDate)
library(forecast)
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
