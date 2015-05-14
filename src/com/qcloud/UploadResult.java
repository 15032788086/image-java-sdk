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
public class UploadResult
{
	public String url;              //资源url
	public String download_url;     //下载url
	public String fileid;           //图片资源的唯一标识
	
	public UploadResult()
	{
		url = "";
		download_url = "";
		fileid = "";
	}
        
        public void Print(){
            System.out.println("url = "+url);
            System.out.println("download_url = "+download_url);
            System.out.println("fileid = "+fileid);
        }
};