package autoscrabble;

import autoscrabble.word.Letter;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;

public class Gui extends JComponent implements MouseListener, KeyListener {
  public static final Font FONT_LARGE = new Font("Arial", Font.PLAIN, 30);
  public static final Font FONT_MEDIUM = new Font("Arial", Font.PLAIN, 20);
  public static final Font FONT_SMALL = new Font("Arial", Font.PLAIN, 12);

  public static final Color BOARD_BACKGROUND_COLOUR = Color.decode("#ECE8D9");
  public static final Color TEXT_COLOUR = Color.black;

  public static final int MESSAGE_X = 15;
  public static final int MESSAGE_Y_OFFSET = 38;

  public static final Color OUTLINE_COLOUR = Color.black;
  public static final int OUTLINE_WEIGHT = 4;

  public static final Color SQUARE_COLOUR = Color.decode("#C8C2A8");
  public static final int SQUARE_SIZE = 40;
  public static final int SQUARE_SPACING = 4;
  public static final int SQUARE_OFFSET = SQUARE_SIZE + SQUARE_SPACING;
  public static final int BOARD_SIZE = 2 * OUTLINE_WEIGHT + SQUARE_SPACING + Board.SIZE * SQUARE_OFFSET;

  public static final int LETTER_COORDS_X_OFFSET = 25;
  public static final int LETTER_COORDS_Y_OFFSET = 10;
  public static final int NUMBER_COORDS_X_OFFSET = 1;
  public static final int NUMBER_COORDS_Y_OFFSET = 11;

  public static final int TILE_LETTER_X = 4;
  public static final int TILE_LETTER_Y = 32;
  public static final int TILE_SCORE_Y = 34;
  public static final int TILE_SCORE_X = 29;
  public static final int TILE_SCORE_X_CORRECTION = 4;

  public static final Color RACK_COLOUR = Color.decode("#8C4000");
  public static final int RACK_X = BOARD_SIZE + 18;
  public static final int RACK_Y = 16;
  public static final int RACK_TILE_SPACING = 4;
  public static final int RACK_WIDTH = SQUARE_SIZE + 2 * RACK_TILE_SPACING;
  public static final int RACK_HEIGHT = Board.RACK_CAPACITY * SQUARE_SIZE + (Board.RACK_CAPACITY + 1) * RACK_TILE_SPACING;

  public static final Color SELECTION_COLOUR = Color.red;
  public static final int SELECTION_INTERIOR_OVERLAP = 2;
  public static final int SELECTION_LINE_WEIGHT = 4;

  public static final Cursor DEFAULT_CURSOR = new Cursor(Cursor.DEFAULT_CURSOR);
  public static final Cursor WAITING_CURSOR = new Cursor(Cursor.WAIT_CURSOR);
  private final Board board;
  private final Frame frame;
  private Image tripleWord;
  private Image doubleWord;
  private Image tripleLetter;
  private Image doubleLetter;
  private Image start;
  private Image tile;
  private int rackSelection = -1;
  private int xSelection = -1;
  private int ySelection = -1;

  public Gui(Board board) {
    super();
    // Load images
    try {
      tripleWord = ImageIO.read(getClass().getResource("/images/tripleWord.png"));
      doubleWord = ImageIO.read(getClass().getResource("/images/doubleWord.png"));
      tripleLetter = ImageIO.read(getClass().getResource("/images/tripleLetter.png"));
      doubleLetter = ImageIO.read(getClass().getResource("/images/doubleLetter.png"));
      start = ImageIO.read(getClass().getResource("/images/start.png"));
      tile = ImageIO.read(getClass().getResource("/images/tile.png"));
    } catch (IOException e) {
      e.printStackTrace();
    }
    Gui gui = this;
    this.frame = new JFrame() {
      {
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        addKeyListener(gui);
        setTitle("Scrabble Perfect");
        setResizable(false);
        setMinimumSize(new Dimension(750, 750));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().add(gui);
        pack();
        setLocationRelativeTo(this);
        setVisible(true);
        getContentPane().setBackground(Color.decode("#F3F3F3"));
      }
    };

    this.board = board;
    addMouseListener(this);
    addKeyListener(this);
  }

  @Override
  protected void paintComponent(Graphics g) {
    paintComponent((Graphics2D) g);
  }

