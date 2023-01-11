rem Start 1 network node, then terminate it
start java DatabaseNode -tcpport 9000 -record 1:1 
timeout 1 > NUL
java DatabaseClient -gateway localhost:9000 -operation terminate
