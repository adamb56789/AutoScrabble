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
    var lineMask = DictEntry.createAlphabetMask(line.toUpperCase());
    return dictionary.stream()
//        .parallel()
        // Preliminarily filtering out words that will always fail to save time ~25x speedup
        .filter(entry -> preliminaryFilter(entry, blankCount, rackMask, lineMask))
        // Get all words that could fit on the given line
        .map(entry -> getWordsFitInLine(line, entry.getWord(), line.toUpperCase()))
        .flatMap(List::stream) // Merge words from each line
        // Check that the word is not directly touching another
        .filter(word -> endsAreFree(word, line))
        // Filter to words that can be placed with available tiles
        .filter(word -> rackHasEnoughLetters(word, line, getLetterFrequency(rack), blankCount))
        .collect(Collectors.toList());
  }

  private boolean endsAreFree(Word1D word, String line) {
    return (word.startIndex == 0 || line.charAt(word.startIndex - 1) == ' ')
        && (word.startIndex + word.length == line.length()
            || line.charAt(word.startIndex + word.length) == ' ');
  }

  private boolean preliminaryFilter(DictEntry entry, int blankCount, int rackMask, int lineMask) {
    // Bitmasks where the nth bit is 1 if the string contains the nth letter of the alphabet
    // First condition: the dictionary word and the line must have at least 1 matching letter
    // Second: enough letters in the word must appear in the rack or line. Usually all of them,
    // less if the rack has blank tiles.
    // (rackMask | lineMask) is the mask for the letters in the rack and line combined.
    // we then get all letters that are in the word and not in the rack or line, and count them
    return (entry.alphabetMask & lineMask) != 0
        && Integer.bitCount(entry.alphabetMask & ~(rackMask | lineMask)) <= blankCount;
  }

  private List<Word1D> getWordsFitInLine(String line, String entry, String upperCaseLine) {
    var list = new ArrayList<Word1D>();
    for (int j = 0; j < line.length() - entry.length() + 1; j++) {
      boolean collision = false;
      boolean connectingLetterExists = false;
      for (int k = j; k < entry.length() + j; k++) {
        // We need to match with at least 1 letter already on the board
        if (line.charAt(k) != ' ') {
          connectingLetterExists = true;
        }
        // Check for collisions with letters on the board
        if (upperCaseLine.charAt(k) != entry.charAt(k - j) && line.charAt(k) != ' ') {
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
