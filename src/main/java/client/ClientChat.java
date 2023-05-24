package client;


import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class ClientChat {
    private static final String CHAT_DIR = "Chat\\";
    private static final String CONFIG_FILE = "client.cfg";
    static final Path WORK_DIR = Path.of(System.getProperty("user.home") + "\\" + CHAT_DIR);
    private static final Path CFG_FILE = Path.of(WORK_DIR + "\\" + CONFIG_FILE);
    private static final Properties properties = new Properties();
    private static int PORT;
    private static String HOST;


    public static void main(String[] args) throws IOException {
        fileConfig();
        new ClientWork(HOST, PORT);
    }

    private static void fileConfig() throws IOException {

        if (!Files.isDirectory(WORK_DIR) || !Files.exists(CFG_FILE)) {
            System.out.println("Configuration file not found");
            System.out.println("A configuration file has been created: " + CFG_FILE);
            PORT = 8080;
            HOST = "localhost";
            System.out.println("host = " + HOST + "\nport = " + PORT);
            System.out.println(Charset.defaultCharset());
            if (!Files.isDirectory(WORK_DIR)) {
                Files.createDirectory(WORK_DIR);
            }
            Files.createFile(CFG_FILE);
            Files.writeString(CFG_FILE, "HOST=localhost\nPORT=8080");
        } else {
            File file = new File(String.valueOf(CFG_FILE));
            properties.load(new FileReader(file));
            PORT = Integer.parseInt(properties.getProperty("PORT"));
            HOST = properties.getProperty("HOST");

        }
    }

}