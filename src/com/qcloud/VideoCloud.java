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
public class VideoCloud {
	protected static String QCLOUD_DOMAIN = "web.video.myqcloud.com/videos/v1";
	protected static String QCLOUD_DOWNLOAD_DOMAIN = "video.myqcloud.com";

	protected int m_appid;
	protected String m_secret_id;
	protected String m_secret_key;

	protected int m_errno;
	protected String m_error;

	/**
	 * VideoCloud 构造方法
	 * @param appid			授权appid
	 * @param secret_id		授权secret_id
	 * @param secret_key	 授权secret_key
	 */
	public VideoCloud(int appid, String secret_id, String secret_key) {
		m_appid = appid;
		m_secret_id = secret_id;
		m_secret_key = secret_key;
		m_errno = 0;
		m_error = "";
	}

	/**
	 * VideoCloud 构造方法
	 * @param appid			授权appid
	 * @param secret_id		授权secret_id
	 * @param secret_key	授权secret_key
	 */
	public VideoCloud(String appid, String secret_id, String secret_key) {
		m_appid = Integer.parseInt(appid);
		m_secret_id = secret_id;
		m_secret_key = secret_key;
		m_errno = 0;
		m_error = "";
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

	public String GetResponse(HttpURLConnection connection) throws IOException {
		String rsp = "";
		try (BufferedReader in = new BufferedReader(new InputStreamReader(
				connection.getInputStream()))) {
			rsp = "";
			String line;
			while ((line = in.readLine()) != null) {
				rsp += line;
			}
		}
		System.out.println("Debug: rsp = " + rsp);
		return rsp;
	}

	/**
	 * Upload 上传视频，适用于较小视频，如果视频过大，请选择SliceUpload函数
	 * @param userid		业务账号,没有填0
	 * @param fileName		上传的文件名
	 * @param title			视频标题
	 * @param desc			 视频描述
	 * @param magicContext	透传字段，需要业务在管理控制台设置回调url
	 * @param result		返回的视频的上传信息
	 * @return 错误码，0为成功
	 */
	public int Upload(String userid, String fileName, String title,
			String desc, String magicContext, UploadResult result) {
		String req_url = "http://" + QCLOUD_DOMAIN + "/" + m_appid + "/"
				+ userid;
		String BOUNDARY = "---------------------------"
				+ MD5.stringToMD5(String.valueOf(System.currentTimeMillis()))
						.substring(0, 15);
		String rsp = "";

		// create sign
		StringBuffer sign = new StringBuffer("");
		long expired = System.currentTimeMillis() / 1000 + 2592000;
		if (FileCloudSign.appSign(Integer.toString(m_appid), m_secret_id,
				m_secret_key, expired, userid, sign) != 0) {
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
			connection.setRequestProperty("Host", "web.video.myqcloud.com");
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

			byte[] endData = ("\r\n--" + BOUNDARY + "\r\n").getBytes();
			out.write(endData);
			byte[] arrTitle = ("Content-Disposition: form-data; name=\"Title\";\r\n\r\n"
					+ title + "\r\n--" + BOUNDARY + "\r\n").getBytes();
			out.write(arrTitle);
			byte[] arrDesc = ("Content-Disposition: form-data; name=\"Desc\";\r\n\r\n"
					+ desc + "\r\n--" + BOUNDARY + "\r\n").getBytes();
			out.write(arrDesc);
			byte[] arrMagicContext = ("Content-Disposition: form-data; name=\"magiccontext\";\r\n\r\n"
					+ magicContext + "\r\n--" + BOUNDARY + "--\r\n").getBytes();
			out.write(arrMagicContext);

			out.flush();
			out.close();

			connection.connect();
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
			result.fileid = jsonObject.getJSONObject("data")
					.getString("fileid");
			if (jsonObject.getJSONObject("data").has("cover_url"))
				result.cover_url = jsonObject.getJSONObject("data").getString(
						"cover_url");
		} catch (JSONException e) {
			return SetError(-1, "json exception, e=" + e.toString());
		}
		return SetError(0, "success");
	}

	/**
	 * Delete 删除视频
	 * @param userid		业务账号,没有填0
	 * @param fileid		视频的唯一标识
	 * @return 				错误码，0为成功
	 */
	public int Delete(String userid, String fileid) {
		String req_url = "http://" + QCLOUD_DOMAIN + "/" + m_appid + "/"
				+ userid + "/" + fileid + "/del";
		String rsp = "";

		// create sign once
		StringBuffer sign = new StringBuffer("");
		if (0 != FileCloudSign.appSignOnce(Integer.toString(m_appid),
				m_secret_id, m_secret_key, userid, fileid, sign)) {
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
	 * Stat 查询视频信息
	 * @param userid		业务账号,没有填0
	 * @param fileid		视频的唯一标识
	 * @param info			返回的视频信息
	 * @return 				错误码，0为成功
	 */
	public int Stat(String userid, String fileid, VideoInfo info) {
		String req_url = "http://" + QCLOUD_DOMAIN + "/" + m_appid + "/"
				+ userid + "/" + fileid;
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
			info.sha = jsonObject.getJSONObject("data").getString("file_sha");
			info.status = jsonObject.getJSONObject("data").getInt(
					"video_status");
			info.status_msg = jsonObject.getJSONObject("data").getString(
					"video_status_msg");

			if (jsonObject.getJSONObject("data").has("video_play_time"))
				info.video_play_time = jsonObject.getJSONObject("data").getInt(
						"video_play_time");
			if (jsonObject.getJSONObject("data").has("video_title"))
				info.video_title = jsonObject.getJSONObject("data").getString(
						"video_title");
			if (jsonObject.getJSONObject("data").has("video_desc"))
				info.video_desc = jsonObject.getJSONObject("data").getString(
						"video_desc");
			if (jsonObject.getJSONObject("data").has("video_cover_url"))
				info.video_cover_url = jsonObject.getJSONObject("data")
						.getString("video_cover_url");
		} catch (JSONException e) {
			return SetError(-1, "json exception, e=" + e.toString());
		}
		return SetError(0, "success");
	}

	/**
	 * SliceUpload 分片上传视频，适用于较大视频，一般文件大于20M或使用直接上传超时时使用
	 * @param userid		业务账号,没有填0
	 * @param fileName		上传的文件名
	 * @param title			视频标题
	 * @param desc			视频描述
	 * @param magicContext	透传字段，需要业务在管理控制台设置回调url
	 * @param result		返回的视频的上传信息
	 * @return 				错误码，0为成功
	 */
	public int SliceUpload(String userid, String fileName, String title,
			String desc, String magicContext, UploadResult result) {
		String req_url = "http://" + QCLOUD_DOMAIN + "/" + m_appid + "/"
				+ userid;
		String BOUNDARY = "---------------------------"
				+ MD5.stringToMD5(String.valueOf(System.currentTimeMillis()))
						.substring(0, 15);
		String rsp = "";
		long fileSize = 0;
		File file;
		if (fileName != null) {
			file = new File(fileName);
			if (file.exists() && file.isFile()) {
				fileSize = file.length();
			} else {
				SetError(-2, "file doesn't exist or is not a file");
			}
		} else {
			SetError(-2, "file doesn't exist or is not a file");
		}

		// create sign
		StringBuffer sign = new StringBuffer("");
		long expired = System.currentTimeMillis() / 1000 + 2592000;
		if (FileCloudSign.appSign(Integer.toString(m_appid), m_secret_id,
				m_secret_key, expired, userid, sign) != 0) {
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
			connection.setRequestProperty("Host", "web.video.myqcloud.com");
			connection.setRequestProperty("user-agent", "qcloud-java-sdk");
			connection.setRequestProperty("Authorization", qcloud_sign);

			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setRequestProperty("Content-Type",
					"multipart/form-data; boundary=" + BOUNDARY);

			OutputStream out = new DataOutputStream(
					connection.getOutputStream());

			byte[] arrOp = ("\r\n--"
					+ BOUNDARY
					+ "\r\n"
					+ "Content-Disposition: form-data; name=\"op\";\r\n\r\nupload_slice"
					+ "\r\n--" + BOUNDARY + "\r\n").getBytes();
			out.write(arrOp);
			byte[] arrFilesize = ("Content-Disposition: form-data; name=\"filesize\";\r\n\r\n"
					+ fileSize + "\r\n--" + BOUNDARY + "\r\n").getBytes();
			out.write(arrFilesize);
			byte[] arrTitle = ("Content-Disposition: form-data; name=\"Title\";\r\n\r\n"
					+ title + "\r\n--" + BOUNDARY + "\r\n").getBytes();
			out.write(arrTitle);
			byte[] arrDesc = ("Content-Disposition: form-data; name=\"Desc\";\r\n\r\n"
					+ desc + "\r\n--" + BOUNDARY + "\r\n").getBytes();
			out.write(arrDesc);
			byte[] arrMagicContext = ("Content-Disposition: form-data; name=\"magiccontext\";\r\n\r\n"
					+ magicContext + "\r\n--" + BOUNDARY + "--\r\n").getBytes();
			out.write(arrMagicContext);

			out.flush();
			out.close();

			connection.connect();
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

			if (jsonObject.getJSONObject("data").has("url"))
				result.url = jsonObject.getJSONObject("data").getString("url");
			if (jsonObject.getJSONObject("data").has("download_url"))
				result.download_url = jsonObject.getJSONObject("data")
						.getString("download_url");
			if (jsonObject.getJSONObject("data").has("fileid"))
				result.fileid = jsonObject.getJSONObject("data").getString(
						"fileid");
			if (jsonObject.getJSONObject("data").has("cover_url"))
				result.cover_url = jsonObject.getJSONObject("data").getString(
						"cover_url");

			if (result.url.length() <= 0 || result.download_url.length() <= 0
					|| result.fileid.length() <= 0) {
				String session = jsonObject.getJSONObject("data").getString(
						"session");
				int offset = jsonObject.getJSONObject("data").getInt("offset");
				int sliceSize = jsonObject.getJSONObject("data").getInt(
						"slice_size");
				File upload_file = new File(fileName);
				DataInputStream ins = new DataInputStream(new FileInputStream(
						upload_file));
				int bytes = 0;
				byte[] bufferOut = new byte[sliceSize];
				while ((bytes = ins.read(bufferOut)) != -1) {
					int ret = UploadData(userid, bufferOut, bytes, session,
							offset, result);
					if (ret != 1) {
						return ret;
					}
					offset += bytes;
				}
			} else {
				return SetError(0, "success");
			}
		} catch (JSONException e) {
			return SetError(-1, "json exception, e=" + e.toString());
		} catch (Exception e) {
			return SetError(-1, "url exception, e=" + e.toString());
		}
		return SetError(0, "success");
	}

	/**
	 * UploadData 			上传分片数据
	 * @param userid	 	业务账号,没有填0
	 * @param data			上传的二进制流
	 * @param size			data大小
	 * @param session		上传文件的唯一标识
	 * @param offset		次分片在整个文件中的偏移量
	 * @param result		返回的视频的上传信息(同session文件都上传完毕，才会有值)
	 * @return 错误码，0为分片上传成功且整个文件上传完毕，1位分片上传成功，其余失败
	 */
	public int UploadData(String userid, byte[] data, int size, String session,
			int offset, UploadResult result) {
		String req_url = "http://" + QCLOUD_DOMAIN + "/" + m_appid + "/"
				+ userid;
		String BOUNDARY = "---------------------------"
				+ MD5.stringToMD5(String.valueOf(System.currentTimeMillis()))
						.substring(0, 15);
		String rsp = "";

		// create sign
		StringBuffer sign = new StringBuffer("");
		long expired = System.currentTimeMillis() / 1000 + 2592000;
		if (FileCloudSign.appSign(Integer.toString(m_appid), m_secret_id,
				m_secret_key, expired, userid, sign) != 0) {
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
			connection.setRequestProperty("Host", "web.video.myqcloud.com");
			connection.setRequestProperty("user-agent", "qcloud-java-sdk");
			connection.setRequestProperty("Authorization", qcloud_sign);

			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setRequestProperty("Content-Type",
					"multipart/form-data; boundary=" + BOUNDARY);

			OutputStream out = new DataOutputStream(
					connection.getOutputStream());
			StringBuilder strBuf = new StringBuilder();

			strBuf.append("\r\n").append("--").append(BOUNDARY).append("\r\n");
			strBuf.append("Content-Disposition: form-data; name=\"FileContent\";\r\n\r\n");

			out.write(strBuf.toString().getBytes());
			out.write(data, 0, size);
			byte[] endData = ("\r\n--" + BOUNDARY + "\r\n").getBytes();
			out.write(endData);

			byte[] arrOp = ("Content-Disposition: form-data; name=\"op\";\r\n\r\nupload_slice"
					+ "\r\n--" + BOUNDARY + "\r\n").getBytes();
			out.write(arrOp);
			byte[] arrSession = ("Content-Disposition: form-data; name=\"session\";\r\n\r\n"
					+ session + "\r\n--" + BOUNDARY + "\r\n").getBytes();
			out.write(arrSession);
			byte[] arrOffect = ("Content-Disposition: form-data; name=\"offset\";\r\n\r\n"
					+ offset + "\r\n--" + BOUNDARY + "--\r\n").getBytes();
			out.write(arrOffect);

			out.flush();
			out.close();

			connection.connect();
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

			if (jsonObject.getJSONObject("data").has("url"))
				result.url = jsonObject.getJSONObject("data").getString("url");
			if (jsonObject.getJSONObject("data").has("download_url"))
				result.download_url = jsonObject.getJSONObject("data")
						.getString("download_url");
			if (jsonObject.getJSONObject("data").has("fileid"))
				result.fileid = jsonObject.getJSONObject("data").getString(
						"fileid");
			if (jsonObject.getJSONObject("data").has("cover_url"))
				result.cover_url = jsonObject.getJSONObject("data").getString(
						"cover_url");

			if (result.url.length() <= 0 || result.download_url.length() <= 0
					|| result.fileid.length() <= 0) {
				return SetError(1, "success");
			} else {
				return SetError(0, "success");
			}
		} catch (JSONException e) {
			return SetError(-1, "json exception, e=" + e.toString());
		}
	}
};
