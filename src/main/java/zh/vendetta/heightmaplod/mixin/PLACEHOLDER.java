package zh.vendetta.heightmaplod.mixin;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityPlayer.class)
public abstract class PLACEHOLDER {

    /**
     * Наше новое поле в классе EntityPlayer - эссенция
     */
    private float essence;

    /**
     * Виртуальное поле, которое ссылается на реальное поле experienceLevel в классе EntityPlayer
     */
    @Shadow(remap = true)
    public int experienceLevel;

    public float getEssence() {
        return this.essence;
    }


    public void setEssence(float value) {
        this.essence = Math.min(value, getMaxEssence());
    }


    public float getMaxEssence() {
        return this.experienceLevel;
    }

    public void setMaxEssence(float value) {
        // NOOP
        System.out.println("Максимальное количество эссенции зависит от уровня игрока!");
    }

    @Inject(method = "writeEntityToNBT", at = @At("TAIL"))
    private void onWriteEntityToNBT(NBTTagCompound compound, CallbackInfo ci) {
        compound.setFloat("Essence", this.getEssence());
    }

    @Inject(method = "readEntityFromNBT", at = @At("TAIL"))
    private void onReadEntityFromNBT(NBTTagCompound compound, CallbackInfo ci) {
        setEssence(compound.getFloat("Essence"));
    }
}