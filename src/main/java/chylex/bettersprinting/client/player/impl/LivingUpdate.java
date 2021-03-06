package chylex.bettersprinting.client.player.impl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.potion.Potion;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import chylex.bettersprinting.client.player.PlayerLogicHandler;

@SideOnly(Side.CLIENT)
final class LivingUpdate{
	public static void callPreSuper(EntityPlayerSP player, Minecraft mc, PlayerLogicHandler logic){		
		if (player.sprintingTicksLeft > 0 && --player.sprintingTicksLeft == 0)player.setSprinting(false);
		if (player.sprintToggleTimer > 0)--player.sprintToggleTimer;
		
		player.prevTimeInPortal = player.timeInPortal;
		
		if (player.inPortal){
			if (mc.currentScreen != null && !mc.currentScreen.doesGuiPauseGame())mc.displayGuiScreen(null);
			
			if (player.timeInPortal == 0F)mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("portal.trigger"),player.getRNG().nextFloat()*0.4F+0.8F));
			
			player.timeInPortal += 0.0125F;

			if (player.timeInPortal >= 1F){
				player.timeInPortal = 1F;
			}
			
			player.inPortal = false;
		}
		else if (player.isPotionActive(Potion.confusion) && player.getActivePotionEffect(Potion.confusion).getDuration() > 60){
			player.timeInPortal += 0.006666667F;
			if (player.timeInPortal > 1F)player.timeInPortal = 1F;
		}
		else{
			if (player.timeInPortal > 0F)player.timeInPortal -= 0.05F;
			if (player.timeInPortal < 0F)player.timeInPortal = 0F;
		}
		
		if (player.timeUntilPortal > 0)--player.timeUntilPortal;
		
		boolean wasJumping = player.movementInput.jump;
		logic.updateMovementInput();
		
		if (player.isUsingItem() && !player.isRiding()){
			player.movementInput.moveStrafe *= 0.2F;
			player.movementInput.moveForward *= 0.2F;
			player.sprintToggleTimer = 0;
		}
		
		AxisAlignedBB playerBoundingBox = player.getEntityBoundingBox();
		pushOutOfBlocks(player,player.posX-player.width*0.35D,playerBoundingBox.minY+0.5D,player.posZ+player.width*0.35D);
		pushOutOfBlocks(player,player.posX-player.width*0.35D,playerBoundingBox.minY+0.5D,player.posZ-player.width*0.35D);
		pushOutOfBlocks(player,player.posX+player.width*0.35D,playerBoundingBox.minY+0.5D,player.posZ-player.width*0.35D);
		pushOutOfBlocks(player,player.posX+player.width*0.35D,playerBoundingBox.minY+0.5D,player.posZ+player.width*0.35D);
		
		logic.updateLiving();
		
		if (player.capabilities.allowFlying){
			if (mc.playerController.isSpectatorMode()){
				if (!player.capabilities.isFlying){
					player.capabilities.isFlying = true;
					player.sendPlayerAbilities();
				}
			}
			else if (!wasJumping && player.movementInput.jump){
				if (player.flyToggleTimer == 0)player.flyToggleTimer = 7;
				else{
					player.capabilities.isFlying = !player.capabilities.isFlying;
					player.sendPlayerAbilities();
					player.flyToggleTimer = 0;
				}
			}
		}

		if (player.capabilities.isFlying && mc.getRenderViewEntity() == player){
			if (player.movementInput.sneak){
				player.motionY -= 0.15D;
			}

			if (player.movementInput.jump){
				player.motionY += 0.15D;
			}
		}

		if (player.isRidingHorse()){
			if (player.horseJumpPowerCounter < 0 && ++player.horseJumpPowerCounter == 0){
				player.horseJumpPower = 0F;
			}

			if (wasJumping && !player.movementInput.jump){
				player.horseJumpPowerCounter = -10;
				player.sendQueue.addToSendQueue(new C0BPacketEntityAction(player,C0BPacketEntityAction.Action.RIDING_JUMP,(int)(player.getHorseJumpPower()*100F)));
			}
			else if (!wasJumping && player.movementInput.jump){
				player.horseJumpPowerCounter = 0;
				player.horseJumpPower = 0F;
			}
			else if (wasJumping){
				if (++player.horseJumpPowerCounter < 10){
					player.horseJumpPower = player.horseJumpPowerCounter*0.1F;
				}
				else{
					player.horseJumpPower = 0.8F+2.0F/(player.horseJumpPowerCounter-9)*0.1F;
				}
			}
		}
		else player.horseJumpPower = 0F;
	}
	
	public static void callPostSuper(EntityPlayerSP player, Minecraft mc, PlayerLogicHandler logic){
		if (player.onGround && player.capabilities.isFlying && !mc.playerController.isSpectatorMode()){
			player.capabilities.isFlying = false;
			player.sendPlayerAbilities();
		}
	}
	
	// COPIED FROM EntityPlayerSP
	
	protected static boolean pushOutOfBlocks(EntityPlayerSP player, double x, double y, double z){
		if (player.noClip)return false;
		
		BlockPos pos = new BlockPos(x,y,z);
		double xDiff = x-pos.getX();
		double zDiff = z-pos.getZ();

		int entHeight = Math.max(Math.round(player.height),1);
		
		if (isHeadspaceFree(player,pos,entHeight)){
			byte side = -1;
			double limit = 9999D;

			if (!isHeadspaceFree(player,pos.west(),entHeight) && xDiff < limit){
				limit = xDiff;
				side = 0;
			}

			if (!isHeadspaceFree(player,pos.east(),entHeight) && 1D-xDiff < limit){
				limit = 1D-xDiff;
				side = 1;
			}

			if (!isHeadspaceFree(player,pos.north(),entHeight) && zDiff < limit){
				limit = zDiff;
				side = 4;
			}

			if (!isHeadspaceFree(player,pos.south(),entHeight) && 1D-zDiff < limit){
				limit = 1D-zDiff;
				side = 5;
			}

			if (side == 0)player.motionX = -0.1F;
			else if (side == 1)player.motionX = 0.1F;
			else if (side == 4)player.motionZ = -0.1F;
			else if (side == 5)player.motionZ = 0.1F;
		}

		return false;
	}
	
	private static boolean isBlockTranslucent(EntityPlayerSP player, BlockPos pos){
		return !player.worldObj.getBlockState(pos).getBlock().isNormalCube() && !player.worldObj.getBlockState(pos.up()).getBlock().isNormalCube();
	}

	private static boolean isHeadspaceFree(EntityPlayerSP player, BlockPos pos, int height){
		for(int yOffset = 0; yOffset < height; yOffset++){
			if (isBlockTranslucent(player,pos.add(0,yOffset,0)))return false;
		}
		
		return true;
	}
	
	private LivingUpdate(){}
}
