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

  public List<Word1D> getWords(String line, char[] h) {
    boolean rackContainsBlank = String.valueOf(h).chars().anyMatch(c -> c == '_');
    // Counting max possible length of words, possible future sorting???

    long t = System.nanoTime();
    var words = getWordsFitInLine(line);
    System.out.println((System.nanoTime() - t) / 1000000);

    int[] handCount = new int[27];

    for (char c : h) {
      if (Character.isAlphabetic(c)) {
        handCount[Character.getNumericValue(c) - 10]++;
      }
    }
    var validList1 = words.toArray(new Word1D[0]);
    int validList1n = validList1.length;
    Word1D[] validList2temp = new Word1D[validList1n];
    if (rackContainsBlank) validList2temp = new Word1D[validList1n * 26];
    int validList2n = 0;
    if (!rackContainsBlank) { // Normal mode
      for (Word1D word : validList1) {
        int[] charCount = new int[26];
        int k = 0;
        int lettersPlaced = 0;
        for (int j = word.getStartIndex();
            j < word.getWord().length() + word.getStartIndex();
            j++) {
          if (line.charAt(j) == ' ') {
            lettersPlaced++;
            charCount[Character.getNumericValue(word.getWord().charAt(k)) - 10]++;
          }
          k++;
        }

        boolean valid = true;
        for (int j = 0; j < 26; j++) {
          if (lettersPlaced == 0) {
            valid = false;
          }
          if (charCount[j] > handCount[j]) {
            valid = false;
          }
        }
        if (valid) {
          validList2temp[validList2n] = word;
          validList2n++;
        }
      }
    } else { // With blank(s) in hand
      for (Word1D word : validList1) {
        int[] charCount = new int[26];
        int k = 0;
        int lettersPlaced = 0;
        for (int j = word.getStartIndex();
            j < word.getWord().length() + word.getStartIndex();
            j++) {
          if (line.charAt(j) == ' ') {
            lettersPlaced++;
            charCount[Character.getNumericValue(word.getWord().charAt(k)) - 10]++;
          }
          k++;
        }
        for (int m = 0; m < 26; m++) {
          handCount[m]++;
          boolean valid = true;
          boolean blankUsed = false;
          for (int j = 0; j < 26; j++) {
            if (lettersPlaced == 0) {
              valid = false;
            }
            if (charCount[j] <= handCount[j]) {
              if (j == m) {
                if (j - 1 != -1) {
                  if ((charCount[j] > handCount[j] - 1)) {
                    blankUsed = true;
                  }
                }
              }
            } else {
              valid = false;
            }
          }

          if (valid) {
            validList2temp[validList2n] = word;
            if (blankUsed) {
              for (int o = 0;
                  o < validList2temp[validList2n].getWord().length();
                  o++) { // Make the first blank letter lower case (not 100% ideal)
                if (Character.getNumericValue(validList2temp[validList2n].getWord().charAt(o)) - 10
                    == m) {
                  if (' ' == line.charAt(validList2temp[validList2n].getStartIndex() + o)) {
                    validList2temp[validList2n].setWord(
                        validList2temp[validList2n].getWord().substring(0, o)
                            + Character.toLowerCase(validList2temp[validList2n].getWord().charAt(o))
                            + validList2temp[validList2n].getWord().substring(o + 1));
                    break;
                  }
                }
              }
            }
            validList2n++;
          }
          handCount[m]--;
        }
      }
    }

    Word1D[] validList2 = new Word1D[validList2n];
    System.arraycopy(validList2temp, 0, validList2, 0, validList2n);
    Word1D[] validList3temp = new Word1D[validList2n];
    int validList3n = 0;
    for (int i = 0; i < validList2n; i++) {
      boolean valid = true;

      var i2 = validList2[i].getStartIndex() - 1;
      if (i2 > 0 && ' ' != line.charAt(i2)) {
        valid = false;
      }

      var i1 = validList2[i].getStartIndex() + validList2[i].getWord().length();
      if (i1 < line.length() && ' ' != line.charAt(i1)) {
        valid = false;
      }

      if (valid) {
        validList3temp[validList3n] = validList2[i];
        validList3n++;
      }
    }
    Word1D[] validList3 = new Word1D[validList3n];
    System.arraycopy(validList3temp, 0, validList3, 0, validList3n);
    return List.of(validList3);
  }

  /**
   * Get words which fit on the line
   */
  private ArrayList<Word1D> getWordsFitInLine(String line) {
    var list = new ArrayList<Word1D>();
    String upperCaseLine = line.toUpperCase();
    int lineAlphabetMask = DictEntry.createAlphabetMask(line);
    for (var entry : dictionary) {
      if ((entry.getAlphabetMask() & lineAlphabetMask) == 0) {
        // Skip if the line and the word have no matching letters
        continue;
      }
      for (int j = 0; j < line.length() - entry.getWord().length() + 1; j++) {
        boolean collision = false;
        boolean connectingLetterExists = false;
        for (int k = j; k < entry.getWord().length() + j; k++) {
          // We need to match with at least 1 letter already on the board
          if (line.charAt(k) != ' ') {
            connectingLetterExists = true;
          }
          // Check for collisions with letters on the board
          if (upperCaseLine.charAt(k) != entry.getWord().charAt(k - j) && line.charAt(k) != ' ') {
            collision = true;
            break;
          }
        }
        if (!collision && connectingLetterExists) {
          list.add(new Word1D(entry.getWord(), j));
        }
      }
    }
    return list;
  }
}
