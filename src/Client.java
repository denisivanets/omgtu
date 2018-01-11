import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;

public class Client {

    private static int index = 0;

    BufferedReader reader;
    PrintWriter writer;
    Socket sock;
    File file;

    public static void main(String[] args) {
        new Client().go();
    }

    public void go() {
        JFrame frame = new JFrame("Chat lab");
        JPanel mainPanel = new JPanel();
        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(new SendButtonListener());
        mainPanel.add(sendButton);
        frame.getContentPane().add(BorderLayout.CENTER, mainPanel);
        setUpNetworking();

        Thread readerThread = new Thread(new IncomingReader());
        readerThread.start();

        frame.setSize(650, 500);
        frame.setVisible(true);

        JFileChooser chooser = new JFileChooser();

        mainPanel.add(chooser);

        chooser.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY
                    .equals(evt.getPropertyName())) {
                    file = chooser.getSelectedFile();
                }
            }
        });
    }

    private void setUpNetworking() {
        try {
            sock = new Socket("127.0.0.1", 5000);
            InputStreamReader streamReader = new InputStreamReader(sock.getInputStream());
            reader = new BufferedReader(streamReader);
            writer = new PrintWriter(sock.getOutputStream());
            System.out.println("networking established");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public class SendButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent ev) {
            try {
                byte[] bytes = Files.readAllBytes(file.toPath());
                String strBytes = new String(bytes, "UTF-8");
                writer.println(strBytes);
                writer.flush();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    class IncomingReader implements Runnable {
        public void run() {
            String message;
            try {
                String strBytes = "";
                while ((message = reader.readLine()) != null) {
                    System.out.println("client read " + message);
                    strBytes += message;
                    byte[] bytes = strBytes.getBytes();
                    index++;
                    File file = new File("textfile" + index + ".txt");
                    file.createNewFile();
                    FileOutputStream fos = new FileOutputStream(file.getName());
                    fos.write(bytes);
                    fos.close();
                    strBytes = "";
                }

            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

}
