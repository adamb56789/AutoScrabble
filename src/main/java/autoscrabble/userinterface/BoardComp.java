package autoscrabble.userinterface;

import autoscrabble.Board;
import autoscrabble.Direction;
import autoscrabble.Rater;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

public class BoardComp extends JComponent {
  public static final int SQUARE_SIZE = 40;

  private static final Font FONT_MEDIUM = new Font("Arial", Font.PLAIN, 20);
  private static final Font FONT_SMALL = new Font("Arial", Font.PLAIN, 12);
  private static final Color BOARD_BACKGROUND_COLOUR = Color.decode("#ECE8D9");
  private static final Color TEXT_COLOUR = Color.black;
  private static final int MESSAGE_X = 15;
  private static final int MESSAGE_Y_OFFSET = 38;
  private static final Color OUTLINE_COLOUR = Color.black;
  private static final int OUTLINE_WEIGHT = 4;
  private static final Color SQUARE_COLOUR = Color.decode("#C8C2A8");
  private static final int SQUARE_SPACING = 4;
  private static final int SQUARE_OFFSET = SQUARE_SIZE + SQUARE_SPACING;
  private static final int BOARD_SIZE =
      2 * OUTLINE_WEIGHT + SQUARE_SPACING + Board.SIZE * SQUARE_OFFSET;

  private static final int LETTER_COORDS_X_OFFSET = 25;
  private static final int LETTER_COORDS_Y_OFFSET = 10;
  private static final int NUMBER_COORDS_X_OFFSET = 1;
  private static final int NUMBER_COORDS_Y_OFFSET = 11;

  private static final int BOARD_RIGHT_SPACING = 20;

  private static final String FIND_WORD = "find word";
  private static final String USER_INTERRUPT = "user interrupt";
  private static final String MOVE_UP = "move up";
  private static final String MOVE_DOWN = "move down";
  private static final String MOVE_LEFT = "move left";
  private static final String MOVE_RIGHT = "move right";

  public static Image tileImage;

  private static Image tripleWordImage;
  private static Image doubleWordImage;
  private static Image tripleLetterImage;
  private static Image doubleLetterImage;
  private static Image startImage;
  private final Board board;
  private int xSelection = 0;
  private int ySelection = 0;
  private Direction typingDirection = null;

