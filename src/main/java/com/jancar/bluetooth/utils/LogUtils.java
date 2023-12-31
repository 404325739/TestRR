package com.jancar.bluetooth.utils;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Log不光作为日志的打印，还可以记录日志 ——> File
 */
public class LogUtils {

	private static SimpleDateFormat mSimpleDateFormat =
			new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public static void i(String text) {
		if (!TextUtils.isEmpty(text)) {
			Log.i("APP_BT", text);
			//writeToFile(text);
		}
	}

	public static void e(String text) {
		if (!TextUtils.isEmpty(text)) {
			Log.e("APP_BT", text);
			//writeToFile(text);
		}
	}

	public static void callStackPrint() {
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		Log.d("BtLog", "callStackPrint: ===========>>>>>>");
		for (int i = 3, j = 0; i < stackTrace.length && j < 4; i++, j++) {
			StackTraceElement ste = stackTrace[i];
			Log.d("BtLog", String.format("%s(%d):%s", ste.getFileName(),
					ste.getLineNumber(), ste.getMethodName()));
		}
		Log.d("BtLog", "callStackPrint: ===========<<<<<<");
	}

	/**
	 * 写入内存卡
	 *
	 * @param text
	 */
	public static void writeToFile(String text) {
		//开始写入
		FileOutputStream fileOutputStream = null;
		BufferedWriter bufferedWriter = null;
		try {
			//文件路径
			String fileRoot = Environment.getExternalStorageDirectory().getPath() + "/Meet/";
			String fileName = "Meet.log";
			// 时间 + 内容
			String log = mSimpleDateFormat.format(new Date()) + " " + text + "\n";
			//检查父路径
			File fileGroup = new File(fileRoot);
			//创建根布局
			if (!fileGroup.exists()) {
				fileGroup.mkdirs();
			}
			//创建文件
			File fileChild = new File(fileRoot + fileName);
			if (!fileChild.exists()) {
				fileChild.createNewFile();
			}
			fileOutputStream = new FileOutputStream(fileRoot + fileName, true);
			//编码问题 GBK 正确的存入中文
			bufferedWriter = new BufferedWriter(new OutputStreamWriter(fileOutputStream, Charset.forName("gbk")));
			bufferedWriter.write(log);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			e(e.toString());
		} catch (IOException e) {
			e.printStackTrace();
			e(e.toString());
		} finally {
			if (bufferedWriter != null) {
				try {
					bufferedWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
