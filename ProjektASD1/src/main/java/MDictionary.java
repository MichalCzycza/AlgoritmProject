/**
 * !!! Podlegać modyfikacji mogę jedynie elementy oznaczone to do. !!!
 */

import org.javatuples.Pair;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.Scanner;

public class MDictionary {

    private Pair<String, Integer>[] dictionary;
    private int size;
    private static final int MAX_SIZE = 30_000; // Maksymalna wielkość słownika\
    private MorfologyTool mt = new MorfologyTool();

    public MDictionary() {
        dictionary = new Pair[MAX_SIZE];
        size = 0;
    }

    /**
     Opróżnia słownik i zwalnia pamięć po kolekcjach słownikowych
     // <remarks>Metoda przydatna na zakończenie Dictu lub przed ponownym załadowaniem</remarks>
     **/
    public void Empty()
    {
        dictionary = new Pair[MAX_SIZE];
        size = 0;
    }

    /**
     Metoda zeruje liczbę wystąpień pojęć w słowniku
     **/
    public void Reset()
    {
        for (int i = 0; i < size; i++) {
            dictionary[i] = Pair.with(dictionary[i].getValue0(), 0);
        }
    }

    /**
     Dodanie pojęcia do słownika na podstawie słowa i numeru klucza haszowego
     **/
    private int Add(String W, int h) {
        String filePath = "profiles/" + W;
        File file = new File(filePath);
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNext()) {
                String word = scanner.next();
                addWordToDictionary(word,h);
            }
        } catch (FileNotFoundException e) {
            System.out.println("Nie można znaleźć pliku: " + W);
            return -1;
        }

        return h;
    }


    /**
     * Dodaje do slownika slowo o danym hashu.
     * @param word
     * @param hash
     * @return
     */
    private void addWordToDictionary(String word, int hash) {
        for (int i = 0; i < size; i++) {
            if (dictionary[i] != null && dictionary[i].getValue0().equals(word)) {
                int currentCount = dictionary[i].getValue1();
                dictionary[i] = Pair.with(word, currentCount + 1);
                return;
            }
        }

        if (size < MAX_SIZE) {
            dictionary[size++] = Pair.with(word, 1);
        }
    }

    /**
     /// Dodanie pojęcia do słownika na podstawie słowa
     **/
    public int Add(String W) {
        int hash = Haszuj(W);
        return Add(W, hash);
    }

    /**
     Podaje klucz dla danego słowa
     **/
    private int Haszuj(String W)
    {
        return Math.abs(W.hashCode()) % 19137;
    }

    /**
     Metoda zwraca numer słowa lub 0 i zwiększa liczbę wystąpień
     **/
    private int Find(String W, int h)
    {
        for (int i = 0; i < size; i++) {
            if (dictionary[i].getValue0().equals(W)) {
                int newCount = dictionary[i].getValue1() + 1;
                dictionary[i] = Pair.with(dictionary[i].getValue0(), newCount);
                return newCount;
            }
        }
        return 0;
    }

    /**
     Metoda zwraca numer słowa lub 0 i zwiększa liczbę wystąpień o n
     **/
    public int FindAndAdd(String W, int n)
    {
        for (int i = 0; i < size; i++) {
            if (dictionary[i].getValue0().equals(W)) {
                // Zwiększenie liczby wystąpień o n
                dictionary[i] = Pair.with(W, dictionary[i].getValue1() + n);
                return dictionary[i].getValue1();
            }
        }
        if (size < dictionary.length) {
            dictionary[size++] = Pair.with(W, n);
            return n;
        }
        return 0;
    }

    /** <summary>
     Metoda zwraca numer słowa lub 0 i zwiększa liczbę wystąpień
     **/
    public int Find(String W)
    {
        int hash = Haszuj(W);

        if(W.endsWith(",") || W.endsWith(".") || W.endsWith("!")) W = W.substring(0,W.length()-1);
        W = W.toLowerCase();
        W = mt.getConcept(W);

        for (int i = 0; i < size; i++) {
            if (dictionary[i] != null && dictionary[i].getValue0().equals(W)) {
                int count = dictionary[i].getValue1() + 1;
                dictionary[i] = Pair.with(W, count);
                return i;
            }
        }


        return -1;
    }

    /**
     * Zwraca słowa w słowniku
     */
    public String[] getWords() {
        String[] words = new String[size];
        for (int i = 0; i < size; i++) {
            words[i] = dictionary[i].getValue0();
        }
        return words;
    }

    /**
     * Zwraca słowa, które wystąpiły w dokumencie
     * @return
     */
    public String[] getAppearedWords() {
        return null;
        //tutaj korzystam z readFile();
    }


    /**
     * Zwraca pojęcia, które wystąpiły oraz liczba wystąpień
     * @return
     */
    public Pair<String, Integer>[] getAppearedWordsWithCount() {
        int count = 0;
        for (Pair<String, Integer> pair : dictionary) {
            if (pair != null && pair.getValue1() > 0) {
                count++;
            }
        }
        Pair<String, Integer>[] result = new Pair[count];
        int index = 0;
        for (Pair<String, Integer> pair : dictionary) {
            if (pair != null && pair.getValue1() > 0) {
                result[index++] = pair;
            }
        }
        return result;
    }


    /**
     * Dodanie pary do slownika.
     * @param newPair
     * @void
     */
    public void addPairToDictionary(Pair<String, Integer> newPair) {
        Pair<String, Integer>[] newDictionary = new Pair[dictionary.length + 1];
        System.arraycopy(dictionary, 0, newDictionary, 0, dictionary.length);
        newDictionary[dictionary.length] = newPair;
        dictionary = newDictionary;
    }

}

