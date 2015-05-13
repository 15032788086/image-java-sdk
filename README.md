# java-sdk
java sdk for picture service of tencentyun.

v0.0.1
初始非稳定版本，仅支持上传。后续将会继续完善。

上传示例：


public class Demo {
	//appid, access id, access key请到http://app.qcloud.com注册
	public static final String APP_ID = "299201";
	public static final String SECRET_ID = "AKIDAsqjH35AoJNmzjB3lfVUIHLDMB18cXG8";
	public static final String SECRET_KEY = "acipl32U6bL7zUN99VXGLEYy9y8t0LRs";
	
	public static void main(String[] args) {

		PicCloud pc = new PicCloud(APP_ID, SECRET_ID, SECRET_KEY);
		String userid = "12345";
		String pic = "D:\\sdk\\java\\test.jpg";		
		String url = "";
		String download_url = "";
		UploadResult result = new UploadResult();
		int ret = pc.Upload(userid, pic, result);
		if(ret == 0){
			System.out.println("upload pic success, url=" + result.url + " download_url="+result.download_url);
		}else{
			System.out.println("upload pic error, error code="+ret);
		}
		
	}
}
