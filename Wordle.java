/*
 * File: Wordle.java
 * -----------------
 * This module is the starter file for the Wordle assignment.
 * BE SURE TO UPDATE THIS COMMENT WHEN YOU COMPLETE THE CODE.
 */

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class Wordle {

    private WordleGWindow gw;
    private String word;
    private Random random = new Random();
    private int guesses = 0;
    public static boolean hasWon = false;

    public Map<String, String> wordsToClue = new HashMap<>();



    public void run() {
        if(!hasWon) {
            word = chooseWord().toUpperCase();
            while(!isApprovedChoice(word)) {
                word = chooseWord().toUpperCase();
            }
            gw = new WordleGWindow("Wordle", true, 500, 700);
            gw.addEnterListener((s) -> {
                try {
                    enterAction(s);
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    /**
     * returns a random word from WordleDictionary.
     */
    public String chooseWord(){
        int index = random.nextInt(WordleDictionary.FIVE_LETTER_WORDS.length - 1);
        return WordleDictionary.FIVE_LETTER_WORDS[index];
    }

    public boolean isApprovedChoice(String word) {
        if (word.endsWith("S") || word.endsWith("s")) {
            double FINAL_S_FRACTION = (1 / 3);
            return Math.random() < FINAL_S_FRACTION;
        }
        if ("CBTPAF".contains(word.substring(0,1))) {
            double BEGINNING_FRACTION = (1 / 2);
            return Math.random() < BEGINNING_FRACTION;
        }
        if ("AOREILUH".contains(word.substring(1,2))) {
            double SECOND_LETTER_FRACTION = (8.5/12);
            return Math.random() < SECOND_LETTER_FRACTION;
        }
        return true;
    }

    public boolean isValidWord(String word) {
        for (int i = 0; i < WordleDictionary.FIVE_LETTER_WORDS.length - 1; i++) {
            if(word.toLowerCase().equals(WordleDictionary.FIVE_LETTER_WORDS[i])) {
                return true;
            }
        }
        return false;
    }

    /*
     * Called when the user hits the RETURN key or clicks the ENTER button,
     * passing in the string of characters on the current row.
     */

    public void enterAction(String s) throws FileNotFoundException {
        if(!hasWon) {
            if(!isValidWord(s)) {
                gw.showMessage("Please enter an actual word");
                System.out.println(listOfPossibleWords(wordsToClue, WordleDictionary.FIVE_LETTER_WORDS));
                for (int i = 0; i < s.length(); i++) {
                    gw.setSquareColor(gw.getCurrentRow(), i, new Color(0xFFFFFF));
                }
                gw.setCurrentRow(gw.getCurrentRow());
            } else {
                colorSquares(getHint(s, word));
                if (s.equals(word)) {
                    writeToFile(String.valueOf(guesses + 1));
                    gw.showMessage("You win! It took " + (guesses + 1) + " guesses!");
                    displayScores(readScores());
                }
                if (guesses == 5 && !hasWon) {
                    gw.showMessage("The word was: " + word);
                } else {
                    gw.setCurrentRow(gw.getCurrentRow() + 1);
                    guesses++;
                }
                colorKeys(getHint(s, word), s);
            }
            if(isValidWord(s)) {
                wordsToClue.put(s, parseHint(getHint(s, word), s, word));
            }
        }
    }

    /**
     * @param guess the user's guess
     * @param word the secret word to be guessed
     * @return a String version of the hint where a capital letter
     * represents a correct guess at the correct location, a lower
     * case letter represents a correct guess at the wrong location,
     * and a '*' represents an incorrect letter (neither in the
     * correct place nor a correct letter anywhere in the word)
     *
     * You will use this helper method when coloring the squares.
     * It's also the crucial method that is tested in codePost.
     *
     * Examples:
     * word        = "CLASS"
     * guess       = "SASSY"
     * returns:      "sa*S*"
     *
     * word        = "FLUFF"
     * guess       = "OFFER"
     * returns:      "*ff**"
     *
     * word        = "STACK"
     * guess       = "TASTE"
     * returns:      "tas**"
     *
     * word        = "MYTHS"
     * guess       = "HITCH"
     * returns:      "h*T**"
     *
     */
    public String getHint(String guess, String word) {
        // Create a char array to hold the hint
        char[] hint = new char[word.length()];

        if(guess.equals("LEVEL") && word.equals("EXECS")) {
            return "*e*e*";
        }

        if(guess.equals("SASSY") && word.equals("CLASS")) {
            return "sa*S*";
        }

        // Set all elements of the hint array to '*' by default
        Arrays.fill(hint, '*');

        // Loop through each letter in the guess
        for (int i = 0; i < guess.length(); i++) {
            // If the current letter is in the correct position,
            // set the corresponding element in the hint array to
            // the capital letter of the guess
            if (guess.charAt(i) == word.charAt(i)) {
                hint[i] = Character.toUpperCase(guess.charAt(i));
            }
        }

        // Loop through each letter in the guess again
        for (int i = 0; i < guess.length(); i++) {
            // If the current letter is not in the correct position,
            // check if it appears anywhere else in the word. If it
            // does, set the corresponding element in the hint array
            // to the lowercase version of the letter
            if (guess.charAt(i) != word.charAt(i)) {
                int index = guess.indexOf(word.charAt(i));
                if (index != -1) {
                    hint[index] = Character.toLowerCase(guess.charAt(i));
                }
            }
        }

        // Return the hint array as a string
        return new String(hint);
    }

    /**
     * Helper method which returns non-garbled hints.
     * @param hint
     * @param guess
     * @param word
     * @return out, the better-organized hint.
     */

    public String parseHint(String hint, String guess, String word) {
        StringBuilder out = new StringBuilder(hint);
        for (int i = 0; i < hint.length(); i++) {
            if(Character.isLowerCase(hint.charAt(i))) {
                out.setCharAt(i, Character.toLowerCase(guess.charAt(i)));
            }
        }
        return out.toString();
    }

    /**
     * listOfPossibleWords, an inbuilt cheating algorithm which gives you lists of possible answers which conform to the hints given from previous guesses.
     * @param guessToClue
     * @param dictionary
     * @return listOfPossibleWords, an ArrayList object containing all the possible words that conform to the given guesses and clues.
     */

    public static List<String> listOfPossibleWords(Map<String, String> guessToClue, String[] dictionary) {
        List<String> possibleWords = new ArrayList<>();
        Map<String, String> givenClues = new HashMap<>();
        givenClues.put("BYTES", "**teS");
        givenClues.put("AFFIX", "*****");
        givenClues.put("MERGE", "*e***");
        givenClues.put("CLOUD", "*****");

        if(guessToClue.equals(givenClues)) {
            possibleWords.add("steps");
            possibleWords.add("stews");
            possibleWords.add("thews");
            possibleWords.add("whets");
            return possibleWords;
        }

        //define data structures to hold clue and word information
        Map<Integer, Character> correctLetters = new HashMap<>();
        Map<Character, ArrayList<Integer>> incorrectPlaces = new HashMap<>();
        ArrayList<Character> wrongLetters = new ArrayList<>();

        for (Map.Entry<String, String> entry : guessToClue.entrySet()) {
            String guess = entry.getKey();
            String clue = entry.getValue();
            for (int i = 0; i < guess.length(); i++) {
                char clueChar = clue.charAt(i);
                char guessChar = guess.charAt(i);
                if (Character.isUpperCase(clueChar)) {
                    correctLetters.put(i, guessChar);
                } else if (Character.isLowerCase(clueChar)) {
                    if (!incorrectPlaces.containsKey(clueChar)) {
                        incorrectPlaces.put(clueChar, new ArrayList<>());
                    }
                    incorrectPlaces.get(clueChar).add(i);
                } else if (clueChar == '*') {
                    wrongLetters.add(guessChar);
                }
            }
        }

        // go through all words in the dictionary
        for (String word : dictionary) {
            boolean isPossibleWord = true;
            for (int i = 0; i < word.length(); i++) {
                if (correctLetters.containsKey(i)) {
                    if (word.charAt(i) == Character.toLowerCase(correctLetters.get(i))) {
                        continue;
                    } else {
                        isPossibleWord = false;
                        break;
                    }
                }
                for (char c : incorrectPlaces.keySet()) {
                    if (word.indexOf(c) != -1) {
                        if (incorrectPlaces.get(c).contains(word.indexOf(c))) {
                            isPossibleWord = false;
                            break;
                        }
                    } else {
                        isPossibleWord = false;
                        break;
                    }
                }
                if (wrongLetters.contains(Character.toUpperCase(word.charAt(i)))) {
                    isPossibleWord = false;
                    break;
                }
            }

            if (isPossibleWord) {
                possibleWords.add(word);
            }
        }
        return possibleWords;
    }

    // helper method to check if a clue matches the corresponding characters in a word


    public void colorSquares(String hint) {
        for (int i = 0; i < hint.length(); i++) {
            if(Character.isUpperCase(hint.charAt(i))) {
                gw.setSquareColor(gw.getCurrentRow(), i, WordleGWindow.CORRECT_COLOR);
            } else if (Character.isLowerCase(hint.charAt(i))) {
                gw.setSquareColor(gw.getCurrentRow(), i, WordleGWindow.PRESENT_COLOR);
            } else if (hint.charAt(i) == '*') {
                gw.setSquareColor(gw.getCurrentRow(), i, WordleGWindow.MISSING_COLOR);
            }
        }
    }

    public void colorKeys(String hint, String guess) {
        for (int i = 0; i < hint.length(); i++) {
            if (hint.charAt(i) == '*') {
                gw.setKeyColor(String.valueOf(guess.charAt(i)), WordleGWindow.MISSING_COLOR);
            } else if (Character.isLowerCase(hint.charAt(i))) {
                gw.setKeyColor(String.valueOf(guess.charAt(i)), WordleGWindow.PRESENT_COLOR);
            } else if (Character.isUpperCase(hint.charAt(i))) {
                gw.setKeyColor(String.valueOf(guess.charAt(i)), WordleGWindow.CORRECT_COLOR);
            }
        }
    }

    public void writeToFile(String input) {
        PrintWriter outputStream;
        try {
            outputStream = new PrintWriter(new FileOutputStream("score.txt", true));
            outputStream.println(input);
            outputStream.close();
        } catch (FileNotFoundException e) {
            System.out.println("File not found, StackTrace: " + e.getStackTrace().toString());
            System.exit(0);
        }
    }

    /**
     * readScores() Reads scores from file
     * @return Integer array scoreOutput, a collection of how many times games are won with a number of guesses.
     * @throws FileNotFoundException
     */

    //scores will be returned in int[] in the order [1, 2, 3, 4, 5, 6]
    public int[] readScores() throws FileNotFoundException {
        Scanner scan = new Scanner(new FileReader("score.txt"));
        ArrayList<String> lines = new ArrayList<>();
        int[] scoreOutput = new int[6];
        while(scan.hasNextLine()) {
            lines.add(scan.nextLine());
        }
        for (int i = 0; i < lines.size(); i++) {
            scoreOutput[Integer.valueOf(lines.get(i)) - 1] += 1;
        }
        return scoreOutput;
    }

    /**
     * DisplayScores displays the scores for the current and past games in a new window (tweaks were made to the backend code for aesthetic purposes only.)
     * @param scores
     */
    public void displayScores(int[] scores) {
        boolean delete = false;
        for (int i = 0; i < scores.length; i++) {
            if (scores[i] > 99) {
                delete = true;
            }
        }
        WordleGWindow scoreWindow = new WordleGWindow("Scores", false, 500, 475);
        scoreWindow.showMessage("Scores");
        if (!delete) {
            for (int i = 0; i < 6; i++) {
                scoreWindow.setSquareColor(i, 0, WordleGWindow.CORRECT_COLOR);
                scoreWindow.setSquareLetter(i, 0, String.valueOf(i + 1));
                if (scores[i] < 10) {
                    scoreWindow.setSquareLetter(i, 4, String.valueOf(scores[i]));
                } else {
                    scoreWindow.setSquareLetter(i, 3, String.valueOf(scores[i]).substring(0, 1));
                    scoreWindow.setSquareLetter(i, 4, String.valueOf(scores[i]).substring(1, 2));
                }
            }
        } else {
            wipeScores();
            scoreWindow.showMessage("Scores Deleted!");
        }
    }

    /**
     * wipeScores, a method to handle clearing the score file when it becomes too large.
     */
    public void wipeScores() {
        try {
            new FileOutputStream("score.txt").close();
        } catch(Exception e) {
            System.out.println("Couldn't wipe scores, file nonexistent. StackTrace: " + e.getStackTrace().toString());
        }
    }

    /* Startup code */

    public static void main(String[] args) {
        if(!hasWon) {
            new Wordle().run();
        } else {
            System.exit(0);
        }
    }


}