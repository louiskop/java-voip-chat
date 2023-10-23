import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.swing.JList;
import javax.swing.JTextArea;

/**
 * The ClientManager class manages the client-side connections and interactions
 * with the server. It implements the Runnable
 * interface to allow for multithreading.
 */
public class ClientManager implements Runnable {

    /** The socket used for communication with the client. */
    Socket socket;

    /** A HashMap that maps usernames to AddressWrapper objects. */
    HashMap<String, AddressWrapper> userList;

    /** A HashMap that maps session IDs to PortWrapper objects. */
    HashMap<Integer, PortWrapper> sessions;

    /** The client's nickname. */
    String nickname;

    /** The text area used to display log messages. */
    JTextArea log;

    /** The list used to display log messages. */
    JList logList;

    /** The output stream used to send objects to the client. */
    ObjectOutputStream out;

    /** The input stream used to receive objects from the client. */
    ObjectInputStream in;

    /**
     * Constructs a new ClientManager object with the specified parameters.
     * 
     * @param s        the socket used for communication with the client
     * @param userList a HashMap that maps usernames to AddressWrapper objects
     * @param sessions a HashMap that maps session IDs to PortWrapper objects
     * @param log      the text area used to display log messages
     * @param logList  the list used to display log messages
     * 
     * @throws Exception if there is an error creating the input or output stream
     */
    public ClientManager(Socket s, HashMap<String, AddressWrapper> userList, HashMap<Integer, PortWrapper> sessions,
            JTextArea log, JList logList) throws Exception {
        this.socket = s;
        this.userList = userList;
        this.sessions = sessions;
        this.log = log;
        this.logList = logList;

        // create input and output streams
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.in = new ObjectInputStream(socket.getInputStream());

    }

