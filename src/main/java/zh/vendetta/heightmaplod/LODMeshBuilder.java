package zh.vendetta.heightmaplod;

import net.minecraft.block.BlockColored;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.color.BlockColors;
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

    private static boolean is_initialized = false;

    private static void init() {
        if (is_initialized) return;
        is_initialized = true;

        Minecraft mc = Minecraft.getMinecraft();

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

    public static int getAverageColor(TextureAtlasSprite sprite) {
        if (sprite == null || sprite.getFrameCount() == 0) return 0xFFFFFFFF;

        int[] pixels = sprite.getFrameTextureData(0)[0];
        long r = 0, g = 0, b = 0;
        int count = 0;

        for (int argb : pixels) {
            int a = (argb >> 24) & 0xFF;
            if (a < 128) continue;

            r += (argb >> 16) & 0xFF;
            g += (argb >> 8) & 0xFF;
            b += argb & 0xFF;
            count++;
        }

        if (count == 0) return 0xFFFFFFFF;

        r /= count;
        g /= count;
        b /= count;

        return (0xFF << 24) | (((int)r & 0xFF) << 16) | (((int)g & 0xFF) << 8) | ((int)b & 0xFF);

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

    private static int brightness(World world, int x, int z, int i) {
        int max = 0;
        for (int ix = x; ix < x + i; ++ix) {
            for (int iz = z; iz < z + i; ++iz) {
                int b = world.getCombinedLight(new BlockPos(ix, world.getHeight(ix, iz), iz), 0);
                if (b > max) max = b;
            }
        }
        return max;
    }

    public static void buildLODMesh(World world, BlockPos chunkPos, BufferBuilder builder, int i) {
        init();

        int baseX = chunkPos.getX();
        int baseZ = chunkPos.getZ();

        // TODO: Calculate correct lighting for all sides (check the vanila)
        // TODO: use different color for faces of blocks like dirt with grass

        float fi = (float) i;

        for (int x = 0; x < 16; x += i) {
            for (int z = 0; z < 16; z += i) {
                int worldX = baseX + x;
                int worldZ = baseZ + z;

                float h = (float) heightmap(world, worldX, worldZ, i);
                if (h < chunkPos.getY() || h > chunkPos.getY() + 15) continue;

                BlockPos pos = new BlockPos(worldX, world.getHeight(worldX, worldZ) - 1, worldZ);
                IBlockState state = world.getBlockState(pos).getBlock().getDefaultState();
                TextureAtlasSprite sprite = Minecraft.getMinecraft().getBlockRendererDispatcher()
                        .getBlockModelShapes().getTexture(state);

                int avg = getAverageColor(sprite);

                // применяем цвет биома, если есть
                BlockColors colors = Minecraft.getMinecraft().getBlockColors();
                int biomeColor = colors.colorMultiplier(state, world, pos, 0);

                float red = ((avg >> 16) & 0xFF) / 255f;
                float green = ((avg >> 8) & 0xFF) / 255f;
                float blue = (avg & 0xFF) / 255f;

                if (biomeColor != -1) {
                    int r1 = (avg >> 16) & 0xFF;
                    int g1 = (avg >> 8) & 0xFF;
                    int b1 = avg & 0xFF;

                    int r2 = (biomeColor >> 16) & 0xFF;
                    int g2 = (biomeColor >> 8) & 0xFF;
                    int b2 = biomeColor & 0xFF;

                    // простое поканальное умножение
                    red = r1 * r2 / 255f / 255f;
                    green = g1 * g2 / 255f/ 255f;
                    blue = b1 * b2 / 255f/ 255f;
                }

                //
                //getUv(world.getBlockState(new BlockPos(worldX, h - 1, worldZ)).getBlock().getDefaultState());

                // TOP FACE

                int[] vertexData = new int[] {
                        Float.floatToRawIntBits(0f), Float.floatToRawIntBits(0f), Float.floatToRawIntBits(fi), 0xFFFFFFFF, Float.floatToRawIntBits(u0), Float.floatToRawIntBits(v0), 0,
                        Float.floatToRawIntBits(fi), Float.floatToRawIntBits(0f), Float.floatToRawIntBits(fi), 0xFFFFFFFF, Float.floatToRawIntBits(u1), Float.floatToRawIntBits(v0), 0,
                        Float.floatToRawIntBits(fi), Float.floatToRawIntBits(0f), Float.floatToRawIntBits(0f), 0xFFFFFFFF, Float.floatToRawIntBits(u1), Float.floatToRawIntBits(v1), 0,
                        Float.floatToRawIntBits(0f), Float.floatToRawIntBits(0f), Float.floatToRawIntBits(0f), 0xFFFFFFFF, Float.floatToRawIntBits(u0), Float.floatToRawIntBits(v1), 0
                };

                //buffer.putBrightness4(b1, b2, b3, b4);
                int b1 = brightness(world, worldX, worldZ, i);
                int b2 = b1;
                int b3 = b1;
                int b4 = b1;

                builder.addVertexData(vertexData);
                builder.putBrightness4(b1, b2, b3, b4);
                builder.putColorMultiplier(red, green, blue, 4);
                builder.putColorMultiplier(red, green, blue, 3);
                builder.putColorMultiplier(red, green, blue, 2);
                builder.putColorMultiplier(red, green, blue, 1);
                builder.putPosition(worldX, h, worldZ);

                // BACK FACE

                float dh1 = (float) (heightmap(world, worldX, worldZ, i) - heightmap(world, worldX, worldZ - i, i));

                if (dh1 > 0) {
                    int[] vertexData1 = new int[]{
                            Float.floatToRawIntBits(0f), Float.floatToRawIntBits(0f), Float.floatToRawIntBits(0f), 0xFFFFFFFF, Float.floatToRawIntBits(u0), Float.floatToRawIntBits(v0), 0,
                            Float.floatToRawIntBits(fi), Float.floatToRawIntBits(0f), Float.floatToRawIntBits(0f), 0xFFFFFFFF, Float.floatToRawIntBits(u1), Float.floatToRawIntBits(v0), 0,
                            Float.floatToRawIntBits(fi), Float.floatToRawIntBits(0f - dh1), Float.floatToRawIntBits(0f), 0xFFFFFFFF, Float.floatToRawIntBits(u1), Float.floatToRawIntBits(v1), 0,
                            Float.floatToRawIntBits(0f), Float.floatToRawIntBits(0f - dh1), Float.floatToRawIntBits(0f), 0xFFFFFFFF, Float.floatToRawIntBits(u0), Float.floatToRawIntBits(v1), 0
                    };

                    int bb1 = (int)(b1 * 0.9);
                    int bb2 = (int)(b2 * 0.9);
                    int bb3 = (int)(b3 * 0.8);
                    int bb4 = (int)(b4 * 0.8);

                    builder.addVertexData(vertexData1);
                    builder.putBrightness4(bb1, bb2, bb3, bb4);
                    builder.putColorMultiplier(red, green, blue, 4);
                    builder.putColorMultiplier(red, green, blue, 3);
                    builder.putColorMultiplier(red, green, blue, 2);
                    builder.putColorMultiplier(red, green, blue, 1);
                    builder.putPosition(worldX, h, worldZ);
                }

                float dh2 = (float) (heightmap(world, worldX, worldZ, i) - heightmap(world, worldX, worldZ + i, i));

                if (dh2 > 0) {
                    int[] vertexData1 = new int[]{
                            Float.floatToRawIntBits(0f), Float.floatToRawIntBits(0f - dh2), Float.floatToRawIntBits(fi), 0xFFFFFFFF, Float.floatToRawIntBits(u0), Float.floatToRawIntBits(v0), 0,
                            Float.floatToRawIntBits(fi), Float.floatToRawIntBits(0f - dh2), Float.floatToRawIntBits(fi), 0xFFFFFFFF, Float.floatToRawIntBits(u1), Float.floatToRawIntBits(v0), 0,
                            Float.floatToRawIntBits(fi), Float.floatToRawIntBits(0f), Float.floatToRawIntBits(fi), 0xFFFFFFFF, Float.floatToRawIntBits(u1), Float.floatToRawIntBits(v1), 0,
                            Float.floatToRawIntBits(0f), Float.floatToRawIntBits(0f), Float.floatToRawIntBits(fi), 0xFFFFFFFF, Float.floatToRawIntBits(u0), Float.floatToRawIntBits(v1), 0
                    };

                    int bb1 = (int)(b1 * 0.9);
                    int bb2 = (int)(b2 * 0.9);
                    int bb3 = (int)(b3 * 0.8);
                    int bb4 = (int)(b4 * 0.8);

                    builder.addVertexData(vertexData1);
                    builder.putBrightness4(bb1, bb2, bb3, bb4);
                    builder.putColorMultiplier(red, green, blue, 4);
                    builder.putColorMultiplier(red, green, blue, 3);
                    builder.putColorMultiplier(red, green, blue, 2);
                    builder.putColorMultiplier(red, green, blue, 1);
                    builder.putPosition(worldX, h, worldZ);
                }

                // LEFT FACE

                float dh3 = (float) (heightmap(world, worldX, worldZ, i) - heightmap(world, worldX - i, worldZ, i));

                if (dh3 > 0) {
                    int[] vertexData1 = new int[]{
                            Float.floatToRawIntBits(0f), Float.floatToRawIntBits(0f - dh3), Float.floatToRawIntBits(0f), 0xFFFFFFFF, Float.floatToRawIntBits(u0), Float.floatToRawIntBits(v0), 0,
                            Float.floatToRawIntBits(0f), Float.floatToRawIntBits(0f - dh3), Float.floatToRawIntBits(fi), 0xFFFFFFFF, Float.floatToRawIntBits(u1), Float.floatToRawIntBits(v0), 0,
                            Float.floatToRawIntBits(0f), Float.floatToRawIntBits(0f), Float.floatToRawIntBits(fi), 0xFFFFFFFF, Float.floatToRawIntBits(u1), Float.floatToRawIntBits(v1), 0,
                            Float.floatToRawIntBits(0f), Float.floatToRawIntBits(0f), Float.floatToRawIntBits(0f), 0xFFFFFFFF, Float.floatToRawIntBits(u0), Float.floatToRawIntBits(v1), 0
                    };

                    int bb1 = (int)(b1 * 0.9);
                    int bb2 = (int)(b2 * 0.9);
                    int bb3 = (int)(b3 * 0.8);
                    int bb4 = (int)(b4 * 0.8);

                    builder.addVertexData(vertexData1);
                    builder.putBrightness4(bb1, bb2, bb3, bb4);
                    builder.putColorMultiplier(red, green, blue, 4);
                    builder.putColorMultiplier(red, green, blue, 3);
                    builder.putColorMultiplier(red, green, blue, 2);
                    builder.putColorMultiplier(red, green, blue, 1);
                    builder.putPosition(worldX, h, worldZ);
                }

                // RIGHT FACE

                float dh4 = (float) (heightmap(world, worldX, worldZ, i) - heightmap(world, worldX + i, worldZ, i));

                if (dh4 > 0) {
                    int[] vertexData1 = new int[]{
                            Float.floatToRawIntBits(fi), Float.floatToRawIntBits(0f), Float.floatToRawIntBits(0f), 0xFFFFFFFF, Float.floatToRawIntBits(u0), Float.floatToRawIntBits(v0), 0,
                            Float.floatToRawIntBits(fi), Float.floatToRawIntBits(0f), Float.floatToRawIntBits(fi), 0xFFFFFFFF, Float.floatToRawIntBits(u1), Float.floatToRawIntBits(v0), 0,
                            Float.floatToRawIntBits(fi), Float.floatToRawIntBits(0f - dh4), Float.floatToRawIntBits(fi), 0xFFFFFFFF, Float.floatToRawIntBits(u1), Float.floatToRawIntBits(v1), 0,
                            Float.floatToRawIntBits(fi), Float.floatToRawIntBits(0f - dh4), Float.floatToRawIntBits(0f), 0xFFFFFFFF, Float.floatToRawIntBits(u0), Float.floatToRawIntBits(v1), 0
                    };

                    int bb1 = (int)(b1 * 0.9);
                    int bb2 = (int)(b2 * 0.9);
                    int bb3 = (int)(b3 * 0.8);
                    int bb4 = (int)(b4 * 0.8);

                    builder.addVertexData(vertexData1);
                    builder.putBrightness4(bb1, bb2, bb3, bb4);
                    builder.putColorMultiplier(red, green, blue, 4);
                    builder.putColorMultiplier(red, green, blue, 3);
                    builder.putColorMultiplier(red, green, blue, 2);
                    builder.putColorMultiplier(red, green, blue, 1);
                    builder.putPosition(worldX, h, worldZ);
                }
            }
        }
    }
}
