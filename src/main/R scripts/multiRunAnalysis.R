getResultFrame = function(){
  fbswcResultPath = "\\resources\\AveragedResults\\FBSWC_byCompany.csv"
  permsResultPath = "\\resources\\AveragedResults\\PERMS_byCompany.csv"
  lowDiscrepancyCompanies = c("ON","VA","TECH","FCS","CTSH","AAPL","ISSC","INTC","CSCO","SNDK") #,"VOD","MSFT","ELNK","EBAY","TXN","YHOO","CGEN"
  dirs = list.dirs(recursive = FALSE)
  #dirs = Filter(notForbidden,dirs)
  size = length(dirs)*2
  resultFrame = data.frame(
    meanImprovedAccuracy = numeric(size),
    avgRet = numeric(size),
    type = character(size),
    run = character(size),
    smoothedRet = numeric(size),
    stringsAsFactors = FALSE
  )
  for(i in 1:length(dirs)){
    dir = dirs[i]
    #fbswc
    fbswc = read.csv(paste(dir,fbswcResultPath,sep = ""))
    fbswc = fbswc[fbswc$company %in% lowDiscrepancyCompanies,]
    resultFrame$avgRet[i*2-1] = mean(fbswc$return,na.rm = TRUE)
    resultFrame$smoothedRet[i*2-1] = mean(fbswc$smoothedReturn,na.rm = TRUE)
    resultFrame$meanImprovedAccuracy[i*2-1] = mean(fbswc$AccuracyIngoreEqual,na.rm = TRUE)
    resultFrame$run[i*2-1] = dir
    resultFrame$type[i*2-1] = "fbswc"
    #perms
    perms = read.csv(paste(dir,permsResultPath,sep = ""))
    perms = fbswc[perms$company %in% lowDiscrepancyCompanies,]
    resultFrame$avgRet[i*2] = mean(perms$return,na.rm = TRUE)
    resultFrame$smoothedRet[i*2] = mean(perms$smoothedReturn,na.rm = TRUE)
    resultFrame$meanImprovedAccuracy[i*2] = mean(perms$AccuracyIngoreEqual,na.rm = TRUE)
    resultFrame$run[i*2] = dir
    resultFrame$type[i*2] = "perms"
  }
  return(resultFrame)
}

library(lattice)
setwd("C:\\Users\\Leon Bornemann\\Desktop\\base Folder\\Final Runs\\Support Serial Modified")
resultFrame = getResultFrame()
setwd("C:\\Users\\Leon Bornemann\\Desktop\\base Folder\\Final Runs\\Number Of Windows")
resultFrame = rbind(resultFrame,getResultFrame())



resultFrame = resultFrame[order(resultFrame$meanImprovedAccuracy),]

xyplot(smoothedRet*100 ~ meanImprovedAccuracy,
       data= resultFrame,
       groups=type,
       type = "o",
       ylim = c(-20,20),
       auto.key=list(space="top", columns=2, cex.title=1),
       main="Smoothed Return",
       xlab = "mean Accuracy",
       ylab = "avg. Smoothed Return"
       )
xyplot(avgRet*100 ~ meanImprovedAccuracy,
       data= resultFrame,
       groups=type,
       type = "o",
       ylim = c(-100,100),
       auto.key=list(space="top", columns=2, cex.title=1),
       main="Return",
       xlab = "mean Accuracy",
       ylab = "avg. Return"
)