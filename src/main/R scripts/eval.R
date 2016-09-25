library(zoo)
library(timeDate)
library(forecast)

library(lattice)
barchart(Species~Reason,data=Reasonstats,groups=Catergory, 
         scales=list(x=list(rot=90,cex=0.8)))

drawReturnGraphs = function(table,methodName){
  yVals = table$avgReturn * 100
  barplot(yVals, width = 1,col = ifelse(yVals < 0,'red','green'), xlab = "time [days]",ylab = "avg. Return [%]",main = methodName)
  maVals = ma(na.omit(yVals),5)
  plot(x= 1:length(maVals),y = maVals,type = "l", ylim=c(-3,3),xlab = "time [days]",ylab="moving avg of Return [%]",main = methodName)
}

drawGraphs = function(vals,name,methodName){
  yVals = vals * 100
  barplot(yVals, width = 1, xlab = "time [days]",ylab = paste(name,"[%]"), ylim = c(0,100),main = paste(methodName,"-",name))
  maVals = ma(na.omit(yVals),5)
  plot(x= 1:length(maVals),y = maVals,type = "l", ylim=c(0,100),xlab = "time [days]",ylab=paste("moving avg of",name,"[%]"),main = paste(methodName,"-",name))
}

drawAllWGraphs = function(fileName,methodName){
  table = read.table(fileName,header=T,sep=",")
  drawReturnGraphs(table,methodName)
  drawGraphs(table$avgPrecision_UP,"avg. Precision for UP",methodName)
  drawGraphs(table$avgPrecision_DOWN,"avg. Precision for DOWN",methodName)
  drawGraphs(table$avgPrecisionIgnoreEqual_UP,"avg. Precision for Up (ignore equal)",methodName)
  drawGraphs(table$avgPrecisionIgnoreEqual_DOWN,"avg. Precision for DOWN (ignore equal)",methodName)
}
#setwd("C:\\Users\\Leon Bornemann\\git\\EpisodePatternMining\\resources\\results\\AAPL\\")
#table = read.table("resultsAsCSV_FBSWC.csv",header = T, sep = ",")
#yVals = table$returnOfInvestment
#plot(x = 1:nrow(a), y = yVals, type = "h",col = ifelse(yVals < 0,'red','green'))
#barplot(yVals, width = 1,col = ifelse(yVals < 0,'red','green'))

setwd("C:\\Users\\Leon Bornemann\\git\\EpisodePatternMining\\resources\\AveragedResults\\")
fsbw = read.table("FBSWC.csv",header=T,sep=",")
perms = read.table("PERMS.csv",header=T,sep=",")
fsbw$method = "fsbw"
perms$method = "perms"
totalTable = rbind(fsbw,perms)

library(lattice)
barchart(avgReturn*100~date,data=totalTable,groups=method,col=c("orange","yellow"),origin = 0,xlab = "time [days]",ylab = "avg. Return [%]",scales=list(x=list(rot=90)))
yVals = totalTable$avgReturn * 100
maVals = ma(yVals,5)
#drawAllWGraphs("FBSWC.csv","FBSWC")
#drawAllWGraphs("PERMS.csv","PERMS")