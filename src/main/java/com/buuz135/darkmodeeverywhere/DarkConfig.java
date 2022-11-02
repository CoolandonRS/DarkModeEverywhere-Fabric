package com.buuz135.darkmodeeverywhere;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;

public class DarkConfig {

    public static Client CLIENT = new Client();

    private static abstract class ConfigClass {
        public ForgeConfigSpec SPEC;

        public abstract void onConfigReload(ModConfig config);
    }

    public static class Client extends ConfigClass {
        public ForgeConfigSpec.ConfigValue<Integer> X;
        public ForgeConfigSpec.ConfigValue<Integer> Y;
        public ForgeConfigSpec.ConfigValue<String> NAME;

        public ForgeConfigSpec.ConfigValue<Integer> MAIN_X;
        public ForgeConfigSpec.ConfigValue<Integer> MAIN_Y;
        public ForgeConfigSpec.ConfigValue<Boolean> SHOW_IN_MAIN;
        public ForgeConfigSpec.ConfigValue<String> MAIN_NAME;

        public Client() {
            final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
            BUILDER.push("Button Position");
            X = BUILDER.comment("Pixels away from the bottom left of the GUI in the x axis").defineInRange("X", 4, 0, Integer.MAX_VALUE);
            Y = BUILDER.comment("Pixels away from the bottom left of the GUI in the y axis").defineInRange("Y", 0, 0, Integer.MAX_VALUE);
            NAME = BUILDER.define("NAME", "Dark Mode");
            BUILDER.pop();
            BUILDER.push("Main Menu Button");
            SHOW_IN_MAIN = BUILDER.comment("Enabled").define("SHOW", true);
            MAIN_X = BUILDER.comment("Pixels away from the bottom left of the GUI in the x axis").defineInRange("X", 4, 0, Integer.MAX_VALUE);
            MAIN_Y = BUILDER.comment("Pixels away from the bottom left of the GUI in the y axis").defineInRange("Y", 40, 0, Integer.MAX_VALUE);
            MAIN_NAME = BUILDER.define("NAME", "Dark Mode");
            BUILDER.pop();
            SPEC = BUILDER.build();
        }

        @Override
        public void onConfigReload(ModConfig config) {
            if (config.getType() == ModConfig.Type.COMMON) {
                SPEC.setConfig(config.getConfigData());
            }
        }
    }
}
