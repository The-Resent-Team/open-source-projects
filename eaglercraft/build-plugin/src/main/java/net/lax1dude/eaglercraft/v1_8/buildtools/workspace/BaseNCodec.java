package net.lax1dude.eaglercraft.v1_8.buildtools.workspace;

import java.nio.charset.Charset;
import java.util.Arrays;

public abstract class BaseNCodec {

	static enum CodecPolicy {
		STRICT, LENIANT;
	}

	/**
	 * Holds thread context so classes can be thread-safe.
	 *
	 * This class is not itself thread-safe; each thread must allocate its own copy.
	 *
	 * @since 1.7
	 */
	static class Context {

		/**
		 * Place holder for the bytes we're dealing with for our based logic. Bitwise
		 * operations store and extract the encoding or decoding from this variable.
		 */
		int ibitWorkArea;

		/**
		 * Place holder for the bytes we're dealing with for our based logic. Bitwise
		 * operations store and extract the encoding or decoding from this variable.
		 */
		long lbitWorkArea;

		/**
		 * Buffer for streaming.
		 */
		byte[] buffer;

		/**
		 * Position where next character should be written in the buffer.
		 */
		int pos;

		/**
		 * Position where next character should be read from the buffer.
		 */
		int readPos;

		/**
		 * Boolean flag to indicate the EOF has been reached. Once EOF has been reached,
		 * this object becomes useless, and must be thrown away.
		 */
		boolean eof;

		/**
		 * Variable tracks how many characters have been written to the current line.
		 * Only used when encoding. We use it to make sure each encoded line never goes
		 * beyond lineLength (if lineLength &gt; 0).
		 */
		int currentLinePos;

		/**
		 * Writes to the buffer only occur after every 3/5 reads when encoding, and
		 * every 4/8 reads when decoding. This variable helps track that.
		 */
		int modulus;

		Context() {
		}

		/**
		 * Returns a String useful for debugging (especially within a debugger.)
		 *
		 * @return a String useful for debugging.
		 */
		@SuppressWarnings("boxing") // OK to ignore boxing here
		@Override
		public String toString() {
			return String.format(
					"%s[buffer=%s, currentLinePos=%s, eof=%s, ibitWorkArea=%s, lbitWorkArea=%s, "
							+ "modulus=%s, pos=%s, readPos=%s]",
					this.getClass().getSimpleName(), Arrays.toString(buffer), currentLinePos, eof, ibitWorkArea,
					lbitWorkArea, modulus, pos, readPos);
		}
	}

	/**
	 * EOF
	 *
	 * @since 1.7
	 */
	static final int EOF = -1;

	/**
	 * MIME chunk size per RFC 2045 section 6.8.
	 *
	 * <p>
	 * The {@value} character limit does not count the trailing CRLF, but counts all
	 * other characters, including any equal signs.
	 * </p>
	 *
	 * @see <a href="http://www.ietf.org/rfc/rfc2045.txt">RFC 2045 section 6.8</a>
	 */
	public static final int MIME_CHUNK_SIZE = 76;

	/**
	 * PEM chunk size per RFC 1421 section 4.3.2.4.
	 *
	 * <p>
	 * The {@value} character limit does not count the trailing CRLF, but counts all
	 * other characters, including any equal signs.
	 * </p>
	 *
	 * @see <a href="http://tools.ietf.org/html/rfc1421">RFC 1421 section
	 *      4.3.2.4</a>
	 */
	public static final int PEM_CHUNK_SIZE = 64;

	private static final int DEFAULT_BUFFER_RESIZE_FACTOR = 2;

	/**
	 * Defines the default buffer size - currently {@value} - must be large enough
	 * for at least one encoded block+separator
	 */
	private static final int DEFAULT_BUFFER_SIZE = 8192;

	/**
	 * The maximum size buffer to allocate.
	 *
	 * <p>
	 * This is set to the same size used in the JDK {@code java.util.ArrayList}:
	 * </p>
	 * <blockquote> Some VMs reserve some header words in an array. Attempts to
	 * allocate larger arrays may result in OutOfMemoryError: Requested array size
	 * exceeds VM limit. </blockquote>
	 */
	private static final int MAX_BUFFER_SIZE = Integer.MAX_VALUE - 8;

