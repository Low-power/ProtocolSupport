package protocolsupport.protocol.packet.middle.serverbound.play;

import protocolsupport.protocol.packet.ServerBoundPacketType;
import protocolsupport.protocol.packet.middle.ServerBoundMiddlePacket;
import protocolsupport.protocol.packet.middleimpl.ServerBoundPacketData;
import protocolsupport.protocol.serializer.StringSerializer;
import protocolsupport.protocol.serializer.VarNumberSerializer;

public abstract class MiddleUpdateCommandMinecart extends ServerBoundMiddlePacket {

	protected MiddleUpdateCommandMinecart(MiddlePacketInit init) {
		super(init);
	}

	protected int entityId;
	protected String command;
	protected boolean trackOutput;

	@Override
	protected void write() {
		codec.writeServerbound(create(entityId, command, trackOutput));
	}

	public static ServerBoundPacketData create(int entityId, String command, boolean trackOutput) {
		ServerBoundPacketData updatecommandminecart = ServerBoundPacketData.create(ServerBoundPacketType.PLAY_UPDATE_COMMAND_MINECART);
		VarNumberSerializer.writeVarInt(updatecommandminecart, entityId);
		StringSerializer.writeVarIntUTF8String(updatecommandminecart, command);
		updatecommandminecart.writeBoolean(trackOutput);
		return updatecommandminecart;
	}

}
