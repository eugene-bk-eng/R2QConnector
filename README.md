## PURPOSE:
R package that connects an R session with multiple Q processes. Use it to

- Execute Q commands remotely.   
- For select that returns Q tables(98h), library will pull the data over the network and convert into R data frame.  


##INSTALL FROM GITHUB:
install.packages("devtools")  
install.packages("rJava")  
library(devtools)  
install_github("ocean927/R2QConnector", force=TRUE)  


## USE:
library(rJava)  
library(kdbconpkg)  


## R API:
```
#creates connection manager
initmanager() 
ex: manager = initmanager()

#connects to Q session
connect(manager,host,port)
ex: 
h1  = connect(manager,"localhost", 5000L) 
h2  = connect(manager,"remotehost", 9000L) 

#execute remote statement
exec(manager,handle, query)
ex: exec(manager,handle,"x: 1 2 3")

#retrieve as data frame
select(handle, query)
ex:
df=select(manager,h1,"3#select from t")

#show open handles, returns a list of strings
handles(manager)

#close Q session
connect(manager,handle)
```

## REQUIREMENTS:
KDB process live and listening on a  port.
Ex: >q -p 5000


## LIMITATIONS:
- Currently only table 98h is returned back into R session.
- The following data types are supported: 
boolean(b), int(i), long(j), float(f), char(c), symbol(s), 
date(d), datetime(z), time(t), 
- For convinience, Q list of char(c) known as string 
will be converted to an R string.
- These types are not implemented:
guid(g), byte(x), short(h), real(e), timestamp(p), month(m),
timespan(n), minute(u), second(v)


## CONVERSION TO R TYPES:
Due to performance, package implements the following conversions:  
date(d) - returned to R as numeric.  
time(t) - returned to R as numeric.  
datetime(z) - returned to R as string "2016.12.24 21:16:38.067 EST"    


## TODO:
1. Retrieve dictionary 99h.  
2. Map remaining KDB reference types.  
3. Write data frame back to Q as a table/dictionary.  
4. Improve performance of select.  
5. Implement batched retrieval for large tables.  
6. Remove manager object and expose only Q handle.


## DESIGN:
Package uses Java library to connect to 
and translate KDB types.


## PERFORMANCE:
Currently converting to R data frame is slow. 

## EXAMPLE:

```
#
# Q code
#

# create table
t1:([] date:`date$(); time:`time$(); sy:`symbol$(); vboolean:`boolean$(); charvalue:`char$(); str:();  px:`float$(); volume:`int$(); long:`long$(); tm:`datetime$() )

# fill with dummy data
`t1 insert(2016.12.1; "T"$"07:00:00.000"; `FOO; 0b; "a"; "xyz"; 1.345f; 100; 1099511627776j; .z.Z  )
...

#
# R code
#

# open session to one or more Q instances
manager = initmanager()
h1  = connect(manager,"localhost", 5000L)
h2  = connect(manager,"host", "port") 

# execute remotely
exec(manager,h1,"x: 1 2 3")
exec(manager,h1,".Q.w[]")

# retrieve as R data frame
df1=select(manager, h1, "10#select from t1")
head(df1)

# retrieve as R data frame, Q memory stats
df2=select(manager, h1, "select from ([] k:key .Q.w[]; v:value .Q.w[])")

# show open handles
handles(manager)

# close Q connection.
close(manager,h1)
```

### OUTPUT:
```
> library(rJava)
> library(kdbconpkg)

> manager = initmanager()
> h1  = connect(manager,"dev1", 5000L)
> df=select(manager,h1,"3#select from yahoo_table")
rows:  3 
   user  system elapsed 
   0.09    0.01    0.17 

# notice number of rows and time it create a data frame

> df
          date sy  open  high   low close adjclose  volume
1 1.475208e+12  A 46.50 47.32 46.30 47.09 47.09000 1754300
2 1.475122e+12  A 47.05 47.27 46.14 46.41 46.29500 1938200
3 1.475035e+12  A 46.90 47.26 46.52 47.18 47.06309 1502500

# notice date column is returned as numeric

```



## FEEDBACK:
For questions and suggestions, write to vortexsny@hotmail.com
