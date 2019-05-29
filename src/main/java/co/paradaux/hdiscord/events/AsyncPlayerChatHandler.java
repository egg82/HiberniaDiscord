package co.paradaux.hdiscord.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import co.paradaux.hdiscord.WorkerThread;

public class MessageEvent implements Listener {
    private String webhookURL;
    private String crafatarURL;
    private String crafatarOptions;

    public MessageEvent(String webhookURL, String crafatarURL, String crafatarOptions) {
        this.webhookURL = webhookURL;
        this.crafatarURL = crafatarURL;
        this.crafatarOptions = crafatarOptions;
    }

    @EventHandler
    public void onMessageEvent(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        String headURL = p.getUniqueId().toString();
        String userName = p.getDisplayName();
        String message = e.getMessage();
        Runnable r = new WorkerThread(headURL, userName, message, webhookURL, crafatarURL, crafatarOptions);
        new Thread(r).start();
    }
}
