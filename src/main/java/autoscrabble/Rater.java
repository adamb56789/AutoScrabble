package autoscrabble;

import autoscrabble.word.Letter;
import autoscrabble.word.LocatedWord;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;

// word format: letter, y coordinate, x coordinate, tile bonuses allowed (y/n), is blank (y/n)
public class Rater {
  public static final char[][] BONUSES = {
    {'W', ' ', ' ', 'l', ' ', ' ', ' ', 'W', ' ', ' ', ' ', 'l', ' ', ' ', 'W'},
    {' ', 'w', ' ', ' ', ' ', 'L', ' ', ' ', ' ', 'L', ' ', ' ', ' ', 'w', ' '},
    {' ', ' ', 'w', ' ', ' ', ' ', 'l', ' ', 'l', ' ', ' ', ' ', 'w', ' ', ' '},
    {'l', ' ', ' ', 'w', ' ', ' ', ' ', 'l', ' ', ' ', ' ', 'w', ' ', ' ', 'l'},
    {' ', ' ', ' ', ' ', 'w', ' ', ' ', ' ', ' ', ' ', 'w', ' ', ' ', ' ', ' '},
    {' ', 'L', ' ', ' ', ' ', 'L', ' ', ' ', ' ', 'L', ' ', ' ', ' ', 'L', ' '},
    {' ', ' ', 'l', ' ', ' ', ' ', 'l', ' ', 'l', ' ', ' ', ' ', 'l', ' ', ' '},
    {'W', ' ', ' ', 'l', ' ', ' ', ' ', 'w', ' ', ' ', ' ', 'l', ' ', ' ', 'W'},
    {' ', ' ', 'l', ' ', ' ', ' ', 'l', ' ', 'l', ' ', ' ', ' ', 'l', ' ', ' '},
    {' ', 'L', ' ', ' ', ' ', 'L', ' ', ' ', ' ', 'L', ' ', ' ', ' ', 'L', ' '},
    {' ', ' ', ' ', ' ', 'w', ' ', ' ', ' ', ' ', ' ', 'w', ' ', ' ', ' ', ' '},
    {'l', ' ', ' ', 'w', ' ', ' ', ' ', 'l', ' ', ' ', ' ', 'w', ' ', ' ', 'l'},
    {' ', ' ', 'w', ' ', ' ', ' ', 'l', ' ', 'l', ' ', ' ', ' ', 'w', ' ', ' '},
    {' ', 'w', ' ', ' ', ' ', 'L', ' ', ' ', ' ', 'L', ' ', ' ', ' ', 'w', ' '},
    {'W', ' ', ' ', 'l', ' ', ' ', ' ', 'W', ' ', ' ', ' ', 'l', ' ', ' ', 'W'}
  };
  public static final int[][] LETTER_BONUSES = computeLetterBonuses();
  public static final int[][] WORD_BONUSES = computeWordBonuses();
  private final Board board;

  public Rater(Board board) {
    this.board = board;
  }

  private static int[][] computeLetterBonuses() {
    int[][] arr = new int[BONUSES.length][BONUSES.length];
    for (int i = 0; i < BONUSES.length; i++) {
      for (int j = 0; j < BONUSES.length; j++) {
        switch (BONUSES[i][j]) {
          case 'l':
            arr[i][j] = 2;
            break;
          case 'L':
            arr[i][j] = 3;
            break;
          default:
            arr[i][j] = 1;
            break;
        }
      }
    }
    return arr;
  }

  private static int[][] computeWordBonuses() {
    int[][] arr = new int[BONUSES.length][BONUSES.length];
    for (int i = 0; i < BONUSES.length; i++) {
      for (int j = 0; j < BONUSES.length; j++) {
        switch (BONUSES[i][j]) {
          case 'w':
            arr[i][j] = 2;
            break;
          case 'W':
            arr[i][j] = 3;
            break;
          default:
            arr[i][j] = 1;
        }
      }
    }
    return arr;
  }

  private int rateWord(List<Letter> word) {
    // Add up the scores of the letters
    int total = word.stream().mapToInt(this::rateLetter).sum();

    // Apply any potential word multipliers
    total *= wordMultiplier(word);

    // Add 50 if 7 letters are used
    if (word.stream().filter(Letter::justPlaced).count() >= 7) {
      total += 50;
    }
    return total;
  }

  private int rateLetter(Letter letter) {
    int letterScore = letter.letterScore();
    if (Character.isLowerCase(letter.getChar())) {
      return 0;
    }
    if (!letter.justPlaced()) {
      return letterScore;
    }
    return letterScore * LETTER_BONUSES[letter.getY()][letter.getX()];
  }