  protected void paintComponent(Graphics2D g) {
    super.paintComponent(g);
    g.setRenderingHint(
        RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

    // Draw the user message
    g.setFont(FONT_MEDIUM);
    g.setColor(TEXT_COLOUR);
    g.drawString(board.getUserMessage(), MESSAGE_X, BOARD_SIZE + MESSAGE_Y_OFFSET);

    g.setColor(BOARD_BACKGROUND_COLOUR);
    g.fillRect(0, 0, BOARD_SIZE, BOARD_SIZE);

    // Draw the coordinates
    g.setFont(FONT_SMALL);
    g.setColor(TEXT_COLOUR);
    for (int i = 0; i < Board.SIZE; i++) {
      g.drawString((char) (i + 97) + "",
              LETTER_COORDS_X_OFFSET + i * SQUARE_OFFSET,
              BOARD_SIZE + LETTER_COORDS_Y_OFFSET);
      g.drawString((i + 1) + "",
              BOARD_SIZE + NUMBER_COORDS_X_OFFSET,
              (Board.SIZE - i) * SQUARE_OFFSET - NUMBER_COORDS_Y_OFFSET);
    }

    // Draw the outline of the board
    g.setColor(OUTLINE_COLOUR);
    g.setStroke(new BasicStroke(OUTLINE_WEIGHT));
    int rectSize = BOARD_SIZE - OUTLINE_WEIGHT;
    g.drawRect(OUTLINE_WEIGHT / 2, OUTLINE_WEIGHT / 2, rectSize, rectSize);

    // Draw the squares
    g.setColor(SQUARE_COLOUR);
    for (int i = 0; i < Board.SIZE; i++) {
      for (int j = 0; j < Board.SIZE; j++) {
        int squareX = j * SQUARE_OFFSET + OUTLINE_WEIGHT;
        int squareY = i * SQUARE_OFFSET + OUTLINE_WEIGHT;
        if (i == Board.SIZE / 2 && j == Board.SIZE / 2) {
          g.drawImage(start, squareX, squareY, this);
        } else {
          switch (Rater.BONUSES[j][i]) {
            case 'W' -> g.drawImage(tripleWord, squareX, squareY, this);
            case 'w' -> g.drawImage(doubleWord, squareX, squareY, this);
            case 'L' -> g.drawImage(tripleLetter, squareX, squareY, this);
            case 'l' -> g.drawImage(doubleLetter, squareX, squareY, this);
            default -> g.fillRect(squareX + SQUARE_SPACING, squareY + SQUARE_SPACING, SQUARE_SIZE, SQUARE_SIZE);
          }
        }
      }
    }

    // Draw the tiles
    g.setColor(TEXT_COLOUR);
    for (int i = 0; i < Board.SIZE; i++) {
      for (int j = 0; j < Board.SIZE; j++) {
        if (board.getBoard()[j][i] != ' ') {
          drawTile(g,
                  board.getBoard()[j][i],
                  i * SQUARE_OFFSET + OUTLINE_WEIGHT + SQUARE_SPACING,
                  j * SQUARE_OFFSET + OUTLINE_WEIGHT + SQUARE_SPACING);
        }
      }
    }

    // Draw the selection box
    if (xSelection != -1) {
      drawSelectionBox(g,
              xSelection * SQUARE_OFFSET + OUTLINE_WEIGHT + SQUARE_SPACING,
              ySelection * SQUARE_OFFSET + OUTLINE_WEIGHT + SQUARE_SPACING);
    }

    drawTileRack(g);
  }

  /**
   * Draw the tile rack and its tiles at the specified location
   *  @param g the graphics to draw on
   * */
  private void drawTileRack(Graphics2D g) {
    // Draw background
    g.setColor(RACK_COLOUR);
    g.fillRect(Gui.RACK_X, Gui.RACK_Y, RACK_WIDTH, RACK_HEIGHT);

    // Draw the tiles
    g.setColor(TEXT_COLOUR);
    char[] hand = board.getRack();
    for (int i = 0; i < hand.length; i++) {
      if (hand[i] != ' ') {
        if (hand[i] == '_' || Character.isAlphabetic(hand[i])) {
          int tileX = Gui.RACK_X + RACK_TILE_SPACING;
          int tileY = Gui.RACK_Y + RACK_TILE_SPACING + i * (SQUARE_SIZE + RACK_TILE_SPACING);
          drawTile(g, hand[i], tileX, tileY);
        }
      }
    }

    // Draw the selection
    if (rackSelection != -1) {
      int tileX = Gui.RACK_X + RACK_TILE_SPACING;
      int tileY = Gui.RACK_Y + RACK_TILE_SPACING + rackSelection * (SQUARE_SIZE + RACK_TILE_SPACING);
      drawSelectionBox(g, tileX, tileY);
      g.setColor(SELECTION_COLOUR);
    }
  }

  /**
   * Draw a selection box at the specified location
   *
   * @param g the graphics to draw on
   * @param x the x position
   * @param y the y position
   */
  private void drawSelectionBox(Graphics2D g, int x, int y) {
    g.setColor(SELECTION_COLOUR);
    int rectSize = SQUARE_SIZE + SELECTION_LINE_WEIGHT - 2 * SELECTION_INTERIOR_OVERLAP;
    int squareOffset = SELECTION_INTERIOR_OVERLAP - SELECTION_LINE_WEIGHT / 2;

    g.setStroke(new BasicStroke(SELECTION_LINE_WEIGHT));
    g.drawRect(x + squareOffset, y + squareOffset, rectSize, rectSize);
  }

  /**
   * Draw a tile at the specified location
   *
   * @param g the graphics to draw on
   * @param letter the tile's letter
   * @param x the x position
   * @param y the y position
   */
  private void drawTile(Graphics2D g, char letter, int x, int y) {
    // The blank tile image
    g.drawImage(tile, x, y, this);

    // The letter
    g.setFont(FONT_LARGE);
    if (letter != '_') { // Do not draw if is a blank
      g.drawString(String.valueOf(letter), x + TILE_LETTER_X, y + TILE_LETTER_Y);
    }

    // The score
    g.setFont(FONT_SMALL);
    String score = Integer.toString(Letter.letterScore(letter));
    int scoreX = TILE_SCORE_X;
    if (score.length() > 1) { // If it is a bigger number it needs more space
      scoreX = TILE_SCORE_X - TILE_SCORE_X_CORRECTION;
    }
    g.drawString(score, x + scoreX, y + TILE_SCORE_Y);
  }

  @Override
  public void mousePressed(MouseEvent e) {
    // Remove the previous selection
    rackSelection = -1;
    xSelection = -1;
    ySelection = -1;

    int x = e.getX();
    int y = e.getY();
    int borderWidth = OUTLINE_WEIGHT + SQUARE_SPACING / 2;
    if (borderWidth <= x && x < BOARD_SIZE - borderWidth &&
            borderWidth <= y && y < BOARD_SIZE - borderWidth) {
      // If the click is in the board
      xSelection = (x - borderWidth) / SQUARE_OFFSET;
      ySelection  = (y - borderWidth) / SQUARE_OFFSET;

      // Right clicking inverts the case to identify a tile as blank
      if (e.getButton() == MouseEvent.BUTTON3) {
        char oldChar = board.getBoard()[ySelection][xSelection];
        if (Character.isUpperCase(oldChar)) {
          board.getBoard()[ySelection][xSelection] = Character.toLowerCase(oldChar);
        } else {
          board.getBoard()[ySelection][xSelection] = Character.toUpperCase(oldChar);
        }
      }
    } else if (RACK_X <= x && x < RACK_X + RACK_WIDTH &&
            RACK_Y + RACK_TILE_SPACING/2 <= y && y < RACK_Y + RACK_HEIGHT - RACK_TILE_SPACING/2) {
      // If the click is in the rack
      rackSelection = (y - RACK_Y + RACK_TILE_SPACING/2) / (SQUARE_SIZE + RACK_TILE_SPACING);
    }
    repaint();
  }

  @Override
  public void keyTyped(KeyEvent e) {
    if (xSelection != -1) {
      if (Character.isAlphabetic(e.getKeyChar())) {
        board.placeTile(e.getKeyChar(), xSelection, ySelection);
      } else if (e.getKeyChar() == KeyEvent.VK_BACK_SPACE) {
        board.placeTile(' ', xSelection, ySelection);
      }
    } else if (rackSelection != -1) {
      if (Character.isAlphabetic(e.getKeyChar())) {
        board.placeInRack(e.getKeyChar(), rackSelection);
      } else if (e.getKeyChar() == KeyEvent.VK_BACK_SPACE) {
        board.placeInRack(' ', rackSelection);
      } else if (e.getKeyChar() == ' ' || e.getKeyChar() == '-') {
        board.placeInRack('_', rackSelection);
      }
    }
    repaint();
  }

  @Override
  public void keyPressed(KeyEvent e) {
    if (e.getExtendedKeyCode() == KeyEvent.VK_ENTER) {
      setCursor(WAITING_CURSOR);
      board.findBestWord();
      setCursor(DEFAULT_CURSOR);
    } else if (e.getExtendedKeyCode() == KeyEvent.VK_ESCAPE) {
      board.userInterrupt();
    } else if (rackSelection == -1) {
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
              && rackSelection > 0) {
        rackSelection--;
      } else if ((e.getExtendedKeyCode() == KeyEvent.VK_DOWN
              || e.getExtendedKeyCode() == KeyEvent.VK_RIGHT)
              && rackSelection < 6) {
        rackSelection++;
      }
    }
    repaint();
  }

  @Override
  public void mouseClicked(MouseEvent e) {}

  @Override
  public void keyReleased(KeyEvent e) {}

  @Override
  public void mouseReleased(MouseEvent e) {}

  @Override
  public void mouseEntered(MouseEvent e) {}

  @Override
  public void mouseExited(MouseEvent e) {}
}
