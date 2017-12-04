package com.gmail.llmdlio.CommandPad.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import org.bukkit.configuration.InvalidConfigurationException;

import com.gmail.llmdlio.CommandPad.CommandPad;


public class CommandPadConfig {
	private CommandPad plugin;
	private CommentedYamlConfiguration config;
	private String newline = System.getProperty("line.separator");
	 
	public CommandPadConfig(CommandPad plugin){
		this.plugin = plugin;
	}
	
	public void reload(){
		loadConfig();
	}
	
	// Method to load UndeadRiders\config.yml
    private void loadConfig(){ 
        File f = new File(plugin.getDataFolder(), "config.yml"); 
         
        if(!f.exists()) { 
            try { 
                f.createNewFile(); 
            } catch (IOException e) { 
                e.printStackTrace(); 
            } 
        } 
         
        config = new CommentedYamlConfiguration();        

        try { 
            config.load(f); 
        } catch (FileNotFoundException e) { 
            e.printStackTrace(); 
        } catch (IOException e) { 
            e.printStackTrace(); 
        } catch (InvalidConfigurationException e) { 
            e.printStackTrace(); 
        } 
        
        addComment("Version","  # ArmouredMobs by LlmDl."); 

         
        addDefault("Version", plugin.getDescription().getVersion()); 
        
        addComment("ValidEffects",newline,"  # List of Valid Visual Particle Effects. Do not alter unless there's been new potion effects types added to MC.");
        addDefault("ValidEffects","EXPLOSION_NORMAL,EXPLOSION_LARGE,EXPLOSION_HUGE,FIREWORKS_SPARK,WATER_SPLASH,WATER_WAKE,SUSPENDED_DEPTH,CRIT,CRIT_MAGIC,SMOKE_NORMAL,SMOKE_LARGE,SPELL,SPELL_INSTANT,SPELL_MOB,SPELL_MOB_AMBIENT,SPELL_WITCH,DRIP_WATER,DRIP_LAVA,VILLAGER_ANGRY,VILLAGER_HAPPY,TOWN_AURA,PORTAL,ENCHANTMENT_TABLE,FLAME,LAVA,CLOUD,REDSTONE,SNOWBALL,SNOW_SHOVEL,SLIME,BARRIER,WATER_DROP,HEART");
        
        addComment("ShowCooldowns", newline,"  # If true, then players will see a message when their command cooldown isn't over."); 
        addDefault("ShowCooldowns", false); 
        
        addComment("DisabledWorlds", newline, 
        		"  # A list of worlds where configured mobs are not given armour/weapons:",
        		"  # Disabled Worlds:",
        		"  #   - world",
        		"  #   - world_nether");
        addDefault("DisabledWorlds", new ArrayList<String>());

        addComment("EffectSigns", newline,
        		"  # These are just options, it is full configurable. In this example when spawn/home/heal are on",
        		"  # the 2nd line of the sign, upon the sign's use the particle effect supplied will be used.",
        		"  # Example: ",
        		"  # EffectSigns:",
        		"  #   spawn:",
        		"  #     - Effect: PORTAL",
        		"  #     - Speed: 15",
        		"  #     - Amount: 50",
        		"  #   home:",
        		"  #     - Effect: PORTAL",
        		"  #     - Speed: 15",
        		"  #     - Amount: 50",
        		"  #   heal:",
        		"  #     - Effect: HEART",
				"  #     - Speed: 15",
				"  #     - Amount: 50");        
        addDefault("EffectSigns", "");
        
        addComment("AliasList", newline,
        		"  # This is a list of commands that are shortcuts/aliases of longer commands which wouldn't fit",
        		"  # a single line of a sign. You may use @p to signify the player who activates the pressure plate's",
        		"  # name. These aliases can be used inside of the EffectSigns section of the config.",
        		"  # Example:",
        		"  # AliasList:",
        		"  #   give_52_10: give @p Diamond_Ore 10",
        		"  #   kit_funtime: kit @p funtime",
        		"  #   shortcmd: areallylongcommandthatwouldneverfitonasign");
        addDefault("AliasList", "");        		

        // Write back config 
        try { 
            config.save(f); 
        } catch (IOException e) { 
            e.printStackTrace(); 
        } 
    } 
	
	public CommentedYamlConfiguration getConfig() {	
		return config;
	}
	
	private boolean hasPath(String path) {
		return config.isSet(path);
	}
	
	private void addComment(String path, String... comment) {
			config.addComment(path, comment);		
	}
	
	private void addDefault(String path, Object defaultValue) {
		if (!hasPath(path))
			config.set(path, defaultValue);		
	}
}