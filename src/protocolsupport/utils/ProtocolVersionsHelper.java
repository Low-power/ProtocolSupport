package protocolsupport.utils;

import java.util.ArrayList;
import java.util.Arrays;
import protocolsupport.api.ProtocolVersion;

public class ProtocolVersionsHelper {

	public static final ProtocolVersion[] BEFORE_1_5 = new ProtocolVersion[] {
		ProtocolVersion.MINECRAFT_1_4_6
	};

	public static final ProtocolVersion[] BEFORE_1_6 = concat(
		BEFORE_1_5,
		ProtocolVersion.MINECRAFT_2_0_PURPLE, ProtocolVersion.MINECRAFT_2_0_RED, ProtocolVersion.MINECRAFT_2_0_BLUE,
		ProtocolVersion.MINECRAFT_1_5_2, ProtocolVersion.MINECRAFT_1_5_0
	);

	public static final ProtocolVersion[] BEFORE_1_7 = concat(
		BEFORE_1_6,
		ProtocolVersion.MINECRAFT_1_6_1, ProtocolVersion.MINECRAFT_1_6_2, ProtocolVersion.MINECRAFT_1_6_4
	);

	public static final ProtocolVersion[] BEFORE_1_8 = concat(
		BEFORE_1_7,
		ProtocolVersion.MINECRAFT_1_7_2, ProtocolVersion.MINECRAFT_1_7_6
	);

	public static final ProtocolVersion[] BEFORE_1_9 = concat(BEFORE_1_8, ProtocolVersion.MINECRAFT_1_8);

	public static final ProtocolVersion[] BEFORE_1_9_1 = concat(BEFORE_1_9, ProtocolVersion.MINECRAFT_1_9_0);

	public static final ProtocolVersion[] BEFORE_1_10 = concat(BEFORE_1_9_1, ProtocolVersion.MINECRAFT_1_9_2, ProtocolVersion.MINECRAFT_1_9_4);

	public static final ProtocolVersion[] BEFORE_1_11 = concat(BEFORE_1_10, ProtocolVersion.MINECRAFT_1_10);

	public static final ProtocolVersion[] BEFORE_1_11_1 = concat(BEFORE_1_11, ProtocolVersion.MINECRAFT_1_11);

	public static final ProtocolVersion[] ALL = concat(BEFORE_1_11_1, ProtocolVersion.MINECRAFT_1_11_1);

	public static final ProtocolVersion[] ALL_1_11 = ProtocolVersion.getAllBetween(ProtocolVersion.MINECRAFT_1_11, ProtocolVersion.MINECRAFT_1_11_1);

	public static final ProtocolVersion[] ALL_1_9 = ProtocolVersion.getAllBetween(ProtocolVersion.MINECRAFT_1_9_4, ProtocolVersion.MINECRAFT_1_9_0);

	public static final ProtocolVersion[] RANGE__1_9__1_11 = ProtocolVersion.getAllBetween(ProtocolVersion.MINECRAFT_1_9_0, ProtocolVersion.MINECRAFT_1_11_1);

	public static final ProtocolVersion[] RANGE__1_10__1_11 = ProtocolVersion.getAllBetween(ProtocolVersion.MINECRAFT_1_10, ProtocolVersion.MINECRAFT_1_11_1);

	public static final ProtocolVersion[] RANGE__1_6__1_7 = ProtocolVersion.getAllBetween(ProtocolVersion.MINECRAFT_1_7_6, ProtocolVersion.MINECRAFT_1_6_1);

	public static final ProtocolVersion[] RANGE__1_8__1_9 = ProtocolVersion.getAllBetween(ProtocolVersion.MINECRAFT_1_9_4, ProtocolVersion.MINECRAFT_1_8);

	public static final ProtocolVersion[] RANGE__1_6__1_10 = ProtocolVersion.getAllBetween(ProtocolVersion.MINECRAFT_1_6_1, ProtocolVersion.MINECRAFT_1_10);

	public static final ProtocolVersion[] AFTER_1_8 = ProtocolVersion.getAllBetween(ProtocolVersion.MINECRAFT_1_9_0, ProtocolVersion.getLatest());

	public static final ProtocolVersion[] concat(ProtocolVersion[] versions, ProtocolVersion... moreVersions) {
		ArrayList<ProtocolVersion> all = new ArrayList<>();
		all.addAll(Arrays.asList(versions));
		all.addAll(Arrays.asList(moreVersions));
		return all.toArray(new ProtocolVersion[all.size()]);
	}

}
