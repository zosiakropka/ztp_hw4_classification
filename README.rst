Zaawansowane techniki programistyczne

================================
Praca domowa 4: (Classification)
================================

Problem
=======

W pliku wejściowym znajdziemy size=n+1 elementowych zbiorów liczb. 
Składowe zbiorów przyjmują wartości całkowitoliczbowe. Zbiory zaczynające się 
liczbą większą od 0 to zbiory uczące. Pierwszy element każdego zbioru uczącego 
określa klasę, do której wektor należy. Kolejnych n elementów każdego zbioru to 
składowe wektora. Jeśli pierwszy element zbioru jest równy 0, uczenie zostaje 
zakończone, a wektor n następnych elementów zbioru to wektor testowy. 

Po nauczeniu programu, bazując na wektorach uczących, należy sklasyfikować 
wektory testowe.

Parametr size oraz nazwa pliku podane są jako parametry wejściowe programu.
Liczba wektorów uczących i testowych nie jest znana - poznajemy je poprzez 
odczytanie pliku z danymi. Plik z danymi może mieć format swobodny.

Uruchamianie
============

Kompilacja: ::

	javac –Xlint Classification.java Main.java

Uruchamianie: ::

	java Main <input_file> <n_size>

Wyjście: ::

	Classification result: <result>


Algorytm
========

Do nauczenia programu rozpoznawania wektorów wykorzystałam jednowarstwową sieć 
neuronową. Za każdą klasę wektorów odpowiada jeden neuron sieci wektorowej. 
W nauczonej sieci każdy neuron po otrzymaniu wektora wejściowego do klasyfikacji
zwraca wartość z zakresu [0, 1], określającą prawdopodobieństwo, że badany 
wektor należy do klasy reprezentowanej przez ten neuron. Klasa, którą 
reprezentuje neuron zwracający najwyższą wartość, zostaje uznana za klasę 
wektora badanego. Wartości wyjściowe neuronów stanowią sumę iloczynów wartości 
na wejściu i odpowiadających im wag.


Proces uczenia polega na regulacji wag każdego z wejść neuronów.  Początkowe 
wartości wag są losowe. Następnie dostrajane są o wartość zależną od ustalonego 
z góry wskaźnika uczenia oraz popełnionego błędu (różnicy między wartością 
oczekiwaną a otrzymaną).


