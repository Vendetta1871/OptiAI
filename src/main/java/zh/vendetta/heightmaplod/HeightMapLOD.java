package zh.vendetta.heightmaplod;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(
        modid = HeightMapLOD.MOD_ID,
        name = HeightMapLOD.MOD_NAME,
        version = HeightMapLOD.VERSION
)
public class HeightMapLOD {

    public static final String MOD_ID = "heightmaplod";
    public static final String MOD_NAME = "HeightMapLOD";
    public static final String VERSION = "1.0";

    @Mod.EventHandler
    public void preinit(FMLPreInitializationEvent event) {
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        // MinecraftForge.EVENT_BUS.register(new SweetMixinListener());
    }

    @Mod.EventHandler
    public void postinit(FMLPostInitializationEvent event) {
    }
}