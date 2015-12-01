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
public class SliceUploadInfo extends UploadResult {
        public String reqUrl;           //上传使用的url
        public String sign;             //上传使用的签名
        public String session;          //唯一标识此文件传输过程的 id
        public int fileSize;    
        public int offset;              //传输的文件位移
        public int sliceSize;           //分片大小
        public boolean finishFlag;       //是否命中秒传

	public SliceUploadInfo() {
                reqUrl = "";
                sign = "";
		session = "";
                fileSize = 0;
		offset = 0;
		sliceSize = 0;
                finishFlag = false;
	}
        
        public SliceUploadInfo copy(){
            SliceUploadInfo newInfo = new SliceUploadInfo();
            
            newInfo.reqUrl = reqUrl;
            newInfo.sign = sign;
            newInfo.session = session;
            newInfo.sliceSize = sliceSize;
            newInfo.fileSize = fileSize;
            newInfo.offset = offset;
            newInfo.finishFlag = finishFlag;
            return newInfo;
        }

	public void print() {
                super.print();
		System.out.println("session = " + session);
                System.out.println("fileSize = " + fileSize);
		System.out.println("offset = " + offset);
		System.out.println("sliceSize = " + sliceSize);
	}
};