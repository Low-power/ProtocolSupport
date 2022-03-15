package protocolsupport.protocol.packet.handler;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.concurrent.Callable;
import org.bukkit.Bukkit;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerPreLoginEvent;

import protocolsupport.api.events.PlayerPropertiesResolveEvent;
import protocolsupport.api.events.PlayerPropertiesResolveEvent.ProfileProperty;
import protocolsupport.protocol.ConnectionImpl;
import protocolsupport.protocol.utils.MinecraftEncryption;
import protocolsupport.protocol.utils.authlib.MinecraftSessionService;
import protocolsupport.protocol.utils.authlib.MinecraftSessionService.AuthenticationUnavailableException;
import protocolsupport.zplatform.ServerPlatform;

@SuppressWarnings("deprecation")
public class PlayerLookupUUID {

	private final Logger log;
	private final AbstractLoginListener listener;
	private final boolean isOnlineMode;

	public PlayerLookupUUID(AbstractLoginListener listener, boolean isOnlineMode) {
		this.log = Bukkit.getLogger();
		this.listener = listener;
		this.isOnlineMode = isOnlineMode;
	}

	public void run() {
		String joinName = listener.profile.getName();
		if(isOnlineMode) try {
			String hash = new BigInteger(MinecraftEncryption.createHash(ServerPlatform.get().getMiscUtils().getEncryptionKeyPair().getPublic(), listener.loginKey)).toString(16);
			listener.profile = MinecraftSessionService.hasJoinedServer(joinName, hash);
			if(listener.profile == null) {
				listener.disconnect("Failed to verify username!");
				log.warning(joinName + " failed authentication");
				return;
			}
		} catch (AuthenticationUnavailableException e) {
			listener.disconnect("Authentication servers are down. Please try again later, sorry!");
			log.log(Level.SEVERE,
				String.format("Couldn't verify user name %s due to authentication server unavailable", joinName),
				e);
		} else {
			listener.initOfflineModeGameProfile();
		}
		try {
			fireLoginEvents();
		} catch(Exception e) {
			listener.disconnect("Error occurred during login");
			log.log(Level.SEVERE, "Exception firing login events for " + joinName, e);
		}
	}

	private void fireLoginEvents() throws InterruptedException, ExecutionException  {
		if (!listener.networkManager.isConnected()) {
			return;
		}

		String playerName = listener.profile.getName();
		InetSocketAddress saddress = listener.networkManager.getAddress();

		InetAddress address = saddress.getAddress();

		PlayerPropertiesResolveEvent propResolve = new PlayerPropertiesResolveEvent(
			ConnectionImpl.getFromChannel(listener.networkManager.getChannel()),
			playerName, listener.profile.getProperties().values()
		);
		Bukkit.getPluginManager().callEvent(propResolve);
		listener.profile.clearProperties();
		for (ProfileProperty property : propResolve.getProperties().values()) {
			listener.profile.addProperty(property);
		}
		UUID uniqueId = listener.profile.getUUID();

		AsyncPlayerPreLoginEvent asyncEvent = new AsyncPlayerPreLoginEvent(playerName, address, uniqueId);
		Bukkit.getPluginManager().callEvent(asyncEvent);

		final PlayerPreLoginEvent syncEvent = new PlayerPreLoginEvent(playerName, address, uniqueId);
		if (asyncEvent.getResult() != PlayerPreLoginEvent.Result.ALLOWED) {
			syncEvent.disallow(asyncEvent.getResult(), asyncEvent.getKickMessage());
		}

		if (PlayerPreLoginEvent.getHandlerList().getRegisteredListeners().length != 0) {
			if (ServerPlatform.get().getMiscUtils().callSyncTask(new Callable<PlayerPreLoginEvent.Result>() {
					public PlayerPreLoginEvent.Result call() throws Exception {
						Bukkit.getPluginManager().callEvent(syncEvent);
						return syncEvent.getResult();
					}
				}
			).get() != PlayerPreLoginEvent.Result.ALLOWED) {
				listener.disconnect(syncEvent.getKickMessage());
				return;
			}
		}

		if (syncEvent.getResult() != PlayerPreLoginEvent.Result.ALLOWED) {
			listener.disconnect(syncEvent.getKickMessage());
			return;
		}

		log.info("UUID of player " + listener.profile.getName() + " is " + listener.profile.getUUID().toString());
		listener.setReadyToAccept();
	}

}
