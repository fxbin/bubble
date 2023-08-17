package cn.fxbin.bubble.fireworks.core.util;

import cn.fxbin.bubble.fireworks.core.exception.UtilException;
import lombok.experimental.UtilityClass;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.Base64;

/**
 * RsaCryptoUtils
 *
 * @author fxbin
 * @version v1.0
 * @since 2021/4/30 9:55
 */
@UtilityClass
public class RsaUtils {

    public String encryptOAEP(String message, X509Certificate certificate)
            throws IllegalBlockSizeException {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, certificate.getPublicKey());

            byte[] data = message.getBytes(StandardCharsets.UTF_8);
            byte[] ciphertext = cipher.doFinal(data);
            return Base64.getEncoder().encodeToString(ciphertext);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new UtilException("当前Java环境不支持RSA v1.5/OAEP", e);
        } catch (InvalidKeyException e) {
            throw new IllegalArgumentException("无效的证书", e);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new IllegalBlockSizeException("加密原串的长度不能超过214字节");
        }
    }

    public String decryptOAEP(String ciphertext, PrivateKey privateKey)
            throws BadPaddingException {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);

            byte[] data = Base64.getDecoder().decode(ciphertext);
            return new String(cipher.doFinal(data), StandardCharsets.UTF_8);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException e) {
            throw new UtilException("当前Java环境不支持RSA v1.5/OAEP", e);
        } catch (InvalidKeyException e) {
            throw new IllegalArgumentException("无效的私钥", e);
        } catch (BadPaddingException | IllegalBlockSizeException e) {
            throw new BadPaddingException("解密失败");
        }
    }

    /**
     * encryptByPrivateKey
     *
     * <p>
     *     私钥签名
     * </p>
     *
     * @since 2021/4/30 10:29
     * @param data 需要加密的数据
     * @param privateKey 私钥
     * @return java.lang.String 加密后的数据
     */
    public String encryptByPrivateKey(String data, PrivateKey privateKey) {
        try {
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(signature.sign());
        } catch (NoSuchAlgorithmException e) {
            throw new UtilException("当前Java环境不支持SHA256withRSA", e);
        } catch (SignatureException e) {
            throw new UtilException("签名计算失败", e);
        } catch (InvalidKeyException e) {
            throw new UtilException("无效的私钥", e);
        }
    }

}
