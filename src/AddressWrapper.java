import java.io.ObjectOutputStream;
import java.net.InetAddress;

/**
 * This class represents a wrapper object for an ObjectOutputStream
 * and an InetAddress object.
 */
public class AddressWrapper {

    public ObjectOutputStream out;
    public InetAddress address;

    /**
     * Constructs a new AddressWrapper object with the specified ObjectOutputStream
     * and InetAddress.
     *
     * @param out     the ObjectOutputStream to be wrapped
     * @param address the InetAddress to be wrapped
     */
    public AddressWrapper(ObjectOutputStream out, InetAddress address) {
        this.out = out;
        this.address = address;
    }

}
