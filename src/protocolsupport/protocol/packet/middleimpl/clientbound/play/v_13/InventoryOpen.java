package protocolsupport.protocol.packet.middleimpl.clientbound.play.v_13;

import protocolsupport.protocol.packet.ClientBoundPacketType;
import protocolsupport.protocol.packet.middle.clientbound.play.MiddleInventoryOpen;
import protocolsupport.protocol.packet.middleimpl.ClientBoundPacketData;
import protocolsupport.protocol.serializer.StringSerializer;
import protocolsupport.protocol.serializer.chat.ChatSerializer;
import protocolsupport.protocol.storage.netcache.ClientCache;
import protocolsupport.protocol.typeremapper.window.WindowTypeIdMappingRegistry;
import protocolsupport.protocol.typeremapper.window.WindowTypeIdMappingRegistry.WindowTypeIdMappingTable;

public class InventoryOpen extends MiddleInventoryOpen {

	public InventoryOpen(MiddlePacketInit init) {
		super(init);
	}

	protected final ClientCache clientCache = cache.getClientCache();

	protected final WindowTypeIdMappingTable windowTypeIdMappingTable = WindowTypeIdMappingRegistry.INSTANCE.getTable(version);

	@Override
	public void writeToClient0() {
		ClientBoundPacketData windowopen = ClientBoundPacketData.create(ClientBoundPacketType.PLAY_WINDOW_OPEN);
		writeData(
			windowopen,
			windowId,
			(String) windowTypeIdMappingTable.get(windowRemapper.toClientWindowType(type)),
			ChatSerializer.serialize(version, clientCache.getLocale(), title),
			windowRemapper.toClientWindowSlots(0)
		);
		codec.writeClientbound(windowopen);
	}

	public static void writeData(ClientBoundPacketData to, int windowId, String type, String titleJson, int slots) {
		to.writeByte(windowId);
		StringSerializer.writeVarIntUTF8String(to, type);
		StringSerializer.writeVarIntUTF8String(to, titleJson);
		to.writeByte(slots);
	}

}
