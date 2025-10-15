package zh.vendetta.heightmaplod;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import org.lwjgl.opengl.GL11;

public class HeightmapLODMesh {

    private final int[][] heightmap = new int[16][16];
    private final int[][] colormap = new int[16][16];
    private final int chunkX, chunkZ;

    private HeightmapLODMesh(int chunkX, int chunkZ) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
    }

    public static HeightmapLODMesh fromChunk(Chunk chunk) {
        HeightmapLODMesh mesh = new HeightmapLODMesh(chunk.x, chunk.z);
        net.minecraft.world.World world = chunk.getWorld();

        boolean hasData = false;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int h = chunk.getHeightValue(x, z);
                if (h > 0 && h < 255) {
                    mesh.heightmap[x][z] = h;
                    BlockPos pos = new BlockPos(chunk.x * 16 + x, h, chunk.z * 16 + z);
                    mesh.colormap[x][z] = getAverageBlockColor(world, pos);
                    hasData = true;
                }
            }
        }
        return hasData ? mesh : null;
    }

    public void render(boolean solidPass) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.BLOCK);

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int y = heightmap[x][z];
                if (y <= 0) continue;

                int color = colormap[x][z];
                float r = ((color >> 16) & 0xFF) / 255f;
                float g = ((color >> 8) & 0xFF) / 255f;
                float b = (color & 0xFF) / 255f;

                BlockPos pos = new BlockPos(chunkX * 16 + x, y, chunkZ * 16 + z);
                int light = 5; // Можно получить из мира, но для LOD часто не критично
                // Если хочешь: light = world.getCombinedLight(pos, 0);

                addQuad(buffer, chunkX * 16 + x, y, chunkZ * 16 + z, r, g, b, light);
            }
        }

        tessellator.draw();
    }

    private static void addQuad(BufferBuilder buffer, int x, int y, int z, float r, float g, float b, int light) {
        int sl = light >> 0x10 & 0xFFFF;
        int bl = light & 0xFFFF;
        float nx = 0, ny = 1, nz = 0;
        float u1 = 0, v1 = 0, u2 = 1, v2 = 1;

        buffer.pos(x,     y, z    ).color(r, g, b, 1f).tex(u1, v1).lightmap(sl, bl).normal(nx, ny, nz).endVertex();
        buffer.pos(x + 1, y, z    ).color(r, g, b, 1f).tex(u2, v1).lightmap(sl, bl).normal(nx, ny, nz).endVertex();
        buffer.pos(x + 1, y, z + 1).color(r, g, b, 1f).tex(u2, v2).lightmap(sl, bl).normal(nx, ny, nz).endVertex();

        buffer.pos(x,     y, z    ).color(r, g, b, 1f).tex(u1, v1).lightmap(sl, bl).normal(nx, ny, nz).endVertex();
        buffer.pos(x + 1, y, z + 1).color(r, g, b, 1f).tex(u2, v2).lightmap(sl, bl).normal(nx, ny, nz).endVertex();
        buffer.pos(x,     y, z + 1).color(r, g, b, 1f).tex(u1, v2).lightmap(sl, bl).normal(nx, ny, nz).endVertex();
    }

    private static int getAverageBlockColor(net.minecraft.world.World world, BlockPos pos) {
        try {
            net.minecraft.world.biome.Biome biome = world.getBiome(pos);
            return biome.getGrassColorAtPos(pos);
        } catch (Exception e) {
            return 0x00AA00;
        }
    }
}
