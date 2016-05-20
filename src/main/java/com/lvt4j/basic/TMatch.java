package com.lvt4j.basic;

public class TMatch {
    
    /**
     * 通讯录式快速匹配 
     * 如"licx","lcx","cx"都可与["li","chen","xi"]匹配
     * 
     * @param key
     * @param matches
     * @return
     */
    public static final boolean smartMatch(String key, String[] matches) {
        for (int i = 0; i < matches.length; i++) {
            if (isSmartMatch(key, 0, matches, i, 0)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 建立在smartMatch上的多重匹配，允许待匹配项有多个
     * 如"藤bia"可与 {{"藤","鞭"},{"藤","bian"},{"teng","鞭"},{"teng","bian"}}匹配
     * @param key
     * @param matches
     * @return
     */
    public static final boolean smartMultiMatch(String key, String[][] matches) {
        for (int i = 0; i < matches.length; i++) {
            if (smartMatch(key, matches[i])) return true;
        }
        return false;
    }
    /**
     * 建立在smartMultiMatch上的多重匹配，允许匹配项及待匹配项有多个
     * 如{"藤辫","藤bian","teng辫","tengbian"}可与 {{"藤","鞭"},{"藤","bian"},{"teng","鞭"},{"teng","bian"}}匹配
     * @param key
     * @param matches
     * @return
     */
    public static final boolean smartMMMatch(String[] key, String[][] matches) {
        for (int i = 0; i < key.length; i++) {
            for (int j = 0; j < matches.length; j++) {
                if (smartMatch(key[i], matches[j])) return true;
            }
        }
        return false;
    }
    
    private static final boolean isSmartMatch(String key, int keyIdx,
            String[] matches, int matchFIdx, int matchSIdx) {
        if (keyIdx >= key.length()) {
            return true;
        }
        if (key.charAt(keyIdx) == matches[matchFIdx].charAt(matchSIdx)) {
            keyIdx += 1;
            if (keyIdx >= key.length()) {
                return true;
            }
            int matchFIdxBak = matchFIdx;
            matchSIdx += 1;
            if (matchSIdx >= matches[matchFIdx].length()) {
                matchSIdx = 0;
                matchFIdx += 1;
                if (matchFIdx >= matches.length) {
                    return false;
                }
            }
            if (isSmartMatch(key, keyIdx, matches, matchFIdx, matchSIdx)) {
                return true;
            } else {
                matchFIdx = matchFIdxBak;
                matchSIdx = 0;
                matchFIdx += 1;
                if (matchFIdx >= matches.length) {
                    return false;
                }
                return isSmartMatch(key, keyIdx, matches, matchFIdx, matchSIdx);
            }
        } else {
            return false;
        }
    }
}
