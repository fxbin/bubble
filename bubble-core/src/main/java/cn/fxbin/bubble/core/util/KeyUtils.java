package cn.fxbin.bubble.core.util;

import cn.fxbin.bubble.core.exception.UtilException;
import lombok.experimental.UtilityClass;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

/**
 * KeyUtils
 *
 * @author fxbin
 * @version v1.0
 * @since 2021/4/29 17:55
 */
@UtilityClass
public class KeyUtils {

    /**
     * loadPrivateKey
     *
     * <p>
     *     加载秘钥
     * </p>
     *
     * @since 2021/4/29 18:41
     * @param inputStream 密钥
     * @return java.security.PrivateKey
     */
    public PrivateKey loadPrivateKey(InputStream inputStream) {
        try {
            ByteArrayOutputStream array = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                array.write(buffer, 0, length);
            }

            String privateKey = array.toString("utf-8")
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s+", "");

            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePrivate(
                    new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKey)));
        } catch (NoSuchAlgorithmException e) {
            throw new UtilException("当前Java环境不支持RSA", e);
        } catch (InvalidKeySpecException e) {
            throw new UtilException("无效的密钥格式");
        } catch (IOException e) {
            throw new UtilException("无效的密钥");
        }
    }


    /**
     * getCertificate
     *
     * <p>
     *     获取证书
     * </p>
     *
     * @since 2021/4/29 17:56
     * @param inputStream 证书文件
     * @return java.security.cert.X509Certificate
     */
    public X509Certificate loadCertificate(InputStream inputStream) {
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X509");
            X509Certificate cert = (X509Certificate)cf.generateCertificate(inputStream);
            cert.checkValidity();
            return cert;
        } catch (CertificateExpiredException var3) {
            throw new UtilException("证书已过期", var3);
        } catch (CertificateNotYetValidException var4) {
            throw new UtilException("证书尚未生效", var4);
        } catch (CertificateException var5) {
            throw new UtilException("无效的证书", var5);
        }
    }

}
