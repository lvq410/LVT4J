package com.lvt4j.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lvt4j.basic.TCode;
import com.lvt4j.basic.TDataConvert;
import com.lvt4j.basic.TFile;

public class Dict {

	public static void gen() throws Exception {
		int[] letBegin = new int[26];
		int[] letEnd = new int[26];
		Map<Character, List<WordP>> index = new HashMap<Character, List<WordP>>();

		File root = new File("D:\\Download\\dictvoice");
		int lastIdx = 52 * 4;
		int binSize = 52 * 4;
		ComparWord comparWord = new ComparWord();
		for (int i = 0; i < 26; i++) {
			Character c = (char) ('a' + i);
			File folder = new File(root.getAbsoluteFile() + "\\" + c);
			List<WordP> letWordPs = new ArrayList<WordP>();
			index.put(c, letWordPs);
			if (folder.exists()) {
				for (File mp3 : folder.listFiles()) {
					WordP wordP = new WordP();
					wordP.setWord(mp3.getName().toLowerCase());
					wordP.file = mp3;
					wordP.len = (short) TFile.read(mp3).length;
					binSize += wordP.len + 2;
					letWordPs.add(wordP);
					System.out.println(mp3.getName());
				}
			}
			if (!letWordPs.isEmpty()) {
				letBegin[i] = lastIdx;
				letEnd[i] = letBegin[i] + 64 * (letWordPs.size() - 1);
				lastIdx = letBegin[i] + 64 * letWordPs.size();
				binSize += 64 * letWordPs.size();
				Collections.sort(letWordPs, comparWord);
			} else {
				letBegin[i] = -1;
				letEnd[i] = -1;
			}
			System.gc();
		}
		// 计算单词索引
		System.out.println("计算单词索引");
		for (int i = 0; i < 26; i++) {
			Character c = (char) ('a' + i);
			List<WordP> letWordPs = index.get(c);
			for (WordP wordP : letWordPs) {
				wordP.p = lastIdx;
				lastIdx = lastIdx + 2 + wordP.len;
			}
		}

		// 生成bin结果
		RandomAccessFile file = new RandomAccessFile(
				System.getProperty("user.dir") + "\\dict.bin", "rw");

		// byte[] bin = new byte[binSize];
		int binIdx = 0;
		// //写入字母索引
		System.out.println("写入字母索引");
		for (int i = 0; i < 26; i++) {
			file.writeInt(letBegin[i]);
			file.writeInt(letEnd[i]);
			binIdx += 8;
			// System.arraycopy(TDataConvert.int2ByteS(letBegin[i]), 0, bin,
			// binIdx, 4);
			// binIdx+=4;
			// System.arraycopy(TDataConvert.int2ByteS(letEnd[i]), 0, bin,
			// binIdx, 4);
			// binIdx+=4;
		}
		// //写入单词索引
		System.out.println("写入单词索引");
		for (int i = 0; i < 26; i++) {
			Character c = (char) ('a' + i);
			List<WordP> letWordPs = index.get(c);
			for (WordP wordP : letWordPs) {
				byte[] word = Arrays.copyOf(wordP.getWord().getBytes("UTF-8"), 60);
				file.write(word);
				file.writeInt(wordP.p);
				binIdx += 64;
				// System.arraycopy(word, 0, bin, binIdx, 60);
				// binIdx+=60;
				// System.arraycopy(TDataConvert.int2ByteS(wordP.p), 0, bin,
				// binIdx, 4);
				// binIdx+=4;
			}
		}
		// //写入单词数据
		System.out.println("写入单词数据");
		for (int i = 0; i < 26; i++) {
			Character c = (char) ('a' + i);
			List<WordP> letWordPs = index.get(c);
			for (WordP wordP : letWordPs) {
				System.out.println(wordP.getWord());
				file.writeShort(wordP.len);
				file.write(wordP.getData());
				binIdx += 2 + wordP.len;
				// System.arraycopy(TDataConvert.int2ByteS(wordP.len), 0, bin,
				// binIdx, 4);
				// binIdx+=4;
				// System.arraycopy(wordP.getData(), 0, bin, binIdx, wordP.len);
				// binIdx += wordP.len;
			}
		}
		file.close();
		System.out.println(binIdx == binSize);
		// TFile.write(new File(System.getProperty("user.dir")+"\\dict.bin"),
		// bin);
		// System.out.println();
	}

