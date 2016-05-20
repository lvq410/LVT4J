package com.lvt4j.study;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lvt4j.basic.TChar;
import com.lvt4j.basic.TFile;
import com.lvt4j.basic.TPath;
import com.lvt4j.basic.TStr;

/**
 * <pre>
 * 获取单词发音工具类
 *  支持说明
 *      需要 res/study.TWordVoice/WordVoice.lib文件的支持
 *      可以使用setLibFile方法重新定位WordVoice.lib文件位置
 *  使用方法
 *      TWordVoice wordVoice = new TWordVoice("bus");	异常说明文件不存在或破损
 *      当有单词的发音时 wordVoice.isTotalMatch()==true
 *        使用getVoiceData()获得音频数据，为mp3形式
 *      当单词不完全匹配，但分割匹配时，如"a bus"可以分别匹配"a","bus"
 *        wordVoice.isSplitMatch()==true
 *        使用getSplitVoiceData()获得分割的音频数据
 * </pre>
 * 
 * @author LV
 * 
 */
public class TWordVoice {
	
	public static final String libFileName = "WordVoice.lib";
	
	private static File libFile = new File(TPath.appPath()
			+ "\\res\\study.TWordVoice\\"+libFileName);

	public static void setLibFile(File libFile) {
		synchronized (TWordVoice.libFile) {
			TWordVoice.libFile = libFile;
		}
	}

	public static void setLibFile(String libFilePath) {
		synchronized (libFile) {
			TWordVoice.libFile = new File(libFilePath);
		}
	}

	public static File getLibFile() {
		synchronized (TWordVoice.libFile) {
			return libFile;
		}
	}

	/**
	 * 生成音频库WordVoice.lib文件方法，需要音频文件以如下格式排列 root/ a/ a.mp3 a bus.mp3 ... b/
	 * but.mp3 ... ...
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings("unused")
	private static void gen() throws Exception {
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
				"D:\\Download\\dictvoice\\dict.bin", "rw");
		int binIdx = 0;
		// 写入字母索引
		System.out.println("写入字母索引");
		for (int i = 0; i < 26; i++) {
			file.writeInt(letBegin[i]);
			file.writeInt(letEnd[i]);
			binIdx += 8;
		}
		// 写入单词索引
		System.out.println("写入单词索引");
		for (int i = 0; i < 26; i++) {
			Character c = (char) ('a' + i);
			List<WordP> letWordPs = index.get(c);
			for (WordP wordP : letWordPs) {
				byte[] word = Arrays.copyOf(wordP.getWord().getBytes("UTF-8"),
						60);
				file.write(word);
				file.writeInt(wordP.p);
				binIdx += 64;
			}
		}
		// 写入单词音频数据
		System.out.println("单词音频数据");
		for (int i = 0; i < 26; i++) {
			Character c = (char) ('a' + i);
			System.out.println(c);
			List<WordP> letWordPs = index.get(c);
			for (WordP wordP : letWordPs) {
				file.writeShort(wordP.len);
				file.write(wordP.getData());
				binIdx += 2 + wordP.len;
			}
		}
		file.close();
		System.out.println(binIdx == binSize);
	}

	private String word;
	private boolean isTotalMatch;
	private byte[] voiceData;
	private boolean isSplitMatch;
	private List<byte[]> splitVoiceData;

	/**
	 * 初始化时会立刻开始获取发音
	 * 
	 * @param word
	 *            需要获取发音的单词
	 * @throws IOException
	 *             异常说明音频库文件不存在或文件损坏
	 */
	public TWordVoice(String word) throws IOException {
		this.word = TStr.trim(TStr.toDBC(word)).toLowerCase();
		synchronized (libFile) {
			RandomAccessFile raf = new RandomAccessFile(libFile, "r");
			int[] indexPos = indexPos(raf, this.word);
			if (indexPos!=null) {
				voiceData = voiceData(raf, indexPos[0], indexPos[1], this.word);
			}
			if (voiceData != null) {
				isTotalMatch = true;
			} else {
				isTotalMatch = false;
				voiceData = null;
				this.splitVoiceData = new ArrayList<byte[]>();
				for (String splitWord : splitWord(this.word)) {
					byte[] splitVoiceData = null;
					indexPos = indexPos(raf, splitWord);
					if (indexPos!=null) {
						splitVoiceData = voiceData(raf, indexPos[0],
								indexPos[1], splitWord);
					}
					if (splitVoiceData == null) {
						isSplitMatch = false;
						splitVoiceData = null;
						this.splitVoiceData = null;
						raf.close();
						return;
					}
					this.splitVoiceData.add(splitVoiceData);
				}
				isSplitMatch = true;
			}
			raf.close();
			return;
		}
	}

