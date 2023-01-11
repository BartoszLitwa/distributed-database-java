rem Start 7 network nodes
start java DatabaseNode -tcpport 9000 -record 1:8
timeout 1 > NUL
start java DatabaseNode -tcpport 9001 -connect localhost:9000 -record 2:7
timeout 1 > NUL
start java DatabaseNode -tcpport 9002 -connect localhost:9001 -record 3:6
timeout 1 > NUL
start java DatabaseNode -tcpport 9003 -connect localhost:9002 -record 4:5
timeout 1 > NUL
start java DatabaseNode -tcpport 9004 -connect localhost:9003 -connect localhost:9000 -record 5:4
timeout 1 > NUL

java DatabaseClient -gateway localhost:9001 -operation get-max
java DatabaseClient -gateway localhost:9001 -operation get-min
timeout 1 > NUL
java DatabaseClient -gateway localhost:9002 -operation terminate
timeout 1 > NUL
java DatabaseClient -gateway localhost:9001 -operation get-max
java DatabaseClient -gateway localhost:9001 -operation get-min
timeout 1 > NUL

java DatabaseClient -gateway localhost:9000 -operation terminate
java DatabaseClient -gateway localhost:9001 -operation terminate
java DatabaseClient -gateway localhost:9003 -operation terminate
java DatabaseClient -gateway localhost:9004 -operation terminate
