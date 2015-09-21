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
	public String fileId; // 图片资源的唯一标识
	public int uploadTime; // 图片上传时间，unix时间戳
	public int size; // 图片大小，单位byte
	public String md5; // 图片md5
	public int width;
	public int height;

	public PicInfo() {
		url = "";
		fileId = "";
		uploadTime = 0;
		size = 0;
		md5 = "";
		width = 0;
		height = 0;
	}

	public void print() {
		System.out.println("url = " + url);
		System.out.println("fileId = " + fileId);
		System.out.println("uploadTime = " + uploadTime);
		System.out.println("size = " + size);
		System.out.println("md5 = " + md5);
		System.out.println("width = " + width);
		System.out.println("height = " + height);
	}
};