package autoscrabble;

import autoscrabble.word.Letter;
import autoscrabble.word.LocatedWord;

import java.util.stream.IntStream;

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

  private double rateWord(Letter[] word, char[] location) {
    double total = 0;
    // Add up the scores of the letters
    for (Letter letter : word) {
      total += rateLetter(letter);
    }

    // Apply any potential word multipliers
    total *= wordMultiplier(word);

    // Add 50 if 7 letters are used
    int lettersOnBoard = 0;
    int lettersInWord = word.length;
    for (char c : location) {
      if (Character.isAlphabetic(c)) {
        lettersOnBoard++;
      }
    }
    if (lettersInWord - lettersOnBoard >= 7) {
      total += 50;
    }
    return total;
  }

  private int rateLetter(Letter letter) {
    int letterScore = letter.letterScore();
    if (Character.isLowerCase(letter.getChar())) {
      return 0;
    }
    if (!letter.bonusAllowed()) {
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

  private int wordMultiplier(Letter[] word) {
    int multiplier = 1;
    for (var letter : word) {
      if (letter.bonusAllowed()) {
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
    char[][] boardCopy = new char[15][15];
    boolean[][] rated = new boolean[15][15];
    int rating = 0;

    for (int i = 0; i < 15; i++) {
      System.arraycopy(board.getBoard()[i], 0, boardCopy[i], 0, 15);
    }
    for (int i = 0; i < 15; i++) {
      System.arraycopy(board.getOccupiedTiles()[i], 0, rated[i], 0, 15);
    }
    for (int i = 0; i < word.getWord().length(); i++) {
      if (word.getDirection() == Direction.HORIZONTAL) {
        boardCopy[word.getY()][word.getX() + i] = word.getWord().charAt(i);
      } else {
        boardCopy[word.getY() + i][word.getX()] = word.getWord().charAt(i);
      }
    }

    var size = board.getBoard().length;
    char[][] lines = new char[size * 2][size];
    char[][] oldLines = new char[size * 2][size];
    boolean[][] alreadyRatedLines = new boolean[size * 2][size];
    boolean[] relevantLines = new boolean[size * 2];

    // Copy rows
    System.arraycopy(boardCopy, 0, lines, 0, size);
    System.arraycopy(rated, 0, alreadyRatedLines, 0, size);
    // Copy columns
    for (int i = size; i < size * 2; i++) {
      for (int j = 0; j < size; j++) {
        lines[i][j] = boardCopy[j][i - size];
        alreadyRatedLines[i][j] = rated[j][i - size];
      }
    }

    // Copy rows of original board
    System.arraycopy(board.getBoard(), 0, oldLines, 0, 15);
    // Copy columns of original board
    for (int i = 15; i < 30; i++) {
      for (int j = 0; j < 15; j++) {
        oldLines[i][j] = board.getBoard()[j][i - 15];
      }
    }

    for (int i = 0; i < 30; i++) { // Get relevant lines
      for (int j = 0; j < 15; j++) {
        if (!alreadyRatedLines[i][j] && Character.isAlphabetic(lines[i][j])) {
          relevantLines[i] = true;
        }
      }
    }

    for (int i = 0; i < size * 2; i++) { // Main loop to go through and add ratings
      if (relevantLines[i]) {
        // Find an index which is inside the word we need to rate by scanning through the line,
        // filtering out already rated and empty tiles, and choosing the first.
        int i1 = i; // final variable for lambda
        int indexInWord =
            IntStream.range(0, size)
                .filter(j -> !alreadyRatedLines[i1][j] && Character.isAlphabetic(lines[i1][j]))
                .findFirst()
                .orElseThrow();

        // Move backwards to find the index of start of the word (inclusive)
        int k = indexInWord - 1;
        while (k >= 0 && k < size && Character.isAlphabetic(lines[i][k])) {
          k--;
        }
        k++;

        // Move forwards to find the index of end of the word (exclusive)
        int l = indexInWord + 1;
        while (l >= 0 && l < size && Character.isAlphabetic(lines[i][l])) {
          l++;
        }

        var letterData = new Letter[l - k];
        for (int m = k, n = 0; m < l; m++, n++) {
          if (i < size) { // Horizontal
            letterData[n] = new Letter(lines[i][m], m, i, !alreadyRatedLines[i][m]);
          } else { // Vertical
            letterData[n] = new Letter(lines[i][m], i - size, m, !alreadyRatedLines[i][m]);
          }
        }

        if (letterData.length > 1) {
          rating += rateWord(letterData, oldLines[i]);
        }
      }
    }
    return rating;
  }
}
