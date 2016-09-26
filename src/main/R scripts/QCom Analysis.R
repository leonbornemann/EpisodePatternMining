setwd("D:\\Personal\\Documents\\Uni\\Master thesis\\Datasets\\Finance\\Time Series\\")
timeSeries = read.table("TECH.csv",header =T,sep = ",")
xAxis = 1:nrow(timeSeries)
plot(value~xAxis, data = timeSeries, type = "l", lwd=1)
