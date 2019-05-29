package co.paradaux.hdiscord;

import co.aikar.commands.PaperCommandManager;
import co.aikar.commands.RegisteredCommand;
import co.aikar.taskchain.BukkitTaskChainFactory;
import co.aikar.taskchain.TaskChainFactory;
import co.paradaux.hdiscord.commands.DiscordCommand;
import co.paradaux.hdiscord.core.Configuration;
import co.paradaux.hdiscord.events.PlayerLoginUpdateNotifyHandler;
import co.paradaux.hdiscord.hooks.PlaceholderAPIHook;
import co.paradaux.hdiscord.hooks.PluginHook;
import co.paradaux.hdiscord.utils.ConfigurationFileUtil;
import co.paradaux.hdiscord.utils.LogUtil;
import co.paradaux.hdiscord.utils.ServiceUtil;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.SetMultimap;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import ninja.egg82.events.BukkitEventSubscriber;
import ninja.egg82.events.BukkitEvents;
import ninja.egg82.service.ServiceLocator;
import ninja.egg82.service.ServiceNotFoundException;
import ninja.egg82.updater.SpigotUpdater;
import org.bukkit.ChatColor;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import co.paradaux.hdiscord.events.AsyncPlayerChatHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main extends JavaPlugin {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private TaskChainFactory taskFactory;
    private PaperCommandManager commandManager;

    private List<BukkitEventSubscriber<?>> events = new ArrayList<>();

    public void onEnable() {
        taskFactory = BukkitTaskChainFactory.create(this);
        commandManager = new PaperCommandManager(this);
        commandManager.enableUnstableAPI("help");

        loadServices();
        loadCommands();
        loadEvents();
        loadHooks();

        getServer().getConsoleSender().sendMessage(LogUtil.getHeading() + ChatColor.GREEN + "Enabled");

        getServer().getConsoleSender().sendMessage(LogUtil.getHeading()
                        + ChatColor.YELLOW + "[" + ChatColor.AQUA + "Version " + ChatColor.WHITE + getDescription().getVersion() + ChatColor.YELLOW +  "] "
                        + ChatColor.YELLOW + "[" + ChatColor.WHITE + commandManager.getRegisteredRootCommands().size() + ChatColor.GOLD + " Commands" + ChatColor.YELLOW +  "] "
                        + ChatColor.YELLOW + "[" + ChatColor.WHITE + events.size() + ChatColor.BLUE + " Events" + ChatColor.YELLOW +  "]"
        );

        checkUpdate();
    }

    public void onDisable() {
        taskFactory.shutdown(8, TimeUnit.SECONDS);
        commandManager.unregisterCommands();

        for (BukkitEventSubscriber<?> event : events) {
            event.cancel();
        }
        events.clear();

        unloadHooks();
        unloadServices();

        getServer().getConsoleSender().sendMessage(LogUtil.getHeading() + ChatColor.DARK_RED + "Disabled");
    }

    private void loadServices() {
        ConfigurationFileUtil.reloadConfig(this);
        ServiceUtil.registerDiscord();
        ServiceLocator.register(new SpigotUpdater(this, 67795));
    }

    private void loadCommands() {
        commandManager.getCommandCompletions().registerCompletion("subcommand", c -> {
            String lower = c.getInput().toLowerCase();
            Set<String> commands = new LinkedHashSet<>();
            SetMultimap<String, RegisteredCommand> subcommands = commandManager.getRootCommand("discord").getSubCommands();
            for (Map.Entry<String, RegisteredCommand> kvp : subcommands.entries()) {
                if (!kvp.getValue().isPrivate() && (lower.isEmpty() || kvp.getKey().toLowerCase().startsWith(lower)) && kvp.getValue().getCommand().indexOf(' ') == -1) {
                    commands.add(kvp.getValue().getCommand());
                }
            }
            return ImmutableList.copyOf(commands);
        });

        commandManager.registerCommand(new DiscordCommand(this, taskFactory));
    }

    private void loadEvents() {
        events.add(BukkitEvents.subscribe(this, AsyncPlayerChatEvent.class, EventPriority.LOWEST).handler(e -> new AsyncPlayerChatHandler().accept(e)));
        events.add(BukkitEvents.subscribe(this, PlayerLoginEvent.class, EventPriority.LOW).handler(e -> new PlayerLoginUpdateNotifyHandler(this).accept(e)));
    }

    private void loadHooks() {
        PluginManager manager = getServer().getPluginManager();

        if (manager.getPlugin("PlaceholderAPI") != null) {
            getServer().getConsoleSender().sendMessage(LogUtil.getHeading() + ChatColor.GREEN + "Enabling support for PlaceholderAPI.");
            ServiceLocator.register(new PlaceholderAPIHook());
        } else {
            getServer().getConsoleSender().sendMessage(LogUtil.getHeading() + ChatColor.YELLOW + "PlaceholderAPI was not found. Skipping support for placeholders.");
        }
    }

    private void checkUpdate() {
        Configuration config;
        SpigotUpdater updater;
        try {
            config = ServiceLocator.get(Configuration.class);
            updater = ServiceLocator.get(SpigotUpdater.class);
        } catch (InstantiationException | IllegalAccessException | ServiceNotFoundException ex) {
            logger.error(ex.getMessage(), ex);
            return;
        }

        if (!config.getNode("update", "check").getBoolean(true)) {
            return;
        }

        updater.isUpdateAvailable().thenAccept(v -> {
            if (!v) {
                return;
            }

            if (config.getNode("update", "notify").getBoolean(true)) {
                try {
                    getServer().getConsoleSender().sendMessage(LogUtil.getHeading() + ChatColor.AQUA + " has an " + ChatColor.GREEN + "update" + ChatColor.AQUA + " available! New version: " + ChatColor.YELLOW + updater.getLatestVersion().get());
                } catch (ExecutionException ex) {
                    logger.error(ex.getMessage(), ex);
                } catch (InterruptedException ex) {
                    logger.error(ex.getMessage(), ex);
                    Thread.currentThread().interrupt();
                }
            }
        });
    }

    private void unloadHooks() {
        Set<? extends PluginHook> hooks = ServiceLocator.remove(PluginHook.class);
        for (PluginHook hook : hooks) {
            hook.cancel();
        }
    }

    private void unloadServices() {
        ServiceUtil.unregisterDiscord();
    }
}
