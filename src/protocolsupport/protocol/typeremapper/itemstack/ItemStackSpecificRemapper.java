package protocolsupport.protocol.typeremapper.itemstack;

import protocolsupport.api.ProtocolVersion;
import protocolsupport.zplatform.itemstack.ItemStackWrapper;

public interface ItemStackSpecificRemapper {

	public ItemStackWrapper remap(ProtocolVersion version, ItemStackWrapper itemstack);

}
