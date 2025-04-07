package fctreddit.api.utils;

import java.io.IOException;
import java.net.*;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * <p>
 * A class to perform service discovery, based on periodic service contact
 * endpoint announcements over multicast communication.
 * </p>
 *
 * <p>
 * Servers announce their *name* and contact *uri* at regular intervals. The
 * server actively collects received announcements.
 * </p>
 *
 * <p>
 * Service announcements have the following format:
 * </p>
 *
 * <p>
 * &lt;service-name-string&gt;&lt;delimiter-char&gt;&lt;service-uri-string&gt;
 * </p>
 */
public class Discovery {
    private static final Logger Log = Logger.getLogger(Discovery.class.getName());

    // Using concurrent map to ensure thread safety
    private final ConcurrentHashMap<String, ServiceInfo> services = new ConcurrentHashMap<>();

    static {
        // addresses some multicast issues on some TCP/IP stacks
        System.setProperty("java.net.preferIPv4Stack", "true");
        // summarizes the logging format
        System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s");
    }

    // The pre-agreed multicast endpoint assigned to perform discovery.
    // Allowed IP Multicast range: 224.0.0.1 - 239.255.255.255
    public static final InetSocketAddress DISCOVERY_ADDR = new InetSocketAddress("226.226.226.226", 2266);
    public static final int DISCOVERY_ANNOUNCE_PERIOD = 1000;
    public static final int DISCOVERY_RETRY_TIMEOUT = 5000;
    public static final int MAX_DATAGRAM_SIZE = 65536;
    public static final int DISCOVERY_ANNOUNCEMENT_EXPIRATION = 5000;

    // Used to separate the two fields that make up a service announcement.
    private static final String DELIMITER = "\t";

    private final InetSocketAddress addr;
    private final String serviceName;
    private final String serviceURI;
    private final MulticastSocket ms;

    // The single instance of Discovery.
    private static Discovery instance = null;

    /**
     * Private constructor to prevent direct instantiation.
     */
    private Discovery(InetSocketAddress addr, String serviceName, String serviceURI)
            throws SocketException, UnknownHostException, IOException {
        if (addr == null) {
            throw new RuntimeException("A multicast address has to be provided.");
        }
        this.addr = addr;
        this.serviceName = serviceName;
        this.serviceURI = serviceURI;
        this.ms = new MulticastSocket(addr.getPort());
        this.ms.joinGroup(addr, NetworkInterface.getByInetAddress(InetAddress.getLocalHost()));
    }

    /**
     * Returns the singleton instance. The first call must supply the parameters.
     *
     * @param addr the multicast address to join
     * @param serviceName the service name (if announcing; otherwise null)
     * @param serviceURI the service URI (if announcing; otherwise null)
     * @return the singleton Discovery instance
     * @throws IOException if an I/O error occurs
     */
    public static synchronized Discovery getInstance(InetSocketAddress addr, String serviceName, String serviceURI)
            throws IOException {
        if (instance == null) {
            instance = new Discovery(addr, serviceName, serviceURI);
        }
        return instance;
    }

    /**
     * Returns the singleton instance if it was already initialized.
     *
     * @return the singleton Discovery instance
     */
    public static Discovery getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Discovery instance is not yet initialized. " +
                    "Call getInstance(InetSocketAddress, String, String) first.");
        }
        return instance;
    }

    /**
     * Starts sending service announcements at regular intervals...
     * @throws IOException
     */
    public void start() {
        // If this discovery instance was initialized with service information,
        // start the thread that makes the periodic announcement.
        if (this.serviceName != null && this.serviceURI != null) {
            Log.info(String.format("Starting Discovery announcements on: %s for: %s -> %s",
                    addr, serviceName, serviceURI));

            byte[] announceBytes = String.format("%s%s%s", serviceName, DELIMITER, serviceURI).getBytes();
            DatagramPacket announcePkt = new DatagramPacket(announceBytes, announceBytes.length, addr);

            new Thread(() -> {
                while (true) {
                    try {
                        ms.send(announcePkt);
                        Thread.sleep(DISCOVERY_ANNOUNCE_PERIOD);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

        // Start thread to collect announcements received from the network.
        new Thread(() -> {
            DatagramPacket pkt = new DatagramPacket(new byte[MAX_DATAGRAM_SIZE], MAX_DATAGRAM_SIZE);
            while (true) {
                try {
                    pkt.setLength(MAX_DATAGRAM_SIZE);
                    ms.receive(pkt);
                    String msg = new String(pkt.getData(), 0, pkt.getLength());
                    String[] msgElems = msg.split(DELIMITER);
                    if (msgElems.length == 2) { // periodic announcement
                        String receivedServiceName = msgElems[0];
                        URI receivedServiceURI = new URI(msgElems[1]);

                        System.out.printf("FROM %s (%s): %s\n", pkt.getAddress().getHostName(),
                                pkt.getAddress().getHostAddress(), msg);

                        ServiceInfo info = services.get(receivedServiceName);
                        if (info == null) {
                            info = new ServiceInfo();
                            services.put(receivedServiceName, info);
                        }

                        // Add the URI if it's not already present.
                        if (!info.serviceUris.contains(receivedServiceURI)) {
                            info.serviceUris.add(receivedServiceURI);
                        }

                        info.serviceTimestamp = Instant.now();
                    }
                } catch (IOException e) {
                    // Do nothing
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    /**
     * Returns the known services.
     *
     * @param serviceName the name of the service being discovered
     * @param minReplies minimum number of requested URIs. Blocks until satisfied.
     * @return an array of URI with the service instances discovered.
     */
    public URI[] knownUrisOf(String serviceName, int minReplies) {
        while (true) {
            ServiceInfo info = services.get(serviceName);
            if (info != null && !info.serviceUris.isEmpty()) {
                long elapsedMillis = Instant.now().toEpochMilli() - info.serviceTimestamp.toEpochMilli();
                if (elapsedMillis <= DISCOVERY_ANNOUNCEMENT_EXPIRATION && info.serviceUris.size() >= minReplies) {
                    return info.serviceUris.toArray(new URI[0]);
                }
            }
        }
    }

    // Main method for testing purposes
    public static void main(String[] args) throws Exception {
        // Initialize the singleton instance
        Discovery discovery = Discovery.getInstance(DISCOVERY_ADDR, "test",
                "http://" + InetAddress.getLocalHost().getHostAddress());
        discovery.start();
    }
}
