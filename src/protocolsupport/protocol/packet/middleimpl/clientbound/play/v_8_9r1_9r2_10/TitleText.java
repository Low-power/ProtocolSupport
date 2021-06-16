package protocolsupport.protocol.packet.middleimpl.clientbound.play.v_8_9r1_9r2_10;

import protocolsupport.protocol.packet.ClientBoundPacketType;
import protocolsupport.protocol.packet.middle.clientbound.play.MiddleTitleText;
import protocolsupport.protocol.packet.middleimpl.ClientBoundPacketData;
import protocolsupport.protocol.serializer.StringSerializer;
import protocolsupport.protocol.serializer.VarNumberSerializer;
import protocolsupport.protocol.serializer.chat.ChatSerializer;
import protocolsupport.protocol.storage.netcache.ClientCache;

public class TitleText extends MiddleTitleText {

	public TitleText(MiddlePacketInit init) {
		super(init);
	}

	protected final ClientCache clientCache = cache.getClientCache();

	@Override
	protected void write() {
		ClientBoundPacketData titletextPacket = ClientBoundPacketData.create(ClientBoundPacketType.PLAY_TITLE_TEXT);
		VarNumberSerializer.writeVarInt(titletextPacket, 0); //legacy title action (0 - set main text)
		StringSerializer.writeVarIntUTF8String(titletextPacket, ChatSerializer.serialize(version, clientCache.getLocale(), text));
		codec.writeClientbound(titletextPacket);
	}

}
