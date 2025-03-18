package me.parthop69.titaniumsecurity.commands;

import me.parthop69.titaniumsecurity.managers.SecurityManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

public class AuthCommand implements CommandExecutor {

    private final SecurityManager securityManager;

    public AuthCommand(SecurityManager securityManager) {
        this.securityManager = securityManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        switch (label.toLowerCase()) {
            case "login":
                handleLogin(player, args);
                break;
            case "logout":
                handleLogout(player);
                break;
            case "register":
                handleRegister(player, args);
                break;
            case "unregister":
                handleUnregister(player, args);
                break;
            case "changepassword":
                handleChangePassword(player, args);
                break;
            default:
                player.sendMessage(ChatColor.RED + "Unknown command.");
        }

        return true;
    }

    private void handleLogin(Player player, String[] args) {
        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Usage: /login <password>");
            return;
        }

        String enteredPassword = args[0];
        String savedPassword = securityManager.getPassword(player.getUniqueId());

        if (savedPassword != null && savedPassword.equals(securityManager.hashPassword(enteredPassword))) {
            securityManager.startSession(player);
            player.removePotionEffect(PotionEffectType.BLINDNESS); // Clear blindness on successful login
            player.sendMessage(ChatColor.GREEN + "You have successfully logged in!");
            player.sendMessage(ChatColor.AQUA + "Do you liked the Authentication system in this server? ");
            player.sendMessage(ChatColor.RED + "You want to use it in your server? Go through the below link :");
            player.sendMessage(ChatColor.YELLOW + "https://modrinth.com/plugin/titaniumsecurity");
        } else {
            securityManager.handleFailedAttempt(player);
        }
    }

    private void handleLogout(Player player) {
        if (securityManager.isAuthenticated(player)) {
            securityManager.endSession(player);
            player.sendMessage(ChatColor.YELLOW + "You have successfully logged out.");
        } else {
            player.sendMessage(ChatColor.RED + "You are not logged in.");
        }
    }

    private void handleRegister(Player player, String[] args) {
        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Usage: /register <password>");
            return;
        }

        String password = securityManager.hashPassword(args[0]);
        securityManager.savePassword(player.getUniqueId(), password);
        player.sendMessage(ChatColor.GREEN + "Registration successful! You can now log in.");
    }

    private void handleUnregister(Player player, String[] args) {
        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Usage: /unregister <password>");
            return;
        }

        String enteredPassword = args[0];
        String savedPassword = securityManager.getPassword(player.getUniqueId());

        if (savedPassword != null && savedPassword.equals(securityManager.hashPassword(enteredPassword))) {
            securityManager.removePlayerData(player.getUniqueId());
            securityManager.endSession(player);
            player.sendMessage(ChatColor.YELLOW + "Your account has been unregistered.");
        } else {
            player.sendMessage(ChatColor.RED + "Incorrect password. Unable to unregister.");
        }
    }

    private void handleChangePassword(Player player, String[] args) {
        if (args.length != 2) {
            player.sendMessage(ChatColor.RED + "Usage: /changepassword <old-password> <new-password>");
            return;
        }

        String oldPassword = args[0];
        String newPassword = args[1];
        String savedPassword = securityManager.getPassword(player.getUniqueId());

        if (savedPassword != null && savedPassword.equals(securityManager.hashPassword(oldPassword))) {
            securityManager.savePassword(player.getUniqueId(), securityManager.hashPassword(newPassword));
            player.sendMessage(ChatColor.GREEN + "Password changed successfully!");
        } else {
            player.sendMessage(ChatColor.RED + "Incorrect old password. Unable to change password.");
        }
    }
}
