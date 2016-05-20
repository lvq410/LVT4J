package com.lvt4j.basic;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Some file tools for Java.
 * 
 * @author LV
 */
public class TFile {
    
    /**
     * Read file content to byte[].
     * 
     * @param file
     *            File to read.
     * @return File's content as byte[].
     */
    public static byte[] read(File file) {
        try {
            FileInputStream fis = new FileInputStream(file);
            byte[] bs = new byte[(int) file.length()];
            fis.read(bs);
            fis.close();
            return bs;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] read(String filePath) {
        return read(new File(filePath));
    }

    /**
     * Read file content to string.
     * 
     * @param file
     *            File to read.
     * @return File's content as String.
     */
    public static String read2Str(File file) {
        byte[] temBS = read(file);
        return new String(temBS, 0, temBS.length);
    }

    public static String read2Str(String filePath) {
        return read2Str(new File(filePath));
    }

    /**
     * Read file to serializable object;
     * @param file
     * @return
     */
    public static Object read2Obj(File file) {
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
            Object obj = ois.readObject();
            ois.close();
            return obj;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Write inputstream to all file.
     * 
     * @param file
     *            File to write.
     * @param is
     *            InputStream to write.
     */
    public static void write(File file, InputStream is) {
        try {
            byte[] buff = new byte[1024];
            int len;
            FileOutputStream fos = new FileOutputStream(file);
            while ((len = is.read(buff)) > 0)
                fos.write(buff, 0, len);
            is.close();
            fos.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Write byte[] to all file.
     * 
     * @param file
     *            File to write.
     * @param outData
     *            Data to write.
     */
    public final static void write(File file, byte[] outData) {
        try {
            if (!file.exists()) {
                if (file.getParentFile()!=null) {
                    file.getParentFile().mkdirs();
                }
                file.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(outData);
            fos.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Write String to file.
     * 
     * @param file
     *            File to write.
     * @param outStr
     *            String to write.
     */
    public final static void write(File file, String outStr) {
        write(file, outStr.getBytes());
    }

    /**
     * Write serializable object to file.
     * @param file
     * @param obj
     */
    public static void writeObj(File file, Object obj) {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
            oos.writeObject(obj);
            oos.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Get size of file/folder.
     * 
     * @param file
     *            File/Folder to get size.
     * @return File's size.
     */
    public static long size(File file) {
        long size = 0;
        if (!file.exists())
            return 0;
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                size += size(files[i]);
            }
        } else {
            size += file.length();
        }
        return size;
    }

    /**
     * Copy file/folder.
     * 
     * @param from
     *            File/Folder copy from.
     * @param to
     *            File/Folder copy to.
     */
    public static void copy(File from, File to) {
        try {
            if (from.isDirectory()) {
                to.mkdirs();
                File[] files = from.listFiles();
                for (int i = 0; i < files.length; i++) {
                    copy(files[i], new File(to.getAbsolutePath() + "\\"
                            + files[i].getName()));
                }
            } else {
                if (!to.getParentFile().exists())
                    to.getParentFile().mkdirs();
                FileInputStream fin = null;
                FileOutputStream fos = null;
                fin = new FileInputStream(from);
                fos = new FileOutputStream(to);
                byte[] buffer = new byte[1024];
                int numRead = 0;
                while ((numRead = fin.read(buffer)) > 0)
                    fos.write(buffer, 0, numRead);
                fin.close();
                fos.close();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Move file or folder.
     * 
     * @param from
     *            File/Folder move from.
     * @param to
     *            File/Folder move to.
     */
    public static void move(File from, File to) {
        if (from.isDirectory()) {
            to.mkdirs();
            File[] files = from.listFiles();
            for (int i = 0; i < files.length; i++) {
                move(files[i],
                        new File(to.getAbsolutePath() + "\\"
                                + files[i].getName()));
            }
            del(from);
        } else {
            from.renameTo(to);
        }
    }

    /**
     * Delete file/folder.
     * 
     * @param file
     *            File/Folder to delete.
     */
    public static void del(File file) {
        try {
            if (TSys.isWindows()) {
                Process p;
                if (file.isDirectory()) {
                    p = Runtime.getRuntime().exec(
                            "cmd /C rd /s /q \"" + file.getAbsolutePath()
                            + "\"");
                } else {
                    p = Runtime.getRuntime().exec(
                            "cmd /C del /q \"" + file.getAbsolutePath() + "\"");
                }
                String msg = getProcessorMsg(p);
                if (!"".equals(msg)) {
                    throw new RuntimeException(msg);
                }
            } else {
                if (file.isDirectory()) {
                    File[] files = file.listFiles();
                    for (int i = 0; i < files.length; i++) {
                        del(files[i]);
                    }
                }
                file.delete();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private static String getProcessorMsg(Process p) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        InputStream is = p.getInputStream();
        byte[] buff = new byte[1024];
        int readLen = 0;
        while ((readLen = is.read(buff)) > 0) {
            baos.write(buff, 0, readLen);
        }
        is.close();
        return new String(baos.toByteArray(), "GBK");
    }
}
