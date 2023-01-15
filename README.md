# Polish Version of Documentation
# Rozproszona baza danych

## Wprowadzenie
Ten projekt to rozproszona baza danych napisana wykorzystujac komunikację o protokole TCP
przy pomocy języka JAVA, który wykorzystuje komunikację TCP do obsługi żądań i odpowiedzi
między węzłami oraz klientem. System składa się z trzech głównych klas: TCPClient, TCPServer i NodeData.
Klasa NodeData reprezentuje węzeł w systemie i wykorzystuje klasy TCPClient i TCPServer
do komunikacji z innymi węzłami. Klasa DatabaseNode reprezentuje węzeł bazy danych -
służy przy tworzeniu nowego węzła bazy danych. Przy uruchamianiu zbiera wszystkie parametry oraz dane,
tworząc nową instancję klasy docelowej NodeData. Klasa TCPClient służy do komunikacji z innymi
węzłami, a klasa TCPServer służy do odbierania żądań od innych węzłów.


## Przepływ komunikacji
Klasa DatabaseNode uruchamia obiekt TCPServer, aby nasłuchiwać połączeń przychodzących na określonym
porcie. Za każdym razem, gdy klient nawiąże połączenie, serwer tworzy nowy wątek, uruchamia go wykonujac operacje,
ktore zostaly wywołane, nie czekając na ich zakończenie od razu czeka na kolejne połączenie od następnego klienta.
Kiedy potrzebuje się skontaktować z innymi węzłami, które zostały podane przy uruchamianiu tego węzłą
uruchamia obiekt TCPClient, który tworzy nowe połączenie z każdym znanym węzłem w celu nawiązania komunikacji
oraz wysłania wszelkich potrzebnych zapytań lub poleceń.
Gdy klient wysyła żądanie do węzła, obiekt TCPServer otrzymuje żądanie i przekazuje je do metody
handleRequestMessages klasy NodeData.
Metoda handleRequestMessages analizuje żądanie i wykonuje odpowiednią operację takie jak sprawdzenie u siebie
lokalnych danych oraz w razie potrzeby prześle żądanie do innych węzłów, czekajac na odpowiedzi.
Jeśli operacja wymaga wysłania żądania do innego węzła, klasa NodeData wykorzystuje obiekt TCPClient,
aby wysłać żądanie i otrzymać odpowiedź.
Klasa NodeData następnie wysyła odpowiedź z powrotem do klienta przez obiekt TCPServer,
odpowiadajac na żadanie oryginalnego klienta, kończąc wątek przy tym wątek. 
Wszystko to odbywa się w jednym wątku, więc nie ma problemu związanego z działaniem wielu klientów
w tym samym czasie.

## Sposób budowy komunikatów
Komunikaty sa wysyłane jako pojedyncze linie tekstu, zakończone znakiem nowej linii.
Każdy komunikat składa się z nazwy operacji, a następnie parametrów operacji, oddzielonych spacjami.
Przykładowo, komunikat wysłany przez klienta w celu znalezienia największej wartosci w bazie danych
może wyglądać następująco:
> java DatabaseClient -gateway localhost:9004 -operation get-max

Węzeł odbiera ten komunikat i rozpoznaje, że jest to żądanie o operację get-max.
Następnie węzeł wysyła żądanie do innych węzłów znanych węzłów, aby znaleźć największą wartość
w bazie danych. Węzeł w tym czasie czeka aż wszystkie odpowiedzi zostaną odebrane.
Każdy węzeł zwraca rekord, który zawiera największą wartość w dla siebie znanej bazie danych, tzn.
porównuje swoją największą wartość z największą wartością, którą otrzymał od innych węzłów i
ją zwraca do węzła, który zapytal o to. Następnie węzeł który, który otrzymał zapytanie 
od klienta zwraca największą wartość do klienta. Można w skrócie powiedzieć, że każdy węzeł
wykonuje operację, a następnie przekazuje żądanie do innych węzłów i jest to proces rekurencyjny.

## Sposób obsługi wielu klientów
Każda operacja jest wykonywana na oddzielnym wątku, tzn. wątek jest tworzony dla każdego klienta,
przez co nie ma problemu z obsługą wielu klientów jednocześnie. Wątek jest tworzony w momencie
nawiązania połączenia przez klienta, a następnie jest zamykany po zakończeniu operacji.
To rozwiązanie pozwala na nie blokowanie wątku głównego, który nasłuchuje na połączenia przychodzące, 
przez co kolejne połączenia mogą być obsługiwane w tym samym czasie.

# Jak skompilować i uruchomić program
Aby skompilować plik .java używając komendy javac z linii poleceń, należy postępować następująco:

1. Otwórz wiersz poleceń (Windows) lub terminal (macOS / Linux).

2. Przejdź do katalogu, w którym znajduje się plik .java, który chcesz skompilować. Możesz to zrobić,
używając komendy cd (np. `cd C:\myproject\src`).

3. Użyj komendy javac nazwa_pliku.java, aby skompilować plik. Na przykład, jeśli plik nazywa 
się Main.java, komenda wyglądać będzie następująco: `javac Main.java`.

4. Jeśli kompilacja przebiegła pomyślnie, zostanie utworzony plik .class o takiej samej nazwie
jak plik .java (np. `Main.class`).

5. Aby uruchomić skompilowany plik, użyj komendy java nazwa_pliku (bez rozszerzenia `.class`),
np. `java Main [ <parametry> ]`.

> Uwaga: Upewnij się, że masz zainstalowane i skonfigurowane środowisko Java Development Kit (JDK)
> na swoim komputerze, aby użyć komendy javac.

# Przykład użycia
## Uruchomienie węzła bazy danych
`java DatabaseNode -tcpport <numer portu TCP> -record <klucz>:<wartość>
  [ -connect <adres>:<port> ]`

gdzie:
- `tcpport` <numer portu TCP> określa numer portu TCP na którym dany węzeł sieci
oczekuje na połączenia od klientów.
- `record <klucz>:<wartość>` oznacza parę liczb całkowitych początkowo
przechowywanych w bazie na danym węźle, gdzie pierwsza to klucz a druga to wartość
związana z tym kluczem. Nie ma wymogu unikalności zarówno klucza jak i wartości.
- `[ -connect <adres>:<port> ]` oznacza listę innych węzłów już będących w sieci, z
którymi dany węzeł ma się połączyć i z którymi może się komunikować w celu wykonywania
operacji. Lista może zawierać wiele węzłów. Dla pierwszego węzła w sieci ta lista jest pusta.
## Przykład wywołania:
`java DatabaseNode -tcpport 9991 -record 17:256 -connect localhost:9990
-connect localhost:9997 -connect localhost:9989`

Oznacza to uruchomienie węzła, który pod kluczem o wartości 17 przechowuje wartość 256,
nasłuchuje na połączenia od klientów lub innych nowych węzłów na porcie 9991 a do podłączenia
siebie do sieci wykorzysta węzły pracujące na komputerze o adresie localhost i portach TCP o
numerach 9990, 9997 i 9989.

## Uruchomienie klienta
`java DatabaseClient -gateway <hostAddress>:<hostPort> -operation <operacja> [ <parametr> ]`

gdzie:
- `gateway <hostAddress>:<hostPort>` oznacza adres i port węzła bazy danych, z którym
klient ma się połączyć.
- `operation <operacja> [ <parametr> ]` oznacza operację, którą klient chce wykonać na bazie danych.
 `[ <parametr> ]` oznacza parametr operacji. Parametr może być opcjonalny, w zależności od operacji.

## Przykład wywołania:
`java DatabaseClient -gateway localhost:9991 -operation get-value 17`

Oznacza uruchomienie klienta, który do połączenia z siecią ma wykorzystać węzeł działający na
komputerze localhost i porcie TCP o numerze 9991. Po połączeniu baza ma podjąć próbę
znalezienia wartości przypisanej do klucza 17.

