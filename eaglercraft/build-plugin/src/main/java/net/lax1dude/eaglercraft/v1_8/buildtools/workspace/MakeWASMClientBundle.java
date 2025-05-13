package net.lax1dude.eaglercraft.v1_8.buildtools.workspace;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.zip.CRC32;

import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.XZOutputStream;

public class MakeWASMClientBundle {
	public static File resent$gradleDirectoryFix;

	private static class DataSegment {
		
		private final int offset;
		private final int length;
		
		private DataSegment(int offset, int length) {
			this.offset = offset;
			this.length = length;
		}
		
	}

	private static class EPKStruct {

		private final String epkLoadPathStr;
		private DataSegment epkLoadPath;
		private final File epkFile;
		private final String epkFilePathStr;
		private DataSegment epkFilePath;
		private byte[] epkData;
		private byte[] epkDataCompressed;

		private int epkDataOffset;

		private EPKStruct(String epkLoadPath, String epkFilePath) {
			this.epkLoadPathStr = epkLoadPath;
			this.epkFile = new File(resent$gradleDirectoryFix, epkFilePath);
			this.epkFilePathStr = this.epkFile.getName();
		}

	}

	public static void main(String[] args) throws IOException {
		if(args.length != 3) {
			System.err.println("Usage: java -jar CompilePackageWASM.jar <epw_src.txt> <epw_meta.txt> <output folder>");
			return;
		}
		
		List<EPKStruct> epks = new ArrayList<>();
		
		File f = new File(args[0]);
		
		System.out.println();
		System.out.println("Parsing: " + f.getAbsolutePath());
		
		Properties propsSrc = new Properties();
		try(InputStream is = new FileInputStream(f)) {
			propsSrc.load(is);
		}

		String offlineTemplate = getRequired(propsSrc, "offline-download-template");
		String bootstrapJS = getRequired(propsSrc, "offline-download-script");
		String downloadName = getRequired(propsSrc, "offline-download-name");
		File loaderJSFile = new File(resent$gradleDirectoryFix, getRequired(propsSrc, "loader-js-file"));
		File loaderWASMFile = new File(resent$gradleDirectoryFix, getRequired(propsSrc, "loader-wasm-file"));
		File eagRuntimeJSFile = new File(resent$gradleDirectoryFix, getRequired(propsSrc, "eagruntime-js-file"));
		File classesWASMFile = new File(resent$gradleDirectoryFix, getRequired(propsSrc, "classes-wasm-file"));
		File classesDeobfTEADBGFile = null;
		File classesDeobfWASMFile = null;
		if(propsSrc.getProperty("classes-deobf-wasm-file") != null) {
			classesDeobfTEADBGFile = new File(resent$gradleDirectoryFix, getRequired(propsSrc, "classes-deobf-teadbg-file"));
			classesDeobfWASMFile = new File(resent$gradleDirectoryFix, getRequired(propsSrc, "classes-deobf-wasm-file"));
		}

		for(int i = 0;; ++i) {
			String fileName = propsSrc.getProperty("assets-epk-" + i + "-file");
			if(fileName == null) {
				if(i == 0) {
					throw new IllegalArgumentException("At least 1 EPK file must be specified!");
				}
				break;
			}
			String pathName = propsSrc.getProperty("assets-epk-" + i + "-path");
			if(pathName == null) {
				pathName = "";
			}else {
				if(pathName.startsWith("/")) {
					pathName = pathName.substring(1);
				}
				if(pathName.endsWith("/")) {
					pathName = pathName.substring(0, pathName.length() - 1);
				}
			}
			epks.add(new EPKStruct(pathName, fileName));
		}

		File splashFile = new File(resent$gradleDirectoryFix, getRequired(propsSrc, "splash-logo-image-file"));
		String splashMIME = getRequired(propsSrc, "splash-logo-image-mime");
		File pressAnyKeyFile = new File(resent$gradleDirectoryFix, getRequired(propsSrc, "press-any-key-image-file"));
		String pressAnyKeyMIME = getRequired(propsSrc, "press-any-key-image-mime");
		File crashFile = new File(resent$gradleDirectoryFix, getRequired(propsSrc, "crash-logo-image-file"));
		String crashMIME = getRequired(propsSrc, "crash-logo-image-mime");
		File faviconFile = new File(resent$gradleDirectoryFix, getRequired(propsSrc, "favicon-image-file"));
		String faviconMIME = getRequired(propsSrc, "favicon-image-mime");
		
		File JSPIUnavailableFile = new File(resent$gradleDirectoryFix, getRequired(propsSrc, "jspi-unavailable-file"));
		
		f = new File(/*resent$gradleDirectoryFix, */args[1]);
		
		System.out.println("Parsing: " + f.getAbsolutePath());
		
		Properties propsMeta = new Properties();
		try(InputStream is = new FileInputStream(f)) {
			propsMeta.load(is);
		}

		int clientVersionInteger = Integer.parseInt(getRequired(propsMeta, "client-version-integer"));
		String clientPackageName = getRequired(propsMeta, "client-package-name");
		String clientOriginName = getRequired(propsMeta, "client-origin-name");
		String clientOriginVersion = getRequired(propsMeta, "client-origin-version");
		String clientOriginVendor = getRequired(propsMeta, "client-origin-vendor");
		String clientForkName = getRequired(propsMeta, "client-fork-name");
		String clientForkVersion = getRequired(propsMeta, "client-fork-version");
		String clientForkVendor = getRequired(propsMeta, "client-fork-vendor");

		Map<String, String> metadata = new HashMap<>();
		for(Object o : propsMeta.keySet()) {
			String k = o.toString();
			if(k.startsWith("metadata-")) {
				metadata.put(k.substring(9), propsMeta.getProperty(k));
			}
		}

		System.out.println();
		System.out.println("Reading: " + loaderJSFile.getAbsolutePath());
		byte[] loaderJSBin = getFileBytes(loaderJSFile);
		System.out.println("Reading: " + loaderWASMFile.getAbsolutePath());
		byte[] loaderWASMBin = getFileBytes(loaderWASMFile);
		System.out.println("Reading: " + eagRuntimeJSFile.getAbsolutePath());
		byte[] eagRuntimeJSBin = getFileBytes(eagRuntimeJSFile);
		System.out.println("Reading: " + classesWASMFile.getAbsolutePath());
		byte[] classesWASMBin = getFileBytes(classesWASMFile);
		byte[] classesDeobfTEADBGBin = null;
		byte[] classesDeobfWASMBin = null;
		if(classesDeobfWASMFile != null) {
			System.out.println("Reading: " + classesDeobfTEADBGFile.getAbsolutePath());
			classesDeobfTEADBGBin = getFileBytes(classesDeobfTEADBGFile);
			System.out.println("Reading: " + classesDeobfWASMFile.getAbsolutePath());
			classesDeobfWASMBin = getFileBytes(classesDeobfWASMFile);
		}
		
		for(int i = 0, l = epks.size(); i < l; ++i) {
			EPKStruct struct = epks.get(i);
			if(struct.epkFile.isFile()) {
				System.out.println("Reading: " + struct.epkFile.getAbsolutePath());
				struct.epkData = getFileBytes(struct.epkFile);
			}else {
				System.out.println("Generating EPK: " + struct.epkFile.getAbsolutePath());
				struct.epkData = makeEPK(struct.epkFile, new File(resent$gradleDirectoryFix, "._assets." + i + ".0.tmp"));
			}
		}
		
		System.out.println("Reading: " + splashFile.getAbsolutePath());
		byte[] splashBin = getFileBytes(splashFile);
		System.out.println("Reading: " + pressAnyKeyFile.getAbsolutePath());
		byte[] pressAnyKeyBin = getFileBytes(pressAnyKeyFile);
		System.out.println("Reading: " + crashFile.getAbsolutePath());
		byte[] crashBin = getFileBytes(crashFile);
		System.out.println("Reading: " + faviconFile.getAbsolutePath());
		byte[] faviconBin = getFileBytes(faviconFile);
		System.out.println("Reading: " + JSPIUnavailableFile.getAbsolutePath());
		byte[] JSPIUnavailableBin = getFileBytes(JSPIUnavailableFile);
		
		System.out.println();
		System.out.println("Compressing eagruntime.js");
		byte[] eagruntimeJSCompressed = compressFileBytes(eagRuntimeJSBin);
		System.out.println("Compressing classes.wasm");
		byte[] classesWASMCompressed = compressFileBytes(classesWASMBin);
		byte[] classesDeobfTEADBGCompressed = null;
		byte[] classesDeobfWASMCompressed = null;
		if(classesDeobfWASMBin != null) {
			System.out.println("Compressing classes.wasm teadbg");
			classesDeobfTEADBGCompressed = compressFileBytes(classesDeobfTEADBGBin);
			System.out.println("Compressing classes.wasm deobfuscator");
			classesDeobfWASMCompressed = compressFileBytes(classesDeobfWASMBin);
		}

		for(int i = 0, l = epks.size(); i < l; ++i) {
			EPKStruct struct = epks.get(i);
			System.out.println("Compressing EPK #" + i + ": " + struct.epkFilePathStr);
			struct.epkDataCompressed = compressFileBytes(struct.epkData);
		}
		
		System.out.println("Compressing JSPI unavailable screen");
		byte[] JSPIUnavailableCompressed = compressFileBytes(JSPIUnavailableBin);

		System.out.println();
		System.out.println("Generating EPW...");
		
		// Write payload and calculate offsets:
		
		int headerLen = ((276 + 32 * epks.size()) + 127) & ~127;
		
		Map<String, DataSegment> stringPool = new HashMap<>();
		
		ByteArrayOutputStream bao = new ByteArrayOutputStream();

		DataSegment clientPackageNameOffset = poolString(clientPackageName, stringPool, bao);
		DataSegment clientOriginNameOffset = poolString(clientOriginName, stringPool, bao);
		DataSegment clientOriginVersionOffset = poolString(clientOriginVersion, stringPool, bao);
		DataSegment clientOriginVendorOffset = poolString(clientOriginVendor, stringPool, bao);
		DataSegment clientForkNameOffset = poolString(clientForkName, stringPool, bao);
		DataSegment clientForkVersionOffset = poolString(clientForkVersion, stringPool, bao);
		DataSegment clientForkVendorOffset = poolString(clientForkVendor, stringPool, bao);
		
		DataSegment splashMIMEOffset = poolString(splashMIME, stringPool, bao);
		DataSegment pressAnyKeyMIMEOffset = poolString(pressAnyKeyMIME, stringPool, bao);
		DataSegment crashMIMEOffset = poolString(crashMIME, stringPool, bao);
		DataSegment faviconMIMEOffset = poolString(faviconMIME, stringPool, bao);

		for(int i = 0, l = epks.size(); i < l; ++i) {
			EPKStruct struct = epks.get(i);
			struct.epkFilePath = poolString(struct.epkFilePathStr, stringPool, bao);
			struct.epkLoadPath = poolString(struct.epkLoadPathStr, stringPool, bao);
		}

		int metadataSegmentOffset = 0;
		int metadataSegmentLength = 0;
		if(!metadata.isEmpty()) {
			int cnt = metadata.size();
			metadataSegmentLength = 4 + cnt * 8;
			byte[] metaList = new byte[metadataSegmentLength];
			ByteBuffer metaListBuf = ByteBuffer.wrap(metaList).order(ByteOrder.LITTLE_ENDIAN);
			metaListBuf.putInt(cnt);
			for(Entry<String, String> etr : metadata.entrySet()) {
				DataSegment k = poolString(etr.getKey(), stringPool, bao);
				DataSegment v = poolString(etr.getValue(), stringPool, bao);
				metaListBuf.putInt(k.offset + headerLen);
				metaListBuf.putInt(k.length);
				metaListBuf.putInt(v.offset + headerLen);
				metaListBuf.putInt(v.length);
			}
			metadataSegmentOffset = bao.size();
			bao.write(metaList);
		}
		
		int splashDataOffset = bao.size();
		bao.write(splashBin);
		
		int pressAnyKeyDataOffset = bao.size();
		bao.write(pressAnyKeyBin);
		
		int crashDataOffset = bao.size();
		bao.write(crashBin);
		
		int faviconDataOffset = bao.size();
		bao.write(faviconBin);
		
		int loaderJSBinOffset = bao.size();
		bao.write(loaderJSBin);
		
		int loaderWASMBinOffset = bao.size();
		bao.write(loaderWASMBin);
		
		int JSPIUnavailableOffset = bao.size();
		bao.write(JSPIUnavailableCompressed);
		
		int eagruntimeJSOffset = bao.size();
		bao.write(eagruntimeJSCompressed);
		
		int classesWASMOffset = bao.size();
		bao.write(classesWASMCompressed);
		
		int classesDeobfTEADBGOffset = 0;
		if(classesDeobfTEADBGCompressed != null) {
			classesDeobfTEADBGOffset = bao.size();
			bao.write(classesDeobfTEADBGCompressed);
		}
		
		int classesDeobfWASMOffset = 0;
		if(classesDeobfWASMCompressed != null) {
			classesDeobfWASMOffset = bao.size();
			bao.write(classesDeobfWASMCompressed);
		}

		for(int i = 0, l = epks.size(); i < l; ++i) {
			EPKStruct struct = epks.get(i);
			struct.epkDataOffset = bao.size();
			bao.write(struct.epkDataCompressed);
		}
		
		byte[] finalPayload = bao.toByteArray();
		
		// Generate the fixed size header:
		
		byte[] epwHeaderDataArr = new byte[headerLen];
		ByteBuffer epwHeaderData = ByteBuffer.wrap(epwHeaderDataArr).order(ByteOrder.LITTLE_ENDIAN);
		
		epwHeaderData.put(new byte[] { 'E', 'A', 'G', '$', 'W', 'A', 'S', 'M' });

		epwHeaderData.putInt(8, 0); // Will be replaced with length
		epwHeaderData.putInt(12, 0); // Will be replaced with CRC32
		
		epwHeaderData.putShort(16, (short)1); // EPW version major
		epwHeaderData.putShort(18, (short)1); // EPW version minor

		// strings section

		epwHeaderData.putInt(20, clientVersionInteger); // Client update version integer

		epwHeaderData.putInt(24, clientPackageNameOffset.offset + headerLen); // Client update pkg name offset
		epwHeaderData.putInt(28, clientPackageNameOffset.length); // Client update pkg name length

		epwHeaderData.putInt(32, clientOriginNameOffset.offset + headerLen); // Client Origin Name String Offset
		epwHeaderData.putInt(36, clientOriginNameOffset.length); // Client Origin Name String Length

		epwHeaderData.putInt(40, clientOriginVersionOffset.offset + headerLen); // Client Origin Version String Offset
		epwHeaderData.putInt(44, clientOriginVersionOffset.length); // Client Origin Version String Length

		epwHeaderData.putInt(48, clientOriginVendorOffset.offset + headerLen); // Client Origin Vendor String Offset
		epwHeaderData.putInt(52, clientOriginVendorOffset.length); // Client Origin Vendor String Length

		epwHeaderData.putInt(56, clientForkNameOffset.offset + headerLen); // Client Fork Name String Offset
		epwHeaderData.putInt(60, clientForkNameOffset.length); // Client Fork Name String Length

		epwHeaderData.putInt(64, clientForkVersionOffset.offset + headerLen); // Client Fork Version String Offset
		epwHeaderData.putInt(68, clientForkVersionOffset.length); // Client Fork Version String Length

		epwHeaderData.putInt(72, clientForkVendorOffset.offset + headerLen); // Client Fork Vendor String Offset
		epwHeaderData.putInt(76, clientForkVendorOffset.length); // Client Fork Vendor String Length

		epwHeaderData.putInt(80, metadataSegmentOffset + headerLen); // Additional Metadata Offset
		epwHeaderData.putInt(84, metadataSegmentLength); // Additional Metadata Length

		// put timestamp

		epwHeaderData.putLong(88, System.currentTimeMillis());

		// put epk count

		epwHeaderData.putInt(96, epks.size());

		// non-compressed offsets section

		epwHeaderData.putInt(100, splashDataOffset + headerLen); // Splash image offset
		epwHeaderData.putInt(104, splashBin.length); // Splash image length
		epwHeaderData.putInt(108, splashMIMEOffset.offset + headerLen); // Splash MIME offset
		epwHeaderData.putInt(112, splashMIMEOffset.length); // Splash MIME length

		epwHeaderData.putInt(116, pressAnyKeyDataOffset + headerLen); // pressAnyKey image offset
		epwHeaderData.putInt(120, pressAnyKeyBin.length); // pressAnyKey image length
		epwHeaderData.putInt(124, pressAnyKeyMIMEOffset.offset + headerLen); // pressAnyKey MIME offset
		epwHeaderData.putInt(128, pressAnyKeyMIMEOffset.length); // pressAnyKey MIME length

		epwHeaderData.putInt(132, crashDataOffset + headerLen); // Crash image offset
		epwHeaderData.putInt(136, crashBin.length); // Crash image length
		epwHeaderData.putInt(140, crashMIMEOffset.offset + headerLen); // Crash MIME offset
		epwHeaderData.putInt(144, crashMIMEOffset.length); // Crash MIME length

		epwHeaderData.putInt(148, faviconDataOffset + headerLen); // favicon image offset
		epwHeaderData.putInt(152, faviconBin.length); // favicon image length
		epwHeaderData.putInt(156, faviconMIMEOffset.offset + headerLen); // favicon MIME offset
		epwHeaderData.putInt(160, faviconMIMEOffset.length); // favicon MIME length

		epwHeaderData.putInt(164, loaderJSBinOffset + headerLen); // Loader JS offset
		epwHeaderData.putInt(168, loaderJSBin.length); // Loader JS length
		epwHeaderData.putInt(172, 0); // reserved
		epwHeaderData.putInt(176, 0); // reserved

		epwHeaderData.putInt(180, loaderWASMBinOffset + headerLen); // Loader WASM offset
		epwHeaderData.putInt(184, loaderWASMBin.length); // Loader WASM length
		epwHeaderData.putInt(188, 0); // reserved
		epwHeaderData.putInt(192, 0); // reserved

		// compressed offsets section

		epwHeaderData.putInt(196, JSPIUnavailableOffset + headerLen); // JSPI unavailable screen offset
		epwHeaderData.putInt(200, JSPIUnavailableCompressed.length); // JSPI unavailable screen compressed length
		epwHeaderData.putInt(204, JSPIUnavailableBin.length); // JSPI unavailable screen uncompressed length
		epwHeaderData.putInt(208, 0); // reserved

		epwHeaderData.putInt(212, eagruntimeJSOffset + headerLen); // EagRuntime.js bin offset
		epwHeaderData.putInt(216, eagruntimeJSCompressed.length); // EagRuntime.js bin compressed length
		epwHeaderData.putInt(220, eagRuntimeJSBin.length); // EagRuntime.js bin uncompressed length
		epwHeaderData.putInt(224, 0); // reserved

		epwHeaderData.putInt(228, classesWASMOffset + headerLen); // classes.wasm bin offset
		epwHeaderData.putInt(232, classesWASMCompressed.length); // classes.wasm bin compressed length
		epwHeaderData.putInt(236, classesWASMBin.length); // classes.wasm bin uncompressed length
		epwHeaderData.putInt(240, 0); // reserved

		epwHeaderData.putInt(244, classesDeobfTEADBGOffset != 0 ? (classesDeobfTEADBGOffset + headerLen) : 0); // classes.wasm deobf data bin offset
		epwHeaderData.putInt(248, classesDeobfTEADBGCompressed != null ? classesDeobfTEADBGCompressed.length : 0); // classes.wasm deobf data compressed length
		epwHeaderData.putInt(252, classesDeobfTEADBGBin != null ? classesDeobfTEADBGBin.length : 0); // classes.wasm deobf data uncompressed length
		epwHeaderData.putInt(256, 0); // reserved

		epwHeaderData.putInt(260, classesDeobfWASMOffset != 0 ? (classesDeobfWASMOffset + headerLen) : 0); // classes.wasm deobfuscator bin offset
		epwHeaderData.putInt(264, classesDeobfWASMCompressed != null ? classesDeobfWASMCompressed.length : 0); // classes.wasm deobfuscator compressed length
		epwHeaderData.putInt(268, classesDeobfWASMBin != null ? classesDeobfWASMBin.length : 0); // classes.wasm deobfuscator uncompressed length
		epwHeaderData.putInt(272, 0); // reserved

		for(int i = 0, l = epks.size(); i < l; ++i) {
			EPKStruct epk = epks.get(i);
			int base = 276 + i * 32;
			epwHeaderData.putInt(base, epk.epkFilePath.offset + headerLen); // original EPK file path offset
			epwHeaderData.putInt(base + 4, epk.epkFilePath.length); // original EPK file path length
			epwHeaderData.putInt(base + 8, epk.epkLoadPath.length > 0 ? (epk.epkLoadPath.offset + headerLen) : 0); // load path offset
			epwHeaderData.putInt(base + 12, epk.epkLoadPath.length); // load path length
			epwHeaderData.putInt(base + 16, epk.epkDataOffset + headerLen); // compressed data offset
			epwHeaderData.putInt(base + 20, epk.epkDataCompressed.length); // compressed data length
			epwHeaderData.putInt(base + 24, epk.epkData.length); // uncompressed length
			epwHeaderData.putInt(base + 28, 0); // reserved
		}

		System.out.println();
		System.out.println("Calculating final CRC32...");
		
		CRC32 crc = new CRC32();
		crc.update(epwHeaderDataArr, 16, headerLen - 16);
		crc.update(finalPayload, 0, finalPayload.length);
		
		epwHeaderData.putInt(8, headerLen + finalPayload.length); // length
		epwHeaderData.putInt(12, (int)crc.getValue()); // CRC32

		File destFolder = new File(/*resent$gradleDirectoryFix, */args[args.length - 1]);

		System.out.println();
		System.out.println("Writing destination file...");

		File destFile = new File(destFolder, "assets.epw");
		try(OutputStream os = new FileOutputStream(destFile)) {
			os.write(epwHeaderDataArr);
			os.write(finalPayload);
		}

		System.out.println();
		System.out.println("Copying favicon.png...");

		try(OutputStream os = new FileOutputStream(new File(destFolder, "favicon.png"))) {
			os.write(faviconBin);
		}

		System.out.println();

		MakeOfflineDownload.main(new String[] {
				new File(resent$gradleDirectoryFix, offlineTemplate).getAbsolutePath(),
				new File(resent$gradleDirectoryFix, bootstrapJS).getAbsolutePath(),
				destFile.getAbsolutePath(),
				(new File(destFolder, downloadName)).getAbsolutePath()
		});

		System.out.println();
		System.out.println("Done!");
		System.out.println();
	}

