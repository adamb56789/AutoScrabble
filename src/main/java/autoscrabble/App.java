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
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

public class App {
  public static final Color BACKGROUND_COLOUR = Color.decode("#F3F3F3");
  public static final int PLAYER_COUNT = 2;
  public static final int ITERATIONS = 12;
  public static final int[] TILE_DISTRIBUTION =
      new int[] {9, 2, 2, 4, 12, 2, 3, 2, 9, 1, 1, 4, 2, 6, 8, 2, 1, 6, 4, 6, 4, 2, 2, 1, 2, 1};
  public static final int MAX_BLANKS = 2;
  static List<int[]> ratings = new ArrayList<>();
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
    autoButton.addActionListener(new multiAuto(panel, wordFinder));
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
    var rng = new Random(seed);
    int smartPlayer = seed % PLAYER_COUNT;
    var racks = new ArrayList<char[]>();
    var bag = new ArrayList<Character>();
    for (int i = 0; i < 26; i++) {
      for (int j = 0; j < TILE_DISTRIBUTION[i]; j++) {
        bag.add((char) (65 + i));
      }
    }
    bag.add('_');
    bag.add('_');

    for (int i = 0; i < PLAYER_COUNT; i++) {
      char[] rack = new char[Board.RACK_CAPACITY];
      for (int j = 0; j < Board.RACK_CAPACITY; j++) {
        rack[j] = drawTile(rng, bag);
      }
      racks.add(rack);
    }

    int[] rating = new int[PLAYER_COUNT + 1];
    rating[PLAYER_COUNT] = smartPlayer;
    int moveCount = 0;
    while (true) {
      int player = moveCount % PLAYER_COUNT;
      board.setRack(racks.get(player));
      RatedWord word;
      if (player == smartPlayer) {
        word = board.findBestWordSmart();
      } else {
        word = board.findBestWord();
      }
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
      rating[player] += word.getRating();
      moveCount++;
      for (int i = 0; i < board.getRack().length; i++) {
        if (board.getRack()[i] == ' ') {
          if (bag.size() > 0) {
            board.getRack()[i] = drawTile(rng, bag);
          }
        }
      }
      gui.repaint();
    }
    App.ratings.add(rating);
    //    gui.repaint();
  }

  private static char drawTile(Random rng, ArrayList<Character> bag) {
    int randomIndex = rng.nextInt(bag.size());
    var character = bag.get(randomIndex);
    bag.remove(randomIndex);
    return character;
  }

  private static class multiAuto extends AbstractAction {
    private final JPanel panel;
    private final WordFinder wordFinder;

    public multiAuto(JPanel panel, WordFinder wordFinder) {
      this.panel = panel;
      this.wordFinder = wordFinder;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      new Thread(
              () -> {
                ratings = new ArrayList<>();
                IntStream.range(0, ITERATIONS)
                    .parallel()
                    .forEach(
                        i ->
                            playAutomatically(
                                panel, ITERATIONS == 1 ? board : new Board(wordFinder), i + 1));
                double[] averages = new double[2];
                for (int[] rating : ratings) {
                  for (int i = 0; i < PLAYER_COUNT; i++) {
                    if (i == rating[PLAYER_COUNT]) {
                      averages[0] += (double) rating[i] / ITERATIONS;
                    } else {
                      averages[1] += (double) rating[i] / (ITERATIONS * (PLAYER_COUNT - 1));
                    }
                  }
                }
                System.out.printf(
                    "%.2f %.2f %.2f%n", averages[0], averages[1], averages[0] - averages[1]);
              })
          .start();
    }
  }
}
