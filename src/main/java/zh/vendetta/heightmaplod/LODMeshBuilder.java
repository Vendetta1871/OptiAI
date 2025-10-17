package zh.vendetta.heightmaplod;

import net.minecraft.block.BlockColored;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class LODMeshBuilder {

    private static float u0;
    private static float u1;
    private static float v0;
    private static float v1;

    private static boolean is_initialized = false;

    private static void init() {
        if (is_initialized) return;
        is_initialized = true;

        Minecraft mc = Minecraft.getMinecraft();
        TextureMap map = mc.getTextureMapBlocks();

        IBlockState state = Blocks.WOOL.getDefaultState()
                .withProperty(BlockColored.COLOR, EnumDyeColor.WHITE);

        TextureAtlasSprite sprite = mc.getBlockRendererDispatcher()
                .getModelForState(state)
                .getParticleTexture();

        u0 = sprite.getMinU();
        u1 = sprite.getMaxU();
        v0 = sprite.getMinV();
        v1 = sprite.getMaxV();
    }

    private static int heightmap(World world, int x, int z, int i) {
        int height = 0;
        for (int ix = x; ix < x + i; ++ix) {
            for (int iz = z; iz < z + i; ++iz) {
                height += world.getHeight(ix, iz);
            }
        }
        return height / i / i;
    }

    public static void buildLODMesh(World world, BlockPos chunkPos, BufferBuilder builder, int i) {
        init();

        int baseX = chunkPos.getX();
        int baseZ = chunkPos.getZ();

        // TODO: Add lighting
        // TODO: Use real color of a block

        float fi = (float) i;

        for (int x = 0; x < 16; x += i) {
            for (int z = 0; z < 16; z += i) {
                int worldX = baseX + x;
                int worldZ = baseZ + z;
                float h = (float) heightmap(world, baseX + x, baseZ + z, i);

                if (h < chunkPos.getY() || h > chunkPos.getY() + 15) continue;
                //

                // TOP FACE

                int[] vertexData = new int[] {
                        Float.floatToRawIntBits(0f), Float.floatToRawIntBits(0f), Float.floatToRawIntBits(fi), 0xFFFFFFFF, Float.floatToRawIntBits(u0), Float.floatToRawIntBits(v0), 0,
                        Float.floatToRawIntBits(fi), Float.floatToRawIntBits(0f), Float.floatToRawIntBits(fi), 0xFFFFFFFF, Float.floatToRawIntBits(u1), Float.floatToRawIntBits(v0), 0,
                        Float.floatToRawIntBits(fi), Float.floatToRawIntBits(0f), Float.floatToRawIntBits(0f), 0xFFFFFFFF, Float.floatToRawIntBits(u1), Float.floatToRawIntBits(v1), 0,
                        Float.floatToRawIntBits(0f), Float.floatToRawIntBits(0f), Float.floatToRawIntBits(0f), 0xFFFFFFFF, Float.floatToRawIntBits(u0), Float.floatToRawIntBits(v1), 0
                };


                builder.addVertexData(vertexData);
                builder.putBrightness4(240, 240, 240, 240); // свет
                builder.putColorMultiplier(0.2f, 1.0f, 0.4f, 4); // множитель цвета
                builder.putColorMultiplier(0.2f, 1.0f, 0.4f, 3);
                builder.putColorMultiplier(0.2f, 1.0f, 0.4f, 2);
                builder.putColorMultiplier(0.2f, 1.0f, 0.4f, 1);
                builder.putPosition(worldX, h, worldZ); // смещение

                // BACK FACE

                float dh1 = (float) (heightmap(world, baseX + x, baseZ + z, i) - heightmap(world, baseX + x, baseZ + z - i, i));

                if (dh1 > 0) {
                    int[] vertexData1 = new int[]{
                            Float.floatToRawIntBits(0f), Float.floatToRawIntBits(0f), Float.floatToRawIntBits(0f), 0xFFFFFFFF, Float.floatToRawIntBits(u0), Float.floatToRawIntBits(v0), 0,
                            Float.floatToRawIntBits(fi), Float.floatToRawIntBits(0f), Float.floatToRawIntBits(0f), 0xFFFFFFFF, Float.floatToRawIntBits(u1), Float.floatToRawIntBits(v0), 0,
                            Float.floatToRawIntBits(fi), Float.floatToRawIntBits(0f - dh1), Float.floatToRawIntBits(0f), 0xFFFFFFFF, Float.floatToRawIntBits(u1), Float.floatToRawIntBits(v1), 0,
                            Float.floatToRawIntBits(0f), Float.floatToRawIntBits(0f - dh1), Float.floatToRawIntBits(0f), 0xFFFFFFFF, Float.floatToRawIntBits(u0), Float.floatToRawIntBits(v1), 0
                    };

                    builder.addVertexData(vertexData1);
                    builder.putBrightness4(240, 240, 240, 240); // свет
                    builder.putColorMultiplier(1.0f, 0.5f, 1.0f, 4); // множитель цвета
                    builder.putColorMultiplier(1.0f, 0.5f, 1.0f, 3);
                    builder.putColorMultiplier(1.0f, 0.5f, 1.0f, 2);
                    builder.putColorMultiplier(1.0f, 0.5f, 1.0f, 1);
                    builder.putPosition(worldX, h, worldZ); // смещение
                }

                float dh2 = (float) (heightmap(world, baseX + x, baseZ + z, i) - heightmap(world, baseX + x, baseZ + z + i, i));

                if (dh2 > 0) {
                    int[] vertexData1 = new int[]{
                            Float.floatToRawIntBits(0f), Float.floatToRawIntBits(0f - dh2), Float.floatToRawIntBits(fi), 0xFFFFFFFF, Float.floatToRawIntBits(u0), Float.floatToRawIntBits(v0), 0,
                            Float.floatToRawIntBits(fi), Float.floatToRawIntBits(0f - dh2), Float.floatToRawIntBits(fi), 0xFFFFFFFF, Float.floatToRawIntBits(u1), Float.floatToRawIntBits(v0), 0,
                            Float.floatToRawIntBits(fi), Float.floatToRawIntBits(0f), Float.floatToRawIntBits(fi), 0xFFFFFFFF, Float.floatToRawIntBits(u1), Float.floatToRawIntBits(v1), 0,
                            Float.floatToRawIntBits(0f), Float.floatToRawIntBits(0f), Float.floatToRawIntBits(fi), 0xFFFFFFFF, Float.floatToRawIntBits(u0), Float.floatToRawIntBits(v1), 0
                    };

                    builder.addVertexData(vertexData1);
                    builder.putBrightness4(240, 240, 240, 240); // свет
                    builder.putColorMultiplier(1.0f, 0.5f, 0.4f, 4); // множитель цвета
                    builder.putColorMultiplier(1.0f, 0.5f, 0.4f, 3);
                    builder.putColorMultiplier(1.0f, 0.5f, 0.4f, 2);
                    builder.putColorMultiplier(1.0f, 0.5f, 0.4f, 1);
                    builder.putPosition(worldX, h, worldZ); // смещение
                }

                // LEFT FACE

                float dh3 = (float) (heightmap(world, baseX + x, baseZ + z, i) - heightmap(world, baseX + x - i, baseZ + z, i));

                if (dh3 > 0) {
                    int[] vertexData1 = new int[]{
                            Float.floatToRawIntBits(0f), Float.floatToRawIntBits(0f - dh3), Float.floatToRawIntBits(0f), 0xFFFFFFFF, Float.floatToRawIntBits(u0), Float.floatToRawIntBits(v0), 0,
                            Float.floatToRawIntBits(0f), Float.floatToRawIntBits(0f - dh3), Float.floatToRawIntBits(fi), 0xFFFFFFFF, Float.floatToRawIntBits(u1), Float.floatToRawIntBits(v0), 0,
                            Float.floatToRawIntBits(0f), Float.floatToRawIntBits(0f), Float.floatToRawIntBits(fi), 0xFFFFFFFF, Float.floatToRawIntBits(u1), Float.floatToRawIntBits(v1), 0,
                            Float.floatToRawIntBits(0f), Float.floatToRawIntBits(0f), Float.floatToRawIntBits(0f), 0xFFFFFFFF, Float.floatToRawIntBits(u0), Float.floatToRawIntBits(v1), 0
                    };

                    builder.addVertexData(vertexData1);
                    builder.putBrightness4(240, 240, 240, 240); // свет
                    builder.putColorMultiplier(.20f, 0.2f, 0.1f, 4); // множитель цвета
                    builder.putColorMultiplier(.20f, 0.2f, 0.1f, 3);
                    builder.putColorMultiplier(.20f, 0.2f, 0.1f, 2);
                    builder.putColorMultiplier(.20f, 0.2f, 0.1f, 1);
                    builder.putPosition(worldX, h, worldZ); // смещение
                }

                // RIGHT FACE

                float dh4 = (float) (heightmap(world, baseX + x, baseZ + z, i) - heightmap(world, baseX + x + i, baseZ + z, i));

                if (dh4 > 0) {
                    int[] vertexData1 = new int[]{
                            Float.floatToRawIntBits(fi), Float.floatToRawIntBits(0f), Float.floatToRawIntBits(0f), 0xFFFFFFFF, Float.floatToRawIntBits(u0), Float.floatToRawIntBits(v0), 0,
                            Float.floatToRawIntBits(fi), Float.floatToRawIntBits(0f), Float.floatToRawIntBits(fi), 0xFFFFFFFF, Float.floatToRawIntBits(u1), Float.floatToRawIntBits(v0), 0,
                            Float.floatToRawIntBits(fi), Float.floatToRawIntBits(0f - dh4), Float.floatToRawIntBits(fi), 0xFFFFFFFF, Float.floatToRawIntBits(u1), Float.floatToRawIntBits(v1), 0,
                            Float.floatToRawIntBits(fi), Float.floatToRawIntBits(0f - dh4), Float.floatToRawIntBits(0f), 0xFFFFFFFF, Float.floatToRawIntBits(u0), Float.floatToRawIntBits(v1), 0
                    };

                    builder.addVertexData(vertexData1);
                    builder.putBrightness4(240, 240, 240, 240); // свет
                    builder.putColorMultiplier(.20f, 0.2f, 0.1f, 4); // множитель цвета
                    builder.putColorMultiplier(.20f, 0.2f, 0.1f, 3);
                    builder.putColorMultiplier(.20f, 0.2f, 0.1f, 2);
                    builder.putColorMultiplier(.20f, 0.2f, 0.1f, 1);
                    builder.putPosition(worldX, h, worldZ); // смещение
                }
            }
        }
    }
}
