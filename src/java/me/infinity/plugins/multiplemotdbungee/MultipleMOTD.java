package me.infinity.plugins.multiplemotdbungee;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.Favicon;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.event.EventHandler;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class MultipleMOTD extends Plugin implements Listener{

    private Configuration cfg;
    private Map<String, MOTD> Map = new HashMap<>();
    private MOTD defaultMotd;


    @Override
    public void onEnable() {
        getLogger().info("Infinity injected.");
        defaultCfg();
        this.getProxy().getPluginManager().registerListener(this, this);
        try {
            cfg = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "config.yml"));

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        File deF = new File(this.getDataFolder() + File.separator + "default_icon.png");
        if(!deF.exists()) {
            try {
                InputStream is = getClass().getResourceAsStream("/default.png");
                BufferedImage as = ImageIO.read(is);
                File outputfile = new File(this.getDataFolder() + File.separator + "default_icon.png");
                ImageIO.write(as, "png", outputfile);
            }catch(Exception io) {

            }
        }
        Favicon defaultIcon = null;
        try {
            defaultIcon = Favicon.create(ImageIO.read(deF));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        defaultMotd = new MOTD(cfg.getString("Default.motd").replace("&", ChatColor.COLOR_CHAR + ""), defaultIcon);

        int in = 1;
        for(String s : cfg.getSection("Servers").getKeys()) {
            String motd = cfg.getString("Servers." + s + ".motd");
            String ip = cfg.getString("Servers." + s + ".ip").toLowerCase();
            String file_name = cfg.getString("Servers." + s + ".file");
            Favicon icon = null;
            try {
                File f = new File(this.getDataFolder() + File.separator + file_name);
                icon = Favicon.create(ImageIO.read(f));
                System.out.println("Motd " + in + " loaded");
                in++;
            }catch(Exception i) {
                System.out.println("Error in config: Servers." + s );
                in++;
                continue;
            }
            MOTD m = new MOTD(motd.replace("&", ChatColor.COLOR_CHAR + ""), icon);
            Map.put(ip, m);
        }

        if(cfg.getBoolean("use-communication-channel")){
            getProxy().registerChannel(cfg.getString("communication-channel"));
            System.out.println("[MultiMOTD] Communication Channel connection has been established!");
        }

    }
    @EventHandler
    public void setMOTD(ProxyPingEvent e) {
        ServerPing ping = e.getResponse();
        if(Map.containsKey(e.getConnection().getVirtualHost().getHostString().toLowerCase())) {
            MOTD m = Map.get(e.getConnection().getVirtualHost().getHostString().toLowerCase());
            ping.setFavicon(m.fav);
            //ping.setDescription(m.motd);
            BaseComponent component = new  TextComponent(m.motd);
            ping.setDescriptionComponent(component);
        }
        else {
            ping.setFavicon(defaultMotd.fav);
            BaseComponent component = new  TextComponent(defaultMotd.motd);
            ping.setDescriptionComponent(component);

        }
        e.setResponse(ping);
    }

    @EventHandler
    public void onConnected(ServerConnectedEvent e){
        if(!cfg.getBoolean("use-communication-channel"))return;
        ProxiedPlayer player = e.getPlayer();
        String message = cfg.getString("communication-message-format");
        if(Map.containsKey(e.getPlayer().getPendingConnection().getVirtualHost().getHostString().toLowerCase())){
            //Communicate with desired server
            sendMessage(message.replace("<Player>", player.getName())
                            .replace("<Server>", e.getPlayer().getPendingConnection().getVirtualHost().getHostString().toLowerCase())
                    , e.getServer().getInfo());

        }
        else{
            //Communicate with default
            sendMessage(message.replace("<Player>", player.getName())
                            .replace("<Server>", "default")
                    , e.getServer().getInfo());

        }

        if(!cfg.getBoolean("log-messages"))return;
        System.out.println("[Multiple MOTD] " + message);


    }

    public void sendMessage(final String message, final ServerInfo server) {
        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        final DataOutputStream out = new DataOutputStream(stream);
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
        server.sendData(cfg.getString("communication-channel"), stream.toByteArray());
    }


    public void defaultCfg() {
        if (!getDataFolder().exists())
            getDataFolder().mkdir();

        File file = new File(getDataFolder(), "config.yml");


        if (!file.exists()) {
            try (InputStream in = getResourceAsStream("config.yml")) {
                Files.copy(in, file.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }



}
