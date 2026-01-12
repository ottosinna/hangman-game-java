import java.util.Scanner;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.List;
import java.util.stream.Collectors;

public class HangMan {
    private String word;
    private ArrayList<Character> guessedLetters;
    private ArrayList<Character> wordState;
    private int wrongGuesses;
    private static int gamesPlayed = 0;
    private static int gamesWon = 0;

    public HangMan(String word) {
        this.word = word;
        this.guessedLetters = new ArrayList<>();
        this.wordState = new ArrayList<>();
        for(int i = 0; i < word.length(); i++) {
            wordState.add('_');
        }
        this.wrongGuesses = 0;
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        
        print("_____________________________");
        print("     Welcome to Hangman! ");
        print(" Mythical Creatures Edition ");
        print("_____________________________");

        boolean playAgain = true;
        
        while(playAgain) {
            // Load and filter words
            ArrayList<String> words = loadWords("words.txt");
            if(words.isEmpty()) return;

            // Select difficulty and word
            String difficulty = selectDifficulty(sc);
            String word = selectWord(words, difficulty);
            
            HangMan game = new HangMan(word);
            game.play(sc);
            
            // Update statistics
            gamesPlayed++;
            if(game.isWordComplete()) {
                gamesWon++;
            }
            
            // Show stats and ask to play again
            print("\nStats: " + gamesWon + "/" + gamesPlayed + " games won!");
            playAgain = askToPlayAgain(sc);
        }
        
        print("Thanks for playing!");
        sc.close();
    }

    public void play(Scanner sc) {
        print("\nNew Game Started!");
        print("Word length: " + word.length());
        
        while(wrongGuesses < 6 && !isWordComplete()) {
            displayGameState();
            
            char guess = getValidGuess(sc, guessedLetters);
            guessedLetters.add(guess);
            
            if(word.indexOf(guess) >= 0) {
                print("Correct!");
                updateWordState(guess);
            } else {
                print("Wrong guess!");
                wrongGuesses++;
                // Provide hint after 3 wrong guesses
                if(wrongGuesses == 3) {
                    print("Hint: The word is related to mythical creatures!");
                }
            }
        }

        // Display final result
        displayGameState();
        if(isWordComplete()) {
            print("Congratulations! You've guessed the word: " + word);
        } else {
            print("Game Over! The word was: " + word);
        }
    }

    private void displayGameState() {
        print(getHangmanArt(wrongGuesses));
        print("Word: " + getWordStateString());
        print("Guessed letters: " + guessedLetters);
        print("Wrong guesses: " + wrongGuesses + "/6");
        showRemainingLetters(guessedLetters);
    }

    // Static methods for game setup
    private static ArrayList<String> loadWords(String filePath) {
        ArrayList<String> words = new ArrayList<>();
        
        try(BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if(!trimmed.isEmpty()) {
                    words.add(trimmed);
                }
            }
        } catch(FileNotFoundException e) {
            System.err.println("File not found: " + e.getMessage());
            return words;
        } catch(IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            return words;
        }

        // Filter words that are too short/long
        words.removeIf(w -> w.length() < 3 || w.length() > 15);
        
        if(words.isEmpty()) {
            System.err.println("No suitable words found in the file!");
        }
        
        return words;
    }

    private static String selectDifficulty(Scanner sc) {
        print("\nSelect difficulty:");
        print("1. Easy (3-5 letters)");
        print("2. Medium (6-8 letters)");
        print("3. Hard (9+ letters)");
        print("Enter choice (1-3): ");
        
        while(true) {
            try {
                int choice = Integer.parseInt(sc.nextLine());
                switch(choice) {
                    case 1: return "easy";
                    case 2: return "medium";
                    case 3: return "hard";
                    default: print("Please enter 1, 2, or 3:");
                }
            } catch(NumberFormatException e) {
                print("Please enter a valid number:");
            }
        }
    }

    private static String selectWord(ArrayList<String> words, String difficulty) {
        Random random = new Random();
        List<String> filtered = words.stream()
            .filter(w -> {
                int len = w.length();
                return switch(difficulty) {
                    case "easy" -> len <= 5;
                    case "medium" -> len <= 8;
                    case "hard" -> len >= 9;
                    default -> true;
                };
            })
            .collect(Collectors.toList());
            
        if(filtered.isEmpty()) {
            // Fallback to any word if no words match the difficulty
            return words.get(random.nextInt(words.size())).toLowerCase();
        }
        
        return filtered.get(random.nextInt(filtered.size())).toLowerCase();
    }

    // Game logic methods
    private static char getValidGuess(Scanner sc, ArrayList<Character> guessedLetters) {
        while(true) {
            print("Enter a letter: ");
            String input = sc.nextLine().toLowerCase().trim();
            
            if(input.isEmpty()) {
                print("Please enter a letter!");
                continue;
            }
            
            if(input.length() > 1) {
                print("Please enter only one letter!");
                continue;
            }
            
            char guess = input.charAt(0);
            
            if(!Character.isLetter(guess)) {
                print("Please enter a valid letter!");
                continue;
            }
            
            if(guessedLetters.contains(guess)) {
                print("You already guessed that letter!");
                continue;
            }
            
            return guess;
        }
    }

    private void updateWordState(char guess) {
        for(int i = 0; i < word.length(); i++) {
            if(word.charAt(i) == guess) {
                wordState.set(i, guess);
            }
        }
    }

    private boolean isWordComplete() {
        return !wordState.contains('_');
    }

    private String getWordStateString() {
        StringBuilder sb = new StringBuilder();
        for(char c : wordState) {
            sb.append(c).append(" ");
        }
        return sb.toString();
    }

    private static void showRemainingLetters(ArrayList<Character> guessedLetters) {
        StringBuilder sb = new StringBuilder("Available letters: ");
        for(char c = 'a'; c <= 'z'; c++) {
            if(!guessedLetters.contains(c)) {
                sb.append(c).append(" ");
            }
        }
        print(sb.toString());
    }

    private static boolean askToPlayAgain(Scanner sc) {
        print("\nPlay again? (y/n): ");
        while(true) {
            String input = sc.nextLine().toLowerCase().trim();
            if(input.equals("y") || input.equals("yes")) {
                return true;
            } else if(input.equals("n") || input.equals("no")) {
                return false;
            } else {
                print("Please enter 'y' or 'n':");
            }
        }
    }

    // Hangman art remains the same
    public static String getHangmanArt(int wrongGuesses){
        return switch(wrongGuesses){
            case 0 -> """
                   +---+
                   |   |
                       |
                       |
                       |
                       |
                 =========""";
            case 1 -> """
                   +---+
                   |   |
                   O   |
                       |
                       |
                       |
                 =========""";
            case 2 -> """
                   +---+
                   |   |
                   O   |
                   |   |
                       |
                       |
                 =========""";
            case 3 -> """
                   +---+
                   |   |
                   O   |
                  /|   |
                       |
                       |
                 =========""";
            case 4 -> """
                   +---+
                   |   |
                   O   |
                  /|\\  |
                       |
                       |
                 =========""";
            case 5 -> """
                   +---+
                   |   |
                   O   |
                  /|\\  |
                  /    |
                       |
                 =========""";
            case 6 -> """
                   +---+
                   |   |
                   O   |
                  /|\\  |
                  / \\  |
                       |
                 =========""";
            default -> "";
        };
    }

    public static void print(String s){
        System.out.println(s);
    }
}