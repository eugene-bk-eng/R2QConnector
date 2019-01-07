// DEMO SCRIPT TO DEMONSTRATE HOW YOU 
// CAN MERGE TWO DATABASES WHICH HAVE IDENTICAL SCHEMA
// BUT DIFFERENT SYM FILES FOR SOME REASON.

// SCRIPT TO RE-ENUMERATE OF ALL ENUMERATED COLUMNS
// IT CAN BE ADOPTED TO VARIOUS WAYS OF 
// MERGING INPUT DATABASES INTO ONE.

// AUTHOR: DABLYA
// DATE: DECEMBER 21, 2018.

// \l C:\projects\kdb\mergedbs.q 

// createtable[`a`b`c]
// t1:createtable[`a`b`c]
createtable:{[startdate;symlist]	
	tpd:10000;              / trades per day
	day:10;               / number of days
	cnt:count symlist;    / number of syms
	len:`int$tpd*cnt*day; / total number of trades
	//syms:`a`b`c`d;
	// len MUST be divisible by 10. slightly inconvinient
	date:asc (raze (`int$(len%10),10i)#(startdate+til 10));
	dt:`$string date;
	time:"t"$raze (cnt*day)#enlist 09:30:00+15*til tpd;
	time+:len?1000;
	sym:len?symlist;
	sy2:len?("ABC";"XYZ";"GHH");
	price:len?100f;
	size:len?1000;
	sym[til count symlist]:symlist;
  	:trades:([] date:date; time:time; sy:sym; sy2:`$sy2; price:price; size:size);
 };
 
// \l C:\projects\kdb\mergedbs.q 
// writepartition["C:/temp/logs/kdb/p1";trades;"crap";2018.01.01]
writepartition:{ [path;table;tablename;mydate] 
  tablepath:raze(path, "/", string mydate, "/", tablename , "/" );
 	(hsym `$tablepath) set table; 
  0N!raze "wrote table: ", tablename, " date: ", string mydate, " records: ", string count table;
 };
 
// \l C:\projects\kdb\mergedbs.q  
// partitionTable["C:/temp/logs/kdb/p1"; createtable[2018.01.01;`a`b`c]; "boo"]
// partitionTable["C:/temp/logs/kdb/p2"; createtable[2018.02.01;`c`d`e]; "boo"]
partitionTable:{[path;table;tablename]   
   0N!"Partitioning table: ", (string count table), " records.";
   table:.Q.en[hsym `$path] table;
   0N!"Sym file: ", (string count value hsym `$raze path,"/","sym"), " elements.";   
   
   {[path;table;tablename;mydate]
   	 table:select from table where date=mydate;
   	 table:delete date from table;
   	 table:update `s#time from `time xasc select from table;
   	 table:update `g#sy from table;
     writepartition[path;table;tablename;mydate];     
   }[path;table;tablename;] each distinct asc table`date;
   
   :table;
 };

//
// sym1:get (hsym `$"C:/temp/logs/kdb/p1","/sym")
// sym2:get (hsym `$"C:/temp/logs/kdb/p2","/sym")
// show sym3:sym1,sym2 where not sym2 in sym1
// (hsym `$"C:/temp/logs/kdb/p3","/sym") set sym3
// get (hsym `$"C:/temp/logs/kdb/p3","/sym")

// find enumerated columns on this
// \l C:\projects\kdb\mergedbs.q 
// findsymbolcolumns["C:/temp/logs/kdb/p1"; 2018.01.01; "boo"]
// .Q.par[hsym `$"C:/temp/logs/kdb/p1"; 2018.01.01; `$"boo"]
findsymbolcolumns:{[path;mydate;tablename]; 	
  tableondiskpath:.Q.par[hsym`$path;mydate;`$tablename];
  // find columns that are `symbol
  columns:{ 
  	$[(`$(((meta x)[y])`t))~`s;:y;`];
  	}[tableondiskpath;] each (key meta tableondiskpath)`c;
  // remove :: columns from result list and return	
  :columns where (`$"")<>{ $[-11=type x;x;`] } each columns;
 };

// \l C:\projects\kdb\mergedbs.q 
// findnonsymbolcolumns["C:/temp/logs/kdb/p1"; 2018.01.01; "boo"]
findnonsymbolcolumns:{[path;mydate;tablename]; 	
  tableondiskpath:.Q.par[hsym`$path;mydate;`$tablename];    
  s1:((key meta tableondiskpath)`c);  
  s2:findsymbolcolumns[path;mydate;tablename];
  :s1 where not s1 in s2;
 };

// reenumeratesymcolumns["C:/temp/logs/kdb/p1"; "C:/temp/logs/kdb/p3"; "boo"; 2018.01.01]
reenumeratesymcolumns:{[pathin;pathout;tablename;mydate];
  tablepathin:.Q.par[hsym`$pathin;mydate;`$tablename];
  tablepathout:.Q.par[hsym`$pathout;mydate;`$tablename];
  symcolumns:findsymbolcolumns[pathin;mydate;tablename];  
  {[tablepathin;pathout;tablepathout;x]
  	column:value get hsym`$raze (string tablepathin,"/",string x);   	
  	column:.Q.en[`$pathout;([]s:column)]`s;
  	(hsym `$raze (string tablepathout,"/",string x)) set column;	
  }[tablepathin;pathout;tablepathout;] each symcolumns;
 };

