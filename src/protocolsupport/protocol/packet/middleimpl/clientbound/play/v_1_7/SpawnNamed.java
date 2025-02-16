package protocolsupport.protocol.packet.middleimpl.clientbound.play.v_1_7;

import protocolsupport.api.ProtocolVersion;
import protocolsupport.api.events.PlayerPropertiesResolveEvent.ProfileProperty;
import protocolsupport.protocol.legacyremapper.LegacyDataWatcherSerializer;
import protocolsupport.protocol.packet.ClientBoundPacket;
import protocolsupport.protocol.packet.middle.clientbound.play.MiddleSpawnNamed;
import protocolsupport.protocol.packet.middleimpl.ClientBoundPacketData;
import protocolsupport.protocol.serializer.StringSerializer;
import protocolsupport.protocol.serializer.VarNumberSerializer;
import protocolsupport.protocol.typeremapper.watchedentity.WatchedDataRemapper;
import protocolsupport.utils.recyclable.RecyclableCollection;
import protocolsupport.utils.recyclable.RecyclableSingletonList;

public class SpawnNamed extends MiddleSpawnNamed {

	@Override
	public RecyclableCollection<ClientBoundPacketData> toData(ProtocolVersion version) {
		ClientBoundPacketData serializer = ClientBoundPacketData.create(ClientBoundPacket.PLAY_SPAWN_NAMED_ID, version);
		VarNumberSerializer.writeVarInt(serializer, playerEntityId);
		StringSerializer.writeString(serializer, version, version == ProtocolVersion.MINECRAFT_1_7_6 ? uuid.toString() : uuid.toString().replace("-", ""));
		StringSerializer.writeString(serializer, version, name);
		if (version == ProtocolVersion.MINECRAFT_1_7_6) {
			VarNumberSerializer.writeVarInt(serializer, properties.size());
			for (ProfileProperty property : properties) {
				StringSerializer.writeString(serializer, version, property.getName());
				StringSerializer.writeString(serializer, version, property.getValue());
				StringSerializer.writeString(serializer, version, property.getSignature());
			}
		}
		serializer.writeInt((int) (x * 32));
		serializer.writeInt((int) (y * 32));
		serializer.writeInt((int) (z * 32));
		serializer.writeByte(yaw);
		serializer.writeByte(pitch);
		serializer.writeShort(0);
		LegacyDataWatcherSerializer.encodeData(serializer, version, WatchedDataRemapper.transform(cache, playerEntityId, metadata, version));
		return RecyclableSingletonList.create(serializer);
	}

}
