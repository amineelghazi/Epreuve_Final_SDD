package mv.sdd.io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// Lecture du fichier d'actions
public class ActionFileReader {
    public static List<Action> readActions(String filePath) throws IOException {
        List<Action> actions = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String ligne;
            while ((ligne = reader.readLine()) != null) {
                try {
                    Action action = ActionParser.parseLigne(ligne);
                    actions.add(action);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        }
        return actions;
    }
}
