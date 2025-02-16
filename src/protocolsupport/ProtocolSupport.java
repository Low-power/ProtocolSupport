package protocolsupport;

import java.text.MessageFormat;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import protocolsupport.api.ProtocolVersion;
import protocolsupport.commands.CommandHandler;
import protocolsupport.listeners.CommandListener;
import protocolsupport.listeners.PlayerListener;
import protocolsupport.logger.AsyncErrorLogger;
import protocolsupport.protocol.legacyremapper.LegacySound;
import protocolsupport.protocol.legacyremapper.chunk.BlockStorageReader;
import protocolsupport.protocol.packet.ClientBoundPacket;
import protocolsupport.protocol.packet.ServerBoundPacket;
import protocolsupport.protocol.packet.handler.AbstractLoginListener;
import protocolsupport.protocol.pipeline.initial.InitialPacketDecoder;
import protocolsupport.protocol.typeremapper.id.IdRemapper;
import protocolsupport.protocol.typeremapper.watchedentity.remapper.SpecificRemapper;
import protocolsupport.protocol.typeremapper.watchedentity.types.WatchedType;
import protocolsupport.protocol.typeskipper.id.IdSkipper;
import protocolsupport.protocol.utils.data.ItemData;
import protocolsupport.protocol.utils.data.PotionData;
import protocolsupport.protocol.utils.data.SoundData;
import protocolsupport.protocol.utils.datawatcher.DataWatcherObjectIdRegistry;
import protocolsupport.protocol.utils.i18n.I18NData;
import protocolsupport.utils.netty.Allocator;
import protocolsupport.utils.netty.Compressor;
import protocolsupport.zplatform.ServerPlatform;

public class ProtocolSupport extends JavaPlugin {

	@Override
	public void onLoad() {
		AsyncErrorLogger.INSTANCE.start();
		if (!ServerPlatform.detect()) {
			getLogger().severe("Unsupported server implementation type, shutting down");
			Bukkit.shutdown();
			return;
		} else {
			getLogger().info(MessageFormat.format("Detected {0} server implementation type", ServerPlatform.get().getName()));
		}
		try {
			ProtocolVersion.values();
			WatchedType.init();
			Allocator.init();
			ItemData.init();
			PotionData.init();
			SoundData.init();
			I18NData.init();
			Compressor.init();
			ServerBoundPacket.init();
			ClientBoundPacket.init();
			InitialPacketDecoder.init();
			AbstractLoginListener.init();
			LegacySound.init();
			IdSkipper.init();
			DataWatcherObjectIdRegistry.init();
			SpecificRemapper.init();
			IdRemapper.init();
			BlockStorageReader.init();
			ServerPlatform.get().inject();
		} catch(Exception e) {
			getLogger().log(Level.SEVERE, "Error when loading, make sure you are using supported server version", e);
			Bukkit.shutdown();
		}
	}

	@Override
	public void onEnable() {
		getCommand("protocolsupport").setExecutor(new CommandHandler());
		getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
		getServer().getPluginManager().registerEvents(new CommandListener(), this);
	}

	@Override
	public void onDisable() {
		Bukkit.shutdown();
		AsyncErrorLogger.INSTANCE.stop();
	}

	public static void logWarning(String message) {
		JavaPlugin.getPlugin(ProtocolSupport.class).getLogger().warning(message);
	}

	public static void logInfo(String message) {
		JavaPlugin.getPlugin(ProtocolSupport.class).getLogger().info(message);
	}

}
