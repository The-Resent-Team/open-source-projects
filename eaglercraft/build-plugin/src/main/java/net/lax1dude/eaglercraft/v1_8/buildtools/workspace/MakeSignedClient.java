package net.lax1dude.eaglercraft.v1_8.buildtools.workspace;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.jcraft.jzlib.GZIPOutputStream;

public class MakeSignedClient {

	private static final String USE_STRICT = "\"use strict\";";

	public static final int torrentBlockSize = 0xFF00;

	public static void main(String[] args) throws IOException {
		if(args.length != 6 && args.length != 7) {
			System.err.println("Usage: MakeSignedClient.jar <template.txt> <classes.js> <assets.epk> [languages dir] <template.html> <downloadSources.txt> <output.html>");
		}

		System.out.println();
		System.out.println("Generating signed client...");
		System.out.println();
		
		System.out.println("Loading file: " + args[0]);
		String templateTxt = new String(loadFile(new File(args[0])), StandardCharsets.UTF_8);
		
		System.out.println("Loading file: " + args[args.length - 3]);
		String templateHtml = new String(loadFile(new File(args[args.length - 3])), StandardCharsets.UTF_8);
		
		System.out.println("Loading file: " + args[args.length - 2]);
		String sourcesList = new String(loadFile(new File(args[args.length - 2])), StandardCharsets.UTF_8);
		
		String date = (new SimpleDateFormat("MM/dd/yyyy")).format(new Date());
		System.out.println();
		System.out.println("Using date: " + date);
		System.out.println();
		
		File datFile = new File(args[args.length - 1] + ".dat");
		byte[] bundleData;
		ByteArrayOutputStream bao;
		if(!datFile.isFile()) {
			StringBuilder classesString = new StringBuilder();
			String[] jsFiles = args[1].split("\\" + System.getProperty("path.separator"));
			
			for(int i = 0; i < jsFiles.length; ++i) {
				System.out.println("Loading file: " + jsFiles[i]);
				String classesJs =  new String(loadFile(new File(jsFiles[i])), StandardCharsets.UTF_8);
			
				if(classesJs.startsWith(USE_STRICT)) {
					classesJs = classesJs.substring(USE_STRICT.length());
				}
				
				if(i > 0) {
					classesString.append("\n\n");
				}
				
				classesString.append(classesJs);
			}
			
			System.out.println("Loading file: " + args[2]);
			String assetsEpk = Base64.encodeBase64String(loadFile(new File(args[2])));
	
			String languagesEpk = null;
			if(args.length == 7) {
				if(args[3].endsWith(".epk")) {
					System.out.println("Loading file: " + args[3]);
					languagesEpk = Base64.encodeBase64String(loadFile(new File(args[5])));
				}else {
					System.out.println();
					System.out.println("Building EPK: " + args[3]);
					File zipIn = new File(args[3]);
					languagesEpk = Base64.encodeBase64String(makeEPK(zipIn, new File(zipIn.getParentFile(), "languages.tmp.epk")));
				}
			}
	
			System.out.println();
			
			if(languagesEpk != null) {
				templateTxt = templateTxt.replace("${assets_epk}", "[ { url: \"data:application/octet-stream;base64," + assetsEpk +
						"\" }, { url: \"data:application/octet-stream;base64," + languagesEpk + "\", path: \"assets/minecraft/lang/\" } ]");
			}else {
				templateTxt = templateTxt.replace("${assets_epk}", "\"data:application/octet-stream;base64," + assetsEpk + "\"");
			}
	
			templateTxt = templateTxt.replace("${classes_js}", classesString.toString());
			
			System.out.println();
			System.out.println("Compressing bundle...");
			System.out.println();
			
			bao = new ByteArrayOutputStream();
			try(GZIPOutputStream gzipOut = new GZIPOutputStream(bao)) {
				gzipOut.write(templateTxt.getBytes(StandardCharsets.UTF_8));
			}
			
			bundleData = bao.toByteArray();
		}else {
			System.out.println();
			System.out.println("File \"" + datFile.getAbsolutePath() + "\" already exists!");
			System.out.println("Delete it to fully recompile client!");
			System.out.println();
			System.out.println("Loading file: " + datFile.getName());
			bundleData = loadFile(datFile);
		}
	
		System.out.println();
		System.out.println("Generating signature...");
		System.out.println();
		
		SHA256Digest sha256 = new SHA256Digest();
		sha256.update(bundleData, 0, bundleData.length);
		byte[] bundleHash = new byte[32];
		sha256.doFinal(bundleHash, 0);
		bao = new ByteArrayOutputStream();
		DataOutputStream dao = new DataOutputStream(new GZIPOutputStream(bao));
		
		dao.writeLong(System.currentTimeMillis());
		
		// bundle info
		dao.writeInt(bundleData.length);
		dao.write(bundleHash);
		
		BufferedReader bis = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
		
		// bundle package name
		String def = "net.lax1dude.eaglercraft.v1_8.client";
		System.out.print("Enter package name string [" + def + "]: ");
		String str = bis.readLine().trim();
		dao.writeUTF(str.length() == 0 ? def : str);
		
		// bundle display name
		def = "EaglercraftX 1.8";
		System.out.print("Enter display name string [" + def + "]: ");
		str = bis.readLine().trim();
		dao.writeUTF(str.length() == 0 ? def : str);
		
		// bundle display author
		System.out.print("Enter author string: ");
		dao.writeUTF(bis.readLine().trim());
		
		System.out.print("Enter version integer: ");
		int i = Integer.parseInt(bis.readLine().trim());
		dao.writeInt(i);
		
		def = "u" + i;
		System.out.print("Enter display version string [" + def + "]: ");
		str = bis.readLine().trim();
		dao.writeUTF(str.length() == 0 ? def : str);
		
		System.out.print("Enter version comment: ");
		dao.writeUTF(bis.readLine().trim().replace("\\n", "\n"));
		
		SecureRandom paddingGen = new SecureRandom();
		
		int paddingLen = paddingGen.nextInt(128) + 64;
		dao.write(paddingLen);
		byte[] padding = new byte[paddingLen];
		paddingGen.nextBytes(padding);
		dao.write(padding);
		
		List<String> splitLines = new ArrayList();
		for(String strr : sourcesList.split("(\\r\\n|\\r|\\n)+")) {
			strr = strr.trim();
			if(strr.length() > 0 && strr.charAt(0) != '#') {
				splitLines.add(strr);
			}
		}
		
		dao.writeInt(splitLines.size());
		for(String strr : splitLines) {
			dao.writeInt(paddingGen.nextInt());
			String[] strrr = strr.split(":", 2);
			dao.writeUTF(strrr[0].trim());
			dao.writeUTF(strrr[1].trim());
		}
		
		dao.close();
		
		byte[] signaturePayload = bao.toByteArray();
		
		if(signaturePayload.length > 32767) throw new IOException("Digital signature is too large! (" + signaturePayload.length + ")");
		
		sha256.reset();
		sha256.update(new byte[] { (byte) 170, (byte) 191, (byte) 203, (byte) 188, (byte) 47, (byte) 37, (byte) 17,
				(byte) 187, (byte) 169, (byte) 225, (byte) 247, (byte) 193, (byte) 100, (byte) 101, (byte) 233,
				(byte) 106, (byte) 80, (byte) 204, (byte) 192, (byte) 140, (byte) 19, (byte) 18, (byte) 165, (byte) 252,
				(byte) 138, (byte) 187, (byte) 229, (byte) 148, (byte) 118, (byte) 208, (byte) 179, (byte) 233 }, 0, 32);
		sha256.update(signaturePayload, 0, signaturePayload.length);
		byte[] hash2048 = new byte[256];
		sha256.doFinal(hash2048, 0);
		sha256.reset();
		sha256.update(new byte[] { (byte) 95, (byte) 222, (byte) 208, (byte) 153, (byte) 171, (byte) 133, (byte) 7,
				(byte) 88, (byte) 111, (byte) 87, (byte) 37, (byte) 104, (byte) 98, (byte) 115, (byte) 185, (byte) 153,
				(byte) 206, (byte) 188, (byte) 143, (byte) 18, (byte) 247, (byte) 28, (byte) 130, (byte) 87, (byte) 56,
				(byte) 223, (byte) 45, (byte) 192, (byte) 108, (byte) 166, (byte) 254, (byte) 19 }, 0, 32);
		sha256.update(signaturePayload, 0, signaturePayload.length);
		sha256.doFinal(hash2048, 32);
		sha256.reset();
		sha256.update(new byte[] { (byte) 101, (byte) 245, (byte) 91, (byte) 125, (byte) 50, (byte) 79, (byte) 71,
				(byte) 52, (byte) 244, (byte) 249, (byte) 84, (byte) 5, (byte) 139, (byte) 21, (byte) 13, (byte) 200,
				(byte) 75, (byte) 0, (byte) 103, (byte) 1, (byte) 14, (byte) 159, (byte) 199, (byte) 194, (byte) 56,
				(byte) 161, (byte) 63, (byte) 248, (byte) 90, (byte) 134, (byte) 96, (byte) 160 }, 0, 32);
		sha256.update(signaturePayload, 0, signaturePayload.length);
		sha256.doFinal(hash2048, 64);
		sha256.reset();
		sha256.update(new byte[] { (byte) 84, (byte) 208, (byte) 74, (byte) 114, (byte) 251, (byte) 86, (byte) 195,
				(byte) 222, (byte) 90, (byte) 18, (byte) 194, (byte) 226, (byte) 20, (byte) 56, (byte) 191, (byte) 235,
				(byte) 187, (byte) 93, (byte) 18, (byte) 122, (byte) 161, (byte) 40, (byte) 160, (byte) 88, (byte) 151,
				(byte) 88, (byte) 215, (byte) 216, (byte) 253, (byte) 235, (byte) 7, (byte) 60 }, 0, 32);
		sha256.update(signaturePayload, 0, signaturePayload.length);
		sha256.doFinal(hash2048, 96);
		
		hash2048[0] = (byte)((signaturePayload.length >> 8) & 0xFF);
		hash2048[1] = (byte)(signaturePayload.length & 0xFF);
		
		System.out.println();
		System.out.print("Enter 2048 bit modulus: ");
		BigInteger modulusKey = new BigInteger(bis.readLine().trim());
		System.out.print("Enter 2048 bit secret: ");
		BigInteger secretKey = new BigInteger(bis.readLine().trim());
		byte[] sighash = (new BigInteger(hash2048)).modPow(secretKey, modulusKey).toByteArray();
		if(sighash.length != 256) {
			throw new IOException("BigInteger was " + sighash.length + " bytes long! should be 256, try again");
		}
		
		bao = new ByteArrayOutputStream();
		
		// write header
		bao.write(new byte[] { (byte)'E', (byte)'A', (byte)'G', (byte)'S', (byte)'I', (byte)'G', (byte)0x00, (byte)0x01 });
		bao.write(sighash);
		bao.write((signaturePayload.length >> 8) & 0xFF);
		bao.write(signaturePayload.length & 0xFF);
		bao.write(signaturePayload);

		byte[] baob = bao.toByteArray();
		templateHtml = templateHtml.replace("${client_signature}", Base64.encodeBase64String(baob));
		templateHtml = templateHtml.replace("${client_bundle}", Base64.encodeBase64String(bundleData));
		
		templateHtml = templateHtml.replace("${date}", date);

		System.out.println();
		System.out.println("Writing cert to: " + args[args.length - 1] + ".cert");
		try(FileOutputStream fos = new FileOutputStream(new File(args[args.length - 1] + ".cert"))) {
			fos.write(baob);
		}
		if(!(new File(args[args.length - 1] + ".dat")).isFile()) {
			System.out.println("Writing data to: " + args[args.length - 1] + ".dat");
			try(FileOutputStream fos = new FileOutputStream(new File(args[args.length - 1] + ".dat"))) {
				fos.write(bundleData);
			}
		}
		System.out.println();
		System.out.println("Writing html to: " + args[args.length - 1]);
		System.out.println();
		
		try(FileOutputStream fos = new FileOutputStream(new File(args[args.length - 1]))) {
			fos.write(templateHtml.getBytes(StandardCharsets.UTF_8));
		}
		
		System.out.println("Finished.");
		System.out.println();
	}

	private static byte[] loadFile(File file) throws IOException {
		byte[] ret = new byte[(int)file.length()];
		int i = 0, j;
		try(FileInputStream is = new FileInputStream(file)) {
			while(i < ret.length && (j = is.read(ret, i, ret.length - i)) != -1) {
				i += j;
			}
		}
		return ret;
	}

	private static byte[] makeEPK(File file, File tmp) throws IOException {
		try {
			Method meth = Class.forName("net.lax1dude.eaglercraft.v1_8.buildtools.workspace.CompilePackage").getDeclaredMethod("main", String[].class);
			meth.invoke(null, new Object[] { new String[] { file.getAbsolutePath(), tmp.getAbsolutePath() } });
		} catch (InvocationTargetException ex) {
			Throwable t = ex.getCause();
			if(t instanceof IOException) {
				throw (IOException)t;
			}else {
				throw new IOException("Failed to run EPKCompiler!", t);
			}
		} catch (Throwable t) {
			throw new IOException("Failed to run EPKCompiler!", t);
		}
		byte[] ret = loadFile(tmp);
		tmp.delete();
		return ret;
	}

}
