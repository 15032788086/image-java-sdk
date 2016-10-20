# tencentyun/image-java-sdk
----------------------------------- 
腾讯云 [万象优图（Cloud Image）](https://www.qcloud.com/product/ci.html) SDK for Java
===================================
简介
----------------------------------- 
java sdk for picture service of tencentyun.

	maven信息：
	GroupId： com.qcloud
	ArtifactId：qcloud-sdk

	wiki地址
	http://www.qcloud.com/wiki/Java-SDK%E8%AF%B4%E6%98%8E%E6%96%87%E6%A1%A3

How to start
----------------------------------- 
### 1. 在腾讯云申请业务的授权
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

	//////////////////////////////////////////////////
	//分片上传，适用于大图片
	//简单模式，提供分片大小，内部自动分片上传
	SliceUploadInfo info = pc.simpleUploadSlice(url, sliceSize);
	//高级模式，可以多线程操作
	//初始化分片操作
	SliceUploadInfo info = initUploadSlice(fileId, data, fileSize, sliceSize)
	//上传分片
	SliceUploadInfo info = UploadSlice(data, sliceUploadInfo)


#### 复制图片
		
	UploadResult result = new UploadResult();
	int ret = pc.copy(fileid, result);

#### 查询图片
		
	PicInfo picInfo = new PicInfo();	
	int ret = pc.stat(fileid, picInfo);

#### 删除图片
		
	//UploadResult是上传的返回结果
	UploadResult result = new UploadResult();
	ret = pc.delete(fileid);

#### 下载图片
下载图片直接利用图片的下载url即可。
如果开启了防盗链，还需要在下载url后面追加签名，请参考腾讯云的wiki页，熟悉鉴权签名的算法。

#### 黄图识别
    //黄图识别(单个Url)
    String url = "http://b.hiphotos.baidu.com/image/pic/item/8ad4b31c8701a18b1efd50a89a2f07082938fec7.jpg";
    PornDetectInfoData info = pc.pornDetect(url);

    //黄图识别(Url)
    String[] pornUrl = {"http://b.hiphotos.baidu.com/image/pic/item/8ad4b31c8701a18b1efd50a89a2f07082938fec7.jpg",
                        "http://c.hiphotos.baidu.com/image/h%3D200/sign=7b991b465eee3d6d3dc680cb73176d41/96dda144ad3459829813ed730bf431adcaef84b1.jpg"
    };
    ArrayList<PornDetectInfo> info = pc.pornDetectUrl(pornUrl);
    //黄图识别(File)
    String[] pornFile = {"D:\\porn\\test1.jpg",
                         "D:\\porn\\test2.jpg",
                         "..\\..\\..\\..\\..\\porn\\测试.png"
    };
    ArrayList<PornDetectInfo> info = pc.pornDetectFile(pornFile);
	
	//PornDetectInfo是黄图识别的返回结果
	//返回结果的各字段含义，请参考官网wiki文档
	//http://www.qcloud.com/wiki/%E4%B8%87%E8%B1%A1%E4%BC%98%E5%9B%BE%E6%99%BA%E8%83%BD%E9%89%B4%E9%BB%84%E6%96%87%E6%A1%A3	

Demo
----------------------------------- 
请参考src/main/java/Demo.java

版本信息
----------------------------------- 
### v2.1.6
黄图识别新增多图片Url和多图片内容的支持；修复bug一项。

### v2.1.5
修复bug一项。

### v2.1.4
分片上传的接口变化了，适应新的restful api.

### v2.1.3
支持图片分片上传（大图片使用，不建议多线程）。

### v2.1.2
增加对黄图识别api的支持。

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


