package protocolsupport.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Collection;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import io.netty.util.ResourceLeakDetector;
import io.netty.util.ResourceLeakDetector.Level;
import protocolsupport.api.ProtocolSupportAPI;
import protocolsupport.api.ProtocolVersion;
import protocolsupport.zplatform.ServerPlatform;
import protocolsupport.zplatform.PlatformUtils;

public class CommandHandler implements CommandExecutor, TabCompleter {

	private static String get_player_names(Collection<Player> players, ProtocolVersion version, String separator, ChatColor color) {
		StringBuilder sb = new StringBuilder();
		for (Player player : players) {
			if (ProtocolSupportAPI.getProtocolVersion(player) == version) {
				if(sb.length() > 0) sb.append(separator);
				if(color != null) sb.append(color.toString());
				sb.append(player.getName());
				if(color != null) sb.append(ChatColor.RESET.toString());
			}
		}
		return sb.toString();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!sender.hasPermission("protocolsupport.admin")) {
			sender.sendMessage(ChatColor.RED + "You have no power here!");
			return true;
		}
		if(args.length < 1) return false;
		if(args[0].equals("list")) {
			if(args.length > 2) return false;
			boolean should_list_all = false;
			if(args.length == 2) {
				if(args[1].equals("-a")) should_list_all = true;
				else return false;
			}
			Collection<Player> players = (Collection<Player>)Bukkit.getOnlinePlayers();
			if(!should_list_all && players.isEmpty()) {
				sender.sendMessage("No players connected");
				return true;
			}
			for (ProtocolVersion version : ProtocolVersion.values()) {
				String name = version.getName();
				if(name == null) continue;
				String player_names = get_player_names(players, version, ", ", ChatColor.GREEN);
				if(!should_list_all && player_names.isEmpty()) continue;
				sender.sendMessage(String.format("%s[%s]%s %s",
					ChatColor.GOLD, name, ChatColor.RESET, player_names));
			}
		} else if(args[0].equals("debug")) {
			if(args.length > 2) return false;
			PlatformUtils utils = ServerPlatform.get().getMiscUtils();
			if(args.length == 2) {
				if(args[1].equalsIgnoreCase("off")) {
					utils.disableDebug();
				} else if(args[1].equalsIgnoreCase("on")) {
					utils.enableDebug();
				} else {
					return false;
				}
			} else if(utils.isDebugging()) {
				utils.disableDebug();
			} else {
				utils.enableDebug();
			}
			sender.sendMessage(ChatColor.GOLD.toString() + 
				(utils.isDebugging() ? "Enabled" : "Disabled") + " debug");
		} else if(args[0].equals("leakdetector")) {
			if(args.length > 2) return false;
			if(args.length == 2) {
				if(args[1].equalsIgnoreCase("off")) {
					ResourceLeakDetector.setLevel(Level.DISABLED);
				} else if(args[1].equalsIgnoreCase("on")) {
					ResourceLeakDetector.setLevel(Level.PARANOID);
				} else {
					return false;
				}
			} else if (ResourceLeakDetector.isEnabled()) {
				ResourceLeakDetector.setLevel(Level.DISABLED);
			} else {
				ResourceLeakDetector.setLevel(Level.PARANOID);
			}
			sender.sendMessage(ChatColor.GOLD.toString() +
				(ResourceLeakDetector.isEnabled() ? "Enabled" : "Disabled") +
				" leak detector");
		} else {
			sender.sendMessage("Invalid subcommand");
			return false;
		}
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		ArrayList<String> completions = new ArrayList<>();
		if ("list".startsWith(args[0])) {
			completions.add("list");
		}
		if ("debug".startsWith(args[0])) {
			completions.add("debug");
		}
		if ("leakdetector".startsWith(args[0])) {
			completions.add("leakdetector");
		}
		return completions;
	}

}
