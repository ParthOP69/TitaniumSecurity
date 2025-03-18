package me.parthop69.titaniumsecurity;

import me.parthop69.titaniumsecurity.commands.AuthCommand;
import me.parthop69.titaniumsecurity.listeners.PlayerRestrictionListener;
import me.parthop69.titaniumsecurity.managers.SecurityManager;
import org.bukkit.plugin.java.JavaPlugin;

public class TitaniumSecurity extends JavaPlugin {

    private SecurityManager securityManager;

    @Override
    public void onEnable() {
        // Load configuration and initialize plugin
        saveDefaultConfig();
        getLogger().info("TitaniumSecurity has been enabled!");

        // Initialize SecurityManager
        this.securityManager = new SecurityManager(this);

        // Register commands
        AuthCommand authCommand = new AuthCommand(securityManager);
        getCommand("login").setExecutor(authCommand);
        getCommand("logout").setExecutor(authCommand);
        getCommand("register").setExecutor(authCommand);
        getCommand("unregister").setExecutor(authCommand);
        getCommand("changepassword").setExecutor(authCommand);

        // Register event listeners
        getServer().getPluginManager().registerEvents(new PlayerRestrictionListener(securityManager), this);

        // SecurityManager is already a listener (handles command suppression)
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("TitaniumSecurity has been disabled!");
    }

    public SecurityManager getSecurityManager() {
        return securityManager;
    }
}
