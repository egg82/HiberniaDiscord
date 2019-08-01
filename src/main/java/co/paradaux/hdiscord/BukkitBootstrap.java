package co.paradaux.hdiscord;

import co.paradaux.hdiscord.utils.LogUtil;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.logging.Level;
import ninja.egg82.maven.Artifact;
import ninja.egg82.maven.Scope;
import ninja.egg82.services.ProxiedURLClassLoader;
import ninja.egg82.utils.DownloadUtil;
import ninja.egg82.utils.InjectUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

public class BukkitBootstrap extends JavaPlugin {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Object concrete;
    private Class<?> concreteClass;

    private final boolean isBukkit;

    private URLClassLoader proxiedClassLoader;

    public BukkitBootstrap() {
        super();
        isBukkit = Bukkit.getName().equals("Bukkit") || Bukkit.getName().equals("CraftBukkit");
    }

    @Override
    public void onLoad() {
        proxiedClassLoader = new ProxiedURLClassLoader(getClass().getClassLoader());

        try {
            loadJars(new File(getDataFolder(), "external"), proxiedClassLoader);
        } catch (ClassCastException | URISyntaxException | IOException | SAXException | IllegalAccessException | InvocationTargetException ex) {
            logger.error(ex.getMessage(), ex);
            throw new RuntimeException("Could not load required deps.");
        }

        try {
            concreteClass = proxiedClassLoader.loadClass("co.paradaux.hdiscord.Main");
            concrete = concreteClass.getDeclaredConstructor(Plugin.class).newInstance(this);
            concreteClass.getMethod("onLoad").invoke(concrete);
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException ex) {
            logger.error(ex.getMessage(), ex);
            throw new RuntimeException("Could not create main class.");
        }
    }

    @Override
    public void onEnable() {
        try {
            concreteClass.getMethod("onEnable").invoke(concrete);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            logger.error(ex.getMessage(), ex);
            throw new RuntimeException("Could not invoke onEnable.");
        }
    }

    @Override
    public void onDisable() {
        try {
            concreteClass.getMethod("onDisable").invoke(concrete);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            logger.error(ex.getMessage(), ex);
            throw new RuntimeException("Could not invoke onDisable.");
        }
    }