	/** Mask used to extract 8 bits, used in decoding bytes */
	protected static final int MASK_8BITS = 0xff;

	/**
	 * Byte used to pad output.
	 */
	protected static final byte PAD_DEFAULT = '='; // Allow static access to default

	/**
	 * Chunk separator per RFC 2045 section 2.1.
	 *
	 * @see <a href="http://www.ietf.org/rfc/rfc2045.txt">RFC 2045 section 2.1</a>
	 */
	static final byte[] CHUNK_SEPARATOR = { '\r', '\n' };

	/**
	 * Compares two {@code int} values numerically treating the values as unsigned.
	 * Taken from JDK 1.8.
	 *
	 * <p>
	 * TODO: Replace with JDK 1.8 Integer::compareUnsigned(int, int).
	 * </p>
	 *
	 * @param x the first {@code int} to compare
	 * @param y the second {@code int} to compare
	 * @return the value {@code 0} if {@code x == y}; a value less than {@code 0} if
	 *         {@code x < y} as unsigned values; and a value greater than {@code 0}
	 *         if {@code x > y} as unsigned values
	 */
	private static int compareUnsigned(final int xx, final int yy) {
		int x = xx + Integer.MIN_VALUE;
		int y = yy + Integer.MIN_VALUE;
		return (x < y) ? -1 : ((x == y) ? 0 : 1);
	}

	/**
	 * Create a positive capacity at least as large the minimum required capacity.
	 * If the minimum capacity is negative then this throws an OutOfMemoryError as
	 * no array can be allocated.
	 *
	 * @param minCapacity the minimum capacity
	 * @return the capacity
	 * @throws OutOfMemoryError if the {@code minCapacity} is negative
	 */
	private static int createPositiveCapacity(final int minCapacity) {
		if (minCapacity < 0) {
			// overflow
			throw new OutOfMemoryError("Unable to allocate array size: " + (minCapacity & 0xffffffffL));
		}
		// This is called when we require buffer expansion to a very big array.
		// Use the conservative maximum buffer size if possible, otherwise the biggest
		// required.
		//
		// Note: In this situation JDK 1.8 java.util.ArrayList returns
		// Integer.MAX_VALUE.
		// This excludes some VMs that can exceed MAX_BUFFER_SIZE but not allocate a
		// full
		// Integer.MAX_VALUE length array.
		// The result is that we may have to allocate an array of this size more than
		// once if
		// the capacity must be expanded again.
		return (minCapacity > MAX_BUFFER_SIZE) ? minCapacity : MAX_BUFFER_SIZE;
	}

	/**
	 * Gets a copy of the chunk separator per RFC 2045 section 2.1.
	 *
	 * @return the chunk separator
	 * @see <a href="http://www.ietf.org/rfc/rfc2045.txt">RFC 2045 section 2.1</a>
	 * @since 1.15
	 */
	public static byte[] getChunkSeparator() {
		return CHUNK_SEPARATOR.clone();
	}

	/**
	 * Checks if a byte value is whitespace or not. Whitespace is taken to mean:
	 * space, tab, CR, LF
	 * 
	 * @param byteToCheck the byte to check
	 * @return true if byte is whitespace, false otherwise
	 */
	protected static boolean isWhiteSpace(final byte byteToCheck) {
		switch (byteToCheck) {
		case ' ':
		case '\n':
		case '\r':
		case '\t':
			return true;
		default:
			return false;
		}
	}

