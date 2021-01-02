package autoscrabble;

import autoscrabble.userinterface.BoardComp;
import autoscrabble.userinterface.RackComp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Random;

public class App {
  public static final Color BACKGROUND_COLOUR = Color.decode("#F3F3F3");
  static Random random = new Random(0);
  private static Board board;

  public static void main(String[] args) {
    board = new Board();
    RackComp rack = new RackComp(board);

    JPanel panel = new JPanel();
    panel.setLayout(new GridBagLayout());
    GridBagConstraints gbc;
    gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    panel.add(new BoardComp(board, rack), gbc);

    gbc = new GridBagConstraints();
    gbc.gridx = 1;
    gbc.gridy = 0;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.insets = new Insets(16, 0, 0, 0);
    gbc.anchor = GridBagConstraints.NORTH;
    panel.add(rack, gbc);

    var autoButton = new JButton();
    autoButton.setText("Auto");
    autoButton.addActionListener(
        new AbstractAction() {
          @Override
          public void actionPerformed(ActionEvent e) {
            (new Thread(() -> playAutomatically(panel))).start();
          }
        });
    gbc = new GridBagConstraints();
    gbc.gridx = 1;
    gbc.gridy = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.anchor = GridBagConstraints.SOUTH;
    panel.add(autoButton, gbc);

    var scrollPane = new JScrollPane(panel);

    var frame = new JFrame();
    frame.add(scrollPane);
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setTitle("AutoScrabble");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setVisible(true);
    frame.getContentPane().setBackground(BACKGROUND_COLOUR);
  }

  private static void playAutomatically(JPanel gui) {
    var tileDistribution =
        new int[] {9, 2, 2, 4, 12, 2, 3, 2, 9, 1, 1, 4, 2, 6, 8, 2, 1, 6, 4, 6, 4, 2, 2, 1, 2, 1};
    var bag = new ArrayList<Character>();
    for (int i = 0; i < 26; i++) {
      for (int j = 0; j < tileDistribution[i]; j++) {
        bag.add((char) (65 + i));
      }
    }

    for (int i = 0; i < 7; i++) {
      board.placeInRack(drawTile(bag), i);
    }

    board.placeTile('e', 8, 7);

    double totalRating = 0;
    int moveCount = 0;
    long start = System.nanoTime();
    while (true) {
      var word = board.findBestWord();
      if (word == null) {
        break;
      }
      board.makeMove(word);
      totalRating += word.getRating();
      moveCount++;
      for (int i = 0; i < board.getRack().length; i++) {
        if (board.getRack()[i] == ' ') {
          if (bag.size() > 0) {
            board.getRack()[i] = drawTile(bag);
          }
        }
      }
      gui.repaint();
    }
    System.out.printf(
        "Moves: %d%nTotal score: %.0f%nAverage: %.1f%nTime: %.1f ms%n",
        moveCount, totalRating, totalRating / moveCount, (System.nanoTime() - start) / 1000000.0);
    gui.repaint();
  }

  private static char drawTile(ArrayList<Character> bag) {
    int randomIndex = random.nextInt(bag.size());
    var character = bag.get(randomIndex);
    bag.remove(randomIndex);
    return character;
  }
}
