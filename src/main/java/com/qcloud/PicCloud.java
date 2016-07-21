/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.qcloud;

import java.io.*;
import java.util.*;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;
import org.json.JSONException;
import org.json.JSONArray;

import com.qcloud.sign.*;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.codec.digest.DigestUtils;

/**
 *
 * @author jusisli
 */
public class PicCloud {
        protected static String VERSION = "2.1.4";
	protected static String QCLOUD_DOMAIN = "image.myqcloud.com";
        protected static String PROCESS_DOMAIN = "service.image.myqcloud.com";

	protected int mAppId;
	protected String mSecretId;
	protected String mSecretKey;
        protected String mBucket;

	protected int mErrno;
	protected String mError;
  
        protected String mMagicContect;
        
        protected CloudClient   mClient;
        

	/**
	 * PicCloud 构造方法
	 * @param appId				授权appId
	 * @param secretId			授权secret_id
	 * @param secretKey                   授权secret_key
	 */
	public PicCloud(int appId, String secretId, String secretKey) {
            mAppId = appId;
            mSecretId = secretId;
            mSecretKey = secretKey;
            mErrno = 0;
            mBucket = "";
            mError = "";
            mClient = new CloudClient();
	}
        
        	/**
	 * PicCloud 构造方法
	 * @param appId				授权appId
	 * @param secret_id			授权secret_id
	 * @param secret_key                   授权secret_key
         * @param bucket `                     空间名 
	 */
	public PicCloud(int appId, String secret_id, String secret_key, String bucket) {
            mAppId = appId;
            mSecretId = secret_id;
            mSecretKey = secret_key;
            mBucket = bucket;
            mErrno = 0;
            mError = "";
            mClient = new CloudClient();
	}
        
        public String getVersion(){
            return VERSION;
        }

	public int getErrno() {
            return mErrno;
	}

	public String getErrMsg() {
            return mError;
	}

	public int setError(int errno, String msg) {
            mErrno = errno;
            mError = msg;
            return errno;
	}

	public String getError() {
            return "errno=" + mErrno + " desc=" + mError;
	}
        
        public void setMagicContext(String context){
            mMagicContect = context;
        }
        
        public String getMagicContext(){
            return mMagicContect;
        }
        
        public String getUrl(String userid, String fileId){
            String url;    
            if ("".equals(mBucket)) {       
                url = String.format("http://web.%s/photos/v1/%d/%s", QCLOUD_DOMAIN, mAppId, userid);
            }else{
                url = String.format("http://web.%s/photos/v2/%d/%s/%s", QCLOUD_DOMAIN, mAppId, mBucket, userid);
            }
            if ("".equals(fileId) == false) {
                String params = fileId;
                try {
                    params = java.net.URLEncoder.encode(fileId, "ISO-8859-1");
                } catch (UnsupportedEncodingException ex) {
                    System.out.printf("url encode failed, fileId=%s", fileId);
                }
                url += "/"+params;
            }
            return url;
        }
        
        public String getDownloadUrl(String userid, String fileId){
            String url;    
            if ("".equals(mBucket)) {
                url = String.format("http://%d.%s/%d/%s/%s/original", mAppId, QCLOUD_DOMAIN, mAppId, userid, fileId);
            }else{
                url = String.format("http://%s-%d.%s/%s-%d/%s/%s/original", mBucket, mAppId, QCLOUD_DOMAIN, mBucket, mAppId, userid, fileId);
            }
            return url;
        }
        
        public JSONObject getResponse(String rsp){
            if("".equals(rsp)){
                setError(-1, "empty rsp");
                return null;
            }
            JSONObject pack = new JSONObject(rsp);
            int code = pack.getInt("code");
            String msg = pack.getString("message");
            if (code != 0) {
                setError(code, msg);
                return null;
            }   
            return pack;
        }

        /**
	 * Upload 上传图片
	 * @param fileName		上传的文件名
	 * @return			返回结果
	 */
        public UploadResult upload(String fileName) {
            return upload(fileName, "", new PicAnalyze());
        }
        
