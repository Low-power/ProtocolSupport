package protocolsupport.api;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;
import java.util.Iterator;
import org.apache.commons.lang3.Validate;

import gnu.trove.map.hash.TIntObjectHashMap;

public enum ProtocolVersion {

	MINECRAFT_FUTURE(-1, new OrderId(17)),
	MINECRAFT_1_11_1(316, new OrderId(16), "1.11.2"),
	MINECRAFT_1_11(315, new OrderId(15), "1.11"),
	MINECRAFT_1_10(210, new OrderId(14), "1.10"),
	MINECRAFT_1_9_4(110, new OrderId(13), "1.9.4"),
	MINECRAFT_1_9_2(109, new OrderId(12), "1.9.2"),
	MINECRAFT_1_9_1(108, new OrderId(11), "1.9.1"),
	MINECRAFT_1_9(107, new OrderId(10), "1.9"),
	MINECRAFT_1_8(47, new OrderId(9), "1.8"),
	MINECRAFT_1_7_10(5, new OrderId(8), "1.7.10"),
	MINECRAFT_1_7_5(4, new OrderId(7), "1.7.5"),
	MINECRAFT_1_6_4(78, new OrderId(6), "1.6.4"),
	MINECRAFT_1_6_2(74, new OrderId(5), "1.6.2"),
	MINECRAFT_1_6_1(73, new OrderId(4), "1.6.1"),
	MINECRAFT_1_5_2(61, new OrderId(3), "1.5.2"),
	MINECRAFT_1_5_1(60, new OrderId(2), "1.5.1"),
	MINECRAFT_1_4_7(51, new OrderId(1), "1.4.7"),
	MINECRAFT_LEGACY(-1, new OrderId(0)),
	UNKNOWN(-1, new OrderId(-1));

	private final int id;
	private final OrderId orderId;
	private final String name;

	ProtocolVersion(int id, OrderId orderid) {
		this(id, orderid, null);
	}

	ProtocolVersion(int id, OrderId orderId, String name) {
		this.id = id;
		this.orderId = orderId;
		this.name = name;
	}

	/**
	 * Returns the network version id of this protocol version
	 * @return network id of this protocol version
	 */
	public int getId() {
		return id;
	}

	/**
	 * Returns user friendly version name
	 * Notice: This name can change, so it shouldn't be used as a key anywhere
	 * @return user friendly version name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns if this version is supported as game version (i.e.: player can join and play on the server)
	 * @return true if this protocol version is supported
	 */
	public boolean isSupported() {
		return name != null;
	}

	/**
	 * Returns if the game version used by this protocol released after the game version used by another protocol version
	 * @param another another protocol version
	 * @return true if game version is released after the game version used by another protocol version
	 * @throws IllegalArgumentException if protocol versions use different protocol types
	 */
	public boolean isAfter(ProtocolVersion another) {
		return orderId.compareTo(another.orderId) > 0;
	}

	/**
	 * Returns if the game version used by this protocol released after (or is the same) the game version used by another protocol version
	 * @param another another protocol version
	 * @return true if game version is released after (or is the same) the game version used by another protocol version
	 * @throws IllegalArgumentException if protocol versions use different protocol types
	 */
	public boolean isAfterOrEq(ProtocolVersion another) {
		return orderId.compareTo(another.orderId) >= 0;
	}

	/**
	 * Returns if the game version used by this protocol released before the game version used by another protocol version
	 * @param another another protocol version
	 * @return true if game version is released before the game version used by another protocol version
	 * @throws IllegalArgumentException if protocol versions use different protocol types
	 */
	public boolean isBefore(ProtocolVersion another) {
		return orderId.compareTo(another.orderId) < 0;
	}

	/**
	 * Returns if the game version used by this protocol released before (or is the same) the game version used by another protocol version
	 * @param another another protocol version
	 * @return true if game version is released before (or is the same) the game version used by another protocol version
	 * @throws IllegalArgumentException if protocol versions use different protocol types
	 */
	public boolean isBeforeOrEq(ProtocolVersion another) {
		return orderId.compareTo(another.orderId) <= 0;
	}

	/**
	 * Returns if the game version used by this protocol released in between (or is the same) of other game versions used by others protocol versions
	 * @param one one protocol version
	 * @param another another protocol version
	 * @return true if game version is released before (or is the same) the game version used by another protocol version
	 * @throws IllegalArgumentException if protocol versions use different protocol types
	 */
	public boolean isBetween(ProtocolVersion one, ProtocolVersion another) {
		return (isAfterOrEq(one) && isBeforeOrEq(another)) || (isBeforeOrEq(one) && isAfterOrEq(another));
	}

	private static final TIntObjectHashMap<ProtocolVersion> byProtocolId = new TIntObjectHashMap<>();
	static {
		for(ProtocolVersion version : ProtocolVersion.values()) {
			if(!version.isSupported()) continue;
			byProtocolId.put(version.id, version);
		}
	}

