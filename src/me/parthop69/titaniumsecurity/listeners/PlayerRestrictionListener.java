package me.parthop69.titaniumsecurity.listeners;

import me.parthop69.titaniumsecurity.managers.SecurityManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PlayerRestrictionListener implements Listener {

    private final SecurityManager securityManager;

    public PlayerRestrictionListener(SecurityManager securityManager) {
        this.securityManager = securityManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!securityManager.isAuthenticated(player)) {
            // Send a join message indicating authentication is required
            player.sendMessage(ChatColor.RED + securityManager.getPlugin().getConfig()
                    .getString("join-message", "Authentication Required: Please login or register to continue."));

            // Apply blindness if configured in the plugin's config file
            if (securityManager.getPlugin().getConfig().getBoolean("apply-blindness", true)) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 1));
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!securityManager.isAuthenticated(player)) {
            // Prevent movement for unauthenticated players
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (!securityManager.isAuthenticated(player)) {
                // Prevent damage for unauthenticated players
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            if (!securityManager.isAuthenticated(player)) {
                // Prevent inventory usage for unauthenticated players
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You cannot use your inventory until you login or register.");
            }
        }
    }
}