	/**
	 * Increases our buffer by the {@link #DEFAULT_BUFFER_RESIZE_FACTOR}.
	 * 
	 * @param context     the context to be used
	 * @param minCapacity the minimum required capacity
	 * @return the resized byte[] buffer
	 * @throws OutOfMemoryError if the {@code minCapacity} is negative
	 */
	private static byte[] resizeBuffer(final Context context, final int minCapacity) {
		// Overflow-conscious code treats the min and new capacity as unsigned.
		final int oldCapacity = context.buffer.length;
		int newCapacity = oldCapacity * DEFAULT_BUFFER_RESIZE_FACTOR;
		if (compareUnsigned(newCapacity, minCapacity) < 0) {
			newCapacity = minCapacity;
		}
		if (compareUnsigned(newCapacity, MAX_BUFFER_SIZE) > 0) {
			newCapacity = createPositiveCapacity(minCapacity);
		}

		final byte[] b = new byte[newCapacity];
		System.arraycopy(context.buffer, 0, b, 0, context.buffer.length);
		context.buffer = b;
		return b;
	}

	/**
	 * @deprecated Use {@link #pad}. Will be removed in 2.0.
	 */
	@Deprecated
	protected final byte PAD = PAD_DEFAULT; // instance variable just in case it needs to vary later

	protected final byte pad; // instance variable just in case it needs to vary later

	/**
	 * Number of bytes in each full block of unencoded data, e.g. 4 for Base64 and 5
	 * for Base32
	 */
	private final int unencodedBlockSize;

	/**
	 * Number of bytes in each full block of encoded data, e.g. 3 for Base64 and 8
	 * for Base32
	 */
	private final int encodedBlockSize;

	/**
	 * Chunksize for encoding. Not used when decoding. A value of zero or less
	 * implies no chunking of the encoded data. Rounded down to nearest multiple of
	 * encodedBlockSize.
	 */
	protected final int lineLength;

	/**
	 * Size of chunk separator. Not used unless {@link #lineLength} &gt; 0.
	 */
	private final int chunkSeparatorLength;

	/**
	 * Defines the decoding behavior when the input bytes contain leftover trailing
	 * bits that cannot be created by a valid encoding. These can be bits that are
	 * unused from the final character or entire characters. The default mode is
	 * lenient decoding. Set this to {@code true} to enable strict decoding.
	 * <ul>
	 * <li>Lenient: Any trailing bits are composed into 8-bit bytes where possible.
	 * The remainder are discarded.
	 * <li>Strict: The decoding will raise an {@link IllegalArgumentException} if
	 * trailing bits are not part of a valid encoding. Any unused bits from the
	 * final character must be zero. Impossible counts of entire final characters
	 * are not allowed.
	 * </ul>
	 *
	 * <p>
	 * When strict decoding is enabled it is expected that the decoded bytes will be
	 * re-encoded to a byte array that matches the original, i.e. no changes occur
	 * on the final character. This requires that the input bytes use the same
	 * padding and alphabet as the encoder.
	 */
	private final CodecPolicy decodingPolicy;

	/**
	 * Note {@code lineLength} is rounded down to the nearest multiple of the
	 * encoded block size. If {@code chunkSeparatorLength} is zero, then chunking is
	 * disabled.
	 * 
	 * @param unencodedBlockSize   the size of an unencoded block (e.g. Base64 = 3)
	 * @param encodedBlockSize     the size of an encoded block (e.g. Base64 = 4)
	 * @param lineLength           if &gt; 0, use chunking with a length
	 *                             {@code lineLength}
	 * @param chunkSeparatorLength the chunk separator length, if relevant
	 */
	protected BaseNCodec(final int unencodedBlockSize, final int encodedBlockSize, final int lineLength,
			final int chunkSeparatorLength) {
		this(unencodedBlockSize, encodedBlockSize, lineLength, chunkSeparatorLength, PAD_DEFAULT);
	}

	/**
	 * Note {@code lineLength} is rounded down to the nearest multiple of the
	 * encoded block size. If {@code chunkSeparatorLength} is zero, then chunking is
	 * disabled.
	 * 
	 * @param unencodedBlockSize   the size of an unencoded block (e.g. Base64 = 3)
	 * @param encodedBlockSize     the size of an encoded block (e.g. Base64 = 4)
	 * @param lineLength           if &gt; 0, use chunking with a length
	 *                             {@code lineLength}
	 * @param chunkSeparatorLength the chunk separator length, if relevant
	 * @param pad                  byte used as padding byte.
	 */
	protected BaseNCodec(final int unencodedBlockSize, final int encodedBlockSize, final int lineLength,
			final int chunkSeparatorLength, final byte pad) {
		this(unencodedBlockSize, encodedBlockSize, lineLength, chunkSeparatorLength, pad, CodecPolicy.LENIANT);
	}

