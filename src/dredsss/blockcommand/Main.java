package dredsss.blockcommand;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener, CommandExecutor {
	public HashMap<UUID, String> cmdBlockPlaceWaitingList = new HashMap<UUID, String>();
	public List<UUID> cmdBlockDeleteWaitingList;
	@Override
	public void onEnable() {
		this.saveDefaultConfig();
		
		this.getServer().getPluginManager().registerEvents(this, this);
		this.getCommand("blockcommand").setExecutor(this);
		// this.getCommand("delblockcommand").setExecutor(this);
	}
	
	@EventHandler(priority=EventPriority.NORMAL)
	public void onClick(PlayerInteractEvent e) {
		if(e.getAction() != Action.LEFT_CLICK_BLOCK && e.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}
		
		Material mat = e.getMaterial();
		Block block = e.getClickedBlock();
		Location loc = block.getLocation();
		String worldName = loc.getWorld().getName();
		Double dx, dy, dz;
		dx = loc.getX();
		dy = loc.getY();
		dz = loc.getZ();
		Integer x, y, z;
		x = dx.intValue();
		y = dy.intValue();
		z = dz.intValue();
		
		Player p = e.getPlayer();
		
		String path = "commands." + worldName + ".X|Y|Z".
				replaceAll("X", x.toString()).replaceAll("Y", y.toString()).replaceAll("Z", z.toString());
		String command = this.getConfig().getString(path);
		
		if (mat == Material.DIAMOND_AXE) {
			if (p.hasPermission("blockcommand.del")) {
				if (this.getConfig().isSet(path)) {
					this.getConfig().set(path, "null");
					p.sendMessage(this.getConfig().getString("messages.success"));
					e.setCancelled(false);
					return;
				}
			}
		}
		
		
		
		if (command == "null" || command == null)
			return;
		
		

		p.performCommand(command);
		e.setCancelled(true);
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onPlace(BlockPlaceEvent e) {
		Player p = e.getPlayer();
		Block b = e.getBlock();
		UUID puuid = p.getUniqueId();
		
		if (this.cmdBlockPlaceWaitingList.containsKey(puuid)) {
			Location loc = b.getLocation();
			Integer x = ((Double) loc.getX()).intValue();
			Integer y = ((Double) loc.getY()).intValue();
			Integer z = ((Double) loc.getZ()).intValue();
			String path = "commands." + loc.getWorld().getName() + "." + "X|Y|Z".replaceAll("X", x.toString()).replaceAll("Y", y.toString()).replaceAll("Z", z.toString());
			this.getConfig().set(path, this.cmdBlockPlaceWaitingList.get(puuid));
			this.saveConfig();
			p.sendMessage(this.getConfig().getString("messages.commandSet"));
			this.cmdBlockPlaceWaitingList.remove(puuid);
			return;
		}
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Only for player usage");
			return true;
		}
		Player p = (Player) sender;
		
		if (args.length == 0)
			return true;
		
		if (!p.hasPermission("blockcommand.set")) {
			p.sendMessage(this.getConfig().getString("messages.noperm"));
			return true;
		}
			
		String commandExpected = String.join(" ", args);
			
		System.out.print(commandExpected);
			
		this.cmdBlockPlaceWaitingList.put(p.getUniqueId(), commandExpected);
		p.sendMessage(this.getConfig().getString("messages.toggled"));
		// System.out.print("BlockCommand command: " + String.join(" ", Arrays.copyOfRange(args, 1, args.length)));
		return false;
	}
}
