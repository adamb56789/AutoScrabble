package autoscrabble;

import autoscrabble.word.LocatedWord;
import autoscrabble.word.RatedWord;
import autoscrabble.word.Word1D;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Board {
  public static final int SIZE = 15;
  public static final int RACK_CAPACITY = 7;
  private final char[] rack = "ABCDEFG".toCharArray();
  private final WordFinder wordFinder;
  private final char[][] board = {
    {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
    {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
    {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
    {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
    {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
    {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
    {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
    {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
    {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
    {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
    {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
    {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
    {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
    {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
    {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '}
  };
  private final boolean[][] occupiedTiles = new boolean[board.length][board.length];
  private boolean userInterrupt;
  private String userMessage = "";

  public Board() {
    var inputStream = getClass().getClassLoader().getResourceAsStream("Dictionary.txt");
    assert inputStream != null;
    var streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
    var reader = new BufferedReader(streamReader);
    var dictionary = new String[] {};
    try {
      dictionary = reader.lines().toArray(String[]::new);
      for (String line; (line = reader.readLine()) != null; ) {
        System.out.println(line);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    wordFinder = new WordFinder(dictionary);
    System.out.println("There are " + dictionary.length + " words in the dictionary.");
  }

  /**
   * Get strings of the horizontal and vertical lines on the board
   *
   * @param board the board
   * @return a list of Strings
   */
  public static List<String> getLines(char[][] board) {
    // Rows
    var lines = Arrays.stream(board).map(String::valueOf).collect(Collectors.toList());

    // Columns
    for (int i = 0; i < board.length; i++) {
      char[] col = new char[board.length];
      for (int j = 0; j < board.length; j++) {
        col[j] = board[j][i];
      }
      lines.add(String.valueOf(col));
    }
    return lines;
  }

  /** Place the given word on the given board. */
  public static void placeWord(LocatedWord word, char[][] board) {
    for (int i = 0; i < word.getWord().length(); i++) {
      if (word.isHorizontal()) {
        board[word.getY()][word.getX() + i] = word.getWord().charAt(i);
      } else {
        board[word.getY() + i][word.getX()] = word.getWord().charAt(i);
      }
    }
  }

  public char[] getRack() {
    return rack;
  }

  public boolean[][] getOccupiedTiles() {
    return occupiedTiles;
  }

  public char[][] getBoard() {
    return board;
  }

  public char[][] getBoardCopy() {
    return Arrays.stream(board).map(char[]::clone).toArray(char[][]::new);
  }

  public String getUserMessage() {
    return userMessage;
  }

  public boolean boardIsValid(LocatedWord move) {
    char[][] boardCopy = getBoardCopy();

    // Place the word if there is one
    if (move != null) {
      placeWord(move, boardCopy);
    }

    return getLines(boardCopy) // Get all horizontal and vertical lines
        .stream()
        .map(arr -> arr.split(" +")) // Split each line around spaces
        .flatMap(Arrays::stream) // Merge the result
        .filter(w -> w.length() > 1) // Filter out blank or 1 letter words
        .allMatch(wordFinder::isWord); // Check against the dictionary
  }

  /**
   * Generate all possible words that could be placed on the given line. The index is the row or
   * column index of the line.
   */
  private List<RatedWord> generateWordsOnLine(
      char[] rack, String line, int index, Direction direction) {
    // If line is blank then skip
    if (line.isBlank()) {
      return null;
    }

    // Get a list of possible words
    List<Word1D> words = wordFinder.getWords(line, rack);

    // TODO if blank tiles were used, replace them

    // Rate all of the words
    var ratedWords = new ArrayList<RatedWord>();
    var rater = new Rater(this);
    for (var word : words) {
      LocatedWord unratedWord;
      if (direction == Direction.HORIZONTAL) {
        unratedWord = new LocatedWord(word.getWord(), word.getStartIndex(), index, direction);
      } else { // Vertical
        unratedWord = new LocatedWord(word.getWord(), index, word.getStartIndex(), direction);
      }
      ratedWords.add((unratedWord.getRatedWord(rater)));
    }
    return ratedWords;
  }

  public RatedWord findBestWord() {
    long startTime = System.currentTimeMillis();

    // If the board is invalid display an error
    if (!boardIsValid(null)) {
      userMessage = "Current board not valid";
      return null;
    }

    // Find all possible words
    var lines = getLines(board);
    // Merge lists
    var words =
        IntStream.range(0, board.length * 2) // Scan through horizontal then vertical lines
            //            .parallel() // I am speed
            .mapToObj(
                i -> {
                  // Get the row index if horizontal or col index if vertical, and the direction
                  int index = i < board.length ? i : i - board.length;
                  var direction = i < board.length ? Direction.HORIZONTAL : Direction.VERTICAL;
                  return generateWordsOnLine(rack, lines.get(i), index, direction);
                })
            .filter(Objects::nonNull) // Remove any lines that were skipped
            .flatMap(List::stream) // Merge lists from each line
            // Sort the words descending by score
            .sorted(Collections.reverseOrder(Comparator.comparingDouble(RatedWord::getRating)))
            .collect(Collectors.toList());

    // Get the first one that is valid
    RatedWord bestWord = null;
    for (var word : words) {
      if (boardIsValid(word)) {
        bestWord = word;
        userMessage =
            String.format(
                "Found %s at (%c%d) %s, scoring at least %s, ",
                word.getWord(),
                ((char) (word.getX() + 97)),
                (15 - word.getY()),
                word.isHorizontal() ? "horizontally" : "vertically",
                word.getRating());
        break;
      }
      if (userInterrupt) {
        userMessage = "Cancelled ";
        break;
      }
    }
    if (bestWord == null) {
      userMessage = "No words found";
    } else {
      long finishTime = System.currentTimeMillis();
      userMessage +=
          String.format("%s %d ms", userInterrupt ? "after" : "in", (finishTime - startTime));
      userInterrupt = false;
    }
    System.out.println(userMessage);
    return bestWord;
  }

  public void userInterrupt() {
    userInterrupt = true;
  }

  /**
   * Place a tile with the given letter at the given location. If the letter is a space, remove the
   * tile.
   */
  public void placeTile(char letter, int xSelection, int ySelection) {
    occupiedTiles[ySelection][xSelection] = letter != ' '; // If placing a space that removes a tile
    board[ySelection][xSelection] = Character.toUpperCase(letter);
  }

  /** Place a tile in the rack at the given location. */
  public void placeInRack(char letter, int handSelection) {
    rack[handSelection] = Character.toUpperCase(letter);
  }
}
