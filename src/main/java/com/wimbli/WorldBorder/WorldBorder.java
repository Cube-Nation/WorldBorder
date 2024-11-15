package com.wimbli.WorldBorder;

import com.github.yannicklamprecht.worldborder.api.WorldBorderApi;
import org.bukkit.Location;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;


public class WorldBorder extends JavaPlugin
{
	public static volatile WorldBorder plugin = null;
	public static volatile WBCommand wbCommand = null;
	private BlockPlaceListener blockPlaceListener = null;
	private MobSpawnListener mobSpawnListener = null;

	private WorldBorderApi worldBorderApi;

	@Override
	public void onEnable()
	{
		if (plugin == null)
			plugin = this;
		if (wbCommand == null)
			wbCommand = new WBCommand();

		// Load (or create new) config file
		Config.load(this, false);

		// our one real command, though it does also have aliases "wb" and "worldborder"
		getCommand("wborder").setExecutor(wbCommand);

		// keep an eye on teleports, to redirect them to a spot inside the border if necessary
		getServer().getPluginManager().registerEvents(new WBListener(), this);
		
		if (Config.preventBlockPlace()) 
			enableBlockPlaceListener(true);

		if (Config.preventMobSpawn())
			enableMobSpawnListener(true);

		// integrate with DynMap if it's available
		DynMapFeatures.setup();

		// integrate with WorldBorderApi if it's available
		RegisteredServiceProvider<WorldBorderApi> apiProvider = plugin.getServer().getServicesManager().getRegistration(WorldBorderApi.class);
		if (apiProvider != null) {
			worldBorderApi = apiProvider.getProvider();
			new FakeBorderManager(this);
			getServer().getPluginManager().registerEvents(new FakeBorderListener(this), this);
		} else {
			plugin.getLogger().info("Vanilla WorldBorder API not found");
		}

		// Well I for one find this info useful, so...
		Location spawn = getServer().getWorlds().get(0).getSpawnLocation();
		Config.log("For reference, the main world's spawn location is at X: " + Config.coord.format(spawn.getX()) + " Y: " + Config.coord.format(spawn.getY()) + " Z: " + Config.coord.format(spawn.getZ()));
	}

	@Override
	public void onDisable()
	{
		DynMapFeatures.removeAllBorders();
		Config.StopBorderTimer();
		Config.StoreFillTask();
		Config.StopFillTask(true);
	}

	// for other plugins to hook into
	public BorderData getWorldBorder(String worldName)
	{
		return Config.Border(worldName);
	}

	/**
	 * @deprecated  Replaced by {@link #getWorldBorder(String worldName)};
	 * this method name starts with an uppercase letter, which it shouldn't
	 */
	@Deprecated
	public BorderData GetWorldBorder(String worldName)
	{
		return getWorldBorder(worldName);
	}

	public void enableBlockPlaceListener(boolean enable)
	{
		if (enable) 
			getServer().getPluginManager().registerEvents(this.blockPlaceListener = new BlockPlaceListener(), this);
		else if (blockPlaceListener != null)
			blockPlaceListener.unregister();
	}

	public void enableMobSpawnListener(boolean enable)
	{
		if (enable)
			getServer().getPluginManager().registerEvents(this.mobSpawnListener = new MobSpawnListener(), this);
		else if (mobSpawnListener != null)
			mobSpawnListener.unregister();
	}

	public WorldBorderApi getWorldBorderApi() {
		return worldBorderApi;
	}

}
