package cn.imaginary.toolkit;

import cn.imaginary.toolkit.image.PixelSheet;
import cn.imaginary.toolkit.json.JsonObject;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import javax.imageio.ImageIO;

public class PixelPacker {
    public static int MaxSize = -1;
    public static int KeepSize = 0;

    private int type_Prefix = -1;
    private int type_Suffix = 1;

    private String lineSeparator = System.lineSeparator();
    private String sign_comment = "sprite pack";
    private String sign_encoding = "utf-8";
    private String sign_png = "png";

    public String suffix_Json = ".json";
    public String suffix_Png = ".png";
    public String suffix_Pack = "_pack_";
    public String suffix_Unpack = "_unpack_";
    public String suffix_Polygon = "polygon_";
    public String suffix_Polygon_Non = "polygon_non_";
    public String suffix_Properties = "properties_";
    public String suffix_Xml = ".xml";

    public String tag_bounds_x = PixelSheet.tag_bounds_x;
    public String tag_bounds_y = PixelSheet.tag_bounds_y;
    public String tag_bounds_width = PixelSheet.tag_bounds_width;
    public String tag_bounds_height = PixelSheet.tag_bounds_height;
    public String tag_name = "name";
    public String tag_path = "path";
    public String tag_shape = "shape";

    private PixelSheet pixelSheet = new PixelSheet();

