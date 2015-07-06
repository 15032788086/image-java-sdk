/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.qcloud;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import org.json.JSONObject;
import org.json.JSONException;

import com.qcloud.sign.*;
import java.io.IOException;

/**
 *
 * @author jusisli
 */
public class PicCloud {
        protected static String VERSION = "2.0.0";
	protected static String QCLOUD_DOMAIN = "image.myqcloud.com";

	protected int m_appid;
	protected String m_secret_id;
	protected String m_secret_key;
        protected String m_bucket;

	protected int m_errno;
	protected String m_error;

	/**
	 * PicCloud 构造方法
	 * @param appid				授权appid
	 * @param secret_id			授权secret_id
	 * @param secret_key                   授权secret_key
	 */
	public PicCloud(int appid, String secret_id, String secret_key) {
            m_appid = appid;
            m_secret_id = secret_id;
            m_secret_key = secret_key;
            m_errno = 0;
            m_bucket = "";
            m_error = "";
	}
        
        	/**
	 * PicCloud 构造方法
	 * @param appid				授权appid
	 * @param secret_id			授权secret_id
	 * @param secret_key                   授权secret_key
         * @param bucket `                     空间名 
	 */
	public PicCloud(int appid, String secret_id, String secret_key, String bucket) {
            m_appid = appid;
            m_secret_id = secret_id;
            m_secret_key = secret_key;
            m_bucket = bucket;
            m_errno = 0;
            m_error = "";
	}
        
        public String GetVersion(){
            return VERSION;
        }

	public int GetErrno() {
            return m_errno;
	}

	public String GetErrMsg() {
            return m_error;
	}

	public int SetError(int errno, String msg) {
            m_errno = errno;
            m_error = msg;
            return errno;
	}

	public String GetError() {
            return "errno=" + m_errno + " desc=" + m_error;
	}
        
        public String GetUrl(String userid, String fileid){
            String url = "";    
            if ("".equals(m_bucket)) {
                url = String.format("http://web.%s/photos/v1/%d/%s", QCLOUD_DOMAIN, m_appid, userid);
            }else{
                url = String.format("http://web.%s/photos/v2/%d/%s/%s", QCLOUD_DOMAIN, m_appid, m_bucket, userid);
            }
            if ("".equals(fileid) == false) {
                url += "/"+fileid;
            }
            return url;
        }
        
        public String GetDownloadUrl(String userid, String fileid){
            String url = "";    
            if ("".equals(m_bucket)) {
                url = String.format("http://%d.%s/%d/%s/%s/original", m_appid, QCLOUD_DOMAIN, m_appid, userid, fileid);
            }else{
                url = String.format("http://%s-%d.%s/%s-%d/%s/%s/original", m_bucket, m_appid, QCLOUD_DOMAIN, m_bucket, m_appid, userid, fileid);
            }
            return url;
        }

