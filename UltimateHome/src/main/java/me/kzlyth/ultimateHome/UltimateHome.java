package me.kzlyth.ultimateHome;

import me.kzlyth.ultimateHome.commands.HomeCommand;
import me.kzlyth.ultimateHome.listeners.HomeGuiListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.logging.Logger;

public final class UltimateHome extends JavaPlugin {
    private final Logger logger = Bukkit.getLogger();

    @Override
    public void onEnable() {
        getCommand("home").setExecutor(new HomeCommand(this));
        getServer().getPluginManager().registerEvents(new HomeGuiListener(this), this);

        logger.info(ChatColor.GREEN + "================================");
        logger.info(ChatColor.GOLD + "[UltimateHome] Plugin is starting up!");
        logger.info(ChatColor.AQUA + "Developer: KzLyth");
        logger.info(ChatColor.AQUA + "Version: " + getDescription().getVersion());
        logger.info(ChatColor.AQUA + "Support Discord: https://discord.gg/jyrjcrGMkw");
        logger.info(ChatColor.AQUA + "GitHub: github.com/Kzlyth");
        logger.info(ChatColor.GREEN + "================================");
    }

    @Override
    public void onDisable() {
        logger.info(ChatColor.RED + "[UltimateHome] Plugin is shutting down...");
    }
}