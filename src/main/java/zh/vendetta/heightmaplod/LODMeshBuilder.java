package zh.vendetta.heightmaplod;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class LODMeshBuilder {
    // Размер меша для одного чанка (16x16)
    public static void buildLODMesh(World world, BlockPos chunkPos, BufferBuilder builder) {
        builder.begin(7, DefaultVertexFormats.BLOCK);

        int baseX = chunkPos.getX();
        int baseZ = chunkPos.getZ();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = baseX + x;
                int worldZ = baseZ + z;
                int height = world.getHeight(worldX, worldZ);
                float h = (float) height;

                // простая квадратная "плитка" на высоте h
                float x0 = x, x1 = x + 1;
                float z0 = z, z1 = z + 1;

                builder.pos(x0, h, z0).color(0.5f, 0.8f, 0.5f, 1f).endVertex();
                builder.pos(x1, h, z0).color(0.5f, 0.8f, 0.5f, 1f).endVertex();
                builder.pos(x1, h, z1).color(0.5f, 0.8f, 0.5f, 1f).endVertex();
                builder.pos(x0, h, z1).color(0.5f, 0.8f, 0.5f, 1f).endVertex();
            }
        }

        builder.finishDrawing(); // завершаем буфер
    }
}
