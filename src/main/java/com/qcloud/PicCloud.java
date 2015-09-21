/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.qcloud;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;
import org.json.JSONException;

import com.qcloud.sign.*;
import java.io.IOException;

/**
 *
 * @author jusisli
 */
public class PicCloud {
        protected static String VERSION = "2.1.0";
	protected static String QCLOUD_DOMAIN = "image.myqcloud.com";

	protected int mAppId;
	protected String mSecretId;
	protected String mSecretKey;
        protected String mBucket;

	protected int mErrno;
	protected String mError;

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

        public String getResponse(HttpURLConnection connection) throws IOException {
            String rsp = "";
            BufferedReader in = new BufferedReader(new InputStreamReader(
                            connection.getInputStream()));

            String line;
            while ((line = in.readLine()) != null) {
                    rsp += line;
            }

            return rsp;
	}

        /**
	 * Upload 上传图片
	 * @param fileName		上传的文件名
	 * @param result		返回的图片的上传信息
	 * @return				错误码，0为成功
	 */
        public int upload(String fileName, UploadResult result) {
            return upload(fileName, "", new PicAnalyze(), result);
        }
        
	public int upload(String fileName, String fileId, UploadResult result) {
            return upload(fileName, fileId, new PicAnalyze(), result);
        }
        
	public int upload(String fileName, String fileId, PicAnalyze flag, UploadResult result) {
            if ("".equals(fileName)) {
                return setError(-1, "invalid file name");
            }
            
            FileInputStream fileStream = null;
            try {
                fileStream = new FileInputStream(fileName);
            } catch (FileNotFoundException ex) {
                return setError(-1, "invalid file name");
            }
            return upload(fileStream, fileId, flag, result);
	}

        public int upload(InputStream inputStream, UploadResult result) {
            return upload(inputStream, "", new PicAnalyze(), result);
        }
        
	public int upload(InputStream inputStream, String fileId, UploadResult result) {
            return upload(inputStream, fileId, new PicAnalyze(), result);
        }
        
        public int upload(InputStream inputStream, String fileId, PicAnalyze flag, UploadResult result) {
            String reqUrl = getUrl("0", fileId);
            String BOUNDARY = "---------------------------abcdefg1234567";
            String rsp;

            //check analyze flag
            String queryString = "";
            if(flag.fuzzy != 0){
                queryString += ".fuzzy";
            }
            if(flag.food != 0){
                queryString += ".food";
            }
            if ("".equals(queryString) == false) {
                reqUrl += "?analyze="+queryString.substring(1);
            }

            // create sign
            long expired = System.currentTimeMillis() / 1000 + 2592000;
            String sign = FileCloudSign.appSignV2(mAppId, mSecretId, mSecretKey, mBucket, expired);
            if (null == sign) {
                    return setError(-1, "create app sign failed");
            }

            try {
                    URL realUrl = new URL(reqUrl);
                    HttpURLConnection connection = (HttpURLConnection) realUrl
                                    .openConnection();
                    // set header
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("accept", "*/*");
                    connection.setRequestProperty("Host", "web.image.myqcloud.com");
                    connection.setRequestProperty("user-agent", "qcloud-java-sdk");
                    connection.setRequestProperty("Authorization", sign);

                    connection.setDoInput(true);
                    connection.setDoOutput(true);
                    connection.setRequestProperty("Content-Type",
                                    "multipart/form-data; boundary=" + BOUNDARY);

                    OutputStream out = new DataOutputStream(
                                    connection.getOutputStream());
                    StringBuilder strBuf = new StringBuilder();
                    strBuf.append("\r\n").append("--").append(BOUNDARY).append("\r\n");
                    strBuf.append("Content-Disposition: form-data; name=\"FileContent\"\r\n\r\n");
                    out.write(strBuf.toString().getBytes());

                    int bytes;
                    byte[] bufferOut = new byte[1024];
                    while ((bytes = inputStream.read(bufferOut)) != -1) {
                        out.write(bufferOut, 0, bytes);
                    }
                            
                    byte[] endData = ("\r\n--" + BOUNDARY + "--\r\n").getBytes();
                    out.write(endData);
                    out.flush();
                    out.close();

                    connection.connect();
                    rsp = getResponse(connection);
            } catch (Exception e) {
                    return setError(-1, "url exception, e=" + e.toString());
            }
            try {
                    JSONObject jsonObject = new JSONObject(rsp);
                    int code = jsonObject.getInt("code");
                    String msg = jsonObject.getString("message");
                    if (0 != code) {
                            return setError(code, msg);
                    }

                    result.url = jsonObject.getJSONObject("data").getString("url");
                    result.downloadUrl = jsonObject.getJSONObject("data").getString(
                                    "download_url");
                    result.fileId = jsonObject.getJSONObject("data")
                                    .getString("fileid");

                    if(jsonObject.getJSONObject("data").has("is_fuzzy")){
                        result.analyze.fuzzy = jsonObject.getJSONObject("data").getInt("is_fuzzy");
                    }
                    if(jsonObject.getJSONObject("data").has("is_food")){
                        result.analyze.food = jsonObject.getJSONObject("data").getInt("is_food");
                    }

            } catch (JSONException e) {
                    return setError(-1, "json exception, e=" + e.toString());
            }
            return setError(0, "success");
	}
        
