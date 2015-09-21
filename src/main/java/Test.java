import com.qcloud.*;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Test {
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
            picV1Test("D:/sss.jpg");
            //v2版本api的demo
            //picV2_test("D:/test.jpg");
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
		UploadResult result = new UploadResult();
		UploadResult result2 = new UploadResult();
		PicInfo info = new PicInfo();

		// 上传一张图片�
		System.out.println("======================================================");
		int ret = pc.upload(pic, result);
		if (ret == 0) {
			System.out.println("upload pic success");
			result.print();
		} else {
			System.out.println("upload pic error, error=" + pc.getError());
		}

                FileInputStream fileStream = new FileInputStream(pic);
                ret = pc.upload(fileStream, result);
		if (ret == 0) {
			System.out.println("upload pic2 success");
			result.print();
		} else {
			System.out.println("upload pic2 error, error=" + pc.getError());
		}
                  
                FileInputStream fileStream2 = new FileInputStream(pic);
                byte[] data = new byte[fileStream2.available()];
                fileStream2.read(data);
                ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
                ret = pc.upload(inputStream, result);
		if (ret == 0) {
			System.out.println("upload pic3 success");
			result.print();
		} else {
			System.out.println("upload pic3 error, error=" + pc.getError());
		}

		// 查询图片的状态��
		System.out.println("======================================================");
		ret = pc.stat(result.fileId, info);
		if (ret == 0) {
			System.out.println("Stat pic success");
			info.print();
		} else {
			System.out.println("Stat pic error, error=" + pc.getError());
		}

		// 复制一张图片
		System.out.println("======================================================");
		ret = pc.copy(result.fileId, result2);
		if (ret == 0) {
			System.out.println("copy pic success");
			result2.print();
		} else {
			System.out.println("copy pic error, error=" + pc.getError());
		}     

		// 删除一张图片
		System.out.println("======================================================");
		//ret = pc.Delete(result.fileid);
		if (ret == 0) {
			System.out.println("delete pic success");
		} else {
			System.out.println("delete pic error, error=" + pc.getError());
		} 
	}
}
