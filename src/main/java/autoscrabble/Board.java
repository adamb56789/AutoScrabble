package autoscrabble;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class Board extends JFrame implements KeyListener {
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
  private final boolean[][] occupiedTiles = new boolean[15][15];
  private boolean userInterrupt;
  private String userMessage = "";
  private int handSelection = -1;
  private int xSelection = -1;
  private int ySelection = -1;

  public Board() {
    addKeyListener(this);
    setFocusable(true);
    setFocusTraversalKeysEnabled(false);
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
   * Gets the score of the given letter.
   *
   * @param letter a char containing the letter
   * @return the score as an int
   */
  public static int getLetterRating(char letter) {
    return switch (letter) {
      case 'A', 'U', 'T', 'S', 'R', 'O', 'N', 'L', 'I', 'E' -> 1;
      case 'D', 'G' -> 2;
      case 'B', 'C', 'P', 'M' -> 3;
      case 'F', 'Y', 'W', 'V', 'H' -> 4;
      case 'K' -> 5;
      case 'J', 'X' -> 8;
      case 'Q', 'Z' -> 10;
      default -> 0;
    };
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

  public String getUserMessage() {
    return userMessage;
  }

  public int getHandSelection() {
    return handSelection;
  }

  public void setHandSelection(int handSelection) {
    this.handSelection = handSelection;
  }

  public int getxSelection() {
    return xSelection;
  }

  public int getySelection() {
    return ySelection;
  }

  public void setSelection(int xSelection, int ySelection) {
    this.xSelection = xSelection;
    this.ySelection = ySelection;
  }

  public boolean boardIsValid(String[] move) {
    char[][] boardCopy = Arrays.stream(board).map(char[]::clone).toArray(char[][]::new);

    // Place the word if there is one
    if (move != null) {
      placeWord(move, boardCopy);
    }

    return getLines(boardCopy) // Get all horizontal and vertical lines
            .stream()
            .map(arr -> arr.split(" +")) // Split each line around spaces
            .flatMap(Arrays::stream) // Merge the result
            .filter(w -> w.length() > 1) // Filter out blank or 1 letter words
            .anyMatch(wordFinder::isWord); // Check against the dictionary
  }

  /**
   * Get strings of the horizontal and vertical lines on the board
   *
   * @param board the board
   * @return a list of Strings
   */
  private List<String> getLines(char[][] board) {
    char[][] lines = new char[board.length * 2][board.length];
    // Copy rows
    System.arraycopy(board, 0, lines, 0, board.length);
    // Copy columns
    for (int i = board.length; i < board.length * 2; i++) {
      for (int j = 0; j < board.length; j++) {
        lines[i][j] = board[j][i - board.length];
      }
    }
    return Arrays.stream(lines).map(String::valueOf).collect(Collectors.toList());
  }

  private void placeWord(String[] word, char[][] board) {
    for (int i = 0; i < word[0].length(); i++) {
      if ("h".equals(word[3])) {
        board[Integer.parseInt(word[2])][Integer.parseInt(word[1]) + i] = word[0].charAt(i);
      } else {
        board[Integer.parseInt(word[2]) + i][Integer.parseInt(word[1])] = word[0].charAt(i);
      }
    }
  }

  public String[][] findWord(int[][] s, char[] Hand) {
    int gapL = Math.abs(s[0][0] - s[1][0]) + Math.abs(s[0][1] - s[1][1]) + 1;
    String[] location = new String[gapL];
    if (s[0][1] - s[1][1] == 0) { // Horizontal
      for (int i = 0; i < gapL; i++) {
        location[i] = board[s[0][1]][s[0][0] + i] + "";
        if (board[s[0][1]][s[0][0] + i] == ' ') {
          location[i] = "";
        }
      }
    } else { // Vertical
      for (int i = 0; i < gapL; i++) {
        location[i] = board[s[0][1] + i][s[0][0]] + "";
        if (board[s[0][1] + i][s[0][0]] == ' ') {
          location[i] = "";
        }
      }
    }
    // If line is blank then skip
    boolean isBlank = true;
    for (int i = 0; i < 15; i++) {
      if (!"".equals(location[i])) {
        isBlank = false;
        break;
      }
    }
    if (isBlank) {
      return new String[][] {};
    }
    // Calculate mode - normal (0), blanks on the board (1), blanks in the hand (2)
    int mode = 0;
    for (String tile : location) {
      if (tile.length() > 0 && Character.isLowerCase(tile.charAt(0))) {
        mode = 1;
      }
    }
    for (char letter : rack) {
      if (letter == '_') {
        mode = 2;
        break;
      }
    }
    String[][] validWords = wordFinder.getWords(location, Hand, mode);
    String[][] wordScores = new String[validWords.length][5];
    var rater = new Rater(this);
    for (int i = 0; i < validWords.length; i++) {
      wordScores[i][0] = validWords[i][0];

      if (s[0][1] - s[1][1] == 0) { // Horizontal
        wordScores[i][1] = (s[0][0] + validWords[i][1]) + "";
        wordScores[i][2] = s[0][1] + "";
        wordScores[i][3] = "h";
      } else { // Vertical
        wordScores[i][1] = s[0][0] + "";
        wordScores[i][2] = (s[0][1] + validWords[i][1]) + "";
        wordScores[i][3] = "v";
      }
      wordScores[i][4] = rater.rate(wordScores[i]) + "";
    }
    return wordScores;
  }

  private void findBestWord() {
    long startTime = System.currentTimeMillis();

    // If the board is invalid display an error
    if (!boardIsValid(null)) {
      userMessage = "Current board not valid";
      setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
      return;
    }

    // Get the index of the blank tile in the rack
    int blankIndex = -1;
    for (int i = 0; i < rack.length; i++) {
      if (rack[i] == '_') {
        if (blankIndex != -1) {
          // Using 2 blanks takes too long so don't allow it
          userMessage = "Only one blank allowed";
          setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
          return;
        }
        blankIndex = i;
      }
    }

    // Find all possible words
    var words = new ArrayList<String[]>();
    for (int i = 0; i < 15; i++) {
      int[][] select1 = {{i, 0}, {i, 14}};
      words.addAll(List.of(findWord(select1, rack)));
      int[][] select2 = {{0, i}, {14, i}};
      words.addAll(List.of(findWord(select2, rack)));
    }

    if (words.size() == 0) {
      userMessage = "No words found";
      setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
      return;
    } else {
      // Sort the words descending by score
      words.sort(Collections.reverseOrder(Comparator.comparingDouble(word -> Double.parseDouble(word[4]))));

      // Get the first one that is valid
      for (String[] word : words) {
        if (boardIsValid(word)) {
          userMessage = String.format("Found %s at (%c%d) %s, scoring at least %s, ",
                  word[0],
                  ((char) (Integer.parseInt(word[1]) + 97)),
                  (15 - Integer.parseInt(word[2])),
                  "h".equals(word[3]) ? "horizontally" : "vertically",
                  word[4]);
          break;
        }
        if (userInterrupt) {
          userMessage = "Cancelled ";
          break;
        }
      }
    }

    long finishTime = System.currentTimeMillis();
    userMessage += String.format("%s %d ms", userInterrupt ? "after" : "in", (finishTime - startTime));
    userInterrupt = false;
    System.out.println(userMessage);
    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    repaint();
  }

  @Override
  public void keyTyped(KeyEvent e) {
    if (xSelection != -1) {
      if (Character.isAlphabetic(e.getKeyChar())) {
        board[ySelection][xSelection] = Character.toUpperCase(e.getKeyChar());
        occupiedTiles[ySelection][xSelection] = true;
      } else if (e.getKeyChar() == KeyEvent.VK_BACK_SPACE) {
        board[ySelection][xSelection] = ' ';
        occupiedTiles[ySelection][xSelection] = false;
      }
    } else if (handSelection != -1) {
      if (Character.isAlphabetic(e.getKeyChar())) {
        rack[handSelection] = Character.toUpperCase(e.getKeyChar());
      } else if (e.getKeyChar() == KeyEvent.VK_BACK_SPACE) {
        rack[handSelection] = ' ';
      } else if (e.getKeyChar() == ' ' || e.getKeyChar() == '-') {
        rack[handSelection] = '_';
      }
    }
    repaint();
  }

  @Override
  public void keyPressed(KeyEvent e) {
    if (e.getExtendedKeyCode() == KeyEvent.VK_ENTER) {
      setCursor(new Cursor(Cursor.WAIT_CURSOR));
      findBestWord();
    } else if (e.getExtendedKeyCode() == KeyEvent.VK_ESCAPE) {
      userInterrupt = true;
    } else if (handSelection == -1) {
      if (e.getExtendedKeyCode() == KeyEvent.VK_LEFT && xSelection > 0) {
        xSelection--;
      } else if (e.getExtendedKeyCode() == KeyEvent.VK_RIGHT && xSelection < 14) {
        xSelection++;
      } else if (e.getExtendedKeyCode() == KeyEvent.VK_UP && ySelection > 0) {
        ySelection--;
      } else if (e.getExtendedKeyCode() == KeyEvent.VK_DOWN && ySelection < 14) {
        ySelection++;
      }
    } else {
      if ((e.getExtendedKeyCode() == KeyEvent.VK_UP || e.getExtendedKeyCode() == KeyEvent.VK_LEFT)
              && handSelection > 0) {
        handSelection--;
      } else if ((e.getExtendedKeyCode() == KeyEvent.VK_DOWN || e.getExtendedKeyCode() == KeyEvent.VK_RIGHT)
              && handSelection < 6) {
        handSelection++;
      }
    }
    repaint();
  }

  @Override
  public void keyReleased(KeyEvent e) {}
}
