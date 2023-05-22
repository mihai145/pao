package audit;

import service.ServiceCommand;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

// Singleton class that handles audit
public class Audit {
    private static Audit instance = null;
    private final FileWriter auditFile;

    // openes or creates a csv audit file
    private Audit() throws IOException {
        File f = new File("/home/mihai145/Uni/an2-sem2/pao/Proiect_PAO/audit.csv");
        //noinspection ResultOfMethodCallIgnored
        f.createNewFile();
        this.auditFile = new FileWriter(f, true);
    }

    public static Audit getInstance() throws IOException {
        if (instance == null) {
            instance = new Audit();
        }
        return instance;
    }

    // logs a user-initiated command
    public void logCommand(ServiceCommand command) {
        try {
            auditFile.write(command + "," + new Date() + "\n");
        } catch (IOException e) {
            System.out.println("Could not write to audit file: " + e.getMessage());
        }
    }

    // logs the start of a new market simulation
    public void logSimulation() {
        try {
            auditFile.write("START_NEW_SIMULATION," + new Date() + "\n");
        } catch (IOException e) {
            System.out.println("Could not write to audit file: " + e.getMessage());
        }
    }

    // closes the file
    public void close() throws IOException {
        auditFile.close();
    }
}