	private static String getRequired(Properties props, String key) throws IOException {
		String ret = props.getProperty(key);
		if(ret == null) {
			throw new IOException("Required option is missing from EPW properties: " + key);
		}
		return ret.toString();
	}

	private static DataSegment poolString(String str, Map<String, DataSegment> pool, ByteArrayOutputStream bao) throws IOException {
		if(str == null || str.length() == 0) {
			return new DataSegment(0, 0);
		}
		DataSegment ret = pool.get(str);
		if(ret == null) {
			int idx = bao.size();
			byte[] dat = str.getBytes(StandardCharsets.UTF_8);
			ret = new DataSegment(idx, dat.length);
			pool.put(str, ret);
			bao.write(dat);
			bao.write(0);
			return ret;
		}else {
			return ret;
		}
	}

	private static byte[] getFileBytes(File file) throws IOException {
		byte[] bytes = new byte[(int)file.length()];
		int i = 0, j;
		try(InputStream is = new FileInputStream(file)) {
			while(i < bytes.length && (j = is.read(bytes, i, bytes.length - i)) != -1) {
				i += j;
			}
		}
		return bytes;
	}

	private static byte[] compressFileBytes(byte[] classesWASMUncompressed) throws IOException {
		ByteArrayOutputStream bao = new ByteArrayOutputStream();
		LZMA2Options opts = new LZMA2Options(LZMA2Options.PRESET_MAX);
		opts.setDictSize(33554432);
		try(OutputStream os = new XZOutputStream(bao, opts, 1)) {
			os.write(classesWASMUncompressed);
		}
		return bao.toByteArray();
	}

	private static byte[] makeEPK(File file, File tmp) throws IOException {
		try {
			Method meth = Class.forName("net.lax1dude.eaglercraft.v1_8.buildtools.workspace.CompilePackage").getDeclaredMethod("main", String[].class);
			meth.invoke(null, new Object[] { new String[] { file.getAbsolutePath(), tmp.getAbsolutePath(), "none" } });
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
		byte[] ret = getFileBytes(tmp);
		tmp.delete();
		return ret;
	}

}