    private void loadJars(File jarsFolder, URLClassLoader classLoader) throws URISyntaxException, IOException, SAXException, IllegalAccessException, InvocationTargetException {
        if (jarsFolder.exists() && !jarsFolder.isDirectory()) {
            Files.delete(jarsFolder.toPath());
        }
        if (!jarsFolder.exists()) {
            if (!jarsFolder.mkdirs()) {
                throw new IOException("Could not create parent directory structure.");
            }
        }

        InjectUtil.injectFile(getFile(), classLoader);

        Artifact serviceLocator = Artifact.builder("ninja.egg82", "service-locator", "1.0.1")
                .addRepository("https://nexus.egg82.me/repository/egg82/")
                .addRepository("https://www.myget.org/F/egg82-java/maven/")
                .addRepository("https://nexus.egg82.me/repository/maven-central/")
                .build();
        loadDep(serviceLocator, jarsFolder, classLoader, "Service Locator");

        /*JarDep.Builder serviceLocator = JarDep.builder(jarsFolder, "ninja.egg82", "service-locator", "1.0.1")
                .addURL("https://nexus.egg82.me/repository/egg82/{GROUP}/{ARTIFACT}/{VERSION}/{ARTIFACT}-{VERSION}.jar")
                .addURL("https://www.myget.org/F/egg82-java/maven/{GROUP}/{ARTIFACT}/{VERSION}/{ARTIFACT}-{VERSION}.jar");
        loadDep(serviceLocator, classLoader, "Service Locator");

        JarDep.Builder acfPaper = JarDep.builder(jarsFolder, "co.aikar", "acf-paper", "0.5.0")
                .addURL("https://nexus.egg82.me/repository/aikar/{GROUP}/{ARTIFACT}/{VERSION}-SNAPSHOT/{ARTIFACT}-{VERSION}-20190401.213856-143-shaded.jar")
                .addURL("https://repo.aikar.co/nexus/content/groups/aikar/{GROUP}/{ARTIFACT}/{VERSION}-SNAPSHOT/{ARTIFACT}-{VERSION}-20190401.213856-143-shaded.jar");
        loadDep(acfPaper, classLoader, "ACF Paper");*/

        /*JarDep acfCore = JarDep.builder(jarsFolder, "co.aikar", "acf-core", "0.5.0")
                .addURL("https://nexus.egg82.me/repository/aikar/{GROUP}/{ARTIFACT}/{VERSION}-SNAPSHOT/{ARTIFACT}-{VERSION}-20190401.213847-143-shaded.jar")
                .addURL("https://repo.aikar.co/nexus/content/groups/aikar/{GROUP}/{ARTIFACT}/{VERSION}-SNAPSHOT/{ARTIFACT}-{VERSION}-20190401.213847-143-shaded.jar")
                .build();
        loadJar(acfCore, jarsFolder, classLoader, "ACF Core");

        JarDep acfPaper = JarDep.builder(jarsFolder, "co.aikar", "acf-paper", "0.5.0")
                .addURL("https://nexus.egg82.me/repository/aikar/{GROUP}/{ARTIFACT}/{VERSION}-SNAPSHOT/{ARTIFACT}-{VERSION}-20190401.213856-143-shaded.jar")
                .addURL("https://repo.aikar.co/nexus/content/groups/aikar/{GROUP}/{ARTIFACT}/{VERSION}-SNAPSHOT/{ARTIFACT}-{VERSION}-20190401.213856-143-shaded.jar")
                .build();
        loadJar(acfPaper, jarsFolder, classLoader, "ACF Paper");

        JarDep taskchainCore = JarDep.builder("co.aikar", "taskchain-core", "3.7.2")
                .addURL("https://nexus.egg82.me/repository/aikar/{GROUP}/{ARTIFACT}/{VERSION}/{ARTIFACT}-{VERSION}.jar")
                .addURL("https://repo.aikar.co/nexus/content/groups/aikar/{GROUP}/{ARTIFACT}/{VERSION}/{ARTIFACT}-{VERSION}.jar")
                .build();
        loadJar(taskchainCore, jarsFolder, classLoader, "Taskchain Core");

        JarDep taskchainBukkit = JarDep.builder(jarsFolder, "co.aikar", "taskchain-bukkit", "3.7.2")
                .addURL("https://nexus.egg82.me/repository/aikar/{GROUP}/{ARTIFACT}/{VERSION}/{ARTIFACT}-{VERSION}.jar")
                .addURL("https://repo.aikar.co/nexus/content/groups/aikar/{GROUP}/{ARTIFACT}/{VERSION}/{ARTIFACT}-{VERSION}.jar")
                .build();
        loadJar(taskchainBukkit, jarsFolder, classLoader, "Taskchain Bukkit");

        JarDep eventchainBukkit = JarDep.builder(jarsFolder, "ninja.egg82", "event-chain-bukkit", "1.0.9")
                .addURL("https://nexus.egg82.me/repository/egg82/ninja.egg82/{ARTIFACT}/{VERSION}/{ARTIFACT}-{VERSION}.jar")
                .addURL("https://www.myget.org/F/egg82-java/maven/ninja.egg82/{ARTIFACT}/{VERSION}/{ARTIFACT}-{VERSION}.jar")
                .build();
        loadJar(eventchainBukkit, jarsFolder, classLoader, "Event Chain Bukkit");

        JarDep spigotUpdater = JarDep.builder(jarsFolder, "ninja.egg82", "spigot-updater", "1.0.1")
                .addURL("https://nexus.egg82.me/repository/egg82/ninja.egg82/{ARTIFACT}/{VERSION}/{ARTIFACT}-{VERSION}.jar")
                .addURL("https://www.myget.org/F/egg82-java/maven/ninja.egg82/{ARTIFACT}/{VERSION}/{ARTIFACT}-{VERSION}.jar")
                .build();
        loadJar(spigotUpdater, jarsFolder, classLoader, "Spigot Updater");

        JarDep configurateCore = JarDep.builder(jarsFolder, "org.spongepowered", "configurate-core", "3.6")
                .addURL("https://nexus.egg82.me/repository/sponge/{GROUP}/{ARTIFACT}/{VERSION}/{ARTIFACT}-{VERSION}.jar")
                .addURL("https://repo.spongepowered.org/maven/{GROUP}/{ARTIFACT}/{VERSION}/{ARTIFACT}-{VERSION}.jar")
                .build();
        loadJar(configurateCore, jarsFolder, classLoader, "Configurate Core");

        JarDep configurateYAML = JarDep.builder(jarsFolder, "org.spongepowered", "configurate-yaml", "3.6")
                .addURL("https://nexus.egg82.me/repository/sponge/{GROUP}/{ARTIFACT}/{VERSION}/{ARTIFACT}-{VERSION}.jar")
                .addURL("https://repo.spongepowered.org/maven/{GROUP}/{ARTIFACT}/{VERSION}/{ARTIFACT}-{VERSION}.jar")
                .build();
        loadJar(configurateYAML, jarsFolder, classLoader, "Configurate YAML");

        JarDep abstractCofiguration = JarDep.builder(jarsFolder, "ninja.egg82", "abstract-configuration", "1.0.1")
                .addURL("https://nexus.egg82.me/repository/egg82/ninja.egg82/{ARTIFACT}/{VERSION}/{ARTIFACT}-{VERSION}.jar")
                .addURL("https://www.myget.org/F/egg82-java/maven/ninja.egg82/{ARTIFACT}/{VERSION}/{ARTIFACT}-{VERSION}.jar")
                .build();
        loadJar(abstractCofiguration, jarsFolder, classLoader, "Abstract Configuration");

        JarDep webhooks = JarDep.builder(jarsFolder, "club.minnced", "discord-webhooks", "0.1.7")
                .addURL("https://nexus.egg82.me/repository/bintray-jcenter/{GROUP}/{ARTIFACT}/{VERSION}/{ARTIFACT}-{VERSION}.jar")
                .addURL("https://jcenter.bintray.com/{GROUP}/{ARTIFACT}/{VERSION}/{ARTIFACT}-{VERSION}.jar")
                .build();
        loadJar(webhooks, jarsFolder, classLoader, "Discord Webhooks");*/

        //loadPom(webhooks.getPomDep(), jarsFolder, classLoader);

        /*
        // Webooks deps

        JarDep okhttp = JarDep.builder("okhttp", "3.12.0")
                .addURL("https://nexus.egg82.me/repository/maven-central/com/squareup/okhttp3/{ARTIFACT}/{VERSION}/{ARTIFACT}-{VERSION}.jar")
                .addURL("http://central.maven.org/maven2/com/squareup/okhttp3/{ARTIFACT}/{VERSION}/{ARTIFACT}-{VERSION}.jar")
                .build();
        loadJar(okhttp, jarsFolder, classLoader, "Okhttp");

        JarDep json = JarDep.builder("json", "20180813")
                .addURL("https://nexus.egg82.me/repository/maven-central/org/json/{ARTIFACT}/{VERSION}/{ARTIFACT}-{VERSION}.jar")
                .addURL("http://central.maven.org/maven2/org/json/{ARTIFACT}/{VERSION}/{ARTIFACT}-{VERSION}.jar")
                .build();
        loadJar(json, jarsFolder, classLoader, "JSON");

        JarDep jetbrainsAnnotations = JarDep.builder("annotations", "16.0.1")
                .addURL("https://nexus.egg82.me/repository/maven-central/org/jetbrains/{ARTIFACT}/{VERSION}/{ARTIFACT}-{VERSION}.jar")
                .addURL("http://central.maven.org/maven2/org/jetbrains/{ARTIFACT}/{VERSION}/{ARTIFACT}-{VERSION}.jar")
                .build();
        loadJar(jetbrainsAnnotations, jarsFolder, classLoader, "Jetbrains Annotations");

        // Okhttp deps

        JarDep okio = JarDep.builder("okio", "2.2.2")
                .addURL("https://nexus.egg82.me/repository/maven-central/com/squareup/okio/{ARTIFACT}/{VERSION}/{ARTIFACT}-{VERSION}.jar")
                .addURL("http://central.maven.org/maven2/com/squareup/okio/{ARTIFACT}/{VERSION}/{ARTIFACT}-{VERSION}.jar")
                .build();
        loadJar(okio, jarsFolder, classLoader, "Okio");

        // Okio deps

        JarDep kotlinStd = JarDep.builder("kotlin-stdlib", "1.2.60")
                .addURL("https://nexus.egg82.me/repository/maven-central/org/jetbrains/kotlin/{ARTIFACT}/{VERSION}/{ARTIFACT}-{VERSION}.jar")
                .addURL("http://central.maven.org/maven2/org/jetbrains/kotlin/{ARTIFACT}/{VERSION}/{ARTIFACT}-{VERSION}.jar")
                .build();
        loadJar(kotlinStd, jarsFolder, classLoader, "Kotlin Standard Lib");

        // Kotlin standard lib deps

        JarDep kotlinCommon = JarDep.builder("kotlin-stdlib-common", "1.2.60")
                .addURL("https://nexus.egg82.me/repository/maven-central/org/jetbrains/kotlin/{ARTIFACT}/{VERSION}/{ARTIFACT}-{VERSION}.jar")
                .addURL("http://central.maven.org/maven2/org/jetbrains/kotlin/{ARTIFACT}/{VERSION}/{ARTIFACT}-{VERSION}.jar")
                .build();
        loadJar(kotlinCommon, jarsFolder, classLoader, "Kotlin Common");
        */
    }

