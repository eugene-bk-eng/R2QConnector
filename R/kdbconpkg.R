# set up library

require(rJava)

.onLoad <- function(libname, pkgname){
  cat("libname: ", libname, "\n")
  cat("pkgname: ", pkgname, "\n")
  jarPath<-"../java/kdbconpkg.jar"
  connectionManagerClass<-"com/local/ideas/experiment/kdb/ConMgr"
}


#' My own onload
#' @return ref
#' @export
myonLoad <- function(libname, pkgname){
  .onLoad("", "")
}

## example of table with most commonly used  data types
# t1:([] date:`date$(); time:`time$(); sy:`symbol$(); vboolean:`boolean$(); charvalue:`char$(); str:();  px:`float$(); volume:`int$(); long:`long$(); tm:`datetime$() )
#`t1 insert(2016.12.15; "T"$"09:30:00.000"; `XYZ; 0b; "a"; "abc"; 1.345f; 100; 1099511627776j; .z.Z  )
#`t1 insert(2016.12.15; "T"$"09:30:00.000"; `XYZ; 1b; "z"; "xyz"; 2.345f; 300; 2099511627776j; .z.Z  )
#`t1 insert(100#select from t1) # duplicate rows n times

# for datetime, you may use
# http://stackoverflow.com/questions/13456241/convert-unix-epoch-to-date-object-in-r
# val <- 1352068320
# as.POSIXct(val, origin="1970-01-01")..
# "2012-11-04 22:32:00 CST"
# as.Date(as.POSIXct(val, origin="1970-01-01"))
# "2012-11-05"

#' Create manager object
#' @return reference to Java connection manager object
#' @export
initmgr<-function() {

  jarPath<-"../java/kdbconpkg.jar"
  connectionManagerClass<-"com/local/ideas/experiment/kdb/ConMgr"

  .jinit()
  .jclassPath()
  .jaddClassPath(jarPath)

  # open connection
  manager=.jnew(connectionManagerClass)
  return(manager)
}

#' Open connection to Q
#' @param manager reference to Java object in memory
#' @param host string
#' @param port string or 5000L as numeric
#' @return int handle to Q connection
# examples: manager$connect("localhost,"5000")
# examples: manager$connect("localhost,5000L)
#' @export
connect<-function(manager,host,port) {
  handle=manager$connect(host, port)
  return (handle)
}

#' Close connection to Q
#' @param manager reference to Java object in memory
#' @param handle int
#' @return nothing
# examples: manager$connect("localhost,5000L)
#' @export
close<-function(manager,handle) {
  manager$close(handle)
}


# Converts KDB table 98h to R data frame.
# @param table reference to java KxTable class
# @return data frame

