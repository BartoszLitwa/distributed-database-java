start java DatabaseNode -tcpport 10000 -record 0:0
timeout 1 > NUL
start java DatabaseNode -tcpport 10001 -connect localhost:10000 -record 1:1
timeout 1 > NUL
start java DatabaseNode -tcpport 10002 -connect localhost:10001 -record 2:2
timeout 1 > NUL
start java DatabaseNode -tcpport 10003 -connect localhost:10002 -connect localhost:10001 -connect localhost:10000 -record 3:3
timeout 1 > NUL
start java DatabaseNode -tcpport 10004 -connect localhost:10003 -connect localhost:10002 -record 4:4
timeout 1 > NUL
start java DatabaseNode -tcpport 10005 -connect localhost:10004 -connect localhost:10003 -record 5:5
timeout 1 > NUL
start java DatabaseNode -tcpport 10006 -connect localhost:10005 -connect localhost:10003 -connect localhost:10000 -record 6:6
timeout 1 > NUL
start java DatabaseNode -tcpport 10007 -connect localhost:10006 -record 7:7
timeout 1 > NUL
start java DatabaseNode -tcpport 10008 -connect localhost:10007 -connect localhost:10002 -record 8:8
timeout 1 > NUL
start java DatabaseNode -tcpport 10009 -connect localhost:10006 -record 9:9
timeout 1 > NUL
start java DatabaseNode -tcpport 10010 -connect localhost:10007 -record 10:10
timeout 1 > NUL
start java DatabaseNode -tcpport 10011 -connect localhost:10010 -record 11:11
timeout 1 > NUL
start java DatabaseNode -tcpport 10012 -connect localhost:10011 -connect localhost:10009 -record 12:12
timeout 1 > NUL
start java DatabaseNode -tcpport 10013 -connect localhost:10012 -record 13:14
timeout 1 > NUL
start java DatabaseNode -tcpport 10014 -connect localhost:10012 -record 14:14
timeout 1 > NUL
start java DatabaseNode -tcpport 10015 -connect localhost:10014 -connect localhost:10013 -record 15:15
timeout 1 > NUL