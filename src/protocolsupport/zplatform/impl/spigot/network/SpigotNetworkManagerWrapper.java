package protocolsupport.zplatform.impl.spigot.network;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.UUID;
import org.bukkit.entity.Player;

import com.mojang.authlib.properties.Property;

import io.netty.channel.Channel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.server.v1_11_R1.ChatComponentText;
import net.minecraft.server.v1_11_R1.EnumProtocol;
import net.minecraft.server.v1_11_R1.NetworkManager;
import net.minecraft.server.v1_11_R1.Packet;
import net.minecraft.server.v1_11_R1.PacketListener;
import net.minecraft.server.v1_11_R1.PlayerConnection;
import protocolsupport.api.events.PlayerPropertiesResolveEvent.ProfileProperty;
import protocolsupport.zplatform.network.NetworkManagerWrapper;
import protocolsupport.zplatform.network.NetworkState;

public class SpigotNetworkManagerWrapper extends NetworkManagerWrapper {

	public static SpigotNetworkManagerWrapper getFromChannel(Channel channel) {
		return new SpigotNetworkManagerWrapper((NetworkManager) channel.pipeline().get(SpigotChannelHandlers.NETWORK_MANAGER));
	}

	private final NetworkManager internal;
	public SpigotNetworkManagerWrapper(NetworkManager internal) {
		this.internal = internal;
	}

	@Override
	public NetworkManager unwrap() {
		return internal;
	}

	@Override
	public InetSocketAddress getAddress() {
		return (InetSocketAddress) internal.getSocketAddress();
	}

	@Override
	public InetSocketAddress getRawAddress() {
		return (InetSocketAddress) internal.getRawAddress();
	}

	@Override
	public void setAddress(InetSocketAddress address) {
		internal.l = address;
	}

	@Override
	public boolean isConnected() {
		return internal.isConnected();
	}

	@Override
	public Channel getChannel() {
		return internal.channel;
	}

	@Override
	public void close(String closeMessage) {
		internal.close(new ChatComponentText(closeMessage));
	}

	@Override
	public void sendPacket(Object packet) {
		internal.sendPacket((Packet<?>) packet);
	}

	@Override
	public void sendPacket(Object packet, GenericFutureListener<? extends Future<? super Void>> genericListener, @SuppressWarnings("unchecked") GenericFutureListener<? extends Future<? super Void>>... futureListeners) {
		internal.sendPacket((Packet<?>) packet, genericListener, futureListeners);
	}

	@Override
	public void setProtocol(NetworkState state) {
		internal.setProtocol(EnumProtocol.values()[state.ordinal()]);
	}

	@Override
	public PacketListener getPacketListener() {
		return internal.i();
	}

	@Override
	public void setPacketListener(Object listener) {
		internal.setPacketListener((PacketListener) listener);
	}

	@Override
	public UUID getSpoofedUUID() {
		return internal.spoofedUUID;
	}

	@Override
	public ProfileProperty[] getSpoofedProperties() {
		if (internal.spoofedProfile == null) {
			return null;
		}
		ProfileProperty[] properties = new ProfileProperty[internal.spoofedProfile.length];
		for(int i = 0; i < properties.length; i++) {
			Property prop = internal.spoofedProfile[i];
			properties[i] = new ProfileProperty(prop.getName(), prop.getValue(), prop.getSignature());
		}
		return properties;
	}

	@Override
	public void setSpoofedProfile(UUID uuid, ProfileProperty[] properties) {
		internal.spoofedUUID = uuid;
		if(properties == null) return;
		internal.spoofedProfile = new Property[properties.length];
		for(int i = 0; i < properties.length; i++) {
			ProfileProperty prop = properties[i];
			internal.spoofedProfile[i] = new Property(prop.getName(), prop.getValue(), prop.getSignature());
		}
	}

	@Override
	public Player getBukkitPlayer() {
		PacketListener listener = getPacketListener();
		if (listener instanceof PlayerConnection) {
			return ((PlayerConnection) listener).player.getBukkitEntity();
		}
		return null;
	}

}
