import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Count the frequency of words in a text file and output the result as a tag
 * cloud in lexicographic order. All the words are converted to lower case for
 * the purpose of counting.
 *
 * @author Xiao Zang (zang.58)
 * @author Henrique Gomes Pereira Painhas (gomespereirapainhas.1)
 *
 */
public final class TagCloudJava {

    /**
     * Nested class comparing {@code Map.Entry<String, Integer>}s with
     * descending order of Integer values. If the values of two
     * {@code Map.Entry<String, Integer>}s are tied, they are compared by
     * ascending order of String keys.
     */

    private static class DescendingValueAcendingKey
            implements Comparator<Map.Entry<String, Integer>> {
        @Override
        public int compare(Map.Entry<String, Integer> m1,
                Map.Entry<String, Integer> m2) {
            int result;
            int compareValue = m2.getValue().compareTo(m1.getValue());
            if (compareValue != 0) {
                result = compareValue;
            } else {
                result = m1.getKey().compareTo(m2.getKey());
            }
            return result;
        }
    }

    /**
     * Nested class comparing {@code String}s with ascending order.
     */

    private static class AscendingString implements Comparator<String> {
        @Override
        public int compare(String s1, String s2) {
            return s1.compareTo(s2);
        }
    }

    /**
     * Default constructor--private to prevent instantiation.
     */
    private TagCloudJava() {
        // no code needed here
    }

    /**
     * String of possible separators.
     */
    private static final String SEPARATORS = " `~!@#$%^&*()-_=+{[}]|;:.,<>?/\t"
            + "\b\n\r\f\'\"\\";

    /**
     * Maximum and Minimum font size.
     */
    private static final int MINFONT = 11, MAXFONT = 48;

    /**
     * Generates a set of characters from the unique characters in the given
     * {@code String}.
     *
     * @param str
     *            the given {@code String}
     * @param strSet
     *            the {@code Set} to be replaced
     * @replaces strSet
     * @ensures strSet = unique entries of str
     */

