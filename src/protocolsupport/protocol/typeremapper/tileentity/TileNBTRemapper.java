package protocolsupport.protocol.typeremapper.tileentity;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;

import protocolsupport.api.ProtocolVersion;
import protocolsupport.protocol.legacyremapper.LegacyEntityType;
import protocolsupport.protocol.typeremapper.itemstack.ItemStackRemapper;
import protocolsupport.protocol.utils.data.ItemData;
import protocolsupport.protocol.utils.types.Position;
import protocolsupport.utils.ProtocolVersionsHelper;
import protocolsupport.utils.Utils;
import protocolsupport.zplatform.ServerPlatform;
import protocolsupport.zplatform.itemstack.NBTTagCompoundWrapper;

@SuppressWarnings("deprecation")
public class TileNBTRemapper {

	private static final String tileEntityTypeKey = "id";

	private static final HashMap<String, String> newToOldType = new HashMap<>();
	static {
		newToOldType.put("minecraft:mob_spawner", "MobSpawner");
		newToOldType.put("minecraft:command_block", "Control");
		newToOldType.put("minecraft:beacon", "Beacon");
		newToOldType.put("minecraft:skull", "Skull");
		newToOldType.put("minecraft:flower_pot", "FlowerPot");
		newToOldType.put("minecraft:banner", "Banner");
		newToOldType.put("minecraft:structure_block", "Structure");
		newToOldType.put("minecraft:end_gateway", "Airportal");
		newToOldType.put("minecraft:sign", "Sign");
	}

	private static final EnumMap<TileEntityUpdateType, EnumMap<ProtocolVersion, List<TileEntitySpecificRemapper>>> registry = new EnumMap<>(TileEntityUpdateType.class);

	private static void register(TileEntityUpdateType type, TileEntitySpecificRemapper transformer, ProtocolVersion... versions) {
		EnumMap<ProtocolVersion, List<TileEntitySpecificRemapper>> map = Utils.getOrCreateDefault(registry, type, new EnumMap<ProtocolVersion, List<TileEntitySpecificRemapper>>(ProtocolVersion.class));
		for (ProtocolVersion version : versions) {
			Utils.getOrCreateDefault(map, version, new ArrayList<TileEntitySpecificRemapper>()).add(transformer);
		}
	}

	static {
		for (TileEntityUpdateType type : TileEntityUpdateType.values()) {
			register(
				type,
				new TileEntitySpecificRemapper() {
					public NBTTagCompoundWrapper remap(ProtocolVersion version, NBTTagCompoundWrapper input) {
						String old_name = newToOldType.get(input.getString(tileEntityTypeKey));
						if(old_name == null) old_name = "Unknown";
						input.setString(tileEntityTypeKey, old_name);
						return input;
					}
				},
				ProtocolVersionsHelper.BEFORE_1_11
			);
		}
		register(
			TileEntityUpdateType.MOB_SPAWNER,
			new TileEntitySpecificRemapper() {
				public NBTTagCompoundWrapper remap(ProtocolVersion version, NBTTagCompoundWrapper input) {
					if (!input.hasKeyOfType("SpawnData", NBTTagCompoundWrapper.TYPE_COMPOUND)) {
						NBTTagCompoundWrapper spawndata = ServerPlatform.get().getWrapperFactory().createEmptyNBTCompound();
						spawndata.setString("id", "minecraft:pig");
						input.setCompound("SpawnData", spawndata);
					}
					return input;
				}
			},
			ProtocolVersionsHelper.ALL
		);
		register(
			TileEntityUpdateType.MOB_SPAWNER,
			new TileEntitySpecificRemapper() {
				public NBTTagCompoundWrapper remap(ProtocolVersion version, NBTTagCompoundWrapper input) {
					NBTTagCompoundWrapper spawndata = input.getCompound("SpawnData");
					if (!spawndata.isNull()) {
						String mobname = spawndata.getString("id");
						if (!mobname.isEmpty()) {
							spawndata.setString("id", LegacyEntityType.getLegacyName(mobname));
						}
					}
					return input;
				}
			},
			ProtocolVersion.getAllBetween(ProtocolVersion.MINECRAFT_1_9_0, ProtocolVersion.MINECRAFT_1_10)
		);
		register(
			TileEntityUpdateType.MOB_SPAWNER,
			new TileEntitySpecificRemapper() {
				public NBTTagCompoundWrapper remap(ProtocolVersion version, NBTTagCompoundWrapper input) {
					NBTTagCompoundWrapper spawndata = input.getCompound("SpawnData");
					input.remove("SpawnPotentials");
					input.remove("SpawnData");
					if (!spawndata.isNull()) {
						String mobname = spawndata.getString("id");
						if (!mobname.isEmpty()) {
							input.setString("EntityId", LegacyEntityType.getLegacyName(mobname));
						}
					}
					return input;
				}
			},
			ProtocolVersionsHelper.BEFORE_1_9
		);
		register(
			TileEntityUpdateType.SKULL,
			new TileEntitySpecificRemapper() {
				public NBTTagCompoundWrapper remap(ProtocolVersion version, NBTTagCompoundWrapper input) {
					if (input.getNumber("SkullType") == 5) {
						input.setByte("SkullType", 3);
						input.setCompound("Owner", ItemStackRemapper.createDragonHeadSkullTag());
					}
					return input;
				}
			},
			ProtocolVersion.getAllBefore(ProtocolVersion.MINECRAFT_1_8)
		);
		register(
			TileEntityUpdateType.SKULL,
			new TileEntitySpecificRemapper() {
				public NBTTagCompoundWrapper remap(ProtocolVersion version, NBTTagCompoundWrapper input) {
					ItemStackRemapper.remapSkull(input);
					return input;
				}
			},
			ProtocolVersion.getAllBefore(ProtocolVersion.MINECRAFT_1_7_2)
		);
		register(
			TileEntityUpdateType.FLOWER_POT,
			new TileEntitySpecificRemapper() {
				public NBTTagCompoundWrapper remap(ProtocolVersion version, NBTTagCompoundWrapper input) {
					Integer id = ItemData.getIdByName(input.getString("Item"));
					if (id != null) {
						input.setInt("Item", ItemStackRemapper.ITEM_ID_REMAPPING_REGISTRY.getTable(version).getRemap(id));
					}
					return input;
				}
			},
			ProtocolVersionsHelper.BEFORE_1_8
		);
	}

	public static String getTileType(NBTTagCompoundWrapper tag) {
		return tag.getString(tileEntityTypeKey);
	}

	public static Position getPosition(NBTTagCompoundWrapper tag) {
		return new Position(tag.getNumber("x"), tag.getNumber("y"), tag.getNumber("z"));
	}

	public static String[] getSignLines(NBTTagCompoundWrapper tag) {
		String[] lines = new String[4];
		for (int i = 0; i < lines.length; i++) {
			lines[i] = tag.getString("Text" + (i + 1));
		}
		return lines;
	}

	public static NBTTagCompoundWrapper remap(ProtocolVersion version, NBTTagCompoundWrapper compound) {
		EnumMap<ProtocolVersion, List<TileEntitySpecificRemapper>> map = registry.get(TileEntityUpdateType.fromType(getTileType(compound)));
		if (map != null) {
			List<TileEntitySpecificRemapper> transformers = map.get(version);
			if (transformers != null) {
				for (TileEntitySpecificRemapper transformer : transformers) {
					compound = transformer.remap(version, compound);
				}
				return compound;
			}
		}
		return compound;
	}

}
