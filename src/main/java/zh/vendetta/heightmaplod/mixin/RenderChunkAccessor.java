package zh.vendetta.heightmaplod.mixin;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(RenderChunk.class)
public interface RenderChunkAccessor {
    @Accessor("compiledChunk")
    void setCompiledChunk(CompiledChunk compiled);

    @Invoker("preRenderBlocks")
    void preRenderLOD(BufferBuilder bufferBuilderIn, BlockPos pos);

    @Invoker("postRenderBlocks")
    void postRenderLOD(BlockRenderLayer layer, float x, float y, float z, BufferBuilder bufferBuilderIn, CompiledChunk compiledChunkIn);
}
