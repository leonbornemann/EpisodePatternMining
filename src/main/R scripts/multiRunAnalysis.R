notForbidden = function(dir){
  a = strsplit(dir,"\\s")
  number = as.numeric(a[[1]][2])
  return((number<11))
}

library(lattice)
setwd("C:\\Users\\Leon Bornemann\\Desktop\\base Folder\\Normal Runs")
fbswcResultPath = "\\resources\\AveragedResults\\FBSWC_byCompany.csv"
permsResultPath = "\\resources\\AveragedResults\\PERMS_byCompany.csv"
dirs = list.dirs(recursive = FALSE)
#dirs = Filter(notForbidden,dirs)
size = length(dirs)*2
resultFrame = data.frame(
  meanPrecisionUp = numeric(size),
  meanPrecisionDown = numeric(size),
  meanPrecisionIEUp = numeric(size),
  meanPrecisionIEDown = numeric(size),
  meanPrecisionImprovUp = numeric(size),
  meanPrecisionImprovDown = numeric(size),
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
  resultFrame$avgRet[i*2-1] = mean(fbswc$return,na.rm = TRUE)
  resultFrame$smoothedRet[i*2-1] = mean(fbswc$smoothedReturn,na.rm = TRUE)
  resultFrame$meanPrecisionUp[i*2-1] = mean(fbswc$Precision_UP,na.rm = TRUE)
  resultFrame$meanPrecisionDown[i*2-1] = mean(fbswc$Precision_DOWN,na.rm = TRUE)
  resultFrame$meanPrecisionIEUp[i*2-1] = mean(fbswc$PrecisionIgnoreEqual_UP,na.rm = TRUE)
  resultFrame$meanPrecisionIEDown[i*2-1] = mean(fbswc$PrecisionIgnoreEqual_DOWN,na.rm = TRUE)
  resultFrame$meanPrecisionImprovUp[i*2-1] = mean(fbswc$ImprovedPrecision_UP,na.rm = TRUE)
  resultFrame$meanPrecisionImprovDown[i*2-1] = mean(fbswc$ImprovedPrecision_DOWN,na.rm = TRUE)
  resultFrame$run[i*2-1] = dir
  resultFrame$type[i*2-1] = "fbswc"
  #perms
  perms = read.csv(paste(dir,permsResultPath,sep = ""))
  resultFrame$avgRet[i*2] = mean(perms$return,na.rm = TRUE)
  resultFrame$smoothedRet[i*2] = mean(perms$smoothedReturn,na.rm = TRUE)
  resultFrame$meanPrecisionUp[i*2] = mean(perms$Precision_UP,na.rm = TRUE)
  resultFrame$meanPrecisionDown[i*2] = mean(perms$Precision_DOWN,na.rm = TRUE)
  resultFrame$meanPrecisionIEUp[i*2] = mean(perms$PrecisionIgnoreEqual_UP,na.rm = TRUE)
  resultFrame$meanPrecisionIEDown[i*2] = mean(perms$PrecisionIgnoreEqual_DOWN,na.rm = TRUE)
  resultFrame$meanPrecisionImprovUp[i*2] = mean(perms$ImprovedPrecision_UP,na.rm = TRUE)
  resultFrame$meanPrecisionImprovDown[i*2] = mean(perms$ImprovedPrecision_DOWN,na.rm = TRUE)
  resultFrame$run[i*2] = dir
  resultFrame$type[i*2] = "perms"
}

resultFrame = resultFrame[order((resultFrame$meanPrecisionImprovUp +resultFrame$meanPrecisionImprovDown) /2),]

xyplot(smoothedRet*100 ~ (meanPrecisionImprovUp +meanPrecisionImprovDown) /2,
       data= resultFrame,
       groups=type,
       type = "l",
       ylim = c(-20,20),
       auto.key=list(space="top", columns=2, cex.title=1),
       main="B - Precision {UP} by Company"
       )