/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.testUtils;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.MessagingException;

/**
 *
 * @author uha
 */
public class Seed {
    
    private byte[] seed = new byte[40];

    public Seed() {
        this(40);
    }

    public Seed(int sizeInBytes) {
        seed = new byte[sizeInBytes];
        Random r = new Random();
        r.nextBytes(seed);
    }

    public Seed(Seed copyFrom) {
        setSeed(copyFrom.getSeed());
    }

    /**
     *
     * @param code The code has to be Base64 encoded
     * @throws Exception
     */
    public Seed(String code) throws IOException, MessagingException  {
        seed = Base64Utils.decode(code.getBytes());
    }

    byte[] getSeed() {
        return seed;
    }

    void setSeed(byte[] seed) {
        this.seed = seed;
    }



    byte[] hash(byte[] seed) throws NoSuchAlgorithmException {
        MessageDigest md;
        md = MessageDigest.getInstance("SHA-1");
        byte[] sha1hash;
        md.update(seed);
        sha1hash = md.digest();
        return sha1hash;
    }

    private void rehash() {
        try {
            setSeed(hash(seed));
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(Seed.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     *
     * @param numberDecissions
     * @return a number between 0 and numberDecissions-1
     */

    public int decide(int numberDecissions) {
        rehash();
        BigInteger b;
        b = new BigInteger(seed);
        BigInteger c = BigInteger.valueOf(numberDecissions);
        return b.mod(c).intValue(); 
    }

    @Override
    public String toString() {
        try {
            return new String(Base64Utils.encode(seed));
        } catch (Exception ex) {
            Logger.getLogger(Seed.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }



}
