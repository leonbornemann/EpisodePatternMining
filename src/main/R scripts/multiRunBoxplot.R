
drawBoxPlots = function(xTitle, permsOnly = FALSE,includeSectors = FALSE){
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
  resultFrame = data.frame(
    ret = numeric(0),
    type = character(0),
    run = numeric(0),
    smoothedRet = numeric(0),
    stringsAsFactors = FALSE
  )
  for(i in 1:length(dirs)){
    dir = dirs[i]
    #fbswc
    if(!permsOnly){
      fbswc = read.csv(paste(dir,fbswcResultPath,sep = ""))
      if(!includeSectors){
        fbswc = fbswc[!(fbswc$company %in% sectorCodes),]
      }
      fbswcResult = data.frame(
        ret = fbswc$return,
        type = "fbswc",
        run = as.numeric(strsplit(dir," ")[[1]][-1]),
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
      run = as.numeric(strsplit(dir," ")[[1]][-1]),
      smoothedRet = perms$smoothedReturn,
      stringsAsFactors = FALSE
    )
    resultFrame = rbind(resultFrame,fbswcResult,permsResult)
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
drawBoxPlots("support Serial (Decreasing)")
setwd("C:\\Users\\Leon Bornemann\\Desktop\\base Folder\\Number Of Windows")
drawBoxPlots("Number Of Windows (Increasing)")
setwd("C:\\Users\\Leon Bornemann\\Desktop\\base Folder\\Perms Only")
drawBoxPlots("Number Of Predictors (Increasing)",TRUE)
setwd("C:\\Users\\Leon Bornemann\\Desktop\\base Folder\\Using semantics")
drawBoxPlots("Various Parameters")