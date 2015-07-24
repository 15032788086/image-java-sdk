import com.qcloud.*;
import com.qcloud.sign.*;

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

	public static void main(String[] args) {
            //v1版本api的demo
            picV1_test("D:/test.jpg");
            //v2版本api的demo
            //picV2_test("D:/test.jpg");
            //视频api的demo
            //video_test("D:/2M.MOV");
	}
        
        public static void sign_test(){
            final String appid = "10001479";
		final String bucket = "hudie";
		final String secretid = "AKIDPC3BSSVrTtcqPSgU471lUqEL98x9uWy3";
		final String secretkey = "igjJ4aZbZJnBBwxVZASnmUP95EcTFPqd";

             StringBuffer sign = new StringBuffer("");
            FileCloudSign.appSignV2(appid, secretid, secretkey, "", 3600, "0", sign);
            System.out.println(sign.toString());
            
        }
        
        public static void picV1_test(String pic){
            PicCloud pc = new PicCloud(APP_ID_V1, SECRET_ID_V1, SECRET_KEY_V1);
            pic_base(pc, pic);
        }
        
        public static void picV2_test(String pic){
            PicCloud pc = new PicCloud(APP_ID_V2, SECRET_ID_V2, SECRET_KEY_V2, BUCKET);
            pic_base(pc, pic);
        }

	public static void pic_base(PicCloud pc, String pic) {
		String userid = "1234567";
		String url = "";
		String download_url = "";
		UploadResult result = new UploadResult();
		UploadResult result2 = new UploadResult();
		PicInfo info = new PicInfo();

		// 涓婁紶涓�寮犲浘鐗�
		System.out.println("======================================================");
		int ret = pc.Upload(userid, pic, result);
		if (ret == 0) {
			System.out.println("upload pic success");
			result.Print();
		} else {
			System.out.println("upload pic error, error=" + pc.GetError());
		}

		System.out.println("======================================================");
		// 鏍规嵁鏄惁寮�鍚槻鐩楅摼锛岄�夋嫨姝ｇ‘鐨勪笅杞芥柟娉�
		// 涓嶅紑鍚槻鐩楅摼
		// ret = pc.Download(userid, result.fileid, "./download.jpg");
		// ret = pc.Download(result.download_url, "./download.jpg");
		// 寮�鍚槻鐩楅摼
		ret = pc.DownloadEx(userid, result.fileid, "./a.jpg");
		if (ret == 0) {
			System.out.println("download pic success");
		} else {
			System.out.println("download pic error, error=" + pc.GetError());
		}

		// 鏌ヨ鍥剧墖鐘舵��
		System.out.println("======================================================");
		ret = pc.Stat(userid, result.fileid, info);
		if (ret == 0) {
			System.out.println("Stat pic success");
			info.Print();
		} else {
			System.out.println("Stat pic error, error=" + pc.GetError());
		}

		// 澶嶅埗鍥剧墖
		System.out.println("======================================================");
		ret = pc.Copy(userid, result.fileid, result2);
		if (ret == 0) {
			System.out.println("copy pic success");
			result2.Print();
		} else {
			System.out.println("copy pic error, error=" + pc.GetError());
		}

		// 鏌ヨ澶嶅埗鐨勫浘鐗囩殑鐘舵��
		System.out.println("======================================================");
		ret = pc.Stat(userid, result.fileid, info);
		if (ret == 0) {
			System.out.println("Stat pic success");
			info.Print();
		} else {
			System.out.println("Stat pic error, error=" + pc.GetError());
		}

		// 鍒犻櫎鍥剧墖
		/*System.out.println("======================================================");
		ret = pc.Delete(userid, result.fileid);
		if (ret == 0) {
			System.out.println("delete pic success");
		} else {
			System.out.println("delete pic error, error=" + pc.GetError());
		}
                */
                
                //test fuzzy and food
                System.out.println("======================================================");
                PicAnalyze flag = new PicAnalyze();
                flag.fuzzy = 1;
                flag.food = 1;
                ret = pc.Upload(userid, pic, "", flag, result);
		if (ret == 0) {
			System.out.println("analyze pic success");
                        System.out.println("is fuzzy =" + result.analyze.fuzzy);
                        System.out.println("is food =" + result.analyze.food);
		} else {
			System.out.println("analyze pic error, error=" + pc.GetError());
		}
	}

	public static void video_test(String strFilePath) {
		VideoCloud vc = new VideoCloud(APP_ID_V1, SECRET_ID_V1, SECRET_KEY_V1);
		String userid = "123456";
		String video = strFilePath;
		String url = "";
		String download_url = "";
		UploadResult result = new UploadResult();

		VideoInfo info = new VideoInfo();

		// 涓婁紶涓�寮犲浘鐗�
		System.out.println("======================================================");
		//视频直接上传，适用于较小视频
		int ret = vc.Upload(userid, video, "title", "desc", "text", result);
		//视频分片上传，适用于较大视频
		//int ret = vc.SliceUpload(userid, video, "title", "desc", "text", result);
		if (ret == 0) {
			System.out.println("upload video success");
			result.Print();
		} else {
			System.out.println("upload video error, error=" + vc.GetError());
		}

		System.out
				.println("======================================================");
		// 鏌ヨ鍥剧墖鐘舵��
		System.out
				.println("======================================================");
		ret = vc.Stat(userid, result.fileid, info);
		if (ret == 0) {
			System.out.println("Stat video success");
			info.Print();
		} else {
			System.out.println("Stat video error, error=" + vc.GetError());
		}

		// 鏌ヨ澶嶅埗鐨勫浘鐗囩殑鐘舵��
		System.out.println("======================================================");
		ret = vc.Stat(userid, result.fileid, info);
		if (ret == 0) {
			System.out.println("Stat video success");
			info.Print();
		} else {
			System.out.println("Stat video error, error=" + vc.GetError());
		}

		// 鍒犻櫎鍥剧墖
		System.out
				.println("======================================================");
		// ret = vc.Delete(userid, result.fileid);
		if (ret == 0) {
			System.out.println("delete video success");
		} else {
			System.out.println("delete video error, error=" + vc.GetError());
		}
	}
}