	public UploadResult upload(String fileName, String fileId) {
            return upload(fileName, fileId, new PicAnalyze());
        }
        
	public UploadResult upload(String fileName, String fileId, PicAnalyze flag) {
            if ("".equals(fileName)) {
                setError(-1, "invalid file name");
                return null;
            }
            
            FileInputStream fileStream = null;
            try {
                fileStream = new FileInputStream(fileName);
            } catch (FileNotFoundException ex) {
                setError(-1, "invalid file name");
                return null;
            }
            UploadResult result = upload(fileStream, fileId, flag);
            try {
                fileStream.close();
            } catch (IOException ex) {
                setError(-1, "close filestream error");
                return null;
            }
            return result;
	}

        public UploadResult upload(InputStream inputStream) {
            return upload(inputStream, "", new PicAnalyze());
        }
        
	public UploadResult upload(InputStream inputStream, String fileId) {
            return upload(inputStream, fileId, new PicAnalyze());
        }
        
        public UploadResult upload(InputStream inputStream, String fileId, PicAnalyze flag) {
            String url = getUrl("0", fileId);
            //check analyze flag
            String queryString = "";
            if(flag.fuzzy != 0){
                queryString += ".fuzzy";
            }
            if(flag.food != 0){
                queryString += ".food";
            }
            if ("".equals(queryString) == false) {
                url += "?analyze="+queryString.substring(1);
            }

            // create sign
            long expired = System.currentTimeMillis() / 1000 + 2592000;
            String sign = getSign(expired);
            if (null == sign) {
                    setError(-1, "create app sign failed");
                    return null;
            }

            HashMap<String, String> header = new HashMap<String, String>();
            header.put("Authorization", sign);
            header.put("Host", "web.image.myqcloud.com");
            HashMap<String, Object> body = new HashMap<String, Object>();
            
            JSONObject rspData = null;
            try {
                byte[] data = new byte[inputStream.available()];
                inputStream.read(data);
                String rsp = mClient.post(url, header, body, data);
                rspData = getResponse(rsp);
                if(null == rspData || false == rspData.has("data")){
                    setError(-1, "qcloud api response error, rsp="+rsp); 
                    return null;
                }
                rspData = rspData.getJSONObject("data");
            } catch (Exception e) {
                    setError(-1, "url exception, e=" + e.toString());
                    return null;
            }
            
            UploadResult info = new UploadResult();
            try {  
                    info.url = rspData.getString("url");
                    info.downloadUrl = rspData.getString("download_url");
                    info.fileId = rspData.getString("fileid");

                    if(rspData.has("info") && rspData.getJSONArray("info").length() > 0 ){
                        info.width = rspData.getJSONArray("info").getJSONObject(0).getJSONObject("0").getInt("width");
                        info.height = rspData.getJSONArray("info").getJSONObject(0).getJSONObject("0").getInt("height"); 
                    } 
                    
                    if(rspData.has("is_fuzzy")){
                        info.analyze.fuzzy = rspData.getInt("is_fuzzy");
                    }
                    if(rspData.has("is_food")){
                        info.analyze.food = rspData.getInt("is_food");
                    }
            } catch (JSONException e) {
                    setError(-1, "json exception, e=" + e.toString());
                    return null;
            }
            
            setError(0, "success");
            return info;
	}
        
        /**
         * 分片上传接口族
         * @param fileName		上传的文件名
	 * @return			返回结果
         */
        public SliceUploadInfo simpleUploadSlice(String fileName){
             return simpleUploadSlice(fileName, "", 0);
        }
        
        public SliceUploadInfo simpleUploadSlice(String fileName, int sliceSize){
             return simpleUploadSlice(fileName, "", sliceSize);
        }
        
