package autoscrabble;

public class DictEntry {
  public static final char[] ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
  private final String word;
  private final int alphabetMask;

  public DictEntry(String word) {
    this.word = word;

    alphabetMask = createAlphabetMask(word);
  }

  public static int createAlphabetMask(String word) {
    int mask = 0;
    for (int i = 0; i < ALPHABET.length; i++) {
      char c = ALPHABET[i];
      if (word.indexOf(c) != -1) {
        mask |= 0b1 << i;
      }
    }
    return mask;
  }

  public String getWord() {
    return word;
  }

  public int getAlphabetMask() {
    return alphabetMask;
  }
}
