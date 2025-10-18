package zh.vendetta.heightmaplod;

import net.minecraft.block.BlockColored;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class LODMeshBuilder {

    private static int u0;
    private static int u1;
    private static int v0;
    private static int v1;

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

        u0 = Float.floatToRawIntBits(sprite.getMinU());
        u1 = Float.floatToRawIntBits(sprite.getMaxU());
        v0 = Float.floatToRawIntBits(sprite.getMinV());
        v1 = Float.floatToRawIntBits(sprite.getMaxV());
    }

    public static void buildLODMesh(World world, BlockPos chunkPos, BufferBuilder builder, int i) {
        initialize();

        MeshBuilderCache cache = new MeshBuilderCache();

        int baseX = chunkPos.getX();
        int baseZ = chunkPos.getZ();

        float fi = (float) i;
        for (int x = 0; x < 16; x += i) {
            for (int z = 0; z < 16; z += i) {
                int worldX = baseX + x;
                int worldZ = baseZ + z;

                float height = (float) cache.heightmap(world, worldX, worldZ, i);
                if (height < chunkPos.getY() || height > chunkPos.getY() + 15) continue;

                // UP FACE

                int upcolor = cache.colormap(world, worldX, worldZ, EnumFacing.UP, i);
                int lup = cache.lightmap(world, worldX, worldZ, i);

                int[] vertexData = new int[]{
                        Float.floatToRawIntBits(0f), Float.floatToRawIntBits(0f), Float.floatToRawIntBits(fi), upcolor, u0, v0, lup,
                        Float.floatToRawIntBits(fi), Float.floatToRawIntBits(0f), Float.floatToRawIntBits(fi), upcolor, u1, v0, lup,
                        Float.floatToRawIntBits(fi), Float.floatToRawIntBits(0f), Float.floatToRawIntBits(0f), upcolor, u1, v1, lup,
                        Float.floatToRawIntBits(0f), Float.floatToRawIntBits(0f), Float.floatToRawIntBits(0f), upcolor, u0, v1, lup
                };

                builder.addVertexData(vertexData);
                builder.putPosition(worldX, height, worldZ);

                // NORTH FACE

                float dh1 = (float) (cache.heightmap(world, worldX, worldZ, i) - cache.heightmap(world, worldX, worldZ - i, i));
                if (i == 1 && z == 0) dh1 = height; // temporary fix for connection between vanilla render and LOD

                if (dh1 > 0) {
                    int northcolor = cache.colormap(world, worldX, worldZ, EnumFacing.NORTH, i);
                    int lnorth = (int) (lup * 0.9f);

                    vertexData = new int[]{
                            Float.floatToRawIntBits(0f), Float.floatToRawIntBits(0f), Float.floatToRawIntBits(0f), northcolor, u0, v0, lnorth,
                            Float.floatToRawIntBits(fi), Float.floatToRawIntBits(0f), Float.floatToRawIntBits(0f), northcolor, u1, v0, lnorth,
                            Float.floatToRawIntBits(fi), Float.floatToRawIntBits(0f - dh1), Float.floatToRawIntBits(0f), northcolor, u1, v1, lnorth,
                            Float.floatToRawIntBits(0f), Float.floatToRawIntBits(0f - dh1), Float.floatToRawIntBits(0f), northcolor, u0, v1, lnorth
                    };

                    builder.addVertexData(vertexData);
                    builder.putPosition(worldX, height, worldZ);
                }

                // SOUTH FACE

                float dh2 = (float) (cache.heightmap(world, worldX, worldZ, i) - cache.heightmap(world, worldX, worldZ + i, i));
                if (i == 1 && z == 15) dh2 = height; // temporary fix for connection between vanilla render and LOD

                if (dh2 > 0) {
                    int southcolor = cache.colormap(world, worldX, worldZ, EnumFacing.SOUTH, i);
                    int lsouth = (int) (lup * 0.9f);

                    vertexData = new int[]{
                            Float.floatToRawIntBits(0f), Float.floatToRawIntBits(0f - dh2), Float.floatToRawIntBits(fi), southcolor, u0, v0, lsouth,
                            Float.floatToRawIntBits(fi), Float.floatToRawIntBits(0f - dh2), Float.floatToRawIntBits(fi), southcolor, u1, v0, lsouth,
                            Float.floatToRawIntBits(fi), Float.floatToRawIntBits(0f), Float.floatToRawIntBits(fi), southcolor, u1, v1, lsouth,
                            Float.floatToRawIntBits(0f), Float.floatToRawIntBits(0f), Float.floatToRawIntBits(fi), southcolor, u0, v1, lsouth
                    };

                    builder.addVertexData(vertexData);
                    builder.putPosition(worldX, height, worldZ);
                }

                // WEST FACE

                float dh3 = (float) (cache.heightmap(world, worldX, worldZ, i) - cache.heightmap(world, worldX - i, worldZ, i));
                if (i == 1 && x == 0) dh3 = height; // temporary fix for connection between vanilla render and LOD

                if (dh3 > 0) {
                    int westcolor = cache.colormap(world, worldX, worldZ, EnumFacing.WEST, i);
                    int lwest = (int) (lup * 0.8f);

                    vertexData = new int[]{
                            Float.floatToRawIntBits(0f), Float.floatToRawIntBits(0f - dh3), Float.floatToRawIntBits(0f), westcolor, u0, v0, lwest,
                            Float.floatToRawIntBits(0f), Float.floatToRawIntBits(0f - dh3), Float.floatToRawIntBits(fi), westcolor, u1, v0, lwest,
                            Float.floatToRawIntBits(0f), Float.floatToRawIntBits(0f), Float.floatToRawIntBits(fi), westcolor, u1, v1, lwest,
                            Float.floatToRawIntBits(0f), Float.floatToRawIntBits(0f), Float.floatToRawIntBits(0f), westcolor, u0, v1, lwest
                    };

                    builder.addVertexData(vertexData);
                    builder.putPosition(worldX, height, worldZ);
                }

                // EAST FACE

                float dh4 = (float) (cache.heightmap(world, worldX, worldZ, i) - cache.heightmap(world, worldX + i, worldZ, i));
                if (i == 1 && x == 15) dh4 = height; // temporary fix for connection between vanilla render and LOD

                if (dh4 > 0) {
                    int eastcolor = cache.colormap(world, worldX, worldZ, EnumFacing.EAST, i);
                    int least = (int) (lup * 0.8f);

                    vertexData = new int[]{
                            Float.floatToRawIntBits(fi), Float.floatToRawIntBits(0f), Float.floatToRawIntBits(0f), eastcolor, u0, v0, least,
                            Float.floatToRawIntBits(fi), Float.floatToRawIntBits(0f), Float.floatToRawIntBits(fi), eastcolor, u1, v0, least,
                            Float.floatToRawIntBits(fi), Float.floatToRawIntBits(0f - dh4), Float.floatToRawIntBits(fi), eastcolor, u1, v1, least,
                            Float.floatToRawIntBits(fi), Float.floatToRawIntBits(0f - dh4), Float.floatToRawIntBits(0f), eastcolor, u0, v1, least
                    };

                    builder.addVertexData(vertexData);
                    builder.putPosition(worldX, height, worldZ);
                }
            }
        }
    }
}
