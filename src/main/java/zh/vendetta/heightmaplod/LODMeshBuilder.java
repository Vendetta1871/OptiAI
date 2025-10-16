package zh.vendetta.heightmaplod;

import net.minecraft.block.BlockColored;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.World;
import zh.vendetta.heightmaplod.mixin.RenderChunkAccessor;

public class LODMeshBuilder {
    // Размер меша для одного чанка (16x16)
    public static void buildLODMesh(World world, BlockPos chunkPos, BufferBuilder builder, BlockRendererDispatcher dispatcher/*, ChunkCache worldView*/) {
        Minecraft mc = Minecraft.getMinecraft();
        TextureMap map = mc.getTextureMapBlocks();

        IBlockState state = Blocks.WOOL.getDefaultState()
                .withProperty(BlockColored.COLOR, EnumDyeColor.WHITE);

        TextureAtlasSprite sprite = mc.getBlockRendererDispatcher()
                .getModelForState(state)
                .getParticleTexture();

        float u0 = sprite.getMinU();
        float u1 = sprite.getMaxU();
        float v0 = sprite.getMinV();
        float v1 = sprite.getMaxV();

        int baseX = chunkPos.getX();
        int baseZ = chunkPos.getZ();

        // TODO: Add lighting
        // TODO: Use real color of a block

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = baseX + x;
                int worldZ = baseZ + z;
                int height = world.getHeight(worldX, worldZ);
                float h = (float) height - 1;

                //

                // TOP FACE

                int[] vertexData = new int[] {
                        Float.floatToRawIntBits(0f), Float.floatToRawIntBits(1f), Float.floatToRawIntBits(1f), 0xFFFFFFFF, Float.floatToRawIntBits(u0), Float.floatToRawIntBits(v0), 0,
                        Float.floatToRawIntBits(1f), Float.floatToRawIntBits(1f), Float.floatToRawIntBits(1f), 0xFFFFFFFF, Float.floatToRawIntBits(u1), Float.floatToRawIntBits(v0), 0,
                        Float.floatToRawIntBits(1f), Float.floatToRawIntBits(1f), Float.floatToRawIntBits(0f), 0xFFFFFFFF, Float.floatToRawIntBits(u1), Float.floatToRawIntBits(v1), 0,
                        Float.floatToRawIntBits(0f), Float.floatToRawIntBits(1f), Float.floatToRawIntBits(0f), 0xFFFFFFFF, Float.floatToRawIntBits(u0), Float.floatToRawIntBits(v1), 0
                };


                builder.addVertexData(vertexData);
                builder.putBrightness4(240, 240, 240, 240); // свет
                builder.putColorMultiplier(0.2f, 1.0f, 0.4f, 4); // множитель цвета
                builder.putColorMultiplier(0.2f, 1.0f, 0.4f, 3);
                builder.putColorMultiplier(0.2f, 1.0f, 0.4f, 2);
                builder.putColorMultiplier(0.2f, 1.0f, 0.4f, 1);
                builder.putPosition(worldX, h, worldZ); // смещение

                // BACK FACE

                float dh1 = (float) (height - world.getHeight(worldX, worldZ - 1));

                if (dh1 > 0) {
                    int[] vertexData1 = new int[]{
                            Float.floatToRawIntBits(0f), Float.floatToRawIntBits(1f), Float.floatToRawIntBits(0f), 0xFFFFFFFF, Float.floatToRawIntBits(u0), Float.floatToRawIntBits(v0), 0,
                            Float.floatToRawIntBits(1f), Float.floatToRawIntBits(1f), Float.floatToRawIntBits(0f), 0xFFFFFFFF, Float.floatToRawIntBits(u1), Float.floatToRawIntBits(v0), 0,
                            Float.floatToRawIntBits(1f), Float.floatToRawIntBits(0f - dh1), Float.floatToRawIntBits(0f), 0xFFFFFFFF, Float.floatToRawIntBits(u1), Float.floatToRawIntBits(v1), 0,
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

                float dh2 = (float) (height - world.getHeight(worldX, worldZ + 1));

                if (dh2 > 0) {
                    int[] vertexData1 = new int[]{
                            Float.floatToRawIntBits(0f), Float.floatToRawIntBits(0f - dh2), Float.floatToRawIntBits(1f), 0xFFFFFFFF, Float.floatToRawIntBits(u0), Float.floatToRawIntBits(v0), 0,
                            Float.floatToRawIntBits(1f), Float.floatToRawIntBits(0f - dh2), Float.floatToRawIntBits(1f), 0xFFFFFFFF, Float.floatToRawIntBits(u1), Float.floatToRawIntBits(v0), 0,
                            Float.floatToRawIntBits(1f), Float.floatToRawIntBits(1f), Float.floatToRawIntBits(1f), 0xFFFFFFFF, Float.floatToRawIntBits(u1), Float.floatToRawIntBits(v1), 0,
                            Float.floatToRawIntBits(0f), Float.floatToRawIntBits(1f), Float.floatToRawIntBits(1f), 0xFFFFFFFF, Float.floatToRawIntBits(u0), Float.floatToRawIntBits(v1), 0
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

                float dh3 = (float) (height - world.getHeight(worldX - 1, worldZ));

                if (dh3 > 0) {
                    int[] vertexData1 = new int[]{
                            Float.floatToRawIntBits(0f), Float.floatToRawIntBits(0f - dh3), Float.floatToRawIntBits(0f), 0xFFFFFFFF, Float.floatToRawIntBits(u0), Float.floatToRawIntBits(v0), 0,
                            Float.floatToRawIntBits(0f), Float.floatToRawIntBits(0f - dh3), Float.floatToRawIntBits(1f), 0xFFFFFFFF, Float.floatToRawIntBits(u1), Float.floatToRawIntBits(v0), 0,
                            Float.floatToRawIntBits(0f), Float.floatToRawIntBits(1f), Float.floatToRawIntBits(1f), 0xFFFFFFFF, Float.floatToRawIntBits(u1), Float.floatToRawIntBits(v1), 0,
                            Float.floatToRawIntBits(0f), Float.floatToRawIntBits(1f), Float.floatToRawIntBits(0f), 0xFFFFFFFF, Float.floatToRawIntBits(u0), Float.floatToRawIntBits(v1), 0
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

                float dh4 = (float) (height - world.getHeight(worldX + 1, worldZ));

                if (dh4 > 0) {
                    int[] vertexData1 = new int[]{
                            Float.floatToRawIntBits(1f), Float.floatToRawIntBits(1f), Float.floatToRawIntBits(0f), 0xFFFFFFFF, Float.floatToRawIntBits(u0), Float.floatToRawIntBits(v0), 0,
                            Float.floatToRawIntBits(1f), Float.floatToRawIntBits(1f), Float.floatToRawIntBits(1f), 0xFFFFFFFF, Float.floatToRawIntBits(u1), Float.floatToRawIntBits(v0), 0,
                            Float.floatToRawIntBits(1f), Float.floatToRawIntBits(0f - dh4), Float.floatToRawIntBits(1f), 0xFFFFFFFF, Float.floatToRawIntBits(u1), Float.floatToRawIntBits(v1), 0,
                            Float.floatToRawIntBits(1f), Float.floatToRawIntBits(0f - dh4), Float.floatToRawIntBits(0f), 0xFFFFFFFF, Float.floatToRawIntBits(u0), Float.floatToRawIntBits(v1), 0
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
