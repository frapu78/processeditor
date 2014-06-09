/**
 *
 * Process Editor - inubit Workbench Natural Plugin Package
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.gui.plugins.natural;

import java.util.StringTokenizer;

/**
 *
 * @author fpu
 */
public class NaturalLanguageParser {

    public static String[] VERB_LIST = { "add", "remove" };
    public static String [] OBJECT_LIST = { "participant", "task", "choice" };
    public static String [] SUBJECT_MARKER_LIST = { "to", "from", "of" };

    /**
     * Parses a sentence in natural language and returns the result.
     * @param input
     * @return
     */
    public static Sentence parseNaturalLanguageString(String input) throws NaturalLanguageParseException {

        // 1. Convert input to lower case
        input = input.toLowerCase();

        // 2. Preprocess (normalize, remove optional words etc.)
        input = preProcess(input);

        // 3. Detect verb
        String verb = detectVerb(input);
        input = consume(input, verb);

        // 4. Detect object
        String object = detectObject(input);
        input = consume(input, object);
        String object_label = detectLabel(input);
        if (object_label != null) input = consume(input, "\""+object_label+"\"");

        // 5. Detect subject
        String subject = detectSubject(input);
        if (subject != null) input = consume(input, "\""+subject+"\"");

        // Create response
        Predicate pre = new Predicate(verb, object, object_label);
        Subject subj = new Subject(subject, null);
        
        return new Sentence(pre,subj);
    }

    protected static String detectSubject(String input) {
        // Check for marker first
        input = input.trim();
        StringTokenizer st = new StringTokenizer(input, " ");
        if (st.hasMoreTokens()) {
            String nextToken = st.nextToken();
            for (String subj_marker: SUBJECT_MARKER_LIST) {
                if (subj_marker.equals(nextToken)) {
                    input = consume(input, subj_marker);
                    String label = detectLabel(input);
                    return label;
                }
            }
        }

        return null;
    }
    
    protected static String detectLabel(String input) {
        //System.out.println("Detecting label "+input);

        // Check if next entry starts with "
        input = input.trim();
        if (input.startsWith("\"")) {
            input = input.substring(1, input.length());
            if (input.indexOf("\"")>=0) {
                String label = input.substring(0, input.indexOf("\""));
                return label;
            }
        }
        // No object label found.
        return null;
    }

    protected static String detectObject(String input) throws NaturalLanguageParseException {
        // Next entry must be an object
        input = input.trim();
        StringTokenizer st = new StringTokenizer(input, " ");
        if (st.hasMoreTokens()) {
            String nextToken = st.nextToken();
            for (String obj: OBJECT_LIST) {
                if (obj.equals(nextToken)) return obj;
            }
        }
        throw new NaturalLanguageParseException("No supported object found in '"+input+"'");
    }

    protected static String detectVerb(String input) throws NaturalLanguageParseException {
        // Next entry must be a verb
        input = input.trim();
        StringTokenizer st = new StringTokenizer(input, " ");
        if (st.hasMoreTokens()) {
            String nextToken = st.nextToken();
            for (String verb: VERB_LIST) {
                if (verb.equals(nextToken)) return verb;
            }
        }
        throw new NaturalLanguageParseException("No supported verb found in '"+input+"'");
    }

    /**
     * Consumes the substring inside the string, e.g. replaces it with ""
     * @param input
     * @param substring
     * @return
     */
    protected static String consume(String input, String substring) {
        //System.out.println("Consuming "+substring+" from "+input);
        if (input.indexOf(substring)<0) return input;
        return input.substring(input.indexOf(substring)+substring.length(), input.length()).trim();
    }

    /**
     * Preprocess (normalize, remove optional words etc.
     * @param input
     * @return
     */
    protected static String preProcess(String input) {

        //@todo: Implement something here!!!
        input = input.trim();

        return input;
    }

    public static void main(String args[]) throws Exception {
        System.out.println(parseNaturalLanguageString("add participant \"xyz\"").toString());
    }

}