## Dostępne operacje
| Operacja	| Parametr | Opis |
| --- | --- | --- |
| set-value | `<klucz>:<wartość>`   | Ustawienie nowej wartości (drugi parametr) dla klucza będącego pierwszym parametrem. Wynikiem operacji jest komunikat `OK` jeśli operacja się powiodła lub `ERROR` jeśli baza nie zawiera żadnej pary, w której występuje żądany klucz. Jeśli w bazie jest kilka rekordów o takim samym kluczu, zmianie musi ulec co najmniej jeden z nich. |
| get-value | `<klucz>`             | Pobranie wartości dla klucza będącego parametrem w bazie.Wynikiem operacji jest komunikat składający się z pary `<klucz>:<wartość>` jeśli operacja się powiodła lub `ERROR` jeśli baza nie zawiera żadnej pary, w której występuje żądany klucz. Jeśli baza zawiera więcej niż jedną parę z takim kluczem, tylko jeden wynik musi zostać zwrócony (którykolwiek). |
| find-key	| `<klucz>`             | Zlecenie wyszukania adresu i numeru portu węzła, na którym przechowywany jest rekord o zadanym kluczu. Jeśli taki węzeł istnieje, odpowiedzią jest para postaci `<adres>:<port>` identyfikująca ten węzeł lub komunikat `ERROR` jeśli żaden węzeł  takiego klucza nie posiada. Jeśli baza zawiera więcej niż jedną parę z takim kluczem, tylko jeden wynik musi zostać zwrócony (którykolwiek). |
| get-max	|                       | Znalezienie największej wartości przypisanej do wszystkich kluczy w bazie. Wynikiem operacji jest komunikat składający się z pary `<klucz>:<wartość>`. Jeśli baza  zawiera więcej niż jedną parę z takim kluczem, tylko jeden wynik musi zostać zwrócony (którykolwiek). |
| get-min	|                       | Znalezienie najmniejszej wartości przypisanej do wszystkich kluczy w bazie. Wynikiem operacji jest komunikat składający się z pary `<klucz>:<wartość>`. Jeśli baza zawiera więcej niż jedną parę z takim kluczem, tylko jeden wynik musi zostać zwrócony (którykolwiek). |
| new-record | `<klucz>:<wartość>`  | Zapamiętanie nowej pary klucz:wartość w miejsce pary przechowywanej na węźle, do którego dany klient jest podłączony. Wynikiem tej operacji jest komunikat `OK`. |
| terminate |	                    | Powoduje odłączenie się węzła od sieci poprzez poinformowanie o tym fakcie swoich sąsiadów oraz zakończenie pracy. Sąsiedzi węzła poinformowani o zakończeniu przez niego pracy uwzględniają ten fakt w swoich zasobach i przestają się z nim komunikować. Przed samym zakończeniem pracy węzeł odsyła do klienta komunikat `OK`. |

## Podsumowanie
Ten projekt dostarcza podstawową implementację rozproszonego systemu z wykorzystaniem komunikacji
TCP w języku Java. Demonstruje on, jak różne węzły mogą się ze sobą komunikować i obsługiwać
żądania i odpowiedzi za pomocą klas TCPClient, TCPServer i NodeData. Projekt jest przeznaczony
jako punkt wyjścia do dalszego rozwoju, ponieważ może być ulepszany i dostosowywany do różnych
przypadków użycia.


# English Version of Documentation
# Distributed database

## Introduction
This project is a distributed database written using TCP communication
using the JAVA language, which uses TCP communication to handle requests and responses
between the nodes and the client. The system consists of three main classes: TCPClient, TCPServer and NodeData.
The NodeData class represents a node in the system and uses the TCPClient and TCPServer classes
to communicate with other nodes. The DatabaseNode class represents a database node -
is used when creating a new database node. At startup, it collects all parameters and data,
creating a new instance of the NodeData target class. The TCPClient class is used to communicate with others
nodes, and the TCPServer class is used to receive requests from other nodes.


## Communication flow
The DatabaseNode class runs a TCPServer object to listen for incoming connections on the specified one
port. Each time a client connects, the server creates a new thread, starts it and performs operations,
that have been called, without waiting for their completion, it immediately waits for another connection from the next client.
When it needs to contact other nodes that were specified when starting this node
starts a TCPClient object that creates a new connection with each known node to establish communication
and send any necessary inquiries or commands.
When a client sends a request to a node, the TCPServer object receives the request and passes it to the method
handleRequestMessages of the NodeData class.
The handleRequestMessages method analyzes the request and performs the appropriate operation, such as self check
local data and, if necessary, will forward the request to other nodes, waiting for responses.
If the operation requires sending a request to another node, the NodeData class uses the TCPClient object,
to send a request and receive a response.
The NodeData class then sends the response back to the client via the TCPServer object,
responding to the original client's request, terminating the thread while doing so.
All this is done in a single thread, so there is no problem of running multiple clients
at the same time.

## The way of building messages
Messages are sent as single lines of text, terminated with a newline character.
Each message consists of the operation name followed by the operation parameters, separated by spaces.
For example, a message sent by a client to find the largest value in a database
may look like this:
> java DatabaseClient -gateway localhost:9004 -operation get-max

The node receives this message and recognizes that it is a request for a get-max operation.
The node then sends a request to other nodes of known nodes to find the largest value
in the database. The node at this time waits until all responses are received.
Each node returns the record that contains the largest value in the known database, i.e.
compares its highest value with the highest value it received from other nodes i
returns it to the node that asked for it. Then the node that received the query
from the customer returns the greatest value to the customer. In short, you can say that every node
it performs an operation and then forwards the request to other nodes and it is a recursive process.

## How we handle multiple clients
Each operation is performed on a separate thread, i.e. a thread is created for each client,
which makes it easy to handle multiple clients at the same time. The thread is created at the moment
client connects and then closes when the operation is complete.
This solution allows you not to block the main thread that listens for incoming connections,
so that subsequent calls can be handled at the same time.

