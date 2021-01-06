package autoscrabble;

import autoscrabble.word.Line;
import autoscrabble.word.LocatedWord;
import autoscrabble.word.RatedWord;

import javax.swing.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Board {
  public static final int SIZE = 15;
  public static final int RACK_CAPACITY = 7;
  public static final int[] TILE_DISTRIBUTION =
      new int[] {9, 2, 2, 4, 12, 2, 3, 2, 9, 1, 1, 4, 2, 6, 8, 2, 1, 6, 4, 6, 4, 2, 2, 1, 2, 1};
  public static final int MAX_BLANKS = 2;
  private final WordFinder wordFinder;
  private char[][] board = new char[SIZE][SIZE];
  private boolean[][] occupiedTiles = new boolean[board.length][board.length];
  private char[] rack = new char[RACK_CAPACITY];
  private String userMessage = "";
  private boolean gameStarted = false;

  public Board(WordFinder wordFinder) {
    initialise();
    this.wordFinder = wordFinder;
  }

  public Board(
      char[][] board,
      boolean[][] occupiedTiles,
      char[] rack,
      WordFinder wordFinder,
      boolean gameStarted) {
    this.board = board;
    this.occupiedTiles = occupiedTiles;
    this.rack = rack;
    this.wordFinder = wordFinder;
    this.gameStarted = gameStarted;
  }

  public static char pickTile(Random rng, ArrayList<Character> bag) {
    int randomIndex = rng.nextInt(bag.size());
    var character = bag.get(randomIndex);
    bag.remove(randomIndex);
    return character;
  }

  public void playAutomatically(JPanel gui, int seed) {
    var playedWords = new ArrayList<RatedWord>();
    var rng = new Random(seed);
    var bag = new ArrayList<Character>();
    for (int i = 0; i < 26; i++) {
      for (int j = 0; j < TILE_DISTRIBUTION[i]; j++) {
        bag.add((char) (65 + i));
      }
    }
    bag.add('_');
    bag.add('_');

    for (int i = 0; i < RACK_CAPACITY; i++) {
      getRack()[i] = pickTile(rng, bag);
    }

    while (true) {
      RatedWord word = findHighestScoringWord();
      if (word == null) {
        break;
      }
      setUserMessage(
          String.format(
              "Found %s at (%c%d) %s, scoring %s, ",
              word.string,
              ((char) (word.x + 97)),
              (15 - word.y),
              word.isHorizontal ? "horizontally" : "vertically",
              word.getRating()));
      System.out.println(word);
      makeMove(word);
      playedWords.add(word);
      for (int i = 0; i < getRack().length; i++) {
        if (getRack()[i] == ' ') {
          if (bag.size() > 0) {
            getRack()[i] = pickTile(rng, bag);
          }
        }
      }
      gui.repaint();
    }
    System.out.println("_____________________________");
    System.out.println("RESULTS");
    int totalScore = 0;
    for (var word : playedWords) {
      System.out.println(word);
      totalScore += word.getRating();
    }
    System.out.println("Total score: " + totalScore);
    gui.repaint();
  }

  public ArrayList<Character> computeBag() {
    var rackFrequency = WordFinder.getLetterFrequency(getRack());
    int rackBlanks = 0;
    for (var c : getRack()) {
      if (c == '_') {
        rackBlanks++;
      }
    }
    int[] boardFrequency = new int[DictEntry.ALPHABET.length];
    int boardBlanks = 0;
    for (var row : getBoard()) {
      for (var c : row) {
        if (Character.isAlphabetic(c) && Character.isUpperCase(c)) {
          boardFrequency[Character.getNumericValue(c) - 10]++;
        } else if (c == '_') {
          boardBlanks += 1;
        }
      }
    }
    var bag = new ArrayList<Character>();
    for (int i = 0; i < MAX_BLANKS - (boardBlanks + rackBlanks); i++) {
      bag.add('_');
    }
    for (int i = 0; i < boardFrequency.length; i++) {
      for (int j = 0; j < TILE_DISTRIBUTION[i] - (boardFrequency[i] + rackFrequency[i]); j++) {
        bag.add((char) (65 + i));
      }
    }
    return bag;
  }

  public Board getCopy() {
    var boardCopy = new char[Board.SIZE][Board.SIZE];
    var occupiedTilesCopy = new boolean[Board.SIZE][Board.SIZE];
    var rackCopy = rack.clone();
    for (int i = 0; i < Board.SIZE; i++) {
      for (int j = 0; j < Board.SIZE; j++) {
        boardCopy[i][j] = board[i][j];
        occupiedTilesCopy[i][j] = occupiedTiles[i][j];
      }
    }
    return new Board(boardCopy, occupiedTilesCopy, rackCopy, wordFinder, gameStarted);
  }

  /**
   * Get strings of the horizontal and vertical lines on the board
   *
   * @param board the board
   * @return a list of Strings
   */
  private List<String> getLines(char[][] board) {
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

  /** Make a move by placing a word on the board and removing tiles from the rack. */
  public void makeMove(LocatedWord word) {
    int blanksNeeded = word.blanksUsed;
    int[] lettersNeeded = word.placedLetterFrequency.clone();
    for (int i = 0; i < rack.length; i++) {
      int cValue = Character.getNumericValue(rack[i]) - 10;
      if (cValue >= 0) { // If a letter
        if (lettersNeeded[cValue] > 0) {
          rack[i] = ' ';
          lettersNeeded[cValue]--;
        }
      } else if (rack[i] == '_') {
        if (blanksNeeded > 0) {
          rack[i] = ' ';
          blanksNeeded--;
        }
      }
    }

    // Sanity check
    for (var n : lettersNeeded) {
      if (n != 0) {
        System.out.println("Mismatch: " + Arrays.toString(word.placedLetterFrequency) + word);
      }
    }
    if (blanksNeeded != 0) {
      System.out.println("Blank mismatch: " + blanksNeeded + word);
    }

    for (int i = 0; i < word.string.length(); i++) {
      int x, y;
      if (word.isHorizontal) {
        x = word.x + i;
        y = word.y;
      } else {
        x = word.x;
        y = word.y + i;
      }
      if (board[y][x] == ' ') {
        board[y][x] = word.string.charAt(i);
        occupiedTiles[y][x] = true;
      }
    }
  }

  private boolean moveIsValid(LocatedWord word) {
    var lines = new ArrayList<String>();
    if (word.isHorizontal) {
      char[] row = board[word.y].clone(); // Clone the row
      for (int i = 0; i < word.length; i++) {
        row[word.x + i] = word.string.charAt(i); // Place the tiles
      }
      lines.add(String.valueOf(row));

      // Create each column containing the new word
      for (int i = word.x; i < word.x + word.length; i++) {
        char[] col = new char[Board.SIZE];
        for (int j = 0; j < Board.SIZE; j++) { // Clone the column
          col[j] = board[j][i];
        }
        col[word.y] = word.string.charAt(i - word.x); // Place the tile
        lines.add(String.valueOf(col));
      }
    } else {
      char[] col = new char[Board.SIZE];
      for (int i = 0; i < Board.SIZE; i++) { // Clone the column
        col[i] = board[i][word.x];
      }
      for (int i = 0; i < word.length; i++) {
        col[word.y + i] = word.string.charAt(i); // Place the tiles
      }
      lines.add(String.valueOf(col));

      // Create each row containing the new word
      for (int i = word.y; i < word.y + word.length; i++) {
        char[] row = board[i].clone();
        row[word.x] = word.string.charAt(i - word.y); // Place the tile
        lines.add(String.valueOf(row));
      }
    }

    return linesAreValid(lines);
  }

  private boolean linesAreValid(List<String> lines) {
    return lines.stream()
        .map(arr -> arr.split(" +")) // Split each line around spaces
        .flatMap(Arrays::stream) // Merge the result
        .filter(w -> w.length() > 1) // Filter out blank or 1 letter words
        .allMatch(wordFinder::isWord); // Check against the dictionary
  }

  public boolean isGameStarted() {
    return gameStarted;
  }

  public void setGameStarted(boolean gameStarted) {
    this.gameStarted = gameStarted;
  }

  public RatedWord findHighestScoringWord() {
    var rater = new Rater(this);

    var bestWord =
        IntStream.range(0, board.length * 2) // Scan through horizontal then vertical lines
            .parallel()
            .mapToObj(
                i -> {
                  String line = getLines(board).get(i);
                  // Get a list of possible words
                  Direction direction =
                      i < board.length ? Direction.HORIZONTAL : Direction.VERTICAL;
                  int index = i < board.length ? i : i - board.length;
                  return wordFinder.getWords(this, new Line(line, direction, index), rack);
                })
            .filter(Objects::nonNull) // Remove any lines that were skipped
            .flatMap(List::stream) // Merge lists from each line
            .filter(this::moveIsValid)
            .map(word -> word.getRatedWord(rater))
            .max(Comparator.comparingDouble(RatedWord::getRating))
            .orElse(null);
    gameStarted = true;
    return bestWord;
  }

  private RatedWord findBestWord() {
    System.out.print("Starting rack: ");
    System.out.println(rack);
    long startTime = System.currentTimeMillis();
    // If the board is invalid display an error
    if (!linesAreValid(getLines(board))) {
      userMessage = "Current board not valid";
      return null;
    }
    var rater = new Rater(this);
    // Find all possible words that fit in the line
    var words =
        IntStream.range(0, board.length * 2) // Scan through horizontal then vertical lines
            .parallel()
            .mapToObj(
                i -> {
                  String line = getLines(board).get(i);
                  // If this is the first move then the line must go through the centre
                  // If not also skip if the line is blank
                  if (!gameStarted && i != SIZE / 2 && i != SIZE + SIZE / 2
                      || gameStarted && line.isBlank()) {
                    return null;
                  }
                  // Get a list of possible words
                  Direction direction =
                      i < board.length ? Direction.HORIZONTAL : Direction.VERTICAL;
                  int index = i < board.length ? i : i - board.length;
                  return wordFinder.getWords(this, new Line(line, direction, index), rack);
                })
            .filter(Objects::nonNull) // Remove any lines that were skipped
            .flatMap(List::stream) // Merge lists from each line
            .filter(this::moveIsValid)
            .map(word -> word.getRatedWord(rater)) // Rate the words
            .sorted(Collections.reverseOrder(Comparator.comparingDouble(RatedWord::getRating)))
            .collect(Collectors.toList());

    if (words.size() == 0) {
      // Give up immediately with no words found
      userMessage = "No words found";
      return null;
    }
    for (var w : words) {
      if (w.getRating() > Math.max(10, words.get(0).getRating() - 30)) {
        System.out.println(w);
      }
    }
    System.out.println("Highest score: " + words.get(0));

    // Limit the list for smart analysis to 10 words with blanks
    int smartListMax = 20;
    int wordsWithBlankMax = 10;
    var smartIndexList = new ArrayList<Integer>();
    int added = 0;
    int wordsWithBlankAdded = 0;
    for (int i = 0; i < words.size() && added < smartListMax; i++) {
      if (words.get(i).blanksUsed == 0) {
        added++;
        smartIndexList.add(i);
      } else if (wordsWithBlankAdded < wordsWithBlankMax) {
        added++;
        wordsWithBlankAdded++;
        smartIndexList.add(i);
      }
    }

    var smartWords =
        smartIndexList.stream()
            .parallel()
            .map(i -> new RatedWord(words.get(i), rater))
            .collect(Collectors.toList());

    for (var w : smartWords) {
      System.out.printf("%s. Smart: %.1f%n", w, w.getSmartRating());
    }

    var bestWord =
        smartWords.stream().max(Comparator.comparingDouble(RatedWord::getSmartRating)).orElse(null);
    assert bestWord != null;
    userMessage =
        String.format(
            "Found %s at (%c%d) %s, scoring %s in %d ms.",
            bestWord.string,
            ((char) (bestWord.x + 97)),
            (15 - bestWord.y),
            bestWord.isHorizontal ? "horizontally" : "vertically",
            bestWord.getRating(),
            (System.currentTimeMillis() - startTime));
    System.out.printf("%s. Smart rating: %.1f%n", userMessage, bestWord.getSmartRating());
    gameStarted = true;
    return bestWord;
  }

  /**
   * Place a tile with the given letter at the given location. If the letter is a space, remove the
   * tile.
   */
  public void placeTile(char letter, int xSelection, int ySelection) {
    occupiedTiles[ySelection][xSelection] = letter != ' '; // If placing a space that removes a tile
    board[ySelection][xSelection] = letter;
  }

  /** Place a tile in the rack at the given location. */
  public void placeInRack(char letter, int handSelection) {
    rack[handSelection] = Character.toUpperCase(letter);
  }

  public char[] getRack() {
    return rack;
  }

  public void setRack(char[] rack) {
    this.rack = rack;
  }

  public boolean[][] getOccupiedTiles() {
    return occupiedTiles;
  }

  public char[][] getBoard() {
    return board;
  }

  public String getUserMessage() {
    return userMessage;
  }

  public void setUserMessage(String userMessage) {
    this.userMessage = userMessage;
  }

  public void initialise() {
    for (var row : board) {
      Arrays.fill(row, ' ');
    }
    for (var row : occupiedTiles) {
      Arrays.fill(row, false);
    }
    Arrays.fill(rack, ' ');
    gameStarted = false;
  }

  public RatedWord runFinder() {
    for (var row : board) {
      for (var c : row) {
        if (c != ' ') {
          gameStarted = true;
          break;
        }
      }
    }

    boolean gameStartedBefore = gameStarted;
    var word = findBestWord();

    // Calculate the bag from the currently known tiles
    var bag = computeBag();
    for (var c : rack) {
      if (Character.isAlphabetic(c)) {
        bag.add(c);
      }
    }
    if (word == null || bag.size() > 14) {
      var rng = new Random(Arrays.deepHashCode(board));

      var bagCopy = new ArrayList<>(bag);

      double avgRating = 1; // Start at 1 to avoid potential div/0 later
      int simCount = 100;
      boolean foundWord = false;
      for (int i = 0; i < simCount; i++) {
        var newRack = new char[RACK_CAPACITY];
        Collections.shuffle(bagCopy, rng);
        for (int j = 0; j < RACK_CAPACITY && j < bagCopy.size(); j++) {
          newRack[j] = bagCopy.get(j);
        }
        var boardCopy = this.getCopy();
        boardCopy.setGameStarted(gameStartedBefore);
        boardCopy.setRack(newRack);
        var newWord = boardCopy.findHighestScoringWord();
        if (newWord != null) {
          foundWord = true;
          avgRating += newWord.getRating();
        }
      }
      if (foundWord) {
        avgRating /= simCount; // Average
        if (word == null) {
          userMessage = "No words found, try tile exchange";
        } else {
          userMessage = userMessage + " Average score: " + String.format("%.1f", avgRating);
        }
      }
    }
    return word;
  }
}