    /**
     * Sends the specified packet to all connected users.
     * 
     * @param packet the packet to send
     */
    public void broadcast(Packet packet) {

        String nicknames[] = userList.keySet().toArray(new String[userList.size()]);

        // send packet to all connected users
        for (int i = 0; i < userList.size(); i++) {
            try {
                userList.get(nicknames[i]).out.writeObject(packet);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    /**
     * Sends the specified packet to the specified set of users.
     * 
     * @param packet the packet to send
     * @param users  the set of users to send the packet to
     * 
     * @throws Exception if there is an error sending the packet
     */
    public void broadcastSet(Packet packet, ArrayList<String> users) throws Exception {

        for (int i = 0; i < users.size(); i++) {
            userList.get(users.get(i)).out.reset();
            userList.get(users.get(i)).out.writeObject(packet);
        }

    }

    /**
     * The main run method of the ClientManager. Waits for requests from the client
     * and handles them accordingly.
     */
    @Override
    public void run() {

        // wait for requests from client
        while (socket.isConnected()) {

            try {
                Packet packet = (Packet) in.readObject();

                switch (packet.type) {

                    // TODO: implement all requests

                    // test request
                    case "echo":
                        log.append(" - " + nickname + " said: " + packet.stringData + "\n");
                        break;

                    // send a message
                    case "message":

                        log.append(
                                " - " + packet.from + " sent message to session " + packet.to + " : "
                                        + packet.messageData + "\n");

                        // send message to session
                        if (sessions.containsKey(packet.to)) {
                            broadcastSet(packet, sessions.get(packet.to).users);
                        } else {
                            packet = new Packet("error", "There exists no such session", null);
                            out.writeObject(packet);
                        }

                        break;

                    // send a voice note
                    case "voicenote":

                        log.append(" - " + packet.from + " sent a voice note to session " + packet.to + "\n");

                        ArrayList<String> sendUsers = (ArrayList<String>) (sessions.get(packet.to).users)
                                .clone();

                        sendUsers.remove(packet.from);

                        // broadcast the voice note to all in session
                        broadcastSet(packet, sendUsers);

                        break;

                    // join or leave call session
                    case "call":

                        // port extraction
                        int port = sessions.get(packet.sessionId).ports.get(packet.channel);
                        int sesId = packet.sessionId;

                        if (packet.isLeave) {
                            log.append(" - " + nickname + " has left the call [channel " + packet.channel
                                    + "] of session " + packet.sessionId + "\n");

                            // update call list
                            sessions.get(packet.sessionId).callList.get(packet.channel).remove(nickname);

                            // leave call on port with same packet
                            out.writeObject(packet);

                            // output event on text area
                            packet = new Packet("message", nickname, packet.sessionId,
                                    "[ ! ] " + nickname + " has left the call [channel " + packet.channel
                                            + "] session.",
                                    packet.isPrivate);
                            broadcastSet(packet, sessions.get(sesId).users);

                        } else {
                            log.append(" - " + nickname + " has joined the call [channel " + packet.channel
                                    + "] of session " + packet.sessionId
                                    + "on port " + port + "\n");

                            // update call list
                            sessions.get(packet.sessionId).callList.get(packet.channel).add(nickname);

                            // join call on port with updated packet
                            packet.port = port;
                            for (int i = 0; i < sessions.get(sesId).users.size(); i++) {
                                // do not add yourself
                                if (sessions.get(sesId).users.get(i).equals(nickname)) {
                                    continue;
                                }
                                packet.userAddresses.add(sessions.get(sesId).userAddresses.get(i));
                            }
                            out.writeObject(packet);

                            // output event on text area
                            packet = new Packet("message", nickname, packet.sessionId,
                                    "[ ! ] " + nickname + " has joined the call [channel " + packet.channel
                                            + "] session.",
                                    packet.isPrivate);
                            broadcastSet(packet, sessions.get(sesId).users);

                        }
                        break;

                    // create a session
                    case "session":
                        log.append(" - " + nickname + " created a session with id " + Server.sessionID + "\n");

                        boolean group = false;

                        if (packet.stringData.equals("Group")) {
                            group = true;
                        }

                        // tell user what session he is in (isPrivate = true , because primitive type !=
                        // null)
                        packet = new Packet("session", true, Server.sessionID, null);
                        out.writeObject(packet);

                        ArrayList<ArrayList<String>> callList = new ArrayList<ArrayList<String>>();
                        callList.add(new ArrayList<String>()); // channel 0
                        callList.add(new ArrayList<String>()); // channel 1
                        callList.add(new ArrayList<String>()); // channel 2
                        callList.add(new ArrayList<String>()); // channel 3
                        ArrayList<String> sessionUsers = new ArrayList<String>();
                        ArrayList<InetAddress> userAddresses = new ArrayList<InetAddress>();
                        userAddresses.add(userList.get(nickname).address);
                        sessionUsers.add(nickname);

                        // assign and increment callport
                        sessions.put(Server.sessionID,
                                new PortWrapper(Server.callPort, sessionUsers, callList, userAddresses));

                        // notify group creator to open frame
                        if (group) {
                            packet = new Packet("notify", Server.sessionID.toString(), null);
                            out.writeObject(packet);

                            // send creator sessionUsers
                            packet = new Packet("sessionUsers", Server.sessionID.toString(),
                                    sessions.get(Server.sessionID).users);
                            out.writeObject(packet);

                        }

                        Server.callPort += 4;
                        Server.sessionID++;

                        break;

                    case "invite":

                        log.append("- " + nickname + " added " + packet.invitee + " to " + packet.sessionId + " \n");

                        // add user and address to session list
                        sessions.get(packet.sessionId).users.add(packet.invitee);
                        sessions.get(packet.sessionId).userAddresses.add(userList.get(packet.invitee).address);

                        ArrayList<String> usersToInvite = sessions.get(packet.sessionId).users;

                        // notify user
                        if (packet.isPrivate) {
                            packet = new Packet("notifyPrivate", packet.sessionId.toString(), usersToInvite);
                            broadcastSet(packet, usersToInvite);
                        } else {
                            ArrayList<String> inviteeList = new ArrayList<String>();
                            inviteeList.add(packet.invitee);
                            packet = new Packet("notify", packet.sessionId.toString(), null);
                            broadcastSet(packet, inviteeList);

                            // send updated sessionUsers
                            packet = new Packet("sessionUsers", packet.stringData,
                                    sessions.get(Integer.parseInt(packet.stringData)).users);
                            broadcastSet(packet, sessions.get(Integer.parseInt(packet.stringData)).users);
                        }

                        break;

                    // register user with nickname
                    case "register":

                        // get selected nickname
                        nickname = packet.stringData;

                        // check duplicates
                        if (userList.containsKey(nickname)) {
                            packet = new Packet("error", "Nickname already in use", null);
                            out.writeObject(packet);
                            break;
                        }

                        // add user to userlist
                        userList.put(nickname, new AddressWrapper(out, socket.getInetAddress()));
                        log.append("[+] A new client has connected: " + nickname + "\n");
                        log.append("\t the current user list : " + userList.toString() + "\n");

                        // send success packet
                        packet = new Packet("success", null, null);
                        out.writeObject(packet);

                        // broadcast new userList
                        packet = new Packet("userList", null, new ArrayList<String>(userList.keySet()));
                        broadcast(packet);

                        // update server userlist
                        String[] clientList = Arrays.copyOf(
                                packet.listData.toArray(), packet.listData.size(), String[].class);
                        logList.setListData(clientList);

                        break;

                    // send the user list to the client
                    case "getUserList":
                        packet = new Packet("userList", null, new ArrayList<String>(userList.keySet()));
                        out.writeObject(packet);
                        break;

                    case "calllist":
                        // send back call list
                        packet = new Packet("calllist", packet.sessionId, packet.isPrivate,
                                sessions.get(packet.sessionId).callList);
                        out.writeObject(packet);
                        break;

                    // disconnect client and finish thread
                    case "disconnect":

                        // remove from user list
                        log.append("[+] Disconnecting client: " + nickname);
                        userList.remove(nickname);

                        // send updated user list to all clients
                        packet = new Packet("userList", null, new ArrayList<>(userList.keySet()));
                        broadcast(packet);

                        // update server user list
                        clientList = Arrays.copyOf(
                                packet.listData.toArray(), packet.listData.size(), String[].class);
                        logList.setListData(clientList);
                        // finish thread
                        socket.close();
                        return;

                    // disconnect user from session
                    // use invite packet to check if private
                    case "disconnectSession":
                        log.append("[+] " + nickname + " left session " + packet.sessionId);

                        sesId = packet.sessionId;

                        // remove user and address from session
                        sessions.get(packet.sessionId).users.remove(nickname);
                        sessions.get(packet.sessionId).userAddresses.remove(userList.get(nickname).address);

                        // tell client to disconnect (packet already in correct format)
                        out.writeObject(packet);

                        // send updated sessionUsers to group members
                        packet = new Packet("sessionUsers", packet.sessionId.toString(),
                                sessions.get(packet.sessionId).users);
                        broadcastSet(packet, sessions.get(sesId).users);

                        break;
                    default:
                        System.out.println("[!] Invalid request received from client");
                        break;
                }

            } catch (Exception e) {
                // System.out.println("[!] Error reading request from client");
                // e.printStackTrace();
            }

        }

    }

}
