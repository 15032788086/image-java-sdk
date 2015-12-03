import com.qcloud.*;
import java.io.*;

public class Demo {
	// appid, access id, access key请去http://app.qcloud.com申请使用
	// 下面的的demo代码请使用自己的appid�
	public static final int APP_ID_V1 = 201437;
	public static final String SECRET_ID_V1 = "AKIDblLJilpRRd7k3ioCHe5JGmSsPvf1uHOf";
	public static final String SECRET_KEY_V1 = "6YvZEJEkTGmXrtqnuFgjrgwBpauzENFG";
        
        public static final int APP_ID_V2 = 10000001;
	public static final String SECRET_ID_V2 = "AKIDNZwDVhbRtdGkMZQfWgl2Gnn1dhXs95C0";
	public static final String SECRET_KEY_V2 = "ZDdyyRLCLv1TkeYOl5OCMLbyH4sJ40wp";
        public static final String BUCKET = "testb";        //空间名
        
        public static final String TEST_URL = "http://b.hiphotos.baidu.com/image/pic/item/8ad4b31c8701a18b1efd50a89a2f07082938fec7.jpg";

	public static void main(String[] args) throws Exception {
            //sign_test();
            //v1版本api的demo
            //apiV1Demo("D:/sss.jpg");
            //v2版本api的demo
            apiV2Demo("D:/test.jpg");
            //分片上传
            //sliceUpload("D:/sss.jpg");
            //黄图识别服务demo
            pornDemo(TEST_URL);
	}
        
        public static void signDemo(){
            PicCloud pc = new PicCloud(APP_ID_V2, SECRET_ID_V2, SECRET_KEY_V2, BUCKET);
            long expired = System.currentTimeMillis() / 1000 + 3600;
            String sign = pc.getSign(expired);
            System.out.println("sign="+sign);
            
        }
        
        public static void apiV1Demo(String pic) throws Exception {
            PicCloud pc = new PicCloud(APP_ID_V1, SECRET_ID_V1, SECRET_KEY_V1);
            picBase(pc, pic);
        }
        
        public static void apiV2Demo(String pic) throws Exception {
            PicCloud pc = new PicCloud(APP_ID_V2, SECRET_ID_V2, SECRET_KEY_V2, BUCKET);
            picBase(pc, pic);
        }

	public static void picBase(PicCloud pc, String pic) throws Exception {
		// 上传一张图片�
                //1. 直接指定图片文件名的方式
		UploadResult result = pc.upload(pic);
                if(result != null){
                    result.print(); 
                }
                //2. 文件流的方式
                FileInputStream fileStream = new FileInputStream(pic);
                result = pc.upload(fileStream);
                if(result != null){
                    result.print(); 
                }
                //3. 字节流的方式
                //ByteArrayInputStream inputStream = new ByteArrayInputStream(你自己的参数);
                //ret = pc.upload(inputStream, result);
		// 查询图片的状态��
		PicInfo info = pc.stat(result.fileId);
                if(info != null){
                    info.print(); 
                }
		// 复制一张图片
		result = pc.copy(result.fileId);
		// 删除一张图片
		int ret = pc.delete(result.fileId);
	}
        
        public static void sliceUpload(String url){
            PicCloud pc = new PicCloud(APP_ID_V2, SECRET_ID_V2, SECRET_KEY_V2, BUCKET);
            SliceUploadInfo info = pc.simpleUploadSlice(url, 128*1024);
            if (info != null) {
                System.out.println("slice upload pic success");
		info.print();
            } else {
		System.out.println("slice upload pic error, error=" + pc.getError());
            }
        }
        
        public static void pornDemo(String url){
            PicCloud pc = new PicCloud(APP_ID_V2, SECRET_ID_V2, SECRET_KEY_V2, BUCKET);
            PornDetectInfo info = pc.pornDetect(url);
            info.print();
        }
}