    private String readString(File file) {
        if (null != file) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String string;
                StringBuilder stringBuilder = new StringBuilder();
                while ((string = reader.readLine()) != null) {
                    stringBuilder.append(string);
                    stringBuilder.append(lineSeparator);
                }
                reader.close();
                return stringBuilder.toString();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    public Properties readProperties(File file) {
        return readJson(file);
//        return readXML(file);
    }

    private Properties readJson(File file) {
        String string = readString(file);
        JsonUtils jsonUtils = new JsonUtils();
        JsonObject jsonObject = jsonUtils.parseJsonObject(string);
        Properties properties = toProperties(jsonObject);
        if (null == properties.get(tag_path)) {
            File dirFile = file.getParentFile();
            File imageFile = new File(dirFile, getPrefix(file.getName()) + suffix_Png);
            String imagePath = imageFile.getAbsolutePath();
            properties.put(tag_path, imagePath);
        }
        return properties;
    }

    private Properties readXML(File file) {
        Properties properties = new Properties();
        try {
            properties.loadFromXML(Files.newInputStream(file.toPath()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return properties;
    }

    private Properties toProperties(JsonObject jsonObject) {
        if (null != jsonObject) {
            Properties properties = new Properties();
            Properties prop = jsonObject.get();
            Set<Object> kset = prop.keySet();
            for (Iterator<Object> iterator = kset.iterator(); iterator.hasNext(); ) {
                Object key = iterator.next();
                Object value = prop.get(key);
                if (value instanceof JsonObject) {
                    value = toProperties((JsonObject) value);
                }
                properties.put(key, value);
            }
            return properties;
        }
        return null;
    }

    private void writeString(String string, File file) {
        if (null != string) {
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                writer.write(string);
                writer.flush();
                writer.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void writeProperties(Properties properties, File file) {
//        System.out.println("properties: " + properties);
        writeJson(properties, file);
//        writeXML(properties, file);
    }

    private void writeJson(Properties properties, File file) {
        if (null != properties) {
            JsonObject jsonObject = toJsonObject(properties);
            String info = jsonObject.toString();
            writeString(info, file);
        }
    }

    private void writeXML(Properties properties, File file) {
        if (null != properties && null != file) {
            try {
                properties.storeToXML(Files.newOutputStream(file.toPath()), sign_comment, sign_encoding);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private JsonObject toJsonObject(Properties properties) {
        JsonObject jsonObject = null;
        if (null != properties) {
            jsonObject = new JsonObject();
            Set<Object> kset = properties.keySet();
            for (Iterator<Object> iterator = kset.iterator(); iterator.hasNext(); ) {
                Object key = iterator.next();
                Object value = properties.get(key);
                if (value instanceof Properties) {
                    jsonObject.add(key.toString(), toJsonObject((Properties) value));
                } else {
                    jsonObject.add(key.toString(), value);
                }
            }
        }
        return jsonObject;
    }

    public BufferedImage read(File file) {
        try {
            return ImageIO.read(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void write(BufferedImage image, File file) {
        if (null != image && null != file) {
            try {
                ImageIO.write(image, sign_png, file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void write(BufferedImage image, String filePath) {
        if (null != filePath) {
            write(image, new File(filePath));
        }
    }

    public File newFile(File file, Object name, String suffix, boolean isSuffix) {
        if (null != file && null != name && null != suffix) {
            File dirFile = file.getParentFile();
            if (null != dirFile) {
                String fileName = file.getName();
                if (isSuffix) {
                    fileName = getPrefix(fileName) + name + suffix;
                } else {
                    fileName = getPrefix(name.toString()) + suffix;
                }
                return new File(dirFile, fileName);
            }
        }
        return null;
    }

    private String getPrefix(String string) {
        return newString(string, type_Prefix);
    }

    private String getSuffix(String string) {
        return newString(string, type_Suffix);
    }

    private String newString(String string, int suffixType) {
        if (null != string) {
            String prefix;
            String suffix;
            int index = string.lastIndexOf(".");
            if (index != -1) {
                prefix = string.substring(0, index);
                suffix = string.substring(index);
            } else {
                prefix = string;
                suffix = "";
            }
            if (suffixType == type_Prefix) {
                return prefix;
            } else if (suffixType == type_Suffix) {
                return suffix;
            }
        }
        return string;
    }

    public void pack(File file, int x, int y, int width, int height, int lineSize, int rowSize, int lineWidth, int rowHeight, boolean isTrim) {
        if (null != file) {
            File dirFile;
            if (file.isFile()) {
                dirFile = file.getParentFile();
            } else {
                dirFile = file;
            }
            if (null != dirFile) {
                File[] array = dirFile.listFiles();
                BufferedImage image = pack(array, x, y, width, height, lineSize, rowSize, lineWidth, rowHeight, isTrim);
                String info = suffix_Pack + x + y + width + height + lineSize + rowSize + lineWidth + rowHeight + isTrim;
                Properties properties = pixelSheet.getPackProperties();
                updateProperties(properties, array);
                pack(dirFile, image, properties, info);
            }
        }
    }

    private void updateProperties(Properties properties, File[] array) {
        if (null != properties && null != array) {
            for (int i = 0; i < array.length; i++) {
                Object key = i;
                Object value = properties.get(key);
                if (null != value) {
                    if (value instanceof Properties) {
                        Properties prop = (Properties) value;
                        prop.put(tag_name, array[i].getName());
                        properties.put(key, value);
                    }
                }
            }
        }
    }

    public void pack(File dirFile, BufferedImage image, Properties properties, String info) {
        if (null != dirFile && null != image && null != info) {
            String path = dirFile.getAbsolutePath();
            String imagePath = path + info + suffix_Png;
            write(image, new File(imagePath));
            String propPath = path + info + suffix_Json;
            if (null != properties) {
                properties.put(tag_path, path);
                writeProperties(properties, new File(propPath));
            }
        }
    }

    public void pack(File imageFile, File propFile, boolean isTrim) {
        pack(imageFile, readProperties(propFile), isTrim);
    }

    public void pack(File imageFile, Properties properties, boolean isTrim) {
        if (null != properties) {
            if (null == imageFile) {
                Object path = properties.get(tag_path);
                if (null != path) {
                    imageFile = new File(path.toString());
                } else {
                    return;
                }
            }
            File dirFile;
            if (imageFile.isFile()) {
                dirFile = imageFile.getParentFile();
            } else {
                dirFile = imageFile;
            }
            if (null != dirFile) {
                String info = suffix_Pack + suffix_Properties + isTrim;
                BufferedImage image = pack(dirFile.listFiles(), properties, isTrim);
                pack(dirFile, image, null, info);
            }
        } else {
            if (null != imageFile) {
                packMaxSize(imageFile, isTrim);
            }
        }
    }

    private BufferedImage pack(File[] array, int x, int y, int width, int height, int lineSize, int rowSize, int lineWidth, int rowHeight, boolean isTrim) {
        if (null != array) {
            ArrayList<BufferedImage> arrayList = new ArrayList<>();
            for (int i = 0; i < array.length; i++) {
                BufferedImage image = read(array[i]);
                if (null != image) {
                    arrayList.add(image);
                }
            }
            return pack(arrayList, x, y, width, height, lineSize, rowSize, lineWidth, rowHeight, isTrim);
        }
        return null;
    }

    private BufferedImage pack(File[] array, Properties properties, boolean isTrim) {
        if (null != array) {
            ArrayList<BufferedImage> arrayList = new ArrayList<>();
            for (int i = 0; i < array.length; i++) {
                BufferedImage image = read(array[i]);
                if (null != image) {
                    arrayList.add(image);
                }
            }
            return pack(arrayList, properties, isTrim);
        }
        return null;
    }

    private BufferedImage pack(ArrayList<BufferedImage> arrayList, int x, int y, int width, int height, int lineSize, int rowSize, int lineWidth, int rowHeight, boolean isTrim) {
        return pack(toArray(arrayList), x, y, width, height, lineSize, rowSize, lineWidth, rowHeight, isTrim);
    }

    private BufferedImage pack(ArrayList<BufferedImage> arrayList, Properties properties, boolean isTrim) {
        return pack(toArray(arrayList), properties, isTrim);
    }

    private BufferedImage pack(BufferedImage[] array, int x, int y, int width, int height, int lineSize, int rowSize, int lineWidth, int rowHeight, boolean isTrim) {
        return pixelSheet.pack(array, x, y, width, height, lineSize, rowSize, lineWidth, rowHeight, isTrim);
    }

    private BufferedImage pack(BufferedImage[] array, Properties properties, boolean isTrim) {
        return pixelSheet.pack(array, properties, isTrim);
    }

    public void packKeepSize(File file, boolean isTrim) {
        packKeepSize(file, 0, 0, 0, 0, isTrim);
    }

    public void packKeepSize(File file, int x, int y, int lineWidth, int rowHeight, boolean isTrim) {
        pack(file, x, y, KeepSize, KeepSize, MaxSize, MaxSize, lineWidth, rowHeight, isTrim);
    }

    public void packMaxSize(File file, boolean isTrim) {
        packMaxSize(file, 0, 0, 0, 0, isTrim);
    }

    public void packMaxSize(File file, int x, int y, int lineWidth, int rowHeight, boolean isTrim) {
        pack(file, x, y, MaxSize, MaxSize, MaxSize, MaxSize, lineWidth, rowHeight, isTrim);
    }

    public void packLineKeepSize(File file, boolean isTrim) {
        packLineKeepSize(file, 0, 0, 0, 0, isTrim);
    }

    public void packLineKeepSize(File file, int x, int y, int lineWidth, int rowHeight, boolean isTrim) {
        pack(file, x, y, KeepSize, KeepSize, MaxSize, 1, lineWidth, rowHeight, isTrim);
    }

    public void packLineMaxSize(File file, boolean isTrim) {
        packLineMaxSize(file, 0, 0, 0, 0, isTrim);
    }

    public void packLineMaxSize(File file, int x, int y, int lineWidth, int rowHeight, boolean isTrim) {
        pack(file, x, y, MaxSize, MaxSize, MaxSize, 1, lineWidth, rowHeight, isTrim);
    }

    public void packRowKeepSize(File file, boolean isTrim) {
        packRowKeepSize(file, 0, 0, 0, 0, isTrim);
    }

    public void packRowKeepSize(File file, int x, int y, int lineWidth, int rowHeight, boolean isTrim) {
        pack(file, x, y, KeepSize, KeepSize, 1, MaxSize, lineWidth, rowHeight, isTrim);
    }

    public void packRowMaxSize(File file, boolean isTrim) {
        packRowMaxSize(file, 0, 0, 0, 0, isTrim);
    }

    public void packRowMaxSize(File file, int x, int y, int lineWidth, int rowHeight, boolean isTrim) {
        pack(file, x, y, MaxSize, MaxSize, 1, MaxSize, lineWidth, rowHeight, isTrim);
    }

    public void packPolygon(File file, boolean isTrim) {
        packPolygonTool(file, isTrim, false);
    }

    private BufferedImage packPolygon(BufferedImage[] array, boolean isTrim) {
        return pixelSheet.packPolygon(array, isTrim);
    }

    public void packPolygonNon(String filePath, boolean isTrim) {
        if (null != filePath) {
            packPolygonNon(new File(filePath), isTrim);
        }
    }

    public void packPolygonNon(File file, boolean isTrim) {
        packPolygonTool(file, isTrim, true);
    }

    public void packPolygonTool(File file, boolean isTrim, boolean isAlpha) {
        if (null != file) {
            File dirFile;
            if (file.isFile()) {
                dirFile = file.getParentFile();
            } else {
                dirFile = file;
            }
            if (null != dirFile) {
                BufferedImage image = packPolygonTool(dirFile.listFiles(), isTrim, isAlpha);
                String info = suffix_Pack;
                if (isAlpha) {
                    info += suffix_Polygon_Non + isTrim;
                } else {
                    info += suffix_Polygon + isTrim;
                }
                pack(dirFile, image, pixelSheet.getPackProperties(), info);
            }
        }
    }

    private BufferedImage packPolygonTool(File[] array, boolean isTrim, boolean isAlpha) {
        if (null != array) {
            ArrayList<BufferedImage> arrayList = new ArrayList<>();
            for (int i = 0; i < array.length; i++) {
                BufferedImage image = read(array[i]);
                if (null != image) {
                    arrayList.add(image);
                }
            }
            return packPolygonTool(arrayList, isTrim, isAlpha);
        }
        return null;
    }

    private BufferedImage packPolygonTool(ArrayList<BufferedImage> arrayList, boolean isTrim, boolean isAlpha) {
        if (isAlpha) {
            return packPolygonNon(toArray(arrayList), isTrim);
        } else {
            return packPolygon(toArray(arrayList), isTrim);
        }
    }

    private BufferedImage packPolygonNon(BufferedImage[] array, boolean isTrim) {
        return pixelSheet.packPolygonNon(array, isTrim);
    }

    public void unpack(File file, int x, int y, int width, int height, boolean isTrim) {
        if (null != file) {
            BufferedImage image = read(file);
            image = unpack(image, x, y, width, height, isTrim);
            String name = suffix_Unpack + x + y + width + height + isTrim;
            write(image, newFile(file, name, suffix_Png, true));
        }
    }

    public void unpack(File file, int x, int y, int width, int height, int lineSize, int rowSize, int lineWidth, int rowHeight, boolean isTrim) {
        if (null != file) {
            ArrayList<BufferedImage> arrayList = unpack(read(file), x, y, width, height, lineSize, rowSize, lineWidth, rowHeight, isTrim);
            if (null != arrayList) {
                String info = suffix_Unpack + x + y + width + height + lineSize + rowSize + lineWidth + rowHeight + isTrim;
                unpack(file, arrayList, pixelSheet.getUnpackProperties(), info);
            }
        }
    }

    public void unpack(File file, ArrayList<BufferedImage> arrayList, Properties properties, String info) {
        if (null != file && null != arrayList && null != info) {
            int index = 0;
            for (int i = 0; i < arrayList.size(); i++) {
                BufferedImage image = arrayList.get(i);
                Object name = null;
                if (null != properties) {
                    Object value = properties.get(String.valueOf(i));
                    if (null != value) {
                        Properties prop = (Properties) value;
                        name = prop.get(tag_name);
                    }
                }
                File imageFile;
                if (null == name) {
                    name = info + "_" + index;
                    imageFile = newFile(file, name, suffix_Png, true);
                } else {
                    imageFile = new File(file.getParentFile(), name.toString());
                }
                write(image, imageFile);
                index++;
            }
        }
    }

    public void unpack(File imageFile, File propFile, boolean isTrim) {
        unpack(imageFile, readProperties(propFile), isTrim);
    }

    public void unpack(File imageFile, Properties properties, boolean isTrim) {
        if (null != properties) {
            if (null == imageFile) {
                Object path = properties.get(tag_path);
                if (null != path) {
                    imageFile = new File(path.toString());
                } else {
                    return;
                }
            }
            String info = suffix_Unpack + suffix_Properties + isTrim;
            ArrayList<BufferedImage> arrayList = unpackList(read(imageFile), properties, isTrim);
//            unpack(imageFile, arrayList, null, info);
            unpack(imageFile, arrayList, properties, info);
        }
    }

    private BufferedImage unpack(BufferedImage root, int x, int y, int width, int height, boolean isTrim) {
        return pixelSheet.unpack(root, x, y, width, height, isTrim);
    }

    private ArrayList<BufferedImage> unpack(BufferedImage image, int x, int y, int width, int height, int lineSize, int rowSize, int lineWidth, int rowHeight, boolean isTrim) {
        return pixelSheet.unpack(image, x, y, width, height, lineSize, rowSize, lineWidth, rowHeight, isTrim);
    }

    private BufferedImage unpack(BufferedImage root, Properties properties, boolean isTrim) {
        return pixelSheet.unpack(root, properties, isTrim);
    }

    private ArrayList<BufferedImage> unpackList(BufferedImage root, Properties properties, boolean isTrim) {
        if (null != root && null != properties) {
            ArrayList<BufferedImage> arrayList = new ArrayList<>();
            Object x = properties.get(tag_bounds_x);
            Object y = properties.get(tag_bounds_y);
            Object width = properties.get(tag_bounds_width);
            Object height = properties.get(tag_bounds_height);
            if (null == x || null == y || null == width || null == height) {
                Set<Object> kset = properties.keySet();
                for (Iterator<Object> iterator = kset.iterator(); iterator.hasNext(); ) {
                    Object key = iterator.next();
                    Object value = properties.get(key);
                    if (value instanceof Properties) {
                        BufferedImage image = unpack(root, (Properties) value, isTrim);
                        if (null != image) {
                            arrayList.add(image);
                        }
                    }
                }
            } else {
                BufferedImage image = unpack(root, properties, isTrim);
                if (null != image) {
                    arrayList.add(image);
                }
            }
            return arrayList;
        }
        return null;
    }

    public BufferedImage[] toArray(ArrayList<BufferedImage> arrayList) {
        if (null != arrayList) {
            BufferedImage[] array = new BufferedImage[arrayList.size()];
            arrayList.toArray(array);
            return array;
        }
        return null;
    }
}
