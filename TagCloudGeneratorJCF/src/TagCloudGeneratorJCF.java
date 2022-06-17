import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

/**
 * Program to generate a Tag Cloud from an input file with the most occurrence N
 * times in alphabetic order.
 *
 * @author Joseph Antonios and Giridhar Srikanth
 */
public final class TagCloudGeneratorJCF {

    /**
     * Compare {@code Map.Entry.key}s in ascending order.
     */
    private static class KeyComparator
            implements Comparator<Map.Entry<String, Integer>> {
        @Override
        public int compare(Map.Entry<String, Integer> o1,
                Map.Entry<String, Integer> o2) {
            return o1.getKey().compareToIgnoreCase(o2.getKey());
        }
    }

    /**
     * Compare {@code Map.Entry.value}s in descending order.
     */
    private static class ValueComparator
            implements Comparator<Map.Entry<String, Integer>> {
        @Override
        public int compare(Map.Entry<String, Integer> o1,
                Map.Entry<String, Integer> o2) {
            return o2.getValue().compareTo(o1.getValue());
        }
    }

    /**
     * Definition of whitespace separators.
     */
    private static final String SEPARATORS = " \t\n\r,-.!?[]';:/()";

    /**
     * Maximum value for font sizes.
     */
    private static final int MAX_FONT = 48;

    /**
     * Minimum value for font sizes.
     */
    private static final int MIN_FONT = 11;

    /**
     * Default constructor--private to prevent instantiation.
     */
    private TagCloudGeneratorJCF() {
        // No code needed here.
    }

    /**
     * Outputs the "opening" tags in the generated HTML file with the proper
     * link to the tag cloud.
     *
     * @param out
     *            the output stream
     * @param fileName
     *            name of the input file
     * @param num
     *            the number containing the top occurrences of words
     * @updates out.content
     * @ensures out.content = #out.content * [the HTML "opening" tags and link
     *          to tag cloud]
     */
    public static void outputHeader(PrintWriter out, String fileName, int num) {
        assert out != null : "Violation of: out is not null";
        assert fileName != null : "Violation of: fileName is not null";

        // Printing out the HTML with the necessary headers
        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head>");

        out.println("<title>Top " + num + " words in " + fileName + "</title>");

        // Creating the proper link to the tag cloud
        out.println("<link href=\"http://web.cse.ohio-state.edu/software/"
                + "2231/web-sw2/assignments/projects/tag-cloud-generator/"
                + "data/tagcloud.css\" rel=\"stylesheet\" type=\"text/css\""
                + ">\r\n");
        out.println("<link href=\"tagcloud.css\" rel=\"stylesheet\" type=\""
                + "text/css\">\r\n");

        out.println("</head>");
        out.println("<body>");
    }

    /**
     * Writes the HTML tag for the tag cloud fonts to an output stream.
     *
     * @param out
     *            output stream
     * @param name
     *            name of input file
     * @param num
     *            number of words in tag cloud
     * @param wordList
     *            list of Map.Entries with the words and their occurrences
     * @param fontMap
     *            map of words and their font size
     * @updates out
     * @requires num >= 0 and num <= |wordList|
     * @ensures out.content = #out.content * [HTML code of the tag cloud
     *          generator]
     */
    private static void outputHTML(PrintWriter out, String name, int num,
            List<Map.Entry<String, Integer>> wordList,
            Map<String, Integer> fontMap) {
        assert out != null : "Violation of: out is not null";
        assert name != null : "Violation of: fileName is not null";
        assert wordList != null : "Violation of: wordList is not null";
        assert num >= 0 : "Violation of: num >= 0";
        assert num <= wordList.size() : "Violation of: num <= the number of "
                + "words in the file";
        assert fontMap != null : "Violation of: fontMap is not null";

        outputHeader(out, name, num);

        // Printing out the header of the page
        out.println("<h2>Top " + num + " words in " + name + "</h2>");
        out.println("<hr>");
        out.println("<div class=\"cdiv\">");
        out.println("<p class=\"cbox\">");

        for (int i = 0; i < num; i++) {
            Map.Entry<String, Integer> entry = wordList.get(i);

            // Printing out the word and count in tag cloud format
            out.println("<span style=\"cursor:default\" class=\"f"
                    + fontMap.get(entry.getKey()) + "\" title=\"count: "
                    + entry.getValue() + "\">" + entry.getKey() + "</span>");
        }

        outputFooter(out);
    }

