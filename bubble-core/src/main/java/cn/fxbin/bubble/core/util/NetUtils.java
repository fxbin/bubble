package cn.fxbin.bubble.core.util;

import cn.hutool.core.net.NetUtil;
import lombok.experimental.UtilityClass;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;

/**
 * NetUtils
 *
 * @author fxbin
 * @version v1.0
 * @since 2022/1/13 5:59 PM
 */
@UtilityClass
public class NetUtils extends NetUtil {

    /**
     * 获取 服务器 hostname
     *
     * @return hostname
     */
    public String getHostName() {
        String hostname;
        try {
            InetAddress address = InetAddress.getLocalHost();
            // force a best effort reverse DNS lookup
            hostname = address.getHostName();
            if (StringUtils.isEmpty(hostname)) {
                hostname = address.toString();
            }
        } catch (UnknownHostException ignore) {
            hostname = LOCAL_IP;
        }
        return hostname;
    }

    /**
     * 获取 服务器 HostIp
     *
     * @return HostIp
     */
    public String getHostIp() {
        String hostAddress;
        try {
            InetAddress address = NetUtils.getLocalHostLanAddress();
            // force a best effort reverse DNS lookup
            hostAddress = address.getHostAddress();
            if (StringUtils.isEmpty(hostAddress)) {
                hostAddress = address.toString();
            }
        } catch (UnknownHostException ignore) {
            hostAddress = LOCAL_IP;
        }
        return hostAddress;
    }

    /**
     * https://stackoverflow.com/questions/9481865/getting-the-ip-address-of-the-current-machine-using-java
     *
     * <p>
     * Returns an <code>InetAddress</code> object encapsulating what is most likely the machine's LAN IP address.
     * <p/>
     * This method is intended for use as a replacement of JDK method <code>InetAddress.getLocalHost</code>, because
     * that method is ambiguous on Linux systems. Linux systems enumerate the loopback network interface the same
     * way as regular LAN network interfaces, but the JDK <code>InetAddress.getLocalHost</code> method does not
     * specify the algorithm used to select the address returned under such circumstances, and will often return the
     * loopback address, which is not valid for network communication. Details
     * <a href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4665037">here</a>.
     * <p/>
     * This method will scan all IP addresses on all network interfaces on the host machine to determine the IP address
     * most likely to be the machine's LAN address. If the machine has multiple IP addresses, this method will prefer
     * a site-local IP address (e.g. 192.168.x.x or 10.10.x.x, usually IPv4) if the machine has one (and will return the
     * first site-local address if the machine has more than one), but if the machine does not hold a site-local
     * address, this method will return simply the first non-loopback address found (IPv4 or IPv6).
     * <p/>
     * If this method cannot find a non-loopback address using this selection algorithm, it will fall back to
     * calling and returning the result of JDK method <code>InetAddress.getLocalHost</code>.
     * <p/>
     *
     * @throws UnknownHostException If the LAN address of the machine cannot be found.
     */
    private InetAddress getLocalHostLanAddress() throws UnknownHostException {
        try {
            InetAddress candidateAddress = null;
            // Iterate all NICs (network interface cards)...
            for (Enumeration<NetworkInterface> iFaces = NetworkInterface.getNetworkInterfaces(); iFaces.hasMoreElements(); ) {
                NetworkInterface iFace = iFaces.nextElement();
                // Iterate all IP addresses assigned to each card...
                for (Enumeration<InetAddress> inetAdders = iFace.getInetAddresses(); inetAdders.hasMoreElements(); ) {
                    InetAddress inetAddr = inetAdders.nextElement();
                    if (!inetAddr.isLoopbackAddress()) {
                        if (inetAddr.isSiteLocalAddress()) {
                            // Found non-loopback site-local address. Return it immediately...
                            return inetAddr;
                        } else if (candidateAddress == null) {
                            // Found non-loopback address, but not necessarily site-local.
                            // Store it as a candidate to be returned if site-local address is not subsequently found...
                            candidateAddress = inetAddr;
                            // Note that we don't repeatedly assign non-loopback non-site-local addresses as candidates,
                            // only the first. For subsequent iterations, candidate will be non-null.
                        }
                    }
                }
            }
            if (candidateAddress != null) {
                // We did not find a site-local address, but we found some other non-loopback address.
                // Server might have a non-site-local address assigned to its NIC (or it might be running
                // IPv6 which deprecates the "site-local" concept).
                // Return this non-loopback candidate address...
                return candidateAddress;
            }
            // At this point, we did not find a non-loopback address.
            // Fall back to returning whatever InetAddress.getLocalHost() returns...
            InetAddress jdkSuppliedAddress = InetAddress.getLocalHost();
            if (jdkSuppliedAddress == null) {
                throw new UnknownHostException("The JDK InetAddress.getLocalHost() method unexpectedly returned null.");
            }
            return jdkSuppliedAddress;
        } catch (Exception e) {
            UnknownHostException unknownHostException = new UnknownHostException("Failed to determine LAN address: " + e);
            unknownHostException.initCause(e);
            throw unknownHostException;
        }
    }

}
