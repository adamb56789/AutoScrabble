package autoscrabble;

import autoscrabble.userinterface.BoardComp;
import autoscrabble.userinterface.RackComp;
import autoscrabble.word.RatedWord;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Random;

public class App {
  public static final Color BACKGROUND_COLOUR = Color.decode("#F3F3F3");
  public static final int PLAYER_COUNT = 4;
  public static final int[] TILE_DISTRIBUTION =
      new int[] {9, 2, 2, 4, 12, 2, 3, 2, 9, 1, 1, 4, 2, 6, 8, 2, 1, 6, 4, 6, 4, 2, 2, 1, 2, 1};
  public static final int MAX_BLANKS = 2;
  private static Board board;

  public static void main(String[] args) {
    // Load the dictionary and prepare the word finder
    var inputStream = App.class.getClassLoader().getResourceAsStream("Dictionary.txt");
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
    var wordFinder = new WordFinder(dictionary);
    board = new Board(wordFinder);

    JPanel panel = new JPanel();
    panel.setLayout(new GridBagLayout());
    GridBagConstraints gbc;
    gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    var boardComp = new BoardComp(board);
    panel.add(boardComp, gbc);

    gbc = new GridBagConstraints();
    gbc.gridx = 1;
    gbc.gridy = 0;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.insets = new Insets(16, 0, 0, 0);
    gbc.anchor = GridBagConstraints.NORTH;
    var rackComp = new RackComp(board);
    panel.add(rackComp, gbc);

    var fastButton = new JButton();
    fastButton.setFocusable(false);
    fastButton.setText("Fast");
    fastButton.addActionListener(boardComp.getFindWordFast());
    gbc = new GridBagConstraints();
    gbc.gridx = 1;
    gbc.gridy = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.anchor = GridBagConstraints.SOUTH;
    panel.add(fastButton, gbc);

    var resetButton = new JButton();
    resetButton.setFocusable(false);
    resetButton.setText("Reset");
    resetButton.addActionListener(
        new AbstractAction() {
          @Override
          public void actionPerformed(ActionEvent e) {
            System.out.println("Reset");
            board.initialise();
            rackComp.resetSelection();
            rackComp.requestFocus();
            panel.repaint();
          }
        });
    gbc = new GridBagConstraints();
    gbc.gridx = 1;
    gbc.gridy = 2;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.anchor = GridBagConstraints.SOUTH;
    panel.add(resetButton, gbc);

    var autoButton = new JButton();
    autoButton.setFocusable(false);
    autoButton.setText("Auto");
    autoButton.addActionListener(new autoModeAction(panel));
    gbc = new GridBagConstraints();
    gbc.gridx = 1;
    gbc.gridy = 3;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.anchor = GridBagConstraints.SOUTH;
    panel.add(autoButton, gbc);

    var scrollPane = new JScrollPane(panel);

    var frame = new JFrame();
    frame.add(scrollPane);
    frame.pack();
    rackComp.requestFocus(); // Start focus on the rack
    frame.setLocationRelativeTo(null);
    frame.setTitle("AutoScrabble");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setVisible(true);
    frame.getContentPane().setBackground(BACKGROUND_COLOUR);
  }

  private static void playAutomatically(JPanel gui, Board board, int seed) {
    var playedWords = new ArrayList<RatedWord>();
    var rng = new Random(seed);
    var bag = new ArrayList<Character>();
    for (int i = 0; i < 26; i++) {
      for (int j = 0; j < TILE_DISTRIBUTION[i]; j++) {
        bag.add((char) (65 + i));
      }
    }
    bag.add('_');
    bag.add('_');

    for (int i = 0; i < Board.RACK_CAPACITY; i++) {
      board.getRack()[i] = pickTile(rng, bag);
    }

    while (true) {
      RatedWord word = board.findBestWord();
      if (word == null) {
        break;
      }
      board.setUserMessage(
          String.format(
              "Found %s at (%c%d) %s, scoring %s, ",
              word.string,
              ((char) (word.x + 97)),
              (15 - word.y),
              word.isHorizontal ? "horizontally" : "vertically",
              word.getRating()));
      System.out.println(word);
      board.makeMove(word);
      playedWords.add(word);
      for (int i = 0; i < board.getRack().length; i++) {
        if (board.getRack()[i] == ' ') {
          if (bag.size() > 0) {
            board.getRack()[i] = pickTile(rng, bag);
          }
        }
      }
      gui.repaint();
    }
    System.out.println("_____________________________");
    System.out.println("RESULTS");
    int totalScore = 0;
    for (var word : playedWords) {
      System.out.println(word);
      totalScore += word.getRating();
    }
    System.out.println("Total score: " + totalScore);
    gui.repaint();
  }

  private static char pickTile(Random rng, ArrayList<Character> bag) {
    int randomIndex = rng.nextInt(bag.size());
    var character = bag.get(randomIndex);
    bag.remove(randomIndex);
    return character;
  }

  private static class autoModeAction extends AbstractAction {
    private final JPanel panel;

    public autoModeAction(JPanel panel) {
      this.panel = panel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      new Thread(() -> playAutomatically(panel, board, 0)).start();
    }
  }
}
