package com.qcloud.sign;

import java.util.Random;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.HmacUtils;

public class FileCloudSign {

	/**
	    app_sign    时效性签名
	    @param  appId       Qcloud上申请的业务IDhttp://app.qcloud.com
	    @param  secret_id   Qcloud上申请的密钥id
	    @param  secret_key  Qcloud上申请的密钥key
	    @param  expired     签名过期时间
	    @return  String     生成的签名
    */
	public static String appSign(int appId, String secret_id, String secret_key,
                    long expired) {
		return appSignBase(appId, secret_id, secret_key, "", expired, "");
	}
        
        public static String appSignV2(int appId, String secret_id, String secret_key,
                    String bucket, 
                    long expired) {
		return appSignBase(appId, secret_id, secret_key, bucket, expired, "");
	}

	 /**
	    app_sign_once   绑定资源的签名,只有这个资源可以使用
	    @param  appId       Qcloud上申请的业务IDhttp://app.qcloud.com
	    @param  secret_id   Qcloud上申请的密钥id
	    @param  secret_key  Qcloud上申请的密钥key
	    @param  fileid      签名资源的唯一标示    
            @return  String     生成的签名
    */    
        public static String appSignOnce(int appId, String secret_id, String secret_key,
    		String fileid){
            return appSignBase(appId, secret_id, secret_key, "", 0, fileid);
        }
        
        public static String appSignOnceV2(int appId, String secret_id, String secret_key,
                String bucket, 
    		String fileid){
            return appSignBase(appId, secret_id, secret_key, bucket, 0, fileid);
        }
    
        private static String appSignBase(int appId, String secret_id,String secret_key, 
                String bucket,
                long expired, String fileid) {
            if (empty(secret_id) || empty(secret_key)){
                return null;
            }
    	
            long now = System.currentTimeMillis() / 1000;  
            int rdm = Math.abs(new Random().nextInt());
            String plain_text = "";
            if(empty(bucket)){
                plain_text = String.format("a=%d&k=%s&e=%d&t=%d&r=%d&u=%s&f=%s",
                        appId, secret_id, expired, now, rdm, 0, fileid);
            }else{
                plain_text = String.format("a=%d&b=%s&k=%s&e=%d&t=%d&r=%d&u=%s&f=%s",
                        appId, bucket, secret_id, expired, now, rdm, 0, fileid);
            }

            //System.out.println("plain_text="+plain_text);
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