# numeric data types (float,int,long) are converted correctly.
# temporal data types are converted to string
# char isconverted to single letter string
# guid (unique id) is not mapped.
# few other types are not mapped
convertToDF<-function(t) {

  # print diagnostics
  n=t$getRowCount()
  cat( "rows: ", t$getRowCount(), "\n")
  #cat( "cols: ", t$getColumnCount() , "\n")

  # create empty data frame w/ dummy column
  df<-data.frame(x=c(1:n))

  # retrieve data
  for(i in 0:(t$getColumnCount()-1) ) {
    name=t$getColumnName(i)
    type=t$getColumnType(i)

    #
    if( type=="[Ljava.sql.Date;") {
      # kdb date -> java.sql.Date
      # converted on the Java side to String
      column=t$getColumnUtilDateArrayAsLong(i)
      x=vector(length=n)
      for(j in 1:n ) {
        #value=.jcall(column[[j]], "S", "toString") # fast call
        #yy=as.numeric(substring(value,0,4))
        #mm=as.numeric(substring(value,6,7))
        #dd=as.numeric(substring(value,9,10))
        #value=as.Date(ISOdate(yy,mm,dd))
        x[j]=column[j]
      }
      df[name]<-x
    }
    #
    else
    if( type=="[Ljava.sql.Time;") {
      # kdb time -> java.sql.Time
      # converted on the Java side to String
      column=t$getColumnUtilDateArrayAsLong(i)
      x=vector(length=n)
      for(j in 1:n ) {
        #value=as.numeric(substring(toString(column[j]),0,10))
        #value=as.POSIXct(value, origin = "1970-01-01")
        x[j]=column[j]
      }
      df[name]<-x
    }

    # tm - timestamp `z column
    else
    if( type=="[Ljava.util.Date;") {
      # kdb datetime -> java Date
      # converted on the Java side to String
      column=t$getColumnUtilDateArrayAsLong(i)
      x=vector(length=n)
      for(j in 1:n ) {
        #value=as.numeric(substring(toString(column[j]),0,10))
        #value=as.POSIXct(value, origin = "1970-01-01")
        #x[j]=toString(value)
        x[j]=column[j]
      }
      df[name]<-x
    }

    else
    if( type=="[Ljava.lang.String;") {
      # kdb symbol -> java string
      column=t$getColumnObject(i)
      x=vector(length=n)
      for(j in 1:n ) {
        x[j]=column[j]
      }
      df[name]<-x
    }
    else
    if( type=="[Ljava.lang.Object;") {
      # kdb char list (string) -> java object. converted on the Java side to String
      column=t$getColumnObjectArrayAsString(i)
      x=vector(length=n)
      for(j in 1:n ) {
        x[j]=column[j]
      }
      df[name]<-x
    }
    else
    if ( type=="[C") {
      #kdb char -> java character
      #the char type does not exist in R
      #so we convert char to String in Java.
      #value is one letter.
      # converted on the Java side to String
      column=t$getColumnCharAsString(i)
      x=vector(length=n)
      for(j in 1:n ) {
        x[j]=column[j]
      }
      df[name]<-x
    }
    else
    if ( type=="[D") {
      # kdb float -> java double
      column=t$getColumnObject(i)
      x=vector(length=n)
      for(j in 1:n ) {
        x[j]=column[j]
      }
      df[name]<-x
    }else
    if ( type=="[Z") {
      # kdb boolean -> java boolean
      column=t$getColumnObject(i)
      x=vector(length=n)
      for(j in 1:n ) {
        x[j]=column[j]
      }
      df[name]<-x
    }
    else
    if ( type=="[I" | type=="[J" ) {
      # kdb int and long -> java int and long
      column=t$getColumnObject(i)
      x=vector(length=n)
      for(j in 1:n ) {
        x[j]=column[j]
      }
      df[name]<-x
    }

    else{
      cat("UNSUPPORTED TYPE: ", (i+1), ", ", t$getColumnName(i), ",", type , "\n")
    }
  }

  # remove dummy column
  df$x<-NULL

  return (df)
}

# Convert Java long to R built-in type
# @param df data frame
# @param column column which must be formatted
# @return data frame
# as example of conversion Java Long->R Date->string
convertJavaUtilDateToRDate<-function(df,column) {
  n=nrow(df)
  replacement=vector(length=n)
  for(j in 1:n ) {
    value=toString(df[j,column])
    value=as.numeric(substring(value,0,10))
    value=as.POSIXct(value, origin = "1970-01-01")
    replacement[j]=toString(value)
  }
  df[,column]=replacement
  return(df)
}

#' Select Q table as R data frame
#' Conversion (KDB)98h ->(Java)KxTable->(R)data frame
#' @param manager reference to java class
#' @param handle int connection handle
#' @param query Q string
#' @return data frame
#' @export
select<-function(manager,handle, query) {
  p1 <- proc.time();
  javaObject=manager$select(handle,query)
  df=convertToDF(javaObject)
  elapsed <- proc.time() - p1;
  print(elapsed)
  return (df)
}

#' Execute Q statement remotely
#' Conversion (KDB)98h ->(Java)KxTable->(R)data frame
#' @param manager reference to java class
#' @param handle int connection handle
#' @param query Q string
#' @return nothing
#' @export
exec<-function(manager,handle,query) {
  p1 <- proc.time();
  javaObject=manager$exec(handle,query)
  elapsed <- proc.time() - p1;
  print(elapsed)
}

#
test <- function(){

  # open connection
  manager = initmgr()
  h1  = connect(manager,"localhost", 5000L)

  #
  df=select(manager,h1,"3#select from t1")
  #df=select(manager,h1,"3#select date,time,tm from t1")
  #df=select(manager,h1,"select volume from taq")
  print(nrow(df))

  df=convertJavaUtilDateToRDate(df,"tm")
  print( head(df,3) )

  close(manager,h1)
}

#test()
