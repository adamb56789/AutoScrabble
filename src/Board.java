import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.util.Arrays;

public class Board extends JFrame implements KeyListener {
  public static boolean giveUp;
  public static boolean useBlank = true;
  public static String[] dictionary;
  public static String[] hand = {"A", "B", "C", "D", "E", "F", "G"};
  public static String[] handT = new String[26 * 4];
  public static boolean[][] rated = new boolean[15][15];
  public static char[][] board = {
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
  private final Gui gui;
  private final WordFinder wordFinder;

  public Board(Gui gui) {
    this.gui = gui;
    addKeyListener(this);
    setFocusable(true);
    setFocusTraversalKeysEnabled(false);
    int lines = 0;
    try {
      lines = countLines("Dictionary.txt") + 1;
      System.out.println("There are " + lines + " words in the dictionary.");
    } catch (IOException ignored) {
    }
    dictionary = new String[lines];

    FileReader fR;
    try {
      fR = new FileReader("Dictionary.txt");
      BufferedReader bR = new BufferedReader(fR);
      for (int i = 0; i < lines; i++) {
        dictionary[i] = bR.readLine();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    wordFinder = new WordFinder(dictionary);
  }

  public static int countLines(String filename) throws IOException {
    try (InputStream is = new BufferedInputStream(new FileInputStream(filename))) {
      byte[] c = new byte[1024];
      int count = 0;
      int readChars;
      boolean empty = true;
      while ((readChars = is.read(c)) != -1) {
        empty = false;
        for (int i = 0; i < readChars; ++i) {
          if (c[i] == '\n') {
            ++count;
          }
        }
      }
      return (count == 0 && !empty) ? 1 : count;
    }
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
        try {
          if (Character.isAlphabetic(lines[i][j]) && Character.isAlphabetic(lines[i][j + 1])) {
            StringBuilder word = new StringBuilder(lines[i][j] + "");
            int k = 1;

            if (!Character.isAlphabetic(lines[i][j + k])) {
              word = new StringBuilder();
            }

            int f = 0;

            try {
              while (Character.isAlphabetic(lines[i][j + k])) {
                word.append(lines[i][j + k]);
                k++;
                f++;
              }
            } catch (Exception ignored) {
            }
            j += f;

            if (!wordFinder.isWord(word.toString())) {
              return false;
            }
          }
        } catch (Exception ignored) {
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
    for (String value : location) {
      try {
        if (Character.isLowerCase(value.charAt(0))) {
          mode = 1;
        }
      } catch (Exception ignored) {
      }
    }
    for (int i = 0; i < location.length; i++) {
      try {
        if ("_".equals(hand[i])) {
          mode = 2;
        }
      } catch (Exception ignored) {
      }
    }
    String[][] validWords = wordFinder.getWords(location, Hand, mode);
    String[][] wordScores = new String[validWords.length][5];
    var rater = new Rater();
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

  public void onEnter() {
    String[] arr = {"check"};
    if (!validBoard(arr)) {
      gui.message = "Current board not valid";
      setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
      return;
    }
    System.arraycopy(hand, 0, handT, 0, hand.length);
    if (!useBlank) {
      useBlank = true;
      for (int i = 0; i < hand.length; i++) {
        if ("_".equals(hand[i])) {
          hand[i] = "";
          i = 9000001;
        }
      }
    }
    gui.message = "Searching for words...";
    gui.handSelect = -1;
    double startTime = System.currentTimeMillis();

    int blankL = -1;
    for (int i = 0; i < hand.length; i++) { // Getting blank location
      if (hand[i].equals("_")) {
        if (blankL != -1) {
          gui.message = "Only one blank allowed";
          setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
          return;
        }
        blankL = i;
      }
    }

    String[][] validWord1 = {};
    if (blankL == -1) { // No blank
      for (int i = 0; i < 15; i++) {
        int[][] select1 = {{i, 0}, {i, 14}};
        validWord1 = joinArray(validWord1, findWord(select1, hand), 5);
        int[][] select2 = {{0, i}, {14, i}};
        validWord1 = joinArray(validWord1, findWord(select2, hand), 5);
      }
    } else { // blank
      for (int i = 0; i < 15; i++) {
        int[][] select1 = {{i, 0}, {i, 14}};
        validWord1 = joinArray(validWord1, findWord(select1, hand), 5);
        int[][] select2 = {{0, i}, {14, i}};
        validWord1 = joinArray(validWord1, findWord(select2, hand), 5);
      }
    }
    String message = "";
    if (validWord1.length == 0) {
      gui.message = "No words found";
      System.arraycopy(handT, 0, hand, 0, hand.length);
      setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
      return;
    } else {
      Arrays.sort(
          validWord1,
          (String[] a, String[] b) ->
              Double.compare(Double.parseDouble(b[4]), Double.parseDouble(a[4])));
      for (int i = 0; i < validWord1.length; i++) {
        message =
            "Checking word "
                + validWord1[i][0]
                + " which would score "
                + validWord1[i][4]
                + " (press ESC to cancel)";
        if (validBoard(validWord1[i])) {
          message = "Found ";
          message +=
              validWord1[i][0]
                  + " at ("
                  + ((char) (Integer.parseInt(validWord1[i][1]) + 97))
                  + (15 - Integer.parseInt(validWord1[i][2]))
                  + ")";
          if ("h".equals(validWord1[i][3])) {
            message += " horizontally";
          } else {
            message += " vertically";
          }
          message += ", scoring at least " + validWord1[i][4] + ", ";
          i = 9000001;
        }
        if (giveUp) {
          giveUp = false;
          message = "Cancelled ";
          i = 9000001;
        }
      }
    }
    gui.message = message;

    double finishTime = System.currentTimeMillis();
    System.out.println("Took " + (finishTime - startTime) + " ms");
    if (gui.message.equals("Cancelled ")) {
      gui.message += "after " + (finishTime - startTime) + " ms";
    } else {
      gui.message += "in " + (finishTime - startTime) + " ms";
    }
    System.out.println(gui.message);
    System.arraycopy(handT, 0, hand, 0, hand.length);
    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    gui.repaint();
  }

  @Override
  public void keyTyped(KeyEvent e) {
    if (gui.selected[0] != -1) {
      if (Character.isAlphabetic(e.getKeyChar())) {
        board[gui.selected[1]][gui.selected[0]] = Character.toUpperCase(e.getKeyChar());
        rated[gui.selected[1]][gui.selected[0]] = true;
      }
      if (e.getKeyChar() == KeyEvent.VK_BACK_SPACE) {
        board[gui.selected[1]][gui.selected[0]] = ' ';
        rated[gui.selected[1]][gui.selected[0]] = false;
      }
    }
    if (gui.handSelect != -1) {
      if (Character.isAlphabetic(e.getKeyChar()))
        hand[gui.handSelect] = (e.getKeyChar() + "").toUpperCase();
      if (e.getKeyChar() == KeyEvent.VK_BACK_SPACE) {
        hand[gui.handSelect] = "";
      }
      if (e.getKeyChar() == '-') {
        hand[gui.handSelect] = "_";
      }
    }
    repaint();
  }

  @Override
  public void keyPressed(KeyEvent e) {
    if (e.getExtendedKeyCode() == KeyEvent.VK_ENTER) {
      setCursor(new Cursor(Cursor.WAIT_CURSOR));
      (new Thread(this::onEnter)).start();
    }
    if (e.getExtendedKeyCode() == KeyEvent.VK_SHIFT) {
      useBlank = false;
      setCursor(new Cursor(Cursor.WAIT_CURSOR));
      (new Thread(this::onEnter)).start();
    }
    if (gui.handSelect == -1) {
      if (e.getExtendedKeyCode() == KeyEvent.VK_LEFT && gui.selected[0] > 0) {
        gui.selected[0]--;
      } else if (e.getExtendedKeyCode() == KeyEvent.VK_RIGHT && gui.selected[0] < 14) {
        gui.selected[0]++;
      } else if (e.getExtendedKeyCode() == KeyEvent.VK_UP && gui.selected[1] > 0) {
        gui.selected[1]--;
      } else if (e.getExtendedKeyCode() == KeyEvent.VK_DOWN && gui.selected[1] < 14) {
        gui.selected[1]++;
      }
    } else {
      if (e.getExtendedKeyCode() == KeyEvent.VK_UP && gui.handSelect > 0) {
        gui.handSelect--;
      } else if (e.getExtendedKeyCode() == KeyEvent.VK_DOWN && gui.handSelect < 6) {
        gui.handSelect++;
      }
    }
    if (e.getExtendedKeyCode() == KeyEvent.VK_ESCAPE) giveUp = true;
    repaint();
  }

  @Override
  public void keyReleased(KeyEvent e) {}
}