    private void loadDep(Artifact artifact, File jarsFolder, URLClassLoader classLoader, String friendlyName) throws IOException, IllegalAccessException, InvocationTargetException {
        File output = new File(jarsFolder, artifact.getGroupId() + "-" + artifact.getArtifactId() + "-" + artifact.getVersion() + ".jar");
        if (!DownloadUtil.hasFile(output)) {
            log(Level.INFO, LogUtil.getHeading() + ChatColor.YELLOW + "Downloading " + ChatColor.WHITE + friendlyName);
        }
        loadDepQuiet(artifact, jarsFolder, classLoader);
    }

    private void loadDepQuiet(Artifact artifact, File jarsFolder, URLClassLoader classLoader) throws IOException, IllegalAccessException, InvocationTargetException {
        File output = new File(jarsFolder, artifact.getGroupId() + "-" + artifact.getArtifactId() + "-" + artifact.getVersion() + ".jar");
        if (DownloadUtil.hasFile(output)) {
            logger.info(artifact.getGroupId() + ":" + artifact.getArtifactId() + "::" + artifact.getVersion() + " exists, not downloading.");
            return;
        }

        logger.info("Downloading & injecting " + artifact.getGroupId() + ":" + artifact.getArtifactId() + "::" + artifact.getVersion());

        artifact.downloadJar(output);
        InjectUtil.injectFile(output, classLoader);

        for (Artifact dep : artifact.getDependencies()) {
            if (dep.getScope() == Scope.COMPILE || dep.getScope() == Scope.RUNTIME) {
                loadDepQuiet(dep, jarsFolder, classLoader);
            }
        }
    }

    private void log(Level level, String message) {
        getServer().getLogger().log(level, (isBukkit) ? ChatColor.stripColor(message) : message);
    }
}
