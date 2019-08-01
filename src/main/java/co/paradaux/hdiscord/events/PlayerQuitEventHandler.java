package co.paradaux.hdiscord.events;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import co.paradaux.hdiscord.core.CachedConfigValues;
import co.paradaux.hdiscord.hooks.PlaceholderAPIHook;
import co.paradaux.hdiscord.utils.ConfigUtil;
import ninja.egg82.service.ServiceLocator;
import org.bukkit.ChatColor;
import org.bukkit.event.player.PlayerQuitEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.function.Consumer;

public class PlayerQuitEventHandler implements Consumer<PlayerQuitEvent> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public void accept(PlayerQuitEvent event) {
        Optional<PlaceholderAPIHook> placeholderapi;
        Optional<WebhookClient> discordClient;

        Optional<CachedConfigValues> cachedConfig = ConfigUtil.getCachedConfig();
        if (!cachedConfig.isPresent()) {
            return;
        }

        if(cachedConfig.get().getLeaveEventMsg().isEmpty()) { return; }

        try {
            placeholderapi = ServiceLocator.getOptional(PlaceholderAPIHook.class);
        } catch(InstantiationException | IllegalAccessException ex) {
            logger.error(ex.getMessage(), ex);
            placeholderapi = Optional.empty();
        }

        try {
            discordClient = ServiceLocator.getOptional(WebhookClient.class);
        } catch(InstantiationException | IllegalAccessException ex) {
            logger.error(ex.getMessage(), ex);
            discordClient = Optional.empty();
        }

        if (!discordClient.isPresent()) {
            return;
        }

        String strippedDisplayName =  ChatColor.stripColor(cachedConfig.get().getLeaveEventMsg().replace("%player%", event.getPlayer().getDisplayName()));

        WebhookMessageBuilder messageBuilder = new WebhookMessageBuilder();
        messageBuilder.setAvatarUrl(cachedConfig.get().getServerIcon());
        messageBuilder.setContent("\u200B");

        if (placeholderapi.isPresent()) {
            String stipppedPlaceholderAPIName = ChatColor.stripColor(placeholderapi.get().withPlaceholders(event.getPlayer(), cachedConfig.get().getLeaveEventMsg().replace("%player%", "%player_displayname%")));
            messageBuilder.setUsername(stipppedPlaceholderAPIName);
        } else {
            messageBuilder.setUsername(strippedDisplayName);
        }

        if (cachedConfig.get().getDebug()) {
            if (!event.getPlayer().getName().equals(strippedDisplayName)) {
                logger.info("Sending message from " + event.getPlayer().getName() + " (" + strippedDisplayName + ")..");
            } else {
                logger.info("Sending message from " + event.getPlayer().getName() + "..");
            }
        }

        discordClient.get().send(messageBuilder.build()).thenRun(() -> {
            if (cachedConfig.get().getDebug()) {
                if (!event.getPlayer().getName().equals(strippedDisplayName)) {
                    logger.info("Successfully sent message from " + event.getPlayer().getName() + " (" + strippedDisplayName + ")");
                } else {
                    logger.info("Successfully sent message from " + event.getPlayer().getName());
                }
            }
        });
    }
}
