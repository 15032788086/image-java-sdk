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
public class UploadResult {
	public String url; // 资源url
	public String download_url; // 下载url
	public String fileid; // 资源的唯一标识
	public String cover_url; // 视频资源的封面url，只有转码过的视频才有，默认是黑屏后的第一帧

	public UploadResult() {
		url = "";
		download_url = "";
		fileid = "";
		cover_url = "";
	}

	public void Print() {
		System.out.println("url = " + url);
		System.out.println("download_url = " + download_url);
		System.out.println("fileid = " + fileid);
		if (cover_url.length() > 0) {
			System.out.println("cover_url = " + cover_url);
		}
	}
};
