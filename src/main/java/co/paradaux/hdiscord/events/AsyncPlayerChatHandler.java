package co.paradaux.hdiscord.events;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import co.paradaux.hdiscord.core.CachedConfigValues;
import co.paradaux.hdiscord.hooks.PlaceholderAPIHook;
import co.paradaux.hdiscord.utils.ConfigUtil;
import java.util.Optional;
import java.util.function.Consumer;
import ninja.egg82.service.ServiceLocator;
import org.bukkit.ChatColor;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AsyncPlayerChatHandler implements Consumer<AsyncPlayerChatEvent> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public AsyncPlayerChatHandler() {}

    public void accept(AsyncPlayerChatEvent event) {
        Optional<PlaceholderAPIHook> placeholderapi;
        Optional<WebhookClient> discordClient;

        Optional<CachedConfigValues> cachedConfig = ConfigUtil.getCachedConfig();
        if (!cachedConfig.isPresent()) {
            return;
        }

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

        String strippedDisplayName = ChatColor.stripColor(event.getPlayer().getDisplayName());

        WebhookMessageBuilder messageBuilder = new WebhookMessageBuilder();
        messageBuilder.setAvatarUrl(cachedConfig.get().getAvatarURL() + event.getPlayer().getUniqueId() + cachedConfig.get().getAvatarOptions());
        messageBuilder.setUsername(strippedDisplayName);

        if (placeholderapi.isPresent()) {
            messageBuilder.setContent(placeholderapi.get().withPlaceholders(event.getPlayer(), event.getMessage()));
        } else {
            messageBuilder.setContent(event.getMessage());
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
