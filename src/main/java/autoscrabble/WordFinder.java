package autoscrabble;

import autoscrabble.word.Word1D;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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

  public List<Word1D> getWords(String line, char[] rack) {
    int blankCount = (int) String.valueOf(rack).chars().filter(c -> c == '_').count();
    // Counting max possible length of words, possible future sorting???

    var rackMask = DictEntry.createAlphabetMask(String.valueOf(rack));
    var lineMask = DictEntry.createAlphabetMask(line);
    // Faster to operate on char arrays
    char[] lineArr = line.toCharArray();
    char[] lineUpperArr = line.toUpperCase().toCharArray();

    List<Word1D> list = new ArrayList<>();
    for (DictEntry entry : dictionary) {
      // Preliminarily filtering out words that will always fail to save time
      // Bitmasks where the nth bit is 1 if the string contains the nth letter of the alphabet
      // First condition: the dictionary word and the line must have at least 1 matching letter
      // Second: enough letters in the word must appear in the rack or line. Usually all of them,
      // less if the rack has blank tiles.
      // (rackMask | lineMask) is the mask for the letters in the rack and line combined.
      // we then get all letters that are in the word and not in the rack or line, and count them
      if ((entry.getAlphabetMask() & lineMask) != 0
          && Integer.bitCount(entry.getAlphabetMask() & ~(rackMask | lineMask)) <= blankCount) {

        // Get all words that could fit on the given line
        var wordsFitInLine = getWordsFitInLine(lineArr, entry.getChars(), lineUpperArr);

        // Filter to words that can be placed with available tiles
        for (Word1D word : wordsFitInLine) {
          if (rackHasEnoughLetters(word, lineArr, getLetterFrequency(rack), blankCount)) {
            list.add(word);
          }
        }
      }
    }
    return list;
  }

  private List<Word1D> getWordsFitInLine(char[] line, char[] entry, char[] upperCaseLine) {
    var list = new ArrayList<Word1D>();
    for (int j = 0; j < line.length - entry.length + 1; j++) {
      boolean collision = false;
      boolean connectingLetterExists = false;
      for (int k = j; k < entry.length + j; k++) {
        // We need to match with at least 1 letter already on the board
        if (line[k] != ' ') {
          connectingLetterExists = true;
        }
        // Check for collisions with letters on the board
        if (upperCaseLine[k] != entry[k - j] && line[k] != ' ') {
          collision = true;
          break;
        }
      }
      if (!collision && connectingLetterExists) {
        list.add(new Word1D(entry, j));
      }
    }
    return list;
  }

  private boolean rackHasEnoughLetters(
      Word1D word, char[] line, int[] rackLetterFrequency, int blankCount) {
    int[] remainingLetters = rackLetterFrequency.clone();
    boolean placedALetter = false; // If we do not place a letter then the word is already there
    boolean ranOutOfLetters = false;
    int blanksRemaining = blankCount;
    for (int j = 0; j < word.getChars().length; j++) { // Iterate through the word
      // If the corresponding position on the line is empty we must fill it from the rack
      if (line[j + word.getStartIndex()] == ' ') {
        placedALetter = true;
        // Decrement the count of the letter
        int letterIndex = Character.getNumericValue(word.getChars()[j]) - 10;
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
    return !ranOutOfLetters && placedALetter;
  }
}
