package zh.vendetta.heightmaplod;

import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MeshBuilderCache {
    private static final Map<IBlockState, int[]> blockStateColorCache = new HashMap<>();

    private final Long2IntOpenHashMap heightCache = new Long2IntOpenHashMap();

    public MeshBuilderCache() {
        heightCache.defaultReturnValue(-1);
    }

    private static TextureAtlasSprite getFaceTexture(IBlockState state, EnumFacing face) {
        Minecraft mc = Minecraft.getMinecraft();
        BlockRendererDispatcher dispatcher = mc.getBlockRendererDispatcher();
        IBakedModel model = dispatcher.getModelForState(state);

        List<BakedQuad> quads = model.getQuads(state, face, 0L);
        TextureAtlasSprite sprite = null;

        if (!quads.isEmpty()) sprite = quads.get(0).getSprite();

        if (sprite == null || sprite == mc.getTextureMapBlocks().getMissingSprite())
            sprite = dispatcher.getBlockModelShapes().getTexture(state);

        return sprite;
    }

    private static int EnumFacing2Int(EnumFacing face) {
        switch (face) {
            case UP: return 0;
            case NORTH: return 1;
            case SOUTH: return 2;
            case WEST: return 3;
            case EAST: return 4;
            case DOWN: return 5;
        }
        return -1; // unreachable
    }

    private static int getBlockStateColor(IBlockState state, EnumFacing face) {
        int index = EnumFacing2Int(face);
        int[] cache = blockStateColorCache.getOrDefault(state, new int[]{-1, -1, -1, -1, -1, -1});
        if (cache[index] != -1) return cache[index];

        TextureAtlasSprite sprite = getFaceTexture(state, face);
        if (sprite.getFrameCount() == 0) return 0xFFFFFFFF;

        int count = 0;
        float y = 0, cb = 0, cr = 0; // JPEG-like color
        for (int argb : sprite.getFrameTextureData(0)[0]) {
            int a = (argb >> 24) & 0xFF;
            if (a < 128) continue;

            int r = (argb >> 16) & 0xFF;
            int g = (argb >> 8) & 0xFF;
            int b = argb & 0xFF;

            y  +=  0.299f * r + 0.587f * g + 0.114f * b;
            cb += -0.168736f * r - 0.331264f * g + 0.5f * b + 128f;
            cr +=  0.5f * r - 0.418688f * g - 0.081312f * b + 128f;

            count++;
        }

        if (count == 0) return 0xFFFFFFFF;

        y /= count;
        cb /= count;
        cr /= count;

        int r = (int)(y + 1.402f * (cr - 128f));
        int g = (int)(y - 0.344136f * (cb - 128f) - 0.714136f * (cr - 128f));
        int b = (int)(y + 1.772f * (cb - 128f));

        cache[index] = (r << 16) | (g << 8) | b;
        blockStateColorCache.put(state, cache);
        return cache[index];
    }

    private static int getBlockColor(World world, int x, int z, EnumFacing face) {
        BlockPos pos = new BlockPos(x, world.getHeight(x, z) - 1, z);
        IBlockState state = world.getBlockState(pos);

        // individual check for grass because it's sides has green color
        boolean isGrassSide = state.getBlock() == Blocks.GRASS && face != EnumFacing.UP;
        if (isGrassSide) face = EnumFacing.DOWN;

        int color = getBlockStateColor(state, face);
        int biomeColor = Minecraft.getMinecraft().getBlockColors().colorMultiplier(state, world, pos, 0);
        if (biomeColor == -1 || isGrassSide) return color;

        int r = ((color >> 16) & 0xFF) * ((biomeColor >> 16) & 0xFF) / 255;
        int g = ((color >> 8) & 0xFF) * ((biomeColor >> 8) & 0xFF) / 255;
        int b = (color & 0xFF) * (biomeColor & 0xFF) / 255;

        return (r << 16) | (g << 8) | b;
    }

    public int colormap(World world, int x, int z, EnumFacing face, int i) {
        int r = 0, g = 0, b = 0;
        for (int ix = x; ix < x + i; ++ix) {
            for (int iz = z; iz < z + i; ++iz) {
                int color = getBlockColor(world, ix, iz, face);
                r += (color >> 16) & 0xFF;
                g += (color >> 8) & 0xFF;
                b += color & 0xFF;
            }
        }
        i *= i;
        return (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) ?
                 (r / i) | ((g / i) << 8) | ((b / i) << 16) | (0xFF << 24) :
                ((r / i) << 24) | ((g / i) << 16) | ((b / i) << 8) | 0xFF;
    }

    public int heightmap(World world, int x, int z, int i) {
        long key = ChunkPos.asLong(x, z);
        int cache = heightCache.get(key);
        if (cache != -1) return cache;

        int height = 0;
        for (int ix = x; ix < x + i; ++ix) {
            for (int iz = z; iz < z + i; ++iz) {
                height += world.getHeight(ix, iz);
            }
        }
        height = height / i / i;

        heightCache.put(key, height);
        return height;
    }

    // TODO: remove after speed test
    public int testHeightMap(World world, int x, int z, int i) {
        long key = ((long) x << 32) | (z & 0xFFFFFFFFL);
        int cache = heightCache.get(key);
        if (cache != -1) return cache;

        int height = 0;
        for (int ix = x; ix < x + i; ++ix) {
            for (int iz = z; iz < z + i; ++iz) {
                height += world.getHeight(ix, iz);
            }
        }
        return height / i / i;
    }

    public int lightmap(World world, int x, int z, int i) {
        int max = 0;
        for (int ix = x; ix < x + i; ++ix) {
            for (int iz = z; iz < z + i; ++iz) {
                int b = world.getCombinedLight(new BlockPos(ix, world.getHeight(ix, iz), iz), 0);
                if (b > max) max = b;
            }
        }
        return max;
    }
}
