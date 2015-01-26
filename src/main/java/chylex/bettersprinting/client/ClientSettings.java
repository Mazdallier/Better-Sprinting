package chylex.bettersprinting.client;
import chylex.bettersprinting.BetterSprintingConfig;
import chylex.bettersprinting.client.compatibility.OldClientConfig;

public class ClientSettings{
	public static int keyCodeSprintHold = 29;
	public static int keyCodeSprintToggle = 34;
	public static int keyCodeSneakToggle = 21;
	public static int keyCodeOptionsMenu = 24;
	
	public static byte flySpeedBoost = 3;
	public static boolean enableDoubleTap = false;
	public static boolean enableAllDirs = false;
	public static boolean disableMod = false;
	
	public static boolean enableUpdateNotifications = true;
	public static boolean enableBuildCheck = true;
	public static boolean showedSneakWarning = false;
	
	public static void refresh(BetterSprintingConfig config){
		config.setCategory("client");
		OldClientConfig.loadAndDeleteOldConfig();
		
		keyCodeSprintHold = config.getInt("keySprintHold",keyCodeSprintHold,"").setShowInGui(false).getInt();
		keyCodeSprintToggle = config.getInt("keySprintToggle",keyCodeSprintToggle,"").setShowInGui(false).getInt();
		keyCodeSneakToggle = config.getInt("keySneakToggle",keyCodeSneakToggle,"").setShowInGui(false).getInt();
		keyCodeOptionsMenu = config.getInt("keyOptionsMenu",keyCodeOptionsMenu,"").setShowInGui(false).getInt();
		
		flySpeedBoost = (byte)config.getInt("flySpeedBoost",flySpeedBoost,"").setShowInGui(false).getInt();
		enableDoubleTap = config.getBool("enableDoubleTap",enableDoubleTap,"").setShowInGui(false).getBoolean();
		enableAllDirs = config.getBool("enableAllDirs",enableAllDirs,"").setShowInGui(false).getBoolean();
		disableMod = config.getBool("disableMod",disableMod,"").setShowInGui(false).getBoolean();
		
		enableUpdateNotifications = config.getBool("enableUpdateNotifications",enableUpdateNotifications,"").getBoolean();
		enableBuildCheck = config.getBool("enableBuildCheck",enableBuildCheck,"").getBoolean();
		showedSneakWarning = config.getBool("showedSneakWarning",showedSneakWarning,"").setShowInGui(false).getBoolean();
		
		ClientModManager.keyBindSprintHold.setKeyCode(keyCodeSprintHold);
		ClientModManager.keyBindSprintToggle.setKeyCode(keyCodeSprintToggle);
		ClientModManager.keyBindSneakToggle.setKeyCode(keyCodeSneakToggle);
		ClientModManager.keyBindSprintMenu.setKeyCode(keyCodeOptionsMenu);
		
		config.forceSave();
	}
}
