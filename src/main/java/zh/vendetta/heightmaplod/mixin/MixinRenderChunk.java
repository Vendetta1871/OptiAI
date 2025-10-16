package zh.vendetta.heightmaplod.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RegionRenderCacheBuilder;
import net.minecraft.client.renderer.chunk.ChunkCompileTaskGenerator;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import zh.vendetta.heightmaplod.LODMeshBuilder;
import net.minecraft.client.renderer.chunk.VisGraph;

@Mixin(RenderChunk.class)
public abstract class MixinRenderChunk {

    @Inject(method = "rebuildChunk", at = @At("HEAD"), cancellable = true)
    private void onRebuildChunk(float x, float y, float z, ChunkCompileTaskGenerator generator, CallbackInfo ci) {
        RenderChunk self = (RenderChunk) (Object) this;
        BlockPos pos = self.getPosition();
        Minecraft mc = Minecraft.getMinecraft();
        World world = mc.world;
        if (world == null || mc.getRenderViewEntity() == null) return;

        double camX = mc.getRenderViewEntity().posX;
        double camZ = mc.getRenderViewEntity().posZ;
        double dx = self.getPosition().getX() + 8 - camX;
        double dz = self.getPosition().getZ() + 8 - camZ;
        double dist = Math.sqrt(dx * dx + dz * dz);

        if (dist > 128.0) {
            if (pos.getY() > 0) ci.cancel();
            // TODO: increment the chunk updates counter
            CompiledChunk compiled = new CompiledChunk();
            ((RenderChunkAccessor)self).setCompiledChunk(compiled);
            generator.setCompiledChunk(compiled);

            boolean[] used = new boolean[BlockRenderLayer.values().length];
            used[BlockRenderLayer.SOLID.ordinal()] = true;
            ((CompiledChunkAccessor) compiled).setLayersStarted(used);

            RegionRenderCacheBuilder cacheBuilder = generator.getRegionRenderCacheBuilder();
            BufferBuilder builder = cacheBuilder.getWorldRendererByLayer(BlockRenderLayer.SOLID);

            ((RenderChunkAccessor) self).preRenderLOD(builder, pos);
            if (dist > 256) LODMeshBuilder.buildLODMesh2(world, pos, builder);
            else LODMeshBuilder.buildLODMesh(world, pos, builder);

            ((CompiledChunkAccessor) compiled).setLayersUsed(used);
            ((RenderChunkAccessor) self).postRenderLOD(BlockRenderLayer.SOLID, pos.getX(), pos.getY(), pos.getY(), builder, compiled);

            compiled.setVisibility(new VisGraph().computeVisibility());
            //generator.setStatus(ChunkCompileTaskGenerator.Status.DONE);

            ci.cancel();
        }
    }
}
