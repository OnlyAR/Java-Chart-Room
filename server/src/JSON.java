import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

public class JSON {
    private final HashMap<String, String> data;

    public JSON(String path) throws IOException {
        try {
            String jsonData = Files.readString(Paths.get(path));
            Gson gson = new GsonBuilder().serializeNulls().create();
            this.data = gson.fromJson(jsonData, new TypeToken<HashMap<String, String>>() {}.getType());
        } catch (IOException e) {
            System.out.println("配置文件 config.json 未找到");
            throw e;
        }
    }

    public String get(String key) {
        return data.get(key);
    }
}
