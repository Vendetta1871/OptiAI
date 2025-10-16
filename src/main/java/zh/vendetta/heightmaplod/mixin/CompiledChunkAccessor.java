package zh.vendetta.heightmaplod.mixin;

import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.util.BlockRenderLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CompiledChunk.class)
public interface CompiledChunkAccessor {
    @Accessor("layersUsed")
    void setLayersUsed(boolean[] used);

    @Accessor("layersStarted")
    void setLayersStarted(boolean[] layer);
}
