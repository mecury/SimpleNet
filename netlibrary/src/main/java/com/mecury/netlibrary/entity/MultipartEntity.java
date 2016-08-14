package com.mecury.netlibrary.entity;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.TextUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;

/**
 * Created by 海飞 on 2016/8/6.
 * POST报文格式请参考博客 : http://blog.csdn.net/bboyfeiyu/article/details/41863951.
 * <p>
 * Android中的多参数类型的Entity实体类,用户可以使用该类来上传文件、文本参数、二进制参数,
 * 不需要依赖于httpmime.jar来实现上传文件的功能.
 * </p>
 */
public class MultipartEntity implements HttpEntity {

    private final static char[] MULTIPART_CHARS = "-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

    /**
     * 换行符
     */
    private final String NEW_LINE_STR = "\r\n";
    private final String CONTENT_TYPE = "Content-Type: ";
    private final String CONTENT_DISPOSITION = "Content_Disposition: ";

    /**
     * 文本参数与字符集
     */
    private final String TYPE_TEXT_CHARSET = "text/plain; charset=UTF-8";

    /**
     * 字节流参数
     */
    private final String TYPE_OCTET_STREAM = "application/octet-stream";

    /**
     * 二进制参数
     */
    private final byte[] BINARY_ENCODING = "Content-Transfer-Encoding: binary\r\n\r\n".getBytes();

    /**
     * 文本参数
     */
    private final byte[] BIT_ENCODING = "Content-Transfer-Encoding: 8bit\r\n\r\n".getBytes();

    /**
     * 分隔符
     */
    private String mBoundary = null;

    /**
     * 输出流
     */
    private ByteArrayOutputStream mOutputStream = new ByteArrayOutputStream();

    public MultipartEntity(){
        this.mBoundary = generateBoundary();
    }

    /**
     * 生成分隔符
     */
    private final String generateBoundary() {
        final StringBuffer buf = new StringBuffer();
        final Random rand = new Random();
        for (int i = 0; i < 30; i++){
            buf.append(MULTIPART_CHARS[rand.nextInt(MULTIPART_CHARS.length)]);
        }
        return buf.toString();
    }

    /**
     * 参数开头的分隔符
     */
    private void writeFirstBoundary() throws IOException {
        mOutputStream.write(("--" + mBoundary + "\r\n").getBytes());
    }

    /**
     * 添加文本参数
     * @param paramName key
     * @param value
     */
    public void addStringPart(final String paramName, final String value){
        writeToOutputStream(paramName, value.getBytes(), TYPE_TEXT_CHARSET, BIT_ENCODING, "");
    }

    /**
     * 将数据写入到输入流中
     * @param paramName key
     * @param rawData
     * @param encodingBytes
     * @param type
     * @param fileName
     * @return
     */
    private void writeToOutputStream(String paramName, byte[] rawData, String type, byte[] encodingBytes, String fileName){
        try{
            writeFirstBoundary();
            mOutputStream.write(getContentDispositionBytes(paramName, fileName));
            mOutputStream.write((CONTENT_TYPE + type + NEW_LINE_STR).getBytes());
            mOutputStream.write(encodingBytes);
            mOutputStream.write(rawData);
            mOutputStream.write(NEW_LINE_STR.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 添加二进制参数，例如Bitmap的字节流数组
     * @param paramName key
     * @param rawData
     */
    private void addBinaryPart(String paramName, final byte[] rawData){
        writeToOutputStream(paramName, rawData, TYPE_OCTET_STREAM, BINARY_ENCODING, "no-file");
    }

    /**
     * 添加文件参数，可以实现文件上传的功能
     * @param key
     * @param file
     * @return
     */
    public void addFilePart(final String key, final File file){
        InputStream fin = null;
        try{
            fin = new FileInputStream(file);
            writeFirstBoundary();
            final String type = CONTENT_TYPE + TYPE_OCTET_STREAM + NEW_LINE_STR;
            mOutputStream.write(getContentDispositionBytes(key, file.getName()));
            mOutputStream.write(type.getBytes());
            mOutputStream.write(BINARY_ENCODING);

            final byte[] tmp = new byte[4096];
            int len = 0;
            while((len = fin.read(tmp)) != -1){
                mOutputStream.write(tmp, 0, len);
            }
            mOutputStream.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            closeSilently(fin);
        }
    }

    /**
     * 关闭
     * @param
     * @return
     */
    private void closeSilently(Closeable closeable){
        try{
            if (closeable != null){
                closeable.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置文件参数的第二行部分
     * @param
     * @return
     */
    private byte[] getContentDispositionBytes(String paramName, String fileName){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(CONTENT_DISPOSITION + "form-data; name=\"" + paramName + "\"");

        //文本参数没有fileName参数，设置为空即可
        if (!TextUtils.isEmpty(fileName)){
            stringBuilder.append("; filename=\"" + fileName +"\"");
        }
        return stringBuilder.append(NEW_LINE_STR).toString().getBytes();
    }

    @Override
    public boolean isRepeatable() {
        return false;
    }

    @Override
    public boolean isChunked() {
        return false;
    }

    @Override
    public long getContentLength() {
        return mOutputStream.toByteArray().length;
    }

    @Override
    public Header getContentType() {
        return new BasicHeader("Content-Type", "multipart/form-data; boundary=" + mBoundary);
    }

    @Override
    public Header getContentEncoding() {
        return null;
    }

    @Override
    public InputStream getContent() throws IOException, UnsupportedOperationException {
        return new ByteArrayInputStream(mOutputStream.toByteArray());
    }

    @Override
    public void writeTo(OutputStream outputstream) throws IOException {
        //参数最末尾的结束符
        final String endString = "--" + mBoundary + "--\r\n";
        //写入结束符
        mOutputStream.write(endString.getBytes());

        outputstream.write(mOutputStream.toByteArray());
    }

    @Override
    public boolean isStreaming() {
        return false;
    }

    @Override
    public void consumeContent() throws IOException {
        if (isStreaming()){
            throw new UnsupportedOperationException("Streaming entity does not implement #consumeContent()");
        }
    }
}
