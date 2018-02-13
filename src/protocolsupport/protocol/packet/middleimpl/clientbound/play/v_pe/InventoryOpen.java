package protocolsupport.protocol.packet.middleimpl.clientbound.play.v_pe;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import protocolsupport.api.Connection;
import protocolsupport.api.ProtocolVersion;
import protocolsupport.protocol.packet.middle.clientbound.play.MiddleInventoryOpen;
import protocolsupport.protocol.packet.middleimpl.ClientBoundPacketData;
import protocolsupport.protocol.serializer.ItemStackSerializer;
import protocolsupport.protocol.serializer.MiscSerializer;
import protocolsupport.protocol.serializer.PositionSerializer;
import protocolsupport.protocol.serializer.VarNumberSerializer;
import protocolsupport.protocol.typeremapper.id.IdRemapper;
import protocolsupport.protocol.typeremapper.pe.PEInventory.InvBlock;
import protocolsupport.protocol.typeremapper.pe.PEPacketIDs;
import protocolsupport.protocol.utils.minecraftdata.PocketData;
import protocolsupport.protocol.utils.minecraftdata.PocketData.PocketEntityData;
import protocolsupport.protocol.utils.types.NetworkEntity;
import protocolsupport.protocol.utils.types.NetworkEntityType;
import protocolsupport.protocol.utils.types.Position;
import protocolsupport.protocol.utils.types.WindowType;
import protocolsupport.utils.recyclable.RecyclableArrayList;
import protocolsupport.utils.recyclable.RecyclableCollection;
import protocolsupport.zplatform.ServerPlatform;
import protocolsupport.zplatform.itemstack.ItemStackWrapper;
import protocolsupport.zplatform.itemstack.NBTTagCompoundWrapper;
import protocolsupport.zplatform.itemstack.NBTTagListWrapper;

public class InventoryOpen extends MiddleInventoryOpen {
	
	@Override
	public RecyclableCollection<ClientBoundPacketData> toData() {
		cache.getInfTransactions().clear();
		if (type == WindowType.HORSE) {
			
			//TODO: Fix this shit. Horses are a pain in the ass and require a different packer. Lama's are even worse with their variable slots. We'll see.
			NetworkEntity horse = cache.getWatchedEntity(horseId);
			if (horse != null) {
				PocketEntityData horseTypeData = PocketData.getPocketEntityData(horse.getType());
				if (horseTypeData != null && horseTypeData.getInventoryFilter() != null) {
					NBTTagCompoundWrapper filter = horseTypeData.getInventoryFilter().getFilter().clone();
					if (horse.getType() == NetworkEntityType.LAMA) {
						NBTTagListWrapper newSlots = filter.getList("slots");
						for(int i = 0; i < (horse.getDataCache().getStrength() * 3); i++) {
							NBTTagCompoundWrapper slot = ServerPlatform.get().getWrapperFactory().createEmptyNBTCompound();
							NBTTagCompoundWrapper item = ServerPlatform.get().getWrapperFactory().createEmptyNBTCompound();
							item.setByte("Count", 1);
							item.setShort("Damage", 0);
							item.setShort("id", 0);
							slot.setCompound("item", item);
							slot.setInt("slotNumber", i+2);
							newSlots.addCompound(slot);
						}
						filter.setList("slots", newSlots);
					}
					RecyclableArrayList<ClientBoundPacketData> packets = RecyclableArrayList.create();
					packets.add(openEquipment(connection.getVersion(), 2, type, horseId, filter));
					packets.add(InventorySetItems.create(connection.getVersion(), cache.getLocale(), 2, new ItemStackWrapper[] {ItemStackWrapper.NULL, ItemStackWrapper.NULL}));
					//return RecyclableSingletonList.create(openEquipment(connection.getVersion(), 2, type, horseId, filter));
					return packets;
				}
			}
			
		}
		RecyclableArrayList<ClientBoundPacketData> packets = RecyclableArrayList.create();
		if (connection.hasMetadata("peInvBlocks")) {
			InvBlock[] blocks = (InvBlock[]) connection.getMetadata("peInvBlocks");
			packets.addAll(InvBlock.prepareFakeInventory(connection.getVersion(), cache.getLocale(), blocks, type, title, cache.getOpenedWindowSlots()));
			if (
				(type == WindowType.CHEST) &&
				(cache.getOpenedWindowSlots() > 27)
			) {
				System.out.println("Smuggling double chest data: " + windowId);
				//When it is a doublechest, re-smuggle the windowId back to the metadata.
				connection.addMetadata("smuggledWindowId", windowId);
			} else {
				//Only double chests need some time to verify on the client (FFS Mojang!), the rest can be instantly opened after preparing.
				packets.add(create(connection.getVersion(), windowId, type, blocks[0].getPosition(), -1));
			}
		}
		return packets;
	}
	
	public static ClientBoundPacketData create(ProtocolVersion version, int windowId, WindowType type, Position pePosition, int horseId) {
		System.out.println("Opening " + type + " inventory: " + windowId);
		return (ClientBoundPacketData) serialize(ClientBoundPacketData.create(PEPacketIDs.CONTAINER_OPEN, version), version, windowId, type, pePosition, horseId);
	}
	
	public static ClientBoundPacketData openEquipment(ProtocolVersion version, int windowId, WindowType type, long entityId, NBTTagCompoundWrapper nbt) {
		ClientBoundPacketData serializer = ClientBoundPacketData.create(PEPacketIDs.EQUIPMENT, version);
		serializer.writeByte(windowId);
		serializer.writeByte(IdRemapper.WINDOWTYPE.getTable(version).getRemap(type.toLegacyId()));
		VarNumberSerializer.writeSVarInt(serializer, 0);
		VarNumberSerializer.writeSVarLong(serializer, entityId);
		System.out.println("OPEN EQ - Eid: "+ entityId + "wId: " + windowId + " type: " + IdRemapper.WINDOWTYPE.getTable(version).getRemap(type.toLegacyId()) +  " Tag: " + nbt);
		ItemStackSerializer.writeTag(serializer, true, version, nbt);
		return serializer;
	}
	
	public static void sendInventory(Connection connection, int windowId, WindowType type, Position pePosition, int horseId) {
		System.out.println("Opening " + type + " inventory: " + windowId);
		ByteBuf serializer = Unpooled.buffer();
		VarNumberSerializer.writeVarInt(serializer, PEPacketIDs.CONTAINER_OPEN);
		serializer.writeByte(0);
		serializer.writeByte(0);
		serialize(serializer, connection.getVersion(), windowId, type, pePosition, horseId);
		connection.sendRawPacket(MiscSerializer.readAllBytes(serializer));
	}
	
	private static ByteBuf serialize(ByteBuf serializer, ProtocolVersion version, int windowId, WindowType type, Position pePosition, int horseId) {
		serializer.writeByte(windowId);
		serializer.writeByte(IdRemapper.WINDOWTYPE.getTable(version).getRemap(type.toLegacyId()));
		PositionSerializer.writePEPosition(serializer, pePosition);
		VarNumberSerializer.writeSVarLong(serializer, horseId);
		return serializer;
	}

}