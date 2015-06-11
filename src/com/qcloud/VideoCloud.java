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
public class VideoCloud
{
	protected static String QCLOUD_DOMAIN = "web.video.myqcloud.com/videos/v1";
	protected static String QCLOUD_DOWNLOAD_DOMAIN = "video.myqcloud.com";
	
	protected int m_appid;
	protected String m_secret_id;
	protected String m_secret_key;
        
        protected int m_errno;
        protected String m_error;

        /**
	    VideoCloud 构造方法
            @param  appid        授权appid
	    @param  secret_id    授权secret_id
	    @param  secret_key   授权secret_key
        */
        public VideoCloud(int appid, String secret_id, String secret_key){
            m_appid = appid;
            m_secret_id = secret_id;
            m_secret_key = secret_key;
            m_errno = 0;
            m_error = "";
	}
            
        /**
	    VideoCloud 构造方法
            @param  appid        授权appid
	    @param  secret_id    授权secret_id
	    @param  secret_key   授权secret_key
        */
	public VideoCloud(String appid, String secret_id, String secret_key){
            m_appid = Integer.parseInt(appid);
            m_secret_id = secret_id;
            m_secret_key = secret_key;
            m_errno = 0;
            m_error = "";
	}
        
        public int GetErrno(){
            return m_errno;
        }
        
        public String GetErrMsg(){
            return m_error;
        }
        
        public int SetError(int errno, String msg){
            m_errno = errno;
            m_error = msg;
            return errno;
        }
        
        public String GetError(){
            return "errno="+m_errno+" desc="+m_error;
        }
        