	/**
	 * Returns protocol version by network game id
	 * @param id network version id
	 * @return Returns protocol version by network game id or UNKNOWN if not found
	 * @deprecated network version ids may be the same for different protocol versions
	 */
	@Deprecated
	public static ProtocolVersion fromId(int id) {
		ProtocolVersion version = byProtocolId.get(id);
		return version != null ? version : UNKNOWN;
	}

	private static final ProtocolVersion[] sorted_versions;
	static {
		//List<ProtocolVersion> sorted_version_list = new ArrayList<>(Arrays.asList(values()));
		//List<ProtocolVersion> sorted_version_list = Arrays.asList(values());
		ProtocolVersion[] versions = values();
		List<ProtocolVersion> sorted_version_list = new ArrayList<>(versions.length);
		for(ProtocolVersion v : versions) {
			if(v.orderId.id < 0) continue;
			sorted_version_list.add(v);
		}
		Collections.sort(sorted_version_list, new Comparator<ProtocolVersion>() {
				public int compare(ProtocolVersion v1, ProtocolVersion v2) {
					return v1.orderId.compareTo(v2.orderId);
				}
			});
		sorted_versions = sorted_version_list.toArray(new ProtocolVersion[0]);
	}

	/**
	 * Returns protocol version that is used by the game version released after game version used by this protocol
	 * Returns null if next game version doesn't exist
	 * @return protocol version that is used by the game version released after game version used by this protocol
	 * @throws IllegalArgumentException if protocol type is UNKNOWN
	 */
	public ProtocolVersion next() {
		int nextVersionOrderId = orderId.id + 1;
		if (nextVersionOrderId < sorted_versions.length) {
			return sorted_versions[nextVersionOrderId];
		} else {
			return null;
		}
	}

	/**
	 * Returns protocol version that is used by the game version released before game version used by this protocol
	 * Returns null if previous game version doesn't exist
	 * @return protocol version that is used by the game version released before game version used by this protocol
	 * @throws IllegalArgumentException if protocol type is UNKNOWN
	 */
	public ProtocolVersion previous() {
		int previousVersionOrderId = orderId.id - 1;
		if (previousVersionOrderId >= 0) {
			return sorted_versions[previousVersionOrderId];
		} else {
			return null;
		}
	}

	/**
	 * Returns all protocol versions that are between specified ones (inclusive)
	 * Throws {@link IllegalArgumentException} if protocol versions types are not the same or one of the types is UNKNOWN
	 * @param one one protocol version
	 * @param another one protocol version
	 * @return all protocol versions that are between specified ones (inclusive)
	 */
	public static ProtocolVersion[] getAllBetween(ProtocolVersion one, ProtocolVersion another) {
		int startId = Math.min(one.orderId.id, another.orderId.id);
		int endId = Math.max(one.orderId.id, another.orderId.id);
		ProtocolVersion[] between = new ProtocolVersion[(endId - startId) + 1];
		for (int i = startId; i <= endId; i++) {
			between[i - startId] = sorted_versions[i];
		}
		return between;
	}

	/**
	 * Returns latest supported protocol version for specified protocol type
	 * @param type protocol type
	 * @return latest supported protocol version for specified protocol type
	 * @throws IllegalArgumentException if protocol type has not supported protocol versions
	 */
	public static ProtocolVersion getLatest() {
		return MINECRAFT_1_11_1;
	}

	/**
	 * Returns oldest supported protocol version for specified protocol type
	 * @param type protocol type
	 * @return oldest supported protocol version for specified protocol type
	 * @throws IllegalArgumentException if protocol type has not supported protocol versions
	 */
	public static ProtocolVersion getOldest() {
		return MINECRAFT_1_4_7;
	}

	/**
	 * Returns all protocol versions that are after specified one (inclusive)
	 * @param version protocol version
	 * @return all protocol versions that are after specified one  (inclusive)
	 * @throws IllegalArgumentException  if getAllBetween(version, getLatest(version.getType())) throws one
	 * @deprecated non intuitive behavior
	 */
	@Deprecated
	public static ProtocolVersion[] getAllAfter(ProtocolVersion version) {
		return getAllBetween(version, getLatest());
	}

	/**
	 * Returns all protocol versions that are before specified one (inclusive)
	 * @param version protocol version
	 * @return all protocol versions that are before specified one
	 * @throws IllegalArgumentException if getAllBetween(getOldest(version.getType()), version) throws one
	 * @deprecated non intuitive behavior
	 */
	@Deprecated
	public static ProtocolVersion[] getAllBefore(ProtocolVersion version) {
		return getAllBetween(getOldest(), version);
	}

	private static class OrderId implements Comparable<OrderId> {

		private final int id;

		public OrderId(int id) {
			this.id = id;
		}

		@Override
		public int compareTo(OrderId o) {
			return Integer.compare(id, o.id);
		}

	}

}
