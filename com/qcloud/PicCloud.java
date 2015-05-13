package com.qcloud;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Random;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import com.qcloud.sign.*;

public class PicCloud
{
	protected static String QCLOUD_DOMAIN = "web.image.myqcloud.com/photos/v1";
	protected static String QCLOUD_DOWNLOAD_DOMAIN = "image.myqcloud.com";
	
	protected String m_appid;
	protected String m_secret_id;
	protected String m_secret_key;

	public PicCloud(String appid, String secret_id, String secret_key){
		m_appid = appid;
		m_secret_id = secret_id;
		m_secret_key = secret_key;
	}

	public int Upload(String userid, String fileName, UploadResult result){
		String req_url = "http://"+QCLOUD_DOMAIN+"/"+m_appid+"/"+userid;
		
		//create sign
		StringBuffer sign = new StringBuffer("");		
		long expired = System.currentTimeMillis() / 1000 + 2592000;
		FileCloudSign.appSign(m_appid, m_secret_id, m_secret_key, expired, userid, sign);
		String qcloud_sign="QCloud "+sign.toString();
		
		try{
			String BOUNDARY = "---------------------------" + MD5.stringToMD5(String.valueOf(System.currentTimeMillis())).substring(0,15);

			URL realUrl = new URL(req_url);
			HttpURLConnection connection = (HttpURLConnection)realUrl.openConnection();
			//set header
			connection.setRequestMethod("POST");
			connection.setRequestProperty("accept", "*/*");
			connection.setRequestProperty("Host", "web.image.myqcloud.com");
			connection.setRequestProperty("user-agent","qcloud-java-sdk");
			connection.setRequestProperty("Authorization", qcloud_sign);
			
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);  

			OutputStream out = new DataOutputStream(connection.getOutputStream());
			StringBuffer strBuf = new StringBuffer();
			
			if(fileName != null){
				File file = new File(fileName);  
				String filename = file.getName();
				String contentType = URLConnection.getFileNameMap().getContentTypeFor(fileName);
				
				strBuf.append("\r\n").append("--").append(BOUNDARY).append("\r\n");  
				strBuf.append("Content-Disposition: form-data; name=\"FileContent\"; filename=\"" + fileName + "\"\r\n");
				strBuf.append("Content-Type:" + contentType + "\r\n\r\n");  
				
				out.write(strBuf.toString().getBytes());

				DataInputStream ins = new DataInputStream(new FileInputStream(file));  
				int bytes = 0;  
				byte[] bufferOut = new byte[1024];  
				while ((bytes = ins.read(bufferOut)) != -1) {  
					out.write(bufferOut, 0, bytes);  
				}
				ins.close(); 
			}

			byte[] endData = ("\r\n--" + BOUNDARY + "--\r\n").getBytes();  
			out.write(endData); 
			out.flush();  
			out.close(); 
			
			connection.connect();
			BufferedReader in = new BufferedReader(new InputStreamReader(
						connection.getInputStream()));
			
			String rsp = "";
			String line;
			while ((line = in.readLine()) != null) {
				rsp += line;
			}

			JSONObject jsonObject = new JSONObject(rsp);
			int code = -1;
			String data = "";
			Iterator iterator = jsonObject.keys();
			while (iterator.hasNext()) {
				String key = (String) iterator.next();
				if(key.equals("code")){
					code = Integer.parseInt(jsonObject.getString(key));
				}else if(key.equals("data")){
					data = jsonObject.getString(key);
				}
			}

			if(code != 0 || data == "")
				return code;

			jsonObject = new JSONObject(data);
			iterator = jsonObject.keys();
			while (iterator.hasNext()) {
				String key = (String) iterator.next();
				if(key.equals("url")){
					result.url = jsonObject.getString(key);
				}else if(key.equals("download_url")){
					result.download_url = jsonObject.getString(key);
				}
			}

		}catch(Exception e){
			e.printStackTrace(); 
			return -1;
		}
		return 0;
	}

};
	
