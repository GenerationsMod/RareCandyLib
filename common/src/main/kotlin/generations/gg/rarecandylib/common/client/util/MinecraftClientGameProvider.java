package generations.gg.rarecandylib.common.client.util;

public class MinecraftClientGameProvider {
    private static final double START_TIME = System.currentTimeMillis();

    public static double getTimePassed() {
        return (System.currentTimeMillis() - START_TIME) / 1000f;
    }
}