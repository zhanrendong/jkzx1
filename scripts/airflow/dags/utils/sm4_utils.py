from gmssl.sm4 import CryptSM4, SM4_ENCRYPT, SM4_DECRYPT
import base64

KEY = 'KaYup#asD1%79iYu'
ENCODING_UTF8 = 'utf-8'


def encrypt_data_cbc(original_text):
    crypt_sm4 = CryptSM4()
    crypt_sm4.set_key(KEY.encode(encoding=ENCODING_UTF8), SM4_ENCRYPT)
    encrypt_data = crypt_sm4.crypt_ecb(original_text.encode(encoding=ENCODING_UTF8)).hex().encode(encoding=ENCODING_UTF8)
    return base64.b64encode(encrypt_data).decode(encoding=ENCODING_UTF8)


def decrypt_data_cbc(encrypted_text):
    encrypted_bytes = bytes.fromhex(base64.b64decode(encrypted_text).decode(encoding=ENCODING_UTF8))
    crypt_sm4 = CryptSM4()
    crypt_sm4.set_key(KEY.encode(encoding=ENCODING_UTF8), SM4_DECRYPT)
    decrypt_bytes = crypt_sm4.crypt_ecb(encrypted_bytes)
    return decrypt_bytes.decode(ENCODING_UTF8)


if __name__ == '__main__':
    password = '123456a.'
    encrypted_pass = encrypt_data_cbc(password)
    print('明文：[{}] 加密为：[{}]'.format(password, encrypted_pass))
    original_pass = decrypt_data_cbc(encrypted_pass)
    print('密文：[{}] 解密为：[{}]'.format(encrypted_pass, original_pass))