  private int wordMultiplier(List<Letter> word) {
    int multiplier = 1;
    for (var letter : word) {
      if (letter.justPlaced()) {
        multiplier *= WORD_BONUSES[letter.getY()][letter.getX()];
      }
    }
    return multiplier;
  }

  public int rate(LocatedWord word) {
    boolean[][] occupiedTiles = board.getOccupiedTiles();
    char[][] board = this.board.getBoard();
    // Compute and filter all lines that need to be evaluated
    var lines = new ArrayList<LineData>();
    if (word.isHorizontal) {
      char[] row = board[word.y].clone(); // Clone the row
      for (int i = 0; i < word.length; i++) {
        row[word.x + i] = word.string.charAt(i); // Place the tiles
      }
      lines.add(new LineData(row, occupiedTiles[word.y], true, word.x));

      // Create each column containing the new word
      for (int i = word.x; i < word.x + word.length; i++) {
        if (board[word.y][i] != ' ') {
          // If there is already a tile here, do not rate this column
          continue;
        }
        char[] col = new char[Board.SIZE];
        boolean[] alreadyRatedCol = new boolean[Board.SIZE];
        for (int j = 0; j < Board.SIZE; j++) { // Clone the column
          col[j] = board[j][i];
          alreadyRatedCol[j] = occupiedTiles[j][i];
        }
        if ((word.y <= 0 || col[word.y - 1] == ' ')
            && (word.y + 1 >= col.length || col[word.y + 1] == ' ')) {
          // If this letter is connected to anything then skip since there is no word to rate
          continue;
        }
        col[word.y] = word.string.charAt(i - word.x); // Place the tile
        lines.add(new LineData(col, alreadyRatedCol, false, i));
      }
    } else {
      char[] col = new char[Board.SIZE];
      boolean[] alreadyRatedCol = new boolean[Board.SIZE];
      for (int i = 0; i < Board.SIZE; i++) { // Clone the column
        col[i] = board[i][word.x];
        alreadyRatedCol[i] = occupiedTiles[i][word.x];
      }
      for (int i = 0; i < word.length; i++) {
        col[word.y + i] = word.string.charAt(i); // Place the tiles
      }
      lines.add(new LineData(col, alreadyRatedCol, true, word.y));

      // Create each row containing the new word
      for (int i = word.y; i < word.y + word.length; i++) {
        if (board[i][word.x] != ' ') {
          // If there is already a tile here, do not rate this column
          continue;
        }
        char[] row = board[i].clone();
        if ((word.x <= 0 || row[word.x - 1] == ' ')
            && (word.x + 1 >= row.length || row[word.x + 1] == ' ')) {
          // If this letter is connected to anything then skip since there is no word to rate
          continue;
        }
        row[word.x] = word.string.charAt(i - word.y); // Place the tile
        lines.add(new LineData(row, occupiedTiles[i], false, i));
      }
    }

    int rating = 0;
    for (LineData line : lines) { // Main loop to go through and add ratings
      // Find an index which is inside the word we need to rate by scanning through the line,
      // and choosing the first unrated letter
      OptionalInt found = OptionalInt.empty();
      for (int j = 0; j < Board.SIZE; j++) {
        if (!line.alreadyRated[j] && line.line[j] != ' ') {
          found = OptionalInt.of(j);
          break;
        }
      }
      int indexInWord = found.orElseThrow();

      // Move backwards to find the index of start of the word (inclusive)
      int k = indexInWord - 1;
      while (k >= 0 && k < Board.SIZE && line.line[k] != ' ') {
        k--;
      }
      k++;

      // Move forwards to find the index of end of the word (exclusive)
      int l = indexInWord + 1;
      while (l >= 0 && l < Board.SIZE && line.line[l] != ' ') {
        l++;
      }

      // Generate the letters along the correct axis
      var letters = new ArrayList<Letter>();
      for (int m = k; m < l; m++) {
        if (word.isHorizontal) {
          if (line.containsEntireWord) {
            letters.add(new Letter(line.line[m], m, word.y, !line.alreadyRated[m]));
          } else {
            letters.add(new Letter(line.line[m], line.letterIndex, m, !line.alreadyRated[m]));
          }
        } else {
          if (line.containsEntireWord) {
            letters.add(new Letter(line.line[m], word.x, m, !line.alreadyRated[m]));
          } else {
            letters.add(new Letter(line.line[m], m, line.letterIndex, !line.alreadyRated[m]));
          }
        }
      }
      rating += rateWord(letters);
    }

    return rating;
  }
}
