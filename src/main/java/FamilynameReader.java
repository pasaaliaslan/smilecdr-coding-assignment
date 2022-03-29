import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * FamilynameReader Class.
 */
public class FamilynameReader {
  
  /**
   * Return list of the family names extracted from filepath.
   */
  public static List<String> extractFamilynamesFromFile(String filepath) {
    ArrayList<String> familynames = new ArrayList<String>();

    try {
      File fileWithSurnames = new File(filepath);
      Scanner fileReader = new Scanner(fileWithSurnames);
      while (fileReader.hasNextLine()) {
        String surname = fileReader.nextLine();
        familynames.add(surname);
      }
      fileReader.close();
    } catch (Exception e) {
      System.out.println("Cannot read from file.");
    }

    return familynames;
  }
}
