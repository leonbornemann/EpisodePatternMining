
drawReturnBoxPlots = function(xTitle, permsOnly = FALSE,includeSectors = FALSE,runValues=NULL, sectorsOnly =FALSE){
  if(sectorsOnly){
    includeSectors = TRUE
  }
  fbswcResultPath = "\\resources\\AveragedResults\\FBSWC_byCompany.csv"
  permsResultPath = "\\resources\\AveragedResults\\PERMS_byCompany.csv"
  sectorCodes = c("Miscellaneous",
                  "Health Care",
                  "Energy",
                  "Consumer Durables",
                  "Technology",
                  "Finance",
                  "Transportation",
                  "Consumer Services",
                  "Capital Goods",
                  "Public Utilities",
                  "Basic Industries",
                  "Consumer Non-Durables")
  lowDiscrepancyCompanies = c("ON","VA","TECH","FCS","CTSH","AAPL","ISSC","INTC","CSCO","SNDK") #,"VOD","MSFT","ELNK","EBAY","TXN","YHOO","CGEN"
  dirs = list.dirs(recursive = FALSE)
  dirs = dirs[order(dirs)]
  resultFrame = data.frame(
    ret = numeric(0),
    type = character(0),
    run = numeric(0),
    smoothedRet = numeric(0),
    accuracyIgnoreEqual = numeric(0),
    stringsAsFactors = FALSE
  )
  for(i in 1:length(dirs)){
    dir = dirs[i]
    if(is.null(runValues)){
      runVal = as.numeric(strsplit(dir," ")[[1]][-1])
    } else{
      runVal = runValues[i]
    }
    #fbswc
    if(!permsOnly){
      fbswc = read.csv(paste(dir,fbswcResultPath,sep = ""))
      if(!includeSectors){
        fbswc = fbswc[!(fbswc$company %in% sectorCodes),]
      }
      fbswc = fbswc[(fbswc$company %in% lowDiscrepancyCompanies),]
      if(sectorsOnly){
        fbswc = fbswc[(fbswc$company %in% sectorCodes),]
      }
      fbswcResult = data.frame(
        ret = fbswc$return,
        type = "fbswc",
        run = runVal,
        smoothedRet = fbswc$smoothedReturn,
        accuracyIgnoreEqual = fbswc$AccuracyIngoreEqual,
        stringsAsFactors = FALSE
      )
    }
    #perms
    perms = read.csv(paste(dir,permsResultPath,sep = ""))
    if(!includeSectors){
      perms = perms[!(perms$company %in% sectorCodes),]
    }
    perms = perms[(perms$company %in% lowDiscrepancyCompanies),]
    if(sectorsOnly){
      perms = perms[(perms$company %in% sectorCodes),]
    }
    permsResult = data.frame(
      ret = perms$return,
      type = "perms",
      run = runVal,
      smoothedRet = perms$smoothedReturn,
      accuracyIgnoreEqual = perms$AccuracyIngoreEqual,
      stringsAsFactors = FALSE
    )
    resultFrame = rbind(resultFrame,permsResult)
    if(!permsOnly){
      resultFrame = rbind(resultFrame,fbswcResult)
    }
  }
  #boxplot(accuracyIgnoreEqual*100~type*run,data=resultFrame, main="PERMS",xlab=xTitle, ylab="Accuracy [%]")
  
  boxplot(ret*100~run,data=resultFrame[resultFrame$type=="perms",], main="PERMS",
          xlab=xTitle, ylab="Return [%]")
  means <- tapply(resultFrame[resultFrame$type=="perms",]$ret,resultFrame[resultFrame$type=="perms",]$run,mean)
  means = means*100
  points(means,col="red",pch=18)
  if(!permsOnly){
    boxplot(ret*100~run,data=resultFrame[resultFrame$type=="fbswc",], main="FBSWC",
            xlab=xTitle, ylab="Return [%]")
    means <- tapply(resultFrame[resultFrame$type=="fbswc",]$ret,resultFrame[resultFrame$type=="fbswc",]$run,mean)
    means = means*100
    points(means,col="red",pch=18)
    
  }
}


