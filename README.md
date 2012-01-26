Crest: Keyczar crypto + REST
=============

Playing with hooking up Keyczar to a REST API.

Generate a 2048-bit RSA private key (without a passphrase):

	openssl genrsa -out privkey.pem 2048

Export the RSA public key in DER format:

	openssl rsa -in privkey.pem -pubout -outform DER -out pubkey.der

PUT the DER-formatted RSA public key in a locally running server:

	curl -T pubkey.der http://localhost:8080/crest/v1/publickey
