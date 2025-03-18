package me.parthop69.titaniumsecurity.managers;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.UUID;

public class SecurityManager implements Listener {

    private final JavaPlugin plugin;
    private final File playerDataFile;
    private final FileConfiguration playerData;
    private final HashMap<UUID, Long> activeSessions = new HashMap<>();
    private final HashMap<UUID, Integer> loginAttempts = new HashMap<>();
    private final int maxLoginAttempts;
    private final int loginTimeout;

    public SecurityManager(JavaPlugin plugin) {
        this.plugin = plugin;

        // Initialize player data file
        this.playerDataFile = new File(plugin.getDataFolder(), "playerdata.yml");
        if (!playerDataFile.exists()) {
            try {
                playerDataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create playerdata.yml!");
                e.printStackTrace();
            }
        }

        this.playerData = YamlConfiguration.loadConfiguration(playerDataFile);

        // Load configuration properties
        FileConfiguration config = plugin.getConfig();
        this.maxLoginAttempts = config.getInt("max-login-attempts", 5);
        this.loginTimeout = config.getInt("login-timeout", 120);

        // Register this class as an event listener
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    // ----------------------------
    // Command Suppression
    // ----------------------------
    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage().toLowerCase(); // The full command

        // List of sensitive commands to suppress from logging
        if (message.startsWith("/login") || message.startsWith("/register")) {
            event.setCancelled(true); // Prevent the command from being logged

            // Process the command manually
            String[] args = message.split(" ");
            String commandLabel = args[0].substring(1); // Remove the leading slash
            String[] commandArgs = args.length > 1 ? message.substring(commandLabel.length() + 2).split(" ") : new String[0];

            // Execute the command without logging
            player.performCommand(commandLabel + " " + String.join(" ", commandArgs));
        }
    }

    // ----------------------------
    // Session Management
    // ----------------------------
    public void startSession(Player player) {
        activeSessions.put(player.getUniqueId(), System.currentTimeMillis());
        player.removePotionEffect(org.bukkit.potion.PotionEffectType.BLINDNESS); // Automatically clears blindness
    }

    public void endSession(Player player) {
        activeSessions.remove(player.getUniqueId());
    }

    public boolean isAuthenticated(Player player) {
        return activeSessions.containsKey(player.getUniqueId());
    }

    public boolean isSessionValid(Player player) {
        Long sessionStartTime = activeSessions.get(player.getUniqueId());
        if (sessionStartTime == null) {
            return false;
        }

        long elapsedTime = (System.currentTimeMillis() - sessionStartTime) / 1000; // Convert to seconds
        return elapsedTime <= loginTimeout;
    }

    // ----------------------------
    // Encryption Utilities
    // ----------------------------
    public String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            plugin.getLogger().severe("Error while hashing password!");
            e.printStackTrace();
            return null;
        }
    }

    // ----------------------------
    // Player Data Management
    // ----------------------------
    public void savePassword(UUID uuid, String hashedPassword) {
        playerData.set(uuid.toString(), hashedPassword);
        savePlayerDataFile();
    }

    public String getPassword(UUID uuid) {
        return playerData.getString(uuid.toString());
    }

    public void removePlayerData(UUID uuid) {
        playerData.set(uuid.toString(), null);
        savePlayerDataFile();
    }

    private void savePlayerDataFile() {
        try {
            playerData.save(playerDataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save playerdata.yml!");
            e.printStackTrace();
        }
    }

    // ----------------------------
    // Login Attempts Management
    // ----------------------------
    public void handleFailedAttempt(Player player) {
        UUID uuid = player.getUniqueId();
        loginAttempts.put(uuid, loginAttempts.getOrDefault(uuid, 0) + 1);

        if (loginAttempts.get(uuid) >= maxLoginAttempts) {
            player.kickPlayer(ChatColor.RED + "Too many failed attempts. You are banned for 15 minutes.");
            loginAttempts.remove(uuid);

            // Schedule unban task after 15 minutes
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> unbanPlayer(uuid), 15 * 60 * 20L); // 15 minutes in ticks
        } else {
            int attemptsLeft = maxLoginAttempts - loginAttempts.get(uuid);
            player.sendMessage(ChatColor.RED + "Incorrect password. Attempts left: " + attemptsLeft);
        }
    }

    public void resetLoginAttempts(UUID uuid) {
        loginAttempts.remove(uuid);
    }

    private void unbanPlayer(UUID uuid) {
        // Logic to unban the player (if integrated with a ban system)
        plugin.getLogger().info("Player with UUID " + uuid + " is now unbanned.");
    }

    // ----------------------------
    // Plugin Instance Getter
    // ----------------------------
    public JavaPlugin getPlugin() {
        return plugin;
    }
}
