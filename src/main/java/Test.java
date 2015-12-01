import com.qcloud.*;
import java.io.*;

public class Test {
	// appid, access id, access key请去http://app.qcloud.com申请使用
	// 下面的的demo代码请使用自己的appid�
//	public static final int APP_ID_V1 = 201437;
//	public static final String SECRET_ID_V1 = "AKIDblLJilpRRd7k3ioCHe5JGmSsPvf1uHOf";
//	public static final String SECRET_KEY_V1 = "6YvZEJEkTGmXrtqnuFgjrgwBpauzENFG";
//        
//        public static final int APP_ID_V2 = 10000001;
//	public static final String SECRET_ID_V2 = "AKIDNZwDVhbRtdGkMZQfWgl2Gnn1dhXs95C0";
//	public static final String SECRET_KEY_V2 = "ZDdyyRLCLv1TkeYOl5OCMLbyH4sJ40wp";
//        public static final String BUCKET = "testb";        //空间名
    
    	public static final int APP_ID_V1 = 201437;
	public static final String SECRET_ID_V1 = "AKIDblLJilpRRd7k3ioCHe5JGmSsPvf1uHOf";
	public static final String SECRET_KEY_V1 = "6YvZEJEkTGmXrtqnuFgjrgwBpauzENFG";
        
        public static final int APP_ID_V2 = 10000956;
	public static final String SECRET_ID_V2 = "AKID1yakE7O1hxNVELIyrCs7ejEOq1hHcGYN";
	public static final String SECRET_KEY_V2 = "RErIXg9yXQex5MPV8aY89kBEqYDePOw9";
        public static final String BUCKET = "dogdog";        //空间名
        
        public static final String TEST_URL = "http://b.hiphotos.baidu.com/image/pic/item/8ad4b31c8701a18b1efd50a89a2f07082938fec7.jpg";
        
	public static void main(String[] args) throws Exception {
            //sign_test();
            //v1版本api的demo
            //picV1Test("D:/sss.jpg");
            //v2版本api的demo
            picV2Test("D:/original.jpg");
            //分片上传
            sliceUpload("D:/original.jpg");
            //黄图识别服务
            pornTest(TEST_URL);
           
	}
        
        public static void signTest() {
            PicCloud pc = new PicCloud(APP_ID_V2, SECRET_ID_V2, SECRET_KEY_V2, BUCKET);
            long expired = System.currentTimeMillis() / 1000 + 3600;
            String sign = pc.getSign(expired);
            System.out.println("sign="+sign);
            
        }
        
        public static void picV1Test(String pic) throws Exception {
            PicCloud pc = new PicCloud(APP_ID_V1, SECRET_ID_V1, SECRET_KEY_V1);
            picBase(pc, pic);
        }
        
        public static void picV2Test(String pic) throws Exception {
            PicCloud pc = new PicCloud(APP_ID_V2, SECRET_ID_V2, SECRET_KEY_V2, BUCKET);
            picBase(pc, pic);
        }

	public static void picBase(PicCloud pc, String pic) throws Exception {
		String url = "";
		String downloadUrl = "";

		// 上传一张图片�
		System.out.println("======================================================");
		UploadResult uInfo = pc.upload(pic);
		if (uInfo != null) {
			System.out.println("upload pic success");
			uInfo.print();
		} else {
			System.out.println("upload pic error, error=" + pc.getError());
		}

                FileInputStream fileStream = new FileInputStream(pic);
                uInfo = pc.upload(fileStream);
		if (uInfo != null) {
			System.out.println("upload pic2 success");
			uInfo.print();
		} else {
			System.out.println("upload pic2 error, error=" + pc.getError());
		}
                  
                FileInputStream fileStream2 = new FileInputStream(pic);
                byte[] data = new byte[fileStream2.available()];
                fileStream2.read(data);
                ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
                uInfo = pc.upload(inputStream);
		if (uInfo != null) {
			System.out.println("upload pic3 success");
			uInfo.print();
		} else {
			System.out.println("upload pic3 error, error=" + pc.getError());
		}

		// 查询图片的状态��
		System.out.println("======================================================");
		PicInfo pInfo = pc.stat(uInfo.fileId);
		if (pInfo != null) {
			System.out.println("Stat pic success");
			pInfo.print();
		} else {
			System.out.println("Stat pic error, error=" + pc.getError());
		}

		// 复制一张图片
		System.out.println("======================================================");
		uInfo = pc.copy(uInfo.fileId);
		if (uInfo != null) {
			System.out.println("copy pic success");
			uInfo.print();
		} else {
			System.out.println("copy pic error, error=" + pc.getError());
		}     

		// 删除一张图片
		System.out.println("======================================================");
		int ret = pc.delete(uInfo.fileId);
		if (ret == 0) {
			System.out.println("delete pic success");
		} else {
			System.out.println("delete pic error, error=" + pc.getError());
		} 
	}
        
        public static void sliceUpload(String url){
            PicCloud pc = new PicCloud(APP_ID_V2, SECRET_ID_V2, SECRET_KEY_V2, BUCKET);
            SliceUploadInfo info = pc.simpleUploadSlice(url, 8*1024);
            if (info != null) {
                System.out.println("slice upload pic success");
		info.print();
            } else {
		System.out.println("slice upload pic error, error=" + pc.getError());
            }
        }
        
        public static void pornTest(String url){
            PicCloud pc = new PicCloud(APP_ID_V2, SECRET_ID_V2, SECRET_KEY_V2, BUCKET);
            PornDetectInfo info = pc.pornDetect(url);
            if (info != null) {
                System.out.println("detect porn pic success");
		info.print();
            } else {
		System.out.println("detect porn pic error, error=" + pc.getError());
            } 
        }
}
