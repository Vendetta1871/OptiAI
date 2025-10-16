package zh.vendetta.heightmaplod;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

@Mod(
        modid = HeightmapLOD.MOD_ID,
        name = HeightmapLOD.MOD_NAME,
        version = HeightmapLOD.VERSION
)
public class HeightmapLOD {

    public static final String MOD_ID = "heightmaplod";
    public static final String MOD_NAME = "HeightmapLOD";
    public static final String VERSION = "1.0";

    public static Logger logger;

    @Mod.EventHandler
    public void preinit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        // MinecraftForge.EVENT_BUS.register(new SweetMixinListener());
    }

    @Mod.EventHandler
    public void postinit(FMLPostInitializationEvent event) {
    }
}