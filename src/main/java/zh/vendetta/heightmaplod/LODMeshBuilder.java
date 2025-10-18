package zh.vendetta.heightmaplod;

import net.minecraft.block.BlockColored;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class LODMeshBuilder {

    private static float u0;
    private static float u1;
    private static float v0;
    private static float v1;

    private static boolean isInitialized = false;

    private static void initialize() {
        if (isInitialized) return;
        isInitialized = true;

        Minecraft mc = Minecraft.getMinecraft();

        IBlockState state = Blocks.STAINED_GLASS.getDefaultState()
                .withProperty(BlockColored.COLOR, EnumDyeColor.WHITE);

        TextureAtlasSprite sprite = mc.getBlockRendererDispatcher()
                .getModelForState(state)
                .getParticleTexture();

        u0 = sprite.getMinU();
        u1 = sprite.getMaxU();
        v0 = sprite.getMinV();
        v1 = sprite.getMaxV();
    }

    public static void buildLODMesh(World world, BlockPos chunkPos, BufferBuilder builder, int i) {
        initialize();

        MeshBuilderCache cache = new MeshBuilderCache();

        int baseX = chunkPos.getX();
        int baseZ = chunkPos.getZ();

        // TODO: use different color for faces of blocks like dirt with grass

        float fi = (float) i;
        for (int x = 0; x < 16; x += i) {
            for (int z = 0; z < 16; z += i) {
                int worldX = baseX + x;
                int worldZ = baseZ + z;

                float height = (float) cache.heightmap(world, worldX, worldZ, i);
                if (height < chunkPos.getY() || height > chunkPos.getY() + 15) continue;

                float[] color = cache.colormap(world, worldX, worldZ, i);

                int ltop = cache.lightmap(world, worldX, worldZ, i);
                int lsouth = (int) (ltop * 0.9f), lnorth = lsouth + 1 - 1; // +1-1 to remove the IntelliJ warning
                int least = (int) (ltop * 0.8f), lwest = least + 1 - 1;

                // TOP FACE

                int[] vertexData = new int[] {
                        Float.floatToRawIntBits(0f), Float.floatToRawIntBits(0f), Float.floatToRawIntBits(fi), 0xFFFFFFFF, Float.floatToRawIntBits(u0), Float.floatToRawIntBits(v0), 0,
                        Float.floatToRawIntBits(fi), Float.floatToRawIntBits(0f), Float.floatToRawIntBits(fi), 0xFFFFFFFF, Float.floatToRawIntBits(u1), Float.floatToRawIntBits(v0), 0,
                        Float.floatToRawIntBits(fi), Float.floatToRawIntBits(0f), Float.floatToRawIntBits(0f), 0xFFFFFFFF, Float.floatToRawIntBits(u1), Float.floatToRawIntBits(v1), 0,
                        Float.floatToRawIntBits(0f), Float.floatToRawIntBits(0f), Float.floatToRawIntBits(0f), 0xFFFFFFFF, Float.floatToRawIntBits(u0), Float.floatToRawIntBits(v1), 0
                };

                builder.addVertexData(vertexData);
                builder.putBrightness4(ltop, ltop, ltop, ltop);
                builder.putColorMultiplier(color[0], color[1], color[2], 4);
                builder.putColorMultiplier(color[0], color[1], color[2], 3);
                builder.putColorMultiplier(color[0], color[1], color[2], 2);
                builder.putColorMultiplier(color[0], color[1], color[2], 1);
                builder.putPosition(worldX, height, worldZ);

                // NORTH FACE

                float dh1 = (float) (cache.heightmap(world, worldX, worldZ, i) - cache.heightmap(world, worldX, worldZ - i, i));

                if (dh1 > 0) {
                    int[] vertexData1 = new int[]{
                            Float.floatToRawIntBits(0f), Float.floatToRawIntBits(0f), Float.floatToRawIntBits(0f), 0xFFFFFFFF, Float.floatToRawIntBits(u0), Float.floatToRawIntBits(v0), 0,
                            Float.floatToRawIntBits(fi), Float.floatToRawIntBits(0f), Float.floatToRawIntBits(0f), 0xFFFFFFFF, Float.floatToRawIntBits(u1), Float.floatToRawIntBits(v0), 0,
                            Float.floatToRawIntBits(fi), Float.floatToRawIntBits(0f - dh1), Float.floatToRawIntBits(0f), 0xFFFFFFFF, Float.floatToRawIntBits(u1), Float.floatToRawIntBits(v1), 0,
                            Float.floatToRawIntBits(0f), Float.floatToRawIntBits(0f - dh1), Float.floatToRawIntBits(0f), 0xFFFFFFFF, Float.floatToRawIntBits(u0), Float.floatToRawIntBits(v1), 0
                    };

                    builder.addVertexData(vertexData1);
                    builder.putBrightness4(lnorth, lnorth, lnorth, lnorth);
                    builder.putColorMultiplier(color[0], color[1], color[2], 4);
                    builder.putColorMultiplier(color[0], color[1], color[2], 3);
                    builder.putColorMultiplier(color[0], color[1], color[2], 2);
                    builder.putColorMultiplier(color[0], color[1], color[2], 1);
                    builder.putPosition(worldX, height, worldZ);
                }

                // SOUTH FACE

                float dh2 = (float) (cache.heightmap(world, worldX, worldZ, i) - cache.heightmap(world, worldX, worldZ + i, i));

                if (dh2 > 0) {
                    int[] vertexData1 = new int[]{
                            Float.floatToRawIntBits(0f), Float.floatToRawIntBits(0f - dh2), Float.floatToRawIntBits(fi), 0xFFFFFFFF, Float.floatToRawIntBits(u0), Float.floatToRawIntBits(v0), 0,
                            Float.floatToRawIntBits(fi), Float.floatToRawIntBits(0f - dh2), Float.floatToRawIntBits(fi), 0xFFFFFFFF, Float.floatToRawIntBits(u1), Float.floatToRawIntBits(v0), 0,
                            Float.floatToRawIntBits(fi), Float.floatToRawIntBits(0f), Float.floatToRawIntBits(fi), 0xFFFFFFFF, Float.floatToRawIntBits(u1), Float.floatToRawIntBits(v1), 0,
                            Float.floatToRawIntBits(0f), Float.floatToRawIntBits(0f), Float.floatToRawIntBits(fi), 0xFFFFFFFF, Float.floatToRawIntBits(u0), Float.floatToRawIntBits(v1), 0
                    };

                    builder.addVertexData(vertexData1);
                    builder.putBrightness4(lsouth, lsouth, lsouth, lsouth);
                    builder.putColorMultiplier(color[0], color[1], color[2], 4);
                    builder.putColorMultiplier(color[0], color[1], color[2], 3);
                    builder.putColorMultiplier(color[0], color[1], color[2], 2);
                    builder.putColorMultiplier(color[0], color[1], color[2], 1);
                    builder.putPosition(worldX, height, worldZ);
                }

                // WEST FACE

                float dh3 = (float) (cache.heightmap(world, worldX, worldZ, i) - cache.heightmap(world, worldX - i, worldZ, i));

                if (dh3 > 0) {
                    int[] vertexData1 = new int[]{
                            Float.floatToRawIntBits(0f), Float.floatToRawIntBits(0f - dh3), Float.floatToRawIntBits(0f), 0xFFFFFFFF, Float.floatToRawIntBits(u0), Float.floatToRawIntBits(v0), 0,
                            Float.floatToRawIntBits(0f), Float.floatToRawIntBits(0f - dh3), Float.floatToRawIntBits(fi), 0xFFFFFFFF, Float.floatToRawIntBits(u1), Float.floatToRawIntBits(v0), 0,
                            Float.floatToRawIntBits(0f), Float.floatToRawIntBits(0f), Float.floatToRawIntBits(fi), 0xFFFFFFFF, Float.floatToRawIntBits(u1), Float.floatToRawIntBits(v1), 0,
                            Float.floatToRawIntBits(0f), Float.floatToRawIntBits(0f), Float.floatToRawIntBits(0f), 0xFFFFFFFF, Float.floatToRawIntBits(u0), Float.floatToRawIntBits(v1), 0
                    };

                    builder.addVertexData(vertexData1);
                    builder.putBrightness4(lwest, lwest, lwest, lwest);
                    builder.putColorMultiplier(color[0], color[1], color[2], 4);
                    builder.putColorMultiplier(color[0], color[1], color[2], 3);
                    builder.putColorMultiplier(color[0], color[1], color[2], 2);
                    builder.putColorMultiplier(color[0], color[1], color[2], 1);
                    builder.putPosition(worldX, height, worldZ);
                }

                // EAST FACE

                float dh4 = (float) (cache.heightmap(world, worldX, worldZ, i) - cache.heightmap(world, worldX + i, worldZ, i));

                if (dh4 > 0) {
                    int[] vertexData1 = new int[]{
                            Float.floatToRawIntBits(fi), Float.floatToRawIntBits(0f), Float.floatToRawIntBits(0f), 0xFFFFFFFF, Float.floatToRawIntBits(u0), Float.floatToRawIntBits(v0), 0,
                            Float.floatToRawIntBits(fi), Float.floatToRawIntBits(0f), Float.floatToRawIntBits(fi), 0xFFFFFFFF, Float.floatToRawIntBits(u1), Float.floatToRawIntBits(v0), 0,
                            Float.floatToRawIntBits(fi), Float.floatToRawIntBits(0f - dh4), Float.floatToRawIntBits(fi), 0xFFFFFFFF, Float.floatToRawIntBits(u1), Float.floatToRawIntBits(v1), 0,
                            Float.floatToRawIntBits(fi), Float.floatToRawIntBits(0f - dh4), Float.floatToRawIntBits(0f), 0xFFFFFFFF, Float.floatToRawIntBits(u0), Float.floatToRawIntBits(v1), 0
                    };

                    builder.addVertexData(vertexData1);
                    builder.putBrightness4(least, least, least, least);
                    builder.putColorMultiplier(color[0], color[1], color[2], 4);
                    builder.putColorMultiplier(color[0], color[1], color[2], 3);
                    builder.putColorMultiplier(color[0], color[1], color[2], 2);
                    builder.putColorMultiplier(color[0], color[1], color[2], 1);
                    builder.putPosition(worldX, height, worldZ);
                }
            }
        }
    }
}
