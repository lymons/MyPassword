package cn.xing.mypassword.utils;

import java.util.Random;

public class StringRandom {
    private static StringRandom instance;
    private char[] chars;  
    private Random random = new Random();  
    private StringRandom(char[] chars) {  
        this.chars = chars;  
    }
    
    public static StringRandom getInstance() {
        if (instance == null) {
            instance = new StringRandom(new char[]{
                    '0','1','2','3','4','5','6','7','8','9',
                    'A','B','C','D','E','F','G','H','I','J',
                    'K','L','M','N','O','P','Q','R','S','T',
                    'U','V','W','X','Y','Z',
                    'a','b','c','d','e','f','g','h','i','j',
                    'k','l','m','n','o','p','q','r','s','t',
                    'u','v','w','x','y','z',
                    '$','#','@','!','&',':',';'
            });
        }
        return instance;
    }
      
    //参数为生成的字符串的长度，根据给定的char集合生成字符串  
    public String getString(int length){
          
        char[] data = new char[length];  
          
        for(int i = 0;i < length;i++){  
            int index = random.nextInt(chars.length);  
            data[i] = chars[index];  
        }  
        String s = new String(data);  
        return s;  
    } 
}
