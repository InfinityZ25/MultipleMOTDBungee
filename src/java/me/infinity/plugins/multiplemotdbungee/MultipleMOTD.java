package me.infinity.plugins.multiplemotdbungee;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.Favicon;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.event.EventHandler;

public class MultipleMOTD extends Plugin implements Listener{

	public Configuration cfg;
	public Map<String, MOTD> Map = new HashMap<>();
	public MOTD defaultMotd;


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
