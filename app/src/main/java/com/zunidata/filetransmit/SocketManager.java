package com.zunidata.filetransmit;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

import com.zunidata.tool.MD5;

import android.os.Environment;

public class SocketManager {
	private ServerSocket server;
	private Socket socket;
	private long rxFailCount;
	private long txFailCount;

	public SocketManager() {
		rxFailCount = 0;
		txFailCount = 0;
	}

//	 接收文件
	public String ReceiveFile() {
		try {
			// 接收文件名
			Socket name = server.accept();
			InputStream nameStream = name.getInputStream();
			InputStreamReader streamReader = new InputStreamReader(nameStream);
			BufferedReader br = new BufferedReader(streamReader);
			String fileName = br.readLine();
			br.close();
			streamReader.close();
			nameStream.close();
			name.close();
			// 接收文件数据
			Socket data = server.accept();
			InputStream dataStream = data.getInputStream();
			String savePath = Environment.getExternalStorageDirectory().getPath() + "/" + fileName+"_receive";
			FileOutputStream file = new FileOutputStream(savePath, false);
			byte[] buffer = new byte[1024];
			int size = -1;
			while ((size = dataStream.read(buffer)) != -1) {
				file.write(buffer, 0, size);
			}
			file.close();
			dataStream.close();
			data.close();
			String result = null;
			if (MD5.md5sum(savePath).equals("2242462FA64A0DCA9C539D8D996010B1"))
				result = "\tPass\n";
			else{
				result = "\tFail\n";
				rxFailCount++;
			}
			return fileName + "\tReceive Complete" + result;
		} catch (Exception e) {
			rxFailCount++;
			return "Receive Error:\n" + e.getMessage();
			
		}
	}
	

//	public String ReceiveFile(InputStream inputStream) {
//		try {
////			Socket name = server.accept();
////			InputStream nameStream = name.getInputStream();
//			InputStreamReader streamReader = new InputStreamReader(inputStream);
//			BufferedReader br = new BufferedReader(streamReader);
//			String fileName = br.readLine();
////			br.close();
////			streamReader.close();
////			inputStream.close();
////			name.close();
//			// �����ļ���
////			Socket name = server.accept();
////			InputStream nameStream = socket.getInputStream();
////			InputStreamReader streamReader = new InputStreamReader(inputStream);
////			BufferedReader br = new BufferedReader(streamReader);
////			name.close();
//			// �����ļ�����
////			Socket data = server.accept();
////			InputStream dataStream = socket.getInputStream();
//			String savePath = Environment.getExternalStorageDirectory().getPath() + "/" + fileName;
//			FileOutputStream file = new FileOutputStream(savePath, false);
//			byte[] buffer = new byte[1024];
//			int size = -1;
//			while ((size = inputStream.read(buffer)) != -1) {
//				file.write(buffer, 0, size);
//			}
//			file.close();
//			inputStream.close();
////			data.close();
//			String result = null;
//			if (MD5.md5sum(savePath).equals("13E8AA8D1757FB5AF94BBA3C347F3F5A"))
//				result = "\tPass\n";
//			else
//				result = "\tFail\n";
//			return fileName + " �������" + result;
//		} catch (Exception e) {
//			return "���մ���:\n" + e.getMessage();
//		}
//	}

	public String SendFile(String fileName, String path, String ipAddress, int port) {
		try {
			Socket name = new Socket(ipAddress, port);
			OutputStream outputName = name.getOutputStream();
			OutputStreamWriter outputWriter = new OutputStreamWriter(outputName);
			BufferedWriter bwName = new BufferedWriter(outputWriter);
			bwName.write(fileName);
			bwName.close();
			outputWriter.close();
			outputName.close();
			name.close();

			Socket data = new Socket(ipAddress, port);
			OutputStream outputData = data.getOutputStream();
			FileInputStream fileInput = new FileInputStream(path);
			int size = -1;
			byte[] buffer = new byte[1024];
			while ((size = fileInput.read(buffer, 0, 1024)) != -1) {
				outputData.write(buffer, 0, size);
			}
			outputData.close();
			fileInput.close();
			data.close();
			return fileName + "\tSend Complete";
		} catch (Exception e) {
			txFailCount++;
			return "Send Error:\n" + e.getMessage();
		}
	}
	
	/*
	public String SendFile(String fileName, String path, OutputStream outputStream) {
		try {
//			Socket name = new Socket(ipAddress, port);
//			OutputStream outputName = socket.getOutputStream();
			OutputStreamWriter outputWriter = new OutputStreamWriter(outputStream);
			BufferedWriter bwName = new BufferedWriter(outputWriter);
			bwName.write(fileName);
			bwName.close();
			outputWriter.close();
			outputStream.flush();
//			name.close();

//			Socket data = new Socket(ipAddress, port);
//			OutputStream outputData = socket.getOutputStream();
			FileInputStream fileInput = new FileInputStream(path);
			int size = -1;
			byte[] buffer = new byte[1024];
			while ((size = fileInput.read(buffer, 0, 1024)) != -1) {
				outputStream.write(buffer, 0, size);
			}
			outputStream.flush();
			fileInput.close();
//			data.close();
			return fileName + " �������";
		} catch (Exception e) {
			return "���ʹ���:\n" + e.getMessage();
		}
	}*/
	
	public ServerSocket getServer() {
		return server;
	}

	public void setServer(ServerSocket server) {
		this.server = server;
	}
	
	public long getTxSummary(){
		return txFailCount;
	}
	
	public long getRxSummary(){
		return rxFailCount;
	}

	public void setRxFailCount(long rxFailCount) {
		this.rxFailCount = rxFailCount;
	}

	public void setTxFailCount(long txFailCount) {
		this.txFailCount = txFailCount;
	}
	
	
}