# How to compile and run the program
To compile a .java file using the javac command from the command line, follow these steps:

1. Open Command Prompt (Windows) or Terminal (macOS/Linux).

2. Go to the directory where the .java file you want to compile is located. You can do it,
   using the cd command (e.g. `cd C:\myproject\src`).

3. Use the javac filename.java command to compile the file. For example, if the file is named
   Main.java appears, the command will look like this: `javac Main.java`.

4. If the compilation was successful, a .class file with the same name will be created
   like a .java file (e.g. `Main.class`).

5. To run the compiled file, use the command java file_name (without the `.class` extension),
   e.g. `java Main [ <parameters> ]`.

> Note: Make sure you have the Java Development Kit (JDK) installed and configured on your machine to use the javac command.

# Example of use
## Starting the database node
`java DatabaseNode -tcpport <TCP port number> -record <key>:<value>
[ -connect <address>:<port> ]`

where:
- `tcpport` <TCP port number> specifies the number of the TCP port on which the given network node
  waiting for calls from customers.
- `record <key>:<value>` means a pair of integers initially
  stored in the database on a given node, where the first is the key and the second is the value
  associated with this key. There is no requirement for both key and value to be unique.
- `[ -connect <address>:<port> ]` means a list of other nodes already in the network, from
  with which a given node is to connect and communicate with for execution
  operation. The list can contain multiple nodes. For the first node in the network, this list is empty.
## Call example:
`java DatabaseNode -tcpport 9991 -record 17:256 -connect localhost:9990
-connect localhost:9997 -connect localhost:9989`

This means starting a node that holds the value 256 under the key value 17,
listens for connections from clients or other new nodes on port 9991 a for connection
himself to the network will use nodes working on a computer with the address localhost and TCP ports o
numbers 9990, 9997 and 9989.

## Starting the client
`java DatabaseClient -gateway <hostAddress>:<hostPort> -operation <operation> [ <parameter> ]`

where:
- `gateway <hostAddress>:<hostPort>` means the address and port of the database node with which
  client is about to connect.
- `operation <operation> [ <parameter> ]` means the operation that the client wants to perform on the database.
  `[ <parameter> ]` means the operation parameter. The parameter may be optional, depending on the operation.

## Call example:
`java DatabaseClient -gateway localhost:9991 -operation get-value 17`

It means launching a client that is to use the node operating on it to connect to the network
localhost computer and TCP port number 9991. After connecting, the database should try
finding the value assigned to key 17.

## Operations available
| operation | Parameter | Description |
| --- | --- | --- |
| set-value | `<key>:<value>` | Setting a new value (second parameter) for the key being the first parameter. The result of the operation is the message `OK` if the operation was successful or `ERROR` if the database does not contain any pair in which the requested key occurs. If there are several records with the same key in the database, at least one of them must change. |
| get-value | `<key>` | Retrieving the value for the key being a parameter in the database. The result of the operation is a message consisting of the pair `<key>:<value>` if the operation was successful or `ERROR` if the database does not contain any pair in which the required key occurs. If the database contains more than one pair with such a key, only one result needs to be returned (any one). |
| find-key | `<key>` | an order to search for the address and port number of the node on which the record with the given key is stored. If such a node exists, the response is a pair of the form `<address>:<port>` identifying the node, or an `ERROR` message if no node has such a key. If the database contains more than one pair with such a key, only one result needs to be returned (any one). |
| get-max | | finding the largest value assigned to all keys in the database. The result of the operation is a message consisting of the pair `<key>:<value>`. If the database contains more than one pair with such a key, only one result needs to be returned (any one). |
| get-min | | Finding the smallest value assigned to all keys in the database. The result of the operation is a message consisting of the pair `<key>:<value>`. If the database contains more than one pair with such a key, only one result needs to be returned (any one). |
| new record | `<key>:<value>` | Saving a new key:value pair in place of the pair stored on the node to which the client is connected. The result of this operation is the message `OK`. |
| terminate | | It causes the node to disconnect from the network by informing its neighbors about this fact and ending its work. Neighbors of the node informed about the completion of its work include this fact in their resources and stop communicating with it. Just before the end of work, the node sends an `OK` message to the client. |

## Summary
This project provides a basic implementation of a distributed system using communications
TCP in Java. It demonstrates how different nodes can communicate and operate with each other
requests and responses using the TCPClient, TCPServer, and NodeData classes. The project is intended
as a starting point for further development as it can be improved and adapted to different
use cases.
