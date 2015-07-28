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
	    @param  mySign      生成的签名
            @return 0表示成功
    */
	public static int appSign(String appId, String secret_id, String secret_key,
			long expired, StringBuffer mySign) {
		return appSignBase(appId, secret_id, secret_key, "", expired, "", mySign);
	}
        
        public static int appSignV2(String appId, String secret_id, String secret_key,
                        String bucket, 
			long expired, StringBuffer mySign) {
		return appSignBase(appId, secret_id, secret_key, bucket, expired, "", mySign);
	}

	 /**
	    app_sign_once   绑定资源的签名,只有这个资源可以使用
	    @param  appId       Qcloud上申请的业务IDhttp://app.qcloud.com
	    @param  secret_id   Qcloud上申请的密钥id
	    @param  secret_key  Qcloud上申请的密钥key
	    @param  fileid      签名资源的唯一标示    
	    @param  mySign        生成的签名
            @return 0表示成功
    */    
        public static int appSignOnce(String appId, String secret_id, String secret_key,
    		String fileid, StringBuffer mySign){
            return appSignBase(appId, secret_id, secret_key, "", 0, fileid, mySign);
        }
        
        public static int appSignOnceV2(String appId, String secret_id, String secret_key,
                String bucket, 
    		String fileid, StringBuffer mySign){
            return appSignBase(appId, secret_id, secret_key, bucket, 0, fileid, mySign);
        }
    
        private static int appSignBase(String appId, String secret_id,String secret_key, 
                String bucket,
                long expired, String fileid,
		StringBuffer mySign) {
            if (empty(secret_id) || empty(secret_key)){
                return -1;
            }
    	
            long now = System.currentTimeMillis() / 1000;  
            int rdm = Math.abs(new Random().nextInt());
            String plain_text = "";
            if(empty(bucket)){
                plain_text = String.format("a=%s&k=%s&e=%d&t=%d&r=%d&u=%s&f=%s",
                        appId, secret_id, expired, now, rdm, 0, fileid);
            }else{
                plain_text = String.format("a=%s&b=%s&k=%s&e=%d&t=%d&r=%d&u=%s&f=%s",
                        appId, bucket, secret_id, expired, now, rdm, 0, fileid);
            }

            //System.out.println("plain_text="+plain_text);
            byte[] bin = HmacUtils.hmacSha1(secret_key, plain_text);

            byte[] all = new byte[bin.length + plain_text.getBytes().length];
            System.arraycopy(bin, 0, all, 0, bin.length);
            System.arraycopy(plain_text.getBytes(), 0, all, bin.length, plain_text.getBytes().length);
        
            all = Base64.encodeBase64(all);
            mySign.append(new String(all));
            System.out.println("sign="+mySign.toString());
            return 0;
	}
    
	public static boolean empty(String s){
		return s == null || s.trim().equals("") || s.trim().equals("null");
	}
		
}
