package autoscrabble;

import autoscrabble.word.Letter;
import autoscrabble.word.LocatedWord;

import java.util.ArrayList;
import java.util.Arrays;
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
  private final Board board;

  public Rater(Board board) {
    this.board = board;
  }

  private double rateWord(List<Letter> word) {
    // Add up the scores of the letters
    double total = word.stream().mapToDouble(this::rateLetter).sum();

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
    switch (BONUSES[letter.getY()][letter.getX()]) {
      case 'l':
        return letterScore * 2;
      case 'L':
        return letterScore * 3;
      default:
        return letterScore;
    }
  }

  private int wordMultiplier(List<Letter> word) {
    int multiplier = 1;
    for (var letter : word) {
      if (letter.justPlaced()) {
        switch (BONUSES[letter.getY()][letter.getX()]) {
          case 'w':
            multiplier *= 2;
            break;
          case 'W':
            multiplier *= 3;
            break;
        }
      }
    }
    return multiplier;
  }

  public int rate(LocatedWord word) {
    char[][] boardCopy = board.getBoardCopy();
    int rating = 0;

    Board.placeWord(word, boardCopy);
    var lines = Board.getLines(boardCopy);

    // Also compute the horizontal and vertical lines for the tiles that have already been rated
    boolean[][] alreadyRatedLines = new boolean[Board.SIZE * 2][Board.SIZE];
    // Copy rows
    System.arraycopy(board.getOccupiedTiles(), 0, alreadyRatedLines, 0, Board.SIZE);
    // Copy columns
    for (int i = Board.SIZE; i < Board.SIZE * 2; i++) {
      for (int j = 0; j < Board.SIZE; j++) {
        alreadyRatedLines[i][j] = board.getOccupiedTiles()[j][i - Board.SIZE];
      }
    }

    var relevantLineIndices = new ArrayList<Integer>();

    // Filter to lines containing unrated letters
    for (int i = 0; i < Board.SIZE * 2; i++) {
      for (int j = 0; j < Board.SIZE; j++) {
        if (!alreadyRatedLines[i][j] && lines.get(i).charAt(j) != ' ') {
          relevantLineIndices.add(i);
          break;
        }
      }
    }

    for (int i : relevantLineIndices) { // Main loop to go through and add ratings
      // Find an index which is inside the word we need to rate by scanning through the line,
      // and choosing the first unrated letter
      OptionalInt found = OptionalInt.empty();
      for (int j = 0; j < Board.SIZE; j++) {
        if (!alreadyRatedLines[i][j] && lines.get(i).charAt(j) != ' ') {
          found = OptionalInt.of(j);
          break;
        }
      }
      int indexInWord = found.orElseThrow();

      // Move backwards to find the index of start of the word (inclusive)
      int k = indexInWord - 1;
      while (k >= 0 && k < Board.SIZE && lines.get(i).charAt(k) != ' ') {
        k--;
      }
      k++;

      // Move forwards to find the index of end of the word (exclusive)
      int l = indexInWord + 1;
      while (l >= 0 && l < Board.SIZE && lines.get(i).charAt(l) != ' ') {
        l++;
      }

      // Generate the letters along the correct axis
      var letters = new ArrayList<Letter>();
      for (int m = k; m < l; m++) {
        if (i < Board.SIZE) { // Horizontal
          letters.add(new Letter(lines.get(i).charAt(m), m, i, !alreadyRatedLines[i][m]));
        } else { // Vertical
          letters.add(
              new Letter(lines.get(i).charAt(m), i - Board.SIZE, m, !alreadyRatedLines[i][m]));
        }
      }

      // Do not rate 1 letter non-words
      if (letters.size() > 1) {
        rating += rateWord(letters);
      }
    }

    return rating;
  }
}
