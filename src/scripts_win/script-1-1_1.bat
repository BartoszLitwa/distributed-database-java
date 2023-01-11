rem Start 1 network node, then find a key using it
start java DatabaseNode -tcpport 9000 -record 1:1 
timeout 1 > NUL
java DatabaseClient -gateway localhost:9000 -operation get-value 1
java DatabaseClient -gateway localhost:9000 -operation terminate
