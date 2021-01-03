package autoscrabble.word;

public class BlankRequiredWord extends Word {
  public int[] blankRequirements;
  public boolean blanksNeeded;

  public BlankRequiredWord(String word, int[] blankRequirements, boolean blanksNeeded) {
    super(word);
    this.blankRequirements = blankRequirements;
    this.blanksNeeded = blanksNeeded;
  }

  public void setBlankRequirements(int[] blankRequirements) {
    this.blankRequirements = blankRequirements;
    this.blanksNeeded = true;
  }
}
