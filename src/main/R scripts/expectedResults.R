
getExpected = function(upTruePositiveRate, downFalseNegativeRate,meanPlus,numPlus,meanNegative,numNegative){
  upTruePositiveRate*meanPlus*numPlus + downFalseNegativeRate*meanNegative*numNegative
}

setwd("D:\\Personal\\Documents\\Uni\\Master thesis\\Datasets\\Finance\\Time Series\\")
#setwd("D:\\Personal\\Documents\\Uni\\Master thesis\\Datasets\\Finance\\Time Series Smoothed\\")
files = list.files()
totalNumRows = 0
#for(file in files){
  file = "AAPL.csv" 
  timeSeries = read.table(file,header =T,sep = ",")
  dtimes = as.character(timeSeries$time)
  dtparts = t(as.data.frame(strsplit(dtimes,' ')))
  row.names(dtparts) = NULL
  finalTS = data.frame(date = as.factor(dtparts[,1]), time = as.factor(dtparts[,2]), value = timeSeries$value )
  diff = c(0,diff(finalTS$value))
  diffDate = c(0,diff(as.numeric(finalTS$date)))
  finalTS$diff = diff
  finalTS$diffDate = diffDate
  
  finalDiffPositive = finalTS[(finalTS$diffDate==0) & (finalTS$diff>0),]$diff
  finalDiffNegative = finalTS[(finalTS$diffDate==0) & (finalTS$diff<0),]$diff
  meanPlus = mean(finalDiffPositive)
  numPositive = length(finalDiffPositive)
  meanNegative = mean(finalDiffNegative)
  numNegative = length(finalDiffNegative)
  meanPlus
  meanNegative
  
  precision = 0.5086334484459792
  inverseDown = 1 - 0.588703261734288
  totalExpectedRet = getExpected(precision,inverseDown,meanPlus,numPositive,meanNegative,numNegative)/finalTS$value[1]
  relative = totalExpectedRet / timeSeries$value[1]
  relative

tableCurExample = read.csv("C:\\Users\\Leon Bornemann\\Desktop\\base Folder\\Normal Runs\\Run 1\\resources\\AveragedResults\\PERMS_byCompany.csv")
expectedRet = tableCurExample[tableCurExample$company=="MSFT",]$return
precision = tableCurExample[tableCurExample$company=="MSFT",]$ImprovedPrecision_UP
inverseDown = 1 - tableCurExample[tableCurExample$company=="MSFT",]$ImprovedPrecision_DOWN
getExpected(precision,inverseDown,meanPlus,numPositive,meanNegative,numNegative)/finalTS$value[1]



#buy and hold

setwd("D:\\Personal\\Documents\\Uni\\Master thesis\\Datasets\\Finance\\Time Series\\")
#setwd("D:\\Personal\\Documents\\Uni\\Master thesis\\Datasets\\Finance\\Time Series Smoothed\\")
files = list.files()
totalNumRows = 0
tableCurExample = read.csv("C:\\Users\\Leon Bornemann\\Desktop\\base Folder\\Normal Runs\\Run 1\\resources\\AveragedResults\\PERMS_byCompany.csv")
diffSUmRet = numeric(0)
for(file in files){
  timeSeries = read.table(file,header =T,sep = ",")
  diff = timeSeries$value[length(timeSeries$value)] - timeSeries$value[1]
  result = diff / timeSeries$value[1]
  cmpId = strsplit(file, "\\.")[[1]][1]
  actualReturn =  tableCurExample[tableCurExample$company == cmpId,]$return - result
  print(paste(file,"Buy and Hold", result, "Actual Return", actualReturn ,"Actual - Buy and Hold", actualReturn-result))
  diffSUmRet = c(diffSUmRet,actualReturn-result)
}
mean(diffSUmRet)