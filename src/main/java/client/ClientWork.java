package client;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;

import static client.ClientChat.WORK_DIR;
import static client.ClientChat.fileSeparator;
import static java.nio.charset.StandardCharsets.*;

class ClientWork {
    private static final String CLIENT_CHAT_LOG = "clientChat.log";

    private static final Path CLIENT_CHAT_LOG_FILE = Path.of(WORK_DIR + fileSeparator + CLIENT_CHAT_LOG);
    private Socket socket;
    private BufferedReader in;
    private BufferedWriter out;
    private BufferedReader inputUser;
    private String nick;

    public ClientWork() {

    }

    public void clientStart(String addr, int port) {

        try {
            this.socket = new Socket(addr, port);
        } catch (IOException e) {
            System.err.println("Socket failed");
        }
        try {
            inputUser = new BufferedReader(new InputStreamReader(System.in, UTF_8));
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), UTF_8));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), UTF_8));
            this.pressNickname();
            new ReadMsg().start();
            new WriteMsg().start();
        } catch (IOException e) {
            ClientWork.this.downService();
        }
    }


    private void pressNickname() {

        System.out.print("Press your nick: ");
        try {
            boolean nn = true;
            while (nn) {
                String select = inputUser.readLine();
                out.write(select + "\n");
                out.flush();
                select = in.readLine();
                if (select.contains("@")) {
                    nn = false;
                    nick = select;
                } else {
                    System.out.println(select);
                }
            }
            hello();
        } catch (IOException ignored) {
        }

    }

    private void hello() {
        try {
            out.write("Hello, " + nick + "!\n");
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void downService() {
        try {
            if (!socket.isClosed()) {
                socket.close();
                in.close();
                out.close();
            }
        } catch (IOException ignored) {
        }
    }

    private class ReadMsg extends Thread {
        @Override
        public void run() {
            String str;
            try {
                while (true) {
                    str = in.readLine();
                    if (str.equals("/exit")) {
                        out.write(nick + " go home");
                        out.flush();
                        ClientWork.this.downService();
                        break;
                    }
                    System.out.println(str);
                }
            } catch (IOException e) {
                ClientWork.this.downService();
            }
        }
    }


    public class WriteMsg extends Thread {

        @Override
        public void run() {
            while (true) {
                String userWord;

                try {
                    userWord = inputUser.readLine();
                    if (userWord.equals("/exit")) {
                        out.write(nick + " left the chat\n");
                        out.flush();
                        out.write("/exit" + "\n");
                        writeLog(outData() + " /exit" + "\n");
                        writeLog(outData() + "+" + nick + " left the chat\n");
                        out.flush();
                        ClientWork.this.downService();
                        break;
                    } else {
                        out.write("[" + outData() + "] " + nick + ": " + userWord + "\n");
                        writeLog("[" + outData() + "] " + nick + ": " + userWord + "\n");
                    }
                    out.flush();
                } catch (IOException e) {
                    ClientWork.this.downService();

                }

            }
        }
    }

    private void writeLog(String msg) throws IOException {
        if (!Files.exists(CLIENT_CHAT_LOG_FILE)) {
            try {
                Files.createFile(CLIENT_CHAT_LOG_FILE);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        Files.write(CLIENT_CHAT_LOG_FILE,
                msg.getBytes(),
                StandardOpenOption.APPEND,
                StandardOpenOption.CREATE
        );
    }

    public static String outData() {
        Date time = new Date();
        SimpleDateFormat dt1 = new SimpleDateFormat("HH:mm:ss");
        return dt1.format(time);
    }
}
