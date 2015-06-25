import com.qcloud.*;

public class Demo {
	// appid, access id, access key璇峰埌http://app.qcloud.com娉ㄥ唽
	// 涓嬮潰鏄痙emo娴嬭瘯鐢ㄧ殑appid锛屼笉鍙綔涓哄晢涓氱敤閫�
	public static final int APP_ID = 200899;
	public static final String SECRET_ID = "AKIDXZE8z7kUBlltXgfjb8NgrgChrpTiiVNo";
	public static final String SECRET_KEY = "8W0dbC201JgEl8XPYTBFu0ulUxiNnuYv";

	public static void main(String[] args) {
		pic_test("D:/test.jpg");
		video_test("D:/2M.MOV");
	}

	public static void pic_test(String pic) {
		PicCloud pc = new PicCloud(APP_ID, SECRET_ID, SECRET_KEY);
		String userid = "123456";
		String url = "";
		String download_url = "";
		UploadResult result = new UploadResult();
		UploadResult result2 = new UploadResult();
		PicInfo info = new PicInfo();
                PicAnalyze flag = new PicAnalyze();

		// 涓婁紶涓�寮犲浘鐗�
		System.out.println("======================================================");
		int ret = pc.Upload(userid, pic, flag, result);
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
		ret = pc.DownloadEx(userid, result.fileid, "./download.jpg");
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
		System.out.println("======================================================");
		ret = pc.Delete(userid, result.fileid);
		if (ret == 0) {
			System.out.println("delete pic success");
		} else {
			System.out.println("delete pic error, error=" + pc.GetError());
		}
                
                //test fuzzy and food
                System.out.println("======================================================");
                flag.fuzzy = 1;
                flag.food = 1;
                ret = pc.Upload(userid, pic, flag, result);
		if (ret == 0) {
			System.out.println("analyze pic success");
                        System.out.println("is fuzzy =" + result.analyze.fuzzy);
                        System.out.println("is food =" + result.analyze.food);
		} else {
			System.out.println("analyze pic error, error=" + pc.GetError());
		}
	}

	public static void video_test(String strFilePath) {
		VideoCloud vc = new VideoCloud(APP_ID, SECRET_ID, SECRET_KEY);
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
