/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.qcloud;
/**
 *
 * @author jusisli
 */
public class VideoInfo
{
	public String url;         	 	//下载url
	public String fileid;       	//视频资源的唯一标识
    public int upload_time;     	//视频上传时间，unix时间戳
    public int size;            	//视频大小，单位byte
    public String sha;          	//视频sha1摘要
    public int status;          	//视频状态码			0-初始化, 1-转码中, 2-转码结束,3-转码失败,4-未审核,5-审核通过,6-审核未通过,7-审核失败
	public String status_msg;   	//视频状态字符串
	public int video_play_time;		//视频视频播放时长,只有使用视频转码的业务才有
	public String video_title;		//视频标题
	public String video_desc;		//视频描述
	public String video_cover_url;	//视频封面url,只有使用视频转码的业务才会有封面
        
	public VideoInfo()
	{
            url = "";
            fileid = "";
            upload_time = 0;
            size = 0;
            sha = "";
            status = 0;
            status_msg = "";
            
            video_play_time = 0;
            video_title = "";
            video_desc = "";
            video_cover_url = "";
	}
        
        public void Print(){
            System.out.println("url = "+url);
            System.out.println("fileid = "+fileid);
            System.out.println("upload_time = "+upload_time);
            System.out.println("size = "+size);
            System.out.println("sha = "+sha);
            System.out.println("status = "+status);
            System.out.println("status_msg = "+status_msg);
			System.out.println("video_play_time = "+video_play_time);
            System.out.println("video_title = "+video_title);
            System.out.println("video_desc = "+video_desc);
            System.out.println("video_cover_url = "+video_cover_url);
        }
};