  public BoardComp(Board board) {
    super();
    this.board = board;
    // Load images
    try {
      tripleWordImage = ImageIO.read(getClass().getResource("/images/tripleWord.png"));
      doubleWordImage = ImageIO.read(getClass().getResource("/images/doubleWord.png"));
      tripleLetterImage = ImageIO.read(getClass().getResource("/images/tripleLetter.png"));
      doubleLetterImage = ImageIO.read(getClass().getResource("/images/doubleLetter.png"));
      startImage = ImageIO.read(getClass().getResource("/images/start.png"));
      tileImage = ImageIO.read(getClass().getResource("/images/tile.png"));
    } catch (IOException e) {
      e.printStackTrace();
    }

    // Set up this component
    var dimension =
        new Dimension(BOARD_SIZE + BOARD_RIGHT_SPACING, BOARD_SIZE + MESSAGE_Y_OFFSET + 10);
    setMinimumSize(dimension);
    setPreferredSize(dimension);
    setFocusable(true);

    // Add listeners
    addMouseListener(new SelectTileMouseListener());
    addKeyListener(new LetterKeyListener());

    // Prepare key bindings
    getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke("UP"), MOVE_UP);
    getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke("DOWN"), MOVE_DOWN);
    getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke("LEFT"), MOVE_LEFT);
    getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke("RIGHT"), MOVE_RIGHT);
    // Global hotkeys
    getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ENTER"), FIND_WORD);
    getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ESCAPE"), USER_INTERRUPT);

    getActionMap().put(MOVE_UP, new MoveAction(0, -1));
    getActionMap().put(MOVE_DOWN, new MoveAction(0, 1));
    getActionMap().put(MOVE_LEFT, new MoveAction(-1, 0));
    getActionMap().put(MOVE_RIGHT, new MoveAction(1, 0));
    getActionMap().put(FIND_WORD, new FindWordAction());
    getActionMap().put(USER_INTERRUPT, new UserInterruptAction());

    addFocusListener(
        new FocusAdapter() {
          @Override
          public void focusGained(FocusEvent e) {
            // Repaint on focus to update selection boxes
            getParent().repaint();
          }
        });
  }

  @Override
  protected void paintComponent(Graphics g) {
    ((Graphics2D) g)
        .setRenderingHint(
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
      g.drawString(
          (char) (i + 97) + "",
          LETTER_COORDS_X_OFFSET + i * SQUARE_OFFSET,
          BOARD_SIZE + LETTER_COORDS_Y_OFFSET);
      g.drawString(
          (i + 1) + "",
          BOARD_SIZE + NUMBER_COORDS_X_OFFSET,
          (Board.SIZE - i) * SQUARE_OFFSET - NUMBER_COORDS_Y_OFFSET);
    }

    // Draw the outline of the board
    g.setColor(OUTLINE_COLOUR);
    ((Graphics2D) g).setStroke(new BasicStroke(OUTLINE_WEIGHT));
    int rectSize = BOARD_SIZE - OUTLINE_WEIGHT;
    g.drawRect(OUTLINE_WEIGHT / 2, OUTLINE_WEIGHT / 2, rectSize, rectSize);

    // Draw the squares
    g.setColor(SQUARE_COLOUR);
    for (int i = 0; i < Board.SIZE; i++) {
      for (int j = 0; j < Board.SIZE; j++) {
        int squareX = j * SQUARE_OFFSET + OUTLINE_WEIGHT;
        int squareY = i * SQUARE_OFFSET + OUTLINE_WEIGHT;
        if (i == Board.SIZE / 2 && j == Board.SIZE / 2) {
          g.drawImage(startImage, squareX, squareY, this);
        } else {
          switch (Rater.BONUSES[j][i]) {
            case 'W':
              g.drawImage(tripleWordImage, squareX, squareY, this);
              break;
            case 'w':
              g.drawImage(doubleWordImage, squareX, squareY, this);
              break;
            case 'L':
              g.drawImage(tripleLetterImage, squareX, squareY, this);
              break;
            case 'l':
              g.drawImage(doubleLetterImage, squareX, squareY, this);
              break;
            default:
              g.fillRect(
                  squareX + SQUARE_SPACING, squareY + SQUARE_SPACING, SQUARE_SIZE, SQUARE_SIZE);
              break;
          }
        }
      }
    }

    // Draw the tiles
    removeAll();
    setLayout(null);
    for (int i = 0; i < Board.SIZE; i++) {
      for (int j = 0; j < Board.SIZE; j++) {
        if (board.getBoard()[j][i] != ' ') {
          Tile.paintTile(
              g,
              board.getBoard()[j][i],
              i * SQUARE_OFFSET + OUTLINE_WEIGHT + SQUARE_SPACING,
              j * SQUARE_OFFSET + OUTLINE_WEIGHT + SQUARE_SPACING);
        }
      }
    }

    // Draw the selection box if in focus
    if (xSelection != -1 && isFocusOwner()) {
      Tile.drawSelectionBox(
          (Graphics2D) g,
          xSelection * SQUARE_OFFSET + OUTLINE_WEIGHT + SQUARE_SPACING,
          ySelection * SQUARE_OFFSET + OUTLINE_WEIGHT + SQUARE_SPACING);
    }
  }

  private class UserInterruptAction extends AbstractAction {
    @Override
    public void actionPerformed(ActionEvent e) {
      board.userInterrupt();
    }
  }

  private class FindWordAction extends AbstractAction {
    @Override
    public void actionPerformed(ActionEvent e) {
      getParent().setCursor(new Cursor(Cursor.WAIT_CURSOR));
      var word = board.findBestWordSmart();
      if (word != null) {
        requestFocus(); // Get focus to display word start location
        board.makeMove(word);
        xSelection = word.x;
        ySelection = word.y;
        typingDirection = null;
      }
      getParent().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
      getParent().repaint();
    }
  }

  private class MoveAction extends AbstractAction {
    private final int x;
    private final int y;

    MoveAction(int x, int y) {
      this.x = x;
      this.y = y;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      xSelection = Math.floorMod(xSelection + x, Board.SIZE);
      ySelection = Math.floorMod(ySelection + y, Board.SIZE);
      if (x == 0) {
        typingDirection = Direction.VERTICAL;
      } else {
        typingDirection = Direction.HORIZONTAL;
      }
      repaint();
    }
  }

  private class LetterKeyListener extends KeyAdapter {
    @Override
    public void keyTyped(KeyEvent e) {
      char keyChar = e.getKeyChar();
      if (Character.isAlphabetic(keyChar)) {
        if (typingDirection == null
            && board.getBoard()[ySelection][xSelection] != ' '
            && xSelection < Board.SIZE - 1
            && board.getBoard()[ySelection][xSelection + 1] == ' ') {
          typingDirection = Direction.HORIZONTAL;
        } else if (typingDirection == null
            && board.getBoard()[ySelection][xSelection] != ' '
            && ySelection < Board.SIZE - 1
            && board.getBoard()[ySelection + 1][xSelection] == ' ') {
          typingDirection = Direction.VERTICAL;
        }

        boolean placeBeforeMove =
            board.getBoard()[ySelection][xSelection] == ' '
                || Character.toUpperCase(board.getBoard()[ySelection][xSelection])
                    == Character.toUpperCase(keyChar);
        if (placeBeforeMove) {
          board.placeTile(Character.toUpperCase(keyChar), xSelection, ySelection);
        }
        if (xSelection < Board.SIZE - 1 && typingDirection == Direction.HORIZONTAL) {
          xSelection++;
        } else if (ySelection < Board.SIZE - 1 && typingDirection == Direction.VERTICAL) {
          ySelection++;
        }
        if (!placeBeforeMove) {
          board.placeTile(Character.toUpperCase(keyChar), xSelection, ySelection);
        }
      } else if (keyChar == KeyEvent.VK_BACK_SPACE) {
        board.placeTile(' ', xSelection, ySelection);
        if (xSelection > 0 && typingDirection == Direction.HORIZONTAL) {
          xSelection--;
        } else if (ySelection > 0 && typingDirection == Direction.VERTICAL) {
          ySelection--;
        }
      }
      repaint();
    }
  }

  private class SelectTileMouseListener extends MouseAdapter {
    @Override
    public void mousePressed(MouseEvent e) {
      typingDirection = null;
      requestFocus(); // Focus on click

      int x = e.getX();
      int y = e.getY();
      int borderWidth = OUTLINE_WEIGHT + SQUARE_SPACING / 2;
      if (borderWidth <= x
          && x < BOARD_SIZE - borderWidth
          && borderWidth <= y
          && y < BOARD_SIZE - borderWidth) {
        // If the click is in the board
        xSelection = (x - borderWidth) / SQUARE_OFFSET;
        ySelection = (y - borderWidth) / SQUARE_OFFSET;

        // Right clicking inverts the case to identify a tile as blank
        if (e.getButton() == MouseEvent.BUTTON3) {
          char oldChar = board.getBoard()[ySelection][xSelection];
          if (Character.isUpperCase(oldChar)) {
            board.getBoard()[ySelection][xSelection] = Character.toLowerCase(oldChar);
          } else {
            board.getBoard()[ySelection][xSelection] = Character.toUpperCase(oldChar);
          }
        }
      }
      getParent().repaint(); // Repaint to update selection boxes
    }
  }
}
