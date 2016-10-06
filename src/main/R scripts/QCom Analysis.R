setwd("D:\\Personal\\Documents\\Uni\\Master thesis\\Datasets\\Finance\\Time Series\\")
files = list.files()
for(file in files){
  timeSeries = read.table(file,header =T,sep = ",")
  dtimes = as.character(timeSeries$time)
  dtparts = t(as.data.frame(strsplit(dtimes,' ')))
  row.names(dtparts) = NULL
  finalTS = data.frame(date = as.factor(dtparts[,1]), time = as.factor(dtparts[,2]), value = timeSeries$value )
  diff = c(0,diff(finalTS$value))
  diffDate = c(0,diff(as.numeric(finalTS$date)))
  finalTS$diff = diff
  finalTS$diffDate = diffDate
  interstingEvents = finalTS[abs(finalTS$diff) > finalTS$value *0.05,]
  print(file)
  print(interstingEvents)
  #xAxis = 1:nrow(timeSeries)
  #plot(value~xAxis, data = timeSeries, type = "l", lwd=1, main = file)
    
}




dtimes = c("2002-06-09 12:45:40","2003-01-29 09:30:40",
                        "2002-09-04 16:45:40","2002-11-13 20:00:40",
                        "2002-07-07 17:30:40")
dtparts = t(as.data.frame(strsplit(dtimes,' ')))
row.names(dtparts) = NULL
thetimes = chron(dates=dtparts[,1],times=dtparts[,2], format=c('y-m-d','h:m:s'))