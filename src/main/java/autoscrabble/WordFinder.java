package autoscrabble;

import autoscrabble.word.Line;
import autoscrabble.word.LocatedWord;
import autoscrabble.word.Word1D;

import java.util.*;
import java.util.stream.Collectors;

public class WordFinder {
  private final List<DictEntry> dictionary;
  private final List<String> dictionaryWords;

  public WordFinder(String[] dictionary) {
    this.dictionary = Arrays.stream(dictionary).map(DictEntry::new).collect(Collectors.toList());
    this.dictionaryWords = List.of(dictionary);
  }

  public static int[] getLetterFrequency(char[] string) {
    int[] frequency = new int[DictEntry.ALPHABET.length];
    for (char c : string) {
      if (Character.isAlphabetic(c) && Character.isUpperCase(c)) {
        frequency[Character.getNumericValue(c) - 10]++;
      }
    }
    return frequency;
  }

  public boolean isWord(String s) {
    // The binary search is >= 0 if the key is found
    return Collections.binarySearch(dictionaryWords, s.toUpperCase()) >= 0;
  }

  public List<LocatedWord> getWords(Board board, Line line, char[] rack) {
    int blankCount = (int) String.valueOf(rack).chars().filter(c -> c == '_').count();
    var occupiedTiles = board.getOccupiedTiles();
    // Counting max possible length of words, possible future sorting???

    String lineUpper = line.string.toUpperCase();
    var rackMask = DictEntry.createAlphabetMask(String.valueOf(rack));
    var lineMask = DictEntry.createAlphabetMask(lineUpper);
    boolean firstMove = !board.isGameStarted();
    int[] rackFrequency = getLetterFrequency(rack);
    boolean lineHasBlanks = !line.string.equals(lineUpper);
    return dictionary.stream()
        .parallel()
        // Preliminarily filtering out words that will always fail to save time TODO measure speedup
        .filter(entry -> preliminaryFilter(entry, blankCount, rackMask, lineMask, firstMove))
        // Get all words that could fit on the given line
        .map(e -> getWords(line, e.getWord(), lineUpper, firstMove, rackFrequency, blankCount))
        .flatMap(List::stream) // Merge
        // Add blanks from the line/rack and position the word on the board
        .map(word -> addBlanks(line, occupiedTiles, word, lineHasBlanks))
        .collect(Collectors.toList());
  }

  private LocatedWord addBlanks(
      Line line, boolean[][] occupiedTiles, Word1D word, boolean lineHasBlank) {

    LocatedWord lWord;
    if (lineHasBlank || word.blanksUsed > 0) {
      // If there any blanks involved we must copy any existing board tiles to the word to catch
      // any blanks on the board, and position the blank tile(s) from the rack
      boolean[] letterAlreadyPlaced = new boolean[word.length];
      int[] letterMultiplier = new int[word.length];
      char[] wordArr = word.string.toCharArray();

      // Copy potentially blank tile from the board
      if (line.direction == Direction.HORIZONTAL) {
        lWord = new LocatedWord(word, word.startIndex, line.index, line.direction);
        for (int i = lWord.x; i < lWord.x + word.length; i++) {
          if (occupiedTiles[lWord.y][i]) {
            letterAlreadyPlaced[i - lWord.x] = occupiedTiles[lWord.y][i];
            wordArr[i - lWord.x] = line.string.charAt(i);
          }
          letterMultiplier[i - lWord.x] = Rater.LETTER_BONUSES[lWord.y][i];
        }
      } else { // Vertical
        lWord = new LocatedWord(word, line.index, word.startIndex, line.direction);
        for (int i = lWord.y; i < lWord.y + word.length; i++) {
          if (occupiedTiles[i][lWord.x]) {
            letterAlreadyPlaced[i - lWord.y] = true;
            wordArr[i - lWord.y] = line.string.charAt(i);
          }
          letterMultiplier[i - lWord.y] = Rater.LETTER_BONUSES[i][lWord.x];
        }
      }

      if (word.blanksUsed > 0) {
        // If there are blanks, put the blanks in the optimal position to avoid letter multipliers
        for (int i = 0; i < word.blankRequirements.length; i++) {
          var blankIndexList = new ArrayList<Integer>();
          for (int j = 0; j < word.length; j++) {
            // If a blank is required for this letter
            int charI = word.string.charAt(j) - 65;
            if (i == charI && !letterAlreadyPlaced[j] && word.blankRequirements[charI] > 0) {
              blankIndexList.add(j);
            }
          }
          // Sort in ascending order by the letter multiplier at that location
          blankIndexList.sort(Comparator.comparingInt(j -> letterMultiplier[j]));
          // Set the characters in the word to be blank-using
          for (int j = 0; j < word.blankRequirements[i]; j++) {
            wordArr[blankIndexList.get(j)] = Character.toLowerCase(wordArr[blankIndexList.get(j)]);
          }
        }
      }
      lWord.string = String.valueOf(wordArr);
    } else {
      if (line.direction == Direction.HORIZONTAL) {
        lWord = new LocatedWord(word, word.startIndex, line.index, line.direction);
      } else {
        lWord = new LocatedWord(word, line.index, word.startIndex, line.direction);
      }
    }
    return lWord;
  }

