package org.example.consumer.api;

public interface Translator {
    /**
     *
     * @param str 需要被翻译的单词
     * @return 英汉互译后的内容，如果词典中不包含此单词返回null
     */
    public String translate(String str);

}
