package com.wzn.service.rmi;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 接收银行返回数据的服务
 */
public class ReceiveMsgServerUTF8 extends Thread {

	private static volatile boolean running = true;

	private long receiveTimeDelay = 45 * 1000;
	private int receiveThreadNum = 4;
	private int port;

	private ExecutorService pool = Executors.newFixedThreadPool(receiveThreadNum);

	private ServerSocket ss;

	public ReceiveMsgServerUTF8(int port) {
		this.port = port;
	}

	public void run() {
		listenServer();
	}

	private void listenServer() {
		try {
			ss = new ServerSocket(port);
			while (running) {
				Socket s = ss.accept();
				String nowDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
				System.out.println(String.format("DATE: %s [ A New Socket Connected ] LocalPort: %s ,RemoteIP: %s ,RemotePort: %s ,", nowDate, port, s.getRemoteSocketAddress(), s.getPort()));
				pool.execute(new SocketAction(s));
			}
		} catch (Exception e) {
			e.printStackTrace();
			running = false;
		}
		if (null != ss) {
			try {
				ss.close();
				ss = null;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	class SocketAction implements Runnable {
		Socket s;
		InputStream in = null;
		boolean run = true;

		long lastReceiveTime = System.currentTimeMillis();

		public SocketAction(Socket s) {
			this.s = s;
		}

		public void run() {
			try {
				in = s.getInputStream();
			} catch (IOException e) {
				e.printStackTrace();
			}
			while (running && run) {
				long currentTime = System.currentTimeMillis();
				if (currentTime - lastReceiveTime > receiveTimeDelay) {
					System.out.println("the lastReceiveTime is:" + lastReceiveTime);
					System.out.println("the currentTime is:" + currentTime);
					System.out.println("the receiveTimeDelay is:" + receiveTimeDelay);
					System.out.println("the delayTime is:" + (currentTime - lastReceiveTime));
					overThis("超时");
				} else {
					try {
						DataInputStream input = null;
						if (null != in && in.available() > 0) {
							input = new DataInputStream(in);
							byte[] buffer = new byte[4];
							input.read(buffer);
							String pkl = new String(buffer, "GB18030");
							if ("0000".equals(pkl)) {
								lastReceiveTime = System.currentTimeMillis();
								String nowDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
								System.out.println(String.format("DATE: %s ,RemoteIP: %s ,RemotePort: %s ,LocalPort: %s ,PackageLength: %s ,ReceivedMsg:\n%s", nowDate, s.getRemoteSocketAddress(), s.getPort(), port, "0000", "0000"));
								continue;
							}
							buffer = new byte[parseStrToInt(pkl, 0)];

							int nIdx = 0;
							int nTotalLen = buffer.length;
							int nReadLen = 0;

							while (nIdx < nTotalLen) {
								nReadLen = input.read(buffer, nIdx, nTotalLen - nIdx);
								if (nReadLen > 0) {
									nIdx = nIdx + nReadLen;
								} else {
									break;
								}
							}
							lastReceiveTime = System.currentTimeMillis();
							String returnXml = new String(buffer, "GB18030");
							String nowDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
							System.out.println(String.format("DATE: %s ,RemoteIP: %s ,RemotePort: %s ,LocalPort: %s ,PackageLength: %s ,ReceivedMsg:\n%s", nowDate, s.getRemoteSocketAddress(), s.getPort(), port, pkl, returnXml));
						} else {
							Thread.sleep(100);
						}
					} catch (Exception e) {
						e.printStackTrace();
						overThis("连接异常");
					}
				}
			}
			// 资源回收
			if (null != in) {
				try {
					in.close();
					in = null;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (null != s && !s.isClosed()) {
				try {
					s.close();
					s = null;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		private void overThis(String message) {
			run = false;
			if (s != null) {
				try {
					s.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			String nowDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
			System.out.println(String.format("DATE: %s ,RemoteIP: %s ,RemotePort: %s ,LocalPort: %s Closed! Reason: %s", nowDate, s.getRemoteSocketAddress(), s.getPort(), port, message));
		}

		private int parseStrToInt(String intStr, int defVal) {
			if (null == intStr || intStr.trim().length() <= 0) {
				return defVal;
			}
			try {
				return Integer.parseInt(intStr);
			} catch (NumberFormatException e) {
				return defVal;
			}
		}
	}

	public static void main(String[] args) {
		for (String port : args) {
			ReceiveMsgServerUTF8 receiveMsgServer = new ReceiveMsgServerUTF8(Integer.parseInt(port));
			receiveMsgServer.start();
			System.out.println("Waiting Receive Msg Service On Port:" + args[0] + " Started !!");
		}
		try {
			Thread.sleep(Long.MAX_VALUE);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
