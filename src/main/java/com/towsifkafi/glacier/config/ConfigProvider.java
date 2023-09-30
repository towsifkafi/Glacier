package com.towsifkafi.glacier.config;

import com.towsifkafi.glacier.GlacierMain;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;

public class ConfigProvider {

    private final GlacierMain plugin;
    private HashMap<String, Object> config;
    private String fileName;

    private Path dataDirectory;

    public ConfigProvider(GlacierMain plugin, Path dataDirectory, String filename) {
        this.plugin = plugin;
        this.dataDirectory = dataDirectory;
        this.fileName = filename;
    }

    public void loadConfig() {
        Path dataDir = dataDirectory;
        if (!dataDir.toFile().exists()) dataDir.toFile().mkdirs();

        Yaml yaml = new Yaml();
        File configFile = new File(dataDir.toString(), fileName);
        if (!configFile.exists()) {
            InputStream inputStream = plugin
                    .getClass()
                    .getClassLoader()
                    .getResourceAsStream(fileName);
            try (OutputStream outputStream = new FileOutputStream(configFile, false)) {
                int read;
                byte[] bytes = new byte[8192];
                while ((read = inputStream.read(bytes)) != -1) {
                    outputStream.write(bytes, 0, read);
                }
            } catch (IOException e) {
                plugin.logger.error("Config file could not be created.");
                e.printStackTrace();
            }
        } else {
//            try {
//                File file = new File(dataDir.toString(), fileName);
//                DumperOptions options = new DumperOptions();
//                options.setAllowUnicode(true);
//                options.setIndent(2);
//                options.setPrettyFlow(true);
//                options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
//                Yaml yamlToWrite = new Yaml(options);
//                InputStream inputStream = new FileInputStream(file);
//                Map<String, Object> map = yamlToWrite.load(inputStream);
//                inputStream.close();
//                InputStream targetInputStream = plugin
//                        .getClass()
//                        .getClassLoader()
//                        .getResourceAsStream(fileName);
//                Map<String, Object> targetMap = yamlToWrite.load(targetInputStream);
//
//                Optional<Map<String, Object>> updatedOptional = updateMap(map, targetMap);
//                if (updatedOptional.isPresent()) {
//                    OutputStreamWriter writer =
//                            new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
//                    yamlToWrite.dump(updatedOptional.get(), writer);
//                }
//            } catch (IOException exception) {
//                plugin.logger.error("Config file could not be updated.");
//                exception.printStackTrace();
//            }
        }

        try {
            FileInputStream fileInputStream = new FileInputStream(configFile);
            config = new HashMap<>(yaml.load(fileInputStream));
            fileInputStream.close();
        } catch (IOException e) {
            plugin.logger.error("Config file could not be loaded.");
            e.printStackTrace();
        }
    }

    public String getString(String key) {
        Object value = getValue(key);
        return value != null ? (String) value : null;
    }

    public int getInt(String key) {
        Object value = getValue(key);
        return value != null ? (int) value : -1;
    }

    public boolean getBoolean(String key) {
        Object value = getValue(key);
        return value != null && (boolean) value;
    }

    @SuppressWarnings("unchecked")
    public List<String> getStringList(String key) {
        Object value = getValue(key);
        return value != null ? (List<String>) value : null;
    }


    @SuppressWarnings("unchecked")
    public LinkedHashMap<String, ?> getMap(String key) {
        Object value = getValue(key);
        return value != null ? (LinkedHashMap<String, ?>) value : null;
    }

    public Object getValue(String key) {
        if (key.contains(".")) {
            return getNestedValue(key);
        } else {
            return config.get(key);
        }
    }

    @SuppressWarnings("unchecked")
    public Object getNestedValue(String key) {
        String[] abstractKey = key.split("\\.");
        if (!config.containsKey(abstractKey[0])) return null;
        Map<String, ?> map = getMap(abstractKey[0]);
        int i = 2;
        List<String> list = new ArrayList<>();
        boolean first = true;
        for (String string : abstractKey) {
            if (first) {
                first = false;
                continue;
            }
            list.add(string);
        }
        for (String s : list) {
            if (map.containsKey(s) && (!(map.get(s) instanceof Map)
                    || i == abstractKey.length)) {
                return map.get(s);
            } else if (map.containsKey(s)) {
                map = (Map<String, ?>) map.get(s);
            }
            i++;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public Optional<Map<String, Object>> updateMap(
            Map<String, Object> map, Map<String, Object> targetMap) {
        Map<String, Object> temporaryMap = new HashMap<>(map);
        for (String targetKey : targetMap.keySet()) {
            if (targetMap.get(targetKey) instanceof Map nestedMap) {
                if (!map.containsKey(targetKey) || !(map.get(targetKey) instanceof Map)) {
                    temporaryMap.put(targetKey, targetMap.get(targetKey));
                    continue;
                }
                Optional<Map<String, Object>> nestedMapOptional =
                        updateMap((Map<String, Object>) map.get(targetKey),
                                (Map<String, Object>) nestedMap);
                nestedMapOptional.ifPresent(stringObjectMap ->
                        temporaryMap.put(targetKey, stringObjectMap));
            } else if (!map.containsKey(targetKey)) {
                temporaryMap.put(targetKey, targetMap.get(targetKey));
            }
        }
        if (!map.equals(temporaryMap)) return Optional.of(temporaryMap);
        return Optional.empty();
    }

}