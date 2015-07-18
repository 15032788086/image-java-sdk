package com.qcloud.sign;

import java.util.Random;

public class FileCloudSign {

	/**
	    app_sign    时效性签名
	    @param  appId       Qcloud上申请的业务IDhttp://app.qcloud.com
	    @param  secret_id   Qcloud上申请的密钥id
	    @param  secret_key  Qcloud上申请的密钥key
	    @param  expired     签名过期时间
	    @param  userid      业务账号系统,没有可以不填
	    @param  mySign      生成的签名
            @return 0表示成功
    */
	public static int appSign(String appId, String secret_id, String secret_key,
			long expired, String userid, StringBuffer mySign) {
		return appSignBase(appId, secret_id, secret_key, "", expired, userid, “”, mySign);
	}
        
        public static int appSignV2(String appId, String secret_id, String secret_key,
                        String bucket, 
			long expired, String userid, StringBuffer mySign) {
		return appSignBase(appId, secret_id, secret_key, bucket, expired, userid, “”, mySign);
	}

	 /**
	    app_sign_once   绑定资源的签名,只有这个资源可以使用
	    @param  appId       Qcloud上申请的业务IDhttp://app.qcloud.com
	    @param  secret_id   Qcloud上申请的密钥id
	    @param  secret_key  Qcloud上申请的密钥key
	    @param  userid      业务账号系统,没有可以不填
	    @param  fileid      签名资源的唯一标示    
	    @param  mySign        生成的签名
            @return 0表示成功
    */    
        public static int appSignOnce(String appId, String secret_id, String secret_key,
    		String userid, String fileid, StringBuffer mySign){
            return appSignBase(appId, secret_id, secret_key, "", 0, userid, fileid, mySign);
        }
        
        public static int appSignOnceV2(String appId, String secret_id, String secret_key,
                String bucket, 
    		String userid, String fileid, StringBuffer mySign){
            return appSignBase(appId, secret_id, secret_key, bucket, 0, userid, fileid, mySign);
        }
    
        private static int appSignBase(String appId, String secret_id,String secret_key, 
                String bucket,
                long expired, String userid, String fileid,
		StringBuffer mySign) {
            if (empty(secret_id) || empty(secret_key)){
                return -1;
            }
    	
            long now = System.currentTimeMillis() / 1000;    
            int rdm = Math.abs(new Random().nextInt());
            String plain_text = "";
            if(empty(bucket)){
                plain_text = String.format("a=%s&k=%s&e=%d&t=%d&r=%d&u=%s&f=%s",
                        appId, secret_id, expired, now, rdm, userid, fileid);
            }else{
                plain_text = String.format("a=%s&b=%s&k=%s&e=%d&t=%d&r=%d&u=%s&f=%s",
                        appId, bucket, secret_id, expired, now, rdm, userid, fileid);
            }

            byte[] bin = hashHmac(plain_text, secret_key);

            byte[] all = new byte[bin.length + plain_text.getBytes().length];
            System.arraycopy(bin, 0, all, 0, bin.length);
            System.arraycopy(plain_text.getBytes(), 0, all, bin.length, plain_text.getBytes().length);
        
            mySign.append(Base64Util.encode(all));
            return 0;
	}

	private static byte[] hashHmac(String plain_text, String accessKey) {
		
		try {
			return HMACSHA1.getSignature(plain_text, accessKey);
		} catch (Exception e) {
			return null;
		}
	}
    
	public static boolean empty(String s){
		return s == null || s.trim().equals("") || s.trim().equals("null");
	}
		
}
