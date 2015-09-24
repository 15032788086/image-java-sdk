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
        public int width;
        public int height;
        public PicAnalyze analyze; //图片分析的结果

	public UploadResult() {
		url = "";
		downloadUrl = "";
		fileId = "";
                width = 0;
                height = 0;
                analyze = new PicAnalyze();
	}

	public void print() {
		System.out.println("url = " + url);
		System.out.println("downloadUrl = " + downloadUrl);
		System.out.println("fileId = " + fileId);
                System.out.println("width = " + width);
                System.out.println("height = " + height);
	}
};
