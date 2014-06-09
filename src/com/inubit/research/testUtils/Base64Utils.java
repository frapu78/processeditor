/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.testUtils;

/**
 *
 * @author uha
 */
import javax.mail.MessagingException;
import javax.mail.internet.MimeUtility;
import java.io.*;

public class Base64Utils {
    public static byte[] encode(byte[] b) throws MessagingException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStream b64os = MimeUtility.encode(baos, "base64");
        b64os.write(b);
        b64os.close();
        return baos.toByteArray();
     }

     public static byte[] decode(byte[] b) throws IOException, MessagingException  {
        ByteArrayInputStream bais = new ByteArrayInputStream(b);
        InputStream b64is = MimeUtility.decode(bais, "base64");
        byte[] tmp = new byte[b.length];
        int n = b64is.read(tmp);
        byte[] res = new byte[n];
        System.arraycopy(tmp, 0, res, 0, n);
        return res;
     }



    public static void main(String[] args) throws Exception {
        String test = "realhowto";

        byte res1[] = Base64Utils.encode(test.getBytes());
        System.out.println(test + " base64 -> " + java.util.Arrays.toString(res1));
        System.out.println(new String(res1));
        byte res2[] = Base64Utils.decode(res1);
        System.out.println("");
        System.out.println( java.util.Arrays.toString(res1) + " string --> "
          + new String(res2));

        /*
         * output
         * realhowto base64 ->
         *     [99, 109, 86, 104, 98, 71, 104, 118, 100, 51, 82, 118]
         *     cmVhbGhvd3Rv
         * [99, 109, 86, 104, 98, 71, 104, 118, 100, 51, 82, 118]
         *     string --> realhowto
         */
    }

}
