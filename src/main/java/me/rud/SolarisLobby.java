package me.rud;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class SolarisLobby extends JavaPlugin implements Listener {

    private Location spawnLocation;
    private String joinTitle;
    private String joinSubtitle;
    private Sound joinSound;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getCommand("setspawn").setExecutor(this);

        loadConfigurations();
        loadSpawnLocation();
    }

    private void loadConfigurations() {
        FileConfiguration config = getConfig();
        joinTitle = config.getString("joinTitle", "&e&lWELCOME");
        joinSubtitle = config.getString("joinSubtitle", "&fserver.net");
        joinSound = Sound.valueOf(config.getString("joinSound", "ENTITY_PLAYER_LEVELUP"));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("setspawn")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                spawnLocation = player.getLocation();
                saveSpawnLocation();
                sender.sendMessage("Punto de aparición colocado con éxito.");
            } else {
                sender.sendMessage("Este comando solo puede ser ejecutado por un jugador.");
            }
            return true;
        }
        return false;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (spawnLocation != null) {
            player.teleport(spawnLocation);
            sendWelcomeTitle(player);
            playWelcomeSound(player);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        e.setJoinMessage("");
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        e.setQuitMessage("");
    }

    @EventHandler
    public void onLoseHunger(FoodLevelChangeEvent e) {
        if (e.getEntity() instanceof org.bukkit.entity.Player)
            e.setCancelled(true);
    }

    @EventHandler
    final void onPlayerDeathEvent(final PlayerDeathEvent event) {
        Bukkit.getScheduler().scheduleSyncDelayedTask((Plugin) this, new Runnable() {
            public void run() {
                event.getEntity().spigot().respawn();
            }
        }, 4L);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            e.setCancelled(true);
        }
    }

    private void sendWelcomeTitle(Player player) {
        String title = joinTitle.replace("&", "§");
        String subtitle = joinSubtitle.replace("&", "§");
        int fadeIn = 10;
        int stay = 70;
        int fadeOut = 20;
        player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
    }

    private void playWelcomeSound(Player player) {
        player.playSound(player.getLocation(), joinSound, 1.0f, 1.0f);
    }

    private void saveSpawnLocation() {
        FileConfiguration config = getConfig();
        config.set("spawnLocation.world", spawnLocation.getWorld().getName());
        config.set("spawnLocation.x", spawnLocation.getX());
        config.set("spawnLocation.y", spawnLocation.getY());
        config.set("spawnLocation.z", spawnLocation.getZ());
        config.set("spawnLocation.yaw", spawnLocation.getYaw());
        config.set("spawnLocation.pitch", spawnLocation.getPitch());
        saveConfig();
    }

    private void loadSpawnLocation() {
        FileConfiguration config = getConfig();
        String worldName = config.getString("spawnLocation.world");
        double x = config.getDouble("spawnLocation.x");
        double y = config.getDouble("spawnLocation.y");
        double z = config.getDouble("spawnLocation.z");
        float yaw = (float) config.getDouble("spawnLocation.yaw");
        float pitch = (float) config.getDouble("spawnLocation.pitch");
        spawnLocation = new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
    }
}
