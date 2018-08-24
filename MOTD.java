package me.infinity.plugins.multiplemotdbungee;

import net.md_5.bungee.api.Favicon;

public class MOTD {
	public String motd;
	public Favicon fav;
	
	public MOTD(String MOTD, Favicon fav) {
		this.fav = fav;
		this.motd = MOTD;		
	}

}
