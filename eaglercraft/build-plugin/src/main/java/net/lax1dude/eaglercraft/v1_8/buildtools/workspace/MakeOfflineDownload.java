package net.lax1dude.eaglercraft.v1_8.buildtools.workspace;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MakeOfflineDownload {

	private static final String USE_STRICT = "\"use strict\";";

	public static void main(String[] args) throws IOException {
		if(args.length != 4 && args.length != 6) {
			System.err.println("Usage: MakeOfflineDownload.jar <template.html> <javascript files> <assets.epk> <en_US output.html> [international output.html] [languages dir/zip or cached languages.epk]");
		}

		System.out.println();
		System.out.println("Generating offline download...");
		System.out.println();
		
		System.out.println("Loading file: " + args[0]);
		String templateHtml = new String(loadFile(new File(args[0])), StandardCharsets.UTF_8);
		
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
		if(args.length == 6) {
			if(args[5].endsWith(".epk")) {
				System.out.println("Loading file: " + args[5]);
				languagesEpk = Base64.encodeBase64String(loadFile(new File(args[5])));
			}else {
				System.out.println();
				System.out.println("Building EPK: " + args[5]);
				File zipIn = new File(args[5]);
				languagesEpk = Base64.encodeBase64String(makeEPK(zipIn, new File(zipIn.getParentFile(), "languages.tmp.epk")));
			}
		}

		System.out.println();
		String date = (new SimpleDateFormat("MM/dd/yyyy")).format(new Date());
		System.out.println("Using date: " + date);

		System.out.println();
		System.out.println("Writing html to: " + args[3]);
		System.out.println();
		
		String classesJs = classesString.toString();
		String offlineHTML = templateHtml.replace("${date}", date).replace("${classes_js}", classesJs).replace("${assets_epk}", "\"data:application/octet-stream;base64," + assetsEpk + "\"");
		
		try(FileOutputStream fos = new FileOutputStream(new File(args[3]))) {
			fos.write(offlineHTML.getBytes(StandardCharsets.UTF_8));
		}
		
		if(languagesEpk != null) {
			System.out.println("Writing html to: " + args[4]);
			System.out.println();
			
			offlineHTML = templateHtml.replace("${date}", date).replace("${classes_js}", classesJs).replace("${assets_epk}",
					"[ { url: \"data:application/octet-stream;base64," + assetsEpk + "\" }, { url: \"data:application/octet-stream;base64," +
							languagesEpk + "\", path: \"assets/minecraft/lang/\" } ]");
			
			try(FileOutputStream fos = new FileOutputStream(new File(args[4]))) {
				fos.write(offlineHTML.getBytes(StandardCharsets.UTF_8));
			}
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
