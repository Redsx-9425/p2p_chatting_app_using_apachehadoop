import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.util.ArrayList;

public class HadoopRegistry {

    private static final String REGISTRY_PATH = "/user_list/registry.txt";

    private static FileSystem getFileSystem() throws Exception {
        Configuration conf = new Configuration();
        return FileSystem.get(new URI("hdfs://localhost:9000"), conf);
    }

    // register file for every user in the format of username.txt containing ip:port
    // I see no difference between this and one file for all users, but this easier to implement
    public static void registerUser(String username, String ip, int port) {
        try {
            FileSystem fs = getFileSystem();
            Path dirPath = new Path(REGISTRY_PATH);

            ArrayList<String> lines = new ArrayList<>();
            boolean exists = fs.exists(dirPath);

            if (exists) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(fs.open(dirPath)))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (!line.startsWith(username + ",")) {
                            lines.add(line);
                        }
                    }
                }
            } else {// if the directory doesn't exist, create it
                fs.mkdirs(new Path("/user_list/"));
            }


            lines.add(username + "," + ip + ":" + port);

            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fs.create(dirPath, true)))) {
                for (String l : lines) {
                    writer.write(l+"\n");

                }
            }
            System.out.println("\n[Hadoop] Successfully registered " + username + " at " + ip + ":" + port);

        } catch (Exception e) {
            System.err.println("\n[Hadoop Error] Failed to register user: " + e.getMessage());
        }
    }

    // list all users in the registry
    // here I regret not using one file for all users and I remember that every file have metadata so it consumes more space and time
    public static ArrayList<String> listAllUsers() {
        ArrayList<String> users = new ArrayList<>();
        try {
            FileSystem fs = getFileSystem();
            Path path = new Path(REGISTRY_PATH);

            if (fs.exists(path)) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(fs.open(path)))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (!line.isBlank()) {
                            String[] record = line.split(",");
                            users.add(record[0]);
                        }
                    }
                }
            } else {
                System.out.println("[Hadoop] No registry file found. Registry is empty.");
            }
        } catch (Exception e) {
            System.err.println("[Hadoop Error] Failed to list registry: " + e.getMessage());
        }
        return users;
    }
    // remove the user from the registry
    // read the file save it to list without the user and rewrite the list to file again
    public static void deregisterUser(String username) {
        try {
            FileSystem fs = getFileSystem();
            Path dirPath = new Path(REGISTRY_PATH);
            if (!fs.exists(dirPath)) return;

            ArrayList<String> afterDelete = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(fs.open(dirPath)))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.startsWith(username + ",")) {
                        afterDelete.add(line);
                    }
                }

            }
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fs.create(dirPath, true)))) {
                for (String l : afterDelete) {
                    writer.write(l);
                    writer.newLine();
                }
            }
        }catch (Exception e) {
            System.err.println("Failed to remove registry file: " + e.getMessage());
        }
    }

    public static String lookupUser(String username) {
        try {
            FileSystem fs = getFileSystem();
            Path path = new Path(REGISTRY_PATH);

            if (fs.exists(path)) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(fs.open(path)))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String[] record = line.split(",");
                        if (record[0].equalsIgnoreCase(username)) {
                            return record[1]; // return the ip:port
                        }
                    }
                }
            }
            System.out.println("[Hadoop] User '" + username + "' not found.");
        } catch (Exception e) {
            System.err.println("[Hadoop Error] Failed to lookup user: " + e.getMessage());
        }
        return null;
    }
}