        public SliceUploadInfo simpleUploadSlice(String fileName, String fileId, int sliceSize){
            int fileSize = 0;
            FileInputStream fs;
            byte[] data = null;
            try {
                fs = new FileInputStream(fileName);
                fileSize = fs.available();
                data = new byte[fileSize];
                fs.read(data);
                fs.close();
            } catch (Exception e) {
                setError(-1, "read file failed");
                return null;
            }
              
            
            SliceUploadInfo info = initUploadSlice(fileId, data, fileSize, sliceSize);
            if(null == info){
                return info;
            }
            
            while(false == info.finishFlag){
                SliceUploadInfo newInfo = UploadSlice(data, info);
                if(newInfo == null){
                    setError(-1, "slice upload failed, need retry");
                    return null;
                }
                info = newInfo;
            }
            
            setError(0, "success");
            return info;
        }
        
        public SliceUploadInfo simpleUploadSlice(String fileName, SliceUploadInfo lastInfo){
            FileInputStream fs;
            byte[] data = null;
            try {
                fs = new FileInputStream(fileName);
                data = new byte[fs.available()];
                fs.read(data);
                fs.close();
            } catch (Exception e) {
                setError(-1, "read file failed");
                return null;
            }
            
            SliceUploadInfo info = initUploadSlice(lastInfo.fileId, data, lastInfo.fileSize, lastInfo.sliceSize, lastInfo.session);
            if(null == info){
                return info;
            }
            
            int maxRetry = 3;
            while(false == info.finishFlag){
                int retry = 0;
                while(retry < maxRetry){
                    retry++;
                    SliceUploadInfo newInfo = UploadSlice(data, info);
                    if(newInfo != null){
                        info = newInfo;
                        break;
                    }
                }
                if(retry >= maxRetry){
                    setError(-1, "slice upload failed, need retry");
                    return null;
                }
            }
            
            setError(0, "success");
            return info;
        }
        
        public SliceUploadInfo initUploadSlice(String fileId, byte[] data, int fileSize, int sliceSize) {  
            return initUploadSlice(fileId, data, fileSize, sliceSize, "");
        }
        
        public SliceUploadInfo initUploadSlice(String fileId, byte[] data, int fileSize, int sliceSize, String session) {  
            SliceUploadInfo info = new SliceUploadInfo();
            //获得文件大小和sha
            String sha = DigestUtils.sha1Hex(data);
            // create sign
            long expired = System.currentTimeMillis() / 1000 + 2592000;
            String sign = getSign(expired);
            if (null == sign) {
                setError(-1, "create app sign failed");
                return null;
            }

            HashMap<String, String> header = new HashMap<String, String>();
            header.put("Authorization", sign);
            header.put("Host", "web.image.myqcloud.com");
            HashMap<String, Object> body = new HashMap<String, Object>();
            body.put("op", "upload_slice");
            body.put("sha", sha);
            body.put("filesize", fileSize);
            if(sliceSize > 0){
                body.put("slice_size", sliceSize);
            }
            if("".equals(session)){
                body.put("session", session);
            }
            
            String url = getUrl("0", fileId);
            JSONObject rspData = null;
            try {
                String rsp = mClient.post(url, header, body, null);
                rspData = getResponse(rsp);
                if(null == rspData || false == rspData.has("data")){
                    setError(-1, "qcloud api response error, rsp="+rsp); 
                    return null;
                }
                rspData = rspData.getJSONObject("data");
            } catch (Exception e) {
                    setError(-1, "url exception, e=" + e.toString());
                    return null;
            }

            try {
                if(rspData.has("url")){
                    //命中秒传
                    info.finishFlag = true;
                    info.url = rspData.getString("url");
                    info.downloadUrl = rspData.getString("download_url");
                    info.fileId = rspData.getString("fileid");
                    if(rspData.has("info") && rspData.getJSONArray("info").length() > 0 ){
                        info.width = rspData.getJSONArray("info").getJSONObject(0).getJSONObject("0").getInt("width");
                        info.height = rspData.getJSONArray("info").getJSONObject(0).getJSONObject("0").getInt("height"); 
                    } 
                }else if(rspData.has("session")){
                    info.finishFlag = false;
                    info.reqUrl = url;
                    info.sign = sign;
                    info.session = rspData.getString("session");
                    info.fileSize = fileSize;
                    info.offset = rspData.getInt("offset");
                    info.sliceSize = rspData.getInt("slice_size");
                }else{
                    setError(-1, "qcloud api response data error"); 
                    return null;
                }
            } catch (JSONException e) {
                    setError(-1, "json exception, e=" + e.toString());
                    return null;
            }
            return info;
        }
        
