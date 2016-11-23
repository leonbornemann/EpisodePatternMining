
drawBoxPlots = function(xTitle, permsOnly = FALSE,includeSectors = FALSE,runValues=NULL){
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
  dirs = list.dirs(recursive = FALSE)
  dirs = dirs[order(dirs)]
  resultFrame = data.frame(
    ret = numeric(0),
    type = character(0),
    run = numeric(0),
    smoothedRet = numeric(0),
    stringsAsFactors = FALSE
  )
  for(i in 1:length(dirs)){
    dir = dirs[i]
    if(missing(runValues)){
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
      fbswcResult = data.frame(
        ret = fbswc$return,
        type = "fbswc",
        run = runVal,
        smoothedRet = fbswc$smoothedReturn,
        stringsAsFactors = FALSE
      )
    }
    #perms
    perms = read.csv(paste(dir,permsResultPath,sep = ""))
    if(!includeSectors){
      perms = perms[!(perms$company %in% sectorCodes),]
    }
    permsResult = data.frame(
      ret = perms$return,
      type = "perms",
      run = runVal,
      smoothedRet = perms$smoothedReturn,
      stringsAsFactors = FALSE
    )
    resultFrame = rbind(resultFrame,permsResult)
    if(!permsOnly){
      resultFrame = rbind(resultFrame,fbswcResult)
    }
  }
  
  boxplot(ret*100~run,data=resultFrame[resultFrame$type=="perms",], main="PERMS",
          xlab=xTitle, ylab="Return [%]",ylim = c(-200,200)) 
  if(!permsOnly){
    boxplot(ret*100~run,data=resultFrame[resultFrame$type=="fbswc",], main="FBSWC",
            xlab=xTitle, ylab="Return [%]",ylim = c(-200,200))
  }
}


library(lattice)
setwd("C:\\Users\\Leon Bornemann\\Desktop\\base Folder\\Support Serial Modified")
drawBoxPlots("support Serial",runValues=c(0.8,0.7,0.6,0.5,0.4))
setwd("C:\\Users\\Leon Bornemann\\Desktop\\base Folder\\Number Of Windows")
drawBoxPlots("Number Of Windows",runValues=c(100,150,200,250,300,350,400))
setwd("C:\\Users\\Leon Bornemann\\Desktop\\base Folder\\Perms Only")
drawBoxPlots("Number Of Predictors",TRUE,runValues=c(10,30,40,50,100,200,300,400,500,1000))
setwd("C:\\Users\\Leon Bornemann\\Desktop\\base Folder\\Using semantics")
drawBoxPlots("Various Parameters")