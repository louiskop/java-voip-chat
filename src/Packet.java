import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;

/**
 * The Packet class represents the packets sent and received by the chat
 * application. A packet can be of different types such as request, message,
 * voice note, invite or call. The packet contains different variables depending
 * on its type. It implements the Serializable interface to make it
 * serializable.
 */
public class Packet implements Serializable {

    // request variables
    String type;
    String stringData;
    ArrayList<String> listData;

    // message variables
    String from;
    Integer to;
    String messageData;

    // voice note variables
    byte[] voiceNote;

    // invite variables
    boolean isPrivate;
    Integer sessionId;
    String invitee;

    // call variables
    boolean isLeave;
    ArrayList<InetAddress> userAddresses;
    int port;
    int channel;
    ArrayList<ArrayList<String>> callList;

    /**
     * Constructs a new Packet object of type request.
     * 
     * @param type       the type of the request packet
     * @param stringData the string data for the request packet
     * @param listData   the list data for the request packet
     */
    public Packet(String type, String stringData, ArrayList<String> listData) {
        this.type = type;
        this.stringData = stringData;
        this.listData = listData;
    }

    /**
     * Constructs a new Packet object of type invite.
     * 
     * @param type      the type of the invite packet
     * @param isPrivate a boolean indicating whether the invite is private or public
     * @param sessionId the session ID of the invite
     * @param invitee   the invitee for the invite packet
     */
    public Packet(String type, boolean isPrivate, Integer sessionId, String invitee) {
        this.type = type;
        this.isPrivate = isPrivate;
        this.sessionId = sessionId;
        this.invitee = invitee;
    }

    /**
     * Constructs a new Packet object of type message.
     * 
     * @param type        the type of the message packet
     * @param from        the sender of the message packet
     * @param to          the recipient of the message packet
     * @param messageData the message data for the message packet
     * @param isPrivate   a boolean indicating whether the message is private or
     *                    public
     */
    public Packet(String type, String from, Integer to, String messageData, boolean isPrivate) {
        this.type = type;
        this.from = from;
        this.to = to;
        this.messageData = messageData;
        this.isPrivate = isPrivate;
    }

    /**
     * Constructs a new Packet object of type voice note.
     * 
     * @param type      the type of the voice note packet
     * @param from      the sender of the voice note packet
     * @param to        the recipient of the voice note packet
     * @param voiceNote the byte array of the voice note for the voice note packet
     * @param isPrivate a boolean indicating whether the voice note is private or
     *                  public
     */
    public Packet(String type, String from, Integer to, byte[] voiceNote, boolean isPrivate) {
        this.type = type;
        this.from = from;
        this.to = to;
        this.voiceNote = voiceNote;
        this.isPrivate = isPrivate;
    }

    /**
     * Constructs a new Packet object of type call.
     * 
     * @param type          the type of the call packet
     * @param sessionId     the session ID of the call
     * @param isLeave       a boolean indicating whether the call is a leave call
     * @param isPrivate     a boolean indicating whether the call is private or
     *                      public
     * @param port          the port for the call
     * @param userAddresses the ArrayList of user addresses for the call
     */
    public Packet(String type, Integer sessionId, boolean isLeave, boolean isPrivate, int port,
            ArrayList<InetAddress> userAddresses, int channel) {
        this.type = type;
        this.sessionId = sessionId;
        this.isLeave = isLeave;
        this.isPrivate = isPrivate;
        this.channel = channel;
        this.port = port;
        this.userAddresses = userAddresses;
    }

    // call list packet
    public Packet(String type, Integer sessionId, boolean isPrivate, ArrayList<ArrayList<String>> callList) {
        this.type = type;
        this.sessionId = sessionId;
        this.isPrivate = isPrivate;
        this.callList = callList;
    }

}
