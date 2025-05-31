/**
 * !!! Podlegać modyfikacji mogę jedynie elementy oznaczone to do. !!!
 */

import org.javatuples.Pair;
import java.io.*;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class SearchEngine {

    private MorfologyTool mt = new MorfologyTool();

    private MDictionary md;

    public SearchEngine() {
        this.md = new MDictionary();
    }

    public String[] readFiles(String directory, MorfologyTool mt) {

        File folder = new File("files");
        HashSet<String> set = new HashSet<>();
        for (final File file : folder.listFiles()) {
            System.out.println(file.toString());
            String[] split = null;
            try {
                FileReader fr = new FileReader(file);
                BufferedReader br = new BufferedReader(fr);
                String line;
                String text = "";
                while ((line = br.readLine()) != null)
                    text += line + " ";
                br.close();
                split = text.split("[^a-zA-Z0-9ąćęłńóśżźĄĆŁŃÓĘŚŻŹ]+");
            } catch (FileNotFoundException e) {
                System.out.println(e.getMessage());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            for (String s : split)
                if (toString().trim().length() > 1)
                    set.add(mt.getConcept(s.toLowerCase()));
        }
        return set.toArray(String[]::new);
    }
    /**
     * Czytanie pliku i jego rozbiór morfologiczny
     * @param file
     * @return
     */
    public String[] readFile(File file) {
        String[] words = new String[100];
        int index = 0;

        try {
            Scanner scanner = new Scanner(file);
            while (scanner.hasNext()) {
                if (index >= words.length) {
                    words = resizeArrayBy100(words);
                }
                words[index++] = scanner.next();
            }
            scanner.close();
        } catch (IOException e) {
            System.out.println("Wystąpił błąd przy odczytywaniu pliku: " + e.getMessage());
        }

        return trimArray(words, index);
    }
    /**
     * Czytanie profili i scalanie ich (merge). Metoda zwraca słownik główny
     *
     */
    public String[] readProfiles() {
        File dir = new File("profiles");
        File[] files;
        if (dir.exists() && dir.isDirectory()) {
            files = dir.listFiles();
        } else {
            return null;
        }
        String[] profiles = new String[files.length];

        for (int i = 0; i < files.length; i++) {
            profiles[i] = files[i].getName();
            if (i >= profiles.length - 1) {
                profiles = resizeArrayBy100(profiles);
            }
        }
        profiles = trimArray(profiles, files.length);


        return profiles;
    }

    /**
     * Zmniejszenie tablicy.
     * @param originalArray
     * @param newSize
     * @return
     */
    public static String[] trimArray(String[] originalArray, int newSize) {
        String[] newArray = new String[newSize];
        System.arraycopy(originalArray, 0, newArray, 0, newSize);
        return newArray;
    }


    /**
     * Rozszerzenie tablicy o 100 elementiow.
     * @param originalArray
     * @return
     */
    public static <T> T[] resizeArrayBy100(T[] originalArray) {
        int newSize = originalArray.length + 100;
        T[] newArray = (T[]) Array.newInstance(originalArray.getClass().getComponentType(), newSize);
        System.arraycopy(originalArray, 0, newArray, 0, originalArray.length);

        return newArray;
    }



    /**
     * Laczenie dwoch tablic.
     * @param t1
     * @param t2
     * @return
     */
    private String[] merge(String[] t1, String[] t2) {
        int diff;
        Comparator<String> comparator = Comparator.naturalOrder();
        int index1 = 0;
        int index2 = 0;
        String[] mergedArr = new String[t1.length+t2.length];
        for(int i = 0; i < t1.length+t2.length; i++){
            if(i > t1.length){
                diff = t2.length-t1.length;
                System.arraycopy(t2, index2, mergedArr, i, diff);
                break;
            }else if(i > t2.length){
                diff = t1.length-t2.length;
                System.arraycopy(t1, index1, mergedArr, i, diff);
                break;
            }
            int result = comparator.compare(t1[index1], t2[index2]);
            if(result > 0){
                mergedArr[i] = t1[index1];
                index1++;
            }else if(result < 0){
                mergedArr[i] = t1[index2];
                index2++;
            }
        }
        mergedArr = trimArray(mergedArr, t1.length+ t2.length);
        return mergedArr;
    }

    /**
     * Wczytanie zawartosci profilu do tablicy.
     * @param profileName
     * @return
     */
    public String[] readProfile(String profileName) {
        String[] words = new String[0];
        int index = 0;

        File file = new File("profiles/" + profileName + ".txt");

        try {
            long lineCount = Files.lines(file.toPath()).count();
            words = new String[(int) lineCount];
            BufferedReader br = new BufferedReader(new FileReader(file));
            String profile = "";
            while ((profile = br.readLine()) != null) {
                words[index++] = profile;
            }
        } catch (IOException e) {
            System.out.println("Wystąpił błąd przy odczytywaniu pliku: " + e.getMessage());
        }
        return trimArray(words, index);
    }

    /**
     * Tworzy plik indeksowy dla danego pliku tekstowego: w każdym wierszu jest pojęcie oraz jego liczba wystąpień w pliku
     * @param fileEntry
     * @param wordsL
     */
    public void makeIndex(File fileEntry, Pair<String, Integer>[] wordsL) {
        String fileName = fileEntry.getName();

        for (Pair<String, Integer> pair : wordsL) {
            String word = pair.getValue0();
            int count = pair.getValue1();

            File wordIndexFile = new File("indices/" + word + ".index");
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(wordIndexFile, true))) {
                bw.write(fileName + " " + count);
                bw.newLine();
            } catch (IOException e) {
                System.out.println("Błąd przy zapisie do pliku: " + wordIndexFile.getName());
                e.printStackTrace();
            }
        }

        String[] parts = fileName.split(".txt");
        File fileIndex = new File("indices/" + parts[0] + ".txt" + ".index");
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileIndex))) {
            for (Pair<String, Integer> pair : wordsL) {
                bw.write(pair.getValue0() + " " + pair.getValue1());
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("Błąd przy zapisie do pliku: " + fileIndex.getName());
            e.printStackTrace();
        }
    }





    public void updateIndex(File fileEntry, String word) {
        // Zakładamy, że każda linia w pliku to "słowo liczba"
        Pair<String, Integer>[] indexEntries = new Pair[100]; // Przykładowy rozmiar tablicy
        int size = 0;

        // Utworzenie obiektu File, który wskazuje na plik w katalogu "indices"
        File fileToRead = new File("indices/" + fileEntry.getName());

        // Odczytanie zawartości pliku
        try (Scanner scanner = new Scanner(fileToRead)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(" ");
                if (parts.length == 2) {
                    String currentWord = parts[0];
                    int count = Integer.parseInt(parts[1]);
                    indexEntries[size++] = Pair.with(currentWord, count);

                    // Zaktualizuj liczbę wystąpień, jeśli to szukane słowo
                    if (currentWord.equals(word)) {
                        indexEntries[size - 1] = Pair.with(currentWord, count + 1);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("Plik nie znaleziony: " + fileToRead.getName());
            return;
        }

        // Zapisanie zmienionej zawartości z powrotem do pliku
        try (PrintWriter writer = new PrintWriter(fileToRead)) {
            for (int i = 0; i < size; i++) {
                writer.println(indexEntries[i].getValue0() + " " + indexEntries[i].getValue1());
            }
        } catch (FileNotFoundException e) {
            System.err.println("Błąd przy zapisie do pliku: " + fileToRead.getName());
        }
    }

    /**
     * Zwraca pliki zawierające podane słowo
     * @param word wyszukiwane słowo
     * @return
     */
    public String[] getDocsContainingWord(String word) {

        File wordIndexFile = new File("indices/" + word + ".index");

        String[] docs = new String[100];
        int count = 0;

        try (Scanner scanner = new Scanner(wordIndexFile)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String x = new String(line.toCharArray(), 0 ,line.length()-2);
                if (count >= docs.length) {
                    docs = resizeArrayBy100(docs);
                }
                docs[count] = x;
                count++;
            }
        } catch (FileNotFoundException e) {
            System.out.println("Nie znaleziono pliku: " + wordIndexFile.getName());
            return null;
        }



        return trimArray(docs, count);
    }


    /**
     * Zwraca dokumenty zawierajace dane slowa.
     * @param words
     * @return
     */
    public String[] getDocsContainingWords(String[] words) {

        String[][] docsForAllWords = new String[words.length][];
        int docsIndex = 0;


        for (String word : words) {
            File wordIndexFile = new File("indices/" + word + ".index");
            String[] docsForWord = new String[100];
            int count = 0;

            try (Scanner scanner = new Scanner(wordIndexFile)) {
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    String[] parts = line.split(".txt");
                    String docName = parts[0] + ".txt";

                    if (count >= docsForWord.length) {
                        docsForWord = resizeArrayBy100(docsForWord);
                    }

                    docsForWord[count++] = docName;
                }
            } catch (FileNotFoundException e) {
                System.out.println("Nie znaleziono pliku: " + wordIndexFile.getName());
                return null;
            }
            docsForWord = trimArray(docsForWord, count);
            docsForAllWords[docsIndex++] = docsForWord;
        }

        String[] commonDocs = docsForAllWords[0];
        for (int i = 1; i < docsForAllWords.length; i++) {
            commonDocs = findCommonDocs(commonDocs, docsForAllWords[i]);
        }


        return commonDocs;

    }


    /**
     * Zwraca czesc wspolna tablic (dokumenty).
     * @param arr1
     * @param arr2
     * @return
     */
    private String[] findCommonDocs(String[] arr1, String[] arr2) {
        String[] commonDocs;
        if(arr1.length <= arr2.length) commonDocs = new String[arr1.length];
        else commonDocs = new String[arr2.length];
        int index = 0;
        for (String doc1 : arr1) {
            for (String doc2 : arr2) {
                if (doc1 != null && doc1.equals(doc2)) {
                    commonDocs[index++] = doc1;
                    break;
                }
            }
        }
        return trimArray(commonDocs, index);
    }

    /**
     * Zwraca n plików zawierających najwięcj poszukiwanych słów
     * @param words
     * @param n
     * @return
     */
    public String[] getDocsWithMaxMatchingWords(String[] words, int n) {

        String[] finalARR = new String[100];

        MDictionary dictionary = new MDictionary();

        String[] arr = readIndices();
        File file;
        String[] zawartosc = {};
        int liczba = 0;
        int count = 0;
        dictionary.Empty();
        for(var elem : arr)
        {
            file = new File("indices/" + elem);
            zawartosc = readFile(file);
            liczba = countCommonWords(zawartosc,words);
            Pair<String, Integer> newPair = new Pair<>(elem,liczba);
            dictionary.addPairToDictionary(newPair);

        }

        Pair<String, Integer>[] wordsL = dictionary.getAppearedWordsWithCount();

        sort(wordsL);

        wordsL = getFirstNElements(wordsL,n);

        for (int i = 0; i < wordsL.length; i++) {
            Pair<String, Integer> pair = wordsL[i];
            finalARR[i] = pair.getValue1() + ". " + pair.getValue0().substring(0,pair.getValue0().length()-6);
            count++;
        }

        finalARR = trimArray(finalARR,count);


        return finalARR;
    }

    /**
     * Zwraca tablice n pierwszych elementow tablicy przekazanej w argumencie.
     * @param wordsL
     * @param n
     * @return
     */
    public Pair<String, Integer>[] getFirstNElements(Pair<String, Integer>[] wordsL, int n) {
        Pair<String, Integer>[] firstNElements = (Pair<String, Integer>[]) Array.newInstance(wordsL.getClass().getComponentType(), n);
        System.arraycopy(wordsL, 0, firstNElements, 0, n);
        return firstNElements;
    }


    /**
     * Zwraca czesc wspolna wyrazow z dwoch tablic.
     * @param array1
     * @param array2
     * @return
     */
    public int countCommonWords(String[] array1, String[] array2) {
        int count = 0;
        for (String word1 : array1) {
            for (String word2 : array2) {
                if (word1.equals(word2)) {
                    count++;
                    break;
                }
            }
        }
        return count;
    }





    /**
     * Zwrócenie n dokumentów z największą zgodnościa z wybranym profilem
     * @param n
     * @return
     */
    public Pair<String,Double>[] getDocsClosestToProfile(int n, String profileName) {
        File folder = new File("files");
        File[] listOfFiles = folder.listFiles();

        Pair<String, Double>[] finalARR = new Pair[listOfFiles.length];

        String[] profileWords = readProfile(profileName);

        for (int i = 0; i < listOfFiles.length; i++) {
            String s = String.valueOf(listOfFiles[i]);
            s = s.replace(".txt",".txt.index");
            File file = new File(s);
            Pair<String, Integer>[] index = readIndex(file, profileWords.length);
            double score = calculateScore(index, profileWords);
            finalARR[i] = Pair.with("files\\"+file.getName().replace(".txt.index",".txt"), score);
        }

        finalARR = sortDouble(finalARR);


        int smaller = n<=finalARR.length?n:finalARR.length;
        return Arrays.copyOfRange(finalARR, 0, smaller);
    }

    /**
     * Liczy zgodnosc.
     * @param index
     * @param profileWords
     * @return
     */
    private double calculateScore(Pair<String, Integer>[] index, String[] profileWords) {
        double totalScore = 0;
            for (Pair<String, Integer> pair : index) {
                    totalScore += Math.log10(pair.getValue1() + 1);
            }
            double result = ((totalScore / profileWords.length) * 100);
            BigDecimal bd = new BigDecimal(result).setScale(1, RoundingMode.HALF_UP);
            result = bd.doubleValue();
        return result;
    }


    /**
     * Czyta count pierwszych lini z danego indeksu.
     * @param file
     * @param count
     * @return
     */
    public Pair<String, Integer>[] readIndex(File file, int count) {
        Pair<String, Integer>[] indexPairs = new Pair[count];
        int index = 0;
        File f = new File("indices/" + file.getName());

        try (Scanner scanner = new Scanner(f)) {
            while (scanner.hasNextLine() && index < count) {
                String line = scanner.nextLine();
                String[] parts = line.split(" ");
                if (parts.length >= 2) {
                    try {
                        String word = parts[0];
                        int wordCount = Integer.parseInt(parts[1]);
                        indexPairs[index++] = Pair.with(word, wordCount);
                    } catch (NumberFormatException e) {
                        System.err.println("Błąd formatu liczbowego w pliku: " + f.getName() + ", w linii: " + line);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("Nie znaleziono pliku: " + f.getName());
        }

        if (index < count) {
            Pair<String, Integer>[] tempPairs = new Pair[index];
            System.arraycopy(indexPairs, 0, tempPairs, 0, index);
            return tempPairs;
        }

        return indexPairs;
    }

    /**
     * Sortowanie QuickSort
     */
    public Pair<String, Integer>[] sort(Pair<String, Integer>[] pairs) {
        quickSort(pairs, 0, pairs.length - 1);
        return pairs;
    }


    private void quickSort(Pair<String, Integer>[] pairs, int low, int high) {
        if (low < high) {
            int pi = partition(pairs, low, high);
            quickSort(pairs, low, pi - 1);
            quickSort(pairs, pi + 1, high);
        }
    }

    private int partition(Pair<String, Integer>[] pairs, int low, int high) {
        int pivot = pairs[high].getValue1();
        int i = (low - 1);
        for (int j = low; j < high; j++) {
            if (pairs[j].getValue1() > pivot) {
                i++;
                Pair<String, Integer> temp = pairs[i];
                pairs[i] = pairs[j];
                pairs[j] = temp;
            }
        }
        Pair<String, Integer> temp = pairs[i + 1];
        pairs[i + 1] = pairs[high];
        pairs[high] = temp;
        return i + 1;
    }


    /**
     * BubbleSort dla double
     */
    private Pair<String, Double>[] sortDouble(Pair<String, Double>[] pairs) {
        boolean swapped;
        for (int i = 0; i < pairs.length - 1; i++) {
            swapped = false;
            for (int j = 0; j < pairs.length - i - 1; j++) {
                if (pairs[j].getValue1() < pairs[j + 1].getValue1()) {
                    Pair<String, Double> temp = pairs[j];
                    pairs[j] = pairs[j + 1];
                    pairs[j + 1] = temp;
                    swapped = true;
                }
            }
            if (!swapped) {
                break;
            }
        }
        return pairs;
    }


    /**
     * Wyczytanie nazw indices.
     */
    public String[] readIndices() {
        File dir = new File("indices");
        if (!dir.exists() || !dir.isDirectory()) {
            return null;
        }

        File[] files = dir.listFiles();
        String[] namesWithSpaces = new String[100];
        int count = 0;

        for (File file : files) {
            if (file.isFile()) {
                String name = file.getName();
                if (name.contains(" ")) {
                    if (count >= namesWithSpaces.length) {
                        namesWithSpaces = resizeArrayBy100(namesWithSpaces);
                    }
                    namesWithSpaces[count++] = name;
                }
            }
        }

        return trimArray(namesWithSpaces, count);
    }


}
