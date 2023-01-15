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
odpowiadajac na żadanie oryginalnego klienta.

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

> Uwaga: Upewnij się, że masz zainstalowane i skonfigurowane środowisko Java Development Kit (JDK) na swoim komputerze, aby użyć komendy javac.

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
| find-key	| `<klucz>`             | zlecenie wyszukania adresu i numeru portu węzła, na którym przechowywany jest rekord o zadanym kluczu. Jeśli taki węzeł istnieje, odpowiedzią jest para postaci `<adres>:<port>` identyfikująca ten węzeł lub komunikat `ERROR` jeśli żaden węzeł  takiego klucza nie posiada. Jeśli baza zawiera więcej niż jedną parę z takim kluczem, tylko jeden wynik musi zostać zwrócony (którykolwiek). |
| get-max	|                       | znalezienie największej wartości przypisanej do wszystkich kluczy w bazie. Wynikiem operacji jest komunikat składający się z pary `<klucz>:<wartość>`. Jeśli baza  zawiera więcej niż jedną parę z takim kluczem, tylko jeden wynik musi zostać zwrócony (którykolwiek). |
| get-min	|                       | Znalezienie najmniejszej wartości przypisanej do wszystkich kluczy w bazie. Wynikiem operacji jest komunikat składający się z pary `<klucz>:<wartość>`. Jeśli baza zawiera więcej niż jedną parę z takim kluczem, tylko jeden wynik musi zostać zwrócony (którykolwiek). |
| new-record | `<klucz>:<wartość>`  | Zapamiętanie nowej pary klucz:wartość w miejsce pary przechowywanej na węźle, do którego dany klient jest podłączony. Wynikiem tej operacji jest komunikat `OK`. |
| terminate |	                    | Powoduje odłączenie się węzła od sieci poprzez poinformowanie o tym fakcie swoich sąsiadów oraz zakończenie pracy. Sąsiedzi węzła poinformowani o zakończeniu przez niego pracy uwzględniają ten fakt w swoich zasobach i przestają się z nim komunikować. Przed samym zakończeniem pracy węzeł odsyła do klienta komunikat `OK`. |

## Podsumowanie
Ten projekt dostarcza podstawową implementację rozproszonego systemu z wykorzystaniem komunikacji
TCP w języku Java. Demonstruje on, jak różne węzły mogą się ze sobą komunikować i obsługiwać
żądania i odpowiedzi za pomocą klas TCPClient, TCPServer i NodeData. Projekt jest przeznaczony
jako punkt wyjścia do dalszego rozwoju, ponieważ może być ulepszany i dostosowywany do różnych
przypadków użycia.
