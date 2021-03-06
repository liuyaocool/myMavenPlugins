package generator.utils;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.channels.FileChannel;

public class FileUtil {

	private static FileUtil fileUtil;
	public static synchronized FileUtil getInstance(){
		if (null == fileUtil) fileUtil = new FileUtil();
		return fileUtil;
	}
	private FileUtil(){}

	public String getInsideFile(String filePath){

		BufferedReader bf = null;
		InputStreamReader inReader = null;
		InputStream in = null;
		StringBuffer content = new StringBuffer();
		try {
			in = this.getClass().getResourceAsStream(filePath);
			inReader = new InputStreamReader(in);
			bf = new BufferedReader(inReader);
			String str;
			while ((str = bf.readLine()) != null) {
				content.append(str).append("\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (null != bf) bf.close();
				if (null != inReader) inReader.close();
				if (null != in) in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return content.toString();
	}

	public File getFile(){
		String jarPath = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
		System.out.println("a: " + jarPath);
		jarPath = System.getProperty("java.class.path");
		System.out.println("b: " + jarPath);
		int firstIndex = jarPath.lastIndexOf(System.getProperty("path.separator")) + 1;
		int lastIndex = jarPath.lastIndexOf(File.separator) + 1;
		jarPath = jarPath.substring(firstIndex, lastIndex);
		System.out.println("c: "+jarPath);

		//获得图片资源
		java.net.URL fileUrl = this.getClass().getResource("/model/Controller.txt");
		System.out.println("d:"+fileUrl);
		Image img = new ImageIcon(fileUrl).getImage();

		//流资源加载
		InputStream in = this.getClass().getResourceAsStream("/model/Controller.txt");
		System.out.println("E:"+in.toString());
		byte[] b = new byte[1024];
		try {
			in.read(b);
		} catch (IOException e) {
			e.printStackTrace();
		}
		for (int i = 0; i < 100; i++) {
			System.out.println(b[i]);
		}
		return null;
	}

	/**
	 *
	 * @param filePath
	 * @param name
	 * @param content
	 * @param encoding
	 * @param ifCover 若已经存在是否覆盖
	 * @throws IOException
	 */
	public void createFile(String filePath, String name, String content, String encoding, boolean ifCover) throws IOException {
		File targetFile = new File(filePath);
		if(!targetFile.exists()){
			targetFile.mkdirs();
		}
		File file = new File(filePath + "/" + name);
		if (!ifCover){
			for (int i = 0; file.exists(); i++) {
				file = new File(filePath + "/" + name.replace(".",i+"."));
			}
		}

		FileOutputStream fos = new FileOutputStream(file, false);
		OutputStreamWriter osw;
		if (encoding == null || "".equals(encoding)) {
			osw = new OutputStreamWriter(fos);
		} else {
			osw = new OutputStreamWriter(fos, encoding);
		}

		BufferedWriter bw = new BufferedWriter(osw);
		bw.write(content);
		bw.close();
		System.out.println(filePath + "/" + name + "创建完成。");
	}

	//存在覆盖文件bug
	public boolean createFile(String filePath, String fileName, String content){

		File targetFile = new File(filePath);
		if(!targetFile.exists()){
			targetFile.mkdirs();
		}
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(filePath + "/" + fileName));
			bw.write(content);
			bw.close();
			System.out.println(filePath + "\\" + fileName);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 目录下查找目录
	 */
	public String getFolder(String path, String folderName) {
		File file = new File(path);
		String resultPath = null;
		// 如果这个路径是文件夹
		if (file.isDirectory()) {
			// 获取路径下的所有文件
			File[] files = file.listFiles();
			for (int i = 0; i < files.length; i++) {
				// 如果是需要的文件夹 则返回
				if (files[i].isDirectory() && folderName.equals(files[i].getName())) {
					resultPath = files[i].getPath();
					break;
				}
				resultPath = getFolder(files[i].getPath(), folderName);
				if (null != resultPath) break;
			}
		}
		return resultPath;
	}

	/**
	 * 转换下划线以及大小写
	 * @param firsCharBig 首字母是否大写
	 */
	public String stringFormat(String str, boolean firsCharBig){

		String[] strs = str.split("_");
		str = "";
		for (int i = 0; i < strs.length; i++) {
			str += strs[i].substring(0, 1).toUpperCase() + strs[i].substring(1).toLowerCase();
		}
		str = str.substring(0,2).toLowerCase() + str.substring(2);
		if (firsCharBig) return str.substring(0,1).toUpperCase() + str.substring(1);
		return str;
	}

	/**
	 * 目录下查找文件
	 */
	public String getFile(String path, String fileName) {
		File file = new File(path);
		String resultPath = null;
		// 如果这个路径是文件夹
		if (file.isDirectory()) {
			// 获取路径下的所有文件
			File[] files = file.listFiles();
			for (int i = 0; i < files.length; i++) {
				// 如果是文件夹 则继续查找
				if (files[i].isDirectory()) {
					resultPath = getFile(files[i].getPath(), fileName);
				} else {
					if (files[i].getName().equals(fileName)) {
						resultPath = files[i].getPath();
						break;
					}
				}
			}
		}
		return resultPath;
	}

	/**
	 * 修改文件内容
	 * @param filePath
	 * @param oldstr
	 * @param newStr
	 * @return
	 */
	public boolean editFileContent(String filePath, String oldstr, String newStr){
		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile(new File(filePath), "rw");
			String line = null;
			long lastPoint = 0; //记住上一次的偏移量
			while ((line = raf.readLine()) != null) {
				final long ponit = raf.getFilePointer();
				if(line.contains(oldstr)){
					String str=line.replace(oldstr, newStr);
					raf.seek(lastPoint);
					raf.writeBytes(str);
				}
				lastPoint = ponit;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				raf.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return true;
	}

	/**
	 * 复制文件
	 * @param source
	 * @param outputFilePath
	 * @return
	 */
	public boolean copyFile(File source, String outputFilePath) {
		FileChannel inputChannel = null;
		FileChannel outputChannel = null;
		boolean ifSuccess = false;
		try {
			inputChannel = new FileInputStream(source).getChannel();
			outputChannel = new FileOutputStream(new File(outputFilePath)).getChannel();
			outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
			ifSuccess = true;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				inputChannel.close();
				outputChannel.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return ifSuccess;
	}

//	public


}
