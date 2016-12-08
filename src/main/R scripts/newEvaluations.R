setwd("C:\\Users\\Leon Bornemann\\Desktop\\base Folder\\Final Runs\\New Experiments\\Threshold\\Run 5\\resources\\AveragedResults")
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
fbswc = read.table("FBSWC_byCompany.csv",header=T,sep=",")
fbswc = fbswc[!fbswc$company %in% sectorCodes,]


perms = read.table("PERMS_byCompany.csv",header=T,sep=",")
perms = perms[!perms$company %in% sectorCodes,]
mean(perms$AccuracyIngoreEqual)
mean(fbswc$AccuracyIngoreEqual)