	/**
	 * Delete 删除图片
	 * @param fileId		图片的唯一标识
	 * @return 				错误码，0为成功
	 */
	public int delete(String fileId) {
		String reqUrl = getUrl("0", fileId) + "/del";
		String rsp;

		// create sign once
                String sign = FileCloudSign.appSignOnceV2(mAppId, mSecretId, mSecretKey, mBucket, fileId);
                if (null == sign) {
                    return setError(-1, "create app sign failed");
                }

		try {
			URL realUrl = new URL(reqUrl);
			HttpURLConnection connection = (HttpURLConnection) realUrl
					.openConnection();
			// set header
			connection.setRequestMethod("POST");
			connection.setRequestProperty("accept", "*/*");
			connection.setRequestProperty("Host", "web.image.myqcloud.com");
			connection.setRequestProperty("user-agent", "qcloud-java-sdk");
			connection.setRequestProperty("Authorization", sign);

			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.connect();

			// read rsp
			rsp = getResponse(connection);
		} catch (Exception e) {
			return setError(-1, "url exception, e=" + e.toString());
		}
                
		try {
			JSONObject jsonObject = new JSONObject(rsp);
			int code = jsonObject.getInt("code");
			String msg = jsonObject.getString("message");
			if (0 != code) {
				return setError(code, msg);
			}
		} catch (JSONException e) {
			return setError(-1, "json exception, e=" + e.toString());
		}

		return setError(0, "success");
	}

	/**
	 * Stat 查询图片信息
	 * @param fileId	图片fileid
	 * @param info	 	返回的图片信息
	 * @return 			错误码，0为成功
	 */
	public int stat(String fileId, PicInfo info) {
		String reqUrl = getUrl("0", fileId);
		String rsp;

		try {
			URL realUrl = new URL(reqUrl);
			HttpURLConnection connection = (HttpURLConnection) realUrl
					.openConnection();
			// set header
			connection.setRequestMethod("GET");
			connection.setRequestProperty("Host", "web.image.myqcloud.com");
			connection.setRequestProperty("user-agent", "qcloud-java-sdk");

			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.connect();

			// read rsp
			rsp = getResponse(connection);
		} catch (Exception e) {
			return setError(-1, "url exception, e=" + e.toString());
		}

		try {
			JSONObject jsonObject = new JSONObject(rsp);
			int code = jsonObject.getInt("code");
			String msg = jsonObject.getString("message");
			if (0 != code) {
				return setError(code, msg);
			}

			info.url = jsonObject.getJSONObject("data").getString("file_url");
			info.fileId = jsonObject.getJSONObject("data").getString(
					"file_fileid");
			info.uploadTime = jsonObject.getJSONObject("data").getInt(
					"file_upload_time");
			info.size = jsonObject.getJSONObject("data").getInt("file_size");
			info.md5 = jsonObject.getJSONObject("data").getString("file_md5");
			info.width = jsonObject.getJSONObject("data").getInt("photo_width");
			info.height = jsonObject.getJSONObject("data").getInt(
					"photo_height");
		} catch (JSONException e) {
			return setError(-1, "json exception, e=" + e.toString());
		}

		return setError(0, "success");
	}

	/**
	 * Copy 复制图片
	 * @param fileId	 图片的唯一标识
	 * @param result	 返回的图片的上传信息
	 * @return 错误码，0为成功
	 */
	public int copy(String fileId, UploadResult result) {
		String reqUrl = getUrl("0", fileId) + "/copy";
		String rsp;

		// create sign once
		String sign = FileCloudSign.appSignOnceV2(mAppId, mSecretId, mSecretKey, mBucket, fileId);
                if (null == sign) {
                    return setError(-1, "create app sign failed");
                }

		try {
			URL realUrl = new URL(reqUrl);
			HttpURLConnection connection = (HttpURLConnection) realUrl
					.openConnection();
			// set header
			connection.setRequestMethod("POST");
			connection.setRequestProperty("accept", "*/*");
			connection.setRequestProperty("Host", "web.image.myqcloud.com");
			connection.setRequestProperty("user-agent", "qcloud-java-sdk");
			connection.setRequestProperty("Authorization", sign);

			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.connect();

			// read rsp
			rsp = getResponse(connection);
		} catch (Exception e) {
			return setError(-1, "url exception, e=" + e.toString());
		}

		try {
			JSONObject jsonObject = new JSONObject(rsp);
			int code = jsonObject.getInt("code");
			String msg = jsonObject.getString("message");
			if (0 != code) {
				return setError(code, msg);
			}

			result.url = jsonObject.getJSONObject("data").getString("url");
			result.downloadUrl = jsonObject.getJSONObject("data").getString(
					"download_url");
			result.fileId = result.url
					.substring(result.url.lastIndexOf('/') + 1);
		} catch (JSONException e) {
			return setError(-1, "json exception, e=" + e.toString());
		}

		return setError(0, "success");
	}

	/**
	 * Download 下载图片
	 * @param url           图片的唯一标识
	 * @param fileName	 下载图片的保存路径
	 * @return 错误码，0为成功
	 */
	public int download(String url, String fileName) {
            if ("".equals(fileName)) {
                return setError(-1, "file name is empty.");
            }
            String rsp = "";
            try {
		URL realUrl = new URL(url);
		HttpURLConnection connection = (HttpURLConnection) realUrl
				.openConnection();
		// set header
		connection.setRequestMethod("GET");
		connection.setRequestProperty("Host", "web.image.myqcloud.com");
		connection.setRequestProperty("user-agent", "qcloud-java-sdk");

		connection.setDoInput(true);
		connection.setDoOutput(true);
		connection.connect();

                InputStream in = new DataInputStream(connection.getInputStream());
                File file = new File(fileName);
                DataOutputStream ops = new DataOutputStream(new FileOutputStream(
                                file));
                int bytes;
                byte[] bufferOut = new byte[1024];
                while ((bytes = in.read(bufferOut)) > 0) {
                        ops.write(bufferOut, 0, bytes);
                }
                ops.close();
                in.close();
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

};
