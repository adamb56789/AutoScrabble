package autoscrabble;

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

  public boolean isWord(String s) {
    // The binary search is >= 0 if the key is found
    return Collections.binarySearch(dictionaryWords, s.toUpperCase()) >= 0;
  }

  private int[] getLetterFrequency(char[] rack) {
    int[] rackLetterFrequency = new int[DictEntry.ALPHABET.length];
    for (char c : rack) {
      if (Character.isAlphabetic(c)) {
        rackLetterFrequency[Character.getNumericValue(c) - 10]++;
      }
    }
    return rackLetterFrequency;
  }

  public List<LocatedWord> getWords(
      Board board, String line, char[] rack, Direction direction, int index) {
    int blankCount = (int) String.valueOf(rack).chars().filter(c -> c == '_').count();
    var occupiedTiles = board.getOccupiedTiles();
    // Counting max possible length of words, possible future sorting???

    var rackMask = DictEntry.createAlphabetMask(String.valueOf(rack));
    var lineMask = DictEntry.createAlphabetMask(line.toUpperCase());
    boolean firstMove = !board.isGameStarted();
    return dictionary.stream()
        .parallel()
        // Preliminarily filtering out words that will always fail to save time  TODO x speedup
        .filter(entry -> preliminaryFilter(entry, blankCount, rackMask, lineMask, firstMove))
        // Get all words that could fit on the given line
        .map(entry -> getWordsFitInLine(line, entry.getWord(), line.toUpperCase(), firstMove))
        .flatMap(List::stream) // Merge words from each line
        // Check that the word is not directly touching another
        .filter(word -> endsAreFree(word, line))
        // Filter to words that can be placed with available tiles
        .filter(word -> rackHasEnoughLetters(word, line, getLetterFrequency(rack), blankCount))
        // Add blanks from the line/rack and position the word on the board
        .map(word -> situateAndAddBlanks(line, direction, index, occupiedTiles, word))
        .collect(Collectors.toList());
  }

  private LocatedWord situateAndAddBlanks(
      String line, Direction direction, int index, boolean[][] occupiedTiles, Word1D word) {

    LocatedWord lWord;
    boolean lineContainsBlank = line.indexOf(' ') != -1;
    if (lineContainsBlank || word.blanksNeeded) {
      // If there any blanks involved we must copy any existing board tiles to the word to catch
      // any blanks on the board, and position the blank tile(s) from the rack
      boolean[] letterAlreadyPlaced = new boolean[word.length];
      int[] letterMultiplier = new int[word.length];
      char[] wordArr = word.string.toCharArray();

      if (direction == Direction.HORIZONTAL) {
        lWord = new LocatedWord(word, word.startIndex, index, direction);
        for (int i = lWord.x; i < lWord.x + word.length; i++) {
          if (occupiedTiles[lWord.y][i]) {
            letterAlreadyPlaced[i - lWord.x] = occupiedTiles[lWord.y][i];
            wordArr[i - lWord.x] = line.charAt(i);
          }
          letterMultiplier[i - lWord.x] = Rater.LETTER_BONUSES[lWord.y][i];
        }
      } else { // Vertical
        lWord = new LocatedWord(word, index, word.startIndex, direction);
        for (int i = lWord.y; i < lWord.y + word.length; i++) {
          if (occupiedTiles[i][lWord.x]) {
            letterAlreadyPlaced[i - lWord.y] = true;
            wordArr[i - lWord.y] = line.charAt(i);
          }
          letterMultiplier[i - lWord.y] = Rater.LETTER_BONUSES[i][lWord.x];
        }
      }

      if (word.blanksNeeded) {
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
      if (direction == Direction.HORIZONTAL) {
        lWord = new LocatedWord(word, word.startIndex, index, direction);
      } else {
        lWord = new LocatedWord(word, index, word.startIndex, direction);
      }
    }
    return lWord;
  }

  private boolean endsAreFree(Word1D word, String line) {
    return (word.startIndex == 0 || line.charAt(word.startIndex - 1) == ' ')
        && (word.startIndex + word.length == line.length()
            || line.charAt(word.startIndex + word.length) == ' ');
  }

  private boolean preliminaryFilter(DictEntry entry, int blankCount, int rackMask, int lineMask, boolean firstMove) {
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

  private List<Word1D> getWordsFitInLine(
      String line, String entry, String upperCaseLine, boolean firstMove) {
    var list = new ArrayList<Word1D>();
    for (int j = 0; j < line.length() - entry.length() + 1; j++) {
      boolean collision = false;
      boolean connectingLetterExists = false;
      for (int k = j; k < entry.length() + j; k++) {
        // We need to match with at least 1 letter already on the board
        if (line.charAt(k) != ' ') {
          connectingLetterExists = true;
        }
        if (firstMove && k == Board.SIZE / 2) {
          connectingLetterExists = true;
        }
        // Check for collisions with letters on the board
        if (upperCaseLine.charAt(k) != entry.charAt(k - j) && line.charAt(k) != ' ') {
          collision = true;
          break;
        }
      }
      if (!collision && (connectingLetterExists)) {
        list.add(new Word1D(entry, j));
      }
    }
    return list;
  }

  private boolean rackHasEnoughLetters(
      Word1D word, String line, int[] rackLetterFrequency, int blankCount) {
    int[] remainingLetters = rackLetterFrequency.clone();
    boolean placedALetter = false; // If we do not place a letter then the word is already there
    boolean ranOutOfLetters = false;
    int blanksRemaining = blankCount;
    for (int j = 0; j < word.string.length(); j++) { // Iterate through the word
      // If the corresponding position on the line is empty we must fill it from the rack
      if (line.charAt(j + word.startIndex) == ' ') {
        placedALetter = true;
        // Decrement the count of the letter
        int letterIndex = Character.getNumericValue(word.string.charAt(j)) - 10;
        remainingLetters[letterIndex]--;
        if (remainingLetters[letterIndex] < 0) {
          // If we are out of letters check if we have a blank, or fail
          if (blanksRemaining > 0) {
            blanksRemaining--;
          } else {
            ranOutOfLetters = true;
            break;
          }
        }
      }
    }
    boolean validWord = !ranOutOfLetters && placedALetter;
    if (validWord && blankCount - blanksRemaining > 0) {
      // If we used blanks, mark the word with which letter(s) they were needed for and how many
      for (int i = 0; i < remainingLetters.length; i++) {
        if (remainingLetters[i] < 0) {
          remainingLetters[i] = -remainingLetters[i];
        } else {
          remainingLetters[i] = 0;
        }
      }
      word.setBlankRequirements(remainingLetters);
    }
    return validWord;
  }
}
