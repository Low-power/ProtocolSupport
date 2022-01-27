package protocolsupport.protocol.packet.middleimpl.clientbound.play.v_1_4__1_5__1_6;

import protocolsupport.api.ProtocolVersion;
import protocolsupport.protocol.packet.ClientBoundPacket;
import protocolsupport.protocol.packet.middle.clientbound.play.MiddleTabComplete;
import protocolsupport.protocol.packet.middleimpl.ClientBoundPacketData;
import protocolsupport.protocol.serializer.StringSerializer;
import protocolsupport.utils.Utils;
import protocolsupport.utils.recyclable.RecyclableCollection;
import protocolsupport.utils.recyclable.RecyclableEmptyList;
import protocolsupport.utils.recyclable.RecyclableSingletonList;

public class TabComplete extends MiddleTabComplete {

	@Override
	public RecyclableCollection<ClientBoundPacketData> toData(ProtocolVersion version) {
		if (matches.length == 0) {
			return RecyclableEmptyList.get();
		}
		ClientBoundPacketData serializer = ClientBoundPacketData.create(ClientBoundPacket.PLAY_TAB_COMPLETE_ID, version);
		StringBuilder s = new StringBuilder();
		for(int i = 0; i < matches.length && s.length() < Short.MAX_VALUE; i++) {
			if(i > 0) s.append("\u0000");
			s.append(matches[i]);
		}
		if(s.length() > Short.MAX_VALUE) s.setLength(Short.MAX_VALUE);
		StringSerializer.writeString(serializer, version, s.toString());
		return RecyclableSingletonList.create(serializer);
	}

}
