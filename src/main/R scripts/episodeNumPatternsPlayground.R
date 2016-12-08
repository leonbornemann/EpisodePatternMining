numItemsets = function(alphabetSize){
  return(2**alphabetSize)
}

numParallelEpisodes = function(alphabetSize){
  return(sum(toCoefficient(1:alphabetSize,alphabetSize)))
}

toCoefficient = function(vec,n){
  newVec = numeric(length(vec))
  for(i in 1:length(vec)){
    newVec[i] = choose(n+i-1,n-1)
  }
  return(newVec)
}

numSerialEpisodes = function(alphabetSize){
  return(sum(toPower(1:alphabetSize,alphabetSize)))
}

toPower = function(vec,base){
  newVec = numeric(length(vec))
  newVec = base
  return(newVec ** vec)
}

n=20
df = data.frame(alphabetSize = numeric(n),numItemsets = numeric(n), numSerialEpisodes = numeric(n),numParallelEpisodes = numeric(n))
for(i in 1:n){
  df$alphabetSize[i] = i
  df$numItemsets[i] = numItemsets(i)
  df$numParallelEpisodes[i] = numParallelEpisodes(i)
  df$numSerialEpisodes[i] = numSerialEpisodes(i)
}
df

library(lattice)

dfrm <- data.frame( numPatterns=c(df$numItemsets,df$numParallelEpisodes,df$numSerialEpisodes),
                    alphabetSize=df$alphabetSize, 
                    grp=rep(c("numItemsets","numParallelEpisodes","numSerialEpisodes"),each=nrow(df)))
xyplot(numPatterns~alphabetSize, 
       group=grp, 
       type="l", 
       data=dfrm, 
       main=list(label = "The Problem of Pattern Explosion",cex = 1.25),
       ylab = list(label="Number of Patterns",cex = 1.5),
       xlab = list(label="Alphabet Size",cex = 1.5),
       auto.key=list(space="top", columns=3, cex.title=1,lines=T, points=F),
       scales = list(y = list(log = 2,cex = 1.1),x=list(rot=0,cex = 1.1)),
       par.settings = list(superpose.line = list(lwd=3)))



colors = c("green","yellow","red")
plot(numItemsets~alphabetSize,data = df,type = "l",col = "green")