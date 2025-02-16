package protocolsupport.protocol.packet.middleimpl.serverbound.play.v_1_7;

import org.bukkit.Material;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DecoderException;
import protocolsupport.api.ProtocolVersion;
import protocolsupport.protocol.packet.middle.serverbound.play.MiddleCustomPayload;
import protocolsupport.protocol.serializer.ByteArraySerializer;
import protocolsupport.protocol.serializer.ItemStackSerializer;
import protocolsupport.protocol.serializer.MiscSerializer;
import protocolsupport.protocol.serializer.StringSerializer;
import protocolsupport.zplatform.itemstack.ItemStackWrapper;

//TODO: Create types for cmd control data and use them to share more code
public class CustomPayload extends MiddleCustomPayload {

	private final ByteBuf newdata = Unpooled.buffer();

	@Override
	public void readFromClientData(ByteBuf clientdata, ProtocolVersion version) {
		tag = StringSerializer.readString(clientdata, version, 20);
		if (clientdata.readableBytes() > Short.MAX_VALUE) {
			throw new DecoderException("Payload may not be larger than 32767 bytes");
		}
		newdata.clear();
		ByteBuf olddata = Unpooled.wrappedBuffer(ByteArraySerializer.readByteArray(clientdata, version));
		if (tag.equals("MC|ItemName")) {
			ByteArraySerializer.writeByteArray(newdata, ProtocolVersion.getLatest(), olddata);
		} else if (tag.equals("MC|BSign") || tag.equals("MC|BEdit")) {
			ItemStackWrapper book = ItemStackSerializer.readItemStack(olddata, version);
			book.setType(Material.BOOK_AND_QUILL);
			ItemStackSerializer.writeItemStack(newdata, ProtocolVersion.getLatest(), book, false);
		} else if (tag.equals("MC|AdvCdm")) {
			tag = "MC|AdvCmd";
			newdata.writeByte(olddata.readByte());
			newdata.writeInt(olddata.readInt());
			newdata.writeInt(olddata.readInt());
			newdata.writeInt(olddata.readInt());
			StringSerializer.writeString(newdata, ProtocolVersion.getLatest(), StringSerializer.readString(olddata, version));
			newdata.writeBoolean(true);
		} else {
			newdata.writeBytes(olddata);
		}
		data = MiscSerializer.readAllBytes(newdata);
	}

}
