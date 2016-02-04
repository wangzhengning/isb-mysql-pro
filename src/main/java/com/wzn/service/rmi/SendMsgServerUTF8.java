package com.wzn.service.rmi;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class SendMsgServerUTF8 extends Thread {
	// 我们的IP是192.168.100.242
	// 端口就是那29236和29237
	private static volatile boolean running = true;

	private static Map<String, List<String>> msgMap = new HashMap<String, List<String>>();

	private String ip;
	private int port;
	private String msgMapKey;
	private Socket cSocket = null;
	private OutputStream outer = null;
	private long heartbeatTimeDelay = 20 * 1000;
	private long lastReceiveTime = System.currentTimeMillis();

	public SendMsgServerUTF8(String ip, int port, String msgMapKey) {
		setIp(ip);
		setPort(port);
		setMsgMapKey(msgMapKey);
	}

	public void run() {
		reStartSendSocket();
		while (running) {
			try {
				if (msgMap.get(msgMapKey).size() > 0) {
					String needSendMsg = msgMap.get(msgMapKey).remove(0);
					String nowDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
					System.out.println(String.format("DATE: %s ,RIP: %s ,RPort: %s ,LPort: %s SMsg:\n%s", nowDate, ip, port, cSocket.getLocalPort(), needSendMsg));
					sendMsg(needSendMsg);
					lastReceiveTime = System.currentTimeMillis();
				} else {
					long currentTime = System.currentTimeMillis();
					if (currentTime - lastReceiveTime > heartbeatTimeDelay) {
						String nowDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
						System.out.println(String.format("DATE: %s ,RIP: %s ,RPort: %s ,LPort: %s SMsg:\n%s", nowDate, ip, port, cSocket.getLocalPort(), "0000"));
						sendMsg("0000");
						lastReceiveTime = System.currentTimeMillis();
					} else {
						Thread.sleep(10);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				running = false;
			}
			if (!running) {
				if (null != outer) {
					try {
						outer.close();
						outer = null;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (null != cSocket) {
					try {
						cSocket.close();
						cSocket = null;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				reStartSendSocket();
			}
		}
	}

	private void reStartSendSocket() {
		try {
			cSocket = new Socket(ip, port);
			cSocket.setKeepAlive(true);
			cSocket.setSoTimeout(45 * 1000);
			outer = cSocket.getOutputStream();
			String nowDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
			System.out.println("DATE: " + nowDate + " ,Long Connection To :" + ip + ":" + port + " For Send Request Created Success!!");
			running = true;
			lastReceiveTime = System.currentTimeMillis();
		} catch (Exception e) {
			e.printStackTrace();
			running = false;
		}
		while (!running) {
			try {
				Thread.sleep(1000 * 3);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			reStartSendSocket();
		}
	}

	private void sendMsg(String msg) throws Exception {
		String nowDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
		if (!isServerClosed(cSocket)) {
			outer.write(msg.getBytes("GB18030"));
			outer.flush();
		} else {
			System.out.println(String.format("DATE: %s ,RIP: %s ,RPort: %s ,LPort: %s Has Closed!", nowDate, ip, port, cSocket.getLocalPort()));
			running = false;
		}
	}

	private boolean isServerClosed(Socket s) {
		return false;
		// try {
		// s.sendUrgentData(0xFF);
		// return false;
		// } catch (IOException e) {
		// e.printStackTrace();
		// return true;
		// }
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getMsgMapKey() {
		return msgMapKey;
	}

	public void setMsgMapKey(String msgMapKey) {
		this.msgMapKey = msgMapKey;
	}

	public static void main(String[] args) throws UnsupportedEncodingException {
		int count = args.length;
		int i = 0;
		for (String ipAndPort : args) {
			String[] ipAndPortArray = ipAndPort.split(":");
			i++;
			SendMsgServerUTF8 sendMsgServer = new SendMsgServerUTF8(ipAndPortArray[0], Integer.parseInt(ipAndPortArray[1]), "" + i);
			sendMsgServer.start();
			msgMap.put("" + i, new ArrayList<String>());
			System.out.println("Long Connection To :" + ipAndPort + " For Send Request Start Creating ......");
		}

		Scanner input = new Scanner(System.in);
		String val = null;
		do {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.print("请输入要调试的接口编号：[3022|7606|2502|5800|5801|5815] >>");
			val = input.next();
			String mapKey = String.valueOf(new java.util.Random().nextInt(count) + 1);
			if ("3022".equals(val)) {
				System.out.println("您要调试的接口是：" + val);
				System.out.println("接口名称:直销银行电子账户开户");
				System.out.println("----------------------------------");
				System.out.println("请完成如下参数输入：");
				System.out.println("请输入[姓名]:");
				String name = input.next();
				System.out.println("请输入[身份证号]:");
				String IDNO = input.next();
				System.out.println("请输入[性别M男F女]:");
				String GENDER = input.next();
				System.out.println("请输入[手机号]:");
				String telno = input.next();
				String msg = getSendMsg3022(name, IDNO, GENDER, telno);
				msgMap.get(mapKey).add(msg);
				continue;
			}
			if ("7606".equals(val)) {
				System.out.println("您要调试的接口是：" + val);
				System.out.println("接口名称:直销银行账户资金转入");
				System.out.println("----------------------------------");
				System.out.println("请完成如下参数输入：");
				System.out.println("请输入[姓名]:");
				String name = input.next();
				System.out.println("请输入[身份证号]:");
				String IDNO = input.next();
				System.out.println("请输入[手机号]:");
				String telno = input.next();
				System.out.println("请输入[电子账户]:");
				String ACCT_NO = input.next();
				System.out.println("请输入[金额]:");
				String AMOUNT = input.next();
				String msg = getSendMsg7606(name, IDNO, telno, ACCT_NO, AMOUNT);
				msgMap.get(mapKey).add(msg);
				continue;
			}
			if ("5800".equals(val)) {
				System.out.println("您要调试的接口是：" + val);
				System.out.println("接口名称:按证件号查询持卡人电子账户号");
				System.out.println("----------------------------------");
				System.out.println("请完成如下参数输入：");
				System.out.println("请输入[身份证号]:");
				String IDNO = input.next();
				String msg = getSendMsg5800(IDNO);
				msgMap.get(mapKey).add(msg);
				continue;
			}
			if ("2502".equals(val)) {
				System.out.println("您要调试的接口是：" + val);
				System.out.println("接口名称:直销银行账户资金转出");
				System.out.println("----------------------------------");
				System.out.println("请完成如下参数输入：");
				System.out.println("请输入[姓名]:");
				String name = input.next();
				System.out.println("请输入[身份证号]:");
				String IDNO = input.next();
				System.out.println("请输入[手机号]:");
				String telno = input.next();
				System.out.println("请输入[电子账户]:");
				String ACCT_NO = input.next();
				System.out.println("请输入[金额]:");
				String AMOUNT = input.next();
				String msg = getSendMsg2502(name,IDNO,telno,ACCT_NO,AMOUNT);
				msgMap.get(mapKey).add(msg);
				continue;
			}
			if ("5801".equals(val)) {
				System.out.println("您要调试的接口是：" + val);
				System.out.println("接口名称:电子账户交易明细查询");
				System.out.println("----------------------------------");
				System.out.println("请完成如下参数输入：");
				System.out.println("请输入[电子账户]:");
				String ACCT_NO = input.next();
				System.out.println("请输入[起始日期]:");
				String startDate = input.next();
				System.out.println("请输入[截止日期]:");
				String endDate = input.next();
				String msg = getSendMsg5801(ACCT_NO,startDate,endDate);
				msgMap.get(mapKey).add(msg);
				continue;
			}
			if ("5815".equals(val)) {
				System.out.println("您要调试的接口是：" + val);
				System.out.println("接口名称:电子账户收益查询");
				System.out.println("----------------------------------");
				System.out.println("请完成如下参数输入：");
				System.out.println("请输入[电子账户]:");
				String ACCT_NO = input.next();
				String msg = getSendMsg5815(ACCT_NO);
				msgMap.get(mapKey).add(msg);
				continue;
			} else {
				System.out.println("您输入的是：" + val + ",输入错误");
				continue;
			}
		} while (!val.equals("exit") && !val.equals("quit"));

		System.out.println("程序已经退出！");
		input.close();
		input = null;
	}

	private static String common11Fields(String trxcode) {
		StringBuffer sb = new StringBuffer("");
		sb.append("<TRXCODE>").append(trxcode).append("</TRXCODE>");
		sb.append("<BANKCODE>").append("30040000").append("</BANKCODE>");
		sb.append("<TRXDATE>").append(new SimpleDateFormat("yyyyMMdd").format(new Date())).append("</TRXDATE>");
		sb.append("<TRXTIME>").append(new SimpleDateFormat("HHmmss").format(new Date())).append("</TRXTIME>");
		sb.append("<COINSTCODE>").append("000001").append("</COINSTCODE>");
		sb.append("<COINSTCHANNEL>").append("000002").append("</COINSTCHANNEL>");
		String yyyyMMddHHmmss = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
		String nanoTime = String.valueOf(System.nanoTime());
		yyyyMMddHHmmss = yyyyMMddHHmmss + nanoTime.substring(nanoTime.length() - 6);
		sb.append("<SEQNO>").append(yyyyMMddHHmmss).append("</SEQNO>");
		sb.append("<SOURCE>").append("L5").append("</SOURCE>");
		sb.append("<RETCODE>").append("").append("</RETCODE>");
		sb.append("<RETMSG>").append("").append("</RETMSG>");
		sb.append("<HEADRESERVED>").append("").append("</HEADRESERVED>");
		return sb.toString();
	}

	private static String getSendMsg3022(String name, String IDNO, String GENDER, String telno) throws UnsupportedEncodingException {
		StringBuffer sb = new StringBuffer("");
		sb.append("<?xml version=\"1.0\" encoding=\"GB18030\"?>");
		sb.append("<Message>");
		sb.append(common11Fields("3022"));
		sb.append("<KEYTYPE>").append("01").append("</KEYTYPE>");
		sb.append("<IDNO>").append(IDNO).append("</IDNO>");
		sb.append("<SURNAME>").append(name).append("</SURNAME>");
		sb.append("<MOBILE>").append(telno).append("</MOBILE>");
		sb.append("<PRODUCT>").append("0002").append("</PRODUCT>");
		sb.append("<SMSFLAG>").append("1").append("</SMSFLAG>");
		sb.append("<RISK_YN>").append("1").append("</RISK_YN>");
		sb.append("<RISK_LEL>").append("").append("</RISK_LEL>");
		sb.append("<ACC_TYPE>").append("0").append("</ACC_TYPE>");
		sb.append("<FUCOMCODE>").append("").append("</FUCOMCODE>");
		sb.append("<ADNO>").append("").append("</ADNO>");
		sb.append("<GENDER>").append(GENDER).append("</GENDER>");
		sb.append("<USR_NO>").append("").append("</USR_NO>");
		sb.append("<RESERVED>").append("").append("</RESERVED>");
		sb.append("</Message>");
		String tempLengthStr = String.valueOf(sb.toString().getBytes("GB18030").length + 100000);
		return tempLengthStr.substring(tempLengthStr.length() - 4) + sb.toString();
	}
	
	private static String getSendMsg7606(String name, String IDNO, String telno, String ACCT_NO, String AMOUNT) throws UnsupportedEncodingException {
		StringBuffer sb = new StringBuffer("");
		sb.append("<?xml version=\"1.0\" encoding=\"GB18030\"?>");
		sb.append("<Message>");
		sb.append(common11Fields("7606"));
		sb.append("<ACCT_NO>").append(ACCT_NO).append("</ACCT_NO>");
		sb.append("<CURRENCY>").append("156").append("</CURRENCY>");
		sb.append("<AMOUNT>").append(AMOUNT).append("</AMOUNT>");
		sb.append("<KEYTYPE>").append("01").append("</KEYTYPE>");
		sb.append("<IDNO>").append(IDNO).append("</IDNO>");
		sb.append("<SURNAME>").append(name).append("</SURNAME>");
		sb.append("<MOBILE>").append(telno).append("</MOBILE>");
		sb.append("<RESERVED>").append("").append("</RESERVED>");
		sb.append("</Message>");
		String tempLengthStr = String.valueOf(sb.toString().getBytes("GB18030").length + 100000);
		return tempLengthStr.substring(tempLengthStr.length() - 4) + sb.toString();
	}
	
	private static String getSendMsg5800(String CUSTID) throws UnsupportedEncodingException {
		StringBuffer sb = new StringBuffer("");
		sb.append("<?xml version=\"1.0\" encoding=\"GB18030\"?>");
		sb.append("<Message>");
		sb.append(common11Fields("5800"));
		sb.append("<KEYTYPE>").append("01").append("</KEYTYPE>");
		sb.append("<CUSTID>").append(CUSTID).append("</CUSTID>");
		sb.append("<RTN_IND>").append("").append("</RTN_IND>");
		sb.append("<CARDNBR>").append("").append("</CARDNBR>");
		sb.append("<RESERVED>").append("").append("</RESERVED>");
		sb.append("</Message>");
		String tempLengthStr = String.valueOf(sb.toString().getBytes("GB18030").length + 100000);
		return tempLengthStr.substring(tempLengthStr.length() - 4) + sb.toString();
	}

	private static String getSendMsg2502(String SURNAME,String IDNO, String MOBILE,String ACCT_NO,String AMOUNT) throws UnsupportedEncodingException {
		StringBuffer sb = new StringBuffer("");
		sb.append("<?xml version=\"1.0\" encoding=\"GB18030\"?>");
		sb.append("<Message>");
		sb.append(common11Fields("2502"));
		sb.append("<ACCT_NO>").append(ACCT_NO).append("</ACCT_NO>");
		sb.append("<CURRENCY>").append("156").append("</CURRENCY>");
		sb.append("<AMOUNT>").append(AMOUNT).append("</AMOUNT>");
		sb.append("<KEYTYPE>").append("01").append("</KEYTYPE>");
		sb.append("<IDNO>").append(IDNO).append("</IDNO>");
		sb.append("<SURNAME>").append(SURNAME).append("</SURNAME>");
		sb.append("<MOBILE>").append(MOBILE).append("</MOBILE>");
		sb.append("</Message>");
		String tempLengthStr = String.valueOf(sb.toString().getBytes("GB18030").length + 100000);
		return tempLengthStr.substring(tempLengthStr.length() - 4) + sb.toString();
	}

	private static String getSendMsg5801(String CARDNBR,String startDate,String endDate) throws UnsupportedEncodingException {
		StringBuffer sb = new StringBuffer("");
		sb.append("<?xml version=\"1.0\" encoding=\"GB18030\"?>");
		sb.append("<Message>");
		sb.append(common11Fields("5801"));
		sb.append("<CARDNBR>").append(CARDNBR).append("</CARDNBR>");
		sb.append("<PINFLAG>").append("0").append("</PINFLAG>");
		sb.append("<PIN>").append("        ").append("</PIN>");
		sb.append("<STARTDATE>").append(startDate).append("</STARTDATE>");
		sb.append("<ENDDATE>").append(endDate).append("</ENDDATE>");
		sb.append("<RTN_IND>").append("").append("</RTN_IND>");
		sb.append("<NX_INPD>").append("").append("</NX_INPD>");
		sb.append("<NX_RELD>").append("").append("</NX_RELD>");
		sb.append("<NX_INPT>").append("").append("</NX_INPT>");
		sb.append("<NX_TRNN>").append("").append("</NX_TRNN>");
		sb.append("<TRANTYPE>").append("").append("</TRANTYPE>");
		sb.append("<TYPE_FLAG>").append(" ").append("</TYPE_FLAG>");
		sb.append("<RESERVED>").append("").append("</RESERVED>");
		sb.append("</Message>");

		String tempLengthStr = String.valueOf(sb.toString().getBytes("GB18030").length + 100000);
		return tempLengthStr.substring(tempLengthStr.length() - 4) + sb.toString();
	}

	private static String getSendMsg5815(String CARDNBR) throws UnsupportedEncodingException {
		StringBuffer sb = new StringBuffer("");
		sb.append("<?xml version=\"1.0\" encoding=\"GB18030\"?>");
		sb.append("<Message>");
		sb.append(common11Fields("5815"));
		sb.append("<CARDNBR>").append(CARDNBR).append("</CARDNBR>");
		sb.append("<PINFLAG>").append("0").append("</PINFLAG>");
		sb.append("<PIN>").append("        ").append("</PIN>");
		sb.append("<INQ_TYP>").append("9").append("</INQ_TYP>");
		sb.append("<RTN_IND>").append("").append("</RTN_IND>");
		sb.append("<NX_FITYPE>").append("").append("</NX_FITYPE>");
		sb.append("<NX_FICODE>").append("").append("</NX_FICODE>");
		sb.append("<RESERVED>").append("").append("</RESERVED>");
		sb.append("</Message>");
		String tempLengthStr = String.valueOf(sb.toString().getBytes("GB18030").length + 100000);
		return tempLengthStr.substring(tempLengthStr.length() - 4) + sb.toString();
	}

}