    public static void generateElements(String str, Set<Character> strSet) {
        assert str != null : "Violation of : str is not null";
        assert strSet != null : "Violation of : str is not null";

        strSet.clear();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (!strSet.contains(c)) {
                strSet.add(c);
            }
        }

    }

    /**
     * Returns the first "word" (maximal length string of characters not in
     * {@code separators}) or "separator string" (maximal length string of
     * characters in {@code separators}) in the given {@code text} starting at
     * the given {@code position}.
     *
     * @param text
     *            the {@code String} from which to get the word or separator
     *            string
     * @param position
     *            the starting index
     * @param separators
     *            the {@code Set} of separator characters
     * @return the first word or separator string found in {@code text} starting
     *         at index {@code position}
     * @requires 0 <= position < |text|
     * @ensures
     *
     *          <pre>
     * nextWordOrSeparator =
     *   text[position, position + |nextWordOrSeparator|)  and
     * if entries(text[position, position + 1)) intersection separators = {}
     * then
     *   entries(nextWordOrSeparator) intersection separators = {}  and
     *   (position + |nextWordOrSeparator| = |text|  or
     *    entries(text[position, position + |nextWordOrSeparator| + 1))
     *      intersection separators /= {})
     * else
     *   entries(nextWordOrSeparator) is subset of separators  and
     *   (position + |nextWordOrSeparator| = |text|  or
     *    entries(text[position, position + |nextWordOrSeparator| + 1))
     *      is not subset of separators)
     *          </pre>
     */
    public static String nextWordOrSeparator(String text, int position,
            Set<Character> separators) {
        boolean isSeparator = separators.contains(text.charAt(position));
        int i = position;
        while (i < text.length()
                && (separators.contains(text.charAt(i)) == isSeparator)) {
            i++;
        }
        return text.substring(position, i);
    }

    /**
     * Count the word frequency in {@code String} str separated by special
     * characters defined in {@code Set} separators, and accordingly update
     * {@code Map} freqMap. All the words are converted to lower case for the
     * purpose of counting.
     *
     * @param str
     *            text source for word frequency counting
     * @param freqMap
     *            A {@code Map} storing words and corresponding counts
     * @param separators
     *            A {@code Set} containing special characters serving as
     *            separators
     *
     * @requires freqMap is a {@code Map<String, Integer>}
     * @updates freqMap
     * @ensures
     *
     *          <pre>
     * For each counted word (converted to lower case) in str:
     * If it is already among the keys of
     * {@code Map} freqMap, then add its count in str to the corresponding value
     * in {@code Map} freqMap.
     * If it is not among the keys of {@code Map} freqMap, then add a new
     * {@code Map.Entry} to {@code Map} freqMap, setting the word as the key and
     * its count as the value.
     *          </pre>
     */
    public static void wordCount(String str, Map<String, Integer> freqMap,
            Set<Character> separators) {
        int i = 0;
        while (i < str.length()) {
            String word = nextWordOrSeparator(str, i, separators);
            if (!separators.contains(word.charAt(0))) {
                word = word.toLowerCase();
                if (freqMap.containsKey(word)) {
                    freqMap.put(word, freqMap.get(word) + 1);
                } else {
                    freqMap.put(word, 1);
                }
            }
            i = i + word.length();
        }

    }

    /**
     * Write a word to output stream in html format with its font size linearly
     * related to its frequency. The font size for the word with its frequency
     * equal to minCount is MINFONT, and the font size for the word with its
     * frequency equal to maxCount is MAXFONT. If minCount = maxCount, i.e. all
     * the words have the same count, the font size is (MINFONT + MAXFONT)/2.
     *
     * @param wordFreq
     *            The {@code Map.Entry<String, Integer>} containing a word and
     *            its count.
     * @param output
     *            the output stream
     * @param minCount
     *            the lower bound of word frequency
     * @param maxCount
     *            the upper bound of word frequency
     * @requires output.is_open, 1 <= minCount <= wordFreq.value() <= maxCount
     * @updates output
     * @ensures output.is_open and output.contect = #output.content * [a line in
     *          the html file for the word with the font size of the tag]
     */
    public static void tagOut(Map.Entry<String, Integer> wordFreq,
            PrintWriter output, int minCount, int maxCount) {
        int count = wordFreq.getValue();
        int font;
        if (maxCount > minCount) {
            font = MINFONT + (count - minCount) * (MAXFONT - MINFONT)
                    / (maxCount - minCount);
        } else {
            font = (MAXFONT - MINFONT) / 2;
        }

        output.println("<span style=\"cursor:default\" class=\"f" + font
                + "\" title=\"count: " + count + "\">" + wordFreq.getKey()
                + "</span>");
    }

    /**
     * Move min(sm1.size(), maxTransfer) number of elements from
     * {@code SortedSet<Map.Entry<String, Integer>>} ss to {@code SortedMap} sm,
     * in the order of ss.comparator().
     *
     * @param ss
     *            The {@code SortedSet} to transfer elements from
     * @param sm
     *            The {@code SortedMap} to transfer elements to
     * @param maxTransfer
     *            Maximum number of elements to transfer
     *
     * @return integer array of length 2, with first element being the minimum
     *         value of transferred {@Map.Entry}, and second element being the
     *         maximum value of transferred {@Map.Entry}.
     * @updates ss, sm
     */
    public static int[] transferWords(SortedSet<Map.Entry<String, Integer>> ss,
            SortedMap<String, Integer> sm, int maxTransfer) {
        int minCount = 0, maxCount = 0;
        Iterator<Map.Entry<String, Integer>> iter = ss.iterator();
        Map.Entry<String, Integer> currentWord;
        int wordNum = ss.size();
        while (ss.size() > 0 && sm.size() < maxTransfer) {
            currentWord = iter.next();
            if (ss.size() == wordNum) {
                maxCount = currentWord.getValue();
            }
            sm.put(currentWord.getKey(), currentWord.getValue());
            if (ss.size() == 1 || sm.size() == maxTransfer) {
                minCount = currentWord.getValue();
            }
            iter.remove();
        }
        int[] toReturn = { minCount, maxCount };
        return toReturn;
    }

    /**
     * Write html header and heading to output stream.
     *
     * @param tagNum
     *            total number of tags
     * @param source
     *            source file
     * @param output
     *            output stream
     * @updates output
     */
    public static void printHeading(int tagNum, String source,
            PrintWriter output) {
        output.println("<html>");
        output.println("<head>");
        output.println(
                "<title>Top " + tagNum + " words in " + source + "</title>");
        output.println(
                "<link href=\"http://web.cse.ohio-state.edu/software/2231/web"
                        + "-sw2/assignments/projects/tag-cloud-generator/data/"
                        + "tagcloud.css\" rel=\"stylesheet\" type=\"text/css\">");
        output.println("</head>");
        output.println("<body>");
        output.println("<h2>Top " + tagNum + " words in " + source + "</h2>");
        output.println("<hr>");
        output.println("<div class=\"cdiv\">");
        output.println("<p class=\"cbox\">");
    }

    /**
     * Main method.
     *
     * @param args
     *            the command line arguments; unused here
     */
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        System.out.print(
                "Please enter the name of a text file to count word occurrences: ");
        String source = in.nextLine();
        System.out.print("Please enter the name of the output file: ");
        String outFile = in.nextLine();
        System.out
                .print("Please enter the maximum number of words to be included in the"
                        + " generated tag cloud: ");
        int maxTags = in.nextInt();
        BufferedReader input = null;
        try {
            input = new BufferedReader(new FileReader(source));
        } catch (IOException e) {
            System.err.println("Error opening input file!");
        }

        PrintWriter output = null;
        try {
            output = new PrintWriter(
                    new BufferedWriter(new FileWriter(outFile)));

        } catch (IOException e) {
            System.err.println("Error opening output file!");
        }
        if (input != null && output != null) {
            try {
                Set<Character> separators = new HashSet<Character>();
                generateElements(SEPARATORS, separators);
                Map<String, Integer> freqMap = new HashMap<String, Integer>();
                String nextWord = input.readLine();
                while (nextWord != null) {
                    wordCount(nextWord, freqMap, separators);
                    nextWord = input.readLine();

                }
                SortedSet<Map.Entry<String, Integer>> sortedFreqMap = new TreeSet<Map.Entry<String, Integer>>(
                        new DescendingValueAcendingKey());
                Iterator<Map.Entry<String, Integer>> iter = freqMap.entrySet()
                        .iterator();
                while (iter.hasNext()) {
                    sortedFreqMap.add(iter.next());
                    iter.remove();
                }
                SortedMap<String, Integer> wordsForOutput = new TreeMap<>(
                        new AscendingString());

                int[] countBounds = transferWords(sortedFreqMap, wordsForOutput,
                        maxTags);
                printHeading(wordsForOutput.size(), source, output);
                Iterator<Map.Entry<String, Integer>> printIter = wordsForOutput
                        .entrySet().iterator();
                while (printIter.hasNext()) {
                    tagOut(printIter.next(), output, countBounds[0],
                            countBounds[1]);
                    printIter.remove();
                }
                output.println("</p>");
                output.println("</div>");
                output.println("</body>");
                output.println("</html>");
            } catch (IOException e) {
                System.err.println("Error reading input file!");
            }

            try {
                input.close();
            } catch (IOException e) {
                System.err.println("Error closing input file!");
            }

            output.close();
        }

        in.close();
    }

}
