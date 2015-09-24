qcloud-java-sdk
===================================
简介
----------------------------------- 
java sdk for picture service of tencentyun.

	maven信息：
	GroupId： com.qcloud
	ArtifactId：qcloud-sdk

How to start
----------------------------------- 
### 1. 在[腾讯云](http://app.qcloud.com) 申请业务的授权
授权包括：
		
	APP_ID 
	SECRET_ID
	SECRET_KEY
2.0版本的云服务在使用前，还需要先创建空间。在使用2.0 api时，需要使用空间名（Bucket）。

### 2. 创建对应操作类的对象
如果要使用图片，需要创建图片操作类对象
		
	//v1版本	
	PicCloud pc = new PicCloud(APP_ID, SECRET_ID, SECRET_KEY);
	//v2版本
	PicCloud pc = new PicCloud(APP_ID, SECRET_ID, SECRET_KEY, Bucket);

### 3. 调用对应的方法
在创建完对象后，根据实际需求，调用对应的操作方法就可以了。sdk提供的方法包括：签名计算、上传、复制、查询、下载和删除等。
#### 获得版本信息
		
	String version = pc.getVersion();

#### 上传数据
如果需要上传图片，根据不同的需求，可以选择不同的上传方法
			
	//UploadResult是上传的返回结果
	UploadResult result = new UploadResult();
	//////////////////////////////////////////////////
	//如果希望使用文件上传
	//1. 最简单的上传接口
	int ret = upload(fileName, result);
	//2. 可以自定义fileid的上传接口
	int ret = upload(fileName, fileid, result);

	//////////////////////////////////////////////////
	//如果希望使用流式上传
	//1. 最简单的上传接口
	int ret = upload(inputStream, result);
	//2. 可以自定义fileid的上传接口
	int ret = upload(inputStream, fileid, result);

#### 复制图片
		
	UploadResult result = new UploadResult();
	int ret = pc.copy(fileid, result);

#### 查询图片
		
	//图片查询
	PicInfo picInfo = new PicInfo();	
	int ret = pc.stat(fileid, picInfo);

#### 删除图片
		
	ret = pc.delete(fileid);

#### 下载图片
下载图片直接利用图片的下载url即可。
如果开启了防盗链，还需要在下载url后面追加签名，请参考腾讯云的wiki页，熟悉鉴权签名的算法。

Demo
----------------------------------- 
请参考src/main/java/Demo.java

版本信息
----------------------------------- 
### v2.1.1
上传返回结果里增加原图的width和height。

### v2.1.0
重新规范化sdk代码，不兼容以前版本。
重载upload接口，增加nputStream的输入方式。
删除已经不再使用的视频api。

### v2.0.4
对fileid urlencode,用于对特殊字符可能的支持。

### v2.0.3
加入commons-codec包，不再自行base64。
去掉userid（图片服务已经强制为0）。

### v2.0.2
修复jar包打包的问题。
改用jdk1.6编译。

### v2.0.1
修复fileid为null的bug

### v2.0.0
支持2.0版本的图片restful api。内部实现了高度封装，对开发者透明。

### v1.2.1
增加视频分片上传功能，较大视频可使用分片上传。

### v1.2.0
稳定版本，支持微视频的基本api。
包括视频的上传、下载、查询和删除。

### v1.0.0
稳定版本，支持图片云的基本api。
包括图片的上传、下载、复制、查询和删除。

### v0.0.1
初始非稳定版本，仅支持上传。后续将会继续完善。


