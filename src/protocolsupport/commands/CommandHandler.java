package protocolsupport.commands;

import java.util.ArrayList;
import java.util.List;

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
			for (ProtocolVersion version : ProtocolVersion.values()) {
				String name = version.getName();
				if(name == null) continue;
				String players = getPlayersStringForProtocol(version);
				if(!should_list_all && players.isEmpty()) continue;
				sender.sendMessage(String.format("%s[%s]%s %s%s%s",
					ChatColor.GOLD, name, ChatColor.RESET,
					ChatColor.GREEN, players, ChatColor.RESET));
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

	private static String getPlayersStringForProtocol(ProtocolVersion version) {
		StringBuilder sb = new StringBuilder();
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (ProtocolSupportAPI.getProtocolVersion(player) == version) {
				sb.append(player.getName());
				sb.append(", ");
			}
		}
		if (sb.length() > 2) {
			sb.delete(sb.length() - 2, sb.length());
		}
		return sb.toString();
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
