startDir = "C:\\Users\\Leon Bornemann\\Desktop\\base Folder\\Final Runs\\Using Semantics\\Run 1\\resources\\results"
allDirs = list.dirs(startDir,recursive = FALSE)
totalTable = data.frame(
  TrainingTimeNS = numeric(0),
  TestTimeNS = numeric(0),
  method = character(0),
  cmp = character(0),
  stringsAsFactors = FALSE
)
permsOnly = FALSE
for(dir in allDirs){
  setwd(dir)
  permsfname = "runtimePerformance.csv"
  numPredPERMS = nrow(read.csv("predictions.csv"))
  perms = read.csv(permsfname)
  perms$TestTimeNS = perms$TestTimeNS / numPredPERMS
  perms$method = "perms"
  perms$cmp = basename(getwd())
  fbswc$cmp = basename(getwd())
  if(!permsOnly){
    fbswcfname = "FBSWCruntimePerformance.csv"
    numPredFBSWC = nrow(read.csv("FBSWC_predictions.csv"))
    fbswc = read.csv(fbswcfname)
    fbswc$TestTimeNS = fbswc$TestTimeNS / numPredFBSWC
    fbswc$method = "fbswc"
    fbswc$cmp = basename(getwd())
    totalTable = rbind(totalTable,fbswc)
  }
  totalTable = rbind(totalTable,perms)
}

totalTable$TrainingTimeMS = totalTable$TrainingTimeNS / 1000000.0
totalTable$TestTimeMS = totalTable$TestTimeNS / 1000000.0

print(mean(totalTable[totalTable$method == "perms",]$TestTimeMS))
print(mean(totalTable[totalTable$method == "fbswc",]$TestTimeMS))

print(mean(totalTable[totalTable$method == "perms",]$TrainingTimeMS))
print(mean(totalTable[totalTable$method == "fbswc",]$TrainingTimeMS))

startDir = "C:\\Users\\Leon Bornemann\\Desktop\\base Folder\\Final Runs\\Average Forecasting\\Run 1\\resources\\results"
allDirs = list.dirs(startDir,recursive = FALSE)
for(dir in allDirs){
  setwd(dir)
  fname = "RandomGuessingruntimePerformance.csv"
  numPred = nrow(read.csv("RandomGuessing_predictions.csv"))
  movingAvg = read.csv(fname)
  movingAvg$TestTimeNS = movingAvg$TestTimeNS / numPred
  movingAvg$method = "simple moving average"
  movingAvg$cmp = basename(getwd())
  totalTable = rbind(totalTable,movingAvg)
}

totalTable$TrainingTimeMS = totalTable$TrainingTimeNS / 1000000.0
totalTable$TestTimeMS = totalTable$TestTimeNS / 1000000.0

mean(totalTable[totalTable$method == "perms",]$TestTimeMS)
mean(totalTable[totalTable$method == "fbswc",]$TestTimeMS)
mean(totalTable[totalTable$method == "simple moving average",]$TestTimeMS)

mean(totalTable[totalTable$method == "perms",]$TrainingTimeMS)
mean(totalTable[totalTable$method == "fbswc",]$TrainingTimeMS)
mean(totalTable[totalTable$method == "simple moving average",]$TrainingTimeMS)


colors = c("purple","blue","orange")
library(lattice)
barchart(TestTimeMS~cmp,
         data=totalTable,
         groups=method,
         par.settings=list(superpose.polygon=list(col=colors)),
         origin = 0,
         ylab = list(label="avg. prediction time [ms]",cex = 1.5),
         auto.key=list(space="top", columns=2, cex.title=1),
         scales=list(x=list(rot=90)),
         main=list(label = "Average Prediction Time",cex = 1.25)
)