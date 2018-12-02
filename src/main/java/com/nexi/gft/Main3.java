package com.nexi.gft;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.*;
import java.util.Properties;

public class Main3 {

	private static String SFTPHOST = "test.rebex.net";
	private static int SFTPPORT = 22;
	private static String SFTPUSER = "demo";
	private static String SFTPPASS = "password";
	private static String SFTPWORKINGDIR = "/TEST.txt";

	public static void main(String[] arg) {
		if (arg.length != 1) {
			System.err.println(
					"usage: java -cp test-ftp-0.0.1-SNAPSHOT-jar-with-dependencies.jar com.nexi.gft.Main3 fileToTransfer");
			System.exit(-1);
		}

		FileInputStream fis = null;
		try {

			Properties prop = new Properties();
			prop.load(Main3.class.getClassLoader().getResourceAsStream("application.properties"));
			//SFTPHOST = prop.getProperty("host");
			//SFTPUSER = prop.getProperty("user");
			//SFTPPASS = prop.getProperty("password");

			String fileName = arg[0];
			String user = SFTPUSER;
			String host = SFTPHOST;
			String rfile = SFTPWORKINGDIR;

			JSch.setLogger(new MyLogger());
			JSch jsch = new JSch();
			Session session = jsch.getSession(user, host, SFTPPORT);
			session.setPassword(SFTPPASS);

			Properties config = new Properties();
			config.put("StrictHostKeyChecking", "no");
			config.put("PreferredAuthentications", "publickey,keyboard-interactive,password");
			session.setConfig(config);

			session.connect();

			boolean ptimestamp = true;

			// exec 'scp -t rfile' remotely
			rfile = rfile.replace("'", "'\"'\"'");
			rfile = "'" + rfile + "'";
			String command = "scp " + (ptimestamp ? "-p" : "") + " -t " + rfile;
			Channel channel = session.openChannel("exec");
			((ChannelExec) channel).setCommand(command);

			// get I/O streams for remote scp
			OutputStream out = channel.getOutputStream();
			InputStream in = channel.getInputStream();

			channel.connect();

			if (checkAck(in) != 0) {
				System.exit(0);
			}

			File _lfile = new File(fileName);

			if (ptimestamp) {
				command = "T" + (_lfile.lastModified() / 1000) + " 0";
				// The access time should be sent here,
				// but it is not accessible with JavaAPI ;-<
				command += (" " + (_lfile.lastModified() / 1000) + " 0\n");
				out.write(command.getBytes());
				out.flush();
				if (checkAck(in) != 0) {
					System.exit(0);
				}
			}

			// send "C0644 filesize filename", where filename should not include '/'
			long filesize = _lfile.length();
			command = "C0644 " + filesize + " ";
			if (fileName.lastIndexOf('/') > 0) {
				command += fileName.substring(fileName.lastIndexOf('/') + 1);
			} else {
				command += fileName;
			}
			command += "\n";
			out.write(command.getBytes());
			out.flush();
			if (checkAck(in) != 0) {
				System.exit(0);
			}

			// send a content of lfile
			fis = new FileInputStream(fileName);
			byte[] buf = new byte[1024];
			while (true) {
				int len = fis.read(buf, 0, buf.length);
				if (len <= 0)
					break;
				out.write(buf, 0, len); //out.flush();
			}
			fis.close();
			fis = null;
			// send '\0'
			buf[0] = 0;
			out.write(buf, 0, 1);
			out.flush();
			if (checkAck(in) != 0) {
				System.exit(0);
			}
			out.close();

			channel.disconnect();
			session.disconnect();

			System.exit(0);
		} catch (Exception e) {
			System.out.println(e);
			try {
				if (fis != null)
					fis.close();
			} catch (Exception ee) {
			}
		}
	}

	static int checkAck(InputStream in) throws IOException {
		int b = in.read();
		// b may be 0 for success,
		//          1 for error,
		//          2 for fatal error,
		//          -1
		if (b == 0)
			return b;
		if (b == -1)
			return b;

		if (b == 1 || b == 2) {
			StringBuffer sb = new StringBuffer();
			int c;
			do {
				c = in.read();
				sb.append((char) c);
			} while (c != '\n');
			if (b == 1) { // error
				System.out.print(sb.toString());
			}
			if (b == 2) { // fatal error
				System.out.print(sb.toString());
			}
		}
		return b;
	}

	public static class MyLogger implements com.jcraft.jsch.Logger {
		static java.util.Hashtable name=new java.util.Hashtable();
		static{
			name.put(new Integer(DEBUG), "DEBUG: ");
			name.put(new Integer(INFO), "INFO: ");
			name.put(new Integer(WARN), "WARN: ");
			name.put(new Integer(ERROR), "ERROR: ");
			name.put(new Integer(FATAL), "FATAL: ");
		}
		public boolean isEnabled(int level){
			return true;
		}
		public void log(int level, String message){
			System.err.print(name.get(new Integer(level)));
			System.err.println(message);
		}
	}

}