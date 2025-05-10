package net.lax1dude.eaglercraft.v1_8.buildtools.workspace;

import java.math.BigInteger;
import java.security.SecureRandom;

public class KeyGen {

	private final static BigInteger one = new BigInteger("1");
	private final static SecureRandom random = new SecureRandom();

	public static void main(String[] args) {
		BigInteger p = BigInteger.probablePrime(1024, random);
		BigInteger q = BigInteger.probablePrime(1024, random);
		BigInteger phi = (p.subtract(one)).multiply(q.subtract(one));

		BigInteger modulus = p.multiply(q);
		BigInteger publicKey = new BigInteger("65537");
		BigInteger privateKey = publicKey.modInverse(phi);

		System.out.println("modulus: " + modulus.toString());
		System.out.println("secret: " + privateKey.toString());
	}
}
