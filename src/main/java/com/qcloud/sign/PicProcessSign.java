/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.qcloud.sign;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.HmacUtils;

/**
 *
 * @author jusisli
 */
public class PicProcessSign {
    public static String sign(int appId, String secret_id,String secret_key, 
                String bucket,
                long expired, String url) {
        //a=[appid]&b=[bucket]&k=[SecretID]&t=[currenTime]&e=[expiredTime]&l=[url link]
        if (empty(secret_id) || empty(secret_key)){
            return null;
        }
    	
        long now = System.currentTimeMillis() / 1000;  
        String plain_text = String.format("a=%d&b=%s&k=%s&t=%d&e=%d&l=%s",
            appId, bucket, secret_id, now, expired, java.net.URLEncoder.encode(url));
      
        byte[] bin = HmacUtils.hmacSha1(secret_key, plain_text);

        byte[] all = new byte[bin.length + plain_text.getBytes().length];
        System.arraycopy(bin, 0, all, 0, bin.length);
        System.arraycopy(plain_text.getBytes(), 0, all, bin.length, plain_text.getBytes().length);
        
        all = Base64.encodeBase64(all);
        return new String(all);
    }
            
    public static boolean empty(String s){
        return s == null || s.trim().equals("") || s.trim().equals("null");
    }
}
