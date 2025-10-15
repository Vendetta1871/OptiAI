package zh.vendetta.heightmaplod.mixin;

import zh.vendetta.heightmaplod.HeightmapLODManager;
import zh.vendetta.heightmaplod.HeightmapLODRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockRenderLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderGlobal.class)
public class MixinRenderGlobal {

    @Inject(
            method = "setupTerrain",
            at = @At("RETURN")
    )
    private void onSetupTerrain(Entity viewEntity, double partialTicks, ICamera camera, int frameCount, boolean playerSpectator, CallbackInfo ci) {
        HeightmapLODManager.prepare(Minecraft.getMinecraft(), viewEntity, camera);
    }

    @Inject(
            method = "renderBlockLayer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/RenderHelper;disableStandardItemLighting()V",
                    shift = At.Shift.AFTER
            )
    )
    private void onRenderBlockLayerPre(BlockRenderLayer layer, double partialTicks, int pass, Entity entity, CallbackInfoReturnable<Integer> ci) {
        if (layer == BlockRenderLayer.SOLID) {
            HeightmapLODRenderer.render(Minecraft.getMinecraft(), true);
        }
    }

    @Inject(
            method = "renderBlockLayer",
            at = @At("RETURN")
    )
    private void onRenderBlockLayerPost(BlockRenderLayer layer, double partialTicks, int pass, Entity entity, CallbackInfoReturnable<Integer> ci) {
        if (layer == BlockRenderLayer.TRANSLUCENT) {
           HeightmapLODRenderer.render(Minecraft.getMinecraft(), false);
        }
    }
}