        public SliceUploadInfo UploadSlice(byte[] data, SliceUploadInfo info) {  
            if(info.finishFlag){
                setError(0, "success");
                return info;
            }
                        
            HashMap<String, String> header = new HashMap<String, String>();
            header.put("Authorization", info.sign);
            header.put("Host", "web.image.myqcloud.com");
            HashMap<String, Object> body = new HashMap<String, Object>();
            body.put("op", "upload_slice");
            body.put("session", info.session);
            body.put("offset", info.offset);
            
            JSONObject rspData = null;
            try {
                int from = info.offset;
                int to = info.offset+info.sliceSize;
                to = to > info.fileSize ? info.fileSize : to;
                byte[] sliceData = Arrays.copyOfRange(data, from, to);
                String rsp = mClient.post(info.reqUrl, header, body, sliceData);
                rspData = getResponse(rsp);
                if(null == rspData || false == rspData.has("data")){
                    setError(-1, "qcloud api response error, rsp="+rsp); 
                    return null;
                }
                rspData = rspData.getJSONObject("data");
            } catch (Exception e) {
                    setError(-1, "url exception, e=" + e.toString());
                    return null;
            }
            
            SliceUploadInfo newInfo = new SliceUploadInfo();
            try {
                if(rspData.has("url")){
                    //上传完成
                    newInfo.finishFlag = true;
                    newInfo.url = rspData.getString("url");
                    newInfo.downloadUrl = rspData.getString("download_url");
                    newInfo.fileId = rspData.getString("fileid");
                    if(rspData.has("info") && rspData.getJSONArray("info").length() > 0 ){
                        newInfo.width = rspData.getJSONArray("info").getJSONObject(0).getJSONObject("0").getInt("width");
                        newInfo.height = rspData.getJSONArray("info").getJSONObject(0).getJSONObject("0").getInt("height"); 
                    }
                }else if(rspData.has("session")){
                    newInfo.finishFlag = false;
                    newInfo.reqUrl = info.reqUrl;
                    newInfo.sign = info.sign;
                    newInfo.session = rspData.getString("session");
                    newInfo.offset = rspData.getInt("offset") + info.sliceSize;
                    newInfo.sliceSize = info.sliceSize;
                    newInfo.fileSize = info.fileSize;
                    //newInfo.sliceSize = rspData.getInt("slice_size");
                }else{
                    setError(-1, "qcloud api response data error"); 
                    return null;
                }
            } catch (JSONException e) {
                    setError(-1, "json exception, e=" + e.toString());
                    return null;
            }
            
            setError(0, "success");
            return newInfo;
        }
        
	/**
	 * Delete 删除图片
	 * @param fileId		图片的唯一标识
	 * @return 				错误码，0为成功
	 */
	public int delete(String fileId) {
            // create sign once
            String sign = getSignOnce(fileId);
            if (null == sign) {
                return setError(-1, "create app sign failed");
            }

            HashMap<String, String> header = new HashMap<String, String>();
            header.put("Authorization", sign);
            header.put("Host", "web.image.myqcloud.com");

            String url = getUrl("0", fileId) + "/del";
            try {
                String rsp = mClient.post(url, header, null, null);
                JSONObject rspData = getResponse(rsp);
                if(null == rspData){
                    return setError(-1, "qcloud api response packet error, rsp="+rsp); 
                }
            } catch (Exception e) {
                    return setError(-1, "url exception, e=" + e.toString());
            }

            return setError(0, "success");
	}

