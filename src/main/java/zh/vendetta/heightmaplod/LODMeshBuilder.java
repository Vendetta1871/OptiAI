package zh.vendetta.heightmaplod;

import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.World;
import zh.vendetta.heightmaplod.mixin.RenderChunkAccessor;

public class LODMeshBuilder {
    // Размер меша для одного чанка (16x16)
    public static void buildLODMesh(World world, BlockPos chunkPos, BufferBuilder builder, BlockRendererDispatcher dispatcher, ChunkCache worldView) {
        int baseX = chunkPos.getX();
        int baseZ = chunkPos.getZ();

        int skyLight = 15 << 20;
        int blockLight = 0 << 4;
        int packedLight = skyLight | blockLight;

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = baseX + x;
                int worldZ = baseZ + z;
                int height = world.getHeight(worldX, worldZ);
                float h = (float) height;

                dispatcher.renderBlock(Blocks.DIRT.getDefaultState(), new BlockPos(worldX, h, worldZ), worldView, builder); //TODO: remove


                float x0 = x, x1 = x + 1;
                float z0 = z, z1 = z + 1;

                float u0 = 0.0f;
                float u1 = 1.0f;
                float v0 = 0.0f;
                float v1 = 1.0f;

                float normX = 0.0f;
                float normY = 1.0f;
                float normZ = 0.0f;

                /*
                // Порядок вершин CCW (если смотреть СВЕРХУ, Y+), нормаль будет направлена ВВЕРХ
                // Вершина 1
                builder.pos(x0, h, z1)
                        .color(0.5f, 0.8f, 0.5f, 1f) // RGBA
                        .tex(u0, v1)                   // UV
                        .lightmap(packedLight, packedLight) // SkyLight, BlockLight
                        .normal(normX, normY, normZ)   // NX, NY, NZ
                        .endVertex();

                // Вершина 2
                builder.pos(x1, h, z1)
                        .color(0.5f, 0.8f, 0.5f, 1f)
                        .tex(u1, v1)
                        .lightmap(packedLight, packedLight)
                        .normal(normX, normY, normZ)
                        .endVertex();

                // Вершина 3
                builder.pos(x1, h, z0)
                        .color(0.5f, 0.8f, 0.5f, 1f)
                        .tex(u1, v0)
                        .lightmap(packedLight, packedLight)
                        .normal(normX, normY, normZ)
                        .endVertex();

                // Вершина 4
                builder.pos(x0, h, z0)
                        .color(0.5f, 0.8f, 0.5f, 1f)
                        .tex(u0, v0)
                        .lightmap(packedLight, packedLight)
                        .normal(normX, normY, normZ)
                        .endVertex();

                 */
            }
        }
    }
}