	/**
	 * Note {@code lineLength} is rounded down to the nearest multiple of the
	 * encoded block size. If {@code chunkSeparatorLength} is zero, then chunking is
	 * disabled.
	 * 
	 * @param unencodedBlockSize   the size of an unencoded block (e.g. Base64 = 3)
	 * @param encodedBlockSize     the size of an encoded block (e.g. Base64 = 4)
	 * @param lineLength           if &gt; 0, use chunking with a length
	 *                             {@code lineLength}
	 * @param chunkSeparatorLength the chunk separator length, if relevant
	 * @param pad                  byte used as padding byte.
	 * @param decodingPolicy       Decoding policy.
	 * @since 1.15
	 */
	protected BaseNCodec(final int unencodedBlockSize, final int encodedBlockSize, final int lineLength,
			final int chunkSeparatorLength, final byte pad, final CodecPolicy decodingPolicy) {
		this.unencodedBlockSize = unencodedBlockSize;
		this.encodedBlockSize = encodedBlockSize;
		final boolean useChunking = lineLength > 0 && chunkSeparatorLength > 0;
		this.lineLength = useChunking ? (lineLength / encodedBlockSize) * encodedBlockSize : 0;
		this.chunkSeparatorLength = chunkSeparatorLength;
		this.pad = pad;
		this.decodingPolicy = decodingPolicy;
	}

	/**
	 * Returns the amount of buffered data available for reading.
	 *
	 * @param context the context to be used
	 * @return The amount of buffered data available for reading.
	 */
	int available(final Context context) { // package protected for access from I/O streams
		return context.buffer != null ? context.pos - context.readPos : 0;
	}

