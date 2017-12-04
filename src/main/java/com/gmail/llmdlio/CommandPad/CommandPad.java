package com.gmail.llmdlio.CommandPad;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.gmail.llmdlio.CommandPad.config.CommandPadConfig;
import com.gmail.llmdlio.CommandPad.ParticleEffect;


public class CommandPad extends JavaPlugin implements Listener {
	
	private static ConfigurationSection EffectSignsConfig;
	private static ConfigurationSection AliasList;
	private static String pluginPrefix = "&f[&4CommandPad&f] ";
	private static Boolean showCooldowns;
	private static List<String> PossibleEffects = new ArrayList<String>();
	private static List<String> DisabledWorlds;
	private static List<String> effectSignNameList = new ArrayList<String>();
	private static List<String> aliasListCmds = new ArrayList<String>();
	public HashMap<String, Long> cooldowns = new HashMap<String, Long>();
	
	private CommandPadConfig config = new CommandPadConfig(this);
	
    @Override
    public void onEnable() {
    	getServer().getPluginManager().registerEvents(this, this);
    	reloadConfig();   	
    	
    	if (!LoadCommandPadSettings()) {
    		getLogger().severe("Config failed to load!");    		
    		this.getServer().getPluginManager().disablePlugin(this);
    	} 
    	if(!this.isEnabled()) 
            return; 
    	getLogger().info("CommandPad" + this.getDescription().getVersion() + " by LlmDl Enabled.");  	

    	pluginPrefix= ChatColor.translateAlternateColorCodes('&', pluginPrefix );
    } 
    
	@Override
    public void onDisable() {
    	getLogger().info("CommandPad Disabled.");
    } 
	
    public void reloadConfig() { 
        if (!getDataFolder().exists())  
            getDataFolder().mkdirs(); 
        config.reload();         
    }
    
	private boolean LoadCommandPadSettings() {		
		EffectSignsConfig = config.getConfig().getConfigurationSection("EffectSigns");
		showCooldowns = config.getConfig().getBoolean("ShowCooldowns");
		DisabledWorlds = config.getConfig().getStringList("DisabledWorlds");
		
		String[] strArray = (config.getConfig().getString("ValidEffects")).split(",");		
		if (strArray != null) {
			for (int ctr = 0; ctr < strArray.length; ctr++)
				if (strArray[ctr] != null)
					PossibleEffects.add(strArray[ctr].trim());
		}
		AliasList = config.getConfig().getConfigurationSection("AliasList");
		parseAliases(AliasList);
		if (!parseEffectSigns(EffectSignsConfig)) {
			return false;
		} else {
			return true;
		}
	}

