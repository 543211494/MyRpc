package org.example.provider.service;

import org.example.provider.anno.RpcService;
import org.example.provider.api.Translator;

import java.util.HashMap;
import java.util.Map;

@RpcService
public class TranslatorImpl implements Translator {

    private Map<String,String> dictionary;

    public TranslatorImpl() {
        dictionary = new HashMap<>();
        dictionary.put("food","食物");
        dictionary.put("English","英语");
    }

    @Override
    public String translate(String str){
        String result = this.dictionary.get(str);
        if(result==null){
            result = "This word does not exist";
        }
        System.out.println("调用该方法翻译单词"+str+",翻译结果为："+result);
        return result;
    }

}
