import java.util.Arrays;

import cn.fpsboost.Client;
import net.minecraft.client.main.Main;

public class Start
{
    public static void main(String[] args)
    {
        Client.isDev = true;
        Main.main(concat(new String[] {"--version", "mcp", "--accessToken", "0", "--assetsDir", "assets", "--assetIndex", "1.8", "--userProperties", "{}"}, args)); // while 别在后面扔代码
    }

    public static <T> T[] concat(T[] first, T[] second)
    {
        T[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }
}