	/**
	 * Tests a given byte array to see if it contains any characters within the
	 * alphabet or PAD.
	 *
	 * Intended for use in checking line-ending arrays
	 *
	 * @param arrayOctet byte array to test
	 * @return {@code true} if any byte is a valid character in the alphabet or PAD;
	 *         {@code false} otherwise
	 */
	protected boolean containsAlphabetOrPad(final byte[] arrayOctet) {
		if (arrayOctet == null) {
			return false;
		}
		for (final byte element : arrayOctet) {
			if (pad == element || isInAlphabet(element)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Decodes a byte[] containing characters in the Base-N alphabet.
	 *
	 * @param pArray A byte array containing Base-N character data
	 * @return a byte array containing binary data
	 */
	public byte[] decode(final byte[] pArray) {
		if (pArray == null || pArray.length == 0) {
			return pArray;
		}
		final Context context = new Context();
		decode(pArray, 0, pArray.length, context);
		decode(pArray, 0, EOF, context); // Notify decoder of EOF.
		final byte[] result = new byte[context.pos];
		readResults(result, 0, result.length, context);
		return result;
	}

	// package protected for access from I/O streams
	abstract void decode(byte[] pArray, int i, int length, Context context);

	/**
	 * Decodes an Object using the Base-N algorithm. This method is provided in
	 * order to satisfy the requirements of the Decoder interface, and will throw a
	 * DecoderException if the supplied object is not of type byte[] or String.
	 *
	 * @param obj Object to decode
	 * @return An object (of type byte[]) containing the binary data which
	 *         corresponds to the byte[] or String supplied.
	 * @throws DecoderException if the parameter supplied is not of type byte[]
	 */
	public Object decode(final Object obj) {
		if (obj instanceof byte[]) {
			return decode((byte[]) obj);
		} else if (obj instanceof String) {
			return decode((String) obj);
		} else {
			return null;
		}
	}

	/**
	 * Decodes a String containing characters in the Base-N alphabet.
	 *
	 * @param pArray A String containing Base-N character data
	 * @return a byte array containing binary data
	 */
	public byte[] decode(final String pArray) {
		return decode(pArray.getBytes(Charset.forName("UTF-8")));
	}

	/**
	 * Encodes a byte[] containing binary data, into a byte[] containing characters
	 * in the alphabet.
	 *
	 * @param pArray a byte array containing binary data
	 * @return A byte array containing only the base N alphabetic character data
	 */
	public byte[] encode(final byte[] pArray) {
		if (pArray == null || pArray.length == 0) {
			return pArray;
		}
		return encode(pArray, 0, pArray.length);
	}

	/**
	 * Encodes a byte[] containing binary data, into a byte[] containing characters
	 * in the alphabet.
	 *
	 * @param pArray a byte array containing binary data
	 * @param offset initial offset of the subarray.
	 * @param length length of the subarray.
	 * @return A byte array containing only the base N alphabetic character data
	 * @since 1.11
	 */
	public byte[] encode(final byte[] pArray, final int offset, final int length) {
		if (pArray == null || pArray.length == 0) {
			return pArray;
		}
		final Context context = new Context();
		encode(pArray, offset, length, context);
		encode(pArray, offset, EOF, context); // Notify encoder of EOF.
		final byte[] buf = new byte[context.pos - context.readPos];
		readResults(buf, 0, buf.length, context);
		return buf;
	}

	// package protected for access from I/O streams
	abstract void encode(byte[] pArray, int i, int length, Context context);

	/**
	 * Encodes an Object using the Base-N algorithm. This method is provided in
	 * order to satisfy the requirements of the Encoder interface, and will throw an
	 * EncoderException if the supplied object is not of type byte[].
	 *
	 * @param obj Object to encode
	 * @return An object (of type byte[]) containing the Base-N encoded data which
	 *         corresponds to the byte[] supplied.
	 * @throws EncoderException if the parameter supplied is not of type byte[]
	 */
	public Object encode(final Object obj) {
		return encode((byte[]) obj);
	}

	/**
	 * Encodes a byte[] containing binary data, into a String containing characters
	 * in the appropriate alphabet. Uses UTF8 encoding.
	 *
	 * @param pArray a byte array containing binary data
	 * @return String containing only character data in the appropriate alphabet.
	 * @since 1.5 This is a duplicate of {@link #encodeToString(byte[])}; it was
	 *        merged during refactoring.
	 */
	public String encodeAsString(final byte[] pArray) {
		return new String(encode(pArray), Charset.forName("UTF-8"));
	}

	/**
	 * Encodes a byte[] containing binary data, into a String containing characters
	 * in the Base-N alphabet. Uses UTF8 encoding.
	 *
	 * @param pArray a byte array containing binary data
	 * @return A String containing only Base-N character data
	 */
	public String encodeToString(final byte[] pArray) {
		return new String(encode(pArray), Charset.forName("UTF-8"));
	}

	/**
	 * Ensure that the buffer has room for {@code size} bytes
	 *
	 * @param size    minimum spare space required
	 * @param context the context to be used
	 * @return the buffer
	 */
	protected byte[] ensureBufferSize(final int size, final Context context) {
		if (context.buffer == null) {
			context.buffer = new byte[Math.max(size, getDefaultBufferSize())];
			context.pos = 0;
			context.readPos = 0;

			// Overflow-conscious:
			// x + y > z == x + y - z > 0
		} else if (context.pos + size - context.buffer.length > 0) {
			return resizeBuffer(context, context.pos + size);
		}
		return context.buffer;
	}

	/**
	 * Returns the decoding behavior policy.
	 * 
	 * <p>
	 * The default is lenient. If the decoding policy is strict, then decoding will
	 * raise an {@link IllegalArgumentException} if trailing bits are not part of a
	 * valid encoding. Decoding will compose trailing bits into 8-bit bytes and
	 * discard the remainder.
	 * </p>
	 *
	 * @return true if using strict decoding
	 * @since 1.15
	 */
	public CodecPolicy getCodecPolicy() {
		return decodingPolicy;
	}

	/**
	 * Get the default buffer size. Can be overridden.
	 *
	 * @return the default buffer size.
	 */
	protected int getDefaultBufferSize() {
		return DEFAULT_BUFFER_SIZE;
	}

	/**
	 * Calculates the amount of space needed to encode the supplied array.
	 *
	 * @param pArray byte[] array which will later be encoded
	 *
	 * @return amount of space needed to encoded the supplied array. Returns a long
	 *         since a max-len array will require &gt; Integer.MAX_VALUE
	 */
	public long getEncodedLength(final byte[] pArray) {
		// Calculate non-chunked size - rounded up to allow for padding
		// cast to long is needed to avoid possibility of overflow
		long len = ((pArray.length + unencodedBlockSize - 1) / unencodedBlockSize) * (long) encodedBlockSize;
		if (lineLength > 0) { // We're using chunking
			// Round up to nearest multiple
			len += ((len + lineLength - 1) / lineLength) * chunkSeparatorLength;
		}
		return len;
	}

	/**
	 * Returns true if this object has buffered data for reading.
	 *
	 * @param context the context to be used
	 * @return true if there is data still available for reading.
	 */
	boolean hasData(final Context context) { // package protected for access from I/O streams
		return context.buffer != null;
	}

	/**
	 * Returns whether or not the {@code octet} is in the current alphabet. Does not
	 * allow whitespace or pad.
	 *
	 * @param value The value to test
	 *
	 * @return {@code true} if the value is defined in the current alphabet,
	 *         {@code false} otherwise.
	 */
	protected abstract boolean isInAlphabet(byte value);

	/**
	 * Tests a given byte array to see if it contains only valid characters within
	 * the alphabet. The method optionally treats whitespace and pad as valid.
	 *
	 * @param arrayOctet byte array to test
	 * @param allowWSPad if {@code true}, then whitespace and PAD are also allowed
	 *
	 * @return {@code true} if all bytes are valid characters in the alphabet or if
	 *         the byte array is empty; {@code false}, otherwise
	 */
	public boolean isInAlphabet(final byte[] arrayOctet, final boolean allowWSPad) {
		for (final byte octet : arrayOctet) {
			if (!isInAlphabet(octet) && (!allowWSPad || (octet != pad) && !isWhiteSpace(octet))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Tests a given String to see if it contains only valid characters within the
	 * alphabet. The method treats whitespace and PAD as valid.
	 *
	 * @param basen String to test
	 * @return {@code true} if all characters in the String are valid characters in
	 *         the alphabet or if the String is empty; {@code false}, otherwise
	 * @see #isInAlphabet(byte[], boolean)
	 */
	public boolean isInAlphabet(final String basen) {
		return isInAlphabet(basen.getBytes(Charset.forName("UTF-8")), true);
	}

	/**
	 * Returns true if decoding behavior is strict. Decoding will raise an
	 * {@link IllegalArgumentException} if trailing bits are not part of a valid
	 * encoding.
	 *
	 * <p>
	 * The default is false for lenient decoding. Decoding will compose trailing
	 * bits into 8-bit bytes and discard the remainder.
	 * </p>
	 *
	 * @return true if using strict decoding
	 * @since 1.15
	 */
	public boolean isStrictDecoding() {
		return decodingPolicy == CodecPolicy.STRICT;
	}

	/**
	 * Extracts buffered data into the provided byte[] array, starting at position
	 * bPos, up to a maximum of bAvail bytes. Returns how many bytes were actually
	 * extracted.
	 * <p>
	 * Package protected for access from I/O streams.
	 *
	 * @param b       byte[] array to extract the buffered data into.
	 * @param bPos    position in byte[] array to start extraction at.
	 * @param bAvail  amount of bytes we're allowed to extract. We may extract fewer
	 *                (if fewer are available).
	 * @param context the context to be used
	 * @return The number of bytes successfully extracted into the provided byte[]
	 *         array.
	 */
	int readResults(final byte[] b, final int bPos, final int bAvail, final Context context) {
		if (context.buffer != null) {
			final int len = Math.min(available(context), bAvail);
			System.arraycopy(context.buffer, context.readPos, b, bPos, len);
			context.readPos += len;
			if (context.readPos >= context.pos) {
				context.buffer = null; // so hasData() will return false, and this method can return -1
			}
			return len;
		}
		return context.eof ? EOF : 0;
	}
}