        public String GetResponse(HttpURLConnection connection) throws IOException {
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
	 * @param userid		业务账号,没有填0
	 * @param fileName		上传的文件名
	 * @param result		返回的图片的上传信息
	 * @return				错误码，0为成功
	 */
        public int Upload(String userid, String fileName, UploadResult result) {
            return Upload(userid, fileName, "", new PicAnalyze(), result);
        }
        
	public int Upload(String userid, String fileName, String fileid, UploadResult result) {
            return Upload(userid, fileName, fileid, new PicAnalyze(), result);
        }
        
	public int Upload(String userid, String fileName, String fileid, PicAnalyze flag, UploadResult result) {
            if ("".equals(fileName)) {
                return SetError(-1, "invalid file name");
            }
            
            String req_url = GetUrl(userid, fileid);
            String BOUNDARY = "---------------------------" + MD5.stringToMD5(String.valueOf(System.currentTimeMillis())).substring(0, 15);
            String rsp = "";

            //check analyze flag
            String query_string = "";
            if(flag.fuzzy != 0){
                query_string += ".fuzzy";
            }
            if(flag.food != 0){
                query_string += ".food";
            }
            if ("".equals(query_string) == false) {
                req_url += "?analyze="+query_string.substring(1);
            }

            // create sign
            StringBuffer sign = new StringBuffer("");
            long expired = System.currentTimeMillis() / 1000 + 2592000;
            if (FileCloudSign.appSignV2(Integer.toString(m_appid), m_secret_id,m_secret_key, 
                    m_bucket,
                    expired, userid, sign) != 0) {
                    return SetError(-1, "create app sign failed");
            }
            String qcloud_sign = sign.toString();

            try {
                    URL realUrl = new URL(req_url);
                    HttpURLConnection connection = (HttpURLConnection) realUrl
                                    .openConnection();
                    // set header
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("accept", "*/*");
                    connection.setRequestProperty("Host", "web.image.myqcloud.com");
                    connection.setRequestProperty("user-agent", "qcloud-java-sdk");
                    connection.setRequestProperty("Authorization", qcloud_sign);

                    connection.setDoInput(true);
                    connection.setDoOutput(true);
                    connection.setRequestProperty("Content-Type",
                                    "multipart/form-data; boundary=" + BOUNDARY);

                    OutputStream out = new DataOutputStream(
                                    connection.getOutputStream());
                    StringBuilder strBuf = new StringBuilder();

                    if (fileName != null) {
                            File file = new File(fileName);
                            String filename = file.getName();
                            String contentType = URLConnection.getFileNameMap()
                                            .getContentTypeFor(fileName);

                            strBuf.append("\r\n").append("--").append(BOUNDARY)
                                            .append("\r\n");
                            strBuf.append(
                                            "Content-Disposition: form-data; name=\"FileContent\"; filename=\"")
                                            .append(fileName).append("\"\r\n");
                            strBuf.append("Content-Type:").append(contentType)
                                            .append("\r\n\r\n");

                            out.write(strBuf.toString().getBytes());

                            DataInputStream ins = new DataInputStream(new FileInputStream(
                                            file));
                            int bytes = 0;
                            byte[] bufferOut = new byte[1024];
                            while ((bytes = ins.read(bufferOut)) != -1) {
                                    out.write(bufferOut, 0, bytes);
                            }
                    }

                    byte[] endData = ("\r\n--" + BOUNDARY + "--\r\n").getBytes();
                    out.write(endData);
                    out.flush();
                    out.close();

                    connection.connect();
                    rsp = GetResponse(connection);
            } catch (Exception e) {
                    return SetError(-1, "url exception, e=" + e.toString());
            }
            System.out.println("rsp=" + rsp);
            try {
                    JSONObject jsonObject = new JSONObject(rsp);
                    int code = jsonObject.getInt("code");
                    String msg = jsonObject.getString("message");
                    if (0 != code) {
                            return SetError(code, msg);
                    }

                    result.url = jsonObject.getJSONObject("data").getString("url");
                    result.download_url = jsonObject.getJSONObject("data").getString(
                                    "download_url");
                    result.fileid = jsonObject.getJSONObject("data")
                                    .getString("fileid");

                    if(jsonObject.getJSONObject("data").has("is_fuzzy")){
                        result.analyze.fuzzy = jsonObject.getJSONObject("data").getInt("is_fuzzy");
                    }
                    if(jsonObject.getJSONObject("data").has("is_food")){
                        result.analyze.food = jsonObject.getJSONObject("data").getInt("is_food");
                    }

            } catch (JSONException e) {
                    return SetError(-1, "json exception, e=" + e.toString());
            }
            return SetError(0, "success");
	}

	/**
	 * Delete 删除图片
	 * @param userid		业务账号,没有填0
	 * @param fileid		图片的唯一标识
	 * @return 				错误码，0为成功
	 */
	public int Delete(String userid, String fileid) {
		String req_url = GetUrl(userid, fileid) + "/del";
		String rsp = "";

		// create sign once
		StringBuffer sign = new StringBuffer("");
		if (0 != FileCloudSign.appSignOnceV2(Integer.toString(m_appid),m_secret_id, m_secret_key,
                        m_bucket,
                        userid, fileid, sign)) {
			return SetError(-1, "create app sign failed");
		}
		String qcloud_sign = sign.toString();

		try {
			URL realUrl = new URL(req_url);
			HttpURLConnection connection = (HttpURLConnection) realUrl
					.openConnection();
			// set header
			connection.setRequestMethod("POST");
			connection.setRequestProperty("accept", "*/*");
			connection.setRequestProperty("Host", "web.image.myqcloud.com");
			connection.setRequestProperty("user-agent", "qcloud-java-sdk");
			connection.setRequestProperty("Authorization", qcloud_sign);

			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.connect();

			// read rsp
			rsp = GetResponse(connection);
		} catch (Exception e) {
			return SetError(-1, "url exception, e=" + e.toString());
		}

		try {
			JSONObject jsonObject = new JSONObject(rsp);
			int code = jsonObject.getInt("code");
			String msg = jsonObject.getString("message");
			if (0 != code) {
				return SetError(code, msg);
			}
		} catch (JSONException e) {
			return SetError(-1, "json exception, e=" + e.toString());
		}

		return SetError(0, "success");
	}

	/**
	 * Stat 查询图片信息
	 * @param userid	业务账号,没有填0
	 * @param info	 	返回的图片信息
	 * @return 			错误码，0为成功
	 */
	public int Stat(String userid, String fileid, PicInfo info) {
		String req_url = GetUrl(userid, fileid);
		String rsp = "";

		try {
			URL realUrl = new URL(req_url);
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
			rsp = GetResponse(connection);
		} catch (Exception e) {
			return SetError(-1, "url exception, e=" + e.toString());
		}

		try {
			JSONObject jsonObject = new JSONObject(rsp);
			int code = jsonObject.getInt("code");
			String msg = jsonObject.getString("message");
			if (0 != code) {
				return SetError(code, msg);
			}

			info.url = jsonObject.getJSONObject("data").getString("file_url");
			info.fileid = jsonObject.getJSONObject("data").getString(
					"file_fileid");
			info.upload_time = jsonObject.getJSONObject("data").getInt(
					"file_upload_time");
			info.size = jsonObject.getJSONObject("data").getInt("file_size");
			info.md5 = jsonObject.getJSONObject("data").getString("file_md5");
			info.width = jsonObject.getJSONObject("data").getInt("photo_width");
			info.height = jsonObject.getJSONObject("data").getInt(
					"photo_height");
		} catch (JSONException e) {
			return SetError(-1, "json exception, e=" + e.toString());
		}

		return SetError(0, "success");
	}

	/**
	 * Copy 复制图片
	 * @param userid	业务账号,没有填0
	 * @param fileid	 图片的唯一标识
	 * @param result	 返回的图片的上传信息
	 * @return 错误码，0为成功
	 */
	public int Copy(String userid, String fileid, UploadResult result) {
		String req_url = GetUrl(userid, fileid) + "/copy";
		String rsp = "";

		// create sign once
		StringBuffer sign = new StringBuffer("");
		if (0 != FileCloudSign.appSignOnceV2(Integer.toString(m_appid),m_secret_id, m_secret_key,
                        m_bucket,
                        userid, fileid, sign)) {
			return SetError(-1, "create app sign failed");
		}
		String qcloud_sign = sign.toString();

		try {
			URL realUrl = new URL(req_url);
			HttpURLConnection connection = (HttpURLConnection) realUrl
					.openConnection();
			// set header
			connection.setRequestMethod("POST");
			connection.setRequestProperty("accept", "*/*");
			connection.setRequestProperty("Host", "web.image.myqcloud.com");
			connection.setRequestProperty("user-agent", "qcloud-java-sdk");
			connection.setRequestProperty("Authorization", qcloud_sign);

			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.connect();

			// read rsp
			rsp = GetResponse(connection);
		} catch (Exception e) {
			return SetError(-1, "url exception, e=" + e.toString());
		}

		try {
			JSONObject jsonObject = new JSONObject(rsp);
			int code = jsonObject.getInt("code");
			String msg = jsonObject.getString("message");
			if (0 != code) {
				return SetError(code, msg);
			}

			result.url = jsonObject.getJSONObject("data").getString("url");
			result.download_url = jsonObject.getJSONObject("data").getString(
					"download_url");
			result.fileid = result.url
					.substring(result.url.lastIndexOf('/') + 1);
		} catch (JSONException e) {
			return SetError(-1, "json exception, e=" + e.toString());
		}

		return SetError(0, "success");
	}

	/**
	 * Download 下载图片（不启用防盗链）
	 * @param userid	业务账号,没有填0
	 * @param fileid	 图片的唯一标识
	 * @param fileName	 下载图片的保存路径
	 * @return 错误码，0为成功
	 */
	public int Download(String userid, String fileid, String fileName) {
		String download_url = GetDownloadUrl(userid, fileid);
		return Download(download_url, fileName);
	}

	/**
	 * DownloadEx 下载图片（启用防盗链）
	 * @param userid		 业务账号,没有填0
	 * @param fileid		图片的唯一标识
	 * @param fileName		下载图片的保存路径
	 * @return 错误码，0为成功
	 */
	public int DownloadEx(String userid, String fileid, String fileName) {
		String download_url =  GetDownloadUrl(userid, fileid);
		// create sign once
		StringBuffer sign = new StringBuffer("");
		if (0 != FileCloudSign.appSignOnceV2(Integer.toString(m_appid), m_secret_id, m_secret_key, 
                                m_bucket,
                                userid, fileid, sign)) {
			return SetError(-1, "create app sign failed");
		}
		download_url += "?sign=" + sign;
		return Download(download_url, fileName);
	}

	/**
	 * Download 下载图片（直接提供url的方式，如果启动防盗链，需要提前自己添加sign）
	 * @param download_url	             下载url
	 * @param fileName           下载图片的保存路径
	 * @return 错误码，0为成功
	 */
	public int Download(String download_url, String fileName) {
		if ("".equals(fileName)) {
			return SetError(-1, "file name is empty.");
		}
		String rsp = "";
		try {
			URL realUrl = new URL(download_url);
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
			int bytes = 0;
			byte[] bufferOut = new byte[1024];
			while ((bytes = in.read(bufferOut)) > 0) {
				ops.write(bufferOut, 0, bytes);
			}
			ops.close();
			in.close();
		} catch (Exception e) {
			return SetError(-1, "url exception, e=" + e.toString());
		}

		return SetError(0, "success");
	}

};