    /**
     * Outputs the "closing" tags in the generated HTML file.
     *
     * @param out
     *            the output stream
     * @updates out.contents
     * @ensures out.content = #out.content * [the HTML "closing" tags]
     */
    public static void outputFooter(PrintWriter out) {
        assert out != null : "Violation of: out is not null";

        // Printing out the HTML with the necessary footers
        out.println("</p>");
        out.println("</div>");
        out.println("</body>");
        out.println("</html>");
    }

    /**
     * Returns the first "word" (maximal length string of characters not in
     * {@code SEPARATORS}) or "separator string" (maximal length string of
     * characters in {@code SEPARATORS}) in the given {@code text} starting at
     * the given {@code position}.
     *
     * @param text
     *            the {@code String} from which to get the word or separator
     *            string
     * @param position
     *            the starting index
     * @return the first word or separator string found in {@code text} starting
     *         at index {@code position}
     * @requires 0 <= position < |text|
     * @ensures <pre>
     * nextWordOrSeparator =
     *   text[position, position + |nextWordOrSeparator|)  and
     * if entries(text[position, position + 1)) intersection entries(SEPARATORS) = {}
     * then
     *   entries(nextWordOrSeparator) intersection entries(SEPARATORS) = {}  and
     *   (position + |nextWordOrSeparator| = |text|  or
     *    entries(text[position, position + |nextWordOrSeparator| + 1))
     *      intersection entries(SEPARATORS) /= {})
     * else
     *   entries(nextWordOrSeparator) is subset of entries(SEPARATORS)  and
     *   (position + |nextWordOrSeparator| = |text|  or
     *    entries(text[position, position + |nextWordOrSeparator| + 1))
     *      is not subset of entries(SEPARATORS))
     * </pre>
     */
    private static String nextWordOrSeparator(String text, int position) {
        assert text != null : "Violation of: text is not null";
        assert 0 <= position : "Violation of: 0 <= position";
        assert position < text.length() : "Violation of: position < |text|";

        int ind = position;
        boolean sep = SEPARATORS.contains(text.substring(ind, ind + 1));

        while (ind < text.length()
                && sep == SEPARATORS.contains(text.substring(ind, ind + 1))) {
            ind++;
        }

        return text.substring(position, ind);
    }

    /**
     * Tokenizes the entire input getting rid of all whitespace separators and
     * adding the tokens into {@code wordMap} with the occurrence of each token.
     *
     * @param in
     *            the input stream
     * @param wordMap
     *            map that will contain every words and occurrences from
     *            {@code in}
     * @replaces wordMap
     * @requires in.is_open
     * @ensures <pre>
     * wordMap =
     *   [the non-whitespace tokens in #in.content, occurrence of each token]
     *   and in.content = <>
     * </pre>
     */
    public static void getWordMap(BufferedReader in,
            Map<String, Integer> wordMap) {
        assert in != null : "Violation of: in is not null";
        assert wordMap != null : "Violation of: wordMap is not null";

        wordMap.clear();

        // Checking if the input file can be read
        try {
            String word = in.readLine();

            // Running a loop till the end of the input
            while (word != null) {
                int position = 0;

                // Running a loop until the whole line is read
                while (position < word.length()) {
                    String token = nextWordOrSeparator(word, position);
                    token = token.toLowerCase();

                    // Only adding words (regardless of case) into the map
                    if (!SEPARATORS.contains(token.substring(0, 1))) {
                        if (!wordMap.containsKey(token)) {
                            wordMap.put(token, 1);
                        } else {
                            // Updating the counter for a duplicate word
                            int newValue = wordMap.remove(token) + 1;
                            wordMap.put(token, newValue);
                        }
                    }

                    position += token.length();
                }

                word = in.readLine();
            }
        } catch (IOException e) {
            System.err.println("Error reading from input file");
            return;
        }
    }

