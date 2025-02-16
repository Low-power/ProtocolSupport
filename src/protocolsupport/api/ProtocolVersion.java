package protocolsupport.api;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;
import java.util.Iterator;
import gnu.trove.map.hash.TIntObjectHashMap;

public enum ProtocolVersion {
	UNKNOWN(-1),
	MINECRAFT_LEGACY(-1),
	MINECRAFT_1_4_6(false, 51, "1.4.7"),
	MINECRAFT_1_5_0(false, 60, "1.5.1"),
	MINECRAFT_2_0_BLUE(false, 90, "2.0 blue"),
	MINECRAFT_2_0_RED(false, 91, "2.0 red"),
	MINECRAFT_2_0_PURPLE(false, 92, "2.0 purple"),
	MINECRAFT_1_5_2(false, 61, "1.5.2"),
	MINECRAFT_1_6_1(false, 73, "1.6.1"),
	MINECRAFT_1_6_2(false, 74, "1.6.2"),
	MINECRAFT_1_6_4(false, 78, "1.6.4"),
	MINECRAFT_1_7_2(true, 4, "1.7.5"),
	MINECRAFT_1_7_6(true, 5, "1.7.10"),
	MINECRAFT_1_8(true, 47, "1.8"),
	MINECRAFT_1_9_0(true, 107, "1.9"),
	MINECRAFT_1_9_1(true, 108, "1.9.1"),
	MINECRAFT_1_9_2(true, 109, "1.9.2"),
	MINECRAFT_1_9_4(true, 110, "1.9.4"),
	MINECRAFT_1_10(true, 210, "1.10"),
	MINECRAFT_1_11(true, 315, "1.11"),
	MINECRAFT_1_11_1(true, 316, "1.11.2"),
	MINECRAFT_FUTURE(-1);

	public static final ProtocolVersion MINECRAFT_1_4_7 = MINECRAFT_1_4_6;
	public static final ProtocolVersion MINECRAFT_1_5_1 = MINECRAFT_1_5_0;
	public static final ProtocolVersion MINECRAFT_1_7_5 = MINECRAFT_1_7_2;
	public static final ProtocolVersion MINECRAFT_1_7_10 = MINECRAFT_1_7_6;
	public static final ProtocolVersion MINECRAFT_1_9 = MINECRAFT_1_9_0;

	private boolean is_modern_protocol;
	private final int id;
	private final String name;

	ProtocolVersion(int id) {
		this(false, id, null);
	}

	ProtocolVersion(boolean is_modern_protocol, int id, String name) {
		this.is_modern_protocol = is_modern_protocol;
		this.id = id;
		this.name = name;
	}

	/**
	 * Returns whether this protocol version is after-Netty-rewrite since 13w41a.
	 * @return true if this version is used by Minecraft 13w41a or later
	 */
	public boolean isModernProtocol() {
		return is_modern_protocol;
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
		return ordinal() > another.ordinal();
	}

	/**
	 * Returns if the game version used by this protocol released after (or is the same) the game version used by another protocol version
	 * @param another another protocol version
	 * @return true if game version is released after (or is the same) the game version used by another protocol version
	 * @throws IllegalArgumentException if protocol versions use different protocol types
	 */
	public boolean isAfterOrEq(ProtocolVersion another) {
		return ordinal() >= another.ordinal();
	}

	/**
	 * Returns if the game version used by this protocol released before the game version used by another protocol version
	 * @param another another protocol version
	 * @return true if game version is released before the game version used by another protocol version
	 * @throws IllegalArgumentException if protocol versions use different protocol types
	 */
	public boolean isBefore(ProtocolVersion another) {
		return ordinal() < another.ordinal();
	}

	/**
	 * Returns if the game version used by this protocol released before (or is the same) the game version used by another protocol version
	 * @param another another protocol version
	 * @return true if game version is released before (or is the same) the game version used by another protocol version
	 * @throws IllegalArgumentException if protocol versions use different protocol types
	 */
	public boolean isBeforeOrEq(ProtocolVersion another) {
		return ordinal() <= another.ordinal();
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

	private static final TIntObjectHashMap<ProtocolVersion> modern_versions_by_number = new TIntObjectHashMap<>();
	private static final TIntObjectHashMap<ProtocolVersion> legacy_versions_by_number = new TIntObjectHashMap<>();
	static {
		for(ProtocolVersion version : ProtocolVersion.values()) {
			if(!version.isSupported()) continue;
			if(version.isModernProtocol()) {
				modern_versions_by_number.put(version.id, version);
			} else {
				legacy_versions_by_number.put(version.id, version);
			}
		}
	}

	/**
	 * Get ProtocolVersion by network protocol version number
	 * @param is_modern_protocol specific whether it is a modern protocol version
	 * @param number the protocol version number
	 * @return Returns protocol version or UNKNOWN if not found
	 */
	public static ProtocolVersion fromProtocolVersionNumber(boolean is_modern_protocol, int number) {
		ProtocolVersion version =
			(is_modern_protocol ? modern_versions_by_number : legacy_versions_by_number).
				get(number);
		return version != null ? version : UNKNOWN;
	}

	/**
	 * Returns protocol version by network game id
	 * @param id network version id
	 * @return Returns protocol version by network game id or UNKNOWN if not found
	 * @deprecated legacy and modern protocol versions may share same numeric IDs
	 */
	@Deprecated
	public static ProtocolVersion fromId(int id) {
		return fromProtocolVersionNumber(true, id);
	}

	/**
	 * Returns protocol version that is used by the game version released after game version used by this protocol
	 * Returns null if next game version doesn't exist
	 * @return protocol version that is used by the game version released after game version used by this protocol
	 * @throws IllegalArgumentException if protocol type is UNKNOWN
	 */
	public ProtocolVersion next() {
		ProtocolVersion[] versions = values();
		int nextVersionOrderId = ordinal() + 1;
		if (nextVersionOrderId < versions.length) {
			return versions[nextVersionOrderId];
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
		ProtocolVersion[] versions = values();
		int previousVersionOrderId = ordinal() - 1;
		if (previousVersionOrderId >= 0) {
			return versions[previousVersionOrderId];
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
		ProtocolVersion[] versions = values();
		int startId = Math.min(one.ordinal(), another.ordinal());
		int endId = Math.max(one.ordinal(), another.ordinal());
		ProtocolVersion[] between = new ProtocolVersion[(endId - startId) + 1];
		for (int i = startId; i <= endId; i++) {
			between[i - startId] = versions[i];
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
		return MINECRAFT_1_4_6;
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

}
