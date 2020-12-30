package autoscrabble;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Board extends JFrame implements KeyListener {
  private final String[] hand = {"A", "B", "C", "D", "E", "F", "G"};
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

  public String[] getHand() {
    return hand;
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

  public boolean validBoard(String[] move) {
    char[][] boardT = new char[15][15];

    for (int i = 0; i < board.length; i++) {
      System.arraycopy(board[i], 0, boardT[i], 0, board[i].length);
    }

    if (!"check".equals(move[0])) {
      for (int i = 0; i < move[0].length(); i++) {
        if ("h".equals(move[3])) {
          boardT[Integer.parseInt(move[2])][Integer.parseInt(move[1]) + i] = move[0].charAt(i);
        } else {
          boardT[Integer.parseInt(move[2]) + i][Integer.parseInt(move[1])] = move[0].charAt(i);
        }
      }
    }

    char[][] lines = new char[30][15];

    // Copy rows
    System.arraycopy(boardT, 0, lines, 0, 15);
    // Copy columns
    for (int i = 15; i < 30; i++) {
      for (int j = 0; j < 15; j++) {
        lines[i][j] = boardT[j][i - 15];
      }
    }

    for (int i = 0; i < 30; i++) {
      for (int j = 0; j < 15; j++) {
        if (Character.isAlphabetic(lines[i][j]) && Character.isAlphabetic(lines[i][j + 1])) {
          StringBuilder word = new StringBuilder(lines[i][j] + "");
          int k = 1;

          if (!Character.isAlphabetic(lines[i][j + k])) {
            word = new StringBuilder();
          }

          int f = 0;

          while (Character.isAlphabetic(lines[i][j + k])) {
            word.append(lines[i][j + k]);
            k++;
            f++;
          }

          j += f;

          if (!wordFinder.isWord(word.toString())) {
            return false;
          }
        }
      }
    }

    return true;
  }

  public String[][] joinArray(String[][] array1, String[][] array2, int l) {
    String[][] array3 = new String[array1.length + array2.length][l];
    System.arraycopy(array1, 0, array3, 0, array1.length);
    System.arraycopy(array2, 0, array3, array1.length, array2.length);
    return array3;
  }

  public String[][] findWord(int[][] s, String[] Hand) {
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
    for (String letter : hand) {
      if ("_".equals(letter)) {
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

  public void onEnter(boolean useBlank) {
    double startTime = System.currentTimeMillis();
    String[] arr = {"check"};
    if (!validBoard(arr)) {
      userMessage = "Current board not valid";
      setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
      return;
    }
    var handTemp = new String[hand.length];
    System.arraycopy(hand, 0, handTemp, 0, hand.length);
    if (!useBlank) {
      for (int i = 0; i < hand.length; i++) {
        if ("_".equals(hand[i])) {
          hand[i] = "";
          break;
        }
      }
    }

    int blankL = -1;
    for (int i = 0; i < hand.length; i++) { // Getting blank location
      if (hand[i].equals("_")) {
        if (blankL != -1) {
          userMessage = "Only one blank allowed";
          setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
          return;
        }
        blankL = i;
      }
    }

    String[][] validWords = {};
    if (blankL == -1) { // No blank
      for (int i = 0; i < 15; i++) {
        int[][] select1 = {{i, 0}, {i, 14}};
        validWords = joinArray(validWords, findWord(select1, hand), 5);
        int[][] select2 = {{0, i}, {14, i}};
        validWords = joinArray(validWords, findWord(select2, hand), 5);
      }
    } else { // blank
      for (int i = 0; i < 15; i++) {
        int[][] select1 = {{i, 0}, {i, 14}};
        validWords = joinArray(validWords, findWord(select1, hand), 5);
        int[][] select2 = {{0, i}, {14, i}};
        validWords = joinArray(validWords, findWord(select2, hand), 5);
      }
    }
    String message = "";
    if (validWords.length == 0) {
      userMessage = "No words found";
      System.arraycopy(handTemp, 0, hand, 0, hand.length);
      setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
      return;
    } else {
      Arrays.sort(
          validWords,
          (String[] a, String[] b) ->
              Double.compare(Double.parseDouble(b[4]), Double.parseDouble(a[4])));
      for (String[] word : validWords) {
        message =
            "Checking word " + word[0] + " which would score " + word[4] + " (press ESC to cancel)";
        if (validBoard(word)) {
          message = "Found ";
          message +=
              word[0]
                  + " at ("
                  + ((char) (Integer.parseInt(word[1]) + 97))
                  + (15 - Integer.parseInt(word[2]))
                  + ")";
          if ("h".equals(word[3])) {
            message += " horizontally";
          } else {
            message += " vertically";
          }
          message += ", scoring at least " + word[4] + ", ";
          break;
        }
        if (userInterrupt) {
          userInterrupt = false;
          message = "Cancelled ";
          break;
        }
      }
    }
    userMessage = message;

    double finishTime = System.currentTimeMillis();
    System.out.println("Took " + (finishTime - startTime) + " ms");
    if (userMessage.equals("Cancelled ")) {
      userMessage += "after " + (finishTime - startTime) + " ms";
    } else {
      userMessage += "in " + (finishTime - startTime) + " ms";
    }
    System.out.println(userMessage);
    System.arraycopy(handTemp, 0, hand, 0, hand.length);
    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    repaint();
  }

  @Override
  public void keyTyped(KeyEvent e) {
    if (xSelection != -1) {
      if (Character.isAlphabetic(e.getKeyChar())) {
        board[ySelection][xSelection] = Character.toUpperCase(e.getKeyChar());
        occupiedTiles[ySelection][xSelection] = true;
      }
      if (e.getKeyChar() == KeyEvent.VK_BACK_SPACE) {
        board[ySelection][xSelection] = ' ';
        occupiedTiles[ySelection][xSelection] = false;
      }
    }
    if (handSelection != -1) {
      if (Character.isAlphabetic(e.getKeyChar()))
        hand[handSelection] = (e.getKeyChar() + "").toUpperCase();
      if (e.getKeyChar() == KeyEvent.VK_BACK_SPACE) {
        hand[handSelection] = "";
      }
      if (e.getKeyChar() == '-') {
        hand[handSelection] = "_";
      }
    }
    repaint();
  }

  @Override
  public void keyPressed(KeyEvent e) {
    if (e.getExtendedKeyCode() == KeyEvent.VK_ENTER) {
      setCursor(new Cursor(Cursor.WAIT_CURSOR));
      onEnter(true);
    }
    if (e.getExtendedKeyCode() == KeyEvent.VK_SHIFT) {
      setCursor(new Cursor(Cursor.WAIT_CURSOR));
      onEnter(false);
    }
    if (handSelection == -1) {
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
      if (e.getExtendedKeyCode() == KeyEvent.VK_UP && handSelection > 0) {
        handSelection--;
      } else if (e.getExtendedKeyCode() == KeyEvent.VK_DOWN && handSelection < 6) {
        handSelection++;
      }
    }
    if (e.getExtendedKeyCode() == KeyEvent.VK_ESCAPE) {
      userInterrupt = true;
    }
    repaint();
  }

  @Override
  public void keyReleased(KeyEvent e) {}
}
