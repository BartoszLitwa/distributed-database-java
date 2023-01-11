rem Start 2 network nodes, find min/max and then terminate them
start java DatabaseNode -tcpport 9000 -record 1:1 
timeout 1 > NUL
start java DatabaseNode -tcpport 9001 -connect localhost:9000 -record 2:2 
timeout 1 > NUL

java DatabaseClient -gateway localhost:9000 -operation find-key 2
java DatabaseClient -gateway localhost:9001 -operation find-key 1
timeout 1 > NUL

java DatabaseClient -gateway localhost:9000 -operation terminate
java DatabaseClient -gateway localhost:9001 -operation terminate