	private void parseAliases(ConfigurationSection aliasSection) {
		aliasListCmds.clear();
		for (String key : aliasSection.getKeys(false)) {
			getLogger().info("Loading command aliases");
			aliasListCmds.add(key);
			getLogger().info("  Found command: " + key + ". which is an alias of " + aliasSection.getString(key));
		}
	}
    private boolean parseEffectSigns(ConfigurationSection effectSection) {
    	effectSignNameList.clear();
    	getLogger().info("Loading EffectSigns");
		for ( String effectSign : effectSection.getKeys(false) ) {			
			getLogger().info("  EffectSign Found: " + effectSign + ", checking integrity...");
			effectSignNameList.add(effectSign);
			if (effectSection.getConfigurationSection(effectSign).getKeys(false).contains("Effect")) {
				String EffectType = effectSection.getConfigurationSection(effectSign).getString("Effect");
				if (PossibleEffects.contains(EffectType)) {
					getLogger().info("    " + effectSign + " Effect Found: " + EffectType);
				} else {
					getLogger().severe("    " + effectSign + " Effect not a possible type!");
					return false;
				}
			} else {
				getLogger().severe("    " + effectSign + " Effect not found!");
				return false;				
			}
			if (effectSection.getConfigurationSection(effectSign).getKeys(false).contains("Speed")) {
				Integer EffectSpeed = effectSection.getConfigurationSection(effectSign).getInt("Speed");
				getLogger().info("    " + effectSign + " Speed Found: " + EffectSpeed);
			} else {
				getLogger().severe("    " + effectSign + " Speed not found!");
				return false;				
			}
			if (effectSection.getConfigurationSection(effectSign).getKeys(false).contains("Amount")) {
				Integer EffectAmount = effectSection.getConfigurationSection(effectSign).getInt("Amount");
				getLogger().info("    " + effectSign + " Amount Found: " + EffectAmount);
			} else {
				getLogger().severe("    " + effectSign + "Amount not found!");
				return false;				
			}
		}
		getLogger().info("EffectSigns Found: " + effectSignNameList ); 
		getLogger().info("EffectSigns Loaded! When these sign types are used there will be an associated particle effect.");		
		return true;			
	}

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (command.getName().equalsIgnoreCase("commandpad")) {
			if (args.length == 0) {
                sender.sendMessage(pluginPrefix + "Version " + this.getDescription().getVersion() + " by LlmDl");
                sender.sendMessage(" /commandpad reload - reloads config.yml");                
                return true;
            }     	
			if (args[0].equalsIgnoreCase("reload")){
				if (!sender.hasPermission("commandpad.reload")) {
					sender.sendMessage(pluginPrefix + ChatColor.RED + "Insufficient Permissions.");
					return false;
				}
				config.reload();
				LoadCommandPadSettings();
				sender.sendMessage(pluginPrefix + "Config.yml reloaded");
				return true;
			}
		}
		return false;
	}
    
	@EventHandler(priority = EventPriority.LOWEST)
	private void onPressurePlate(PlayerInteractEvent event) {
		Player p = event.getPlayer();		
		if (event.getAction().equals(org.bukkit.event.block.Action.PHYSICAL))
			if (event.getClickedBlock().getType() == Material.GOLD_PLATE) {
				Location l = event.getClickedBlock().getLocation();
				Block b = event.getClickedBlock().getRelative(0, -2, 0);
				if (b.getType().equals(Material.WALL_SIGN) || b.getType().equals(Material.SIGN_POST) && b.getState() instanceof Sign ) {
					Sign sign = (Sign)b.getState();
					if (!sign.getLine(0).equals("[CommandPad]"))
						return;
					if (DisabledWorlds != null)
			    		if (DisabledWorlds.contains(l.getWorld().getName())) {
			    			p.sendMessage(pluginPrefix + ChatColor.RED + "CommandPad is disabled in this world.");
			    			return;
			    		}
					String L2 = sign.getLine(1);
					String L3 = sign.getLine(2);
					String L4 = sign.getLine(3);					
					if (!L3.isEmpty()) {						
						int cooldownTime = Integer.parseInt(L3);
						if (cooldowns != null ) 
					        if (cooldowns.containsKey(p.getName())) {
					            long secondsLeft = ((cooldowns.get(p.getName())/1000)+cooldownTime) - (System.currentTimeMillis()/1000);
					            if (secondsLeft>0) {
					            	if (showCooldowns)
					            		p.sendMessage(pluginPrefix + ChatColor.RED + "You cant use that CommandPad for another " + secondsLeft + " seconds!");
					                return;
					            }
					        }					        
						cooldowns.put(p.getName(), System.currentTimeMillis());
					}				    
					activateSign(p, L2, L3, L4);					
				}
			}
	}

	private void activateSign(Player p, String l2, String l3, String l4) {
		boolean asOp = false;
		if (l2.equals("")) {
			return;
		} 
		if (l4.equals("[AsOp]"))
			asOp = true;
		
		l2 = checkForAliases(l2);
		
		String[] line2 = l2.split(" ");
		if (l2.contains(" ")) {
			l2 = l2.replace("@p", p.getName());
		}
			
		if (effectSignNameList.contains(line2[0])) {
			String eff = EffectSignsConfig.getConfigurationSection(line2[0]).getString("Effect");
			Integer effSpeed = EffectSignsConfig.getConfigurationSection(line2[0]).getInt("Speed");
			Integer effAmount = EffectSignsConfig.getConfigurationSection(line2[0]).getInt("Amount");
			Location l = p.getLocation();
			spawnParticles(l, eff, effSpeed, effAmount);
			runCommandSign(p, l2, asOp);
		} else {	
			runCommandSign(p, l2, asOp);
		}
	}
	
	private String checkForAliases(String l2) {
		if (aliasListCmds.contains(l2))
			l2 = AliasList.getString(l2);
		return l2;		
	}

	private void runCommandSign(Player p, String command, Boolean asOp) {
		
		if (!asOp) {
			Bukkit.dispatchCommand(p, command);
		} else {
			if (p.isOp()) {
				Bukkit.dispatchCommand(p, command);
				return;
			}
			try	{
			    p.setOp(true);
			    Bukkit.dispatchCommand(p, command);
			}
			catch(Exception e) {
			    e.printStackTrace();
			}
			finally	{
			    p.setOp(false);
			}
		}
	}
	
	private void spawnParticles(Location l, String eff, Integer effSpeed, Integer effAmount) {
		ParticleEffect.valueOf(eff).display(0, 2, 0, effSpeed, effAmount, l, 15);		
	}

	
	@EventHandler(priority = EventPriority.MONITOR)
	private void onSignPlace(SignChangeEvent event) {
		Player p = event.getPlayer();
		Block b = event.getBlock();
		Sign sign = (Sign)b.getState();
		if (!event.getLine(0).equals("[CommandPad]"))
			return;
		if (!event.getLine(2).equals("")){
			try {
				@SuppressWarnings("unused")
				int test = Integer.parseInt(event.getLine(2));
			} catch (Exception e) {
				sign.getBlock().breakNaturally();
			    p.sendMessage(pluginPrefix + ChatColor.RED + "Line 3 has to be an integer.");
			    return;
			}
		}
		
		if (!p.hasPermission("commandpad.create")) {
			sign.getBlock().breakNaturally();
			p.sendMessage(pluginPrefix + ChatColor.RED + "You are not allowed to make CommandPads.");
			
		} else if (event.getLine(3).equals("[AsOp]")) {
			if (!p.isOp()) {
				sign.getBlock().breakNaturally();
				p.sendMessage(pluginPrefix + ChatColor.RED + "You are not allowed to make CommandPads that run as Op.");
			}				
		}
	}
}
