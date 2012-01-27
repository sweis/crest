Crest: Crypto + REST
=============

This is just playing with hooking up a Crypto REST API.

Generate a 2048-bit RSA private key (without a passphrase):

	openssl genrsa -out privkey.pem 2048

Export the RSA public key in DER format:

	openssl rsa -in privkey.pem -pubout -outform DER -out pubkey.der

PUT a DER-encoded RSA public key on a server:

	curl -T pubkey.der http://hostname/crest/v1/publickey

GET a public key by the SHA-1 hash of its DER-encoded bytes:

	curl http://hostname/crest/v1/publickey/{keyHash}
