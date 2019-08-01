package co.paradaux.hdiscord.commands.internal;

import co.aikar.commands.CommandIssuer;
import co.aikar.taskchain.TaskChain;
import co.paradaux.hdiscord.enums.Message;
import co.paradaux.hdiscord.utils.ConfigurationFileUtil;
import co.paradaux.hdiscord.utils.ServiceUtil;
import java.net.MalformedURLException;
import org.bukkit.plugin.Plugin;

public class ReloadCommand implements Runnable {
    private final Plugin plugin;
    private final TaskChain<?> chain;
    private final CommandIssuer issuer;

    public ReloadCommand(Plugin plugin, TaskChain<?> chain, CommandIssuer issuer) {
        this.plugin = plugin;
        this.chain = chain;
        this.issuer = issuer;
    }

    public void run() {
        issuer.sendInfo(Message.RELOAD__BEGIN);

        chain
                .async(ServiceUtil::unregisterDiscord)
                .async(() -> ConfigurationFileUtil.reloadConfig(plugin))
                .<Boolean>asyncCallback((v, f) -> {
                    try {
                        ServiceUtil.registerDiscord();
                        f.accept(true);
                    } catch (MalformedURLException ex) {
                        issuer.sendInfo(Message.ERROR__WEBHOOK_INVALID);
                    }
                    f.accept(false);
                })
                .abortIf(v -> !v)
                .sync(() -> issuer.sendInfo(Message.RELOAD__END))
                .execute();
    }
}
