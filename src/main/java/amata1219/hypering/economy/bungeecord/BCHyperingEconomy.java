package amata1219.hypering.economy.bungeecord;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.google.common.io.ByteStreams;

import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public class BCHyperingEconomy extends Plugin{

	private static BCHyperingEconomy plugin;

	private BCManager manager;
	private Configuration config;

	@Override
	public void onEnable(){
		plugin = this;

		File file = new File(getDataFolder(), "bcside_config.yml");
		if(!file.exists()){
			try{
				getDataFolder().mkdirs();
				file.createNewFile();
			}catch(IOException e){

			}
			try(FileOutputStream output = new FileOutputStream(file);
					InputStream input = getResourceAsStream("bcside_config.yml")) {
					ByteStreams.copy(input, output);
			}catch(IOException e){
				e.printStackTrace();
				return;
			}
		}
		try{
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
		}catch(IOException e){
			e.printStackTrace();
		}

		getProxy().registerChannel("BungeeCord");

		getProxy().getPluginManager().registerListener(plugin, manager = new BCManager());
		manager.startTaskRunnable();
	}

	@Override
	public void onDisable(){
		manager.stopTaskRunnable();
		manager.getPlayerDataMap().values().forEach(data -> data.save());
	}

	public static BCHyperingEconomy getPlugin(){
		return plugin;
	}

	public BCHyperingEconomyAPI getBCHyperingEconomyAPI(){
		return manager;
	}

	public Configuration getConfig(){
		return config;
	}

}
