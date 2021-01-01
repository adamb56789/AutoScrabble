package autoscrabble;

import autoscrabble.word.LineWord;
import autoscrabble.word.RatedWord;
import autoscrabble.word.LocatedWord;

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
import java.util.stream.IntStream;

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

  public boolean boardIsValid(LocatedWord move) {
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
            .allMatch(wordFinder::isWord); // Check against the dictionary
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

  private void placeWord(LocatedWord word, char[][] board) {
    for (int i = 0; i < word.getWord().length(); i++) {
      if (word.getDirection() == Direction.HORIZONTAL) {
        board[word.getY()][word.getX() + i] = word.getWord().charAt(i);
      } else {
        board[word.getY() + i][word.getX()] = word.getWord().charAt(i);
      }
    }
  }

  /**
   * Generate all possible words that could be placed on the given line. The index is the row or
   * column index of the line.
   */
  private List<RatedWord> generateWordsOnLine(char[] rack, String line, int index, Direction direction) {
    // If line is blank then skip
    if (line.isBlank()) {
      return null;
    }

    // Get a list of possible words
    List<LineWord> words = wordFinder.getWords(line.toCharArray(), rack);

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
    var lines = getLines(board);
    var words = IntStream.range(0, board.length * 2) // Scan through horizontal then vertical lines
            .parallel() // I am speed
            .mapToObj(i -> {
              // Get the row index if horizontal or col index if vertical, and the direction
              int index = i < board.length ? i : i - board.length;
              var direction = i < board.length ? Direction.HORIZONTAL : Direction.VERTICAL;
              return generateWordsOnLine(rack, lines.get(i), index, direction);
            })
            .filter(Objects::nonNull) // Remove any lines that were skipped
            .flatMap(List::stream) // Merge lists
            .collect(Collectors.toList());

    if (words.size() == 0) {
      userMessage = "No words found";
      setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
      return;
    } else {
      // Sort the words descending by score
      words.sort(Collections.reverseOrder(Comparator.comparingDouble(RatedWord::getRating)));

      // Get the first one that is valid
      for (var word : words) {
        if (boardIsValid(word)) {
          userMessage = String.format("Found %s at (%c%d) %s, scoring at least %s, ",
                  word.getWord(),
                  ((char) (word.getX() + 97)),
                  (15 - word.getY()),
                  word.getDirection() == Direction.HORIZONTAL ? "horizontally" : "vertically",
                  word.getRating());
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