// copynonsymcolumns["C:/temp/logs/kdb/p1"; "C:/temp/logs/kdb/p3"; "boo";2018.01.01]
copynonsymcolumns:{[pathin;pathout;tablename;mydate];  
  tablepathin:.Q.par[hsym`$pathin;mydate;`$tablename];
  tablepathout:.Q.par[hsym`$pathout;mydate;`$tablename];
  nonsymcolumns:findnonsymbolcolumns[pathin;mydate;tablename];
  // 
  {[tablepathin;pathout;tablepathout;x]
  	(hsym `$raze (string tablepathout,"/",string x)) 
  	set get hsym`$raze (string tablepathin,"/",string x);   	  	
  }[tablepathin;pathout;tablepathout;] each nonsymcolumns;
 };

// copy_d_file["C:/temp/logs/kdb/p1"; "C:/temp/logs/kdb/p3"; "boo"; 2018.01.01 ]
copy_d_file:{[pathin;pathout;tablename;mydate];  
  	src:raze(pathin,"/",string mydate,"/",tablename,"/.d");
  	dst:raze(pathout,"/",string mydate,"/",tablename,"/.d");
  	:system ("cp ",src," ",dst);
 }; 

// \l C:\projects\kdb\mergedbs.q 
// setmergedsymfile["C:/temp/logs/kdb/p1";"C:/temp/logs/kdb/p2";"C:/temp/logs/kdb/p3"]
// get (hsym `$"C:/temp/logs/kdb/p1","/sym")
// get (hsym `$"C:/temp/logs/kdb/p2","/sym")
// get (hsym `$"C:/temp/logs/kdb/p3","/sym")
setmergedsymfile:{ [path1;path2;pathout]
 sym1:get (hsym `$path1,"/sym");
 sym2:get (hsym `$path2,"/sym");
 sym3:sym1,sym2 where not sym2 in sym1;
 `sym set sym3; / set global sym var
 (hsym `$pathout,"/sym") set sym3; // write it out
 :get (hsym `$pathout,"/sym"); // return enumeration
 };

// demo method
// it creates a sample table in one dir, second dir.
demo:{[]

   0N!"begin merge";	
   dir1:"C:/temp/logs/kdb/p1";	
   dir2:"C:/temp/logs/kdb/p2";
   dirmerged:"C:/temp/logs/kdb/p3";

   // create first sample set, different dst dir, different enumeration
   `sym set ();
   t1:createtable[2018.01.01;`a`b`c];
   partitionTable[dir1;t1;"boo"];

   // create second sample set, different dst dir, different enumeration
   `sym set ();
   t2:createtable[2018.02.01;`c`d`e];
   partitionTable[dir2;t2;"boo"];

   // merge sym files and write it out
   0N!"set merge files";
   0N!setmergedsymfile[dir1;dir2;dirmerged];

   // renumerate
   0N!"beginning re-enumeration";
   {[dir1;dirmerged;x] reenumeratesymcolumns[dir1;dirmerged;"boo";x]}[dir1;dirmerged;] each (asc distinct t1`date);
   {[dir2;dirmerged;x] reenumeratesymcolumns[dir2;dirmerged;"boo";x]}[dir2;dirmerged;] each (asc distinct t2`date);

   // other columns
   0N!"copy non sym columns";
   {[dir1;dirmerged;x] copynonsymcolumns[dir1;dirmerged;"boo";x]}[dir1;dirmerged;] each (asc distinct t1`date);
   {[dir2;dirmerged;x] copynonsymcolumns[dir2;dirmerged;"boo";x]}[dir2;dirmerged;] each (asc distinct t2`date);

   // .d file
   0N!"copy .d files";
   {[dir1;dirmerged;x] copy_d_file[dir1;dirmerged;"boo";x]}[dir1;dirmerged;] each (asc distinct t1`date);
   {[dir2;dirmerged;x] copy_d_file[dir2;dirmerged;"boo";x]}[dir2;dirmerged;] each (asc distinct t2`date);

   0N!"end merge, ", raze string ((count distinct t1`date)+(count distinct t2`date)), " partitions";
 };

// compare each date from one directory to merged file
compare:{ 
  s1:{
  	src:get hsym`$raze"C:/temp/logs/kdb/p1/",string x,"/boo";
  	dst:get hsym`$raze"C:/temp/logs/kdb/p3/",string x,"/boo";
  	// check all column values by casting them to string
  	result:(count src)=count where 1={ (string value x[z])~(string value y[z]) }[src;dst;] each til count src;
  	:(x;result);
  } each (2018.01.01+til 10);

  s2:{
  	src:get hsym`$raze"C:/temp/logs/kdb/p2/",string x,"/boo";
  	dst:get hsym`$raze"C:/temp/logs/kdb/p3/",string x,"/boo";
  	// check all column values by casting them to string
  	result:(count src)=count where 1={ (string value x[z])~(string value y[z]) }[src;dst;] each til count src;
  	:(x;result);
  } each (2018.02.01+til 10);
  :s1,s2;
 };

// \l C:\projects\kdb\mergedbs.q
//demo[]; 