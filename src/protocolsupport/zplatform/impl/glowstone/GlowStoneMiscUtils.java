package protocolsupport.zplatform.impl.glowstone;

import java.security.KeyPair;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import org.bukkit.Achievement;
import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.CachedServerIcon;

import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import net.glowstone.GlowServer;
import net.glowstone.constants.GlowAchievement;
import net.glowstone.constants.GlowStatistic;
import net.glowstone.entity.meta.profile.PlayerProfile;
import net.glowstone.entity.meta.profile.PlayerProperty;
import net.glowstone.io.nbt.NbtSerialization;
import net.glowstone.net.protocol.ProtocolType;
import net.glowstone.util.GlowServerIcon;
import protocolsupport.api.events.PlayerPropertiesResolveEvent.ProfileProperty;
import protocolsupport.protocol.pipeline.IPacketPrepender;
import protocolsupport.protocol.pipeline.IPacketSplitter;
import protocolsupport.protocol.utils.authlib.GameProfile;
import protocolsupport.zplatform.PlatformUtils;
import protocolsupport.zplatform.impl.glowstone.itemstack.GlowStoneNBTTagCompoundWrapper;
import protocolsupport.zplatform.impl.glowstone.network.GlowStoneChannelHandlers;
import protocolsupport.zplatform.impl.glowstone.network.GlowStoneNetworkManagerWrapper;
import protocolsupport.zplatform.impl.glowstone.network.pipeline.GlowStoneFramingHandler;
import protocolsupport.zplatform.itemstack.NBTTagCompoundWrapper;
import protocolsupport.zplatform.network.NetworkManagerWrapper;
import protocolsupport.zplatform.network.NetworkState;

@SuppressWarnings("unchecked")
public class GlowStoneMiscUtils implements PlatformUtils {
	static {
		// Workaround for error: illegal forward reference
		HashMap map = new HashMap();
		for(Statistic stat_type : Statistic.values()) {
			String name = GlowStatistic.getName(stat_type);
			if(name == null) continue;
			map.put(name, stat_type);
		}
		statByName = map;
		map = new HashMap();
		for(Achievement ach_type : Achievement.values()) {
			String name = GlowAchievement.getName(ach_type);
			if(name == null) continue;
			map.put(name, ach_type);
		}
		achByName = map;
	}

	public static GlowServer getServer() {
		return ((GlowServer) Bukkit.getServer());
	}

	public static PlayerProfile toGlowStoneGameProfile(GameProfile profile) {
		Collection<ProfileProperty> properties = profile.getProperties().values();
		List<PlayerProperty> glowstone_properties = new ArrayList<>(properties.size());
		for(ProfileProperty prop : properties) {
			glowstone_properties.add(new PlayerProperty(prop.getName(), prop.getValue(), prop.getSignature()));
		}
		return new PlayerProfile(profile.getName(), profile.getUUID(), glowstone_properties);
	}

	public static ProtocolType netStateToProtocol(NetworkState type) {
		switch (type) {
			case HANDSHAKING: {
				return ProtocolType.HANDSHAKE;
			}
			case PLAY: {
				return ProtocolType.PLAY;
			}
			case LOGIN: {
				return ProtocolType.LOGIN;
			}
			case STATUS: {
				return ProtocolType.STATUS;
			}
			default: {
				throw new IllegalArgumentException(MessageFormat.format("Unknown state {0}", type));
			}
		}
	}

	public static NetworkState protocolToNetState(ProtocolType type) {
		switch (type) {
			case HANDSHAKE: {
				return NetworkState.HANDSHAKING;
			}
			case LOGIN: {
				return NetworkState.LOGIN;
			}
			case STATUS: {
				return NetworkState.STATUS;
			}
			case PLAY: {
				return NetworkState.PLAY;
			}
			default: {
				throw new IllegalArgumentException(MessageFormat.format("Unknown protocol {0}", type));
			}
		}
	}

	@Override
	public ItemStack createItemStackFromNBTTag(NBTTagCompoundWrapper tag) {
		return NbtSerialization.readItem(((GlowStoneNBTTagCompoundWrapper) tag).unwrap());
	}

	@Override
	public NBTTagCompoundWrapper createNBTTagFromItemStack(ItemStack itemstack) {
		return GlowStoneNBTTagCompoundWrapper.wrap(NbtSerialization.writeItem(itemstack, 0));
	}

	@Override
	public String getOutdatedServerMessage() {
		return "Outdated server! I\'m running {0}";
	}

	@Override
	public boolean isBungeeEnabled() {
		return getServer().getProxySupport();
	}

	private boolean debug = false;

	@Override
	public boolean isDebugging() {
		return debug == true;
	}

	@Override
	public void enableDebug() {
		debug = true;
	}

	@Override
	public void disableDebug() {
		debug = false;
	}

	@Override
	public int getCompressionThreshold() {
		return getServer().getCompressionThreshold();
	}

	@Override
	public KeyPair getEncryptionKeyPair() {
		return getServer().getKeyPair();
	}

	@Override
	public <V> FutureTask<V> callSyncTask(Callable<V> call) {
		FutureTask<V> task = new FutureTask<>(call);
		Bukkit.getScheduler().scheduleSyncDelayedTask(null, task);
		return task;
	}

	@Override
	public String getModName() {
		return "GlowStone";
	}

	@Override
	public String getVersionName() {
		return GlowServer.GAME_VERSION;
	}

	private static final Map<String, Statistic> statByName;
	private static final Map<String, Achievement> achByName;

	@Override
	public Statistic getStatisticByName(String value) {
		return statByName.get(value);
	}

	@Override
	public String getStatisticName(Statistic stat) {
		return GlowStatistic.getName(stat);
	}

	@Override
	public Achievement getAchievmentByName(String value) {
		return achByName.get(value);
	}

	@Override
	public String getAchievmentName(Achievement achievement) {
		return GlowAchievement.getName(achievement);
	}

	@Override
	public String convertBukkitIconToBase64(CachedServerIcon icon) {
		return ((GlowServerIcon) icon).getData();
	}

	@Override
	public NetworkState getNetworkStateFromChannel(Channel channel) {
		return GlowStoneNetworkManagerWrapper.getFromChannel(channel).getProtocol();
	}

	@Override
	public NetworkManagerWrapper getNetworkManagerFromChannel(Channel channel) {
		return GlowStoneNetworkManagerWrapper.getFromChannel(channel);
	}

	@Override
	public String getReadTimeoutHandlerName() {
		return GlowStoneChannelHandlers.READ_TIMEOUT;
	}

	@Override
	public String getSplitterHandlerName() {
		return GlowStoneChannelHandlers.FRAMING;
	}

	@Override
	public String getPrependerHandlerName() {
		return GlowStoneChannelHandlers.FRAMING;
	}

	@Override
	public void setFraming(ChannelPipeline pipeline, IPacketSplitter splitter, IPacketPrepender prepender) {
		((GlowStoneFramingHandler) pipeline.get(GlowStoneChannelHandlers.FRAMING)).setRealFraming(prepender, splitter);
	}

}
