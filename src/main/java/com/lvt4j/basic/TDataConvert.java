package com.lvt4j.basic;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * 基本上可以做到所有基本数据类型之间的转换
 * @author LV
 */
public class TDataConvert {

    private static char[] lowChar = { '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
    private static char[] upChar = { '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

    public static byte bit2Byte(boolean bit) {
        return (byte) (bit ? 1 : 0);
    }

    public static char bit2BitChar(boolean bit) {
        return bit ? '1' : '0';
    }

    public static String bit2BitStr(boolean bit) {
        return bit ? "1" : "0";
    }

    public static byte bitS2Byte(boolean[] bitS) {
        if (bitS.length != 8)
            throw new RuntimeException("Illegal bitS length<" + bitS.length + ">!=8");
        byte b = 0;
        for (int i = 0; i <= 7; i++)
            b += bitS[i] ? (1 << (7 - i)) : 0;
        return b;
    }

    public static byte[] bitS2ByteS(boolean[] bitS) {
        if (bitS.length % 8 != 0)
            throw new RuntimeException("Illegal bitS length<" + bitS.length + ">");
        byte[] byteS = new byte[bitS.length / 8];
        for (int i = 0; i < byteS.length; i++) {
            boolean[] temBits = new boolean[8];
            System.arraycopy(bitS, i * 8, temBits, 0, 8);
            byteS[i] = bitS2Byte(temBits);
        }
        return byteS;
    }

    public static String bitS2BitStr(boolean[] bitS) {
        char[] cS = new char[bitS.length];
        for (int i = 0; i < cS.length; i++)
            cS[i] = bit2BitChar(bitS[i]);
        return new String(cS);
    }

    public static short bitS2Short(boolean[] bitS) {
        if (bitS.length != 16)
            throw new RuntimeException("Illegal bitS length<" + bitS.length + ">!=16.");
        short d = 0;
        for (int i = 0; i <= 15; i++)
            d += bitS[i] ? (1 << (15 - i)) : 0;
        return d;
    }

    public static int bitS2Int(boolean[] bitS) {
        if (bitS.length != 32)
            throw new RuntimeException("Illegal bitS length<" + bitS.length + ">!=32.");
        int d = 0;
        for (int i = 0; i <= 31; i++)
            d += bitS[i] ? (1 << (31 - i)) : 0;
        return d;
    }

    public static String bitS2Str(boolean[] bitS) {
        try {
            return new String(bitS2ByteS(bitS), "UTF-8");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean byte2Bit(byte b) {
        if (b == 1)
            return true;
        if (b == 0)
            return false;
        throw new RuntimeException("Unsupported byte<" + b + ">!=[0-1].");
    }

    public static boolean[] byte2BitS(byte b) {
        boolean[] bitS = new boolean[8];
        for (int i = 7; i >= 0; i--) {
            bitS[i] = (b & 1) == 1;
            b >>>= 1;
        }
        return bitS;
    }

    public static String byte2HexStr(byte b) {
        char[] cS = new char[2];
        cS[1] = lowChar[b & 0xF];
        cS[0] = lowChar[(b >> 4) & 0xF];
        return new String(cS);
    }

    public static boolean[] byteS2BitS(byte[] byteS) {
        boolean[] bitS = new boolean[byteS.length * 8];
        for (int i = 0; i < byteS.length; i++)
            System.arraycopy(byte2BitS(byteS[i]), 0, bitS, i * 8, 8);
        return bitS;
    }

    public static short byteS2Short(byte[] byteS) {
        if (byteS.length != 2)
            throw new RuntimeException("Illegal byteS length<" + byteS.length
                    + ">!=2.");
        short d = (short) (byteS[0] & 0xFF);
        d <<= 8;
        d |= (byteS[1] & 0xFF);
        return d;
    }

    public static int byteS2Int(byte[] byteS) {
        if (byteS.length != 4)
            throw new RuntimeException("Illegal byteS length<" + byteS.length
                    + ">!=4.");
        int d = (byteS[0] & 0xFF);
        for (int i = 1; i < 4; i++) {
            d <<= 8;
            d |= (byteS[i] & 0xFF);
        }
        return d;
    }

    public static long byteS2Long(byte[] byteS) {
        if (byteS.length != 8)
            throw new RuntimeException("Illegal byteS length<" + byteS.length
                    + ">!=8.");
        long d = (byteS[0] & 0xFF);
        for (int i = 1; i < 8; i++) {
            d <<= 8;
            d |= (byteS[i] & 0xFF);
        }
        return d;
    }

    public static String byteS2HexStr(byte[] byteS) {
        StringBuilder sb = new StringBuilder();
        for (byte b : byteS)
            sb.append(byte2HexStr(b));
        return sb.toString();
    }

    public static Object byteS2Obj(byte[] byteS) {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(byteS);
            ObjectInputStream ois = new ObjectInputStream(bais);
            return ois.readObject();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
    
    public static boolean[] short2BitS(short s) {
        boolean[] bitS = new boolean[16];
        for (int i = 15; i >= 0; i--) {
            bitS[i] = (s & 1) == 1;
            s >>>= 1;
        }
        return bitS;
    }

    public static byte[] short2ByteS(short s) {
        byte[] byteS = new byte[2];
        byteS[1] = (byte) (s & 0xFF);
        s >>= 8;
        byteS[0] = (byte) (s & 0xFF);
        return byteS;
    }

    public static String short2HexStr(short s) {
        char[] cS = new char[4];
        for (int i = 3; i >= 0; i--) {
            cS[i] = lowChar[s & 0xF];
            s >>= 4;
        }
        return new String(cS);
    }

    public static boolean[] int2BitS(int d) {
        boolean[] bitS = new boolean[32];
        for (int i = 31; i >= 0; i--) {
            bitS[i] = (d & 1) == 1;
            d >>>= 1;
        }
        return bitS;
    }

    public static byte[] int2ByteS(int d) {
        byte[] byteS = new byte[4];
        for (int i = 3; i > 0; i--) {
            byteS[i] = (byte) (d & 0xFF);
            d >>= 8;
        }
        byteS[0] = (byte) (d & 0xFF);
        return byteS;
    }

    public static String int2HexStr(int d) {
        char[] cS = new char[8];
        for (int i = 7; i >= 0; i--) {
            cS[i] = lowChar[d & 0xF];
            d >>= 4;
        }
        return new String(cS);
    }

    public static byte[] long2ByteS(long d) {
        byte[] byteS = new byte[8];
        for (int i = 7; i > 0; i--) {
            byteS[i] = (byte) (d & 0xFF);
            d >>= 8;
        }
        byteS[0] = (byte) (d & 0xFF);
        return byteS;
    }

    public static String long2HexStr(long d) {
        char[] cS = new char[16];
        for (int i = 15; i >= 0; i--) {
            cS[i] = lowChar[(int) (d & 0xF)];
            d >>= 4;
        }
        return new String(cS);
    }

    public static boolean[] str2BitS(String str) {
        try {
            return byteS2BitS(str.getBytes("UTF-8"));
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static String str2HexStr(String str) {
        try {
            return byteS2HexStr(str.getBytes("UTF-8"));
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean bitChar2Bit(char bitChar) {
        if (bitChar == '1')
            return true;
        if (bitChar == '0')
            return false;
        throw new RuntimeException("Illegal bitChar<" + bitChar + ">!=[0-1]");
    }

    public static boolean[] bitStr2BitS(String bitStr) {
        boolean[] bitS = new boolean[bitStr.length()];
        for (int i = 0; i < bitS.length; i++)
            bitS[i] = bitChar2Bit(bitStr.charAt(i));
        return bitS;
    }

    public static boolean bitStr2Bit(String bitStr) {
        if (bitStr.length() != 1)
            throw new RuntimeException("Illegal BitStr length<" + bitStr.length()
                    + ">!=1.");
        if ("1".equals(bitStr))
            return true;
        if ("0".equals(bitStr))
            return false;
        throw new RuntimeException("Illegal bitStr<" + bitStr + ">!=[0-1]");
    }

    public static byte hexStr2Byte(String hexStr) {
        if (hexStr.length() != 2)
            throw new RuntimeException("Illegal HexString length<" + hexStr.length()
                    + ">!=2.");
        char[] cS = hexStr.toCharArray();
        return (byte) ((digit(cS[0]) << 4) | digit(cS[1]));
    }

    public static byte[] hexStr2ByteS(String hexStr) {
        if (hexStr.length() % 2 != 0)
            throw new RuntimeException("Illegal HexString length<" + hexStr.length()
                    + ">%2!=0.");
        byte[] byteS = new byte[hexStr.length() / 2];
        for (int i = 0; i < byteS.length; i++)
            byteS[i] = hexStr2Byte(hexStr.substring(i * 2, i * 2 + 2));
        return byteS;
    }

    public static short hexStr2Short(String hexStr) {
        if (hexStr.length() != 4)
            throw new RuntimeException("Illegal HexString length<" + hexStr.length()
                    + ">!=4.");
        short d = 0;
        for (int i = 0; i < 3; i++) {
            d |= digit(hexStr.charAt(i));
            d <<= 4;
        }
        d |= digit(hexStr.charAt(3));
        return d;
    }

    public static int hexStr2Int(String hexStr) {
        if (hexStr.length() != 8)
            throw new RuntimeException("Illegal HexString length<" + hexStr.length()
                    + ">!=8.");
        int d = 0;
        for (int i = 0; i < 7; i++) {
            d |= digit(hexStr.charAt(i));
            d <<= 4;
        }
        d |= digit(hexStr.charAt(7));
        return d;
    }

    public static long hexStr2Long(String hexStr) {
        if (hexStr.length() != 16)
            throw new RuntimeException("Illegal HexString length<" + hexStr.length()
                    + ">!=16.");
        long d = 0;
        for (int i = 0; i < 15; i++) {
            d |= digit(hexStr.charAt(i));
            d <<= 4;
        }
        d |= digit(hexStr.charAt(15));
        return d;
    }

    public static String hexStr2Str(String hexStr) {
        try {
            return new String(hexStr2ByteS(hexStr), "UTF-8");
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static final byte[] obj2ByteS(Object obj) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(obj);
            return baos.toByteArray();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
    
    private static int digit(char c) {
        for (int i = 0; i < lowChar.length; i++)
            if (c == lowChar[i])
                return i;
        for (int i = 0; i < upChar.length; i++)
            if (c == upChar[i])
                return i;
        throw new RuntimeException("Unsupported char<" + c + ">!=[0-9a-fA-F].");
    }
}
