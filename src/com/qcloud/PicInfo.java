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
public class PicInfo {
	public String url; // 下载url
	public String fileid; // 图片资源的唯一标识
	public int upload_time; // 图片上传时间，unix时间戳
	public int size; // 图片大小，单位byte
	public String md5; // 图片md5
	public int width;
	public int height;

	public PicInfo() {
		url = "";
		fileid = "";
		upload_time = 0;
		size = 0;
		md5 = "";
		width = 0;
		height = 0;
	}

	public void Print() {
		System.out.println("url = " + url);
		System.out.println("fileid = " + fileid);
		System.out.println("upload_time = " + upload_time);
		System.out.println("size = " + size);
		System.out.println("md5 = " + md5);
		System.out.println("width = " + width);
		System.out.println("height = " + height);
	}
};