start java DatabaseNode -tcpport 10000 -record 0:0
TIMEOUT 1 > NUL
start java DatabaseNode -tcpport 10001 -connect localhost:10000 -record 1:1
TIMEOUT 1 > NUL
start java DatabaseNode -tcpport 10002 -connect localhost:10001 -record 2:2
TIMEOUT 1 > NUL
start java DatabaseNode -tcpport 10003 -connect localhost:10002 -connect localhost:10001 -connect localhost:10000 -record 3:3
TIMEOUT 1 > NUL
start java DatabaseNode -tcpport 10004 -connect localhost:10003 -connect localhost:10002 -record 4:4
TIMEOUT 1 > NUL
start java DatabaseNode -tcpport 10005 -connect localhost:10004 -connect localhost:10003 -record 5:5
TIMEOUT 1 > NUL
start java DatabaseNode -tcpport 10006 -connect localhost:10005 -connect localhost:10003 -connect localhost:10000 -record 6:6
TIMEOUT 1 > NUL
start java DatabaseNode -tcpport 10007 -connect localhost:10006 -record 7:7
TIMEOUT 1 > NUL
start java DatabaseNode -tcpport 10008 -connect localhost:10007 -connect localhost:10002 -record 8:8
TIMEOUT 1 > NUL
start java DatabaseNode -tcpport 10009 -connect localhost:10006 -record 9:9
TIMEOUT 1 > NUL
start java DatabaseNode -tcpport 10010 -connect localhost:10007 -record 10:10
TIMEOUT 1 > NUL
start java DatabaseNode -tcpport 10011 -connect localhost:10010 -record 11:11
TIMEOUT 1 > NUL
start java DatabaseNode -tcpport 10012 -connect localhost:10011 -connect localhost:10009 -record 12:12
TIMEOUT 1 > NUL
start java DatabaseNode -tcpport 10013 -connect localhost:10012 -record 13:14
TIMEOUT 1 > NUL
start java DatabaseNode -tcpport 10014 -connect localhost:10012 -record 14:14
TIMEOUT 1 > NUL
start java DatabaseNode -tcpport 10015 -connect localhost:10014 -connect localhost:10013 -record 15:15
TIMEOUT 1 > NUL
 
java DatabaseClient -gateway localhost:10009 -operation get-value 0
 
java DatabaseClient -gateway localhost:10015 -operation get-value 0
 
java DatabaseClient -gateway localhost:10000 -operation get-max
 
java DatabaseClient -gateway localhost:10014 -operation set-value 1:15
 
java DatabaseClient -gateway localhost:10014 -operation get-min 
 
java DatabaseClient -gateway localhost:10012 -operation get-max
 
java DatabaseClient -gateway localhost:10010 -operation find-key 1
 
java DatabaseClient -gateway localhost:10002 -operation set-value 15:40
 
java DatabaseClient -gateway localhost:10000 -operation get-max
 
java DatabaseClient -gateway localhost:10004 -operation find-key 19
 
java DatabaseClient -gateway localhost:10004 -operation find-key 14
 
java DatabaseClient -gateway localhost:10008 -operation new-record 20:44
 
java DatabaseClient -gateway localhost:10000 -operation find-key 20
 
java DatabaseClient -gateway localhost:10000 -operation get-value 44
 
java DatabaseClient -gateway localhost:10015 -operation get-value 44
 
java DatabaseClient -gateway localhost:10007 -operation terminate
timeout 1 > NUL
java DatabaseClient -gateway localhost:10008 -operation terminate
timeout 1 > NUL
java DatabaseClient -gateway localhost:10009 -operation terminate
timeout 1 > NUL
java DatabaseClient -gateway localhost:10010 -operation terminate
timeout 1 > NUL
java DatabaseClient -gateway localhost:10011 -operation terminate
timeout 1 > NUL
java DatabaseClient -gateway localhost:10000 -operation terminate
timeout 1 > NUL
java DatabaseClient -gateway localhost:10001 -operation terminate
timeout 1 > NUL
 
java DatabaseClient -gateway localhost:10004 -operation find-key 14
 
java DatabaseClient -gateway localhost:10013 -operation get-max
 
java DatabaseClient -gateway localhost:10005 -operation get-max
 
java DatabaseClient -gateway localhost:10006 -operation get-min
 
java DatabaseClient -gateway localhost:10003 -operation get-min
 
java DatabaseClient -gateway localhost:10013 -operation find-key 12
 
java DatabaseClient -gateway localhost:10013 -operation find-key 40
 
java DatabaseClient -gateway localhost:10002 -operation terminate
timeout 1 > NUL
java DatabaseClient -gateway localhost:10003 -operation terminate
timeout 1 > NUL
java DatabaseClient -gateway localhost:10004 -operation terminate
timeout 1 > NUL
java DatabaseClient -gateway localhost:10005 -operation terminate
timeout 1 > NUL
java DatabaseClient -gateway localhost:10006 -operation terminate
timeout 1 > NUL
java DatabaseClient -gateway localhost:10012 -operation terminate
timeout 1 > NUL
java DatabaseClient -gateway localhost:10013 -operation terminate
timeout 1 > NUL
java DatabaseClient -gateway localhost:10014 -operation terminate
timeout 1 > NUL
java DatabaseClient -gateway localhost:10015 -operation terminate
timeout 1 > NUL

pause