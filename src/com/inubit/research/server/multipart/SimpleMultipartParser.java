/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.multipart;

import com.inubit.research.server.errors.MultipartBoundaryNotFoundException;
import com.inubit.research.server.errors.MultipartInputSizeOverflowException;
import com.inubit.research.server.errors.MultipartParseException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class for parsing the multipart String that is created on form submission in browser
 * @author Felix
 */
public class SimpleMultipartParser {

    public static int MAX_INPUT_SIZE = 1024*1024*16; // Default to 16MB max size for multipart

    public MultiPartObject parseSource( InputStream is ) throws IOException,
            MultipartInputSizeOverflowException, MultipartBoundaryNotFoundException,
            MultipartParseException {
        StringBuilder b = new StringBuilder(1000);
        BufferedReader r = new BufferedReader(new InputStreamReader(is));
        String l;

        while ((l = r.readLine()) != null) {
            b.append(l);
            b.append("\n");
            if (b.length()>MAX_INPUT_SIZE) {
                throw new MultipartInputSizeOverflowException();
            }
        }
        return this.parseSource(b.toString());
    }

    public MultiPartObject parseSource(String multiPart) throws MultipartBoundaryNotFoundException, MultipartParseException {
        BufferedReader br = new BufferedReader(new StringReader(multiPart));
        try {
            String boundary = br.readLine();
            if (!boundary.startsWith("--")) {
                throw new MultipartBoundaryNotFoundException();
            }

            String[] parts = multiPart.split(boundary);
            MultiPartObject o = new MultiPartObject();
            
            for (String part : parts) {
                if (part.trim().equals("")) continue;
                
                Map<String, String> attributes = parseContentDisposition(part);
                String contentType = parseContentType(part);
                BufferedReader b = new BufferedReader(new StringReader(part.trim()));

                String line;
                //skip headers
                while((line = b.readLine()) != null) {
                    if (line.trim().equals("")) break;
                }

                //read content
                StringBuilder builder = new StringBuilder(500);
                while((line = b.readLine()) != null) {
                    builder.append(line);
                    builder.append("\n");
                }

                o.addItem(new MultiPartItem(attributes, builder.toString(), contentType));
            }

            return o;

        } catch (IOException e) {
            e.printStackTrace();
        }

        throw new MultipartParseException();
    }

   public byte[] parseItemContentAsByteArray( BufferedInputStream bis, String itemName ) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        ByteArrayOutputStream value = new ByteArrayOutputStream();
        int i;

        int lineBreakByte1 = "\n".getBytes()[0];
        int lineBreakByte2 = "\r".getBytes()[0];
        try {
            while ((i = bis.read()) !=  lineBreakByte1 && i != lineBreakByte2 && i > -1)
                bos.write(i);

            String boundary = bos.toString().trim();

            boolean found = false;
            while ((i = bis.read()) != -1 && !found) {
                bos = new ByteArrayOutputStream();
                bos.write(i);
                while ((i = bis.read()) != -1 && i != lineBreakByte1 && i != lineBreakByte2)
                    bos.write(i);

                String line = bos.toString().trim();

                if (line.startsWith("Content-Disposition") && line.contains("name=\"" + itemName + "\"")) {
                    found = true;
                    boolean leading = true;
                    while (!line.equals(boundary)) {
                        bos = new ByteArrayOutputStream();
                        while ((i = bis.read()) != -1 && i != lineBreakByte1 && i != lineBreakByte2)
                            bos.write(i);

                        line = bos.toString();

                        //only starts with here since e.g. Opera adds "--" at later boundaries
                        if (line.trim().startsWith(boundary))
                            break;
                        if (line.startsWith("Content-Type"))
                            continue;

                        //ignore leading blank lines
                        if ( leading && line.trim().equals(""))
                            continue;

                        else {
                            leading = false;
                            value.write(bos.toByteArray());
                            value.write(i);
                        }
                    }

                    return value.toByteArray();
                }
            }

        } catch (IOException ex) {
            System.err.println("#bytes: " + bos.size());
            ex.printStackTrace();
        }

        return null;
    }

    private Map<String, String> parseContentDisposition(String part) {
        Map<String, String> attributes = new HashMap<String, String>();

        Pattern p = Pattern.compile("Content-Disposition:(.+?)$", Pattern.MULTILINE);
        Matcher m = p.matcher(part);

        if (m.find()) {
            String[] atts = m.group(1).split(";");

            for (String attr : atts) {
                attr = attr.trim();
                if (attr.matches(".+=.+")) {
                    attributes.put(attr.split("=")[0], attr.split("=")[1].replaceAll("\"", "").trim());
                } else {
                    attributes.put(attr, null);
                }
            }
        }
        return attributes;
    }

    private String parseContentType( String part ) {
        Pattern p = Pattern.compile("Content-Type:(.+?)$", Pattern.MULTILINE);
        Matcher m = p.matcher(part);

        if (m.find())
            return m.group(1).trim();

        return null;
    }
}
