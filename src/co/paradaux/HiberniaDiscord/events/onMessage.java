package co.paradaux.HiberniaDiscord.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import co.paradaux.HiberniaDiscord.workerThread;

public class onMessage implements Listener {
	
	private String webhook_url;
	private String crafatar_url;
	private String crafatar_options;
	
	public onMessage(String webhook_url, String crafatar_url, String crafatar_options) {
		
		this.webhook_url = webhook_url;
		this.crafatar_url = crafatar_url;
		this.crafatar_options = crafatar_options;
		
	}
	
	
	@EventHandler
	public void onMessageEvent(AsyncPlayerChatEvent e) {
		
		Player p = e.getPlayer();
		String headURL = p.getUniqueId().toString();
		String userName = p.getDisplayName();
		String message = e.getMessage();
		Runnable r = new workerThread(headURL, userName, message, webhook_url, crafatar_url, crafatar_options);
		new Thread(r).start();

	}
	
}