	/**
	 * Stat 查询图片信息
	 * @param fileId	图片fileid
	 * @return             返回结果		
	 */
	public PicInfo stat(String fileId) {                   
            HashMap<String, String> header = new HashMap<String, String>();
            header.put("Host", "web.image.myqcloud.com");
            
            String url = getUrl("0", fileId);
            JSONObject rspData = null;
            try {
                String rsp = mClient.get(url, header, null);
                rspData = getResponse(rsp);
                if(null == rspData || false == rspData.has("data")){
                    setError(-1, "qcloud api response error, rsp="+rsp); 
                    return null;
                }
                rspData = rspData.getJSONObject("data");
            } catch (Exception e) {
                    setError(-1, "url exception, e=" + e.toString());
                    return null;
            }

            PicInfo info = new PicInfo();
            try {
                info.url = rspData.getString("file_url");
                info.fileId = rspData.getString("file_fileid");
                info.uploadTime = rspData.getInt("file_upload_time");
                info.size = rspData.getInt("file_size");
                info.md5 = rspData.getString("file_md5");
                info.width = rspData.getInt("photo_width");
                info.height = rspData.getInt("photo_height");
            } catch (JSONException e) {
                setError(-1, "json exception, e=" + e.toString());
                return null;
            }

            setError(0, "success");
            return info;
	}

	/**
	 * Copy 复制图片
	 * @param fileId	 图片的唯一标识
	 * @return			返回结果
	 */
	public UploadResult copy(String fileId) {
            // create sign once
            String sign = getSignOnce(fileId);
            if (null == sign) {
                setError(-1, "create app sign failed");
                return null;
            }
                
            HashMap<String, String> header = new HashMap<String, String>();
            header.put("Authorization", sign);
            header.put("Host", "web.image.myqcloud.com");

            String url = getUrl("0", fileId) + "/copy";
            JSONObject rspData = null;
            try {
                String rsp = mClient.post(url, header, null, null);
                rspData = getResponse(rsp);
                if(null == rspData || false == rspData.has("data")){
                    setError(-1, "qcloud api response error, rsp="+rsp); 
                    return null;
                }
                rspData = rspData.getJSONObject("data");
            } catch (Exception e) {
                    setError(-1, "url exception, e=" + e.toString());
                    return null;
            }

            UploadResult info = new UploadResult();
            try {
                info.url = rspData.getString("url");
                info.downloadUrl = rspData.getString("download_url");
                info.fileId = info.url.substring(info.url.lastIndexOf('/') + 1);

                if(rspData.has("info") && rspData.getJSONArray("info").length() > 0 ){
                    info.width = rspData.getJSONArray("info").getJSONObject(0).getJSONObject("0").getInt("width");
                    info.height = rspData.getJSONArray("info").getJSONObject(0).getJSONObject("0").getInt("height"); 
                } 

            } catch (JSONException e) {
                    setError(-1, "json exception, e=" + e.toString());
                    return null;
            }

            setError(0, "success");
            return info;
	}

	/**
	 * Download 下载图片
	 * @param url           图片的唯一标识
	 * @param fileName	 下载图片的保存路径
	 * @return 错误码，0为成功
	 */
	public int download(String url, String fileName) {
            JSONObject rspData = null;
            try {
                String rsp = mClient.get(url, null, null);
                File file = new File(fileName);
                DataOutputStream ops = new DataOutputStream(new FileOutputStream(
                                file));
                ops.writeBytes(rsp);
                ops.close();  
            } catch (Exception e) {
                    return setError(-1, "url exception, e=" + e.toString());
            }
            
            return setError(0, "success");
	}
        
        public String getSign(long expired) {
            return FileCloudSign.appSignV2(mAppId, mSecretId, mSecretKey, mBucket, expired);
        }
            
        public String getSignOnce(String fileId) {
            return FileCloudSign.appSignOnceV2(mAppId, mSecretId, mSecretKey, mBucket, fileId);
        }  
        
