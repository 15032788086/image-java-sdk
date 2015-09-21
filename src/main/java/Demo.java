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
        public static final String BUCKET = "testa";        //空间名

	public static void main(String[] args) throws Exception {
            //sign_test();
            //v1版本api的demo
            //apiV1Demo("D:/sss.jpg");
            //v2版本api的demo
            apiV2Demo("D:/test.jpg");
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
		String url = "";
		String downloadUrl = "";
		UploadResult result = new UploadResult();
		UploadResult result2 = new UploadResult();
		PicInfo info = new PicInfo();

		// 上传一张图片�
                //1. 直接指定图片文件名的方式
		int ret = pc.upload(pic, result);
                //2. 文件流的方式
                FileInputStream fileStream = new FileInputStream(pic);
                ret = pc.upload(fileStream, result);
                //3. 字节流的方式
                //ByteArrayInputStream inputStream = new ByteArrayInputStream(你自己的参数);
                //ret = pc.upload(inputStream, result);
		// 查询图片的状态��
		ret = pc.stat(result.fileId, info);
		// 复制一张图片
		ret = pc.copy(result.fileId, result2);
		// 删除一张图片
		//ret = pc.delete(result.fileId);
	}
}
