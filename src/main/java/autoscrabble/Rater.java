package autoscrabble;

import autoscrabble.word.LocatedWord;

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

  private int rateWord(String[][] word, char[] location) {
    int[] rating = countPoints(word);
    int total = 0;

    for (int i = 0; i < rating.length; i++) {
      total += rateLetter(word[i], rating[i]);
    }
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

  private int rateLetter(String[] c, int base) {
    if ("y".equals(c[4])) {
      return 0;
    }
    if ("n".equals(c[3])) {
      return base;
    }
    if (BONUSES[Integer.parseInt(c[1])][Integer.parseInt(c[2])] == 'l') {
      return base * 2;
    }
    if (BONUSES[Integer.parseInt(c[1])][Integer.parseInt(c[2])] == 'L') {
      return base * 3;
    }
    return base;
  }

  private int wordMultiplier(String[][] c) {
    int m = 1;
    for (String[] strings : c) {
      if (!"n".equals(strings[3])) {
        if (BONUSES[Integer.parseInt(strings[1])][Integer.parseInt(strings[2])] == 'w') {
          m *= 2;
        }
        if (BONUSES[Integer.parseInt(strings[1])][Integer.parseInt(strings[2])] == 'W') {
          m *= 3;
        }
      }
    }
    return m;
  }

  private int[] countPoints(String[][] word) {
    int[] rating = new int[word.length];
    for (int i = 0; i < word.length; i++) {
      rating[i] += Board.getLetterRating(word[i][0].charAt(0));
    }
    return rating;
  }

  public int rate(LocatedWord word) {
    char[][] boardT = new char[15][15];
    boolean[][] rated = new boolean[15][15];
    int rating = 0;

    for (int i = 0; i < 15; i++) {
      System.arraycopy(board.getBoard()[i], 0, boardT[i], 0, 15);
    }
    for (int i = 0; i < 15; i++) {
      System.arraycopy(board.getOccupiedTiles()[i], 0, rated[i], 0, 15);
    }
    for (int i = 0; i < word.getWord().length(); i++) {
      if (word.getDirection() == Direction.HORIZONTAL) {
        boardT[word.getY()][word.getX() + i] = word.getWord().charAt(i);
      } else {
        boardT[word.getY() + i][word.getX()] = word.getWord().charAt(i);
      }
    }

    char[][] lines = new char[30][15];
    char[][] oldLines = new char[30][15];
    boolean[][] ratedLines = new boolean[30][15];
    boolean[] relevantLines = new boolean[30];

    // Copy rows
    System.arraycopy(boardT, 0, lines, 0, 15);
    System.arraycopy(rated, 0, ratedLines, 0, 15);
    // Copy columns
    for (int i = 15; i < 30; i++) {
      for (int j = 0; j < 15; j++) {
        lines[i][j] = boardT[j][i - 15];
        ratedLines[i][j] = rated[j][i - 15];
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
        if (!ratedLines[i][j] && Character.isAlphabetic(lines[i][j])) {
          relevantLines[i] = true;
        }
      }
    }
    for (int i = 0; i < 30; i++) { // Main loop to go through and add ratings
      if (relevantLines[i]) {
        String[][] wordData = null;
        int length = 0;
        for (int j = 0; j < 15; j++) {
          if (!ratedLines[i][j] && Character.isAlphabetic(lines[i][j])) {
            boolean foobar = true;
            int k = j - 1;
            while (foobar) { // Count backwards
              length++;
              if (k < 0 || k >= lines[0].length || !Character.isAlphabetic(lines[i][k])) {
                foobar = false;
              }
              k--;
            }
            k++;
            foobar = true;
            int l = j + 1;
            length--;
            while (foobar) { // Count forwards
              length++;
              if (l < 0 || l >= lines[0].length || !Character.isAlphabetic(lines[i][l])) {
                foobar = false;
              }
              l++;
            }
            length--;
            wordData = new String[l - k - 2][5];
            for (int m = k + 1, n = 0; m < l - 1; m++, n++) {
              wordData[n][0] = lines[i][m] + "";
              if (i < 15) { // Horizontal
                wordData[n][1] = i + "";
                wordData[n][2] = m + "";
              } else { // Vertical
                wordData[n][1] = m + "";
                wordData[n][2] = i - 15 + "";
              }
              if (ratedLines[i][m]) {
                wordData[n][3] = "n";
              }
              if (Character.isLowerCase(wordData[n][0].charAt(0))) {
                wordData[n][4] = "y";
              }
            }
            break;
          }
        }
        length++;
        if (length > 1) {
          rating += rateWord(wordData, oldLines[i]);
        }
      }
    }
    return rating;
  }
}
