results = read.csv("C:\\Users\\Leon Bornemann\\Desktop\\base Folder\\Toy stuff\\toEval.csv")
results$discrepancy = abs(results$realReturn - results$expectedReturn)
companies = unique(results$company)
discrepancies = unique(results$discrepancy)
companiesOrdered = companies[order(discrepancies)]

results$company = ordered(results$company,levels = companiesOrdered)

cmpInfo = data.frame(company = ordered(companiesOrdered,levels = companiesOrdered),
                     numDifferences = numeric(length(companiesOrdered)),
                     discrepancy = numeric(length(companiesOrdered)),
                     std = numeric(length(companiesOrdered)),
                     numOutliers = numeric(length(companiesOrdered)),
                     numUniqueDiff = numeric(length(companiesOrdered)))

cmps = ordered(companiesOrdered,levels = companiesOrdered)

for(i in 1: length(cmps)){
  cmp = cmps[i]
  cmpInfo$company[i] = cmp
  cmpInfo$numDifferences[i] = sum(results$company == cmp)
  cmpInfo$discrepancy[i] = results[results$company==cmp,]$discrepancy[1]
  cmpInfo$std[i] = sd(results[results$company==cmp,]$diff)
  tempBoxPlot = boxplot(results[results$company==cmp,]$diff)
  cmpInfo$numOutliers[i] = length(tempBoxPlot$out)
  cmpInfo$numUniqueDiff[i] = length(unique(results[results$company==cmp,]$diff))
}

library(lattice)

xyplot(discrepancy*100 ~ company ,data = cmpInfo, type = "o",main = "Return Discrepancy for Random Guessing",xlab = "companies",ylab = "|exp. Return - actual Return| [%]")
xyplot(std*100 ~ company ,data = cmpInfo, type = "o",main = "Standard Deviation of the Difference Vector",xlab = "companies",ylab = "std ( DIFF ) [%]")
xyplot(numOutliers ~ company ,data = cmpInfo, type = "o",main = "Number of Outliers",xlab = "companies",ylab = "num Outliers")



xyplot(std ~ company ,data = cmpInfo, type = "o")

lines(discrepancy ~ company,data = cmpInfo, type = "o",col="red")

boxplot(diff*100~company,data=results, main="Boxes",
        xlab="companies", ylab="Diff [%]",ylim = c(-2,2)) 






numbered = paste(1:40,"_",sep = "")
companiesOrderedRenamed = paste(numbered,companiesOrdered,sep="")
compMapping = data.frame(companiesOrdered,companiesOrderedRenamed)