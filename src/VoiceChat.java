import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

/**
 * The voice chat class handles the UDP calling feature of the application. It
 * can start and stop a call on a given port
 */
public class VoiceChat {

    // audio configuration
    private static final int PACKET_SIZE = 1000;
    private static DatagramSocket socket;
    private static TargetDataLine inputLine;
    private static SourceDataLine outputLine;

    /**
     * Join a call on the specified port with the users with adresses
     * 
     * @param port      The port on which the call is taking place
     * @param addresses The adresses of all the users in the session.
     * @throws Exception Any errors relating to the call
     */
    public static void join(Integer port, ArrayList<InetAddress> addresses) throws Exception {

        // create socket
        socket = new DatagramSocket(port);

        // create 2 threads for input and output
        Thread inputThread = new Thread(() -> {
            listen(port, addresses);
        });
        Thread outputThread = new Thread(() -> {
            playback(port);
        });

        // start threads
        inputThread.start();
        outputThread.start();

    }

    /**
     * Leave the call and close the datalines
     */
    public static void leave() {
        // close the lines and socket
        inputLine.stop();
        outputLine.stop();
        socket.close();
    }

    /**
     * 
     * Listen to voice data on microphone and send to the other users over UDP
     * 
     * @param port      Port over which to send data
     * @param addresses The addresses of the other users
     */
    // listen for user input and send over UDP
    public static void listen(Integer port, ArrayList<InetAddress> addresses) {

        try {
            // set format
            AudioFormat format = new AudioFormat(8000.0f, 16, 1, true, true);

            // listen for voice input
            DataLine.Info inputInfo = new DataLine.Info(TargetDataLine.class, format);
            inputLine = (TargetDataLine) AudioSystem.getLine(inputInfo);
            inputLine.open(format);
            inputLine.start();

            // create and read data into buffer
            byte[] voiceBuffer = new byte[PACKET_SIZE];

            while (true) {

                int bytes = inputLine.read(voiceBuffer, 0, voiceBuffer.length);

                // send packets to all users in session (except self)
                for (int i = 0; i < addresses.size(); i++) {
                    // create UDP packet from buffer
                    DatagramPacket packet = new DatagramPacket(voiceBuffer, bytes, addresses.get(i), port);
                    // send packet
                    socket.send(packet);
                }

            }

        } catch (Exception e) {
            // e.printStackTrace();
        }

    }

    /**
     * 
     * Listen for incoming UDP packets on the current call and play it back to the
     * user
     * 
     * @param port The port on which to listen for traffic
     */
    public static void playback(Integer port) {

        try {
            // set format
            AudioFormat format = new AudioFormat(8000.0f, 16, 1, true, true);

            // setup line to play received audio
            DataLine.Info outputInfo = new DataLine.Info(SourceDataLine.class, format);
            outputLine = (SourceDataLine) AudioSystem.getLine(outputInfo);
            outputLine.open(format);
            outputLine.start();

            // create and read received data into buffer
            byte[] voiceBuffer = new byte[PACKET_SIZE];

            while (true) {

                // receive a UDP packet
                DatagramPacket packet = new DatagramPacket(voiceBuffer, voiceBuffer.length);
                socket.receive(packet);

                // write to outputline
                outputLine.write(voiceBuffer, 0, packet.getLength());

            }

        } catch (Exception e) {

            // e.printStackTrace();

        }

    }

}