        public String GetResponse(HttpURLConnection connection) throws IOException{
            String rsp = "";
            try (BufferedReader in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()))) {
                rsp = "";
                String line;
                while ((line = in.readLine()) != null) {
                    rsp += line;
                }
            }
            System.out.println("Debug: rsp = "+rsp);
            return rsp;
        }


        
        /**
	    Upload      上传视频
        @param  userid      业务账号,没有填0
	    @param  fileName    上传的文件名
		@param  title    	视频标题
		@param  desc        视频描述
		@param  magicContext    透传字段，需要业务在管理控制台设置回调url		
	    @param  result      返回的视频的上传信息
        @return 错误码，0为成功
        */
	public int Upload(String userid, String fileName, String title, String desc,String magicContext, UploadResult result){
            String req_url = "http://"+QCLOUD_DOMAIN+"/"+m_appid+"/"+userid;
            String BOUNDARY = "---------------------------" + MD5.stringToMD5(String.valueOf(System.currentTimeMillis())).substring(0,15); 
            String rsp = "";
		
            //create sign
            StringBuffer sign = new StringBuffer("");		
            long expired = System.currentTimeMillis() / 1000 + 2592000;
            if( FileCloudSign.appSign(Integer.toString(m_appid), m_secret_id, m_secret_key, expired, userid, sign) != 0){
                return SetError(-1, "create app sign failed");
            }
            String qcloud_sign=sign.toString();

            try{
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
                StringBuilder strBuf = new StringBuilder();
			
                if(fileName != null){
                    File file = new File(fileName);  
                    String filename = file.getName();
                    String contentType = URLConnection.getFileNameMap().getContentTypeFor(fileName);
				
                    strBuf.append("\r\n").append("--").append(BOUNDARY).append("\r\n");  
                    strBuf.append("Content-Disposition: form-data; name=\"FileContent\"; filename=\"").append(fileName).append("\"\r\n");
                    strBuf.append("Content-Type:").append(contentType).append("\r\n\r\n");  
				
                    out.write(strBuf.toString().getBytes());

                    DataInputStream ins = new DataInputStream(new FileInputStream(file));
                    int bytes = 0;
                    byte[] bufferOut = new byte[1024];
                    while ((bytes = ins.read(bufferOut)) != -1) {
                        out.write(bufferOut, 0, bytes);  
                    }
		}

		byte[] endData = ("\r\n--" + BOUNDARY + "\r\n").getBytes();
		out.write(endData); 
		byte[] arrTitle = ("Content-Disposition: form-data; name=\"Title\";\r\n\r\n" + title + "\r\n--" + BOUNDARY + "\r\n").getBytes();
		out.write(arrTitle); 
		byte[] arrDesc = ("Content-Disposition: form-data; name=\"Desc\";\r\n\r\n" + desc + "\r\n--" + BOUNDARY + "\r\n").getBytes();
		out.write(arrDesc); 
		byte[] arrMagicContext = ("Content-Disposition: form-data; name=\"magiccontext\";\r\n\r\n" + magicContext + "\r\n--" + BOUNDARY + "--\r\n").getBytes();
		out.write(arrMagicContext); 
		
		out.flush();
		out.close(); 
			
		connection.connect();		
		rsp = GetResponse(connection);
            }catch(Exception e){
                return SetError(-1, "url exception, e="+e.toString());
            }
            
            try{
                JSONObject jsonObject = new JSONObject(rsp);
                int code = jsonObject.getInt("code");
                String msg = jsonObject.getString("message");
                if(0 != code){
                    return SetError(code, msg);
                }
                
                result.url = jsonObject.getJSONObject("data").getString("url");
                result.download_url = jsonObject.getJSONObject("data").getString("download_url");   
                result.fileid = jsonObject.getJSONObject("data").getString("fileid"); 
            }catch(JSONException e){
		return SetError(-1, "json exception, e="+e.toString());
            }
            return SetError(0, "success");
	}
        
        /**
	    Delete      删除视频
        @param  userid      业务账号,没有填0
	    @param  fileid      视频的唯一标识
        @return 错误码，0为成功
        */
        public int Delete(String userid, String fileid){
            String req_url = "http://"+QCLOUD_DOMAIN+"/"+m_appid+"/"+userid+"/"+fileid+"/del";
            String download_url = "http://"+m_appid+"."+QCLOUD_DOWNLOAD_DOMAIN+"/"+m_appid+"/"+userid+"/"+fileid+"/original";
            String rsp = "";
            
            //create sign once
            StringBuffer sign = new StringBuffer("");		
            if(0 != FileCloudSign.appSignOnce(Integer.toString(m_appid), m_secret_id, m_secret_key, userid, download_url, sign)){
                return SetError(-1, "create app sign failed");
            }
            String qcloud_sign=sign.toString();
            
            try{
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
		connection.connect();
                
                //read rsp
                rsp = GetResponse(connection);
            }catch(Exception e){
                return SetError(-1, "url exception, e="+e.toString());
            }
            
            try{
                JSONObject jsonObject = new JSONObject(rsp);
                int code = jsonObject.getInt("code");
                String msg = jsonObject.getString("message");
                if(0 != code){
                    return SetError(code, msg);
                }         
            }catch(JSONException e){
		return SetError(-1, "json exception, e="+e.toString());
            }
            
            return SetError(0, "success");
        }
 
        /**
	    Stat        查询视频信息
        @param  userid      业务账号,没有填0
	    @param  fileid      视频的唯一标识
        @param  info        返回的视频信息
        @return 错误码，0为成功
        */
        public int Stat(String userid, String fileid, VideoInfo info){
            String req_url = "http://"+QCLOUD_DOMAIN+"/"+m_appid+"/"+userid+"/"+fileid;
            String rsp = "";
            
            try{
                URL realUrl = new URL(req_url);
		HttpURLConnection connection = (HttpURLConnection)realUrl.openConnection();
		//set header
		connection.setRequestMethod("GET");
		connection.setRequestProperty("Host", "web.image.myqcloud.com");
		connection.setRequestProperty("user-agent","qcloud-java-sdk");
			
		connection.setDoInput(true);
		connection.setDoOutput(true);	
		connection.connect();
                
                //read rsp
                rsp = GetResponse(connection);
            }catch(Exception e){
                return SetError(-1, "url exception, e="+e.toString());
            }
            
            try{
                JSONObject jsonObject = new JSONObject(rsp);
                int code = jsonObject.getInt("code");
                String msg = jsonObject.getString("message");
                if(0 != code){
                    return SetError(code, msg);
                }         
                
                info.url = jsonObject.getJSONObject("data").getString("file_url");
                info.fileid = jsonObject.getJSONObject("data").getString("file_fileid");   
                info.upload_time = jsonObject.getJSONObject("data").getInt("file_upload_time"); 
                info.size = jsonObject.getJSONObject("data").getInt("file_size"); 
                info.sha = jsonObject.getJSONObject("data").getString("file_sha"); 
                info.status = jsonObject.getJSONObject("data").getInt("video_status"); 
                info.status_msg = jsonObject.getJSONObject("data").getString("video_status_msg");
                info.video_play_time = jsonObject.getJSONObject("data").getInt("video_play_time");
                info.video_title = jsonObject.getJSONObject("data").getString("video_title");
                info.video_desc = jsonObject.getJSONObject("data").getString("video_desc");
                info.video_cover_url = jsonObject.getJSONObject("data").getString("video_cover_url");				
            }catch(JSONException e){
		return SetError(-1, "json exception, e="+e.toString());
            }
            
            return SetError(0, "success");            
        }
};
	
