package zh.vendetta.heightmaplod.mixin;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.util.BlockRenderLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(RenderChunk.class)
public interface RenderChunkAccessor {
    @Accessor("compiledChunk")
    void setCompiledChunk(CompiledChunk compiled);

    //@Invoker("uploadChunk")
    //void callUploadChunk(BlockRenderLayer layer, BufferBuilder buffer, CompiledChunk compiled);
}
