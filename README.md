## PURPOSE:
A prototype package that connects an R session with Q process. It can be used to

1. Execute Q commands remotely. 
Note: operation result is not returned back to R session.

2. Convert these Q result sets into R data frame.


## INSTALLATION:

install.packages("devtools")

install.packages("rJava")

install_github("ocean927/R2QCon")

library(rJava)
library(kdbconpkg)

Go to examples section below

=======

install_github("ocean927/R2QCon")

library(rJava)

library(kdbconpkg)

Go to examples section below

## FUNCTIONS:
```
//creates a java connection manager
initmgr() 
ex: 
manager = initmgr()

//connects to Q session
connect(manager,host,port)
ex: 
handle  = connect(manager,"localhost", 5000L) 

//execute remote statement
exec(manager,handle, query)
ex:
exec(manager,handle,"x: 1 2 3")

// retrieve as data frame
select(handle, query)
ex:
df=select(manager,h1,"3#select from t")

// close Q session
connect(manager(javaref),handle(int))
```

## REQUIREMENTS:
KDB process live and listening on a  port.
Ex: >q -p 5000


## LIMITATIONS:
- Currently only table 98h is returned back into R session.
- The following data types are supported: 
boolean(b), int(i), long(j), float(f), char(c), symbol(s), 
date(d), datetime(z), time(t), 
- Additionally Q list of char(c) known as string 
will be converted to R string. See exampe:
- These types are not yet implemented:
guid(g), byte(x), short(h), real(e), timestamp(p), month(m),
timespan(n), minute(u), second(v)

## CONVERSION TO R TYPES
Due to performance, package will implement 
the following conversions:

date(d) - returned to R as numeric

time(t) - returned to R as numeric. 

datetime(z) - returned to R as string "2016.12.24 21:16:38.067 EST"

Implementation may change.

## TODO
1. Retrieve dictionary 99h.
2. Map remaining KDB reference types.
3. Write data frame back to Q as a table/dictionary.
4. Improve performance of select.
5. Implement batched retrieval for large tables.


## DESIGN:
Package uses Java library to connect to 
and translate KDB types.


## PERFORMANCE:
Example:
KDB and R are started locally.

table: 1M rows x 10 columns, 62455442 bytes.

1. Java library retrieves 1M records in 0.9 seconds.

2. R package retrieves 1M rows and converts to dataframe in ~10 seconds.


## EXAMPLE:

```
//create table
t1:([] date:`date$(); time:`time$(); sy:`symbol$(); vboolean:`boolean$(); charvalue:`char$(); str:();  px:`float$(); volume:`int$(); long:`long$(); tm:`datetime$() )

//fill with dummy data
`t1 insert(2016.12.1; "T"$"07:00:00.000"; `FOO; 0b; "a"; "xyz"; 1.345f; 100; 1099511627776j; .z.Z  )
`t1 insert(til 1000; .... )

# open session to one or more Q instances
manager = initmgr()
h1  = connect(manager,"localhost", 5000L)
h2  = connect(manager,"host", "port") 

# execute remotely
exec(manager,h1,"x: 1 2 3")
exec(manager,h1,".Q.w[]")

# retrieve as R data frame
df1=select(manager,h1,"10#select from t1")
head(df1)

# retrieve as R data frame, Q memory stats
df2=manager.select(h1,"select from ([] k:key .Q.w[]; v:value .Q.w[])")
head(df2)

# close Q connection.
close(manager,h1)
```


## FEEDBACK:
For questions and feedback, write to vortexsny@hotmail.com