        public String getProcessSign(long expired) {
            return PicProcessSign.sign(mAppId, mSecretId, mSecretKey, mBucket, expired);
        }
        
        public PornDetectInfoData pornDetect(String url) {
            // create sign
            long expired = System.currentTimeMillis() / 1000 + 3600*24;
            String sign = getProcessSign(expired);
            if (null == sign) {
                    setError(-1, "create app sign failed");
                    return null;
            }
            
            HashMap<String, String> header = new HashMap<String, String>();
            header.put("Authorization", sign);
            header.put("Host", PROCESS_DOMAIN);
            header.put("Content-Type", "application/json");
            //create body
            JSONObject reqData = new JSONObject();
            reqData.put("appid", mAppId);
            reqData.put("bucket", mBucket);
            reqData.put("url", url);
            
            String reqUrl = "http://"+PROCESS_DOMAIN + "/detection/pornDetect";
            JSONObject rspData = null;
            try {
                String rsp = mClient.post(reqUrl, header, null, reqData.toString().getBytes());
                rspData = getResponse(rsp);
                if(null == rspData || false == rspData.has("data")){
                    //setError(-1, "qcloud api response error"); 
                    return null;
                }
                rspData = rspData.getJSONObject("data");
            } catch (Exception e) {
                    setError(-1, "url exception, e=" + e.toString());
                    return null;
            }

            PornDetectInfoData info = new PornDetectInfoData();
            try {
                info.result = rspData.getInt("result");
                info.confidence = rspData.getDouble("confidence");
                info.pornScore = rspData.getDouble("porn_score");
                info.normalScore = rspData.getDouble("normal_score");
                info.hotScore = rspData.getDouble("hot_score");
                info.forbidStatus = rspData.getInt("forbid_status");
            } catch (JSONException e) {
                    setError(-1, "json exception, e=" + e.toString());
                    return null;
            }
            setError(0, "success");  
            return info;
        }
        
        public ArrayList<PornDetectInfo> pornDetectUrl(String[] pornUrl) {
            // create sign
            long expired = System.currentTimeMillis() / 1000 + 3600*24;
            String sign = getProcessSign(expired);
            if (null == sign) {
                    setError(-1, "create app sign failed");
                    return null;
            }
            
            HashMap<String, String> header = new HashMap<String, String>();
            header.put("Authorization", sign);
            header.put("Host", PROCESS_DOMAIN);
            header.put("Content-Type", "application/json");
            //create body
            JSONObject reqData = new JSONObject();
            reqData.put("appid", mAppId);
            reqData.put("bucket", mBucket);
            reqData.put("url_list", pornUrl);
            
            String reqUrl = "http://"+PROCESS_DOMAIN + "/detection/pornDetect";
            JSONObject rspData;
            try {
                String rsp = mClient.post(reqUrl, header, null, reqData.toString().getBytes());
                //rspData = getResponse(rsp);
                if("".equals(rsp)){
                    setError(-1, "empty rsp");
                    return null;
                }
                rspData = new JSONObject(rsp);
            } catch (Exception e) {
                    setError(-1, "url exception, e=" + e.toString());
                    return null;
            }
            if(!rspData.has("result_list")){
                System.out.println("code = " + rspData.getInt("code"));
		System.out.println("message = " + rspData.getString("message"));
		System.out.println("data = " + rspData.getString("data"));
                return null;
            }
            
            ArrayList<PornDetectInfo> info = new ArrayList<PornDetectInfo>();
            try {            
                JSONArray rl;
                rl = rspData.getJSONArray("result_list");
                
                for(int i=0;i<rl.length();i++){
                    PornDetectInfo tmpinfo = new PornDetectInfo();
                    JSONObject res=rl.getJSONObject(i);
                    tmpinfo.code = res.getInt("code");
                    tmpinfo.message = res.getString("message");
                    tmpinfo.name = res.getString("url"); 
                    if(res.has("data")){
                        JSONObject resData = res.getJSONObject("data");
                        tmpinfo.data.result = resData.getInt("result");
                        tmpinfo.data.confidence = resData.getDouble("confidence");
                        tmpinfo.data.pornScore = resData.getDouble("porn_score");
                        tmpinfo.data.normalScore = resData.getDouble("normal_score");
                        tmpinfo.data.hotScore = resData.getDouble("hot_score");
                        tmpinfo.data.forbidStatus = resData.getInt("forbid_status");
                    }
                    info.add(tmpinfo);
                }
            } catch (JSONException e) {
                setError(-2, "json exception, e=" + e.toString());
                return null;
            }
            setError(0, "success");  
            return info;
        }

