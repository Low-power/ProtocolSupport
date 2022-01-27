package protocolsupport.zplatform.impl.glowstone;

import java.io.IOException;
import java.io.InputStream;

import org.bukkit.Material;

import protocolsupport.protocol.packet.handler.AbstractHandshakeListener;
import protocolsupport.protocol.packet.handler.AbstractLoginListener;
import protocolsupport.protocol.packet.handler.AbstractStatusListener;
import protocolsupport.zplatform.PlatformWrapperFactory;
import protocolsupport.zplatform.impl.glowstone.itemstack.GlowStoneItemStackWrapper;
import protocolsupport.zplatform.impl.glowstone.itemstack.GlowStoneNBTTagCompoundWrapper;
import protocolsupport.zplatform.impl.glowstone.itemstack.GlowStoneNBTTagListWrapper;
import protocolsupport.zplatform.impl.glowstone.network.handler.GlowStoneLegacyLoginListener;
import protocolsupport.zplatform.impl.glowstone.network.handler.GlowStoneModernLoginListener;
import protocolsupport.zplatform.itemstack.ItemStackWrapper;
import protocolsupport.zplatform.itemstack.NBTTagCompoundWrapper;
import protocolsupport.zplatform.itemstack.NBTTagListWrapper;
import protocolsupport.zplatform.network.NetworkManagerWrapper;

public class GlowStoneWrapperFactory implements PlatformWrapperFactory {

	@Override
	public NBTTagListWrapper createEmptyNBTList() {
		return GlowStoneNBTTagListWrapper.create();
	}

	@Override
	public NBTTagCompoundWrapper createNBTCompoundFromJson(String json) {
		return GlowStoneNBTTagCompoundWrapper.fromJson(json);
	}

	@Override
	public NBTTagCompoundWrapper createNBTCompoundFromStream(InputStream in) throws IOException {
		return GlowStoneNBTTagCompoundWrapper.fromStream(in);
	}

	@Override
	public NBTTagCompoundWrapper createEmptyNBTCompound() {
		return GlowStoneNBTTagCompoundWrapper.createEmpty();
	}

	@Override
	public NBTTagCompoundWrapper createNullNBTCompound() {
		return GlowStoneNBTTagCompoundWrapper.createNull();
	}

	@Override
	public ItemStackWrapper createNullItemStack() {
		return GlowStoneItemStackWrapper.createNull();
	}

	@SuppressWarnings("deprecation")
	@Override
	public ItemStackWrapper createItemStack(Material material) {
		return GlowStoneItemStackWrapper.create(material.getId());
	}

	@Override
	public ItemStackWrapper createItemStack(int typeId) {
		return GlowStoneItemStackWrapper.create(typeId);
	}

	@Override
	public AbstractHandshakeListener createModernHandshakeListener(final NetworkManagerWrapper networkmanager, final boolean hasCompression) {
		return new AbstractHandshakeListener(networkmanager) {
			@Override
			protected AbstractStatusListener getStatusListener(NetworkManagerWrapper networkManager) {
				return new AbstractStatusListener(networkmanager) {
				};
			}
			@Override
			protected AbstractLoginListener getLoginListener(NetworkManagerWrapper networkManager, String hostname) {
				return new GlowStoneModernLoginListener(networkmanager, hostname, hasCompression);
			}
		};
	}

	@Override
	public AbstractHandshakeListener createLegacyHandshakeListener(final NetworkManagerWrapper networkmanager) {
		return new AbstractHandshakeListener(networkmanager) {
			@Override
			protected AbstractStatusListener getStatusListener(NetworkManagerWrapper networkManager) {
				return new AbstractStatusListener(networkmanager) {
				};
			}
			@Override
			protected AbstractLoginListener getLoginListener(NetworkManagerWrapper networkManager, String hostname) {
				return new GlowStoneLegacyLoginListener(networkmanager, hostname);
			}
		};
	}

}
