package com.nexi.gft;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

public class Main {

	private static String SFTPHOST;
	private static String SFTPUSER;
	private static String SFTPPASS;

	public static void main(String[] args) throws Exception {
		if (args.length != 1) {
			System.err.println(
					"usage: java -cp test-ftp-0.0.1-SNAPSHOT-jar-with-dependencies.jar com.nexi.gft.Main fileToTransfer");
			System.exit(-1);
		}
		System.out.println("MAIN");

		String fileName = args[0];

		Properties prop = new Properties();
		prop.load(Main.class.getClassLoader().getResourceAsStream("application.properties"));
		SFTPHOST = prop.getProperty("host");
		SFTPUSER = prop.getProperty("user");
		SFTPPASS = prop.getProperty("password");

		System.out.println("host: " + SFTPHOST + " user: " + SFTPUSER + " password: " + SFTPPASS);

		send(fileName);
	}

	public static void send(String fileName) {

		int SFTPPORT = 22;
		String SFTPWORKINGDIR = "/home/javauser/TEST.txt";

		Session session = null;
		Channel channel = null;
		ChannelSftp channelSftp = null;
		System.out.println("preparing the host information for sftp.");
		try {
			JSch jsch = new JSch();
			session = jsch.getSession(SFTPUSER, SFTPHOST, SFTPPORT);
			session.setPassword(SFTPPASS);
			java.util.Properties config = new java.util.Properties();
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);
			session.connect();
			System.out.println("Host connected.");
			channel = session.openChannel("sftp");
			channel.connect();
			System.out.println("sftp channel opened and connected.");
			channelSftp = (ChannelSftp) channel;
			channelSftp.cd(SFTPWORKINGDIR);
			System.out.println("read file " + fileName);
			File f = new File(fileName);
			channelSftp.put(new FileInputStream(f), f.getName());
			System.out.println("File transfered successfully to host.");
		} catch (Exception ex) {
			System.out.println("Exception found while tranfer the response.");
			ex.printStackTrace();
		} finally {

			channelSftp.exit();
			System.out.println("sftp Channel exited.");
			channel.disconnect();
			System.out.println("Channel disconnected.");
			session.disconnect();
			System.out.println("Host Session disconnected.");
		}
	}
}
