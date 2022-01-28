package protocolsupport.protocol.packet.middleimpl.clientbound.status.v_1_5__1_6;

import protocolsupport.api.ProtocolVersion;
import protocolsupport.protocol.packet.ClientBoundPacket;
import protocolsupport.protocol.packet.middle.clientbound.status.MiddleServerInfo;
import protocolsupport.protocol.packet.middleimpl.ClientBoundPacketData;
import protocolsupport.protocol.serializer.StringSerializer;
import protocolsupport.protocol.utils.pingresponse.PingResponse;
import protocolsupport.utils.recyclable.RecyclableCollection;
import protocolsupport.utils.recyclable.RecyclableSingletonList;

public class ServerInfo extends MiddleServerInfo {

	@Override
	public RecyclableCollection<ClientBoundPacketData> toData(ProtocolVersion version) {
		ClientBoundPacketData serializer = ClientBoundPacketData.create(ClientBoundPacket.STATUS_SERVER_INFO_ID, version);
		PingResponse ping = PingResponse.fromJson(pingJson);
		int versionId = ping.getProtocolData().getVersion();
		StringBuilder response = new StringBuilder();
		response.append("§1").append("\u0000");
		response.append(String.valueOf(versionId == ProtocolVersion.getLatest().getId() ? version.getId() : versionId)).append("\u0000");
		response.append(ping.getProtocolData().getName()).append("\u0000");
		response.append(ping.getMotd().toLegacyText()).append("\u0000");
		response.append(String.valueOf(ping.getPlayers().getOnline())).append("\u0000");
		response.append(String.valueOf(ping.getPlayers().getMax()));
		StringSerializer.writeString(serializer, version, response.toString());
		return RecyclableSingletonList.create(serializer);
	}

}
