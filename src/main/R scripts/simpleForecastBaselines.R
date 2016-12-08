
library(lattice)
setwd("C:\\Users\\Leon Bornemann\\Desktop\\base Folder\\Final Runs\\Average Forecasting\\Run 1\\resources\\AveragedResults\\")
totalTable = read.table("SimpleAverageForecasting_byCompany.csv",header=T,sep=",")
totalTable$method = "simple moving average"
setwd("C:\\Users\\Leon Bornemann\\Desktop\\base Folder\\Final Runs\\Support Serial Modified\\Run 2\\resources\\AveragedResults\\")
#by company
fbswc = read.table("FBSWC_byCompany.csv",header=T,sep=",")
perms = read.table("PERMS_byCompany.csv",header=T,sep=",")
fbswc$method = "fbswc"
perms$method = "perms"
totalTable = rbind(fbswc,perms,totalTable)


lowDiscrepancyCompanies = c("ON","VA","TECH","FCS","CTSH","AAPL","ISSC","INTC","CSCO","SNDK") #,"VOD","MSFT","ELNK","EBAY","TXN","YHOO","CGEN"
#totalTable = totalTable[totalTable$company %in% lowDiscrepancyCompanies,]
colors = c("purple","blue","orange")


boxplot(return*100~method,data=totalTable,
        ylab="Total Return [%]",ylim = c(-250,500))
boxplot(AccuracyIngoreEqual*100~method,data=totalTable,
        ylab="Accuracy (Ignore Equal) [%]")
means <- tapply(totalTable$AccuracyIngoreEqual,totalTable$method,mean)
means = means*100
points(means,col="red",pch=18)

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
         ylim = c(-20,250)
)