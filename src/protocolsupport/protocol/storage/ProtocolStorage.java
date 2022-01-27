package protocolsupport.protocol.storage;

import java.net.SocketAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import protocolsupport.protocol.ConnectionImpl;

//TODO: guard with single rw lock because otherwise after moving handshake processing to separate executor race conditions may occur
public class ProtocolStorage {

	private static final Map<SocketAddress, Data> primaryStorage = new ConcurrentHashMap<>(1000);
	private static final Map<SocketAddress, ConnectionImpl> secondayStorage = new ConcurrentHashMap<>(2000);

	public static final void addConnection(SocketAddress address, ConnectionImpl connection) {
		primaryStorage.put(address, new Data(connection));
		secondayStorage.put(address, connection);
	}

	public static final void addAddress(SocketAddress primary, SocketAddress additional) {
		Data dataentry = primaryStorage.get(primary);
		dataentry.addresses.add(additional);
		secondayStorage.put(additional, dataentry.connection);
	}

	public static ConnectionImpl getConnection(SocketAddress address) {
		return secondayStorage.get(address);
	}

	public static ConnectionImpl removeConnection(SocketAddress address) {
		Data dataentry = primaryStorage.remove(address);
		for (SocketAddress aaddr : dataentry.addresses) {
			secondayStorage.remove(aaddr);
		}
		secondayStorage.remove(address);
		return dataentry.connection;
	}

	public static Collection<ConnectionImpl> getConnections() {
		Collection<Data> data_collection = primaryStorage.values();
		ArrayList<ConnectionImpl> connections = new ArrayList<>(data_collection.size());
		for(Data data : data_collection) connections.add(data.connection);
		return connections;
	}

	private static class Data {
		private final ConnectionImpl connection;
		private final Set<SocketAddress> addresses = Collections.newSetFromMap(new ConcurrentHashMap<SocketAddress, Boolean>());
		public Data(ConnectionImpl connection) {
			this.connection = connection;
		}
	}

}
