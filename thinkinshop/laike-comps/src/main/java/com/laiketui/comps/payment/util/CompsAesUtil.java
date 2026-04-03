package com.laiketui.comps.payment.util;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * @author wangxian
 */
public class CompsAesUtil
{
    static final  int    KEY_LENGTH_BYTE = 32;
    static final  int    TAG_LENGTH_BIT  = 128;
    private final byte[] aesKey;

    public CompsAesUtil(byte[] key)
    {
        if (key.length != KEY_LENGTH_BYTE)
        {
            throw new IllegalArgumentException("无效的ApiV3Key，长度必须为32个字节");
        }
        this.aesKey = key;
    }

    /**
     * 微信v3回调信息解密
     * @param associatedData
     * @param nonce
     * @param ciphertext
     * @return
     * @throws GeneralSecurityException
     * @throws IOException
     */
    public String decryptToString(byte[] associatedData, byte[] nonce, String ciphertext) throws GeneralSecurityException
    {
        try
        {
            SecretKeySpec key = new SecretKeySpec(this.aesKey, "AES");
            GCMParameterSpec spec = new GCMParameterSpec(128, nonce);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(2, key, spec);
            cipher.updateAAD(associatedData);
            return new String(cipher.doFinal(Base64.getDecoder().decode(ciphertext)), StandardCharsets.UTF_8);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        } catch (InvalidAlgorithmParameterException | InvalidKeyException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
