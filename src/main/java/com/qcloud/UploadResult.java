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
	public String downloadUrl; // 下载url
	public String fileId; // 资源的唯一标识
	public String coverUrl; // 视频资源的封面url，只有转码过的视频才有，默认是黑屏后的第一帧
        public PicAnalyze analyze; //图片分析的结果

	public UploadResult() {
		url = "";
		downloadUrl = "";
		fileId = "";
		coverUrl = "";
                analyze = new PicAnalyze();
	}

	public void print() {
		System.out.println("url = " + url);
		System.out.println("downloadUrl = " + downloadUrl);
		System.out.println("fileId = " + fileId);
		if (coverUrl.length() > 0) {
			System.out.println("coverUrl = " + coverUrl);
		}
	}
};
