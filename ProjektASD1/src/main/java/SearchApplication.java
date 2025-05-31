import org.javatuples.Pair;

import java.io.File;

/**
 * !!! Podlegać modyfikacji mogę jedynie elementy oznaczone to do. !!!
 */

public class SearchApplication {
    public static void main(String[] args) {

        MorfologyTool mt = new MorfologyTool();
        SearchEngine se = new SearchEngine();
        String[] keyWords = se.readProfiles();
        MDictionary mDict = new MDictionary();
        for(String word : keyWords) {
            mDict.Add(word);
        }
        System.out.println("-------- In dictionary -------");
        for (String word : mDict.getWords())
            System.out.println(word);
        String word1 = "";
        String word2 = "";
        String phrase = "";
        String[] words = null;
        String[] ss = se.readFiles("files", mt);
        for(String s : ss)
            System.out.println(s);
        File folder = new File("files");
        for (final File fileEntry : folder.listFiles()) {
            words = se.readFile(fileEntry);
            mDict.Reset();
            for(String word : words){
                mDict.Find(mt.getConcept(word));
            }
            Pair<String, Integer>[] wordsL = mDict.getAppearedWordsWithCount();
            se.makeIndex(fileEntry, wordsL);
        }
        // wyświetl pliki zawierające dane słowo
        String word = "rakieta";
        System.out.println("--------- Files containing " + word + " --------");
        for(String file : se.getDocsContainingWord(word))
            System.out.println(file);
        // wyświetl pliki zawierające wszystkie słowa
        words = new String[] {"armia", "artyleria", "front", "wojsko"};
        System.out.print("--------- Files containing ");
        for(String w : words)
            System.out.print(w + " ");
        System.out.println(" --------");
        long start = System.nanoTime();
        String[] strings = se.getDocsContainingWords(words);
        long end = System.nanoTime();
        double time = (end - start *1.0)/1_000_000;
        System.out.println("Czas w ms = " + time);
        for(String file : strings)
            System.out.println(file);
        // wyświetl pliki zawierające najwięcej z podanych słów
        words = new String[] {"armia", "artyleria", "front", "generał", "wojsko", "broń", "bitwa", "atakować"};
        System.out.print("--------- Files containing max of: ");
        for(String w : words)
            System.out.print(w + " ");
        System.out.println(" --------");
        for(String file : se.getDocsWithMaxMatchingWords(words, 6))
            System.out.println(file);
        String profileName = "militaria";
        System.out.println("-------- Files closest to the profile: '"+ profileName + "' --------");
        start = System.nanoTime();
        Pair<String, Double> [] files = se.getDocsClosestToProfile(10, profileName);
        end = System.nanoTime();
        time = (end - start *1.0)/1_000_000;
        System.out.println("Czas w ms = " + time);
        for(Pair<String, Double> pair : files)
            System.out.println(pair.getValue0() + ": " + pair.getValue1());

    }
}
