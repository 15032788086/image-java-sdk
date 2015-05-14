import com.qcloud.*;

public class Demo {
	//appid, access id, access key请到http://app.qcloud.com注册
        //下面是demo测试用的appid，不可作为商业用途
	public static final int APP_ID = 299201;
	public static final String SECRET_ID = "AKIDAsqjH35AoJNmzjB3lfVUIHLDMB18cXG8";
	public static final String SECRET_KEY = "acipl32U6bL7zUN99VXGLEYy9y8t0LRs";
	
	public static void main(String[] args) {
		PicCloud pc = new PicCloud(APP_ID, SECRET_ID, SECRET_KEY);
		int userid = 123456;
		String pic = "./test.jpg";		
		String url = "";
		String download_url = "";
		UploadResult result = new UploadResult();
                UploadResult result2 = new UploadResult();
                PicInfo info = new PicInfo();
                
                //上传一张图片
                System.out.println("======================================================");
		int ret = pc.Upload(userid, pic, result);
		if(ret == 0){
                    System.out.println("upload pic success");
                    result.Print();
		}else{
                    System.out.println("upload pic error, error="+pc.GetError());
		}
                
                System.out.println("======================================================");
                //根据是否开启防盗链，选择正确的下载方法
                //不开启防盗链
                //ret = pc.Download(userid, result.fileid, "./download.jpg");
                //ret = pc.Download(result.download_url, "./download.jpg");
                //开启防盗链
                ret = pc.DownloadEx(userid, result.fileid, "./download.jpg");
                if(ret == 0){
                    System.out.println("download pic success");
		}else{
                    System.out.println("download pic error, error="+pc.GetError());
		}
                
                //查询图片状态
                System.out.println("======================================================");
                ret = pc.Stat(userid, result.fileid, info);
                if(ret == 0){
                    System.out.println("Stat pic success");
                    info.Print();
                }else{
                    System.out.println("Stat pic error, error="+pc.GetError());
		}
                
                //复制图片
                System.out.println("======================================================");
                ret = pc.Copy(userid, result.fileid, result2);
                if(ret == 0){
                    System.out.println("copy pic success");
                    result2.Print();
                }else{
                    System.out.println("copy pic error, error="+pc.GetError());
		}   
                
                //查询复制的图片的状态
                System.out.println("======================================================");
                ret = pc.Stat(userid, result.fileid, info);
                if(ret == 0){
                    System.out.println("Stat pic success");
                    info.Print();
                }else{
                    System.out.println("Stat pic error, error="+pc.GetError());
		}
                
                //删除图片
                System.out.println("======================================================");
		ret = pc.Delete(userid, result.fileid);
                if(ret == 0){
                    System.out.println("delete pic success");
                }else{
                    System.out.println("delete pic error, error="+pc.GetError());
		}
	}
}

