package protocolsupport.zplatform.impl.glowstone.network;

import java.net.InetSocketAddress;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.entity.Player;

import com.flowpowered.network.Message;
import com.flowpowered.network.protocol.AbstractProtocol;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.glowstone.entity.meta.profile.PlayerProfile;
import net.glowstone.entity.meta.profile.PlayerProperty;
import net.glowstone.net.GlowSession;
import net.glowstone.net.ProxyData;
import net.glowstone.net.pipeline.MessageHandler;
import net.glowstone.net.protocol.ProtocolType;
import protocolsupport.api.events.PlayerPropertiesResolveEvent.ProfileProperty;
import protocolsupport.zplatform.impl.glowstone.GlowStoneMiscUtils;
import protocolsupport.zplatform.network.NetworkManagerWrapper;
import protocolsupport.zplatform.network.NetworkState;

public class GlowStoneNetworkManagerWrapper extends NetworkManagerWrapper {

	private static final AttributeKey<Object> packet_listener_key = AttributeKey.valueOf("ps_packet_listener");
	private static final UUID fakeUUID = UUID.randomUUID();

	public static Object getPacketListener(GlowSession session) {
		return session.getChannel().attr(packet_listener_key).get();
	}

	public static GlowStoneNetworkManagerWrapper getFromChannel(Channel channel) {
		return new GlowStoneNetworkManagerWrapper((MessageHandler) channel.pipeline().get(GlowStoneChannelHandlers.NETWORK_MANAGER));
	}

	private final MessageHandler handler;
	public GlowStoneNetworkManagerWrapper(MessageHandler handler) {
		this.handler = handler;
	}

	public GlowSession getSession() {
		return handler.getSession().get();
	}

	@Override
	public Object unwrap() {
		return handler;
	}

	@Override
	public InetSocketAddress getAddress() {
		return getSession().getAddress();
	}

	@Override
	public void setAddress(InetSocketAddress address) {
		ProxyData old = getSession().getProxyData();
		if (old != null) {
			getSession().setProxyData(new ProxyData(null, null, address, null, old.getProfile().getUniqueId(), old.getProfile().getProperties()));
		} else {
			List<PlayerProperty> empty_list = Collections.emptyList();
			getSession().setProxyData(new ProxyData(null, null, address, null, fakeUUID, empty_list));
		}
	}

	@Override
	public boolean isConnected() {
		return getChannel().isOpen();
	}

	@Override
	public Channel getChannel() {
		return getSession().getChannel();
	}

	@Override
	public void close(String closeMessage) {
		getSession().disconnect(closeMessage);
	}

	@Override
	public void sendPacket(Object packet) {
		getSession().send((Message) packet);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void sendPacket(Object packet, GenericFutureListener<? extends Future<? super Void>> genericListener, GenericFutureListener<? extends Future<? super Void>>... futureListeners) {
		getSession().sendWithFuture((Message) packet).addListener(genericListener).addListeners(futureListeners);
	}

	public NetworkState getProtocol() {
		AbstractProtocol proto = getSession().getProtocol();
		for (ProtocolType type : ProtocolType.values()) {
			if (type.getProtocol() == proto) {
				return GlowStoneMiscUtils.protocolToNetState(type);
			}
		}
		throw new IllegalStateException(MessageFormat.format("Unkown protocol {0}", proto));
	}

	@Override
	public void setProtocol(NetworkState state) {
		getSession().setProtocol(GlowStoneMiscUtils.netStateToProtocol(state));
	}

	@Override
	public Object getPacketListener() {
		return getPacketListener(getSession());
	}

	@Override
	public void setPacketListener(Object listener) {
		getChannel().attr(packet_listener_key).set(listener);
	}

	@Override
	public UUID getSpoofedUUID() {
		PlayerProfile profile = getSpoofedProfile();
		return profile != null ? profile.getUniqueId() : null;
	}

	@Override
	public ProfileProperty[] getSpoofedProperties() {
		PlayerProfile profile = getSpoofedProfile();
		if(profile == null) return null;
		List<PlayerProperty> glowstone_properties = profile.getProperties();
		ProfileProperty[] properties = new ProfileProperty[glowstone_properties.size()];
		for(int i = 0; i < properties.length; i++) {
			PlayerProperty property = glowstone_properties.get(i);
			properties[i] = new ProfileProperty(property.getName(), property.getValue(), property.getSignature());
		}
		return properties;
	}

	private PlayerProfile getSpoofedProfile() {
		ProxyData proxydata = getSession().getProxyData();
		return proxydata != null ? proxydata.getProfile("?[]___PSFakeProfile!!!!!!!") : null;
	}

	@Override
	public void setSpoofedProfile(UUID uuid, ProfileProperty[] properties) {
		ProxyData old = getSession().getProxyData();
		List<PlayerProperty> glowproperties;
		if (properties == null) {
			glowproperties = Collections.emptyList();
		} else {
			glowproperties = new ArrayList<>(properties.length);
			for(ProfileProperty prop : properties) {
				glowproperties.add(new PlayerProperty(prop.getName(), prop.getValue(), prop.getSignature()));
			}
		}
		if (old != null) {
			getSession().setProxyData(new ProxyData(null, null, old.getAddress(), null, uuid, glowproperties));
		} else {
			getSession().setProxyData(new ProxyData(null, null, getAddress(), null, uuid, glowproperties));
		}
	}

	@Override
	public Player getBukkitPlayer() {
		return getSession().getPlayer();
	}

}
