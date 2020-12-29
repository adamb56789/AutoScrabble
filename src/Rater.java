import java.util.Arrays;

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
      try {
        if (Character.isAlphabetic(c)) {
          lettersOnBoard++;
        }
      } catch (Exception ignored) {
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
      switch (word[i][0].charAt(0)) {
        case 'A':
        case 'U':
        case 'T':
        case 'S':
        case 'R':
        case 'O':
        case 'N':
        case 'L':
        case 'I':
        case 'E':
          rating[i] += 1;
          break;
        case 'D':
        case 'G':
          rating[i] += 2;
          break;
        case 'B':
        case 'C':
        case 'P':
        case 'M':
          rating[i] += 3;
          break;
        case 'F':
        case 'Y':
        case 'W':
        case 'V':
        case 'H':
          rating[i] += 4;
          break;
        case 'K':
          rating[i] += 5;
          break;
        case 'J':
        case 'X':
          rating[i] += 8;
          break;
        case 'Q':
        case 'Z':
          rating[i] += 10;
          break;
        default:
      }
    }
    return rating;
  }

  public int rate(String[] location) {
    char[][] boardT = new char[15][15];
    boolean[][] rated = new boolean[15][15];
    int rating = 0;

    for (int i = 0; i < 15; i++) {
      System.arraycopy(board.getBoard()[i], 0, boardT[i], 0, 15);
    }
    for (int i = 0; i < 15; i++) {
      System.arraycopy(board.getOccupiedTiles()[i], 0, rated[i], 0, 15);
    }
    for (int i = 0; i < location[0].length(); i++) {
      if ("h".equals(location[3])) {
        boardT[Integer.parseInt(location[2])][Integer.parseInt(location[1]) + i] =
            location[0].charAt(i);
      } else {
        boardT[Integer.parseInt(location[2]) + i][Integer.parseInt(location[1])] =
            location[0].charAt(i);
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
        String[][] word = null;
        int length = 0;
        for (int j = 0; j < 15; j++) {
          if (!ratedLines[i][j] && Character.isAlphabetic(lines[i][j])) {
            boolean foobar = true;
            int k = j - 1;
            while (foobar) { // Count backwards
              length++;
              try {
                if (!Character.isAlphabetic(lines[i][k])) {
                  foobar = false;
                }
              } catch (Exception e) {
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
              try {
                if (!Character.isAlphabetic(lines[i][l])) {
                  foobar = false;
                }
              } catch (Exception e) {
                foobar = false;
              }
              l++;
            }
            length--;
            word = new String[l - k - 2][5];
            for (int m = k + 1, n = 0; m < l - 1; m++, n++) {
              word[n][0] = lines[i][m] + "";
              if (i < 15) { // Horizontal
                word[n][1] = i + "";
                word[n][2] = m + "";
              } else { // Vertical
                word[n][1] = m + "";
                word[n][2] = i - 15 + "";
              }
              if (ratedLines[i][m]) {
                word[n][3] = "n";
              }
              if (Character.isLowerCase(word[n][0].charAt(0))) {
                word[n][4] = "y";
              }
            }
            j = 9000001;
          }
        }
        length++;
        if (length > 1) {
          rating += rateWord(word, oldLines[i]);
        }
      }
    }
    return rating;
  }
}
