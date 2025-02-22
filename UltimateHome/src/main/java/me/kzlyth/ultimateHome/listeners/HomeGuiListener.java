package me.kzlyth.ultimateHome.listeners;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import me.kzlyth.ultimateHome.UltimateHome;
import me.kzlyth.ultimateHome.commands.HomeCommand;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

public class HomeGuiListener implements Listener {
    private final UltimateHome plugin;
    private final HashMap<UUID, Location> teleporting = new HashMap<>();
    private final HashMap<UUID, BukkitRunnable> tasks = new HashMap<>();

    public HomeGuiListener(UltimateHome plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();

        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null) return;

        if (title.equals(HomeCommand.GUI_TITLE)) {
            handleMainGuiClick(player, event.getSlot(), clicked);
        }
        else if (title.startsWith(HomeCommand.CONFIRM_GUI_TITLE)) {
            handleConfirmGuiClick(player, clicked, title);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (teleporting.containsKey(player.getUniqueId())) {
            Location from = teleporting.get(player.getUniqueId());
            Location to = event.getTo();

            if (from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ()) {
                cancelTeleport(player);
            }
        }
    }

    private void handleMainGuiClick(Player player, int slot, ItemStack clicked) {
        if (slot >= 11 && slot <= 15 && clicked.getType().toString().contains("BED")) {
            String homeName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
            if (clicked.getType() != Material.GRAY_BED) {
                startTeleport(player, homeName);
            }
            return;
        }

        if (slot >= 20 && slot <= 24 && clicked.getType() == Material.BLUE_DYE) {
            int homeNumber = slot - 19;
            ((HomeCommand) plugin.getCommand("home").getExecutor()).openConfirmGUI(player, homeNumber);
        }
    }

    private void startTeleport(Player player, String homeName) {
        player.closeInventory();

        teleporting.put(player.getUniqueId(), player.getLocation());

        BukkitRunnable task = new BukkitRunnable() {
            int countdown = 3;

            @Override
            public void run() {
                if (countdown > 0) {
                    player.spigot().sendMessage(
                            ChatMessageType.ACTION_BAR,
                            new TextComponent(ChatColor.YELLOW + "Teleporting in " + countdown + " | Don't move!")
                    );
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                    countdown--;
                } else {
                    player.spigot().sendMessage(
                            ChatMessageType.ACTION_BAR,
                            new TextComponent(ChatColor.GREEN + "Teleporting...")
                    );
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
                    teleportToHome(player, homeName);
                    teleporting.remove(player.getUniqueId());
                    tasks.remove(player.getUniqueId());
                    this.cancel();
                }
            }
        };

        tasks.put(player.getUniqueId(), task);
        task.runTaskTimer(plugin, 0L, 20L);
    }

    private void cancelTeleport(Player player) {
        UUID uuid = player.getUniqueId();
        if (tasks.containsKey(uuid)) {
            tasks.get(uuid).cancel();
            tasks.remove(uuid);
        }
        teleporting.remove(uuid);
        player.spigot().sendMessage(
                ChatMessageType.ACTION_BAR,
                new TextComponent(ChatColor.RED + "Teleport cancelled because you Moved.")
        );
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
    }

    private void handleConfirmGuiClick(Player player, ItemStack clicked, String title) {
        int homeNumber = Integer.parseInt(title.substring(title.length() - 1));

        switch (clicked.getType()) {
            case RED_STAINED_GLASS_PANE:
                player.closeInventory();
                break;

            case LIME_STAINED_GLASS_PANE:
                UUID uuid = player.getUniqueId();
                ItemStack[] contents = player.getOpenInventory().getTopInventory().getContents();
                String homeName = ChatColor.stripColor(contents[13].getItemMeta().getDisplayName());
                deleteHome(uuid, homeName);
                player.closeInventory();
                plugin.getCommand("home").execute(player, "home", new String[]{});
                break;
        }
    }

    private void deleteHome(UUID uuid, String homeName) {
        File userFile = new File("plugins/Essentials/userdata", uuid.toString() + ".yml");
        if (!userFile.exists()) return;

        try {
            YamlConfiguration userData = YamlConfiguration.loadConfiguration(userFile);

            if (!userData.contains("homes." + homeName)) {
                Bukkit.getPlayer(uuid).sendMessage(ChatColor.RED + "This home doesn't exist!");
                return;
            }

            userData.set("homes." + homeName, null);
            userData.save(userFile);

            Bukkit.getPlayer(uuid).sendMessage(ChatColor.GREEN + "Home '" + homeName + "' has been deleted!");

        } catch (IOException e) {
            Bukkit.getPlayer(uuid).sendMessage(ChatColor.RED + "Error deleting home!");
            e.printStackTrace();
        }
    }

    private void teleportToHome(Player player, String homeName) {
        File userFile = new File("plugins/Essentials/userdata", player.getUniqueId().toString() + ".yml");
        if (!userFile.exists()) return;

        YamlConfiguration userData = YamlConfiguration.loadConfiguration(userFile);
        if (!userData.contains("homes." + homeName)) {
            player.sendMessage(ChatColor.RED + "This home doesn't exist!");
            return;
        }

        String worldName = userData.getString("homes." + homeName + ".world-name");
        double x = userData.getDouble("homes." + homeName + ".x");
        double y = userData.getDouble("homes." + homeName + ".y");
        double z = userData.getDouble("homes." + homeName + ".z");
        float yaw = (float) userData.getDouble("homes." + homeName + ".yaw");
        float pitch = (float) userData.getDouble("homes." + homeName + ".pitch");

        Location loc = new Location(plugin.getServer().getWorld(worldName), x, y, z, yaw, pitch);
        player.teleport(loc);
        player.sendMessage(ChatColor.GREEN + "Teleported to home '" + homeName + "'!");
    }
}