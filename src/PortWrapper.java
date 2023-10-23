import java.net.InetAddress;
import java.util.ArrayList;

/**
 * The PortWrapper class represents a wrapper object containing port number, a
 * list of user names, a list of user addresses,
 * and a list of active calls.
 */
public class PortWrapper {

    // int port;
    ArrayList<Integer> ports = new ArrayList<Integer>();
    ArrayList<String> users;
    ArrayList<InetAddress> userAddresses;
    ArrayList<ArrayList<String>> callList;

    /**
     * Creates a new PortWrapper object with the given port number, list of user
     * names, list of user addresses, and list of
     * active calls.
     *
     * @param port          the port number
     * @param users         the list of user names
     * @param callList      the list of active calls
     * @param userAddresses the list of user addresses
     */
    public PortWrapper(int port, ArrayList<String> users, ArrayList<ArrayList<String>> callList,
            ArrayList<InetAddress> userAddresses) {
        this.ports.add(port);
        this.ports.add(port + 1);
        this.ports.add(port + 2);
        this.ports.add(port + 3);
        this.users = users;
        this.callList = callList;
        this.userAddresses = userAddresses;
    }

}