	public static byte[] get(String word) throws Exception {
		// 计算字母索引
		word = word.toLowerCase();
		Dict.word = word;
		RandomAccessFile raf = new RandomAccessFile(new File(
				System.getProperty("user.dir") + "\\dict.bin"), "r");
		Character firstChar = word.charAt(0);
		raf.seek((firstChar - 'a') * 8);
		int letBegin = raf.readInt();
		int letEnd = raf.readInt();
		int wordPos = searchWordPos(raf, letBegin, letEnd);
		System.out.println(wordPos);
		System.out.println(readWordAt(raf, wordPos));
		int dataPos = readWordDataPos(raf, wordPos);
		byte[] data = getWordData(raf,dataPos);
		TFile.write(new File(
				System.getProperty("user.dir") + "\\tem.mp3"), data);
		raf.close();
		return null;
	}

	public static String word;

	public static int searchWordPos(RandomAccessFile raf, int beginPos,
			int endPos) throws Exception {
		//二分法
		int middle = beginPos + (((endPos-beginPos)/64) >> 1)*64;
		String middleWord = readWordAt(raf, middle);
		if (middleWord.compareTo(word) == 0) {
			return middle;
		} else if (middleWord.compareTo(word) < 0) {
			if (endPos-middle==64) {
				if (readWordAt(raf, endPos).compareTo(word)==0) {
					return endPos;
				} else {
					return -1;
				}
			}
			return searchWordPos(raf, middle, endPos);
		} else {
			return searchWordPos(raf, beginPos, middle);
		}
		
		//--顺序查找法
//		while (beginPos<=endPos) {
//			String middleWord = readWordAt(raf, beginPos);
//			if (middleWord.equals(word)) {
//				return beginPos;
//			}
//			beginPos+=64;
//		}
//		return -1;
	}

	

	public static String readWordAt(RandomAccessFile raf, int pos)
			throws Exception {
		raf.seek(pos);
		byte[] wordData = new byte[60];
		raf.read(wordData);
		int realLen;

		for (realLen = wordData.length - 1; realLen >= 0; realLen--) {
			if (wordData[realLen] != 0) {
				wordData = Arrays.copyOf(wordData, realLen + 1);
				break;
			}
		}
		return new String(wordData, "UTF-8");
	}
	public static int readWordDataPos(RandomAccessFile raf, int pos)
			throws Exception {
		raf.seek(pos+60);
		return raf.readInt();
	}
	
	
	public static byte[] getWordData(RandomAccessFile raf, int pos)throws Exception {
		raf.seek(pos);
		int dataLen = raf.readShort();
		byte[] data = new byte[dataLen];
		raf.read(data);
		return data;
	}
	
	public static void testEqual() {
		
	}
	
	public static void main(String[] args) throws Exception {
		String word = "K";
		TFile.del(new File(System.getProperty("user.dir") + "\\tem.mp3"));
		get(word);
		String md51=TCode.MD5.encode(new File(System.getProperty("user.dir") + "\\tem.mp3"));
		String md52 = TCode.MD5.encode(new File("D:\\Download\\dictvoice\\"+word.charAt(0)+"\\"+word+".mp3"));
		System.out.println(md51);
		System.out.println(md51.equals(md52));
//		 gen();
	}


}

class WordP{
	private String word;
	public short len;
	public int p;
	public File file;

	
	public String getWord() {
		return word;
	}
	public void setWord(String word) {
		if (!word.endsWith(".mp3")) {
			System.out.println();
		}
		word = word.substring(0,word.length()-4);
		this.word = word;
	}
	@Override
	public String toString() {
		return word;
	}

	public byte[] getData() {
		return TFile.read(file);
	}
}
class ComparWord implements Comparator<WordP> {
	public int compare(WordP o1, WordP o2) {
		return o1.getWord().compareTo(o2.getWord());
	}
}

class CharCom implements Comparator<Character> {

	public int compare(Character o1, Character o2) {
		return o1.compareTo(o2);
	}

}