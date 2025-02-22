package me.kzlyth.ultimateHome.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import me.kzlyth.ultimateHome.UltimateHome;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class HomeCommand implements CommandExecutor {
    private final UltimateHome plugin;
    public static final String GUI_TITLE = "ʜᴏᴍᴇ";
    public static final String CONFIRM_GUI_TITLE = ChatColor.GOLD + "Confirm deletion";

    public HomeCommand(UltimateHome plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;
        openHomeGUI(player);
        return true;
    }

    private void openHomeGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 36, GUI_TITLE);

        List<String> homes = getPlayerHomes(player);

        for (int i = 0; i < 5; i++) {
            if (i < homes.size()) {
                addHomeButton(gui, 11 + i, homes.get(i), Material.LIGHT_BLUE_BED, "Click to teleport to " + homes.get(i));
            } else {
                addHomeButton(gui, 11 + i, "Home " + (i + 1), Material.GRAY_BED, "No home set");
            }
        }

        for (int i = 0; i < 5; i++) {
            if (i < homes.size()) {
                addGuiItem(gui, 20 + i, Material.BLUE_DYE, ChatColor.BLUE + "Delete Home " + (i + 1), "Click to delete home " + (i + 1));
            } else {
                addGuiItem(gui, 20 + i, Material.GRAY_DYE, ChatColor.GRAY + "Set Home " + (i + 1), "Click to set home");
            }
        }

        player.openInventory(gui);
    }

    public void openConfirmGUI(Player player, int homeNumber) {
        Inventory gui = Bukkit.createInventory(null, 27, CONFIRM_GUI_TITLE + " " + homeNumber);

        ItemStack cancelItem = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta cancelMeta = cancelItem.getItemMeta();
        cancelMeta.setDisplayName(ChatColor.RED + "Cancel");
        cancelItem.setItemMeta(cancelMeta);
        gui.setItem(11, cancelItem);

        ItemStack homeItem = new ItemStack(Material.BLUE_DYE);
        ItemMeta homeMeta = homeItem.getItemMeta();
        homeMeta.setDisplayName(ChatColor.BLUE + "Delete Home " + homeNumber);
        List<String> lore = new ArrayList<>();
        homeMeta.setLore(lore);
        homeItem.setItemMeta(homeMeta);
        gui.setItem(13, homeItem);

        ItemStack confirmItem = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta confirmMeta = confirmItem.getItemMeta();
        confirmMeta.setDisplayName(ChatColor.GREEN + "Confirm");
        confirmItem.setItemMeta(confirmMeta);
        gui.setItem(15, confirmItem);

        player.openInventory(gui);
    }

    private List<String> getPlayerHomes(Player player) {
        List<String> homes = new ArrayList<>();
        File essentialsFolder = new File("plugins/Essentials/userdata");
        File userFile = new File(essentialsFolder, player.getUniqueId().toString() + ".yml");

        if (userFile.exists()) {
            YamlConfiguration userData = YamlConfiguration.loadConfiguration(userFile);
            if (userData.contains("homes")) {
                Set<String> homeNames = userData.getConfigurationSection("homes").getKeys(false);
                homes.addAll(homeNames);
            }
        }
        return homes;
    }

    private void addHomeButton(Inventory gui, int slot, String name, Material material, String description) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + name);

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + description);
        lore.add("");
        lore.add(ChatColor.YELLOW + "» Click to teleport");
        meta.setLore(lore);

        item.setItemMeta(meta);
        gui.setItem(slot, item);
    }

    private void addGuiItem(Inventory gui, int slot, Material material, String name, String description) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + description);
        meta.setLore(lore);

        item.setItemMeta(meta);
        gui.setItem(slot, item);
    }
}