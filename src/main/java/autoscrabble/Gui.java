package autoscrabble;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.IOException;

public class Gui extends JComponent implements MouseListener, MouseMotionListener {
  private final Board board;
  boolean foobar = true;
  int x, y;
  private Image tripleWord;
  private Image doubleWord;
  private Image tripleLetter;
  private Image doubleLetter;
  private Image start;
  private Image tile;
  private Image tileBackground;

  public Gui(Board board) {
    super();
    this.board = board;
    try {
      tripleWord = ImageIO.read(getClass().getResource("/images/tripleWord.png"));
      doubleWord = ImageIO.read(getClass().getResource("/images/doubleWord.png"));
      tripleLetter = ImageIO.read(getClass().getResource("/images/tripleLetter.png"));
      doubleLetter = ImageIO.read(getClass().getResource("/images/doubleLetter.png"));
      start = ImageIO.read(getClass().getResource("/images/start.png"));
      tile = ImageIO.read(getClass().getResource("/images/tile.png"));
      tileBackground = ImageIO.read(getClass().getResource("/images/tileBackground.png"));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void paintComponent(Graphics g) {
    if (foobar) {
      addMouseListener(this);
      addMouseMotionListener(this);
      foobar = false;
    }
    super.paintComponent(g);
    Graphics2D g2 = (Graphics2D) g;
    g2.setRenderingHint(
        RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

    g.setColor(Color.decode("#C8C2A8"));
    g.fillRect(0, 0, 672, 672);

    g.drawImage(tileBackground, 690, 16, this);

    Font large = new Font("Arial", Font.PLAIN, 30);
    Font medium = new Font("Arial", Font.PLAIN, 20);
    Font small = new Font("Arial", Font.PLAIN, 12);
    g2.setFont(medium);
    g2.setColor(Color.black);
    g2.drawString(board.getUserMessage(), 15, 710);
    g2.setFont(small);
    for (int i = 0; i < 15; i++) {
      g2.drawString((char) (i + 97) + "", 25 + i * 44, 682);
      g2.drawString((i + 1) + "", 673, (15 - i) * 44 - 11);
    }
    g2.setFont(large);

    // draw the lines of the board
    g.setColor(Color.decode("#ECE8D9"));
    for (int i = 0; i < 16; i++) {
      g.fillRect(i * 44 + 4, 4, 4, 664);
      g.fillRect(4, i * 44 + 4, 664, 4);
    }
    g.setColor(Color.black);
    g.fillRect(0, 0, 4, 668);
    g.fillRect(15 * 44 + 8, 0, 4, 672);
    g.fillRect(0, 0, 668, 4);
    g.fillRect(0, 15 * 44 + 8, 672, 4);

    // draw the images of the board
    for (int i = 0; i < 15; i++) {
      for (int j = 0; j < 15; j++) {
        if (i == 7 && j == 7) {
          g.drawImage(start, j * 44 + 4, i * 44 + 4, this);
        } else {
          switch (Rater.BONUSES[j][i]) {
            case 'W' -> g.drawImage(tripleWord, j * 44 + 4, i * 44 + 4, this);
            case 'w' -> g.drawImage(doubleWord, j * 44 + 4, i * 44 + 4, this);
            case 'L' -> g.drawImage(tripleLetter, j * 44 + 4, i * 44 + 4, this);
            case 'l' -> g.drawImage(doubleLetter, j * 44 + 4, i * 44 + 4, this);
          }
        }
      }
    }

    // Draw the tiles
    for (int i = 0; i < 15; i++) {
      for (int j = 0; j < 15; j++) {
        if (board.getBoard()[j][i] != ' ') {
          g.drawImage(tile, i * 44 + 8, j * 44 + 8, this);
          g2.setFont(large);
          if (Character.isUpperCase(board.getBoard()[j][i])) {
            g2.drawString(board.getBoard()[j][i] + "", i * 44 + 12, j * 44 + 40);
          } else if (Character.isLowerCase(board.getBoard()[j][i])) {
            g2.setFont(small);
            g2.drawString("(" + board.getBoard()[j][i] + ")", i * 44 + 18, j * 44 + 32);
            g2.drawString("0", i * 44 + 37, j * 44 + 42);
          }
          g2.setFont(small);
          int nx = 37;
          if (board.getBoard()[j][i] == 'Q' || board.getBoard()[j][i] == 'Z') {
            nx = 33;
          }
          int ny = 42;
          switch (board.getBoard()[j][i]) {
            case 'A':
            case 'E':
            case 'I':
            case 'L':
            case 'N':
            case 'O':
            case 'R':
            case 'S':
            case 'T':
            case 'U':
              g2.drawString("1", i * 44 + nx, j * 44 + ny);
              break;
            case 'D':
            case 'G':
              g2.drawString("2", i * 44 + nx, j * 44 + ny);
              break;
            case 'B':
            case 'C':
            case 'M':
            case 'P':
              g2.drawString("3", i * 44 + nx, j * 44 + ny);
              break;
            case 'F':
            case 'H':
            case 'V':
            case 'W':
            case 'Y':
              g2.drawString("4", i * 44 + nx, j * 44 + ny);
              break;
            case 'K':
              g2.drawString("5", i * 44 + nx, j * 44 + ny);
              break;
            case 'J':
            case 'X':
              g2.drawString("8", i * 44 + nx, j * 44 + ny);
              break;
            case 'Q':
            case 'Z':
              g2.drawString("10", i * 44 + nx, j * 44 + ny);
              break;
            default:
          }
        }
      }
    }

    // Draw the hand
    int sp = 6;
    int so = 0;
    for (int i = 0; i < board.getHand().length; i++) {
      if (!"".equals(board.getHand()[i])) {
        try {
          if ("_".equals(board.getHand()[i]) || Character.isAlphabetic(board.getHand()[i].charAt(0))) {
            g.drawImage(tile, 700 - sp, 20 + i * 44 - so, this);
            g2.setFont(large);
            if (Character.isUpperCase(board.getHand()[i].charAt(0))) {
              g2.drawString(board.getHand()[i] + "", 704 - sp, i * 44 + 54 - so);
            }
            g2.setFont(small);
            int nx = 29 - sp;
            if ("Q".equals(board.getHand()[i]) || "Z".equals(board.getHand()[i])) {
              nx = 25 - sp;
            }
            int ny = 42 + 12 - so;
            switch (board.getHand()[i].charAt(0)) {
              case 'A':
              case 'E':
              case 'I':
              case 'U':
              case 'T':
              case 'S':
              case 'R':
              case 'O':
              case 'N':
              case 'L':
                g2.drawString("1", 700 + nx, i * 44 + ny);
                break;
              case 'D':
              case 'G':
                g2.drawString("2", 700 + nx, i * 44 + ny);
                break;
              case 'B':
              case 'C':
              case 'P':
              case 'M':
                g2.drawString("3", 700 + nx, i * 44 + ny);
                break;
              case 'F':
              case 'H':
              case 'Y':
              case 'W':
              case 'V':
                g2.drawString("4", 700 + nx, i * 44 + ny);
                break;
              case 'K':
                g2.drawString("5", 700 + nx, i * 44 + ny);
                break;
              case 'J':
              case 'X':
                g2.drawString("8", 700 + nx, i * 44 + ny);
                break;
              case 'Q':
              case 'Z':
                g2.drawString("10", 700 + nx, i * 44 + ny);
                break;
              default:
            }
          }
        } catch (Exception ignored) {
        }
      }
    }

    // Draw the selection
    var xSelection = board.getxSelection();
    var ySelection = board.getySelection();
    if (xSelection != -1) {
      g.setColor(Color.red);
      g.fillRect(xSelection * 44 + 6, ySelection * 44 + 6, 4, 44);
      g.fillRect(xSelection * 44 + 46, ySelection * 44 + 6, 4, 44);
      g.fillRect(xSelection * 44 + 6, ySelection * 44 + 6, 44, 4);
      g.fillRect(xSelection * 44 + 6, ySelection * 44 + 46, 44, 4);
    }
    var handSelection = board.getHandSelection();
    if (handSelection != -1) {
      g.setColor(Color.red);
      g.fillRect(698 - sp, 18 + handSelection * 44, 4, 44);
      g.fillRect(698 + 40 - sp, 18 + handSelection * 44, 4, 44);
      g.fillRect(698 - sp, 18 + handSelection * 44, 44, 4);
      g.fillRect(698 - sp, 58 + handSelection * 44, 44, 4);
    }
  }

  @Override
  public void mouseClicked(MouseEvent e) {}

  @Override
  public void mousePressed(MouseEvent e) {
    int sp = 6;
    var xSelection = (x - 6) / 44;
    var ySelection  = (y - 6) / 44;
    if (x > 665 || y > 665) {
      xSelection = -1;
      if (x > 698 - sp && x < 742 - sp && y > 18 && y < 326) {
        board.setHandSelection((y - 18) / 44);
        ySelection = -1;
      } else {
        board.setHandSelection(-1);
      }
    } else {
      board.setHandSelection(-1);
    }
    if (e.getButton() == 3 && xSelection != -1) {
      if (Character.isUpperCase(board.getBoard()[ySelection][xSelection])) {
        board.getBoard()[ySelection][xSelection] =
            Character.toLowerCase(board.getBoard()[ySelection][xSelection]);
      } else {
        board.getBoard()[ySelection][xSelection] =
            Character.toUpperCase(board.getBoard()[ySelection][xSelection]);
      }
    }
    board.setSelection(xSelection, ySelection);
    repaint();
  }

  @Override
  public void mouseReleased(MouseEvent e) {}

  @Override
  public void mouseEntered(MouseEvent e) {}

  @Override
  public void mouseExited(MouseEvent e) {}

  @Override
  public void mouseDragged(MouseEvent e) {}

  @Override
  public void mouseMoved(MouseEvent e) {
    x = e.getX();
    y = e.getY();
  }
}
