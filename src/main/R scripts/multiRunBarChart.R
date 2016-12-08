getResultFrame = function(categoryName,runames){
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
    category= character(size),
    stringsAsFactors = FALSE
  )
  for(i in 1:length(dirs)){
    dir = dirs[i]
    #fbswc
    fbswc = read.csv(paste(dir,fbswcResultPath,sep = ""))
    #fbswc = fbswc[fbswc$company %in% lowDiscrepancyCompanies,]
    resultFrame$avgRet[i*2-1] = mean(fbswc$return,na.rm = TRUE)
    resultFrame$smoothedRet[i*2-1] = mean(fbswc$smoothedReturn,na.rm = TRUE)
    resultFrame$meanImprovedAccuracy[i*2-1] = mean(fbswc$AccuracyIngoreEqual,na.rm = TRUE)
    resultFrame$run[i*2-1] = runames[i]
    resultFrame$type[i*2-1] = "fbswc"
    #perms
    perms = read.csv(paste(dir,permsResultPath,sep = ""))
    perms = fbswc[perms$company %in% lowDiscrepancyCompanies,]
    resultFrame$avgRet[i*2] = mean(perms$return,na.rm = TRUE)
    resultFrame$smoothedRet[i*2] = mean(perms$smoothedReturn,na.rm = TRUE)
    resultFrame$meanImprovedAccuracy[i*2] = mean(perms$AccuracyIngoreEqual,na.rm = TRUE)
    resultFrame$run[i*2] = runames[i]
    resultFrame$type[i*2] = "perms"
  }
  resultFrame$category = categoryName
  return(resultFrame)
}

library(lattice)
setwd("C:\\Users\\Leon Bornemann\\Desktop\\base Folder\\Final Runs\\Using semantics")
resultFrame = getResultFrame("semantic",c("0.8","0.7","0.6"))
setwd("C:\\Users\\Leon Bornemann\\Desktop\\base Folder\\Final Runs\\Using semantics Counterpart")
resultFrame = rbind(resultFrame,getResultFrame("non-semantic",c("0.8","0.7","0.6")))
resultFrame$grouping = as.factor(paste(resultFrame$type,resultFrame$category))

resultFrame$run = as.factor(resultFrame$run)
typeof(resultFrame$grouping)

colors = c("purple","coral","blue","gold")

barchart(meanImprovedAccuracy*100~run,
         data=resultFrame,
         groups=grouping,
         par.settings=list(superpose.polygon=list(col=colors)),
         origin = 50,
         ylab = list(label="Mean Accuracy [%]",cex = 1.5),
         xlab = list(label="Serial Support Threshold",cex = 1.5),
         auto.key=list(space="top", columns=2, cex.title=1),
         scales=list(x=list(rot=90,cex = 1.1),y=list(rot=0,cex=1.2)),
         main=list(label = "Mean Accuracy Comparison",cex = 1.25)
)