    /**
     * Finds a font size proportional to the occurrences of the word and stores
     * that value in a map.
     *
     * @param wordList
     *            list that contain Map pairs
     * @param map
     *            key values and their associated font size
     * @param max
     *            the max value for the number of occurrences
     * @param min
     *            the min value for the number of occurrences
     * @replaces map
     * @ensures Map.get_value = ((a-b)(x-min)/(max - min)) + a
     */
    private static void mapFont(List<Map.Entry<String, Integer>> wordList,
            Map<String, Integer> map, int min, int max) {
        assert wordList != null : "Violation of: wordList is not null";
        assert map != null : "Violation of: map is not null";

        map.clear();
        int altMin = min;

        // To prevent division by 0
        if (min == max) {
            altMin--;
        }

        for (int i = 0; i < wordList.size(); i++) {
            Map.Entry<String, Integer> entryTmp = wordList.get(i);

            /*
             * Generating a font size proportional to the occurrences of words
             * formula: ((a-b)(x-min)/(max - min)) + a, where a = greatest
             * occurred word in the map, b = least occurred word in the map, x =
             * occurrences of current word in the map, max = 48 (max font size),
             * and min = 11 (min font size)
             */
            int font = (((MAX_FONT - MIN_FONT) * (entryTmp.getValue() - altMin))
                    / (max - altMin)) + MIN_FONT;
            map.put(entryTmp.getKey(), font);
        }
    }

    /**
     * Updating the list to contain only the n occurrences required.
     *
     * @param list
     *            list containing a Map.Entries of words and occurrences
     * @param n
     *            integer to display the top words
     * @requires n >= 0 and n < |list|
     * @updates list
     * @ensures list contains the first n Map.Entries
     */
    private static void removeNum(List<Map.Entry<String, Integer>> list,
            int n) {
        assert list != null : "Violation of: list is not null";
        assert n >= 0 : "Violation of: num >= 0";
        assert n <= list
                .size() : "Violation of: num <= the number of words in the file";

        List<Map.Entry<String, Integer>> tmpList = new ArrayList<>();

        // Adding to a temporary list to add only the first n values
        for (int i = 0; i < n; i++) {
            tmpList.add(list.get(i));
        }

        list.clear();

        for (int i = 0; i < tmpList.size(); i++) {
            list.add(tmpList.get(i));
        }
    }

    /**
     * Main method.
     *
     * @param args
     *            the command line arguments; unused here
     */
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);

        // Prompting the user for an input file
        System.out.print("Enter an input file: ");
        String inFile = in.nextLine();

        BufferedReader inputFile;

        // Error-checking opening the input file
        try {
            inputFile = new BufferedReader(new FileReader(inFile));
        } catch (IOException e) {
            System.err.println("Inavlid input file. Unable to open it.");
            return;
        }

        // Prompting the user for an output file
        System.out.print("Enter an output file: ");
        String outFile = in.nextLine();

        PrintWriter outputFile;

        // Error checking opening the output file
        try {
            outputFile = new PrintWriter(
                    new BufferedWriter(new FileWriter(outFile)));
        } catch (IOException e) {
            System.err.println("Inavlid output file. Unable to open it.");
            return;
        }

        // Loading the words and occurrences into a map
        Map<String, Integer> wordMap = new HashMap<>();
        getWordMap(inputFile, wordMap);

        int num = -1;
        int length = wordMap.size();

        // Error-checking to prompt the user for a positive integer
        while (num < 0 || num > length) {
            System.out.print(
                    "Enter a positive number (less than the number of words in the file: "
                            + length
                            + ") of words to be included in the tag cloud: ");
            num = in.nextInt();
        }

        System.out.println();

        /*
         * Creating two kinds of comparators where the first one is to arrange
         * the Map.Entries based on the number of occurrences (decreasing order)
         * and then the Map.Entries in ascending order based of the top
         * occurrences
         */
        Comparator<Map.Entry<String, Integer>> descendingOrder = new ValueComparator();
        Comparator<Map.Entry<String, Integer>> ascendingOrder = new KeyComparator();

        // List to store the most occurrences
        List<Entry<String, Integer>> wordList = new ArrayList<>(
                wordMap.entrySet());
        Collections.sort(wordList, descendingOrder);

        // Updating the list to contain the n occurrences
        removeNum(wordList, num);

        // Obtaining the minimum and maximum values
        int max = 0;
        int min = 0;

        // Preventing out of bounds errors
        if (!wordList.isEmpty()) {
            max = wordList.get(0).getValue();
            min = wordList.get(wordList.size() - 1).getValue();
        }

        // Alphabetizing the list
        Collections.sort(wordList, ascendingOrder);

        // Map to map words to font values
        Map<String, Integer> font = new HashMap<>();

        // Generating a font map with numbers > 0 only
        mapFont(wordList, font, min, max);

        // Output the HTML page
        outputHTML(outputFile, inFile, num, wordList, font);

        in.close();
        outputFile.close();

        // Error-checking closing the input file
        try {
            inputFile.close();
        } catch (IOException e) {
            System.err.println("Error when closing the input file.");
            return;
        }
    }
}