drawAccuracyBoxPlots = function(xTitle, permsOnly = FALSE,includeSectors = FALSE,runValues=NULL,sectorsOnly = FALSE){
  if(sectorsOnly){
    includeSectors = TRUE
  }
  fbswcResultPath = "\\resources\\AveragedResults\\FBSWC_byCompany.csv"
  permsResultPath = "\\resources\\AveragedResults\\PERMS_byCompany.csv"
  sectorCodes = c("Miscellaneous",
                  "Health Care",
                  "Energy",
                  "Consumer Durables",
                  "Technology",
                  "Finance",
                  "Transportation",
                  "Consumer Services",
                  "Capital Goods",
                  "Public Utilities",
                  "Basic Industries",
                  "Consumer Non-Durables")
  lowDiscrepancyCompanies = c("ON","VA","TECH","FCS","CTSH","AAPL","ISSC","INTC","CSCO","SNDK") #,"VOD","MSFT","ELNK","EBAY","TXN","YHOO","CGEN"
  dirs = list.dirs(recursive = FALSE)
  dirs = dirs[order(dirs)]
  resultFrame = data.frame(
    ret = numeric(0),
    type = character(0),
    run = numeric(0),
    smoothedRet = numeric(0),
    accuracyIgnoreEqual = numeric(0),
    cmp = character(0),
    stringsAsFactors = FALSE
  )
  for(i in 1:length(dirs)){
    dir = dirs[i]
    if(is.null(runValues)){
      runVal = as.numeric(strsplit(dir," ")[[1]][-1])
    } else{
      runVal = runValues[i]
    }
    #fbswc
    if(!permsOnly){
      fbswc = read.csv(paste(dir,fbswcResultPath,sep = ""))
      if(!includeSectors){
        fbswc = fbswc[!(fbswc$company %in% sectorCodes),]
      }
      if(sectorsOnly){
        fbswc = fbswc[(fbswc$company %in% sectorCodes),]
      }
      fbswcResult = data.frame(
        ret = fbswc$return,
        type = "fbswc",
        run = runVal,
        smoothedRet = fbswc$smoothedReturn,
        accuracyIgnoreEqual = fbswc$AccuracyIngoreEqual,
        cmp = fbswc$company,
        stringsAsFactors = FALSE
      )
    }
    #perms
    perms = read.csv(paste(dir,permsResultPath,sep = ""))
    if(!includeSectors){
      perms = perms[!(perms$company %in% sectorCodes),]
    }
    if(sectorsOnly){
      fbswc = fbswc[(fbswc$company %in% sectorCodes),]
    }
    permsResult = data.frame(
      ret = perms$return,
      type = "perms",
      run = runVal,
      smoothedRet = perms$smoothedReturn,
      accuracyIgnoreEqual = perms$AccuracyIngoreEqual,
      cmp = perms$company,
      stringsAsFactors = FALSE
    )
    resultFrame = rbind(resultFrame,permsResult)
    if(!permsOnly){
      resultFrame = rbind(resultFrame,fbswcResult)
    }
  }
  resultFrame = resultFrame[resultFrame$accuracyIgnoreEqual!=0,]
  #boxplot(accuracyIgnoreEqual*100~type*run,data=resultFrame, main="PERMS",xlab=xTitle, ylab="Accuracy [%]")
  
  boxplot(accuracyIgnoreEqual*100~run,data=resultFrame[resultFrame$type=="perms",], main="PERMS",
          xlab=xTitle, ylab="Accuracy [%]",ylim = c(32,75))
  means <- tapply(resultFrame[resultFrame$type=="perms",]$accuracyIgnoreEqual,resultFrame[resultFrame$type=="perms",]$run,mean,na.rm = TRUE)
  means = means*100
  points(means,col="red",pch=18)
  if(!permsOnly){
    boxplot(accuracyIgnoreEqual*100~run,data=resultFrame[resultFrame$type=="fbswc",], main="FBSWC",
            xlab=xTitle, ylab="Accuracy [%]",ylim = c(32,75))
    means <- tapply(resultFrame[resultFrame$type=="fbswc",]$accuracyIgnoreEqual,resultFrame[resultFrame$type=="fbswc",]$run,mean,na.rm = TRUE)
    means = means*100
    points(means,col="red",pch=18)
    
  }
  
  a = resultFrame[resultFrame$accuracyIgnoreEqual>0.6,]
  print(a[!is.na(a$accuracyIgnoreEqual),])
  b = resultFrame[resultFrame$accuracyIgnoreEqual<0.4,]
  print(b[!is.na(b$accuracyIgnoreEqual),])
  
  
}


library(lattice)
setwd("C:\\Users\\Leon Bornemann\\Desktop\\base Folder\\Final Runs\\Support Serial Modified")
drawAccuracyBoxPlots("Serial Support Threshold",runValues=c(0.8,0.7,0.6,0.5,0.4))
drawReturnBoxPlots("Serial Support Threshold",runValues=c(0.8,0.7,0.6,0.5,0.4))
setwd("C:\\Users\\Leon Bornemann\\Desktop\\base Folder\\Final Runs\\Number Of Windows")
drawAccuracyBoxPlots("Number Of Windows",runValues=c(100,150,200,250,300,350,400))
setwd("C:\\Users\\Leon Bornemann\\Desktop\\base Folder\\Final Runs\\Perms Only")
drawAccuracyBoxPlots("Number Of Predictors",TRUE,runValues=c(10,30,40,50,100,200,300,400,500,1000))
drawReturnBoxPlots("Number Of Predictors",TRUE,c(10,30,40,50,100,200,300,400,500,1000))
setwd("C:\\Users\\Leon Bornemann\\Desktop\\base Folder\\Final Runs\\Using semantics")
drawAccuracyBoxPlots("Serial Support Threshold",runValues=c(0.8,0.7,0.6))
drawAccuracyBoxPlots("Serial Support Threshold",sectorsOnly = TRUE,runValues=c(0.8,0.7,0.6))
setwd("C:\\Users\\Leon Bornemann\\Desktop\\base Folder\\Final Runs\\Using semantics Counterpart")
drawAccuracyBoxPlots("Serial Support Threshold",runValues=c(0.8,0.7,0.6))