        public ArrayList<PornDetectInfo> pornDetectFile(String[] pornFile) {
            // create sign
            long expired = System.currentTimeMillis() / 1000 + 3600*24;
            String sign = getProcessSign(expired);
            if (null == sign) {
                    setError(-1, "create app sign failed");
                    return null;
            }
            
            HashMap<String, String> header = new HashMap<String, String>();
            header.put("Authorization", sign);
            header.put("Host", PROCESS_DOMAIN);
            HashMap<String, Object> body = new HashMap<String, Object>();
            body.put("appid", mAppId);
            body.put("bucket", mBucket);
            
            byte[][] data = new byte[pornFile.length][];
            for(int i=0;i<pornFile.length ;i++){
                String fileName = pornFile[i];
                if ("".equals(fileName)) {
                    setError(-1, "invalid file name");
                    return null;
                }
                FileInputStream fileStream = null;
                try {
                    fileStream = new FileInputStream(fileName);
                } catch (FileNotFoundException ex) {
                    setError(-1, fileName + " not found");
                    return null;
                }
                try {
                    data[i] = new byte[fileStream.available()];
                    fileStream.read(data[i]);
                    fileStream.close();
                } catch (Exception ex) {
                    setError(-1, "io exception, e=" + ex.toString());
                    return null;
                }
            }
            String reqUrl = "http://"+PROCESS_DOMAIN + "/detection/pornDetect";
            JSONObject rspData;
            try {
                String rsp = mClient.postfiles(reqUrl, header, body, data, pornFile);
                //rspData = getResponse(rsp);
                if("".equals(rsp)){
                    setError(-1, "empty rsp");
                    return null;
                }
                rspData = new JSONObject(rsp);
            } catch (Exception e) {
                    setError(-1, "url exception, e=" + e.toString());
                    return null;
            }
            if(!rspData.has("result_list")){
                System.out.println("code = " + rspData.getInt("code"));
		System.out.println("message = " + rspData.getString("message"));
                if(rspData.has("data")){
                    System.out.println("data = " + rspData.getString("data"));
                }
                return null;
            }
            
            ArrayList<PornDetectInfo> info = new ArrayList<PornDetectInfo>();
            try {            
                JSONArray rl;
                rl = rspData.getJSONArray("result_list");
                
                for(int i=0;i<rl.length();i++){
                    PornDetectInfo tmpinfo = new PornDetectInfo();
                    JSONObject res=rl.getJSONObject(i);
                    tmpinfo.code = res.getInt("code");
                    tmpinfo.message = res.getString("message");
                    tmpinfo.name = res.getString("filename"); 
                    if(res.has("data")){
                        JSONObject resData = res.getJSONObject("data");
                        tmpinfo.data.result = resData.getInt("result");
                        tmpinfo.data.confidence = resData.getDouble("confidence");
                        tmpinfo.data.pornScore = resData.getDouble("porn_score");
                        tmpinfo.data.normalScore = resData.getDouble("normal_score");
                        tmpinfo.data.hotScore = resData.getDouble("hot_score");
                        tmpinfo.data.forbidStatus = resData.getInt("forbid_status");
                    }
                    info.add(tmpinfo);
                }
            } catch (JSONException e) {
                setError(-2, "json exception, e=" + e.toString());
                return null;
            }
            setError(0, "success");  
            return info;
        }

};
