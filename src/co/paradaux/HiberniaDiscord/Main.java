package co.paradaux.HiberniaDiscord;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import co.paradaux.HiberniaDiscord.events.onMessage;



public class Main extends JavaPlugin {

	private File config;
	private YamlConfiguration modifyConfig;
	
	private String webhook_url = this.getConfig().getString("webhook_url");
	private String crafatar_url = this.getConfig().getString("crafatar_url");
	private String crafatar_options = this.getConfig().getString("crafatar_options");
	
	public void onEnable() {

		this.getConfig().options().copyDefaults();
		saveDefaultConfig();
		
		
		Bukkit.getPluginManager().registerEvents(new onMessage(webhook_url, crafatar_url, crafatar_options), this);
		
	}
	
	public void initaliseConfig() throws IOException {
		config = new File(Bukkit.getServer().getPluginManager().getPlugin("HiberniaHardcore").getDataFolder(), "config.yml");
		if (!config.exists()) {
			config.createNewFile();
		}
		
		modifyConfig = YamlConfiguration.loadConfiguration(config);
		
	}
	
	public YamlConfiguration getConfigFile() {
		
		return modifyConfig;
		
	}
	
	public File getFile() {
		
		return config;
		
	}
	
}