  private boolean preliminaryFilter(
      DictEntry entry, int blankCount, int rackMask, int lineMask, boolean firstMove) {
    // Bitmasks where the nth bit is 1 if the string contains the nth letter of the alphabet
    // First condition: the dictionary word and the line must have at least 1 matching letter
    // (skip if on the first move)
    // Second: enough letters in the word must appear in the rack or line. Usually all of them,
    // less if the rack has blank tiles.
    // (rackMask | lineMask) is the mask for the letters in the rack and line combined.
    // we then get all letters that are in the word and not in the rack or line, and count them
    return ((entry.alphabetMask & lineMask) != 0 || firstMove)
        && Integer.bitCount(entry.alphabetMask & ~(rackMask | lineMask)) <= blankCount;
  }

  private List<Word1D> getWords(
      Line line,
      String entry,
      String lineUpper,
      boolean firstMove,
      int[] rackLetterFrequency,
      int blankCount) {
    var list = new ArrayList<Word1D>();
    for (int j = 0; j < line.string.length() - entry.length() + 1; j++) {
      if (j != 0 && line.string.charAt(j - 1) != ' '
          || j + entry.length() != line.string.length()
              && line.string.charAt(j + entry.length()) != ' ') {
        // Check to see if the ends of the word are blocked
        continue;
      }

      boolean collision = false;
      int connectingLetters = 0;
      int[] placedLetterFrequency = new int[DictEntry.ALPHABET.length];
      int blanksUsed = 0;
      for (int k = j; k < entry.length() + j; k++) {
        if (line.string.charAt(k) != ' ') {
          // If this letter is the same as a letter on the board
          if (lineUpper.charAt(k) != entry.charAt(k - j)) {
            collision = true;
            break;
          } else {
            connectingLetters++;
          }
        } else {
          // If the letter must come from the rack
          int letterIndex = Character.getNumericValue(entry.charAt(k - j)) - 10;
          placedLetterFrequency[letterIndex]++;
          if (placedLetterFrequency[letterIndex] > rackLetterFrequency[letterIndex]) {
            // If we are out of letters check if we have a blank, or fail
            if (blanksUsed < blankCount) {
              blanksUsed++;
            } else {
              collision = true;
              break;
            }
          }
        }
        if (firstMove && k == Board.SIZE / 2) {
          // If on the first move the centre counts as a connecting letter
          connectingLetters++;
        }
      }
      // Add if there was no collision, 0 < letters placed < len, and the rack has enough letters
      if (!collision
          && connectingLetters != entry.length()
          && connectingLetters > 0
          && entry.length() - connectingLetters <= Board.RACK_CAPACITY) {
        int[] blankRequirements = null;
        if (blanksUsed > 0) {
          // If we used blanks, mark the word with which letter(s) they were needed for and how many
          blankRequirements = new int[DictEntry.ALPHABET.length];
          for (int i = 0; i < placedLetterFrequency.length; i++) {
            if (placedLetterFrequency[i] > rackLetterFrequency[i]) {
              blankRequirements[i] = placedLetterFrequency[i] - rackLetterFrequency[i];
              placedLetterFrequency[i] -=
                  blankRequirements[i]; // If we use a blank don't record it here
            } else {
              blankRequirements[i] = 0;
            }
          }
        }
        Word1D word = new Word1D(entry, j, blankRequirements, blanksUsed, placedLetterFrequency);
        list.add(word);
      }
    }
    return list;
  }
}
