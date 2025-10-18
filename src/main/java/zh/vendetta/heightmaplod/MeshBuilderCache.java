package zh.vendetta.heightmaplod;

import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

public class MeshBuilderCache {
    private static final Map<IBlockState, Integer> blockStateColorCache = new HashMap<>();

    private final Long2IntOpenHashMap heightCache = new Long2IntOpenHashMap();

    public MeshBuilderCache() {
        heightCache.defaultReturnValue(-1);
    }

    private static int getBlockStateColor(IBlockState state) {
        int cache = blockStateColorCache.getOrDefault(state, -1);
        if (cache != -1) return cache;

        BlockRendererDispatcher dispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
        TextureAtlasSprite sprite = dispatcher.getBlockModelShapes().getTexture(state);

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

        int color = (r << 16) | (g << 8) | b;
        blockStateColorCache.put(state, color);
        return color;
    }

    private static int getBlockColor(World world, int x, int z) {
        BlockPos pos = new BlockPos(x, world.getHeight(x, z) - 1, z);
        IBlockState state = world.getBlockState(pos);

        int color = getBlockStateColor(state);
        int biomeColor = Minecraft.getMinecraft().getBlockColors().colorMultiplier(state, world, pos, 0);

        if (biomeColor == -1) return color;

        int r = ((color >> 16) & 0xFF) * ((biomeColor >> 16) & 0xFF) / 255;
        int g = ((color >> 8) & 0xFF) * ((biomeColor >> 8) & 0xFF) / 255;
        int b = (color & 0xFF) * (biomeColor & 0xFF) / 255;

        return (r << 16) | (g << 8) | b;
    }

    public float[] colormap(World world, int x, int z, int i) {
        float r = 0, g = 0, b = 0;
        for (int ix = x; ix < x + i; ++ix) {
            for (int iz = z; iz < z + i; ++iz) {
                int color = getBlockColor(world, ix, iz);
                r += (color >> 16) & 0xFF;
                g += (color >> 8) & 0xFF;
                b += color & 0xFF;
            }
        }
        float s = i * i * 255f;
        return new float[]{r / s, g / s, b / s};
    }

    public int heightmap(World world, int x, int z, int i) {
        long key = ((long) x << 32) | (z & 0xFFFFFFFFL);
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
