package protocolsupport.protocol.packet.handler;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadFactory;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.util.CachedServerIcon;

import io.netty.channel.ChannelFutureListener;
import protocolsupport.ProtocolSupport;
import protocolsupport.api.ProtocolVersion;
import protocolsupport.api.events.ServerPingResponseEvent;
import protocolsupport.api.events.ServerPingResponseEvent.ProtocolInfo;
import protocolsupport.protocol.ConnectionImpl;
import protocolsupport.utils.Utils;
import protocolsupport.zplatform.ServerPlatform;
import protocolsupport.zplatform.network.NetworkManagerWrapper;

public abstract class AbstractStatusListener {

	private static final int statusThreads = Utils.getJavaPropertyValue("statusthreads", 2);
	private static final int statusThreadKeepAlive = Utils.getJavaPropertyValue("statusthreadskeepalive", 60);

	public static void init() {
		ProtocolSupport.logInfo(MessageFormat.format("Status threads max count: {0}, keep alive time: {1}", statusThreads, statusThreadKeepAlive));
	}

	private static final Executor statusprocessor = new ThreadPoolExecutor(
		1, statusThreads,
		statusThreadKeepAlive, TimeUnit.SECONDS,
		new LinkedBlockingQueue<Runnable>(),
		new ThreadFactory() {
			public Thread newThread(Runnable r) {
				return new Thread(r, "StatusProcessingThread");
			}
		}
	);

	protected final NetworkManagerWrapper networkManager;
	public AbstractStatusListener(NetworkManagerWrapper networkmanager) {
		this.networkManager = networkmanager;
	}

	private boolean sentInfo = false;

	public void handleStatusRequest() {
		if (sentInfo) {
			networkManager.close("Status request has already been handled.");
		}
		sentInfo = true;

		statusprocessor.execute(new Runnable() {
			public void run() {
				InetSocketAddress addr = networkManager.getAddress();

				ArrayList<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());

				String motd = Bukkit.getMotd();
				int maxPlayers = Bukkit.getMaxPlayers();

				InternalServerListPingEvent bevent = new InternalServerListPingEvent(addr.getAddress(), motd, maxPlayers, players);
				bevent.setServerIcon(Bukkit.getServerIcon());
				Bukkit.getPluginManager().callEvent(bevent);

				String icon = bevent.getIcon() != null ? ServerPlatform.get().getMiscUtils().convertBukkitIconToBase64(bevent.getIcon()) : null;
				motd = bevent.getMotd();
				maxPlayers = bevent.getMaxPlayers();

				List<String> profiles = new ArrayList<>(players.size());
				for (Player player : players) {
					profiles.add(player.getName());
				}

				ServerPingResponseEvent revent = new ServerPingResponseEvent(
					ConnectionImpl.getFromChannel(networkManager.getChannel()),
						new ProtocolInfo(ProtocolVersion.getLatest(), ServerPlatform.get().getMiscUtils().getModName() + " " + ServerPlatform.get().getMiscUtils().getVersionName()),
					icon, motd, maxPlayers, profiles
				);
				Bukkit.getPluginManager().callEvent(revent);

				networkManager.sendPacket(ServerPlatform.get().getPacketFactory().createStausServerInfoPacket(
					revent.getPlayers(), revent.getProtocolInfo(),
					revent.getIcon(), revent.getMotd(), revent.getMaxPlayers()
				));
			}
		});
	}

	@SuppressWarnings("unchecked")
	public void handlePing(long pingId) {
		networkManager.sendPacket(ServerPlatform.get().getPacketFactory().createStatusPongPacket(pingId), ChannelFutureListener.CLOSE);
	}

	public static class InternalServerListPingEvent extends ServerListPingEvent {

		private final List<Player> players;
		protected InternalServerListPingEvent(InetAddress address, String motd, int maxPlayers, List<Player> players) {
			super(address, motd, maxPlayers);
			this.players = players;
		}

		protected CachedServerIcon icon;
		public CachedServerIcon getIcon() {
			return icon;
		}

		@Override
		public void setServerIcon(CachedServerIcon icon) {
			this.icon = icon;
		}

		@Override
		public Iterator<Player> iterator() throws UnsupportedOperationException {
			return players.iterator();
		}

	}

}
