# java-sdk
java sdk for picture service of tencentyun.

v1.0.0
稳定版本，支持图片云的基本api。
包括图片的上传、下载、复制、查询和删除。

v0.0.1
初始非稳定版本，仅支持上传。后续将会继续完善。

使用说明：
1. 在万象（腾讯图片云）官网申请业务的授权
授权包括：
	APP_ID 
	SECRET_ID
	SECRET_KEY

2. 创建PicCloud对象
	PicCloud pc = new PicCloud(APP_ID, SECRET_ID, SECRET_KEY);

3. 调用对应的方法
	上传
		UploadResult result = new UploadResult();
		int ret = pc.Upload(userid, pic, result);
		if(ret == 0){
			System.out.println("upload pic success, url=" + result.url + " download_url="+result.download_url);
		}else{
			System.out.println("upload pic error, error code="+ret);
		}
		
	复制
		UploadResult result = new UploadResult();
		int ret = pc.Copy(userid, fileid, result);
                if(ret == 0){
                    System.out.println("copy pic success");
                    result.Print();
                }else{
                    System.out.println("copy pic error, error="+pc.GetError());
		}

