package com.alistair.community.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    //将敏感词替换为三个*
    private static final String REPLACEMENT = "***";

    /**
     * 定义前缀树节点
     */
    private class TrieNode {
        //关键词结束的标识
        private  boolean isKeywordEnd = false;
        //字节点
        private Map<Character, TrieNode> subNodes = new HashMap<>();
        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }
        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }
        //添加子节点
        public void addSubNode(Character c, TrieNode node){
            subNodes.put(c, node);
        }
        //获取子节点
        public TrieNode getSubNode(Character c) {
            return subNodes.get(c);
        }
    }

    /**
     * 初始化前缀树
     */
    private TrieNode rootNode = new TrieNode();

    /**
     * 加上Post Construct注解，容器启动的时候初始化前缀树
     */
    @PostConstruct
    public void init(){
        try (
                InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
                //得到缓冲流
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))
        ){
            String keyWord;
            while ((keyWord = reader.readLine()) != null){
                //添加到前缀树
                this.addKeyWord(keyWord);
            }
        } catch (Exception e) {
            logger.error("加载敏感词文件失败" + e.getMessage());
        }
    }

    //将一个敏感词添加到前缀数
    private void addKeyWord(String keyWord) {
        TrieNode tempNode = rootNode;
        for(int i=0;i<keyWord.length();i++){
            char c = keyWord.charAt(i);
            TrieNode subNode = tempNode.getSubNode(c);
            if(subNode == null){
                //初始化子节点
                subNode = new TrieNode();
                tempNode.addSubNode(c,subNode);
            }
            //指向子节点，进入下一轮循环
            tempNode = subNode;

            //设置结束标识，用来标记关键词的结束
            if(i == keyWord.length()-1) {
                tempNode.setKeywordEnd(true);
            }
        }
    }



    /**
     * 识别并替换敏感词
     * @param text 待过滤的文本
     * @return 过滤后的文本
     */
    public String filter(String text) {
        if(StringUtils.isBlank(text)) {
            return null;
        }
        //指针1
        TrieNode tempNode = rootNode;
        //指针2
        int begin = 0;
        //指针3
        int position = 0;
        //存放结果
        StringBuilder sb = new StringBuilder();
        while (begin < text.length()) {
            char c;
            if (position < text.length()){
                c = text.charAt(position);
                //跳过符号
                if(isSymbol(c)) {
                    //若指针1处于根节点，将符号计入结果，让指针2向下走一步
                    if(tempNode == rootNode) {
                        sb.append(c);
                        begin++;
                    }
                    //无论符号在开头还是结尾，指针3都向下走一步
                    position++;
                    continue;
                }

                //检查下级结点
                tempNode = tempNode.getSubNode(c);
                if (tempNode == null) {
                    //以begin为开头的字符串，不是敏感词
                    sb.append(text.charAt(begin));
                    //进入下一个位置
                    position = ++begin;
                    // 指针1重新指向根节点
                    tempNode = this.rootNode;
                }else if (tempNode.isKeywordEnd) {
                    // 发现了敏感词，将begin到position的字符串替换成***
                    sb.append("***");
                    // 跳过敏感词
                    begin = ++position;
                    tempNode = this.rootNode;
                }else {
                    //继续检查下一个字符
                    position++;
                }
            }else {//position越界仍然没有找到敏感词
                //以begin为开头的字符串，不是敏感词
                sb.append(text.charAt(begin));
                //进入下一个位置
                position = ++begin;
                // 指针1重新指向根节点
                tempNode = this.rootNode;
            }
        }

        return sb.toString();
    }

    //判断是否为符号
    public boolean isSymbol(Character c) {
        //不是普通字符，且不是东亚字符
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c >0x9FFF);
    }


}
