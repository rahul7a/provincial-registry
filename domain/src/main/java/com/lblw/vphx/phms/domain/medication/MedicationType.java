package com.lblw.vphx.phms.domain.medication;

/**
 * @see <a href="https://miro.com/app/board/o9J_l8yNn14=/?moveToWidget=3458764532961406757&cot=14">
 *     Medication Type</a>
 */
public enum MedicationType {
  DRUG("DRG"),
  COMPOUND("COMPOUND"),
  DEVICE("DEV");

  private final String shortName;

  private MedicationType(String shortName) {
    this.shortName = shortName;
  }

  public String getShortName() {
    return shortName;
  }

}