	public void setWord(String word) {
		this.word = TStr.trim(TStr.toDBC(this.word)).toLowerCase();
	}

	public String getWord() {
		return word;
	}

	/**
	 * 是否完全匹配
	 * 
	 * @return 是否完全匹配
	 */
	public boolean isTotalMatch() {
		return isTotalMatch;
	}

	/**
	 * 是否分裂匹配
	 * 
	 * @return 是否分裂匹配
	 */
	public boolean isSplitMatch() {
		return isSplitMatch;
	}

	/**
	 * 完全匹配音频数据
	 * 
	 * @return 完全匹配音频数据
	 */
	public byte[] getVoiceData() {
		return voiceData;
	}

	/**
	 * 分裂匹配音频数据
	 * 
	 * @return 分裂匹配音频数据
	 */
	public List<byte[]> getSplitVoiceData() {
		return splitVoiceData;
	}

	private int[] indexPos(RandomAccessFile raf, String word)
			throws IOException {
		int indexPos[] = new int[2];
		int pos = (word.charAt(0) - 'a') * 8;
		if (pos < 0)
			return null;
		raf.seek(pos);
		indexPos[0] = raf.readInt();
		indexPos[1] = raf.readInt();
		return indexPos;
	}

	private List<String> splitWord(String word) {
		char[] cS = new char[60];
		int idx = 0;
		List<String> rst = new ArrayList<String>();
		for (int i = 0; i < word.length(); i++) {
			TChar charType = TChar.checkType(word.charAt(i));
			if (TChar.LETTER == charType || TChar.NUM == charType) {
				cS[idx++] = word.charAt(i);
			} else {
				if (idx != 0) {
					rst.add(new String(cS, 0, idx));
					idx = 0;
				}
			}
		}
		if (idx != 0)
			rst.add(new String(cS, 0, idx));
		return rst;
	}

	private byte[] voiceData(RandomAccessFile raf, int indexBeginPos,
			int indexEndPos, String word) throws IOException {
		int middle = indexBeginPos
				+ (((indexEndPos - indexBeginPos) / 64) >> 1) * 64;
		String middleWord = indexWord(raf, middle);
		if (middleWord.compareTo(word) == 0) {
			return voiceData(raf, middle);
		} else if (middleWord.compareTo(word) < 0) {
			if (indexEndPos - middle == 64) {
				if (indexWord(raf, indexEndPos).compareTo(word) == 0) {
					return voiceData(raf, indexEndPos);
				} else {
					return null;
				}
			}
			return voiceData(raf, middle, indexEndPos, word);
		} else {
			return voiceData(raf, indexBeginPos, middle, word);
		}
	}

	private String indexWord(RandomAccessFile raf, int pos) throws IOException {
		raf.seek(pos);
		byte[] wordData = new byte[60];
		raf.read(wordData);
		for (int realLen = wordData.length - 1; realLen >= 0; realLen--)
			if (wordData[realLen] != 0) {
				wordData = Arrays.copyOf(wordData, realLen + 1);
				break;
			}
		return new String(wordData, "UTF-8");
	}

	private byte[] voiceData(RandomAccessFile raf, int wordIndexPos)
			throws IOException {
		raf.seek(wordIndexPos + 60);
		int dataPos = raf.readInt();
		raf.seek(dataPos);
		int dataLen = raf.readShort();
		byte[] data = new byte[dataLen];
		raf.read(data);
		return data;
	}

	private static class WordP {
		private String word;
		private short len;
		private int p;
		private File file;

		public String getWord() {
			return word;
		}

		public void setWord(String word) {
			if (!word.endsWith(".mp3")) {
				System.out.println();
			}
			word = word.substring(0, word.length() - 4);
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

	private static class ComparWord implements Comparator<WordP> {
		public int compare(WordP o1, WordP o2) {
			return o1.getWord().compareTo(o2.getWord());
		}
	}
}