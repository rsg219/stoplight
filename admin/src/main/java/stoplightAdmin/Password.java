package jnm219.admin;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Hashtable;
import java.util.Random;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.SecretKeyFactory;
import java.math.BigInteger;
import java.security.SecureRandom;

public class Password
{
    public static String getPassword() 
    {

        // Random number generator
        Random rand = new Random();
        // Hashtable
        Hashtable<Integer, String> ref = new Hashtable<Integer, String>();
        ref.put(0,"Dynamite");
        ref.put(1,"Napoleon");
        ref.put(2,"Pedro");
        ref.put(3,"Deb");
        ref.put(4,"Kipling");
        ref.put(5,"Rico");
        ref.put(6,"Summer");
        ref.put(7,"Trisha");
        ref.put(8,"LaFawnduh");
        ref.put(9,"Grandma");
        ref.put(10,"Rex");
        ref.put(11,"Don");
        ref.put(12,"AuntIlene");
        ref.put(13,"Spear");
        ref.put(14,"Liger");

        String rtVal = "";
        rtVal = ref.get(rand.nextInt(15));
        for (int i=0; i<3; i++) 
        {
            rtVal += rand.nextInt(10);
        }
        return rtVal;
    }

    // How the backend creates and salts the password

    public static byte [] encryptPw (String password,byte [] salt) throws NoSuchAlgorithmException, InvalidKeySpecException
    {
        int iterations= 1000;
        char[] chars = password.toCharArray();
        PBEKeySpec spec = new PBEKeySpec(chars, salt, iterations, 64 * 8);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte [] securedPw= skf.generateSecret(spec).getEncoded();
        return securedPw;
    } 

    public static byte [] getSalt() throws NoSuchAlgorithmException
    {
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        byte[] salt = new byte[16];
        sr.nextBytes(salt);
        return salt;
    }
 
    public static String toHex(byte[] array) throws NoSuchAlgorithmException
    {
        BigInteger bi = new BigInteger(1, array);
        String hex = bi.toString(16);
        int paddingLength = (array.length * 2) - hex.length();
        if(paddingLength > 0)
        {
            return String.format("%0"  +paddingLength + "d", 0) + hex;
        }else{
            return hex;
        }
    }
    
    static Hashtable<String,Integer> logged_in=new Hashtable<String,Integer>();
    public static int keyGenerator ()
    {
        Random rand = new Random();
        int  random = rand.nextInt(10000) + 1000;
        return random;
    }
 
}
